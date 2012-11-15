/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
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

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientPushWorkFlow
{
   private static Properties properties;

   public ClientPushWorkFlow()
   {
      properties = Constants.loadProperties();
   }

   public int mvnPush(String sampleProject, String... extraPushOptions)
   {
      String baseDir = properties.getProperty(Constants.sampleProjects.value());
      Preconditions.checkState(!(Strings.isNullOrEmpty(sampleProject) || Strings.isNullOrEmpty(baseDir)), "base dir and sample project can't be empty");

      File projectDir = new File(baseDir, sampleProject);
      log.info("about to push project at: {}", projectDir.getAbsolutePath());

      Process process = null;
      try
      {
         List<String> command = zanataMavenPushCommand(extraPushOptions);
         Joiner joiner = Joiner.on(" ");
         log.info("execute command: \n{}\n", joiner.join(command));

         process = invokeMaven(projectDir, command);
         process.waitFor();

         printOutput(process);
         return process.exitValue();

      }
      catch (Exception e)
      {
         log.error("exception", e);
         return 1;
      }
   }

   private static List<String> zanataMavenPushCommand(String... extraPushOptions)
   {
      String userConfig = getUserConfigPath();
      // @formatter:off
      ImmutableList.Builder<String> builder = ImmutableList.<String>builder()
            .add("mvn").add("--batch-mode")
            .add("zanata:push")
            .add("-Dzanata.userConfig=" + userConfig)
            .add("-Dzanata.username=admin")
            .add("-Dzanata.key=" + properties.getProperty(Constants.zanataApiKey.value()))
            .add(extraPushOptions);
      // @formatter:on
      return builder.build();
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
      log.info("M2: {}", env.get("M2"));
      log.info("JAVA_HOME: {}", env.get("JAVA_HOME"));
//      log.debug("env: {}", env);
      processBuilder.directory(projectDir);
      return processBuilder.start();
   }

   private static void printOutput(Process process) throws IOException
   {
      InputStream inputStream = process.getInputStream();
      List<String> lines = IOUtils.readLines(inputStream);
      for (String line : lines)
      {
         log.info(line);
      }
      IOUtils.closeQuietly(inputStream);
   }
}
