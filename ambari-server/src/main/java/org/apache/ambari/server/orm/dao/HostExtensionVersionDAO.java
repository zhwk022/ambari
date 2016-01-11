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

import java.util.Collection;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import org.apache.ambari.server.orm.RequiresSession;
import org.apache.ambari.server.orm.entities.HostExtensionVersionEntity;
import org.apache.ambari.server.state.RepositoryVersionState;
import org.apache.ambari.server.state.ExtensionId;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

/**
 * The {@link org.apache.ambari.server.orm.dao.HostExtensionVersionDAO} class manages the {@link org.apache.ambari.server.orm.entities.HostExtensionVersionEntity}
 * instances associated with a host. Each host can have multiple extension versions in {@link org.apache.ambari.server.state.RepositoryVersionState#INSTALLED}
 * which are installed, exactly one extension version that is either {@link org.apache.ambari.server.state.RepositoryVersionState#CURRENT} or
 * {@link org.apache.ambari.server.state.RepositoryVersionState#UPGRADING}.
 */
@Singleton
public class HostExtensionVersionDAO {
  @Inject
  Provider<EntityManager> entityManagerProvider;
  @Inject
  DaoUtils daoUtils;

  /**
   * Get the object with the given id.
   *
   * @param id Primary key id
   * @return Return the object with the given primary key
   */
  @RequiresSession
  public HostExtensionVersionEntity findByPK(long id) {
    return entityManagerProvider.get().find(HostExtensionVersionEntity.class, id);
  }

  /**
   * Retrieve all of the host versions for the given cluster name, extension name,
   * and extension version.
   *
   * @param clusterName
   *          Cluster name
   * @param extensionId
   *          Extension (e.g., EXT-1.0)
   * @param version
   *          Extension version (e.g., 1.0.0.1-995)
   * @return Return all of the host versions that match the criteria.
   */
  @RequiresSession
  public List<HostExtensionVersionEntity> findByClusterExtensionAndVersion(
      String clusterName, ExtensionId extensionId, String version) {
    final TypedQuery<HostExtensionVersionEntity> query = entityManagerProvider.get().createNamedQuery("hostExtensionVersionByClusterAndExtensionAndVersion", HostExtensionVersionEntity.class);
    query.setParameter("clusterName", clusterName);
    query.setParameter("extensionName", extensionId.getExtensionName());
    query.setParameter("extensionVersion", extensionId.getExtensionVersion());
    query.setParameter("version", version);

    return daoUtils.selectList(query);
  }

  /**
   * Retrieve all of the host versions for the given host name across all clusters.
   *
   * @param hostName FQDN of host
   * @return Return all of the host versions that match the criteria.
   */
  @RequiresSession
  public List<HostExtensionVersionEntity> findByHost(String hostName) {
    final TypedQuery<HostExtensionVersionEntity> query = entityManagerProvider.get()
        .createNamedQuery("hostExtensionVersionByHostname", HostExtensionVersionEntity.class);
    query.setParameter("hostName", hostName);

    return daoUtils.selectList(query);
  }

  /**
   * Retrieve all of the host versions for the given cluster name and host name.
   *
   * @param clusterName Cluster name
   * @param hostName FQDN of host
   * @return Return all of the host versions that match the criteria.
   */
  @RequiresSession
  public List<HostExtensionVersionEntity> findByClusterAndHost(String  clusterName, String hostName) {
    final TypedQuery<HostExtensionVersionEntity> query = entityManagerProvider.get()
        .createNamedQuery("hostExtensionVersionByClusterAndHostname", HostExtensionVersionEntity.class);
    query.setParameter("clusterName", clusterName);
    query.setParameter("hostName", hostName);

    return daoUtils.selectList(query);
  }

  /**
   * Retrieve all of the host versions for the given cluster name, host name, and state.
   *
   * @param clusterName Cluster name
   * @param hostName FQDN of host
   * @param state repository version state
   * @return Return all of the host versions that match the criteria.
   */
  @RequiresSession
  public List<HostExtensionVersionEntity> findByClusterHostAndState(String  clusterName, String hostName, RepositoryVersionState state) {
    final TypedQuery<HostExtensionVersionEntity> query = entityManagerProvider.get()
        .createNamedQuery("hostExtensionVersionByClusterHostnameAndState", HostExtensionVersionEntity.class);
    query.setParameter("clusterName", clusterName);
    query.setParameter("hostName", hostName);
    query.setParameter("state", state);

    return daoUtils.selectList(query);
  }

  /**
   * Retrieve the single host version whose state is {@link org.apache.ambari.server.state.RepositoryVersionState#CURRENT}, of which there should be exactly one at all times
   * for the given host.
   *
   * @param clusterName Cluster name
   * @param hostName Host name
   * @return Returns the single host version for this host whose state is {@link org.apache.ambari.server.state.RepositoryVersionState#CURRENT}, or {@code null} otherwise.
   */
  @RequiresSession
  public HostExtensionVersionEntity findByHostAndStateCurrent(String clusterName, String hostName) {
    try {
      List<?> results = findByClusterHostAndState(clusterName, hostName, RepositoryVersionState.CURRENT);
      if (results.isEmpty()) {
        return null;
      } else {
        if (results.size() == 1) {
          return (HostExtensionVersionEntity) results.get(0);
        }
      }
      throw new NonUniqueResultException();
    } catch (NoResultException ignored) {
      return null;
    }
  }

  /**
   * Retrieve the single host version for the given cluster, extension name, extension
   * version, and host name.
   *
   * @param clusterName
   *          Cluster name
   * @param extensionId
   *          Extension ID (e.g., EXT-1.0)
   * @param version
   *          Extension version (e.g., 1.0.0.1-995)
   * @param hostName
   *          FQDN of host
   * @return Returns the single host version that matches the criteria.
   */
  @RequiresSession
  public HostExtensionVersionEntity findByClusterExtensionVersionAndHost(String clusterName,
      ExtensionId extensionId, String version, String hostName) {

    final TypedQuery<HostExtensionVersionEntity> query = entityManagerProvider.get()
        .createNamedQuery("hostExtensionVersionByClusterExtensionVersionAndHostname", HostExtensionVersionEntity.class);
    query.setParameter("clusterName", clusterName);
    query.setParameter("extensionName", extensionId.getExtensionName());
    query.setParameter("extensionVersion", extensionId.getExtensionVersion());
    query.setParameter("version", version);
    query.setParameter("hostName", hostName);

    return daoUtils.selectSingle(query);
  }

  @RequiresSession
  public List<HostExtensionVersionEntity> findAll() {
    return daoUtils.selectAll(entityManagerProvider.get(), HostExtensionVersionEntity.class);
  }

  @Transactional
  public void refresh(HostExtensionVersionEntity hostExtensionVersionEntity) {
    entityManagerProvider.get().refresh(hostExtensionVersionEntity);
  }

  @Transactional
  public void create(HostExtensionVersionEntity hostExtensionVersionEntity) {
    entityManagerProvider.get().persist(hostExtensionVersionEntity);
  }

  @Transactional
  public HostExtensionVersionEntity merge(HostExtensionVersionEntity hostExtensionVersionEntity) {
    return entityManagerProvider.get().merge(hostExtensionVersionEntity);
  }

  @Transactional
  public void remove(HostExtensionVersionEntity hostExtensionVersionEntity) {
    entityManagerProvider.get().remove(merge(hostExtensionVersionEntity));
  }

  @Transactional
  public void removeByHostName(String hostName) {
    Collection<HostExtensionVersionEntity> hostExtensionVersions = this.findByHost(hostName);
    for (HostExtensionVersionEntity hostExtensionVersion : hostExtensionVersions) {
      this.remove(hostExtensionVersion);
    }
  }

  @Transactional
  public void removeByPK(long id) {
    remove(findByPK(id));
  }
}
