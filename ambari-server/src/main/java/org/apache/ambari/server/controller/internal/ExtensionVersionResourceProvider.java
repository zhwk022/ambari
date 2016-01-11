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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.inject.Inject;
import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.StaticallyInject;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.ExtensionVersionRequest;
import org.apache.ambari.server.controller.ExtensionVersionResponse;
import org.apache.ambari.server.controller.spi.NoSuchParentResourceException;
import org.apache.ambari.server.controller.spi.NoSuchResourceException;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.Resource.Type;
import org.apache.ambari.server.controller.spi.SystemException;
import org.apache.ambari.server.controller.spi.UnsupportedPropertyException;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.apache.ambari.server.state.kerberos.KerberosDescriptor;
import org.apache.ambari.server.state.kerberos.KerberosDescriptorFactory;
import org.apache.ambari.server.state.kerberos.KerberosServiceDescriptor;
import org.apache.ambari.server.state.kerberos.KerberosServiceDescriptorFactory;

@StaticallyInject
public class ExtensionVersionResourceProvider extends ReadOnlyResourceProvider {

  public static final String EXTENSION_VERSION_PROPERTY_ID     = PropertyHelper.getPropertyId("Versions", "extension_version");
  public static final String EXTENSION_NAME_PROPERTY_ID        = PropertyHelper.getPropertyId("Versions", "extension_name");
  public static final String EXTENSION_MIN_VERSION_PROPERTY_ID = PropertyHelper.getPropertyId("Versions", "min_upgrade_version");
  public static final String EXTENSION_ACTIVE_PROPERTY_ID      = PropertyHelper.getPropertyId("Versions", "active");
  public static final String EXTENSION_VALID_PROPERTY_ID      = PropertyHelper.getPropertyId("Versions", "valid");
  public static final String EXTENSION_ERROR_SET      = PropertyHelper.getPropertyId("Versions", "extension-errors");
  public static final String EXTENSION_CONFIG_TYPES            = PropertyHelper.getPropertyId("Versions", "config_types");
  public static final String EXTENSION_PARENT_PROPERTY_ID      = PropertyHelper.getPropertyId("Versions", "parent_extension_version");
  public static final String UPGRADE_PACKS_PROPERTY_ID = PropertyHelper.getPropertyId("Versions", "upgrade_packs");

  private static Set<String> pkPropertyIds = new HashSet<String>(
      Arrays.asList(new String[] { EXTENSION_NAME_PROPERTY_ID, EXTENSION_VERSION_PROPERTY_ID }));

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

  protected ExtensionVersionResourceProvider(Set<String> propertyIds,
      Map<Type, String> keyPropertyIds,
      AmbariManagementController managementController) {
    super(propertyIds, keyPropertyIds, managementController);
  }

  @Override
  public Set<Resource> getResources(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
      NoSuchResourceException, NoSuchParentResourceException {

    final Set<ExtensionVersionRequest> requests = new HashSet<ExtensionVersionRequest>();

    if (predicate == null) {
      requests.add(getRequest(Collections.<String, Object>emptyMap()));
    } else {
      for (Map<String, Object> propertyMap : getPropertyMaps(predicate)) {
        requests.add(getRequest(propertyMap));
      }
    }

    Set<String> requestedIds = getRequestPropertyIds(request, predicate);

    Set<ExtensionVersionResponse> responses = getResources(new Command<Set<ExtensionVersionResponse>>() {
      @Override
      public Set<ExtensionVersionResponse> invoke() throws AmbariException {
        return getManagementController().getExtensionVersions(requests);
      }
    });

    Set<Resource> resources = new HashSet<Resource>();

    for (ExtensionVersionResponse response : responses) {
      Resource resource = new ResourceImpl(Resource.Type.ExtensionVersion);

      setResourceProperty(resource, EXTENSION_NAME_PROPERTY_ID,
          response.getExtensionName(), requestedIds);

      setResourceProperty(resource, EXTENSION_VERSION_PROPERTY_ID,
          response.getExtensionVersion(), requestedIds);

      /*setResourceProperty(resource, EXTENSION_MIN_VERSION_PROPERTY_ID,
          response.getMinUpgradeVersion(), requestedIds);

      setResourceProperty(resource, EXTENSION_ACTIVE_PROPERTY_ID,
          response.isActive(), requestedIds);*/

      setResourceProperty(resource, EXTENSION_VALID_PROPERTY_ID,
          response.isValid(), requestedIds);

      setResourceProperty(resource, EXTENSION_ERROR_SET,
          response.getErrors(), requestedIds);

      setResourceProperty(resource, EXTENSION_PARENT_PROPERTY_ID,
        response.getParentVersion(), requestedIds);

      setResourceProperty(resource, EXTENSION_CONFIG_TYPES,
          response.getConfigTypes(), requestedIds);

      setResourceProperty(resource, UPGRADE_PACKS_PROPERTY_ID,
          response.getUpgradePacks(), requestedIds);

      resources.add(resource);
    }

    return resources;
  }

  /**
   * Given data from a ExtensionVersionResponse build a complete Kerberos descriptor hierarchy.
   *
   * @param extensionVersionResponse the ExtensionVersionResponse instance containing the details of the
   *                             extension and the relevant Kerberos descriptor files
   * @return a KerberosDescriptor containing the complete hierarchy for the extension
   * @throws IOException     if the specified File is not found or not a readable
   * @throws AmbariException if the specified File does not contain valid JSON-encoded Kerberos
   *                         descriptor
   */
  private KerberosDescriptor buildKerberosDescriptor(ExtensionVersionResponse extensionVersionResponse)
      throws IOException {
    KerberosDescriptor kerberosDescriptor = null;

    // Process the extension-level Kerberos descriptor file
    File extensionKerberosDescriptorFile = extensionVersionResponse.getExtensionKerberosDescriptorFile();
    if (extensionKerberosDescriptorFile != null) {
      kerberosDescriptor = kerberosDescriptorFactory.createInstance(extensionKerberosDescriptorFile);
    }

    // Process the service-level Kerberos descriptor files
    Collection<File> serviceDescriptorFiles = extensionVersionResponse.getServiceKerberosDescriptorFiles();
    if ((serviceDescriptorFiles != null) && !serviceDescriptorFiles.isEmpty()) {
      // Make sure kerberosDescriptor is not null. This will be the case if there is no extension-level
      // Kerberos descriptor file.
      if (kerberosDescriptor == null) {
        kerberosDescriptor = new KerberosDescriptor();
      }

      // For each service-level Kerberos descriptor file, parse into an array of KerberosServiceDescriptors
      // and then append each to the KerberosDescriptor hierarchy.
      for (File file : serviceDescriptorFiles) {
        KerberosServiceDescriptor[] serviceDescriptors = kerberosServiceDescriptorFactory.createInstances(file);

        if (serviceDescriptors != null) {
          for (KerberosServiceDescriptor serviceDescriptor : serviceDescriptors) {
            kerberosDescriptor.putService(serviceDescriptor);
          }
        }
      }
    }

    return kerberosDescriptor;
  }

  private ExtensionVersionRequest getRequest(Map<String, Object> properties) {
    return new ExtensionVersionRequest(
        (String) properties.get(EXTENSION_NAME_PROPERTY_ID),
        (String) properties.get(EXTENSION_VERSION_PROPERTY_ID));
  }

  @Override
  protected Set<String> getPKPropertyIds() {
    return pkPropertyIds;
  }

}
