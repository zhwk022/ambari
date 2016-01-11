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

package org.apache.ambari.server.api.services;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.ambari.server.api.resources.ResourceInstance;
import org.apache.ambari.server.controller.spi.Resource;

/**
 * Service for extensions management.
 */
@Path("/extensions/")
public class ExtensionsService extends BaseService {

  @GET
  @Produces("text/plain")
  public Response getExtensions(String body, @Context HttpHeaders headers, @Context UriInfo ui) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionResource(null));
  }

  @GET
  @Path("{extensionName}")
  @Produces("text/plain")
  public Response getExtension(String body, @Context HttpHeaders headers,
                           @Context UriInfo ui,
                           @PathParam("extensionName") String extensionName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionResource(extensionName));
  }

  @GET
  @Path("{extensionName}/versions")
  @Produces("text/plain")
  public Response getExtensionVersions(String body,
                                   @Context HttpHeaders headers,
                                   @Context UriInfo ui, @PathParam("extensionName") String extensionName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionVersionResource(extensionName, null));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}")
  @Produces("text/plain")
  public Response getExtensionVersion(String body,
                                  @Context HttpHeaders headers,
                                  @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                  @PathParam("extensionVersion") String extensionVersion) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionVersionResource(extensionName, extensionVersion));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/links")
  @Produces("text/plain")
  public Response getExtensionVersionLinks(String body,
                                  @Context HttpHeaders headers,
                                  @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                  @PathParam("extensionVersion") String extensionVersion) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionLinkResource(null, null, extensionName, extensionVersion));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/configurations")
  @Produces("text/plain")
  public Response getExtensionLevelConfigurations(String body, @Context HttpHeaders headers,
                                   @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                   @PathParam("extensionVersion") String extensionVersion) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionLevelConfigurationsResource(extensionName, extensionVersion, null));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/configurations/{propertyName}")
  @Produces("text/plain")
  public Response getExtensionLevelConfiguration(String body, @Context HttpHeaders headers,
                                        @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                        @PathParam("extensionVersion") String extensionVersion,
                                        @PathParam("serviceName") String serviceName,
                                        @PathParam("propertyName") String propertyName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionLevelConfigurationsResource(extensionName, extensionVersion, propertyName));
  }


  @GET
  @Path("{extensionName}/versions/{extensionVersion}/services")
  @Produces("text/plain")
  public Response getExtensionServices(String body, @Context HttpHeaders headers,
                                   @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                   @PathParam("extensionVersion") String extensionVersion) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionServiceResource(extensionName, extensionVersion, null));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/services/{serviceName}")
  @Produces("text/plain")
  public Response getExtensionService(String body, @Context HttpHeaders headers,
                                  @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                  @PathParam("extensionVersion") String extensionVersion,
                                  @PathParam("serviceName") String serviceName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionServiceResource(extensionName, extensionVersion, serviceName));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/artifacts")
  @Produces("text/plain")
  public Response getExtensionArtifacts(String body, @Context HttpHeaders headers,
                                              @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                              @PathParam("extensionVersion") String extensionVersion) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionArtifactsResource(extensionName, extensionVersion, null));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/artifacts/{artifactName}")
  @Produces("text/plain")
  public Response getExtensionArtifact(String body, @Context HttpHeaders headers,
                                   @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                   @PathParam("extensionVersion") String extensionVersion,
                                   @PathParam("artifactName") String artifactName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionArtifactsResource(extensionName, extensionVersion, artifactName));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/services/{serviceName}/artifacts")
  @Produces("text/plain")
  public Response getExtensionServiceArtifacts(String body, @Context HttpHeaders headers,
                                  @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                  @PathParam("extensionVersion") String extensionVersion,
                                  @PathParam("serviceName") String serviceName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionServiceArtifactsResource(extensionName, extensionVersion, serviceName, null));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/services/{serviceName}/themes")
  @Produces("text/plain")
  public Response getExtensionServiceThemes(String body, @Context HttpHeaders headers,
                                           @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                           @PathParam("extensionVersion") String extensionVersion,
                                           @PathParam("serviceName") String serviceName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
      createExtensionServiceThemesResource(extensionName, extensionVersion, serviceName, null));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/services/{serviceName}/themes/{themeName}")
  @Produces("text/plain")
  public Response getExtensionServiceTheme(String body, @Context HttpHeaders headers,
                                           @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                           @PathParam("extensionVersion") String extensionVersion,
                                           @PathParam("serviceName") String serviceName,
                                           @PathParam("themeName") String themeName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
      createExtensionServiceThemesResource(extensionName, extensionVersion, serviceName, themeName));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/services/{serviceName}/artifacts/{artifactName}")
  @Produces("text/plain")
  public Response getExtensionServiceArtifact(String body, @Context HttpHeaders headers,
                                           @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                           @PathParam("extensionVersion") String extensionVersion,
                                           @PathParam("serviceName") String serviceName,
                                           @PathParam("artifactName") String artifactName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionServiceArtifactsResource(extensionName, extensionVersion, serviceName, artifactName));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/services/{serviceName}/configurations")
  @Produces("text/plain")
  public Response getExtensionConfigurations(String body,
                                         @Context HttpHeaders headers,
                                         @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                         @PathParam("extensionVersion") String extensionVersion,
                                         @PathParam("serviceName") String serviceName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionConfigurationResource(extensionName, extensionVersion, serviceName, null));
  }


  @GET
  @Path("{extensionName}/versions/{extensionVersion}/services/{serviceName}/configurations/{propertyName}")
  @Produces("text/plain")
  public Response getExtensionConfiguration(String body, @Context HttpHeaders headers,
                                        @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                        @PathParam("extensionVersion") String extensionVersion,
                                        @PathParam("serviceName") String serviceName,
                                        @PathParam("propertyName") String propertyName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionConfigurationResource(extensionName, extensionVersion, serviceName, propertyName));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/services/{serviceName}/configurations/{propertyName}/dependencies")
  @Produces("text/plain")
  public Response getExtensionConfigurationDependencies(String body, @Context HttpHeaders headers,
                                        @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                        @PathParam("extensionVersion") String extensionVersion,
                                        @PathParam("serviceName") String serviceName,
                                        @PathParam("propertyName") String propertyName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionConfigurationDependencyResource(extensionName, extensionVersion, serviceName, propertyName));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/services/{serviceName}/components")
  @Produces("text/plain")
  public Response getServiceComponents(String body,
                                       @Context HttpHeaders headers,
                                       @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                       @PathParam("extensionVersion") String extensionVersion,
                                       @PathParam("serviceName") String serviceName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionServiceComponentResource(extensionName, extensionVersion, serviceName, null));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/services/{serviceName}/components/{componentName}/dependencies")
  @Produces("text/plain")
  public Response getServiceComponentDependencies(String body, @Context HttpHeaders headers,
                                                  @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                                  @PathParam("extensionVersion") String extensionVersion,
                                                  @PathParam("serviceName") String serviceName,
                                                  @PathParam("componentName") String componentName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionServiceComponentDependencyResource(extensionName, extensionVersion, serviceName, componentName, null));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/services/{serviceName}/components/{componentName}/dependencies/{dependencyName}")
  @Produces("text/plain")
  public Response getServiceComponentDependency(String body, @Context HttpHeaders headers,
                                      @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                      @PathParam("extensionVersion") String extensionVersion,
                                      @PathParam("serviceName") String serviceName,
                                      @PathParam("componentName") String componentName,
                                      @PathParam("dependencyName") String dependencyName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionServiceComponentDependencyResource(extensionName, extensionVersion, serviceName, componentName, dependencyName));
  }

  @GET
  @Path("{extensionName}/versions/{extensionVersion}/services/{serviceName}/components/{componentName}")
  @Produces("text/plain")
  public Response getServiceComponent(String body, @Context HttpHeaders headers,
                                      @Context UriInfo ui, @PathParam("extensionName") String extensionName,
                                      @PathParam("extensionVersion") String extensionVersion,
                                      @PathParam("serviceName") String serviceName,
                                      @PathParam("componentName") String componentName) {

    return handleRequest(headers, body, ui, Request.Type.GET,
        createExtensionServiceComponentResource(extensionName, extensionVersion, serviceName, componentName));
  }

  /**
   * Handles ANY /{extensionName}/versions/{extensionVersion}/operating_systems.
   *
   * @param extensionName extension name
   * @param extensionVersion extension version
   * @return operating system service
   */
  @Path("{extensionName}/versions/{extensionVersion}/operating_systems")
  public ExtensionOperatingSystemService getOperatingSystemsHandler(@PathParam("extensionName") String extensionName, @PathParam("extensionVersion") String extensionVersion) {
    final Map<Resource.Type, String> extensionProperties = new HashMap<Resource.Type, String>();
    extensionProperties.put(Resource.Type.Extension, extensionName);
    extensionProperties.put(Resource.Type.ExtensionVersion, extensionVersion);
    return new ExtensionOperatingSystemService(extensionProperties);
  }

  /**
   * Handles ANY /{extensionName}/versions/{extensionVersion}/repository_versions.
   *
   * @param extensionName extension name
   * @param extensionVersion extension version
   * @return repository version service
   */
  @Path("{extensionName}/versions/{extensionVersion}/repository_versions")
  public ExtensionRepositoryVersionService getRepositoryVersionHandler(@PathParam("extensionName") String extensionName, @PathParam("extensionVersion") String extensionVersion) {
    final Map<Resource.Type, String> extensionProperties = new HashMap<Resource.Type, String>();
    extensionProperties.put(Resource.Type.Extension, extensionName);
    extensionProperties.put(Resource.Type.ExtensionVersion, extensionVersion);
    return new ExtensionRepositoryVersionService(extensionProperties);
  }

  /**
   * Handles ANY /{extensionName}/versions/{extensionVersion}/compatible_repository_versions.
   *
   * @param extensionName extension name
   * @param extensionVersion extension version
   * @return repository version service
   */
  @Path("{extensionName}/versions/{extensionVersion}/compatible_repository_versions")
  public CompatibleExtensionRepositoryVersionService getCompatibleRepositoryVersionHandler(
      @PathParam("extensionName") String extensionName,
      @PathParam("extensionVersion") String extensionVersion) {
    final Map<Resource.Type, String> extensionProperties = new HashMap<Resource.Type, String>();
    extensionProperties.put(Resource.Type.Extension, extensionName);
    extensionProperties.put(Resource.Type.ExtensionVersion, extensionVersion);
    return new CompatibleExtensionRepositoryVersionService(extensionProperties);
  }

  ResourceInstance createExtensionServiceComponentResource(
      String extensionName, String extensionVersion, String serviceName, String componentName) {

    Map<Resource.Type, String> mapIds = new HashMap<Resource.Type, String>();
    mapIds.put(Resource.Type.Extension, extensionName);
    mapIds.put(Resource.Type.ExtensionVersion, extensionVersion);
    mapIds.put(Resource.Type.ExtensionService, serviceName);
    mapIds.put(Resource.Type.ExtensionServiceComponent, componentName);

    return createResource(Resource.Type.ExtensionServiceComponent, mapIds);
  }

  ResourceInstance createExtensionServiceComponentDependencyResource(
      String extensionName, String extensionVersion, String serviceName, String componentName, String dependencyName) {

    Map<Resource.Type, String> mapIds = new HashMap<Resource.Type, String>();
    mapIds.put(Resource.Type.Extension, extensionName);
    mapIds.put(Resource.Type.ExtensionVersion, extensionVersion);
    mapIds.put(Resource.Type.ExtensionService, serviceName);
    mapIds.put(Resource.Type.ExtensionServiceComponent, componentName);
    mapIds.put(Resource.Type.ExtensionServiceComponentDependency, dependencyName);

    return createResource(Resource.Type.ExtensionServiceComponentDependency, mapIds);
  }

  ResourceInstance createExtensionConfigurationResource(String extensionName,
                                                    String extensionVersion, String serviceName, String propertyName) {

    Map<Resource.Type, String> mapIds = new HashMap<Resource.Type, String>();
    mapIds.put(Resource.Type.Extension, extensionName);
    mapIds.put(Resource.Type.ExtensionVersion, extensionVersion);
    mapIds.put(Resource.Type.ExtensionService, serviceName);
    mapIds.put(Resource.Type.ExtensionConfiguration, propertyName);

    return createResource(Resource.Type.ExtensionConfiguration, mapIds);
  }

  ResourceInstance createExtensionConfigurationDependencyResource(String extensionName,
                                                              String extensionVersion, String serviceName, String propertyName) {

    Map<Resource.Type, String> mapIds = new HashMap<Resource.Type, String>();
    mapIds.put(Resource.Type.Extension, extensionName);
    mapIds.put(Resource.Type.ExtensionVersion, extensionVersion);
    mapIds.put(Resource.Type.ExtensionService, serviceName);
    mapIds.put(Resource.Type.ExtensionConfiguration, propertyName);

    return createResource(Resource.Type.ExtensionConfigurationDependency, mapIds);
  }

  ResourceInstance createExtensionServiceResource(String extensionName,
                                              String extensionVersion, String serviceName) {
    Map<Resource.Type, String> mapIds = new HashMap<Resource.Type, String>();
    mapIds.put(Resource.Type.Extension, extensionName);
    mapIds.put(Resource.Type.ExtensionVersion, extensionVersion);
    mapIds.put(Resource.Type.ExtensionService, serviceName);

    return createResource(Resource.Type.ExtensionService, mapIds);
  }

  ResourceInstance createExtensionVersionResource(String extensionName,
                                              String extensionVersion) {
    Map<Resource.Type, String> mapIds = new HashMap<Resource.Type, String>();
    mapIds.put(Resource.Type.Extension, extensionName);
    mapIds.put(Resource.Type.ExtensionVersion, extensionVersion);

    return createResource(Resource.Type.ExtensionVersion, mapIds);
  }

  ResourceInstance createExtensionLevelConfigurationsResource(String extensionName,
      String extensionVersion, String propertyName) {
    Map<Resource.Type, String> mapIds = new HashMap<Resource.Type, String>();
    mapIds.put(Resource.Type.Extension, extensionName);
    mapIds.put(Resource.Type.ExtensionVersion, extensionVersion);
    mapIds.put(Resource.Type.ExtensionLevelConfiguration, propertyName);

    return createResource(Resource.Type.ExtensionLevelConfiguration, mapIds);
  }

  ResourceInstance createExtensionArtifactsResource(String extensionName, String extensionVersion, String artifactName) {
    Map<Resource.Type, String> mapIds = new HashMap<Resource.Type, String>();
    mapIds.put(Resource.Type.Extension, extensionName);
    mapIds.put(Resource.Type.ExtensionVersion, extensionVersion);
    mapIds.put(Resource.Type.ExtensionArtifact, artifactName);

    return createResource(Resource.Type.ExtensionArtifact, mapIds);
  }

  ResourceInstance createExtensionServiceArtifactsResource(String extensionName,
                                                       String extensionVersion,
                                                       String serviceName,
                                                       String artifactName) {

    Map<Resource.Type, String> mapIds = new HashMap<Resource.Type, String>();
    mapIds.put(Resource.Type.Extension, extensionName);
    mapIds.put(Resource.Type.ExtensionVersion, extensionVersion);
    mapIds.put(Resource.Type.ExtensionService, serviceName);
    mapIds.put(Resource.Type.ExtensionArtifact, artifactName);

    return createResource(Resource.Type.ExtensionArtifact, mapIds);
  }

  ResourceInstance createExtensionServiceThemesResource(String extensionName, String extensionVersion, String serviceName,
                                                    String themeName) {
    Map<Resource.Type, String> mapIds = new HashMap<Resource.Type, String>();
    mapIds.put(Resource.Type.Extension, extensionName);
    mapIds.put(Resource.Type.ExtensionVersion, extensionVersion);
    mapIds.put(Resource.Type.ExtensionService, serviceName);
    mapIds.put(Resource.Type.Theme, themeName);

    return createResource(Resource.Type.Theme, mapIds);
  }

  ResourceInstance createExtensionLinkResource(String stackName, String stackVersion,
                                  String extensionName, String extensionVersion) {
    Map<Resource.Type, String> mapIds = new HashMap<Resource.Type, String>();
    mapIds.put(Resource.Type.Stack, stackName);
    mapIds.put(Resource.Type.StackVersion, stackVersion);
    mapIds.put(Resource.Type.Extension, extensionName);
    mapIds.put(Resource.Type.ExtensionVersion, extensionVersion);

    return createResource(Resource.Type.ExtensionLink, mapIds);
  }

  ResourceInstance createExtensionResource(String extensionName) {

    return createResource(Resource.Type.Extension,
        Collections.singletonMap(Resource.Type.Extension, extensionName));

  }
}
