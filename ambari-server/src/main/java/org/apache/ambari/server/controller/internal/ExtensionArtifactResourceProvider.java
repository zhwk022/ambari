/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.controller.internal;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.rits.cloning.Cloner;
import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.StackAccessException;
import org.apache.ambari.server.StaticallyInject;
import org.apache.ambari.server.api.services.AmbariMetaInfo;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.spi.NoSuchParentResourceException;
import org.apache.ambari.server.controller.spi.NoSuchResourceException;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.RequestStatus;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.ResourceAlreadyExistsException;
import org.apache.ambari.server.controller.spi.SystemException;
import org.apache.ambari.server.controller.spi.UnsupportedPropertyException;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.apache.ambari.server.state.ServiceInfo;
import org.apache.ambari.server.state.ExtensionInfo;
import org.apache.ambari.server.state.kerberos.KerberosDescriptor;
import org.apache.ambari.server.state.kerberos.KerberosDescriptorFactory;
import org.apache.ambari.server.state.kerberos.KerberosServiceDescriptor;
import org.apache.ambari.server.state.kerberos.KerberosServiceDescriptorFactory;
import org.apache.ambari.server.state.stack.Metric;
import org.apache.ambari.server.state.stack.MetricDefinition;
import org.apache.ambari.server.state.stack.WidgetLayout;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Provider for extension and extension service artifacts.
 * Artifacts contain an artifact name as the PK and artifact data in the form of
 * a map which is the content of the artifact.
 * <p>
 * An example of an artifact is a kerberos descriptor.
 * <p>
 * Extension artifacts are part of the extension definition and therefore can't
 * be created, updated or deleted.
 */
@StaticallyInject
public class ExtensionArtifactResourceProvider extends AbstractControllerResourceProvider {
  /**
   * extension name
   */
  public static final String EXTENSION_NAME_PROPERTY_ID =
      PropertyHelper.getPropertyId("Artifacts", "extension_name");

  /**
   * extension version
   */
  public static final String EXTENSION_VERSION_PROPERTY_ID =
      PropertyHelper.getPropertyId("Artifacts", "extension_version");

  /**
   * extension service name
   */
  public static final String EXTENSION_SERVICE_NAME_PROPERTY_ID =
      PropertyHelper.getPropertyId("Artifacts", "service_name");

  /**
   * extension service name
   */
  public static final String EXTENSION_COMPONENT_NAME_PROPERTY_ID =
    PropertyHelper.getPropertyId("Artifacts", "component_name");

  /**
   * artifact name
   */
  public static final String ARTIFACT_NAME_PROPERTY_ID =
      PropertyHelper.getPropertyId("Artifacts", "artifact_name");

  /**
   * artifact data
   */
  public static final String ARTIFACT_DATA_PROPERTY_ID = "artifact_data";

  /**
   * primary key fields
   */
  public static Set<String> pkPropertyIds = new HashSet<String>();

  /**
   * map of resource type to fk field
   */
  public static Map<Resource.Type, String> keyPropertyIds =
      new HashMap<Resource.Type, String>();

  /**
   * resource properties
   */
  public static Set<String> propertyIds = new HashSet<String>();

  /**
   * name of the kerberos descriptor artifact.
   */
  public static final String KERBEROS_DESCRIPTOR_NAME = "kerberos_descriptor";

  /**
   * name of the metrics descriptor artifact.
   */
  public static final String METRICS_DESCRIPTOR_NAME = "metrics_descriptor";

  /**
   * name of the widgets descriptor artifact.
   */
  public static final String WIDGETS_DESCRIPTOR_NAME = "widgets_descriptor";

  /**
   * KerberosDescriptorFactory used to create KerberosDescriptor instances
   */
  @Inject
  private static KerberosDescriptorFactory kerberosDescriptorFactory;

  /**
   * KerberosServiceDescriptorFactory used to create KerberosServiceDescriptor instances
   */
  @Inject
  private static KerberosServiceDescriptorFactory kerberosServiceDescriptorFactory;

  Type widgetLayoutType = new TypeToken<Map<String, List<WidgetLayout>>>(){}.getType();
  Gson gson = new Gson();

  /**
   * set resource properties, pk and fk's
   */
  static {
    // resource properties
    propertyIds.add(EXTENSION_NAME_PROPERTY_ID);
    propertyIds.add(EXTENSION_VERSION_PROPERTY_ID);
    propertyIds.add(EXTENSION_SERVICE_NAME_PROPERTY_ID);
    propertyIds.add(ARTIFACT_NAME_PROPERTY_ID);
    propertyIds.add(ARTIFACT_DATA_PROPERTY_ID);

    // pk property
    pkPropertyIds.add(ARTIFACT_NAME_PROPERTY_ID);

    // fk properties
    keyPropertyIds.put(Resource.Type.ExtensionArtifact, ARTIFACT_NAME_PROPERTY_ID);
    keyPropertyIds.put(Resource.Type.Extension, EXTENSION_NAME_PROPERTY_ID);
    keyPropertyIds.put(Resource.Type.ExtensionVersion, EXTENSION_VERSION_PROPERTY_ID);
    keyPropertyIds.put(Resource.Type.ExtensionService, EXTENSION_SERVICE_NAME_PROPERTY_ID);
  }

  /**
   * Constructor.
   *
   * @param managementController ambari controller
   */
  protected ExtensionArtifactResourceProvider(AmbariManagementController managementController) {
    super(propertyIds, keyPropertyIds, managementController);
  }

  @Override
  public Set<Resource> getResources(Request request, Predicate predicate)
      throws SystemException,
             UnsupportedPropertyException,
             NoSuchResourceException,
             NoSuchParentResourceException {

    Set<Resource> resources = new HashSet<Resource>();

    resources.addAll(getKerberosDescriptors(request, predicate));
    resources.addAll(getMetricsDescriptors(request, predicate));
    resources.addAll(getWidgetsDescriptors(request, predicate));
    // add other artifacts types here

    if (resources.isEmpty()) {
      throw new NoSuchResourceException(
          "The requested resource doesn't exist: Artifact not found, " + predicate);
    }

    return resources;
  }

  @Override
  protected Set<String> getPKPropertyIds() {
    return pkPropertyIds;
  }

  @Override
  public RequestStatus createResources(Request request)
      throws SystemException,
      UnsupportedPropertyException,
      ResourceAlreadyExistsException,
      NoSuchParentResourceException {

    throw new UnsupportedOperationException("Creating extension artifacts is not supported");
  }

  @Override
  public RequestStatus updateResources(Request request, Predicate predicate)
      throws SystemException,
             UnsupportedPropertyException,
             NoSuchResourceException,
             NoSuchParentResourceException {

    throw new UnsupportedOperationException("Updating of extension artifacts is not supported");
  }

  @Override
  public RequestStatus deleteResources(Predicate predicate)
      throws SystemException,
             UnsupportedPropertyException,
             NoSuchResourceException,
             NoSuchParentResourceException {

    throw new UnsupportedOperationException("Deletion of extension artifacts is not supported");
  }

  /**
   * Get all extension and extension service kerberos descriptor resources.
   *
   * @param request    user request
   * @param predicate  request predicate
   *
   * @return set of all extension related kerberos descriptor resources; will not return null
   *
   * @throws SystemException                if an unexpected exception occurs
   * @throws UnsupportedPropertyException   if an unsupported property was requested
   * @throws NoSuchParentResourceException  if a specified parent resource doesn't exist
   * @throws NoSuchResourceException        if the requested resource doesn't exist
   */
  private Set<Resource> getKerberosDescriptors(Request request, Predicate predicate)
      throws SystemException,
             UnsupportedPropertyException,
             NoSuchParentResourceException,
             NoSuchResourceException {

    Set<Resource> resources = new HashSet<Resource>();

    for (Map<String, Object> properties : getPropertyMaps(predicate)) {
      String artifactName = (String) properties.get(ARTIFACT_NAME_PROPERTY_ID);
      if (artifactName == null || artifactName.equals(KERBEROS_DESCRIPTOR_NAME)) {
        String extensionName = (String) properties.get(EXTENSION_NAME_PROPERTY_ID);
        String extensionVersion = (String) properties.get(EXTENSION_VERSION_PROPERTY_ID);
        String extensionService = (String) properties.get(EXTENSION_SERVICE_NAME_PROPERTY_ID);

        Map<String, Object> descriptor;
        try {
          descriptor = getKerberosDescriptor(extensionName, extensionVersion, extensionService);
        } catch (IOException e) {
          LOG.error("Unable to process Kerberos Descriptor. Properties: " + properties, e);
          throw new SystemException("An internal exception occurred while attempting to build a Kerberos Descriptor " +
              "artifact. See ambari server logs for more information", e);
        }

        if (descriptor != null) {
          Resource resource = new ResourceImpl(Resource.Type.ExtensionArtifact);
          Set<String> requestedIds = getRequestPropertyIds(request, predicate);
          setResourceProperty(resource, ARTIFACT_NAME_PROPERTY_ID, KERBEROS_DESCRIPTOR_NAME, requestedIds);
          setResourceProperty(resource, ARTIFACT_DATA_PROPERTY_ID, descriptor, requestedIds);
          setResourceProperty(resource, EXTENSION_NAME_PROPERTY_ID, extensionName, requestedIds);
          setResourceProperty(resource, EXTENSION_VERSION_PROPERTY_ID, extensionVersion, requestedIds);
          if (extensionService != null) {
            setResourceProperty(resource, EXTENSION_SERVICE_NAME_PROPERTY_ID, extensionService, requestedIds);
          }
          resources.add(resource);
        }
      }
    }
    return resources;
  }

  /**
   * Get all extension and extension service metrics descriptor resources.
   *
   * @param request    user request
   * @param predicate  request predicate
   *
   * @return set of all extension related kerberos descriptor resources; will not return null
   *
   * @throws SystemException                if an unexpected exception occurs
   * @throws UnsupportedPropertyException   if an unsupported property was requested
   * @throws NoSuchParentResourceException  if a specified parent resource doesn't exist
   * @throws NoSuchResourceException        if the requested resource doesn't exist
   */
  private Set<Resource> getMetricsDescriptors(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
           NoSuchParentResourceException, NoSuchResourceException {

    Set<Resource> resources = new HashSet<Resource>();

    for (Map<String, Object> properties : getPropertyMaps(predicate)) {
      String artifactName = (String) properties.get(ARTIFACT_NAME_PROPERTY_ID);
      if (artifactName == null || artifactName.equals(METRICS_DESCRIPTOR_NAME)) {
        String extensionName = (String) properties.get(EXTENSION_NAME_PROPERTY_ID);
        String extensionVersion = (String) properties.get(EXTENSION_VERSION_PROPERTY_ID);
        String extensionService = (String) properties.get(EXTENSION_SERVICE_NAME_PROPERTY_ID);
        String componentName = (String) properties.get(EXTENSION_COMPONENT_NAME_PROPERTY_ID);

        Map<String, Object> descriptor;
        AmbariMetaInfo metaInfo = getManagementController().getAmbariMetaInfo();

        try {
          List<MetricDefinition> componentMetrics;
          Map<String, Map<String, List<MetricDefinition>>> serviceMetrics;
          if (extensionService != null) {
            if (componentName == null) {
              // Service
              serviceMetrics = removeAggregateFunctions(metaInfo.getExtensionServiceMetrics(extensionName,
                      extensionVersion, extensionService));
              descriptor = Collections.singletonMap(extensionService, (Object) serviceMetrics);
            } else {
              // Component
              componentMetrics = removeAggregateFunctions(metaInfo.getExtensionMetrics(extensionName,
                      extensionVersion, extensionService, componentName, Resource.Type.Component.name()));
              descriptor = Collections.singletonMap(componentName, (Object) componentMetrics);
            }
          } else {
            // Cluster
            Map<String, Map<String, PropertyInfo>> clusterMetrics =
              PropertyHelper.getMetricPropertyIds(Resource.Type.Cluster);
            // Host
            Map<String, Map<String, PropertyInfo>> hostMetrics =
              PropertyHelper.getMetricPropertyIds(Resource.Type.Host);

            descriptor = new HashMap<String, Object>();
            descriptor.put(Resource.Type.Cluster.name(), clusterMetrics);
            descriptor.put(Resource.Type.Host.name(), hostMetrics);
          }


        } catch (IOException e) {
          LOG.error("Unable to process Metrics Descriptor. Properties: " + properties, e);
          throw new SystemException("An internal exception occurred while attempting to build a Metrics Descriptor " +
            "artifact. See ambari server logs for more information", e);
        }

        Resource resource = new ResourceImpl(Resource.Type.ExtensionArtifact);
        Set<String> requestedIds = getRequestPropertyIds(request, predicate);
        setResourceProperty(resource, ARTIFACT_NAME_PROPERTY_ID, METRICS_DESCRIPTOR_NAME, requestedIds);
        setResourceProperty(resource, ARTIFACT_DATA_PROPERTY_ID, descriptor, requestedIds);
        setResourceProperty(resource, EXTENSION_NAME_PROPERTY_ID, extensionName, requestedIds);
        setResourceProperty(resource, EXTENSION_VERSION_PROPERTY_ID, extensionVersion, requestedIds);
        if (extensionService != null) {
          setResourceProperty(resource, EXTENSION_SERVICE_NAME_PROPERTY_ID, extensionService, requestedIds);
        }
        resources.add(resource);
      }
    }
    return resources;
  }

  private Set<Resource> getWidgetsDescriptors(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
             NoSuchParentResourceException, NoSuchResourceException {

    Set<Resource> resources = new HashSet<Resource>();

    for (Map<String, Object> properties : getPropertyMaps(predicate)) {
      String artifactName = (String) properties.get(ARTIFACT_NAME_PROPERTY_ID);
      if (artifactName == null || artifactName.equals(WIDGETS_DESCRIPTOR_NAME)) {
        String extensionName = (String) properties.get(EXTENSION_NAME_PROPERTY_ID);
        String extensionVersion = (String) properties.get(EXTENSION_VERSION_PROPERTY_ID);
        String extensionService = (String) properties.get(EXTENSION_SERVICE_NAME_PROPERTY_ID);

        Map<String, Object> descriptor;
        try {
          descriptor = getWidgetsDescriptor(extensionName, extensionVersion, extensionService);
        } catch (IOException e) {
          LOG.error("Unable to process Widgets Descriptor. Properties: " + properties, e);
          throw new SystemException("An internal exception occurred while attempting to build a Widgets Descriptor " +
            "artifact. See ambari server logs for more information", e);
        }

        if (descriptor != null) {
          Resource resource = new ResourceImpl(Resource.Type.ExtensionArtifact);
          Set<String> requestedIds = getRequestPropertyIds(request, predicate);
          setResourceProperty(resource, ARTIFACT_NAME_PROPERTY_ID, WIDGETS_DESCRIPTOR_NAME, requestedIds);
          setResourceProperty(resource, ARTIFACT_DATA_PROPERTY_ID, descriptor, requestedIds);
          setResourceProperty(resource, EXTENSION_NAME_PROPERTY_ID, extensionName, requestedIds);
          setResourceProperty(resource, EXTENSION_VERSION_PROPERTY_ID, extensionVersion, requestedIds);
          if (extensionService != null) {
            setResourceProperty(resource, EXTENSION_SERVICE_NAME_PROPERTY_ID, extensionService, requestedIds);
          }
          resources.add(resource);
        }
      }
    }
    return resources;
  }

  private Map<String, Object> getWidgetsDescriptor(String extensionName,
      String extensionVersion, String serviceName)
        throws NoSuchParentResourceException, IOException {

    AmbariManagementController controller = getManagementController();
    ExtensionInfo extensionInfo;
    try {
      extensionInfo = controller.getAmbariMetaInfo().getExtension(extensionName, extensionVersion);
    } catch (StackAccessException e) {
      throw new NoSuchParentResourceException(String.format(
        "Parent extension resource doesn't exist: extensionName='%s', extensionVersion='%s'", extensionName, extensionVersion));
    }

    if (StringUtils.isEmpty(serviceName)) {
      return getWidgetsDescriptorForCluster(extensionInfo);
    } else {
      return getWidgetsDescriptorForService(extensionInfo, serviceName);
    }
  }

  public Map<String, Object> getWidgetsDescriptorForService(ExtensionInfo extensionInfo, String serviceName)
      throws NoSuchParentResourceException, IOException {

    Map<String, Object> widgetDescriptor = null;

    ServiceInfo serviceInfo = extensionInfo.getService(serviceName);
    if (serviceInfo == null) {
      throw new NoSuchParentResourceException("Service not found. serviceName" + " = " + serviceName);
    }

    File widgetDescriptorFile = serviceInfo.getWidgetsDescriptorFile();
    if (widgetDescriptorFile != null && widgetDescriptorFile.exists()) {
      widgetDescriptor = gson.fromJson(new FileReader(widgetDescriptorFile), widgetLayoutType);
    }

    return widgetDescriptor;
  }

  public Map<String, Object> getWidgetsDescriptorForCluster(ExtensionInfo extensionInfo)
      throws NoSuchParentResourceException, IOException {

    Map<String, Object> widgetDescriptor = null;

    String widgetDescriptorFileLocation = extensionInfo.getWidgetsDescriptorFileLocation();
    if (widgetDescriptorFileLocation != null) {
      File widgetDescriptorFile = new File(widgetDescriptorFileLocation);
      if (widgetDescriptorFile.exists()) {
        widgetDescriptor = gson.fromJson(new FileReader(widgetDescriptorFile), widgetLayoutType);
      }
    }

    return widgetDescriptor;
  }

  /**
   * Get a kerberos descriptor.
   *
   * @param extensionName     extension name
   * @param extensionVersion  extension version
   * @param serviceName   service name
   *
   * @return map of kerberos descriptor data or null if no descriptor exists
   *
   * @throws IOException if unable to parse the associated kerberos descriptor file
   * @throws NoSuchParentResourceException if the parent extension or extension service doesn't exist
   */
  private Map<String, Object> getKerberosDescriptor(String extensionName, String extensionVersion, String serviceName)
      throws NoSuchParentResourceException, IOException {

      return serviceName == null ?
          buildExtensionDescriptor(extensionName, extensionVersion) :
          getServiceDescriptor(extensionName, extensionVersion, serviceName);
  }

  /**
   * Build a kerberos descriptor for the specified extension. This descriptor is for the entire extension version
   * and will contain both the extension descriptor as well as all service descriptors.
   *
   * @return map of kerberos descriptor data or null if no descriptor exists
   *
   * @throws IOException     if unable to read the kerberos descriptor file
   * @throws AmbariException if unable to parse the kerberos descriptor file json
   * @throws NoSuchParentResourceException if the parent extension doesn't exist
   */
  private Map<String, Object> buildExtensionDescriptor(String extensionName, String extensionVersion)
      throws NoSuchParentResourceException, IOException {

    KerberosDescriptor kerberosDescriptor = null;

    AmbariManagementController controller = getManagementController();
    ExtensionInfo extensionInfo;
    try {
      extensionInfo = controller.getAmbariMetaInfo().getExtension(extensionName, extensionVersion);
    } catch (StackAccessException e) {
      throw new NoSuchParentResourceException(String.format(
          "Parent extension resource doesn't exist: extensionName='%s', extensionVersion='%s'", extensionName, extensionVersion));
    }

    Collection<KerberosServiceDescriptor> serviceDescriptors = getServiceDescriptors(extensionInfo);

    String kerberosFileLocation = extensionInfo.getKerberosDescriptorFileLocation();
    if (kerberosFileLocation != null) {
      kerberosDescriptor = kerberosDescriptorFactory.createInstance(new File(kerberosFileLocation));
    } else if (! serviceDescriptors.isEmpty()) {
      // service descriptors present with no extension descriptor,
      // create an empty extension descriptor to hold services
      kerberosDescriptor = new KerberosDescriptor();
    }

    if (kerberosDescriptor != null) {
      for (KerberosServiceDescriptor descriptor : serviceDescriptors) {
        kerberosDescriptor.putService(descriptor);
      }
      return kerberosDescriptor.toMap();
    } else {
      return null;
    }
  }

  /**
   * Get the kerberos descriptor for the specified extension service.
   *
   * @param extensionName     extension name
   * @param extensionVersion  extension version
   * @param serviceName   service name
   *
   * @return map of kerberos descriptor data or null if no descriptor exists
   *
   * @throws IOException if unable to read or parse the kerberos descriptor file
   * @throws NoSuchParentResourceException if the parent extension or extension service doesn't exist
   */
  private Map<String, Object> getServiceDescriptor(
      String extensionName, String extensionVersion, String serviceName) throws NoSuchParentResourceException, IOException {

    AmbariManagementController controller = getManagementController();

    ServiceInfo serviceInfo;
    try {
      serviceInfo = controller.getAmbariMetaInfo().getExtensionService(extensionName, extensionVersion, serviceName);
    } catch (StackAccessException e) {
      throw new NoSuchParentResourceException(String.format(
          "Parent extension/service resource doesn't exist: extensionName='%s', extensionVersion='%s', serviceName='%s'",
          extensionName, extensionVersion, serviceName));
    }
    File kerberosFile = serviceInfo.getKerberosDescriptorFile();

    if (kerberosFile != null) {
      KerberosServiceDescriptor serviceDescriptor =
          kerberosServiceDescriptorFactory.createInstance(kerberosFile, serviceName);

      if (serviceDescriptor != null) {
        return serviceDescriptor.toMap();
      }
    }
    return null;
  }

  /**
   * Get a collection of all service descriptors for the specified extension.
   *
   * @param extension  extension name
   *
   * @return collection of all service descriptors for the extension; will not return null
   *
   * @throws IOException if unable to read or parse a descriptor file
   */
  private Collection<KerberosServiceDescriptor> getServiceDescriptors(ExtensionInfo extension) throws IOException {
    Collection<KerberosServiceDescriptor> serviceDescriptors = new ArrayList<KerberosServiceDescriptor>();
    for (ServiceInfo service : extension.getServices()) {
      File descriptorFile = service.getKerberosDescriptorFile();
      if (descriptorFile != null) {
        KerberosServiceDescriptor descriptor =
            kerberosServiceDescriptorFactory.createInstance(descriptorFile, service.getName());

        if (descriptor != null) {
          serviceDescriptors.add(descriptor);
        }
      }
    }
    return serviceDescriptors;
  }

  private Map<String, Map<String, List<MetricDefinition>>> removeAggregateFunctions(
          Map<String, Map<String, List<MetricDefinition>>> serviceMetrics  ) {
    Map<String, Map<String, List<MetricDefinition>>> filteredServiceMetrics = null;
    if (serviceMetrics != null) {
      Cloner cloner = new Cloner();
      filteredServiceMetrics = cloner.deepClone(serviceMetrics);
      // For every Component
      for (Map<String, List<MetricDefinition>> componentMetricDef :  filteredServiceMetrics.values()) {
        // For every Component / HostComponent category
        for (Map.Entry<String, List<MetricDefinition>> metricDefEntry : componentMetricDef.entrySet()) {
          //For every metric definition
          for (MetricDefinition metricDefinition : metricDefEntry.getValue()) {
            // Metrics System metrics only
            if (metricDefinition.getType().equals("ganglia")) {
              // Create a new map for each category
              for (Map<String, Metric> metricByCategory : metricDefinition.getMetricsByCategory().values()) {
                Iterator<Map.Entry<String, Metric>> iterator = metricByCategory.entrySet().iterator();
                while (iterator.hasNext()) {
                  Map.Entry<String, Metric> entry = iterator.next();
                  String metricName = entry.getKey();
                  if (PropertyHelper.hasAggregateFunctionSuffix(metricName)) {
                    iterator.remove();
                  }
                }
              }
            }
          }
        }
      }
    }
    return filteredServiceMetrics;
  }

  private List<MetricDefinition> removeAggregateFunctions(List<MetricDefinition> componentMetrics) {
    List<MetricDefinition> filteredComponentMetrics = null;
    if (componentMetrics != null) {
      Cloner cloner = new Cloner();
      filteredComponentMetrics = cloner.deepClone(componentMetrics);
      // For every metric definition
      for (MetricDefinition metricDefinition : filteredComponentMetrics) {
        // Metrics System metrics only
        if (metricDefinition.getType().equals("ganglia")) {
          // Create a new map for each category
          for (Map<String, Metric> metricByCategory : metricDefinition.getMetricsByCategory().values()) {
            Iterator<Map.Entry<String, Metric>> iterator = metricByCategory.entrySet().iterator();
            while (iterator.hasNext()) {
              Map.Entry<String, Metric> entry = iterator.next();
              String metricName = entry.getKey();
              if (PropertyHelper.hasAggregateFunctionSuffix(metricName)) {
                iterator.remove();
              }
            }
          }
        }
      }
    }
    return  filteredComponentMetrics;
  }
}
