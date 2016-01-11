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

import org.apache.ambari.server.state.AutoDeployInfo;
import org.apache.ambari.server.state.ComponentInfo;
import org.apache.ambari.server.state.CustomCommandDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Extension service component response.
 */
public class ExtensionServiceComponentResponse {
  /**
   * extension name
   */
  private String extensionName;

  /**
   * extension version
   */
  private String extensionVersion;

  /**
   * service name
   */
  private String serviceName;

  /**
   * component name
   */
  private String componentName;

  /**
   * component display name
   */
  private String componentDisplayName;

  /**
   * component category
   */
  private String componentCategory;

  /**
   * is component a client component
   */
  private boolean isClient;

  /**
   * is component a master component
   */
  private boolean isMaster;

  /**
   * cardinality requirement
   */
  private String cardinality;

  /**
   * does the component need to advertise a version
   */
  private boolean versionAdvertised;

  /**
   * should the component be installed on the same node as server components
   */
  private boolean notPreferableOnServerComponents;

  private boolean notValuable;

  private boolean useCardinalityForLayout;

  private String layout = null;

  private int idealMinimum = 1;

  /**
   * auto deploy information
   */
  private AutoDeployInfo autoDeploy;

  /**
   * The names of the custom commands defined for the component.
   */
  private List<String> customCommands;


  /**
   * Constructor.
   *
   * @param component
   *          the component to generate the response from (not {@code null}).
   */
  public ExtensionServiceComponentResponse(ComponentInfo component) {
    componentName = component.getName();
    componentDisplayName = component.getDisplayName();
    componentCategory = component.getCategory();
    isClient = component.isClient();
    isMaster = component.isMaster();
    cardinality = component.getCardinality();
    versionAdvertised = component.isVersionAdvertised();
    notPreferableOnServerComponents = component.isNotPreferableOnServerComponents();
    notValuable = component.isNotValuable();
    useCardinalityForLayout = component.isUseCardinalityForLayout();
    layout = component.getLayout();
    autoDeploy = component.getAutoDeploy();

    // the custom command names defined for this component
    List<CustomCommandDefinition> definitions = component.getCustomCommands();
    if (null == definitions || definitions.size() == 0) {
      customCommands = Collections.emptyList();
    } else {
      customCommands = new ArrayList<String>(definitions.size());
      for (CustomCommandDefinition command : definitions) {
        customCommands.add(command.getName());
      }
    }
  }

  /**
   * Get extension name.
   *
   * @return extension name
   */
  public String getExtensionName() {
    return extensionName;
  }

  /**
   * Set extension name.
   *
   * @param extensionName  extension name
   */
  public void setExtensionName(String extensionName) {
    this.extensionName = extensionName;
  }

  /**
   * Get extension version.
   *
   * @return extension version
   */
  public String getExtensionVersion() {
    return extensionVersion;
  }

  /**
   * Set extension version.
   *
   * @param extensionVersion  extension version
   */
  public void setExtensionVersion(String extensionVersion) {
    this.extensionVersion = extensionVersion;
  }

  /**
   * Get service name.
   *
   * @return service name
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * Set service name.
   *
   * @param serviceName  service name
   */
  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  /**
   * Get component name.
   *
   * @return component name
   */
  public String getComponentName() {
    return componentName;
  }

  /**
   * Set component name.
   *
   * @param componentName  component name
   */
  public void setComponentName(String componentName) {
    this.componentName = componentName;
  }

  /**
   * Get component display name.
   *
   * @return component display name
   */

  public String getComponentDisplayName() {
    return componentDisplayName;
  }

  /**
   * Set component display name.
   *
   * @param componentDisplayName  component display name
   */
  public void setComponentDisplayName(String componentDisplayName) {
    this.componentDisplayName = componentDisplayName;
  }

  /**
   * Get component category.
   *
   * @return component category
   */
  public String getComponentCategory() {
    return componentCategory;
  }

  /**
   * Set the component category.
   *
   * @param componentCategory  component category
   */
  public void setComponentCategory(String componentCategory) {
    this.componentCategory = componentCategory;
  }

  /**
   * Determine whether the component is a client component.
   *
   * @return whether the component is a client component
   */
  public boolean isClient() {
    return isClient;
  }

  /**
   * Set whether the component is a client component.
   *
   * @param isClient whether the component is a client
   */
  public void setClient(boolean isClient) {
    this.isClient = isClient;
  }

  /**
   * Determine whether the component is a master component.
   *
   * @return whether the component is a master component
   */
  public boolean isMaster() {
    return isMaster;
  }

  /**
   * Set whether the component is a master component.
   *
   * @param isMaster whether the component is a master
   */
  public void setMaster(boolean isMaster) {
    this.isMaster = isMaster;
  }

  /**
   * Get cardinality requirement of component.
   *
   * @return component cardinality requirement
   */
  public String getCardinality() {
    return cardinality;
  }

  /**
   * Set component cardinality requirement.
   *
   * @param cardinality cardinality requirement
   */
  public void setCardinality(String cardinality) {
    this.cardinality = cardinality;
  }


  /**
   * Get whether the components needs to advertise a version.
   *
   * @return Whether the components needs to advertise a version
   */
  public boolean isVersionAdvertised() {
    return versionAdvertised;
  }

  /**
   * Set whether the component needs to advertise a version.
   *
   * @param versionAdvertised whether the component needs to advertise a version
   */
  public void setVersionAdvertised(boolean versionAdvertised) {
    this.versionAdvertised = versionAdvertised;
  }

  public boolean isNotPreferableOnServerComponents() {
    return notPreferableOnServerComponents;
  }

  public void setNotPreferableOnServerComponents(boolean notPreferableOnServerComponents) {
    this.notPreferableOnServerComponents = notPreferableOnServerComponents;
  }

  public boolean isNotValuable() {
    return notValuable;
  }

  public void setNotValuable(boolean notValuable) {
    this.notValuable = notValuable;
  }

  public boolean isUseCardinalityForLayout() {
    return useCardinalityForLayout;
  }

  public void setUseCardinalityForLayout(boolean useCardinalityForLayout) {
    this.useCardinalityForLayout = useCardinalityForLayout;
  }

  public String getLayout() {
    return layout;
  }

  public void setLayout(String layout) {
    this.layout = layout;
  }

  public int getIdealMinimum() {
    return idealMinimum;
  }

  public void setIdealMinimum(int idealMinimum) {
    this.idealMinimum = idealMinimum;
  }

  /**
   * Get auto deploy information.
   *
   * @return auto deploy information
   */
  public AutoDeployInfo getAutoDeploy() {
    return autoDeploy;
  }

  /**
   * Set auto deploy information.
   *
   * @param autoDeploy auto deploy info
   */
  public void setAutoDeploy(AutoDeployInfo autoDeploy) {
    this.autoDeploy = autoDeploy;
  }

  /**
   * Gets the names of all of the custom commands for this component.
   *
   * @return the commands or an empty list (never {@code null}).
   */
  public List<String> getCustomCommands() {
    return customCommands;
  }
}
