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

import java.util.List;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import org.apache.ambari.server.orm.RequiresSession;
import org.apache.ambari.server.orm.entities.ClusterExtensionVersionEntity;
import org.apache.ambari.server.state.RepositoryVersionState;
import org.apache.ambari.server.state.ExtensionId;

import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

/**
 * The {@link ClusterExtensionVersionDAO} class manages the {@link ClusterExtensionVersionEntity} instances associated with a cluster.
 * Each cluster can have multiple extension versions {@link org.apache.ambari.server.state.RepositoryVersionState#INSTALLED},
 * exactly one extension version for each extension name that is {@link org.apache.ambari.server.state.RepositoryVersionState#CURRENT}, and at most one
 * extension version for each extension name that is {@link org.apache.ambari.server.state.RepositoryVersionState#UPGRADING}.
 */
@Singleton
public class ClusterExtensionVersionDAO extends CrudDAO<ClusterExtensionVersionEntity, Long>{
  /**
   * Constructor.
   */
  public ClusterExtensionVersionDAO() {
    super(ClusterExtensionVersionEntity.class);
  }

  /**
   * Retrieve all of the cluster versions for the given extension and version.
   *
   * @param extensionName
   *          the extension name (e.g., EXT)
   * @param extensionVersion
   *          the extension version (e.g., 1.0)
   * @param version
   *          Repository version (e.g., 1.0.0.1-995)
   * @return Return a list of cluster versions that match the extension and version.
   */
  @RequiresSession
  public List<ClusterExtensionVersionEntity> findByExtensionAndVersion(String extensionName,
      String extensionVersion, String version) {
    final TypedQuery<ClusterExtensionVersionEntity> query = entityManagerProvider.get().createNamedQuery("clusterExtensionVersionByExtensionVersion", ClusterExtensionVersionEntity.class);
    query.setParameter("extensionName", extensionName);
    query.setParameter("extensionVersion", extensionVersion);
    query.setParameter("version", version);

    return daoUtils.selectList(query);
  }

  /**
   * Get the cluster version for the given cluster name, extension name, and extension
   * version.
   *
   * @param clusterName
   *          Cluster name
   * @param extensionId
   *          Extension id (e.g., EXT-1.0)
   * @param version
   *          Repository version (e.g., 1.0.0.1-995)
   * @return Return all of the cluster versions associated with the given
   *         cluster.
   */
  @RequiresSession
  public ClusterExtensionVersionEntity findByClusterAndExtensionAndVersion(
      String clusterName, ExtensionId extensionId, String version) {
    final TypedQuery<ClusterExtensionVersionEntity> query = entityManagerProvider.get()
        .createNamedQuery("clusterExtensionVersionByClusterAndExtensionAndVersion", ClusterExtensionVersionEntity.class);
    query.setParameter("clusterName", clusterName);
    query.setParameter("extensionName", extensionId.getExtensionName());
    query.setParameter("extensionVersion", extensionId.getExtensionVersion());
    query.setParameter("version", version);

    return daoUtils.selectSingle(query);
  }

  /**
   * Retrieve all of the cluster versions for the given cluster.
   *
   * @param clusterName Cluster name
   * @return Return all of the cluster versions associated with the given cluster.
   */
  @RequiresSession
  public List<ClusterExtensionVersionEntity> findByCluster(String clusterName) {
    final TypedQuery<ClusterExtensionVersionEntity> query = entityManagerProvider.get()
        .createNamedQuery("clusterExtensionVersionByCluster", ClusterExtensionVersionEntity.class);
    query.setParameter("clusterName", clusterName);

    return daoUtils.selectList(query);
  }

  /**
   * Retrieve all of the cluster versions for the given cluster.
   *
   * @param clusterName Cluster name
   * @return Return all of the cluster versions associated with the given cluster.
   */
  @RequiresSession
  public List<ClusterExtensionVersionEntity> findByClusterAndExtensionName(String clusterName, String extensionName) {
    final TypedQuery<ClusterExtensionVersionEntity> query = entityManagerProvider.get()
        .createNamedQuery("clusterExtensionVersionByClusterAndExtensionName", ClusterExtensionVersionEntity.class);
    query.setParameter("clusterName", clusterName);
    query.setParameter("extensionName", extensionName);

    return daoUtils.selectList(query);
  }

  /**
   * Retrieve the single cluster extension version whose state is {@link org.apache.ambari.server.state.RepositoryVersionState#CURRENT}, of which there should be exactly one at all times
   * for the given cluster and extension name.
   *
   * @param clusterName Cluster name
   * @param extensionName Extension name
   * @return Returns the single cluster extension version for the cluster's extension with the specified name whose state is {@link org.apache.ambari.server.state.RepositoryVersionState#CURRENT}, or {@code null} otherwise.
   */
  @RequiresSession
  public ClusterExtensionVersionEntity findByClusterAndExtensionNameAndStateCurrent(String clusterName, String extensionName) {
    final TypedQuery<ClusterExtensionVersionEntity> query = entityManagerProvider.get()
        .createNamedQuery("clusterExtensionVersionByClusterAndExtensionNameAndState", ClusterExtensionVersionEntity.class);
    query.setParameter("clusterName", clusterName);
    query.setParameter("extensionName", extensionName);
    query.setParameter("state", RepositoryVersionState.CURRENT);

    try {
      List results = query.getResultList();
      if (results.isEmpty()) {
        return null;
      } else {
        if (results.size() == 1) {
          return (ClusterExtensionVersionEntity) results.get(0);
        }
      }
      throw new NonUniqueResultException();
    } catch (NoResultException ignored) {
      return null;
    }
  }

  /**
   * Retrieve all of the cluster versions for the cluster with the given name and a state.
   *
   * @param clusterName Cluster name
   * @param state Cluster version state
   * @return Returns a list of cluster versions for the given cluster and a state.
   */
  @RequiresSession
  public List<ClusterExtensionVersionEntity> findByClusterAndState(String clusterName, RepositoryVersionState state) {
    final TypedQuery<ClusterExtensionVersionEntity> query = entityManagerProvider.get()
        .createNamedQuery("clusterExtensionVersionByClusterAndState", ClusterExtensionVersionEntity.class);
    query.setParameter("clusterName", clusterName);
    query.setParameter("state", state);

    return daoUtils.selectList(query);
  }

  /**
   * Construct a Cluster Extension Version. Additionally this will update parent connection relations without
   * forcing refresh of parent entity
   * @param entity entity to create
   */
  @Override
  @Transactional
  public void create(ClusterExtensionVersionEntity entity) throws IllegalArgumentException {
    // check if repository version is not missing, to avoid NPE
    if (entity.getRepositoryVersion() == null) {
      throw new IllegalArgumentException("RepositoryVersion argument is not set for the entity");
    }

    super.create(entity);
    //entity.getRepositoryVersion().updateClusterVersionEntityRelation(entity);
  }
}
