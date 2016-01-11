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
package org.apache.ambari.server.state.stack;

import java.io.File;
import java.util.Collection;

import org.apache.ambari.server.state.ExtensionInfo;
import org.apache.ambari.server.state.RepositoryInfo;

/**
 * Encapsulates the work to resolve the latest repo information for an extension.
 * This class must be used AFTER the extension has created its owned repositories.
 */
public class LatestExtensionRepoCallable extends AbstractLatestRepoCallable {

  private ExtensionInfo extension = null;

  public LatestExtensionRepoCallable(String latestSourceUri, File repoFolder, OsFamily os_family, ExtensionInfo extension) {
    super(latestSourceUri, repoFolder, os_family);
    this.extension = extension;
  }

  @Override
  public String getName() {
    return extension.getName();
  }

  @Override
  public String getVersion() {
    return extension.getVersion();
  }

  @Override
  public Collection<RepositoryInfo> getRepositories() {
    return extension.getRepositories();
  }

}
