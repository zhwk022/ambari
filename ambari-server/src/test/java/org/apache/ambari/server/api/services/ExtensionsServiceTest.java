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

import org.apache.ambari.server.api.resources.ResourceInstance;
import org.apache.ambari.server.api.services.parsers.RequestBodyParser;
import org.apache.ambari.server.api.services.serializers.ResultSerializer;
import org.easymock.EasyMock;
import org.junit.Test;


import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;


import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.notNull;
import static org.easymock.EasyMock.same;
import static org.junit.Assert.assertEquals;

/**
* Unit tests for ExtensionsService.
*/
public class ExtensionsServiceTest extends BaseServiceTest {

  @Override
  public List<ServiceTestInvocation> getTestInvocations() throws Exception {
    List<ServiceTestInvocation> listInvocations = new ArrayList<ServiceTestInvocation>();

    // getExtension
    ExtensionsService service = new TestExtensionsService("extensionName", null);
    Method m = service.getClass().getMethod("getExtension", String.class, HttpHeaders.class, UriInfo.class, String.class);
    Object[] args = new Object[] {null, getHttpHeaders(), getUriInfo(), "extensionName"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    //getExtensions
    service = new TestExtensionsService(null, null);
    m = service.getClass().getMethod("getExtensions", String.class, HttpHeaders.class, UriInfo.class);
    args = new Object[] {null, getHttpHeaders(), getUriInfo()};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    // getExtensionVersion
    service = new TestExtensionsService("extensionName", "extensionVersion");
    m = service.getClass().getMethod("getExtensionVersion", String.class, HttpHeaders.class, UriInfo.class, String.class, String.class);
    args = new Object[] {null, getHttpHeaders(), getUriInfo(), "extensionName", "extensionVersion"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    // getExtensionVersions
    service = new TestExtensionsService("extensionName", null);
    m = service.getClass().getMethod("getExtensionVersions", String.class, HttpHeaders.class, UriInfo.class, String.class);
    args = new Object[] {null, getHttpHeaders(), getUriInfo(), "extensionName"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    // getExtensionService
    service = new TestExtensionsService("extensionName", null);
    m = service.getClass().getMethod("getExtensionService", String.class, HttpHeaders.class, UriInfo.class, String.class,
        String.class, String.class);
    args = new Object[] {null, getHttpHeaders(), getUriInfo(), "extensionName", "extensionVersion", "service-name"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    // getExtensionServices
    service = new TestExtensionsService("extensionName", null);
    m = service.getClass().getMethod("getExtensionServices", String.class, HttpHeaders.class, UriInfo.class, String.class, String.class);
    args = new Object[] {null, getHttpHeaders(), getUriInfo(), "extensionName", "extensionVersion"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    // getExtensionConfiguration
    service = new TestExtensionsService("extensionName", "extensionVersion");
    m = service.getClass().getMethod("getExtensionConfiguration", String.class, HttpHeaders.class, UriInfo.class,
        String.class, String.class, String.class, String.class);
    args = new Object[] {null, getHttpHeaders(), getUriInfo(), "extensionName", "extensionVersion", "service-name", "property-name"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    // getExtensionConfigurations
    service = new TestExtensionsService("extensionName", null);
    m = service.getClass().getMethod("getExtensionConfigurations", String.class, HttpHeaders.class, UriInfo.class,
        String.class, String.class, String.class);
    args = new Object[] {null, getHttpHeaders(), getUriInfo(), "extensionName", "extensionVersion", "service-name"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    // getServiceComponent
    service = new TestExtensionsService("extensionName", "extensionVersion");
    m = service.getClass().getMethod("getServiceComponent", String.class, HttpHeaders.class, UriInfo.class,
        String.class, String.class, String.class, String.class);
    args = new Object[] {null, getHttpHeaders(), getUriInfo(), "extensionName", "extensionVersion", "service-name", "component-name"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    // getServiceComponents
    service = new TestExtensionsService("extensionName", "extensionVersion");
    m = service.getClass().getMethod("getServiceComponents", String.class, HttpHeaders.class, UriInfo.class,
        String.class, String.class, String.class);
    args = new Object[] {null, getHttpHeaders(), getUriInfo(), "extensionName", "extensionVersion", "service-name"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    //get extension artifacts
    service = new TestExtensionsService("extensionName", null);
    m = service.getClass().getMethod("getExtensionArtifacts", String.class, HttpHeaders.class, UriInfo.class, String.class, String.class);
    args = new Object[] {null, getHttpHeaders(), getUriInfo(), "extensionName", "extensionVersion"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    //get extension artifact
    service = new TestExtensionsService("extensionName", null);
    m = service.getClass().getMethod("getExtensionArtifact", String.class, HttpHeaders.class, UriInfo.class, String.class,
        String.class, String.class);
    args = new Object[] {null, getHttpHeaders(), getUriInfo(), "extensionName", "extensionVersion", "artifact-name"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    //get extension service artifacts
    service = new TestExtensionsService("extensionName", "extensionVersion");
    m = service.getClass().getMethod("getExtensionServiceArtifacts", String.class, HttpHeaders.class, UriInfo.class,
        String.class, String.class, String.class);
    args = new Object[] {null, getHttpHeaders(), getUriInfo(), "extensionName", "extensionVersion", "service-name"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    //get extension service artifact
    service = new TestExtensionsService("extensionName", "extensionVersion");
    m = service.getClass().getMethod("getExtensionServiceArtifact", String.class, HttpHeaders.class, UriInfo.class,
        String.class, String.class, String.class, String.class);
    args = new Object[] {null, getHttpHeaders(), getUriInfo(), "extensionName", "extensionVersion", "service-name", "artifact-name"};
    listInvocations.add(new ServiceTestInvocation(Request.Type.GET, service, m, args, null));

    return listInvocations;
  }

  private class TestExtensionsService extends ExtensionsService {

    private String m_extensionId;
    private String m_extensionVersion;

    private TestExtensionsService(String extensionName, String extensionVersion) {
      m_extensionId = extensionName;
      m_extensionVersion = extensionVersion;
    }

    @Override
    ResourceInstance createExtensionResource(String extensionName) {
      assertEquals(m_extensionId, extensionName);
      return getTestResource();
    }

    @Override
    ResourceInstance createExtensionVersionResource(String extensionName, String extensionVersion) {
      assertEquals(m_extensionId, extensionName);
      assertEquals(m_extensionVersion, extensionVersion);
      return getTestResource();
    }

    @Override
    ResourceInstance createExtensionServiceResource(String extensionName,
        String extensionVersion, String serviceName) {
      return getTestResource();
    }

    @Override
    ResourceInstance createExtensionConfigurationResource(String extensionName,
        String extensionVersion, String serviceName, String propertyName) {
      return getTestResource();
    }

    @Override
    ResourceInstance createExtensionServiceComponentResource(String extensionName,
        String extensionVersion, String serviceName, String componentName) {
      return getTestResource();
    }

    @Override
    ResourceInstance createExtensionArtifactsResource(String extensionName, String extensionVersion, String artifactName) {
      return getTestResource();
    }

    @Override
    ResourceInstance createExtensionServiceArtifactsResource(String extensionName, String extensionVersion, String serviceName, String artifactName) {
      return getTestResource();
    }

    @Override
    RequestFactory getRequestFactory() {
      return getTestRequestFactory();
    }

    @Override
    protected RequestBodyParser getBodyParser() {
      return getTestBodyParser();
    }

    @Override
    protected ResultSerializer getResultSerializer() {
      return getTestResultSerializer();
    }
  }
}
