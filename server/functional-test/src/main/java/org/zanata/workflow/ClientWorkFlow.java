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
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.IOUtils;
import org.zanata.util.Constants;
import org.zanata.util.PropertiesHolder;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.SimpleTimeLimiter;

public class ClientWorkFlow {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ClientWorkFlow.class);
    private final int timeoutDuration;

    /**
     * @param timeoutDuration
     *            in seconds.
     */
    public ClientWorkFlow(int timeoutDuration) {
        this.timeoutDuration = timeoutDuration;
    }

    /**
     * With default timeout 120 seconds.
     */
    public ClientWorkFlow() {
        timeoutDuration = 120;
    }

    public File getProjectRootPath(String sampleProject) {
        String baseDir =
                PropertiesHolder.getProperty(Constants.sampleProjects.value());
        Preconditions.checkState(
                !(Strings.isNullOrEmpty(sampleProject)
                        || Strings.isNullOrEmpty(baseDir)),
                "base dir and sample project can\'t be empty");
        File projectDir = new File(baseDir, sampleProject);
        ClientWorkFlow.log.info("about to push project at: {}",
                projectDir.getAbsolutePath());
        return projectDir;
    }

    public static String getUserConfigPath(String user) {
        String configName = "zanata-" + user + ".ini";
        URL resource = Thread.currentThread().getContextClassLoader()
                .getResource(configName);
        Preconditions.checkNotNull(resource, configName + " can not be found.");
        return resource.getPath();
    }

    public List<String> callWithTimeout(final File workingDirectory,
            String command) {
        log.info("=== about to call ===\n{}", command);
        if (!workingDirectory.isDirectory()) {
            throw new RuntimeException(
                    "working directory does not exist: " + workingDirectory);
        }
        final List<String> commands =
                Lists.newArrayList(Splitter.on(" ").split(command));
        SimpleTimeLimiter timeLimiter = new SimpleTimeLimiter();
        Callable<List<String>> work = () -> {
            Process process =
                    ClientWorkFlow.invokeClient(workingDirectory, commands);
            process.waitFor();
            List<String> output = ClientWorkFlow.getOutput(process);
            logOutputLines(output);
            return output;
        };
        try {
            return timeLimiter.callWithTimeout(work, timeoutDuration,
                    TimeUnit.SECONDS, true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPushSuccessful(List<String> output) {
        Optional<String> successOutput = Iterables.tryFind(output,
                input -> input.contains("BUILD SUCCESS"));
        return successOutput.isPresent();
    }

    private static synchronized Process invokeClient(File projectDir,
            List<String> command) throws IOException {
        ProcessBuilder processBuilder =
                new ProcessBuilder(command).redirectErrorStream(true);

        // Map<String, String> env = processBuilder.environment();
        // log.debug("env: {}", env);
        processBuilder.directory(projectDir);
        return processBuilder.start();
    }

    private void logOutputLines(List<String> output) {
        for (String line : output) {
            ClientWorkFlow.log.info(line);
        }
    }

    /**
     * Returns process's output as a list of strings; closes all I/O streams.
     *
     * @param process
     * @return
     * @throws IOException
     */
    private static List<String> getOutput(Process process) throws IOException {
        try (
                InputStream in = process.getInputStream();
                InputStream stdErr = process.getErrorStream();
                OutputStream ignored2 = process.getOutputStream()) {
            List<String> output = IOUtils.readLines(in, Charsets.UTF_8);
            output.addAll(IOUtils.readLines(stdErr, Charsets.UTF_8));
            return output;
        }
    }
}
