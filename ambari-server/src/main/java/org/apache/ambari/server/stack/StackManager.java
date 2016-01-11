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

package org.apache.ambari.server.stack;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.ambari.server.AmbariException;
import org.apache.ambari.server.api.services.AmbariMetaInfo;
import org.apache.ambari.server.configuration.Configuration;
import org.apache.ambari.server.metadata.ActionMetadata;
import org.apache.ambari.server.orm.dao.ExtensionDAO;
import org.apache.ambari.server.orm.dao.ExtensionLinkDAO;
import org.apache.ambari.server.orm.dao.MetainfoDAO;
import org.apache.ambari.server.orm.dao.StackDAO;
import org.apache.ambari.server.orm.entities.ExtensionEntity;
import org.apache.ambari.server.orm.entities.ExtensionLinkEntity;
import org.apache.ambari.server.orm.entities.StackEntity;
import org.apache.ambari.server.state.ExtensionInfo;
import org.apache.ambari.server.state.ServiceInfo;
import org.apache.ambari.server.state.StackInfo;
import org.apache.ambari.server.state.stack.OsFamily;
import org.apache.ambari.server.state.stack.ServiceMetainfoXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;


/**
 * Manages all stack related behavior including parsing of stacks and providing access to
 * stack information.
 */
public class StackManager {

  /**
   * Delimiter used for parent path string
   * Example:
   *  HDP/2.0.6/HDFS
   *  common-services/HDFS/2.1.0.2.0
   */
  public static String PATH_DELIMITER = "/";

  /**
   * Prefix used for common services parent path string
   */
  public static final String COMMON_SERVICES = "common-services";

  /**
   * Prefix used for extension services parent path string
   */
  public static final String EXTENSIONS = "extensions";

  /**
   * extensions file name
   */
  public static final String EXTENSIONS_XML_FILE_NAME = "extensions.xml";

  public static final String METAINFO_FILE_NAME = "metainfo.xml";

  /**
   * Provides access to non-stack server functionality
   */
  private StackContext stackContext;

  private File stackRoot;

  /**
   * Logger
   */
  private final static Logger LOG = LoggerFactory.getLogger(StackManager.class);

  /**
   * Map of stack id to stack info
   */
  private Map<String, StackInfo> stackMap = new HashMap<String, StackInfo>();

  /**
   * Map of extension id to extension info
   */
  private Map<String, ExtensionInfo> extensionMap = new HashMap<String, ExtensionInfo>();

  /**
   * Constructor. Initialize stack manager.
   *
   * @param stackRoot
   *          stack root directory
   * @param commonServicesRoot
   *          common services root directory
   * @param extensionRoot
   *          extensions root directory
   * @param osFamily
   *          the OS family read from resources
   * @param metaInfoDAO
   *          metainfo DAO automatically injected
   * @param actionMetadata
   *          action meta data automatically injected
   * @param stackDao
   *          stack DAO automatically injected
   * @param extensionDao
   *          extension DAO automatically injected
   * @param linkDao
   *          extension link DAO automatically injected
   *
   * @throws AmbariException
   *           if an exception occurs while processing the stacks
   */
  @Inject
  public StackManager(@Assisted("stackRoot") File stackRoot,
      @Assisted("commonServicesRoot") @Nullable File commonServicesRoot,
      @Assisted("extensionRoot") @Nullable File extensionRoot,
      @Assisted OsFamily osFamily, MetainfoDAO metaInfoDAO,
      ActionMetadata actionMetadata, StackDAO stackDao, ExtensionDAO extensionDao,
      ExtensionLinkDAO linkDao)
      throws AmbariException {

    LOG.info("Initializing the stack manager...");

    validateStackDirectory(stackRoot);
    validateCommonServicesDirectory(commonServicesRoot);
    validateExtensionDirectory(extensionRoot);

    this.stackRoot = stackRoot;
    stackMap = new HashMap<String, StackInfo>();
    stackContext = new StackContext(metaInfoDAO, actionMetadata, osFamily);

    extensionMap = new HashMap<String, ExtensionInfo>();

    Map<String, ServiceModule> commonServiceModules = parseCommonServicesDirectory(commonServicesRoot);
    Map<String, StackModule> stackModules = parseStackDirectory(stackRoot);
    Map<String, ExtensionModule> extensionModules = parseExtensionDirectory(extensionRoot);

    //Read the extension links from the DB
    for (StackModule module : stackModules.values()) {
      StackInfo stack = module.getModuleInfo();
      List<ExtensionLinkEntity> entities = linkDao.findByStack(stack.getName(), stack.getVersion());
      for (ExtensionLinkEntity entity : entities) {
        String name = entity.getExtension().getExtensionName();
        String version = entity.getExtension().getExtensionVersion();
        String key = name + StackManager.PATH_DELIMITER + version;
        ExtensionModule extensionModule = extensionModules.get(key);
        if (extensionModule != null) {
          //Add the extension to the stack
          module.getExtensionModules().put(key, extensionModule);
          //linkStackToExtension(stack, extension);
        }
      }
    }

    fullyResolveCommonServices(stackModules, commonServiceModules, extensionModules);
    fullyResolveExtensions(stackModules, commonServiceModules, extensionModules);
    fullyResolveStacks(stackModules, commonServiceModules, extensionModules);

    populateDB(stackDao, extensionDao);

  }

  private void populateDB(StackDAO stackDao, ExtensionDAO extensionDao) throws AmbariException {

    // for every stack read in, ensure that we have a database entry for it;
    // don't put try/catch logic around this since a failure here will
    // cause other things to break down the road
    Collection<StackInfo> stacks = getStacks();
    for(StackInfo stack : stacks ){
      String stackName = stack.getName();
      String stackVersion = stack.getVersion();

      if (stackDao.find(stackName, stackVersion) == null) {
        LOG.info("Adding stack {}-{} to the database", stackName, stackVersion);

        StackEntity stackEntity = new StackEntity();
        stackEntity.setStackName(stackName);
        stackEntity.setStackVersion(stackVersion);

        stackDao.create(stackEntity);
      }
    }

    // for every extension read in, ensure that we have a database entry for it;
    // don't put try/catch logic around this since a failure here will
    // cause other things to break down the road
    Collection<ExtensionInfo> extensions = getExtensions();
    for(ExtensionInfo extension : extensions ){
      String extensionName = extension.getName();
      String extensionVersion = extension.getVersion();

      if (extensionDao.find(extensionName, extensionVersion) == null) {
        LOG.info("Adding extension {}-{} to the database", extensionName, extensionVersion);

        ExtensionEntity extensionEntity = new ExtensionEntity();
        extensionEntity.setExtensionName(extensionName);
        extensionEntity.setExtensionVersion(extensionVersion);

        extensionDao.create(extensionEntity);
      }
    }

  }

  public void unlinkStackFromExtension(StackInfo stack, ExtensionInfo extension) throws AmbariException {
    /*File stackVersionDir = new File(new File(stackRoot, stack.getName()), stack.getVersion());
    File servicesDir = new File(stackVersionDir, "services");

    for (ServiceInfo service : extension.getServices()) {
      String name = service.getName();
      File serviceDir = new File(servicesDir, name);
      if (serviceDir.exists() && !serviceDir.isFile()) {
        File xmlFile = new File(serviceDir, METAINFO_FILE_NAME);
        if (xmlFile.exists())
          xmlFile.delete();
        serviceDir.delete();
      }
    }*/

  }

  /*private void writeServiceMetainfoXmlFile(ServiceMetainfoXml serviceXml, File file) throws AmbariException {
    PrintWriter writer = null;
    try {
      writer = new PrintWriter(file);
      writer.println("<?xml version=\"1.0\"?>");
      writer.println("<metainfo>");
      writer.println("  <schemaVersion>2.0</schemaVersion>");
      writer.println("  <services>");
      for (ServiceInfo service : serviceXml.getServices()) {
        writer.println("    <service>");
        writer.println("      <name>" + service.getName() + "</name>");
        writer.println("      <extends>" + service.getParent() + "</extends>");
        writer.println("    </service>");
      }
      writer.println("  </services>");
      writer.println("</metainfo>");

      writer.flush();
    }
    catch (Exception e) {
      throw new AmbariException("Unable to create service metainfo file for extension service: " + file.getAbsolutePath(), e);
    }
    finally {
      writer.close();
    }
  }*/

  public void linkStackToExtension(StackInfo stack, ExtensionInfo extension) throws AmbariException {
    /*File stackVersionDir = new File(new File(stackRoot, stack.getName()), stack.getVersion());
    File servicesDir = new File(stackVersionDir, "services");

    for (ServiceInfo service : extension.getServices()) {
      String name = service.getName();
      File serviceDir = new File(servicesDir, name);
      if (serviceDir.exists() && serviceDir.isFile())
        throw new AmbariException("Unable to create service directory for extension service: " + serviceDir.getAbsolutePath());
      if (!serviceDir.exists())
        serviceDir.mkdir();
      if (!serviceDir.exists())
        throw new AmbariException("Unable to create service directory for extension service: " + serviceDir.getAbsolutePath());

      ServiceInfo stackService = new ServiceInfo();
      stackService.setName(name);
      String parent = StackManager.EXTENSIONS + PATH_DELIMITER + extension.getName()
		  + PATH_DELIMITER + extension.getVersion() + PATH_DELIMITER + name;
      stackService.setParent(parent);

      ServiceMetainfoXml serviceXML = new ServiceMetainfoXml();
      List<ServiceInfo> services = new ArrayList<ServiceInfo>();
      services.add(stackService);
      serviceXML.setServices(services);

      File xmlFile = new File(serviceDir, METAINFO_FILE_NAME);
      writeServiceMetainfoXmlFile(serviceXML, xmlFile);
    }*/

  }

  /**
   * Obtain the stack info specified by name and version.
   *
   * @param name     name of the stack
   * @param version  version of the stack
   * @return The stack corresponding to the specified name and version.
   *         If no matching stack exists, null is returned.
   */
  public StackInfo getStack(String name, String version) {
    return stackMap.get(name + StackManager.PATH_DELIMITER + version);
  }

  /**
   * Obtain all stacks for the given name.
   *
   * @param name  stack name
   * @return A collection of all stacks with the given name.
   *         If no stacks match the specified name, an empty collection is returned.
   */
  public Collection<StackInfo> getStacks(String name) {
    Collection<StackInfo> stacks = new HashSet<StackInfo>();
    for (StackInfo stack: stackMap.values()) {
      if (stack.getName().equals(name)) {
        stacks.add(stack);
      }
    }
    return stacks;
  }

  /**
   * Obtain all stacks.
   *
   * @return collection of all stacks
   */
  public Collection<StackInfo> getStacks() {
    return stackMap.values();
  }

  /**
   * Obtain the extension info specified by name and version.
   *
   * @param name     name of the extension
   * @param version  version of the extension
   * @return The extension corresponding to the specified name and version.
   *         If no matching stack exists, null is returned.
   */
  public ExtensionInfo getExtension(String name, String version) {
    return extensionMap.get(name + StackManager.PATH_DELIMITER + version);
  }

  /**
   * Obtain all extensions for the given name.
   *
   * @param name  extension name
   * @return A collection of all extensions with the given name.
   *         If no extensions match the specified name, an empty collection is returned.
   */
  public Collection<ExtensionInfo> getExtensions(String name) {
    Collection<ExtensionInfo> extensions = new HashSet<ExtensionInfo>();
    for (ExtensionInfo extension: extensionMap.values()) {
      if (extension.getName().equals(name)) {
	  extensions.add(extension);
      }
    }
    return extensions;
  }

  /**
   * Obtain all extensions.
   *
   * @return collection of all extensions
   */
  public Collection<ExtensionInfo> getExtensions() {
    return extensionMap.values();
  }

  /**
   * Determine if all tasks which update stack repo urls have completed.
   *
   * @return true if all of the repo update tasks have completed; false otherwise
   */
  public boolean haveAllRepoUrlsBeenResolved() {
    return stackContext.haveAllRepoTasksCompleted();
  }

  /**
   * Fully resolve all stacks.
   *
   * @param stackModules          map of stack id which contains name and version to stack module.
   * @param commonServiceModules  map of common service id which contains name and version to stack module.
   * @throws AmbariException if unable to resolve all stacks
   */
  private void fullyResolveStacks(
      Map<String, StackModule> stackModules, Map<String, ServiceModule> commonServiceModules, Map<String, ExtensionModule> extensions)
      throws AmbariException {
    // Resolve all stacks without finalizing the stacks.
    for (StackModule stack : stackModules.values()) {
      if (stack.getModuleState() == ModuleState.INIT) {
        stack.resolve(null, stackModules, commonServiceModules, extensions);
      }
    }
    // Finalize the common services and stacks to remove sub-modules marked for deletion.
    // Finalizing the stacks AFTER all stacks are resolved ensures that the sub-modules marked for deletion are
    // inherited into the child module when explicit parent is defined and thereby ensuring all modules from parent module
    // are inlined into the child module even if the module is marked for deletion.
    for(ServiceModule commonService : commonServiceModules.values()) {
      commonService.finalizeModule();
    }
    for (ExtensionModule extension : extensions.values()) {
      extension.finalizeModule();
    }
    for (StackModule stack : stackModules.values()) {
      stack.finalizeModule();
    }
    // Execute all of the repo tasks in a single thread executor
    stackContext.executeRepoTasks();
  }

  /**
   * Fully resolve common services.
   *
   * @param stackModules          map of stack id which contains name and version to stack module.
   * @param commonServiceModules  map of common service id which contains name and version to common service module.
   * @throws AmbariException if unable to resolve all common services
   */
  private void fullyResolveCommonServices(
      Map<String, StackModule> stackModules, Map<String, ServiceModule> commonServiceModules, Map<String, ExtensionModule> extensions)
      throws AmbariException {
    for(ServiceModule commonService : commonServiceModules.values()) {
      if (commonService.getModuleState() == ModuleState.INIT) {
        commonService.resolveCommonService(stackModules, commonServiceModules, extensions);
      }
    }
  }

  /**
   * Fully resolve extensions.
   *
   * @param extensionModules      map of extension id which contains name and version to extension module.
   * @param stackModules          map of stack id which contains name and version to stack module.
   * @param commonServiceModules  map of common service id which contains name and version to common service module.
   * @throws AmbariException if unable to resolve all extensions
   */
  private void fullyResolveExtensions(Map<String, StackModule> stackModules, Map<String, ServiceModule> commonServiceModules,
      Map<String, ExtensionModule> extensionModules)
      throws AmbariException {
    for(ExtensionModule extensionModule : extensionModules.values()) {
      if (extensionModule.getModuleState() == ModuleState.INIT) {
        extensionModule.resolve(null, stackModules, commonServiceModules, extensionModules);
      }
    }
  }

  /**
   * Validate that the specified common services root is a valid directory.
   *
   * @param commonServicesRoot the common services root directory to validate
   * @throws AmbariException if the specified common services root directory is invalid
   */
  private void validateCommonServicesDirectory(File commonServicesRoot) throws AmbariException {
    if(commonServicesRoot != null) {
      LOG.info("Validating common services directory {} ...",
          commonServicesRoot);

      String commonServicesRootAbsolutePath = commonServicesRoot.getAbsolutePath();
      if (LOG.isDebugEnabled()) {
        LOG.debug("Loading common services information"
            + ", commonServicesRoot = " + commonServicesRootAbsolutePath);
      }

      if (!commonServicesRoot.isDirectory() && !commonServicesRoot.exists()) {
        throw new AmbariException("" + Configuration.COMMON_SERVICES_DIR_PATH
            + " should be a directory with common services"
            + ", commonServicesRoot = " + commonServicesRootAbsolutePath);
      }
    }
  }

  /**
   * Validate that the specified stack root is a valid directory.
   *
   * @param stackRoot  the stack root directory to validate
   * @throws AmbariException if the specified stack root directory is invalid
   */
  private void validateStackDirectory(File stackRoot) throws AmbariException {
    LOG.info("Validating stack directory {} ...", stackRoot);

    String stackRootAbsPath = stackRoot.getAbsolutePath();
    if (LOG.isDebugEnabled()) {
      LOG.debug("Loading stack information"
          + ", stackRoot = " + stackRootAbsPath);
    }

    if (!stackRoot.isDirectory() && !stackRoot.exists()) {
      throw new AmbariException("" + Configuration.METADATA_DIR_PATH
          + " should be a directory with stack"
          + ", stackRoot = " + stackRootAbsPath);
    }
  }



  /**
   * Validate that the specified extension root is a valid directory.
   *
   * @param extensionRoot  the extension root directory to validate
   * @throws AmbariException if the specified extension root directory is invalid
   */
  private void validateExtensionDirectory(File extensionRoot) throws AmbariException {
    LOG.info("Validating extension directory {} ...", extensionRoot);

    if (extensionRoot == null)
	return;

    String extensionRootAbsPath = extensionRoot.getAbsolutePath();
    if (LOG.isDebugEnabled()) {
      LOG.debug("Loading extension information"
          + ", extensionRoot = " + extensionRootAbsPath);
    }

    //For backwards compatibility extension directory may not exist
    if (extensionRoot.exists() && !extensionRoot.isDirectory()) {
      throw new AmbariException("" + Configuration.METADATA_DIR_PATH
          + " should be a directory"
          + ", extensionRoot = " + extensionRootAbsPath);
    }
  }

  /**
   * Parse the specified common services root directory
   *
   * @param commonServicesRoot  the common services root directory to parse
   * @return map of common service id which contains name and version to common service module.
   * @throws AmbariException if unable to parse all common services
   */
  private Map<String, ServiceModule> parseCommonServicesDirectory(File commonServicesRoot) throws AmbariException {
    Map<String, ServiceModule> commonServiceModules = new HashMap<String, ServiceModule>();

    if(commonServicesRoot != null) {
      File[] commonServiceFiles = commonServicesRoot.listFiles(AmbariMetaInfo.FILENAME_FILTER);
      for (File commonService : commonServiceFiles) {
        if (commonService.isFile()) {
          continue;
        }
        for (File serviceFolder : commonService.listFiles(AmbariMetaInfo.FILENAME_FILTER)) {
          String serviceName = serviceFolder.getParentFile().getName();
          String serviceVersion = serviceFolder.getName();
          ServiceDirectory serviceDirectory = new CommonServiceDirectory(serviceFolder.getPath());
          ServiceMetainfoXml metaInfoXml = serviceDirectory.getMetaInfoFile();
          if (metaInfoXml != null) {
            if (metaInfoXml.isValid()) {
              for (ServiceInfo serviceInfo : metaInfoXml.getServices()) {
                ServiceModule serviceModule = new ServiceModule(stackContext, serviceInfo, serviceDirectory, true);

                String commonServiceKey = serviceInfo.getName() + StackManager.PATH_DELIMITER + serviceInfo.getVersion();
                commonServiceModules.put(commonServiceKey, serviceModule);
              }
            } else {
              ServiceModule serviceModule = new ServiceModule(stackContext, new ServiceInfo(), serviceDirectory, true);
              serviceModule.setValid(false);
              serviceModule.setErrors(metaInfoXml.getErrors());
              commonServiceModules.put(metaInfoXml.getSchemaVersion(), serviceModule);
              metaInfoXml.setSchemaVersion(null);
            }
          }
        }
      }
    }
    return commonServiceModules;
  }

  /**
   * Parse the specified stack root directory
   *
   * @param stackRoot  the stack root directory to parse
   * @return map of stack id which contains name and version to stack module.
   * @throws AmbariException if unable to parse all stacks
   */
  private Map<String, StackModule> parseStackDirectory(File stackRoot) throws AmbariException {
    Map<String, StackModule> stackModules = new HashMap<String, StackModule>();

    File[] stackFiles = stackRoot.listFiles(AmbariMetaInfo.FILENAME_FILTER);
    for (File stack : stackFiles) {
      if (stack.isFile()) {
        continue;
      }
      for (File stackFolder : stack.listFiles(AmbariMetaInfo.FILENAME_FILTER)) {
        if (stackFolder.isFile()) {
          continue;
        }
        String stackName = stackFolder.getParentFile().getName();
        String stackVersion = stackFolder.getName();

        StackModule stackModule = new StackModule(new StackDirectory(stackFolder.getPath()), stackContext);
        String stackKey = stackName + StackManager.PATH_DELIMITER + stackVersion;
        stackModules.put(stackKey, stackModule);
        stackMap.put(stackKey, stackModule.getModuleInfo());
      }
    }

    if (stackMap.isEmpty()) {
      throw new AmbariException("Unable to find stack definitions under " +
          "stackRoot = " + stackRoot.getAbsolutePath());
    }
    return stackModules;
  }

  /**
   * Parse the specified extension root directory
   *
   * @param extensionRoot  the extension root directory to parse
   * @return map of extension id which contains name and version to extension module.
   * @throws AmbariException if unable to parse all extensions
   */
  private Map<String, ExtensionModule> parseExtensionDirectory(File extensionRoot) throws AmbariException {
    Map<String, ExtensionModule> extensionModules = new HashMap<String, ExtensionModule>();
    if (extensionRoot == null || !extensionRoot.exists())
      return extensionModules;

    File[] extensionFiles = extensionRoot.listFiles(AmbariMetaInfo.FILENAME_FILTER);
    for (File extensionNameFolder : extensionFiles) {
      if (extensionNameFolder.isFile()) {
        continue;
      }
      for (File extensionVersionFolder : extensionNameFolder.listFiles(AmbariMetaInfo.FILENAME_FILTER)) {
        if (extensionVersionFolder.isFile()) {
          continue;
        }
        String extensionName = extensionNameFolder.getName();
        String extensionVersion = extensionVersionFolder.getName();

        ExtensionModule extensionModule = new ExtensionModule(new ExtensionDirectory(extensionVersionFolder.getPath()), stackContext);
        String extensionKey = extensionName + StackManager.PATH_DELIMITER + extensionVersion;
        extensionModules.put(extensionKey, extensionModule);
        extensionMap.put(extensionKey, extensionModule.getModuleInfo());
      }
    }

    if (stackMap.isEmpty()) {
      throw new AmbariException("Unable to find extension definitions under " +
          "extensionRoot = " + extensionRoot.getAbsolutePath());
    }
    return extensionModules;
  }
}
