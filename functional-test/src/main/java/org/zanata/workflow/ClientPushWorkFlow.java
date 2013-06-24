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

import org.zanata.util.PropertiesHolder;
import org.zanata.util.Constants;
import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.io.CharStreams;
import com.google.common.io.InputSupplier;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientPushWorkFlow
{

   // TODO remove this and use invokeClient instead
   public int mvnPush(String sampleProject, String... extraPushOptions)
   {
      File projectDir = getProjectRootPath(sampleProject);
      List<String> command = zanataMavenPushCommand(extraPushOptions);

      Process process = null;
      try
      {
         Joiner joiner = Joiner.on(" ");
         log.info("execute command: \n{}\n", joiner.join(command));

         process = invokeClient(projectDir, command);
         process.waitFor();

         return process.exitValue();

      }
      catch (Exception e)
      {
         log.error("exception", e);
         return 1;
      }
   }

   public File getProjectRootPath(String sampleProject)
   {
      String baseDir = PropertiesHolder.getProperty(Constants.sampleProjects.value());
      Preconditions.checkState(!(Strings.isNullOrEmpty(sampleProject) || Strings.isNullOrEmpty(baseDir)), "base dir and sample project can't be empty");

      File projectDir = new File(baseDir, sampleProject);
      log.info("about to push project at: {}", projectDir.getAbsolutePath());
      return projectDir;
   }

   public static List<String> zanataMavenPushCommand(String... extraPushOptions)
   {
      String userConfig = getAdminUserConfigPath();
      // @formatter:off
      ImmutableList.Builder<String> builder = ImmutableList.<String>builder()
            .add("mvn").add("--batch-mode")
            .add("zanata:push")
            .add("-Dzanata.userConfig=" + userConfig)
            .add(extraPushOptions);
      // @formatter:on
      return builder.build();
   }

   public static String getAdminUserConfigPath()
   {
      return getUserConfigPath("admin");
   }

   public static String getUserConfigPath(String user)
   {
      String configName = "zanata-" + user + ".ini";
      URL resource = Thread.currentThread().getContextClassLoader().getResource(configName);
      Preconditions.checkNotNull(resource, configName + " can not be found.");
      return resource.getPath();
   }

   public synchronized static Process invokeClient(File projectDir, List<String> command) throws IOException
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

   public static List<String> getOutput(Process process) throws IOException
   {
      final InputStream inputStream = process.getInputStream();

      return CharStreams.readLines(CharStreams.newReaderSupplier(new InputSupplier<InputStream>()
      {
         @Override
         public InputStream getInput() throws IOException
         {
            return inputStream;
         }
      }, Charsets.UTF_8));
   }
}
