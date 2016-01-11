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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.api.resources.RepositoryResourceDefinition;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.ExtensionRepositoryRequest;
import org.apache.ambari.server.controller.ExtensionRepositoryResponse;
import org.apache.ambari.server.controller.RepositoryRequest;
import org.apache.ambari.server.controller.RepositoryResponse;
import org.apache.ambari.server.controller.spi.NoSuchParentResourceException;
import org.apache.ambari.server.controller.spi.NoSuchResourceException;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.RequestStatus;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.Resource.Type;
import org.apache.ambari.server.controller.spi.ResourceAlreadyExistsException;
import org.apache.ambari.server.controller.spi.SystemException;
import org.apache.ambari.server.controller.spi.UnsupportedPropertyException;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.apache.commons.lang.BooleanUtils;

public class ExtensionRepositoryResourceProvider extends AbstractControllerResourceProvider {

  public static final String REPOSITORY_REPO_NAME_PROPERTY_ID             = PropertyHelper.getPropertyId("Repositories", "repo_name");
  public static final String REPOSITORY_EXTENSION_NAME_PROPERTY_ID        = PropertyHelper.getPropertyId("Repositories", "extension_name");
  public static final String REPOSITORY_EXTENSION_VERSION_PROPERTY_ID     = PropertyHelper.getPropertyId("Repositories", "extension_version");
  public static final String REPOSITORY_OS_TYPE_PROPERTY_ID               = PropertyHelper.getPropertyId("Repositories", "os_type");
  public static final String REPOSITORY_BASE_URL_PROPERTY_ID              = PropertyHelper.getPropertyId("Repositories", "base_url");
  public static final String REPOSITORY_REPO_ID_PROPERTY_ID               = PropertyHelper.getPropertyId("Repositories", "repo_id");
  public static final String REPOSITORY_MIRRORS_LIST_PROPERTY_ID          = PropertyHelper.getPropertyId("Repositories", "mirrors_list");
  public static final String REPOSITORY_DEFAULT_BASE_URL_PROPERTY_ID      = PropertyHelper.getPropertyId("Repositories", "default_base_url");
  public static final String REPOSITORY_VERIFY_BASE_URL_PROPERTY_ID       = PropertyHelper.getPropertyId("Repositories", "verify_base_url");
  public static final String REPOSITORY_LATEST_BASE_URL_PROPERTY_ID       = PropertyHelper.getPropertyId("Repositories", "latest_base_url");
  public static final String REPOSITORY_REPOSITORY_VERSION_ID_PROPERTY_ID = PropertyHelper.getPropertyId("Repositories", "repository_version_id");

  @SuppressWarnings("serial")
  private static Set<String> pkPropertyIds = new HashSet<String>() {
    {
      add(REPOSITORY_EXTENSION_NAME_PROPERTY_ID);
      add(REPOSITORY_EXTENSION_VERSION_PROPERTY_ID);
      add(REPOSITORY_OS_TYPE_PROPERTY_ID);
      add(REPOSITORY_REPO_ID_PROPERTY_ID);
    }
  };

  @SuppressWarnings("serial")
  public static Set<String> propertyIds = new HashSet<String>() {
    {
      add(REPOSITORY_REPO_NAME_PROPERTY_ID);
      add(REPOSITORY_EXTENSION_NAME_PROPERTY_ID);
      add(REPOSITORY_EXTENSION_VERSION_PROPERTY_ID);
      add(REPOSITORY_OS_TYPE_PROPERTY_ID);
      add(REPOSITORY_BASE_URL_PROPERTY_ID);
      add(REPOSITORY_REPO_ID_PROPERTY_ID);
      add(REPOSITORY_MIRRORS_LIST_PROPERTY_ID);
      add(REPOSITORY_DEFAULT_BASE_URL_PROPERTY_ID);
      add(REPOSITORY_VERIFY_BASE_URL_PROPERTY_ID);
      add(REPOSITORY_LATEST_BASE_URL_PROPERTY_ID);
      add(REPOSITORY_REPOSITORY_VERSION_ID_PROPERTY_ID);
    }
  };

  @SuppressWarnings("serial")
  public static Map<Type, String> keyPropertyIds = new HashMap<Type, String>() {
    {
      put(Resource.Type.Extension, REPOSITORY_EXTENSION_NAME_PROPERTY_ID);
      put(Resource.Type.ExtensionVersion, REPOSITORY_EXTENSION_VERSION_PROPERTY_ID);
      put(Resource.Type.OperatingSystem, REPOSITORY_OS_TYPE_PROPERTY_ID);
      put(Resource.Type.Repository, REPOSITORY_REPO_ID_PROPERTY_ID);
      put(Resource.Type.RepositoryVersion, REPOSITORY_REPOSITORY_VERSION_ID_PROPERTY_ID);
    }
  };

  public ExtensionRepositoryResourceProvider(AmbariManagementController managementController) {
    super(propertyIds, keyPropertyIds, managementController);
  }

  @Override
  public RequestStatus updateResources(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
      NoSuchResourceException, NoSuchParentResourceException {

    final Set<ExtensionRepositoryRequest> requests = new HashSet<ExtensionRepositoryRequest>();

    Iterator<Map<String,Object>> iterator = request.getProperties().iterator();
    if (iterator.hasNext()) {
      for (Map<String, Object> propertyMap : getPropertyMaps(iterator.next(), predicate)) {
        requests.add(getRequest(propertyMap));
      }
    }

    modifyResources(new Command<Void>() {
      @Override
      public Void invoke() throws AmbariException {
        getManagementController().updateExtensionRepositories(requests);
        return null;
      }
    });

    return getRequestStatus(null);
  }

  @Override
  public Set<Resource> getResources(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
      NoSuchResourceException, NoSuchParentResourceException {

    final Set<ExtensionRepositoryRequest> requests = new HashSet<ExtensionRepositoryRequest>();

    if (predicate == null) {
      requests.add(getRequest(Collections.<String, Object>emptyMap()));
    } else {
      for (Map<String, Object> propertyMap : getPropertyMaps(predicate)) {
        requests.add(getRequest(propertyMap));
      }
    }
    Set<String> requestedIds = getRequestPropertyIds(request, predicate);

    Set<ExtensionRepositoryResponse> responses = getResources(new Command<Set<ExtensionRepositoryResponse>>() {
      @Override
      public Set<ExtensionRepositoryResponse> invoke() throws AmbariException {
        return getManagementController().getExtensionRepositories(requests);
      }
    });

    Set<Resource> resources = new HashSet<Resource>();

    for (ExtensionRepositoryResponse response : responses) {
        Resource resource = new ResourceImpl(Resource.Type.Repository);

        setResourceProperty(resource, REPOSITORY_EXTENSION_NAME_PROPERTY_ID, response.getExtensionName(), requestedIds);
        setResourceProperty(resource, REPOSITORY_EXTENSION_VERSION_PROPERTY_ID, response.getExtensionVersion(), requestedIds);
        setResourceProperty(resource, REPOSITORY_REPO_NAME_PROPERTY_ID, response.getRepoName(), requestedIds);
        setResourceProperty(resource, REPOSITORY_BASE_URL_PROPERTY_ID, response.getBaseUrl(), requestedIds);
        setResourceProperty(resource, REPOSITORY_OS_TYPE_PROPERTY_ID, response.getOsType(), requestedIds);
        setResourceProperty(resource, REPOSITORY_REPO_ID_PROPERTY_ID, response.getRepoId(), requestedIds);
        setResourceProperty(resource, REPOSITORY_MIRRORS_LIST_PROPERTY_ID, response.getMirrorsList(), requestedIds);
        setResourceProperty(resource, REPOSITORY_DEFAULT_BASE_URL_PROPERTY_ID, response.getDefaultBaseUrl(), requestedIds);
        setResourceProperty(resource, REPOSITORY_LATEST_BASE_URL_PROPERTY_ID, response.getLatestBaseUrl(), requestedIds);
        if (response.getRepositoryVersionId() != null) {
          setResourceProperty(resource, REPOSITORY_REPOSITORY_VERSION_ID_PROPERTY_ID, response.getRepositoryVersionId(), requestedIds);
        }

        resources.add(resource);
    }

    return resources;
  }

  @Override
  public RequestStatus createResources(Request request) throws SystemException, UnsupportedPropertyException, ResourceAlreadyExistsException, NoSuchParentResourceException {
    final String validateOnlyProperty = request.getRequestInfoProperties().get(RepositoryResourceDefinition.VALIDATE_ONLY_DIRECTIVE);
    if (BooleanUtils.toBoolean(validateOnlyProperty)) {
      final Set<ExtensionRepositoryRequest> requests = new HashSet<ExtensionRepositoryRequest>();
      final Iterator<Map<String,Object>> iterator = request.getProperties().iterator();
      if (iterator.hasNext()) {
        for (Map<String, Object> propertyMap : request.getProperties()) {
          requests.add(getRequest(propertyMap));
        }
      }
      createResources(new Command<Void>() {

        @Override
        public Void invoke() throws AmbariException {
          getManagementController().verifyExtensionRepositories(requests);
          return null;
        }

      });
      return getRequestStatus(null);
    } else {
      throw new SystemException("Cannot create repositories.", null);
    }
  }

  @Override
  public RequestStatus deleteResources(Predicate predicate)
      throws SystemException, UnsupportedPropertyException,
      NoSuchResourceException, NoSuchParentResourceException {
    throw new SystemException("Cannot delete repositories.", null);
  }

  private ExtensionRepositoryRequest getRequest(Map<String, Object> properties) {
    ExtensionRepositoryRequest request = new ExtensionRepositoryRequest(
        (String) properties.get(REPOSITORY_EXTENSION_NAME_PROPERTY_ID),
        (String) properties.get(REPOSITORY_EXTENSION_VERSION_PROPERTY_ID),
        (String) properties.get(REPOSITORY_OS_TYPE_PROPERTY_ID),
        (String) properties.get(REPOSITORY_REPO_ID_PROPERTY_ID));

    if (properties.containsKey(REPOSITORY_REPOSITORY_VERSION_ID_PROPERTY_ID)) {
      request.setRepositoryVersionId(Long.parseLong(properties.get(REPOSITORY_REPOSITORY_VERSION_ID_PROPERTY_ID).toString()));
    }

    if (properties.containsKey(REPOSITORY_BASE_URL_PROPERTY_ID)) {
      request.setBaseUrl((String) properties.get(REPOSITORY_BASE_URL_PROPERTY_ID));

      if (properties.containsKey(REPOSITORY_VERIFY_BASE_URL_PROPERTY_ID)) {
        request.setVerifyBaseUrl("true".equalsIgnoreCase(properties.get(REPOSITORY_VERIFY_BASE_URL_PROPERTY_ID).toString()));
      }
    }

    return request;
  }

  @Override
  public Set<String> getPKPropertyIds() {
    return pkPropertyIds;
  }
}
