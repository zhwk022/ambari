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
package org.apache.ambari.server.orm.dao;

import java.text.MessageFormat;
import java.util.List;

import javax.persistence.TypedQuery;

import com.google.inject.persist.Transactional;
import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.orm.RequiresSession;
import org.apache.ambari.server.orm.entities.ExtensionRepositoryVersionEntity;
import org.apache.ambari.server.orm.entities.ExtensionEntity;
import org.apache.ambari.server.state.ExtensionId;

import com.google.inject.Singleton;

/**
 * DAO for repository versions.
 *
 */
@Singleton
public class ExtensionRepositoryVersionDAO extends CrudDAO<ExtensionRepositoryVersionEntity, Long> {
  /**
   * Constructor.
   */
  public ExtensionRepositoryVersionDAO() {
    super(ExtensionRepositoryVersionEntity.class);
  }

  /**
   * Retrieves repository version by name.
   *
   * @param displayName display name
   * @return null if there is no suitable repository version
   */
  @RequiresSession
  public ExtensionRepositoryVersionEntity findByDisplayName(String displayName) {
    // TODO, this assumes that the display name is unique, but neither the code nor the DB schema enforces this.
    final TypedQuery<ExtensionRepositoryVersionEntity> query = entityManagerProvider.get().createNamedQuery("extensionRepositoryVersionByDisplayName", ExtensionRepositoryVersionEntity.class);
    query.setParameter("displayname", displayName);
    return daoUtils.selectSingle(query);
  }

  /**
   * Retrieves repository version by extension.
   *
   * @param extensionId
   *          extensionId
   * @param version
   *          version
   * @return null if there is no suitable repository version
   */
  @RequiresSession
  public ExtensionRepositoryVersionEntity findByExtensionAndVersion(ExtensionId extensionId,
      String version) {
    return findByExtensionNameAndVersion(extensionId.getExtensionName(), version);
  }

  /**
   * Retrieves repository version by extension.
   *
   * @param extensionEntity Extension entity
   * @param version version
   * @return null if there is no suitable repository version
   */
  @RequiresSession
  public ExtensionRepositoryVersionEntity findByExtensionAndVersion(ExtensionEntity extensionEntity,
      String version) {
    return findByExtensionNameAndVersion(extensionEntity.getExtensionName(), version);
  }

  /**
   * Retrieves repository version, which is unique in this extension.
   *
   * @param extensionName Extension name such as HDP, HDPWIN, BIGTOP
   * @param version version
   * @return null if there is no suitable repository version
   */
  @RequiresSession
  public ExtensionRepositoryVersionEntity findByExtensionNameAndVersion(String extensionName, String version) {
    // TODO, need to make a unique composite key in DB using the extension_repo_version's extension_id and version.
    // Ideally, 1.0-1234 foo should be unique in all HDP extensions.
    // The composite key is slightly more relaxed since it would only prevent 2.3.0-1234 multiple times in the HDP 2.3 extension.
    // There's already business logic to prevent creating 2.3-1234 in the wrong extension such as HDP 2.2.
    final TypedQuery<ExtensionRepositoryVersionEntity> query = entityManagerProvider.get().createNamedQuery("repositoryVersionByExtensionNameAndVersion", ExtensionRepositoryVersionEntity.class);
    query.setParameter("extensionName", extensionName);
    query.setParameter("version", version);
    return daoUtils.selectSingle(query);
  }

  /**
   * Retrieves repository version by extension.
   *
   * @param extensionId extension id
   *          extension with major version
   * @return null if there is no suitable repository version
   */
  @RequiresSession
  public List<ExtensionRepositoryVersionEntity> findByExtension(ExtensionId extensionId) {
    final TypedQuery<ExtensionRepositoryVersionEntity> query = entityManagerProvider.get().createNamedQuery("repositoryVersionByExtension", ExtensionRepositoryVersionEntity.class);
    query.setParameter("extensionName", extensionId.getExtensionName());
    query.setParameter("extensionVersion", extensionId.getExtensionVersion());
    return daoUtils.selectList(query);
  }

  /**
   * Creates entity.
   *
   * @param entity entity to create
   */
  @Override
  @Transactional
  public void create(ExtensionRepositoryVersionEntity entity){
    super.create(entity);
  }

  /**
   * Validates and creates an object.
   * The version must be unique within this extension name.
   * @param extensionEntity Extension entity.
   * @param version Extension version, e.g., 1.0, 1.0.0.0
   * @param displayName Unique display name
   * @param operatingSystems JSON structure of repository URLs for each OS
   * @return Returns the object created if successful, and throws an exception otherwise.
   * @throws AmbariException
   */
  @Transactional
  public ExtensionRepositoryVersionEntity create(ExtensionEntity extensionEntity,
      String version, String displayName, String operatingSystems) throws AmbariException {

    if (extensionEntity == null || version == null || version.isEmpty()
        || displayName == null || displayName.isEmpty()) {
      throw new AmbariException("At least one of the required properties is null or empty");
    }

    ExtensionRepositoryVersionEntity existingByDisplayName = findByDisplayName(displayName);

    if (existingByDisplayName != null) {
      throw new AmbariException("Repository version with display name '" + displayName + "' already exists");
    }

    ExtensionRepositoryVersionEntity existingVersionInExtension = findByExtensionNameAndVersion(extensionEntity.getExtensionName(), version);

    if (existingVersionInExtension != null) {
      throw new AmbariException(MessageFormat.format("Repository Version for version {0} already exists, in extension {1}-{2}",
          version, existingVersionInExtension.getExtension().getExtensionName(), existingVersionInExtension.getExtension().getExtensionVersion()));
    }

    ExtensionRepositoryVersionEntity newEntity = new ExtensionRepositoryVersionEntity(
        extensionEntity, version, displayName, operatingSystems);
    this.create(newEntity);
    return newEntity;
  }
}
