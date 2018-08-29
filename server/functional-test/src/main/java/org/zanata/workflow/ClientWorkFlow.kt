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
package org.zanata.workflow

import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings
import org.apache.commons.io.IOUtils
import org.zanata.util.Constants
import org.zanata.util.PropertiesHolder
import com.google.common.base.Charsets
import com.google.common.base.Preconditions
import com.google.common.base.Splitter
import com.google.common.base.Strings
import com.google.common.collect.Lists
import com.google.common.util.concurrent.SimpleTimeLimiter

import java.util.concurrent.Executors.newCachedThreadPool

class ClientWorkFlow {
    private val timeoutDuration: Int

    /**
     * @param timeoutDuration in seconds.
     */
    @Suppress("unused")
    constructor(timeoutDuration: Int) {
        this.timeoutDuration = timeoutDuration
    }

    /**
     * With default timeout 120 seconds.
     */
    constructor() {
        timeoutDuration = 120
    }

    fun getProjectRootPath(sampleProject: String): File {
        val baseDir = PropertiesHolder.getProperty(Constants.sampleProjects.value())
        Preconditions.checkState(
                !(Strings.isNullOrEmpty(sampleProject) || Strings.isNullOrEmpty(baseDir)),
                "base dir and sample project can\'t be empty")
        val projectDir = File(baseDir, sampleProject)
        ClientWorkFlow.log.info("about to push project at: {}",
                projectDir.absolutePath)
        return projectDir
    }

    @SuppressFBWarnings(value = ["GBU_GUAVA_BETA_CLASS_USAGE"], justification = "field SimpleTimeLimiter")
    fun callWithTimeout(workingDirectory: File,
                        command: String): List<String> {
        log.info("CallWithTimeout:\n{}\n{}", workingDirectory, command)
        if (!workingDirectory.isDirectory) {
            throw RuntimeException(
                    "working directory does not exist: $workingDirectory")
        }
        val commands = Lists.newArrayList(Splitter.on(" ").split(command))
        val timeLimiter = SimpleTimeLimiter.create(
                newCachedThreadPool())
        val work = {
            val process = ClientWorkFlow.invokeClient(workingDirectory, commands)
            process.waitFor()
            val output = ClientWorkFlow.getOutput(process)
            logOutputLines(output)
            output
        }
        try {
            return timeLimiter.callWithTimeout(work, timeoutDuration.toLong(),
                    TimeUnit.SECONDS)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    fun isPushSuccessful(output: List<String>): Boolean {
        return output.stream().anyMatch { s -> s.contains("BUILD SUCCESS") }
    }

    private fun logOutputLines(output: List<String>) {
        for (line in output) {
            ClientWorkFlow.log.info(line)
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ClientWorkFlow::class.java)

        fun getUserConfigPath(user: String): String {
            val configName = "zanata-$user.ini"
            val resource = Thread.currentThread().contextClassLoader
                    .getResource(configName)
            Preconditions.checkNotNull(resource, "$configName can not be found.")
            return resource!!.path
        }

        @Synchronized
        @Throws(IOException::class)
        private fun invokeClient(projectDir: File,
                                 command: List<String>): Process {
            val processBuilder = ProcessBuilder(command).redirectErrorStream(true)

            // Map<String, String> env = processBuilder.environment();
            // log.debug("env: {}", env);
            processBuilder.directory(projectDir)
            return processBuilder.start()
        }

        /**
         * Returns process's output as a list of strings; closes all I/O streams.
         *
         * @param process
         * @return
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun getOutput(process: Process): List<String> {
            process.inputStream.use { `in` ->
                process.errorStream.use { stdErr ->
                    val output = IOUtils.readLines(`in`, Charsets.UTF_8)
                    output.addAll(IOUtils.readLines(stdErr, Charsets.UTF_8))
                    return output
                }
            }
        }
    }
}
