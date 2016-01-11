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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.ExtensionOperatingSystemRequest;
import org.apache.ambari.server.controller.ExtensionOperatingSystemResponse;
import org.apache.ambari.server.controller.spi.NoSuchParentResourceException;
import org.apache.ambari.server.controller.spi.NoSuchResourceException;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.Resource.Type;
import org.apache.ambari.server.controller.spi.SystemException;
import org.apache.ambari.server.controller.spi.UnsupportedPropertyException;
import org.apache.ambari.server.controller.utilities.PropertyHelper;

public class ExtensionOperatingSystemResourceProvider extends ReadOnlyResourceProvider {

  public static final String OPERATING_SYSTEM_EXTENSION_NAME_PROPERTY_ID        = PropertyHelper.getPropertyId("OperatingSystems", "extension_name");
  public static final String OPERATING_SYSTEM_EXTENSION_VERSION_PROPERTY_ID     = PropertyHelper.getPropertyId("OperatingSystems", "extension_version");
  public static final String OPERATING_SYSTEM_OS_TYPE_PROPERTY_ID               = PropertyHelper.getPropertyId("OperatingSystems", "os_type");
  public static final String OPERATING_SYSTEM_REPOSITORY_VERSION_ID_PROPERTY_ID = PropertyHelper.getPropertyId("OperatingSystems", "repository_version_id");

  @SuppressWarnings("serial")
  private static Set<String> pkPropertyIds = new HashSet<String>() {
    {
      add(OPERATING_SYSTEM_OS_TYPE_PROPERTY_ID);
      add(OPERATING_SYSTEM_EXTENSION_NAME_PROPERTY_ID);
      add(OPERATING_SYSTEM_EXTENSION_VERSION_PROPERTY_ID);
    }
  };

  @SuppressWarnings("serial")
  public static Set<String> propertyIds = new HashSet<String>() {
    {
      add(OPERATING_SYSTEM_OS_TYPE_PROPERTY_ID);
      add(OPERATING_SYSTEM_EXTENSION_NAME_PROPERTY_ID);
      add(OPERATING_SYSTEM_EXTENSION_VERSION_PROPERTY_ID);
      add(OPERATING_SYSTEM_REPOSITORY_VERSION_ID_PROPERTY_ID);
    }
  };

  @SuppressWarnings("serial")
  public static Map<Type, String> keyPropertyIds = new HashMap<Type, String>() {
    {
      put(Resource.Type.OperatingSystem, OPERATING_SYSTEM_OS_TYPE_PROPERTY_ID);
      put(Resource.Type.Extension, OPERATING_SYSTEM_EXTENSION_NAME_PROPERTY_ID);
      put(Resource.Type.ExtensionVersion, OPERATING_SYSTEM_EXTENSION_VERSION_PROPERTY_ID);
      put(Resource.Type.RepositoryVersion, OPERATING_SYSTEM_REPOSITORY_VERSION_ID_PROPERTY_ID);
      put(Resource.Type.CompatibleRepositoryVersion, OPERATING_SYSTEM_REPOSITORY_VERSION_ID_PROPERTY_ID);
    }
  };

  protected ExtensionOperatingSystemResourceProvider(AmbariManagementController managementController) {
    super(propertyIds, keyPropertyIds, managementController);
  }

  @Override
  public Set<Resource> getResources(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
      NoSuchResourceException, NoSuchParentResourceException {

    final Set<ExtensionOperatingSystemRequest> requests = new HashSet<ExtensionOperatingSystemRequest>();

    if (predicate == null) {
      requests.add(getRequest(Collections.<String, Object>emptyMap()));
    } else {
      for (Map<String, Object> propertyMap : getPropertyMaps(predicate)) {
        requests.add(getRequest(propertyMap));
      }
    }

    Set<String> requestedIds = getRequestPropertyIds(request, predicate);

    Set<ExtensionOperatingSystemResponse> responses = getResources(new Command<Set<ExtensionOperatingSystemResponse>>() {
      @Override
      public Set<ExtensionOperatingSystemResponse> invoke() throws AmbariException {
        return getManagementController().getExtensionOperatingSystems(requests);
      }
    });

    Set<Resource> resources = new HashSet<Resource>();

    for (ExtensionOperatingSystemResponse response : responses) {
      Resource resource = new ResourceImpl(Resource.Type.OperatingSystem);

      setResourceProperty(resource, OPERATING_SYSTEM_EXTENSION_NAME_PROPERTY_ID,
          response.getExtensionName(), requestedIds);

      setResourceProperty(resource, OPERATING_SYSTEM_EXTENSION_VERSION_PROPERTY_ID,
          response.getExtensionVersion(), requestedIds);

      setResourceProperty(resource, OPERATING_SYSTEM_OS_TYPE_PROPERTY_ID,
          response.getOsType(), requestedIds);

      if (response.getRepositoryVersionId() != null) {
        setResourceProperty(resource, OPERATING_SYSTEM_REPOSITORY_VERSION_ID_PROPERTY_ID,
            response.getRepositoryVersionId(), requestedIds);
      }

      resources.add(resource);
    }

    return resources;
  }

  private ExtensionOperatingSystemRequest getRequest(Map<String, Object> properties) {
    final ExtensionOperatingSystemRequest request = new ExtensionOperatingSystemRequest(
        (String) properties.get(OPERATING_SYSTEM_EXTENSION_NAME_PROPERTY_ID),
        (String) properties.get(OPERATING_SYSTEM_EXTENSION_VERSION_PROPERTY_ID),
        (String) properties.get(OPERATING_SYSTEM_OS_TYPE_PROPERTY_ID));
    if (properties.containsKey(OPERATING_SYSTEM_REPOSITORY_VERSION_ID_PROPERTY_ID)) {
      request.setRepositoryVersionId(Long.parseLong(properties.get(OPERATING_SYSTEM_REPOSITORY_VERSION_ID_PROPERTY_ID).toString()));
    }
    return request;
  }

  @Override
  protected Set<String> getPKPropertyIds() {
    return pkPropertyIds;
  }

}
