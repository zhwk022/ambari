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

package org.apache.ambari.server.controller;

import org.apache.ambari.server.state.PropertyDependencyInfo;


public class ExtensionConfigurationDependencyResponse {

  private String extensionName;
  private String extensionVersion;
  private String serviceName;
  private String propertyName;
  private String dependencyName;
  private String dependencyType;


  public ExtensionConfigurationDependencyResponse(PropertyDependencyInfo info) {
    this(info.getName(), info.getType());
  }

  public ExtensionConfigurationDependencyResponse(String dependencyName) {
    this.dependencyName = dependencyName;
  }

  public ExtensionConfigurationDependencyResponse(String dependencyName,
                                              String dependencyType) {
    this.dependencyName = dependencyName;
    this.dependencyType = dependencyType;
  }

  public String getExtensionName() {
    return extensionName;
  }

  public void setExtensionName(String extensionName) {
    this.extensionName = extensionName;
  }

  public String getExtensionVersion() {
    return extensionVersion;
  }

  public void setExtensionVersion(String extensionVersion) {
    this.extensionVersion = extensionVersion;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public void setPropertyName(String propertyName) {
    this.propertyName = propertyName;
  }

  public String getDependencyName() {
    return dependencyName;
  }

  public void setDependencyName(String dependencyName) {
    this.dependencyName = dependencyName;
  }

  public String getDependencyType() {
    return dependencyType;
  }

  public void setDependencyType(String dependencyType) {
    this.dependencyType = dependencyType;
  }
}
