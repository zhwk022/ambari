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

package org.apache.ambari.server.orm.entities;

import javax.persistence.*;

@javax.persistence.IdClass(HostComponentStateEntityPK.class)
@javax.persistence.Table(name = "hostcomponentstate", schema = "ambari", catalog = "")
@Entity
public class HostComponentStateEntity {

  private String clusterName;

  @javax.persistence.Column(name = "cluster_name", insertable = false, updatable = false)
  @Id
  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  private String hostName;

  @javax.persistence.Column(name = "host_name", insertable = false, updatable = false)
  @Id
  public String getHostName() {
    return hostName;
  }

  public void setHostName(String hostName) {
    this.hostName = hostName;
  }

  private String componentName;

  @javax.persistence.Column(name = "component_name")
  @Id
  public String getComponentName() {
    return componentName;
  }

  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  private String currentState;

  @javax.persistence.Column(name = "current_state")
  @Basic
  public String getCurrentState() {
    return currentState;
  }

  public void setCurrentState(String currentState) {
    this.currentState = currentState;
  }

  private String currentConfigVersion;

  @javax.persistence.Column(name = "current_config_version")
  @Basic
  public String getCurrentConfigVersion() {
    return currentConfigVersion;
  }

  public void setCurrentConfigVersion(String currentConfigVersion) {
    this.currentConfigVersion = currentConfigVersion;
  }

  private String currentStackVersion;

  @javax.persistence.Column(name = "current_stack_version")
  @Basic
  public String getCurrentStackVersion() {
    return currentStackVersion;
  }

  public void setCurrentStackVersion(String currentStackVersion) {
    this.currentStackVersion = currentStackVersion;
  }

  private ClusterEntity clusterEntity;

  @ManyToOne
  @JoinColumn(name = "cluster_name")
  public ClusterEntity getClusterEntity() {
    return clusterEntity;
  }

  public void setClusterEntity(ClusterEntity clusterEntity) {
    this.clusterEntity = clusterEntity;
  }

  private HostEntity hostEntity;

  @ManyToOne
  @JoinColumn(name = "host_name")
  public HostEntity getHostEntity() {
    return hostEntity;
  }

  public void setHostEntity(HostEntity hostEntity) {
    this.hostEntity = hostEntity;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HostComponentStateEntity that = (HostComponentStateEntity) o;

    if (clusterName != null ? !clusterName.equals(that.clusterName) : that.clusterName != null) return false;
    if (componentName != null ? !componentName.equals(that.componentName) : that.componentName != null) return false;
    if (currentConfigVersion != null ? !currentConfigVersion.equals(that.currentConfigVersion) : that.currentConfigVersion != null)
      return false;
    if (currentStackVersion != null ? !currentStackVersion.equals(that.currentStackVersion) : that.currentStackVersion != null)
      return false;
    if (currentState != null ? !currentState.equals(that.currentState) : that.currentState != null) return false;
    if (hostName != null ? !hostName.equals(that.hostName) : that.hostName != null) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = clusterName != null ? clusterName.hashCode() : 0;
    result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
    result = 31 * result + (componentName != null ? componentName.hashCode() : 0);
    result = 31 * result + (currentState != null ? currentState.hashCode() : 0);
    result = 31 * result + (currentConfigVersion != null ? currentConfigVersion.hashCode() : 0);
    result = 31 * result + (currentStackVersion != null ? currentStackVersion.hashCode() : 0);
    return result;
  }
}
