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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.ambari.server.stack.StackManager;
import org.apache.ambari.server.stack.Validable;

/**
 * Represents the stack's <code>extensions.xml</code> file.
 */
@XmlRootElement(name="extensions")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExtensionsXml implements Validable{

  @XmlElements(@XmlElement(name="extension"))
  private List<Extension> extensions = new ArrayList<Extension>();

  @XmlTransient
  private boolean valid = true;

  /**
   *
   * @return valid xml flag
   */
  @Override
  public boolean isValid() {
    return valid;
  }

  public List<String> getExtensionKeys() {
    List<String> keys = new ArrayList<String>();
    for (Extension extension : extensions) {
      keys.add(extension.getName() + StackManager.PATH_DELIMITER + extension.getVersion());
    }
    return keys;
  }

  public List<Extension> getExtensions() {
    return extensions;
  }

  public void addExtension(String name, String version) {
    Extension extension = new Extension(name, version);
    extensions.add(extension);
  }

  /**
   *
   * @param valid set validity flag
   */
  @Override
  public void setValid(boolean valid) {
    this.valid = valid;
  }

  @XmlTransient
  private Set<String> errorSet = new HashSet<String>();

  @Override
  public void setErrors(String error) {
    errorSet.add(error);
  }

  @Override
  public Collection getErrors() {
    return errorSet;
  }

  @Override
  public void setErrors(Collection error) {
    this.errorSet.addAll(error);
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Version {
    private Version() {
    }
    private boolean active = false;
    private String upgrade = null;

    /**
     * @return <code>true</code> if the stack is active
     */
    public boolean isActive() {
      return active;
    }

    /**
     * @return the upgrade version number, if set
     */
    public String getUpgrade() {
      return upgrade;
    }
  }

  @XmlAccessorType(XmlAccessType.FIELD)
  public static class Extension {
    Extension() {}

    Extension(String name, String version) {
      this.name = name;
      this.version = version;
    }

    private String name = null;
    private String version = null;

    /**
     * @return the stack name
     */
    public String getName() {
      return name;
    }

    /**
     * @return the stack version, this may be something like 1.0.*
     */
    public String getVersion() {
      return version;
    }
  }

}
