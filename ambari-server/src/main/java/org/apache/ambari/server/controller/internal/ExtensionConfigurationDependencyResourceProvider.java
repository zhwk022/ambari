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

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.ExtensionConfigurationDependencyRequest;
import org.apache.ambari.server.controller.ExtensionConfigurationDependencyResponse;
import org.apache.ambari.server.controller.spi.NoSuchParentResourceException;
import org.apache.ambari.server.controller.spi.NoSuchResourceException;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.Resource.Type;
import org.apache.ambari.server.controller.spi.SystemException;
import org.apache.ambari.server.controller.spi.UnsupportedPropertyException;
import org.apache.ambari.server.controller.utilities.PropertyHelper;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExtensionConfigurationDependencyResourceProvider extends
    ReadOnlyResourceProvider {

  public static final String EXTENSION_NAME_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionConfigurationDependency", "extension_name");

  public static final String EXTENSION_VERSION_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionConfigurationDependency", "extension_version");

  public static final String SERVICE_NAME_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionConfigurationDependency", "service_name");

  public static final String PROPERTY_NAME_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionConfigurationDependency", "property_name");

  public static final String DEPENDENCY_NAME_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionConfigurationDependency", "dependency_name");

  public static final String DEPENDENCY_TYPE_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionConfigurationDependency", "dependency_type");

  private static Set<String> pkPropertyIds = new HashSet<String>(
      Arrays.asList(new String[] { EXTENSION_NAME_PROPERTY_ID,
          EXTENSION_VERSION_PROPERTY_ID, SERVICE_NAME_PROPERTY_ID,
          PROPERTY_NAME_PROPERTY_ID, DEPENDENCY_NAME_PROPERTY_ID }));

  protected ExtensionConfigurationDependencyResourceProvider(Set<String> propertyIds,
                                                         Map<Type, String> keyPropertyIds,
                                                         AmbariManagementController managementController) {
    super(propertyIds, keyPropertyIds, managementController);
  }

  @Override
  public Set<Resource> getResources(final Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
      NoSuchResourceException, NoSuchParentResourceException {

    final Set<ExtensionConfigurationDependencyRequest> requests =
      new HashSet<ExtensionConfigurationDependencyRequest>();

    if (predicate == null) {
      requests.add(getRequest(Collections.<String, Object>emptyMap()));
    } else {
      for (Map<String, Object> propertyMap : getPropertyMaps(predicate)) {
        requests.add(getRequest(propertyMap));
      }
    }

    Set<String> requestedIds = getRequestPropertyIds(request, predicate);

    Set<ExtensionConfigurationDependencyResponse> responses = getResources(new Command<Set<ExtensionConfigurationDependencyResponse>>() {
      @Override
      public Set<ExtensionConfigurationDependencyResponse> invoke() throws AmbariException {
        return getManagementController().getExtensionConfigurationDependencies(requests);
      }
    });

    Set<Resource> resources = new HashSet<Resource>();

    for (ExtensionConfigurationDependencyResponse response : responses) {
      Resource resource = new ResourceImpl(Type.ExtensionConfigurationDependency);

      setResourceProperty(resource, EXTENSION_NAME_PROPERTY_ID,
          response.getExtensionName(), requestedIds);

      setResourceProperty(resource, EXTENSION_VERSION_PROPERTY_ID,
          response.getExtensionVersion(), requestedIds);

      setResourceProperty(resource, SERVICE_NAME_PROPERTY_ID,
          response.getServiceName(), requestedIds);

      setResourceProperty(resource, PROPERTY_NAME_PROPERTY_ID,
          response.getPropertyName(), requestedIds);

      setResourceProperty(resource, DEPENDENCY_NAME_PROPERTY_ID,
          response.getDependencyName(), requestedIds);

      setResourceProperty(resource, DEPENDENCY_TYPE_PROPERTY_ID,
          response.getDependencyType(), requestedIds);


      resources.add(resource);
    }

    return resources;
  }

  private ExtensionConfigurationDependencyRequest getRequest(Map<String, Object> properties) {
    return new ExtensionConfigurationDependencyRequest(
        (String) properties.get(EXTENSION_NAME_PROPERTY_ID),
        (String) properties.get(EXTENSION_VERSION_PROPERTY_ID),
        (String) properties.get(SERVICE_NAME_PROPERTY_ID),
        (String) properties.get(PROPERTY_NAME_PROPERTY_ID),
        (String) properties.get(DEPENDENCY_NAME_PROPERTY_ID));
  }

  @Override
  protected Set<String> getPKPropertyIds() {
    return pkPropertyIds;
  }

}
