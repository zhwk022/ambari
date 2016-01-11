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

import com.google.inject.Inject;
import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.StaticallyInject;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.ExtensionServiceRequest;
import org.apache.ambari.server.controller.ExtensionServiceResponse;
import org.apache.ambari.server.controller.spi.*;
import org.apache.ambari.server.controller.spi.Resource.Type;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.apache.ambari.server.state.kerberos.KerberosServiceDescriptor;
import org.apache.ambari.server.state.kerberos.KerberosServiceDescriptorFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

@StaticallyInject
public class ExtensionServiceResourceProvider extends ReadOnlyResourceProvider {

  protected static final String SERVICE_NAME_PROPERTY_ID = PropertyHelper.getPropertyId(
      "ExtensionServices", "service_name");

  public static final String EXTENSION_NAME_PROPERTY_ID = PropertyHelper.getPropertyId(
      "ExtensionServices", "extension_name");

  public static final String EXTENSION_VERSION_PROPERTY_ID = PropertyHelper.getPropertyId(
      "ExtensionServices", "extension_version");

  private static final String SERVICE_DISPLAY_NAME_PROPERTY_ID = PropertyHelper.getPropertyId(
      "ExtensionServices", "display_name");

  private static final String USER_NAME_PROPERTY_ID = PropertyHelper.getPropertyId(
      "ExtensionServices", "user_name");

  private static final String COMMENTS_PROPERTY_ID = PropertyHelper.getPropertyId(
      "ExtensionServices", "comments");

  private static final String VERSION_PROPERTY_ID = PropertyHelper.getPropertyId(
      "ExtensionServices", "service_version");

  private static final String CONFIG_TYPES = PropertyHelper.getPropertyId(
      "ExtensionServices", "config_types");

  private static final String REQUIRED_SERVICES_ID = PropertyHelper.getPropertyId(
      "ExtensionServices", "required_services");

  private static final String SERVICE_CHECK_SUPPORTED_PROPERTY_ID = PropertyHelper.getPropertyId(
      "ExtensionServices", "service_check_supported");

  private static final String CUSTOM_COMMANDS_PROPERTY_ID = PropertyHelper.getPropertyId(
      "ExtensionServices", "custom_commands");

  private static Set<String> pkPropertyIds = new HashSet<String>(
      Arrays.asList(new String[] { EXTENSION_NAME_PROPERTY_ID,
          EXTENSION_VERSION_PROPERTY_ID, SERVICE_NAME_PROPERTY_ID }));

  /**
   * KerberosServiceDescriptorFactory used to create KerberosServiceDescriptor instances
   */
  @Inject
  private static KerberosServiceDescriptorFactory kerberosServiceDescriptorFactory;

  protected ExtensionServiceResourceProvider(Set<String> propertyIds,
      Map<Type, String> keyPropertyIds,
      AmbariManagementController managementController) {
    super(propertyIds, keyPropertyIds, managementController);
  }

  @Override
  public Set<Resource> getResources(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
      NoSuchResourceException, NoSuchParentResourceException {

    final Set<ExtensionServiceRequest> requests = new HashSet<ExtensionServiceRequest>();

    if (predicate == null) {
      requests.add(getRequest(Collections.<String, Object>emptyMap()));
    } else {
      for (Map<String, Object> propertyMap : getPropertyMaps(predicate)) {
        requests.add(getRequest(propertyMap));
      }
    }

    Set<String> requestedIds = getRequestPropertyIds(request, predicate);

    Set<ExtensionServiceResponse> responses = getResources(new Command<Set<ExtensionServiceResponse>>() {
      @Override
      public Set<ExtensionServiceResponse> invoke() throws AmbariException {
        return getManagementController().getExtensionServices(requests);
      }
    });

    Set<Resource> resources = new HashSet<Resource>();

    for (ExtensionServiceResponse response : responses) {
      Resource resource = new ResourceImpl(Resource.Type.ExtensionService);

      setResourceProperty(resource, EXTENSION_NAME_PROPERTY_ID,
          response.getExtensionName(), requestedIds);

      setResourceProperty(resource, EXTENSION_VERSION_PROPERTY_ID,
          response.getExtensionVersion(), requestedIds);

      setResourceProperty(resource, SERVICE_NAME_PROPERTY_ID,
          response.getServiceName(), requestedIds);

      setResourceProperty(resource, SERVICE_DISPLAY_NAME_PROPERTY_ID,
          response.getServiceDisplayName(), requestedIds);

      setResourceProperty(resource, USER_NAME_PROPERTY_ID,
          response.getUserName(), requestedIds);

      setResourceProperty(resource, COMMENTS_PROPERTY_ID,
          response.getComments(), requestedIds);

      setResourceProperty(resource, VERSION_PROPERTY_ID,
          response.getServiceVersion(), requestedIds);

      setResourceProperty(resource, CONFIG_TYPES,
          response.getConfigTypes(), requestedIds);

      setResourceProperty(resource, REQUIRED_SERVICES_ID,
          response.getRequiredServices(), requestedIds);

      setResourceProperty(resource, SERVICE_CHECK_SUPPORTED_PROPERTY_ID,
          response.isServiceCheckSupported(), requestedIds);

      setResourceProperty(resource, CUSTOM_COMMANDS_PROPERTY_ID,
          response.getCustomCommands(), requestedIds);

      resources.add(resource);
    }

    return resources;
  }

  private ExtensionServiceRequest getRequest(Map<String, Object> properties) {
    return new ExtensionServiceRequest(
        (String) properties.get(EXTENSION_NAME_PROPERTY_ID),
        (String) properties.get(EXTENSION_VERSION_PROPERTY_ID),
        (String) properties.get(SERVICE_NAME_PROPERTY_ID));
  }

  @Override
  protected Set<String> getPKPropertyIds() {
    return pkPropertyIds;
  }

}
