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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.ExtensionConfigurationResponse;
import org.apache.ambari.server.controller.ExtensionLevelConfigurationRequest;
import org.apache.ambari.server.controller.spi.NoSuchParentResourceException;
import org.apache.ambari.server.controller.spi.NoSuchResourceException;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.Resource.Type;
import org.apache.ambari.server.controller.spi.SystemException;
import org.apache.ambari.server.controller.spi.UnsupportedPropertyException;
import org.apache.ambari.server.controller.utilities.PropertyHelper;

public class ExtensionLevelConfigurationResourceProvider extends
    ReadOnlyResourceProvider {

  public static final String EXTENSION_NAME_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionLevelConfigurations", "extension_name");

  public static final String EXTENSION_VERSION_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionLevelConfigurations", "extension_version");

  public static final String PROPERTY_NAME_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionLevelConfigurations", "property_name");

  public static final String PROPERTY_VALUE_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionLevelConfigurations", "property_value");

  public static final String PROPERTY_VALUE_ATTRIBUTES_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionLevelConfigurations", "property_value_attributes");

  public static final String DEPENDS_ON_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionLevelConfigurations", "property_depends_on");

  public static final String PROPERTY_DESCRIPTION_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionLevelConfigurations", "property_description");

  public static final String PROPERTY_PROPERTY_TYPE_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionLevelConfigurations", "property_type");

  public static final String PROPERTY_TYPE_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionLevelConfigurations", "type");

  public static final String PROPERTY_FINAL_PROPERTY_ID = PropertyHelper
      .getPropertyId("ExtensionLevelConfigurations", "final");


  private static Set<String> pkPropertyIds = new HashSet<String>(
      Arrays.asList(new String[] { EXTENSION_NAME_PROPERTY_ID,
          EXTENSION_VERSION_PROPERTY_ID, PROPERTY_NAME_PROPERTY_ID }));

  protected ExtensionLevelConfigurationResourceProvider(Set<String> propertyIds,
      Map<Type, String> keyPropertyIds,
      AmbariManagementController managementController) {
    super(propertyIds, keyPropertyIds, managementController);
  }


  @Override
  public Set<Resource> getResources(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
      NoSuchResourceException, NoSuchParentResourceException {

    final Set<ExtensionLevelConfigurationRequest> requests = new HashSet<ExtensionLevelConfigurationRequest>();

    if (predicate == null) {
      requests.add(getRequest(Collections.<String, Object>emptyMap()));
    } else {
      for (Map<String, Object> propertyMap : getPropertyMaps(predicate)) {
        requests.add(getRequest(propertyMap));
      }
    }

    Set<String> requestedIds = getRequestPropertyIds(request, predicate);

    Set<ExtensionConfigurationResponse> responses = getResources(new Command<Set<ExtensionConfigurationResponse>>() {
      @Override
      public Set<ExtensionConfigurationResponse> invoke() throws AmbariException {
        return getManagementController().getExtensionLevelConfigurations(requests);
      }
    });

    Set<Resource> resources = new HashSet<Resource>();

    for (ExtensionConfigurationResponse response : responses) {
      Resource resource = new ResourceImpl(Resource.Type.ExtensionLevelConfiguration);

      setResourceProperty(resource, EXTENSION_NAME_PROPERTY_ID,
          response.getExtensionName(), requestedIds);

      setResourceProperty(resource, EXTENSION_VERSION_PROPERTY_ID,
          response.getExtensionVersion(), requestedIds);

      setResourceProperty(resource, PROPERTY_NAME_PROPERTY_ID,
          response.getPropertyName(), requestedIds);

      setResourceProperty(resource, PROPERTY_VALUE_PROPERTY_ID,
          response.getPropertyValue(), requestedIds);

      setResourceProperty(resource, PROPERTY_VALUE_ATTRIBUTES_PROPERTY_ID,
          response.getPropertyValueAttributes(), requestedIds);

      setResourceProperty(resource, DEPENDS_ON_PROPERTY_ID,
          response.getDependsOnProperties(), requestedIds);

      setResourceProperty(resource, PROPERTY_DESCRIPTION_PROPERTY_ID,
          response.getPropertyDescription(), requestedIds);

      setResourceProperty(resource, PROPERTY_PROPERTY_TYPE_PROPERTY_ID,
          response.getPropertyType(), requestedIds);

      setResourceProperty(resource, PROPERTY_TYPE_PROPERTY_ID,
          response.getType(), requestedIds);

      setDefaultPropertiesAttributes(resource, requestedIds);

      for (Map.Entry<String, String> attribute : response.getPropertyAttributes().entrySet()) {
        setResourceProperty(resource, PropertyHelper.getPropertyId("ExtensionLevelConfigurations", attribute.getKey()),
            attribute.getValue(), requestedIds);
      }

      resources.add(resource);
    }

    return resources;
  }

  /**
   * Set default values for properties attributes before applying original ones
   * to prevent absence in case of empty attributes map
   */
  private void setDefaultPropertiesAttributes(Resource resource, Set<String> requestedIds) {
    setResourceProperty(resource, PROPERTY_FINAL_PROPERTY_ID,
        "false", requestedIds);
  }

  private ExtensionLevelConfigurationRequest getRequest(Map<String, Object> properties) {
    return new ExtensionLevelConfigurationRequest(
        (String) properties.get(EXTENSION_NAME_PROPERTY_ID),
        (String) properties.get(EXTENSION_VERSION_PROPERTY_ID),
        (String) properties.get(PROPERTY_NAME_PROPERTY_ID));
  }

  @Override
  protected Set<String> getPKPropertyIds() {
    return pkPropertyIds;
  }

}
