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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.controller.AmbariManagementController;
/*import org.apache.ambari.server.controller.ExtensionConfigurationRequest;
import org.apache.ambari.server.controller.ExtensionConfigurationResponse;
import org.apache.ambari.server.controller.ExtensionLevelConfigurationRequest;
import org.apache.ambari.server.controller.ExtensionServiceComponentRequest;
import org.apache.ambari.server.controller.ExtensionServiceComponentResponse;
import org.apache.ambari.server.controller.ExtensionServiceRequest;
import org.apache.ambari.server.controller.ExtensionServiceResponse;*/
import org.apache.ambari.server.orm.entities.ExtensionEntity;
import org.apache.ambari.server.state.AutoDeployInfo;
import org.apache.ambari.server.state.ComponentInfo;
import org.apache.ambari.server.state.DependencyInfo;
import org.apache.ambari.server.state.PropertyInfo;
import org.apache.ambari.server.topology.Cardinality;
import org.apache.ambari.server.topology.Configuration;

/**
 * Encapsulates extension information.
 */
public class Extension {
  /**
   * Extension name
   */
  private String name;

  /**
   * Extension version
   */
  private String version;

  /**
   * Map of service name to components
   */
  private Map<String, Collection<String>> serviceComponents =
      new HashMap<String, Collection<String>>();

  /**
   * Map of component to service
   */
  private Map<String, String> componentService = new HashMap<String, String>();

  /**
   * Map of component to dependencies
   */
  private Map<String, Collection<DependencyInfo>> dependencies =
      new HashMap<String, Collection<DependencyInfo>>();

  /**
   * Map of dependency to conditional service
   */
  private Map<DependencyInfo, String> dependencyConditionalServiceMap =
      new HashMap<DependencyInfo, String>();

  /**
   * Map of database component name to configuration property which indicates whether
   * the database in to be managed or if it is an external non-managed instance.
   * If the value of the config property starts with 'New', the database is determined
   * to be managed, otherwise it is non-managed.
   */
  private Map<String, String> dbDependencyInfo = new HashMap<String, String>();

  /**
   * Map of component to required cardinality
   */
  private Map<String, String> cardinalityRequirements = new HashMap<String, String>();

  //todo: instead of all these maps from component -> * ,
  //todo: we should use a Component object with all of these attributes
  private Set<String> masterComponents = new HashSet<String>();

  /**
   * Map of component to auto-deploy information
   */
  private Map<String, AutoDeployInfo> componentAutoDeployInfo =
      new HashMap<String, AutoDeployInfo>();

  /**
   * Map of service to config type properties
   */
  /*private Map<String, Map<String, Map<String, ConfigProperty>>> serviceConfigurations =
      new HashMap<String, Map<String, Map<String, ConfigProperty>>>();*/

  /**
   * Map of service to required type properties
   */
  /*private Map<String, Map<String, Map<String, ConfigProperty>>> requiredServiceConfigurations =
      new HashMap<String, Map<String, Map<String, ConfigProperty>>>();*/

  /**
   * Map of service to config type properties
   */
  /*private Map<String, Map<String, ConfigProperty>> extensionConfigurations =
      new HashMap<String, Map<String, ConfigProperty>>();*/

  /**
   * Map of service to set of excluded config types
   */
  /*private Map<String, Set<String>> excludedConfigurationTypes =
    new HashMap<String, Set<String>>();*/

  /**
   * Ambari Management Controller, used to obtain Extension definitions
   */
  private final AmbariManagementController controller;


  /**
   * Constructor.
   *
   * @param extension
   *          the extension (not {@code null}).
   * @param ambariManagementController
   *          the management controller (not {@code null}).
   * @throws AmbariException
   */
  public Extension(ExtensionEntity extension, AmbariManagementController ambariManagementController) throws AmbariException {
    this(extension.getExtensionName(), extension.getExtensionVersion(), ambariManagementController);
  }

  /**
   * Constructor.
   *
   * @param name     extension name
   * @param version  extension version
   *
   * @throws AmbariException an exception occurred getting extension information
   *                         for the specified name and version
   */
  //todo: don't pass management controller in constructor
  public Extension(String name, String version, AmbariManagementController controller) throws AmbariException {
    this.name = name;
    this.version = version;
    this.controller = controller;

    /*Set<ExtensionServiceResponse> extensionServices = controller.getExtensionServices(
        Collections.singleton(new ExtensionServiceRequest(name, version, null)));

    for (ExtensionServiceResponse extensionService : extensionServices) {
      String serviceName = extensionService.getServiceName();
      parseComponents(serviceName);
      parseExcludedConfigurations(extensionService);
      parseConfigurations(serviceName);
      registerConditionalDependencies();
    }*/

    //todo: already done for each service
    //parseExtensionConfigurations();
  }

  /**
   * Obtain extension name.
   *
   * @return extension name
   */
  public String getName() {
    return name;
  }

  /**
   * Obtain extension version.
   *
   * @return extension version
   */
  public String getVersion() {
    return version;
  }


  Map<DependencyInfo, String> getDependencyConditionalServiceMap() {
    return dependencyConditionalServiceMap;
  }

  /**
   * Get services contained in the extension.
   *
   * @return collection of all services for the extension
   */
  public Collection<String> getServices() {
    return serviceComponents.keySet();
  }

  /**
   * Get components contained in the extension for the specified service.
   *
   * @param service  service name
   *
   * @return collection of component names for the specified service
   */
  public Collection<String> getComponents(String service) {
    return serviceComponents.get(service);
  }

  /**
   * Get all service components
   *
   * @return map of service to associated components
   */
  public Map<String, Collection<String>> getComponents() {
    Map<String, Collection<String>> serviceComponents = new HashMap<String, Collection<String>>();
    for (String service : getServices()) {
      Collection<String> components = new HashSet<String>();
      components.addAll(getComponents(service));
      serviceComponents.put(service, components);
    }
    return serviceComponents;
  }

  /**
   * Get info for the specified component.
   *
   * @param component  component name
   *
   * @return component information for the requested component
   *         or null if the component doesn't exist in the extension
   */
  public ComponentInfo getComponentInfo(String component) {
    ComponentInfo componentInfo = null;
    String service = getServiceForComponent(component);
    if (service != null) {
      try {
        componentInfo = controller.getAmbariMetaInfo().getComponent(
            getName(), getVersion(), service, component);
      } catch (AmbariException e) {
        // just return null if component doesn't exist
      }
    }
    return componentInfo;
  }

  /**
   * Get all configuration types, including excluded types for the specified service.
   *
   * @param service  service name
   *
   * @return collection of all configuration types for the specified service
   */
  /*public Collection<String> getAllConfigurationTypes(String service) {
    return serviceConfigurations.get(service).keySet();
  }*/

  /**
   * Get configuration types for the specified service.
   * This doesn't include any service excluded types.
   *
   * @param service  service name
   *
   * @return collection of all configuration types for the specified service
   */
  /*public Collection<String> getConfigurationTypes(String service) {
    Set<String> serviceTypes = new HashSet<String>(serviceConfigurations.get(service).keySet());
    serviceTypes.removeAll(getExcludedConfigurationTypes(service));

    return serviceTypes;
  }*/

  /**
   * Get the set of excluded configuration types for this service.
   *
   * @param service service name
   *
   * @return Set of names of excluded config types. Will not return null.
   */
  /*public Set<String> getExcludedConfigurationTypes(String service) {
    return excludedConfigurationTypes.containsKey(service) ?
        excludedConfigurationTypes.get(service) :
        Collections.<String>emptySet();
  }*/

  /**
   * Get config properties for the specified service and configuration type.
   *
   * @param service  service name
   * @param type     configuration type
   *
   * @return map of property names to values for the specified service and configuration type
   */
  /*public Map<String, String> getConfigurationProperties(String service, String type) {
    Map<String, String> configMap = new HashMap<String, String>();
    Map<String, ConfigProperty> configProperties = serviceConfigurations.get(service).get(type);
    if (configProperties != null) {
      for (Map.Entry<String, ConfigProperty> configProperty : configProperties.entrySet()) {
        configMap.put(configProperty.getKey(), configProperty.getValue().getValue());
      }
    }
    return configMap;
  }*/

  /**
   * Get all required config properties for the specified service.
   *
   * @param service  service name
   *
   * @return collection of all required properties for the given service
   */
  /*public Collection<ConfigProperty> getRequiredConfigurationProperties(String service) {
    Collection<ConfigProperty> requiredConfigProperties = new HashSet<ConfigProperty>();
    Map<String, Map<String, ConfigProperty>> serviceProperties = requiredServiceConfigurations.get(service);
    if (serviceProperties != null) {
      for (Map.Entry<String, Map<String, ConfigProperty>> typePropertiesEntry : serviceProperties.entrySet()) {
        requiredConfigProperties.addAll(typePropertiesEntry.getValue().values());
      }
    }
    return requiredConfigProperties;
  }*/

  /**
   * Get required config properties for the specified service and configuration type.
   *
   * @param service  service name
   * @param type     configuration type
   *
   * @return collection of required properties for the given service and type
   */
  //todo: change type to PropertyInfo.PropertyType
  /*public Collection<ConfigProperty> getRequiredConfigurationProperties(String service, String type) {
    Collection<ConfigProperty> requiredConfigs = new HashSet<ConfigProperty>();
    Map<String, ConfigProperty> configProperties = requiredServiceConfigurations.get(service).get(type);
    if (configProperties != null) {
      requiredConfigs.addAll(configProperties.values());
    }
    return requiredConfigs;
  }

  public boolean isPasswordProperty(String service, String type, String propertyName) {
    return (serviceConfigurations.containsKey(service) &&
            serviceConfigurations.get(service).containsKey(type) &&
            serviceConfigurations.get(service).get(type).containsKey(propertyName) &&
            serviceConfigurations.get(service).get(type).get(propertyName).getPropertyTypes().
                contains(PropertyInfo.PropertyType.PASSWORD));
  }

  //todo
  public Map<String, String> getExtensionConfigurationProperties(String type) {
    Map<String, String> configMap = new HashMap<String, String>();
    Map<String, ConfigProperty> configProperties = extensionConfigurations.get(type);
    if (configProperties != null) {
      for (Map.Entry<String, ConfigProperty> configProperty : configProperties.entrySet()) {
        configMap.put(configProperty.getKey(), configProperty.getValue().getValue());
      }
    }
    return configMap;
  }*/

  /**
   * Get config attributes for the specified service and configuration type.
   *
   * @param service  service name
   * @param type     configuration type
   *
   * @return  map of attribute names to map of property names to attribute values
   *          for the specified service and configuration type
   */
  /*public Map<String, Map<String, String>> getConfigurationAttributes(String service, String type) {
    Map<String, Map<String, String>> attributesMap = new HashMap<String, Map<String, String>>();
    Map<String, ConfigProperty> configProperties = serviceConfigurations.get(service).get(type);
    if (configProperties != null) {
      for (Map.Entry<String, ConfigProperty> configProperty : configProperties.entrySet()) {
        String propertyName = configProperty.getKey();
        Map<String, String> propertyAttributes = configProperty.getValue().getAttributes();
        if (propertyAttributes != null) {
          for (Map.Entry<String, String> propertyAttribute : propertyAttributes.entrySet()) {
            String attributeName = propertyAttribute.getKey();
            String attributeValue = propertyAttribute.getValue();
            if (attributeValue != null) {
              Map<String, String> attributes = attributesMap.get(attributeName);
              if (attributes == null) {
                  attributes = new HashMap<String, String>();
                  attributesMap.put(attributeName, attributes);
              }
              attributes.put(propertyName, attributeValue);
            }
          }
        }
      }
    }
    return attributesMap;
  }

  //todo:
  public Map<String, Map<String, String>> getExtensionConfigurationAttributes(String type) {
    Map<String, Map<String, String>> attributesMap = new HashMap<String, Map<String, String>>();
    Map<String, ConfigProperty> configProperties = extensionConfigurations.get(type);
    if (configProperties != null) {
      for (Map.Entry<String, ConfigProperty> configProperty : configProperties.entrySet()) {
        String propertyName = configProperty.getKey();
        Map<String, String> propertyAttributes = configProperty.getValue().getAttributes();
        if (propertyAttributes != null) {
          for (Map.Entry<String, String> propertyAttribute : propertyAttributes.entrySet()) {
            String attributeName = propertyAttribute.getKey();
            String attributeValue = propertyAttribute.getValue();
            Map<String, String> attributes = attributesMap.get(attributeName);
            if (attributes == null) {
              attributes = new HashMap<String, String>();
              attributesMap.put(attributeName, attributes);
            }
            attributes.put(propertyName, attributeValue);
          }
        }
      }
    }
    return attributesMap;
  }*/

  /**
   * Get the service for the specified component.
   *
   * @param component  component name
   *
   * @return service name that contains tha specified component
   */
  public String getServiceForComponent(String component) {
    return componentService.get(component);
  }

  /**
   * Get the names of the services which contains the specified components.
   *
   * @param components collection of components
   *
   * @return collection of services which contain the specified components
   */
  public Collection<String> getServicesForComponents(Collection<String> components) {
    Set<String> services = new HashSet<String>();
    for (String component : components) {
      services.add(getServiceForComponent(component));
    }

    return services;
  }

  /**
   * Obtain the service name which corresponds to the specified configuration.
   *
   * @param config  configuration type
   *
   * @return name of service which corresponds to the specified configuration type
   */
  /*public String getServiceForConfigType(String config) {
    for (Map.Entry<String, Map<String, Map<String, ConfigProperty>>> entry : serviceConfigurations.entrySet()) {
      Map<String, Map<String, ConfigProperty>> typeMap = entry.getValue();
      if (typeMap.containsKey(config)) {
        return entry.getKey();
      }
    }
    throw new IllegalArgumentException(
        "Specified configuration type is not associated with any service: " + config);
  }*/

  /**
   * Return the dependencies specified for the given component.
   *
   * @param component  component to get dependency information for
   *
   * @return collection of dependency information for the specified component
   */
  //todo: full dependency graph
  public Collection<DependencyInfo> getDependenciesForComponent(String component) {
    return dependencies.containsKey(component) ? dependencies.get(component) :
        Collections.<DependencyInfo>emptySet();
  }

  /**
   * Get the service, if any, that a component dependency is conditional on.
   *
   * @param dependency  dependency to get conditional service for
   *
   * @return conditional service for provided component or null if dependency
   *         is not conditional on a service
   */
  public String getConditionalServiceForDependency(DependencyInfo dependency) {
    return dependencyConditionalServiceMap.get(dependency);
  }

  public String getExternalComponentConfig(String component) {
    return dbDependencyInfo.get(component);
  }

  /**
   * Obtain the required cardinality for the specified component.
   */
  public Cardinality getCardinality(String component) {
    return new Cardinality(cardinalityRequirements.get(component));
  }

  /**
   * Obtain auto-deploy information for the specified component.
   */
  public AutoDeployInfo getAutoDeployInfo(String component) {
    return componentAutoDeployInfo.get(component);
  }

  public boolean isMasterComponent(String component) {
    return masterComponents.contains(component);
  }

  /*public Configuration getConfiguration(Collection<String> services) {
    Map<String, Map<String, Map<String, String>>> attributes = new HashMap<String, Map<String, Map<String, String>>>();
    Map<String, Map<String, String>> properties = new HashMap<String, Map<String, String>>();

    for (String service : services) {
      Collection<String> serviceConfigTypes = getConfigurationTypes(service);
      for (String type : serviceConfigTypes) {
        Map<String, String> typeProps = properties.get(type);
        if (typeProps == null) {
          typeProps = new HashMap<String, String>();
          properties.put(type, typeProps);
        }
        typeProps.putAll(getConfigurationProperties(service, type));

        Map<String, Map<String, String>> extensionTypeAttributes = getConfigurationAttributes(service, type);
        if (!extensionTypeAttributes.isEmpty()) {
          if (! attributes.containsKey(type)) {
            attributes.put(type, new HashMap<String, Map<String, String>>());
          }
          Map<String, Map<String, String>> typeAttributes = attributes.get(type);
          for (Map.Entry<String, Map<String, String>> attribute : extensionTypeAttributes.entrySet()) {
            String attributeName = attribute.getKey();
            Map<String, String> attributeProps = typeAttributes.get(attributeName);
            if (attributeProps == null) {
              attributeProps = new HashMap<String, String>();
              typeAttributes.put(attributeName, attributeProps);
            }
            attributeProps.putAll(attribute.getValue());
          }
        }
      }
    }
    return new Configuration(properties, attributes);
  }*/

  /*public Configuration getConfiguration() {
    Map<String, Map<String, Map<String, String>>> extensionAttributes = new HashMap<String, Map<String, Map<String, String>>>();
    Map<String, Map<String, String>> extensionConfigs = new HashMap<String, Map<String, String>>();

    for (String service : getServices()) {
      for (String type : getAllConfigurationTypes(service)) {
        Map<String, String> typeProps = extensionConfigs.get(type);
        if (typeProps == null) {
          typeProps = new HashMap<String, String>();
          extensionConfigs.put(type, typeProps);
        }
        typeProps.putAll(getConfigurationProperties(service, type));

        Map<String, Map<String, String>> extensionTypeAttributes = getConfigurationAttributes(service, type);
        if (!extensionTypeAttributes.isEmpty()) {
          if (! extensionAttributes.containsKey(type)) {
            extensionAttributes.put(type, new HashMap<String, Map<String, String>>());
          }
          Map<String, Map<String, String>> typeAttrs = extensionAttributes.get(type);
          for (Map.Entry<String, Map<String, String>> attribute : extensionTypeAttributes.entrySet()) {
            String attributeName = attribute.getKey();
            Map<String, String> attributes = typeAttrs.get(attributeName);
            if (attributes == null) {
              attributes = new HashMap<String, String>();
              typeAttrs.put(attributeName, attributes);
            }
            attributes.putAll(attribute.getValue());
          }
        }
      }
    }
    return new Configuration(extensionConfigs, extensionAttributes);
  }*/

  /**
   * Parse components for the specified service from the extension definition.
   *
   * @param service  service name
   *
   * @throws AmbariException an exception occurred getting components from the extension definition
   */
  private void parseComponents(String service) throws AmbariException{
    Collection<String> componentSet = new HashSet<String>();

    /*Set<ExtensionServiceComponentResponse> components = controller.getExtensionComponents(
        Collections.singleton(new ExtensionServiceComponentRequest(name, version, service, null)));

    // extension service components
    for (ExtensionServiceComponentResponse component : components) {
      String componentName = component.getComponentName();
      componentSet.add(componentName);
      componentService.put(componentName, service);
      String cardinality = component.getCardinality();
      if (cardinality != null) {
        cardinalityRequirements.put(componentName, cardinality);
      }
      AutoDeployInfo autoDeploy = component.getAutoDeploy();
      if (autoDeploy != null) {
        componentAutoDeployInfo.put(componentName, autoDeploy);
      }

      // populate component dependencies
      //todo: remove usage of AmbariMetaInfo
      Collection<DependencyInfo> componentDependencies = controller.getAmbariMetaInfo().getComponentDependencies(
          name, version, service, componentName);

      if (componentDependencies != null && ! componentDependencies.isEmpty()) {
        dependencies.put(componentName, componentDependencies);
      }
      if (component.isMaster()) {
        masterComponents.add(componentName);
      }
    }*/
    serviceComponents.put(service, componentSet);
  }

  /**
   * Parse configurations for the specified service from the extension definition.
   *
   * @param service  service name
   *
   * @throws AmbariException an exception occurred getting configurations from the extension definition
   */
  private void parseConfigurations(String service) throws AmbariException {
    /*Map<String, Map<String, ConfigProperty>> mapServiceConfig = new HashMap<String, Map<String, ConfigProperty>>();
    Map<String, Map<String, ConfigProperty>> mapRequiredServiceConfig = new HashMap<String, Map<String, ConfigProperty>>();

    serviceConfigurations.put(service, mapServiceConfig);
    requiredServiceConfigurations.put(service, mapRequiredServiceConfig);

    Set<ExtensionConfigurationResponse> serviceConfigs = controller.getExtensionConfigurations(
        Collections.singleton(new ExtensionConfigurationRequest(name, version, service, null)));
    Set<ExtensionConfigurationResponse> extensionLevelConfigs = controller.getExtensionLevelConfigurations(
        Collections.singleton(new ExtensionLevelConfigurationRequest(name, version, null)));
    serviceConfigs.addAll(extensionLevelConfigs);

    // shouldn't have any required properties in extension level configuration
    for (ExtensionConfigurationResponse config : serviceConfigs) {
      ConfigProperty configProperty = new ConfigProperty(config);
      String type = configProperty.getType();

      Map<String, ConfigProperty> mapTypeConfig = mapServiceConfig.get(type);
      if (mapTypeConfig == null) {
        mapTypeConfig = new HashMap<String, ConfigProperty>();
        mapServiceConfig.put(type, mapTypeConfig);
      }

      mapTypeConfig.put(config.getPropertyName(), configProperty);
      if (config.isRequired()) {
        Map<String, ConfigProperty> requiredTypeConfig = mapRequiredServiceConfig.get(type);
        if (requiredTypeConfig == null) {
          requiredTypeConfig = new HashMap<String, ConfigProperty>();
          mapRequiredServiceConfig.put(type, requiredTypeConfig);
        }
        requiredTypeConfig.put(config.getPropertyName(), configProperty);
      }
    }*/
  }

  private void parseExtensionConfigurations () throws AmbariException {

    /*Set<ExtensionConfigurationResponse> extensionLevelConfigs = controller.getExtensionLevelConfigurations(
        Collections.singleton(new ExtensionLevelConfigurationRequest(name, version, null)));

    for (ExtensionConfigurationResponse config : extensionLevelConfigs) {
      ConfigProperty configProperty = new ConfigProperty(config);
      String type = configProperty.getType();

      Map<String, ConfigProperty> mapTypeConfig = extensionConfigurations.get(type);
      if (mapTypeConfig == null) {
        mapTypeConfig = new HashMap<String, ConfigProperty>();
        extensionConfigurations.put(type, mapTypeConfig);
      }

      mapTypeConfig.put(config.getPropertyName(),
          configProperty);
    }*/
  }

  /**
   * Obtain the excluded configuration types from the ExtensionServiceResponse
   *
   * @param extensionServiceResponse the response object associated with this extension service
   */
  /*private void parseExcludedConfigurations(ExtensionServiceResponse extensionServiceResponse) {
    excludedConfigurationTypes.put(extensionServiceResponse.getServiceName(), extensionServiceResponse.getExcludedConfigTypes());
  }*/

  /**
   * Register conditional dependencies.
   */
  void registerConditionalDependencies() {
  }

  /**
   * Contains a configuration property's value and attributes.
   */
  public static class ConfigProperty {
    private String name;
    private String value;
    private Map<String, String> attributes;
    private Set<PropertyInfo.PropertyType> propertyTypes;
    private String type;

    /*private ConfigProperty(ExtensionConfigurationResponse config) {
      this.name = config.getPropertyName();
      this.value = config.getPropertyValue();
      this.attributes = config.getPropertyAttributes();
      this.propertyTypes = config.getPropertyType();
      this.type = normalizeType(config.getType());
    }*/

    public ConfigProperty(String type, String name, String value) {
      this.type = type;
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return name;
    }

    public String getValue() {
      return value;
    }

    public void setValue(String value) {
      this.value = value;
    }

    public String getType() {
      return type;
    }

    public Set<PropertyInfo.PropertyType> getPropertyTypes() {
      return propertyTypes;
    }

    public void setPropertyTypes(Set<PropertyInfo.PropertyType> propertyTypes) {
      this.propertyTypes = propertyTypes;
    }

    public Map<String, String> getAttributes() {
      return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
      this.attributes = attributes;
    }

    private String normalizeType(String type) {
      //strip .xml from type
      if (type.endsWith(".xml")) {
        type = type.substring(0, type.length() - 4);
      }
      return type;
    }
  }
}
