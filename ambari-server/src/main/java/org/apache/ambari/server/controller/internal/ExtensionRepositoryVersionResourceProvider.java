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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.inject.Provider;
import com.google.inject.persist.Transactional;
import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.ObjectNotFoundException;
import org.apache.ambari.server.api.resources.ExtensionOperatingSystemResourceDefinition;
import org.apache.ambari.server.api.resources.ExtensionRepositoryResourceDefinition;
import org.apache.ambari.server.api.services.AmbariMetaInfo;
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
import org.apache.ambari.server.orm.dao.ClusterVersionDAO;
import org.apache.ambari.server.orm.dao.ExtensionRepositoryVersionDAO;
import org.apache.ambari.server.orm.dao.ExtensionDAO;
import org.apache.ambari.server.orm.entities.ClusterVersionEntity;
import org.apache.ambari.server.orm.entities.ExtensionEntity;
import org.apache.ambari.server.orm.entities.ExtensionRepositoryVersionEntity;
import org.apache.ambari.server.orm.entities.OperatingSystemEntity;
import org.apache.ambari.server.orm.entities.RepositoryEntity;
import org.apache.ambari.server.security.authorization.AuthorizationException;
import org.apache.ambari.server.security.authorization.AuthorizationHelper;
import org.apache.ambari.server.security.authorization.ResourceType;
import org.apache.ambari.server.security.authorization.RoleAuthorization;
import org.apache.ambari.server.state.Cluster;
import org.apache.ambari.server.state.Clusters;
import org.apache.ambari.server.state.OperatingSystemInfo;
import org.apache.ambari.server.state.RepositoryVersionState;
import org.apache.ambari.server.state.ExtensionId;
import org.apache.ambari.server.state.ExtensionInfo;
import org.apache.ambari.server.state.StackInfo;
import org.apache.ambari.server.state.stack.UpgradePack;
import org.apache.ambari.server.state.stack.upgrade.RepositoryVersionHelper;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.inject.Inject;

/**
 * Resource provider for repository versions resources.
 */
public class ExtensionRepositoryVersionResourceProvider extends AbstractAuthorizedResourceProvider {

  // ----- Property ID constants ---------------------------------------------

  public static final String REPOSITORY_VERSION_ID_PROPERTY_ID                 = PropertyHelper.getPropertyId("RepositoryVersions", "id");
  public static final String REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID     = PropertyHelper.getPropertyId("RepositoryVersions", "extension_name");
  public static final String REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID  = PropertyHelper.getPropertyId("RepositoryVersions", "extension_version");
  public static final String REPOSITORY_VERSION_REPOSITORY_VERSION_PROPERTY_ID = PropertyHelper.getPropertyId("RepositoryVersions", "repository_version");
  public static final String REPOSITORY_VERSION_DISPLAY_NAME_PROPERTY_ID       = PropertyHelper.getPropertyId("RepositoryVersions", "display_name");
  public static final String SUBRESOURCE_OPERATING_SYSTEMS_PROPERTY_ID         = new ExtensionOperatingSystemResourceDefinition().getPluralName();
  public static final String SUBRESOURCE_REPOSITORIES_PROPERTY_ID              = new ExtensionRepositoryResourceDefinition().getPluralName();

  @SuppressWarnings("serial")
  private static Set<String> pkPropertyIds = new HashSet<String>() {
    {
      add(REPOSITORY_VERSION_ID_PROPERTY_ID);
    }
  };

  @SuppressWarnings("serial")
  public static Set<String> propertyIds = new HashSet<String>() {
    {
      add(REPOSITORY_VERSION_ID_PROPERTY_ID);
      add(REPOSITORY_VERSION_REPOSITORY_VERSION_PROPERTY_ID);
      add(REPOSITORY_VERSION_DISPLAY_NAME_PROPERTY_ID);
      add(REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID);
      add(REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID);
      add(SUBRESOURCE_OPERATING_SYSTEMS_PROPERTY_ID);
    }
  };

  @SuppressWarnings("serial")
  public static Map<Type, String> keyPropertyIds = new HashMap<Type, String>() {
    {
      put(Type.Extension, REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID);
      put(Type.ExtensionVersion, REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID);
      put(Type.ExtensionRepositoryVersion, REPOSITORY_VERSION_ID_PROPERTY_ID);
    }
  };

  @Inject
  private Gson gson;

  @Inject
  private ExtensionRepositoryVersionDAO repositoryVersionDAO;

  @Inject
  private ClusterVersionDAO clusterVersionDAO;

  @Inject
  private AmbariMetaInfo ambariMetaInfo;

  @Inject
  private RepositoryVersionHelper repositoryVersionHelper;

  @Inject
  private Provider<Clusters> clusters;

  /**
   * Data access object used for lookup up extensions.
   */
  @Inject
  private ExtensionDAO extensionDAO;

  /**
   * Create a new resource provider.
   *
   */
  public ExtensionRepositoryVersionResourceProvider() {
    super(propertyIds, keyPropertyIds);
    setRequiredCreateAuthorizations(EnumSet.of(RoleAuthorization.AMBARI_MANAGE_STACK_VERSIONS));
    setRequiredDeleteAuthorizations(EnumSet.of(RoleAuthorization.AMBARI_MANAGE_STACK_VERSIONS));
    setRequiredUpdateAuthorizations(EnumSet.of(RoleAuthorization.AMBARI_MANAGE_STACK_VERSIONS, RoleAuthorization.AMBARI_EDIT_STACK_REPOS));

    setRequiredGetAuthorizations(EnumSet.of(
        RoleAuthorization.AMBARI_MANAGE_STACK_VERSIONS,
        RoleAuthorization.AMBARI_EDIT_STACK_REPOS,
        RoleAuthorization.CLUSTER_VIEW_STACK_DETAILS,
        RoleAuthorization.CLUSTER_UPGRADE_DOWNGRADE_STACK));
  }

  @Override
  protected RequestStatus createResourcesAuthorized(final Request request)
      throws SystemException,
      UnsupportedPropertyException,
      ResourceAlreadyExistsException,
      NoSuchParentResourceException {

    for (final Map<String, Object> properties : request.getProperties()) {
      createResources(new Command<Void>() {

        @Override
        public Void invoke() throws AmbariException {
          final String[] requiredProperties = {
              REPOSITORY_VERSION_DISPLAY_NAME_PROPERTY_ID,
              SUBRESOURCE_OPERATING_SYSTEMS_PROPERTY_ID,
              REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID,
              REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID,
              REPOSITORY_VERSION_REPOSITORY_VERSION_PROPERTY_ID
          };
          for (String propertyName: requiredProperties) {
            if (properties.get(propertyName) == null) {
              throw new AmbariException("Property " + propertyName + " should be provided");
            }
          }
          final ExtensionRepositoryVersionEntity entity = toRepositoryVersionEntity(properties);

          if (repositoryVersionDAO.findByDisplayName(entity.getDisplayName()) != null) {
            throw new AmbariException("Repository version with name " + entity.getDisplayName() + " already exists");
          }
          if (repositoryVersionDAO.findByExtensionAndVersion(entity.getExtension(), entity.getVersion()) != null) {
            throw new AmbariException("Repository version for extension " + entity.getExtension() + " and version " + entity.getVersion() + " already exists");
          }
          validateRepositoryVersion(entity);
          repositoryVersionDAO.create(entity);
          notifyCreate(Resource.Type.RepositoryVersion, request);
          return null;
        }
      });
    }

    return getRequestStatus(null);
  }

  @Override
  protected Set<Resource> getResourcesAuthorized(Request request, Predicate predicate)
      throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {
    final Set<Resource> resources = new HashSet<Resource>();
    final Set<String> requestedIds = getRequestPropertyIds(request, predicate);
    final Set<Map<String, Object>> propertyMaps = getPropertyMaps(predicate);

    List<ExtensionRepositoryVersionEntity> requestedEntities = new ArrayList<ExtensionRepositoryVersionEntity>();
    for (Map<String, Object> propertyMap: propertyMaps) {
      final ExtensionId extensionId = getExtensionInformationFromUrl(propertyMap);

      if (extensionId != null && propertyMaps.size() == 1 && propertyMap.get(REPOSITORY_VERSION_ID_PROPERTY_ID) == null) {
        requestedEntities.addAll(repositoryVersionDAO.findByExtension(extensionId));
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
      final Resource resource = new ResourceImpl(Resource.Type.RepositoryVersion);

      setResourceProperty(resource, REPOSITORY_VERSION_ID_PROPERTY_ID, entity.getId(), requestedIds);
      setResourceProperty(resource, REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID, entity.getExtensionName(), requestedIds);
      setResourceProperty(resource, REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID, entity.getExtensionVersion(), requestedIds);
      setResourceProperty(resource, REPOSITORY_VERSION_DISPLAY_NAME_PROPERTY_ID, entity.getDisplayName(), requestedIds);
      setResourceProperty(resource, REPOSITORY_VERSION_REPOSITORY_VERSION_PROPERTY_ID, entity.getVersion(), requestedIds);

      resources.add(resource);
    }
    return resources;
  }

  @Override
  @Transactional
  protected RequestStatus updateResourcesAuthorized(Request request, Predicate predicate)
    throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {
    final Set<Map<String, Object>> propertyMaps = request.getProperties();

    modifyResources(new Command<Void>() {
      @Override
      public Void invoke() throws AmbariException, AuthorizationException {
        for (Map<String, Object> propertyMap : propertyMaps) {
          final Long id;
          try {
            id = Long.parseLong(propertyMap.get(REPOSITORY_VERSION_ID_PROPERTY_ID).toString());
          } catch (Exception ex) {
            throw new AmbariException("Repository version should have numerical id");
          }

          final ExtensionRepositoryVersionEntity entity = repositoryVersionDAO.findByPK(id);
          if (entity == null) {
            throw new ObjectNotFoundException("There is no repository version with id " + id);
          }

		  //TODO - Need to do a lookup based on the stack?
          // Prevent changing repo version if there's already a cluster version that has performed some meaningful action on it.
          /*ExtensionEntity extensionEntity = entity.getExtension();
          String extensionName = extensionEntity.getExtensionName();
          String extensionVersion = extensionEntity.getExtensionVersion();

          final List<ClusterVersionEntity> clusterVersionEntities = clusterVersionDAO.findByStackAndVersion(
              extensionName, extensionVersion, entity.getVersion());

          if (!clusterVersionEntities.isEmpty()) {
            final ClusterVersionEntity firstClusterVersion = clusterVersionEntities.get(0);
            throw new AmbariException("Upgrade pack can't be changed for repository version which has a state of " +
              firstClusterVersion.getState().name() + " on cluster " + firstClusterVersion.getClusterEntity().getClusterName());
          }*/

          List<OperatingSystemEntity> operatingSystemEntities = null;

          if (StringUtils.isNotBlank(ObjectUtils.toString(propertyMap.get(SUBRESOURCE_OPERATING_SYSTEMS_PROPERTY_ID)))) {
            if(!AuthorizationHelper.isAuthorized(ResourceType.AMBARI,null, RoleAuthorization.AMBARI_EDIT_STACK_REPOS)) {
              throw new AuthorizationException("The authenticated user does not have authorization to modify extension repositories");
            }

            final Object operatingSystems = propertyMap.get(SUBRESOURCE_OPERATING_SYSTEMS_PROPERTY_ID);
            final String operatingSystemsJson = gson.toJson(operatingSystems);
            try {
              operatingSystemEntities = repositoryVersionHelper.parseOperatingSystems(operatingSystemsJson);
            } catch (Exception ex) {
              throw new AmbariException("Json structure for operating systems is incorrect", ex);
            }
            entity.setOperatingSystems(operatingSystemsJson);
          }

          if (StringUtils.isNotBlank(ObjectUtils.toString(propertyMap.get(REPOSITORY_VERSION_DISPLAY_NAME_PROPERTY_ID)))) {
            entity.setDisplayName(propertyMap.get(REPOSITORY_VERSION_DISPLAY_NAME_PROPERTY_ID).toString());
          }

          validateRepositoryVersion(entity);
          repositoryVersionDAO.merge(entity);

          //
          // Update metaInfo table as well
          //
          if (operatingSystemEntities != null) {
		ExtensionEntity extensionEntity = entity.getExtension();
            String extensionName = extensionEntity.getExtensionName();
            String extensionVersion = extensionEntity.getExtensionVersion();
            for (OperatingSystemEntity osEntity : operatingSystemEntities) {
              List<RepositoryEntity> repositories = osEntity.getRepositories();
              for (RepositoryEntity repository : repositories) {
                ambariMetaInfo.updateExtensionRepoBaseURL(extensionName, extensionVersion, osEntity.getOsType(), repository.getRepositoryId(), repository.getBaseUrl());
              }
            }
          }
        }
        return null;
      }
    });

    return getRequestStatus(null);
  }

  @Override
  protected RequestStatus deleteResourcesAuthorized(Predicate predicate)
      throws SystemException, UnsupportedPropertyException, NoSuchResourceException, NoSuchParentResourceException {
    final Set<Map<String, Object>> propertyMaps = getPropertyMaps(predicate);

    final List<ExtensionRepositoryVersionEntity> entitiesToBeRemoved = new ArrayList<ExtensionRepositoryVersionEntity>();
    for (Map<String, Object> propertyMap : propertyMaps) {
      final Long id;
      try {
        id = Long.parseLong(propertyMap.get(REPOSITORY_VERSION_ID_PROPERTY_ID).toString());
      } catch (Exception ex) {
        throw new SystemException("Repository version should have numerical id");
      }

      final ExtensionRepositoryVersionEntity entity = repositoryVersionDAO.findByPK(id);
      if (entity == null) {
        throw new NoSuchResourceException("There is no repository version with id " + id);
      }

      //TODO - Need to do a lookup based on the stack?
      /*ExtensionEntity extensionEntity = entity.getExtension();
      String extensionName = extensionEntity.getExtensionName();
      String extensionVersion = extensionEntity.getExtensionVersion();

      final List<ClusterVersionEntity> clusterVersionEntities = clusterVersionDAO.findByExtensionAndVersion(
          extensionName, extensionVersion, entity.getVersion());

      final List<RepositoryVersionState> forbiddenToDeleteStates = Lists.newArrayList(
          RepositoryVersionState.CURRENT,
          RepositoryVersionState.INSTALLED,
          RepositoryVersionState.INSTALLING,
          RepositoryVersionState.UPGRADED,
          RepositoryVersionState.UPGRADING);
      for (ClusterVersionEntity clusterVersionEntity : clusterVersionEntities) {
        if (clusterVersionEntity.getRepositoryVersion().getId().equals(id) && forbiddenToDeleteStates.contains(clusterVersionEntity.getState())) {
          throw new SystemException("Repository version can't be deleted as it is " +
              clusterVersionEntity.getState().name() + " on cluster " + clusterVersionEntity.getClusterEntity().getClusterName());
        }
      }*/

      entitiesToBeRemoved.add(entity);
    }

    for (ExtensionRepositoryVersionEntity entity: entitiesToBeRemoved) {
      repositoryVersionDAO.remove(entity);
    }

    return getRequestStatus(null);
  }

  @Override
  protected Set<String> getPKPropertyIds() {
    return pkPropertyIds;
  }

  /**
   * Validates newly created repository versions to contain actual information.
   *
   * @param repositoryVersion repository version
   * @throws AmbariException exception with error message
   */
  protected void validateRepositoryVersion(ExtensionRepositoryVersionEntity repositoryVersion) throws AmbariException {
    final ExtensionId requiredExtension = new ExtensionId(repositoryVersion.getExtension());

    final String extensionName = requiredExtension.getExtensionName();
    final String extensionMajorVersion = requiredExtension.getExtensionVersion();
    final String extensionFullName = requiredExtension.getExtensionId();

    // check that extension exists
    final ExtensionInfo extensionInfo = ambariMetaInfo.getExtension(extensionName, extensionMajorVersion);
    if (extensionInfo == null) {
      throw new AmbariException("Extension " + extensionFullName + " doesn't exist");
    }

    //TODO - do we need this check?  If so fix the code to look up based on extensions
    /*if (!upgradePackExists(repositoryVersion.getVersion())) {
      throw new AmbariException("Extension " + extensionFullName + " doesn't have upgrade packages");
    }*/

    // List of all repo urls that are already added at extension
    Set<String> existingRepoUrls = new HashSet<String>();
    List<ExtensionRepositoryVersionEntity> existingRepoVersions = repositoryVersionDAO.findByExtension(requiredExtension);
    for (ExtensionRepositoryVersionEntity existingRepoVersion : existingRepoVersions) {
      for (OperatingSystemEntity operatingSystemEntity : existingRepoVersion.getOperatingSystems()) {
        for (RepositoryEntity repositoryEntity : operatingSystemEntity.getRepositories()) {
          if (! existingRepoVersion.getId().equals(repositoryVersion.getId())) { // Allow modifying already defined repo version
            existingRepoUrls.add(repositoryEntity.getBaseUrl());
          }
        }
      }
    }

    // check that repositories contain only supported operating systems
    final Set<String> osSupported = new HashSet<String>();
    for (OperatingSystemInfo osInfo: ambariMetaInfo.getExtensionOperatingSystems(extensionName, extensionMajorVersion)) {
      osSupported.add(osInfo.getOsType());
    }
    final Set<String> osRepositoryVersion = new HashSet<String>();
    for (OperatingSystemEntity os: repositoryVersion.getOperatingSystems()) {
      osRepositoryVersion.add(os.getOsType());

      for (RepositoryEntity repositoryEntity : os.getRepositories()) {
        String baseUrl = repositoryEntity.getBaseUrl();
        if (existingRepoUrls.contains(baseUrl)) {
          throw new AmbariException("Base url " + baseUrl + " is already defined for another repository version. " +
                  "Setting up base urls that contain the same versions of components will cause upgrade to fail.");
        }
      }
    }
    if (osRepositoryVersion.isEmpty()) {
      throw new AmbariException("At least one set of repositories for OS should be provided");
    }
    for (String os: osRepositoryVersion) {
      if (!osSupported.isEmpty() && !osSupported.contains(os)) {
        throw new AmbariException("Operating system type " + os + " is not supported by extension " + extensionFullName);
      }
    }

    //TODO - Do we need this check?
    /*if (!RepositoryVersionEntity.isVersionInStack(repositoryVersion.getStackId(), repositoryVersion.getVersion())) {
        throw new AmbariException(MessageFormat.format("Version {0} needs to belong to stack {1}",
            repositoryVersion.getVersion(), repositoryVersion.getStackName() + "-" + repositoryVersion.getStackVersion()));
    }*/
  }

  /**
   * Check for required upgrade pack across all stack definitions
   * @param checkVersion version to check (e.g. 2.2.3.0-1111)
   * @return existence flag
   */
  private boolean upgradePackExists(String checkVersion) throws AmbariException{
    Collection<StackInfo> stacks = new ArrayList<StackInfo>();

    // Search results only in the installed stacks
    for (Cluster cluster : clusters.get().getClusters().values()){
      stacks.add(ambariMetaInfo.getStack(cluster.getCurrentStackVersion().getStackName(),
                                          cluster.getCurrentStackVersion().getStackVersion()));
    }

    for (StackInfo si : stacks){
      Map<String, UpgradePack> upgradePacks = si.getUpgradePacks();
      if (upgradePacks!=null) {
        for (UpgradePack upgradePack: upgradePacks.values()){
          if (upgradePack.canBeApplied(checkVersion)) {
            // If we found at least one match, the rest could be skipped
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Transforms map of json properties to repository version entity.
   *
   * @param properties json map
   * @return constructed entity
   * @throws AmbariException if some properties are missing or json has incorrect structure
   */
  protected ExtensionRepositoryVersionEntity toRepositoryVersionEntity(Map<String, Object> properties) throws AmbariException {
    final ExtensionRepositoryVersionEntity entity = new ExtensionRepositoryVersionEntity();
    final String extensionName = properties.get(REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID).toString();
    final String extensionVersion = properties.get(REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID).toString();

    ExtensionEntity extensionEntity = extensionDAO.find(extensionName, extensionVersion);

    entity.setDisplayName(properties.get(REPOSITORY_VERSION_DISPLAY_NAME_PROPERTY_ID).toString());
    entity.setExtension(extensionEntity);

    entity.setVersion(properties.get(REPOSITORY_VERSION_REPOSITORY_VERSION_PROPERTY_ID).toString());
    final Object operatingSystems = properties.get(SUBRESOURCE_OPERATING_SYSTEMS_PROPERTY_ID);
    final String operatingSystemsJson = gson.toJson(operatingSystems);
    try {
      repositoryVersionHelper.parseOperatingSystems(operatingSystemsJson);
    } catch (Exception ex) {
      throw new AmbariException("Json structure for operating systems is incorrect", ex);
    }
    entity.setOperatingSystems(operatingSystemsJson);
    return entity;
  }

  protected ExtensionId getExtensionInformationFromUrl(Map<String, Object> propertyMap) {
    if (propertyMap.containsKey(REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID) && propertyMap.containsKey(REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID)) {
      return new ExtensionId(propertyMap.get(REPOSITORY_VERSION_EXTENSION_NAME_PROPERTY_ID).toString(), propertyMap.get(REPOSITORY_VERSION_EXTENSION_VERSION_PROPERTY_ID).toString());
    }
    return null;
  }

  @Override
  protected ResourceType getResourceType(Request request, Predicate predicate) {
    // This information is not associated with any particular resource
    return null;
  }
}
