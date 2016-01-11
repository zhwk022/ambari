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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ambari.server.StaticallyInject;
import org.apache.ambari.server.api.resources.OperatingSystemResourceDefinition;
import org.apache.ambari.server.api.resources.RepositoryResourceDefinition;
import org.apache.ambari.server.api.services.AmbariMetaInfo;
import org.apache.ambari.server.controller.AmbariManagementController;
import org.apache.ambari.server.controller.spi.NoSuchParentResourceException;
import org.apache.ambari.server.controller.spi.NoSuchResourceException;
import org.apache.ambari.server.controller.spi.Predicate;
import org.apache.ambari.server.controller.spi.Request;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.spi.Resource.Type;
import org.apache.ambari.server.controller.spi.SystemException;
import org.apache.ambari.server.controller.spi.UnsupportedPropertyException;
import org.apache.ambari.server.orm.dao.ExtensionRepositoryVersionDAO;
import org.apache.ambari.server.orm.dao.RepositoryVersionDAO;
import org.apache.ambari.server.orm.entities.ExtensionRepositoryVersionEntity;
import org.apache.ambari.server.orm.entities.RepositoryVersionEntity;
import org.apache.ambari.server.state.ExtensionId;
import org.apache.ambari.server.state.StackId;
import org.apache.ambari.server.state.stack.UpgradePack;
import org.apache.ambari.server.state.stack.upgrade.RepositoryVersionHelper;

import com.google.common.collect.Sets;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Resource provider for repository versions resources.
 */
@StaticallyInject
public class CompatibleExtensionRepositoryVersionResourceProvider extends ReadOnlyResourceProvider {

  // ----- Property ID constants ---------------------------------------------

  public static final String REPOSITORY_VERSION_ID_PROPERTY_ID                 = "CompatibleRepositoryVersions/id";
  public static final String REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID         = "CompatibleRepositoryVersions/extension_name";
  public static final String REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID      = "CompatibleRepositoryVersions/extension_version";
  public static final String REPOSITORY_VERSION_REPOSITORY_VERSION_PROPERTY_ID = "CompatibleRepositoryVersions/repository_version";
  public static final String REPOSITORY_VERSION_DISPLAY_NAME_PROPERTY_ID       = "CompatibleRepositoryVersions/display_name";
  public static final String REPOSITORY_VERSION_UPGRADE_PACK_PROPERTY_ID       = "CompatibleRepositoryVersions/upgrade_pack";
  public static final String SUBRESOURCE_OPERATING_SYSTEMS_PROPERTY_ID         = new OperatingSystemResourceDefinition().getPluralName();
  public static final String SUBRESOURCE_REPOSITORIES_PROPERTY_ID              = new RepositoryResourceDefinition().getPluralName();

  private static Set<String> pkPropertyIds = Collections.singleton(REPOSITORY_VERSION_ID_PROPERTY_ID);

  static Set<String> propertyIds = Sets.newHashSet(
      REPOSITORY_VERSION_ID_PROPERTY_ID,
      REPOSITORY_VERSION_REPOSITORY_VERSION_PROPERTY_ID,
      REPOSITORY_VERSION_DISPLAY_NAME_PROPERTY_ID,
      REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID,
      REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID,
      REPOSITORY_VERSION_UPGRADE_PACK_PROPERTY_ID,
      SUBRESOURCE_OPERATING_SYSTEMS_PROPERTY_ID);

  static Map<Type, String> keyPropertyIds = new HashMap<Type, String>() {
    {
      put(Type.Stack, REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID);
      put(Type.StackVersion, REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID);
      put(Type.CompatibleRepositoryVersion, REPOSITORY_VERSION_ID_PROPERTY_ID);
    }
  };

  @Inject
  private static ExtensionRepositoryVersionDAO repositoryVersionDAO;

  @Inject
  private static Provider<AmbariMetaInfo> s_ambariMetaInfo;

  @Inject
  private static Provider<RepositoryVersionHelper> s_repositoryVersionHelper;

  /**
   * Create a new resource provider.
   *
   */
  public CompatibleExtensionRepositoryVersionResourceProvider(AmbariManagementController amc) {
    super(propertyIds, keyPropertyIds, amc);
  }

  @Override
  public Set<Resource> getResources(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {
    final Set<Resource> resources = new HashSet<Resource>();
    final Set<String> requestedIds = getRequestPropertyIds(request, predicate);
    final Set<Map<String, Object>> propertyMaps = getPropertyMaps(predicate);


    List<ExtensionRepositoryVersionEntity> requestedEntities = new ArrayList<ExtensionRepositoryVersionEntity>();
    for (Map<String, Object> propertyMap: propertyMaps) {

      final ExtensionId extensionId = getExtensionInformationFromUrl(propertyMap);

      if (extensionId != null && propertyMaps.size() == 1 && propertyMap.get(REPOSITORY_VERSION_ID_PROPERTY_ID) == null) {
        requestedEntities.addAll(repositoryVersionDAO.findByExtension(extensionId));

        Map<String, UpgradePack> packs = s_ambariMetaInfo.get().getExtensionUpgradePacks(
            extensionId.getExtensionName(), extensionId.getExtensionVersion());

        //TODO - This is wrong
        for (UpgradePack up : packs.values()) {
          if (null != up.getTargetStack()) {
            ExtensionId targetExtensionId = new ExtensionId(up.getTargetStack());
            requestedEntities.addAll(repositoryVersionDAO.findByExtension(targetExtensionId));
          }
        }

      } else {
        final Long id;
        try {
          id = Long.parseLong(propertyMap.get(REPOSITORY_VERSION_ID_PROPERTY_ID).toString());
        } catch (Exception ex) {
          throw new SystemException("Repository version should have numerical id");
        }
        final ExtensionRepositoryVersionEntity entity = repositoryVersionDAO.findByPK(id);
        if (entity == null) {
          throw new NoSuchResourceException("There is no repository version with id " + id);
        } else {
          requestedEntities.add(entity);
        }
      }
    }

    for (ExtensionRepositoryVersionEntity entity: requestedEntities) {

      final Resource resource = new ResourceImpl(Resource.Type.CompatibleRepositoryVersion);

      setResourceProperty(resource, REPOSITORY_VERSION_ID_PROPERTY_ID, entity.getId(), requestedIds);
      setResourceProperty(resource, REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID, entity.getExtensionName(), requestedIds);
      setResourceProperty(resource, REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID, entity.getExtensionVersion(), requestedIds);
      setResourceProperty(resource, REPOSITORY_VERSION_DISPLAY_NAME_PROPERTY_ID, entity.getDisplayName(), requestedIds);
      setResourceProperty(resource, REPOSITORY_VERSION_UPGRADE_PACK_PROPERTY_ID, entity.getUpgradePackage(), requestedIds);
      setResourceProperty(resource, REPOSITORY_VERSION_REPOSITORY_VERSION_PROPERTY_ID, entity.getVersion(), requestedIds);

      resources.add(resource);
    }
    return resources;
  }

  @Override
  protected Set<String> getPKPropertyIds() {
    return pkPropertyIds;
  }

  /**
   * Gets the extension id from the request map
   * @param propertyMap the request map
   * @return  the ExtensionId, or {@code null} if not found.
   */
  protected ExtensionId getExtensionInformationFromUrl(Map<String, Object> propertyMap) {
    if (propertyMap.containsKey(REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID) && propertyMap.containsKey(REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID)) {
      return new ExtensionId(propertyMap.get(REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID).toString(), propertyMap.get(REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID).toString());
    }
    return null;
  }

}
