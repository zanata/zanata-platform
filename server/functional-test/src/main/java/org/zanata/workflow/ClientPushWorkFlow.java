/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.workflow;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.util.Constants;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

public class ClientPushWorkFlow
{
   private static final Logger LOGGER = LoggerFactory.getLogger(ClientPushWorkFlow.class);
   private static Properties properties;

   public ClientPushWorkFlow()
   {
      properties = Constants.loadProperties();
   }

   public int mvnPush(String sampleProject)
   {
      String baseDir = properties.getProperty(Constants.sampleProjects.value());
      Preconditions.checkState(!(Strings.isNullOrEmpty(sampleProject) || Strings.isNullOrEmpty(baseDir)), "base dir and sample project can't be empty");

      File projectDir = new File(baseDir, sampleProject);
      LOGGER.info("about to push project at: {}", projectDir.getAbsolutePath());

      Process process = null;
      try
      {
         List<String> command = zanataMavenPushCommand();
         LOGGER.info("execute command: {}", command);

         process = invokeMaven(projectDir, command);
         process.waitFor();

         printOutput(process);
         return process.exitValue();

      }
      catch (Exception e)
      {
         LOGGER.error("exception", e);
         return 1;
      }
   }

   private static List<String> zanataMavenPushCommand()
   {
      String userConfig = getUserConfigPath();
      return ImmutableList.<String>builder()
                  .add("mvn").add("--batch-mode").add("zanata:push")
                  .add("-Dzanata.userConfig=" + userConfig).add("-Dzanata.username=admin")
                  .add("-Dzanata.key=" + properties.getProperty(Constants.zanataApiKey.value()))
                  .build();
   }

   private static String getUserConfigPath()
   {
      URL resource = Thread.currentThread().getContextClassLoader().getResource("zanata-autotest.ini");
      Preconditions.checkNotNull(resource, "userConfig can not be found.");
      return resource.getPath();
   }

   private synchronized static Process invokeMaven(File projectDir, List<String> command) throws IOException
   {
      ProcessBuilder processBuilder = new ProcessBuilder(command).redirectErrorStream(true);
      Map<String, String> env = processBuilder.environment();
      // mvn and java home
      LOGGER.info("M2: {}", env.get("M2"));
      LOGGER.info("JAVA_HOME: {}", env.get("JAVA_HOME"));
      LOGGER.debug("env: {}", env);
      processBuilder.directory(projectDir);
      return processBuilder.start();
   }

   private static void printOutput(Process process) throws IOException
   {
      InputStream inputStream = process.getInputStream();
      List<String> lines = IOUtils.readLines(inputStream);
      for (String line : lines)
      {
         LOGGER.info(line);
      }
      IOUtils.closeQuietly(inputStream);
   }
}
