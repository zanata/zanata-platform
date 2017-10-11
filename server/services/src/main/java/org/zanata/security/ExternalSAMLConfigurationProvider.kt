/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.security

import org.picketlink.common.ErrorCodes
import org.picketlink.common.exceptions.ConfigurationException
import org.picketlink.common.exceptions.ProcessingException
import org.picketlink.config.federation.IDPType
import org.picketlink.config.federation.PicketLinkType
import org.picketlink.config.federation.SPType
import org.picketlink.identity.federation.web.config.AbstractSAMLConfigurationProvider
import org.picketlink.identity.federation.web.util.ConfigurationUtil
import org.slf4j.LoggerFactory
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Paths

/**
 * This file is responsible to load the picketlink configuration file (path
 * given by system property).
 *
 * @author Patrick Huang
 * [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class ExternalSAMLConfigurationProvider : AbstractSAMLConfigurationProvider() {

    @Throws(ProcessingException::class)
    override fun getIDPConfiguration(): IDPType {
        throw RuntimeException(ErrorCodes.ILLEGAL_METHOD_CALLED)
    }

    @Throws(ProcessingException::class)
    override fun getSPConfiguration(): SPType? {
        try {
            val inputStream: InputStream? = readConfigurationFile()
            return inputStream?.use {
                ConfigurationUtil.getSPConfiguration(it)
            }
        } catch (e: Exception) {
            throw RuntimeException("Could not load SP configuration: $configurationFilePath", e)
        }
    }

    @Throws(ProcessingException::class)
    override fun getPicketLinkConfiguration(): PicketLinkType? {
        try {
            val inputStream: InputStream? = readConfigurationFile()
            return inputStream?.use {
                ConfigurationUtil.getConfiguration(it)
            }

        } catch (e: Exception) {
            throw RuntimeException(
                    "Could not load PicketLink configuration: $configurationFilePath", e)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(ExternalSAMLConfigurationProvider::class.java)

        private val configFile: String? = System.getProperty("picketlink.file")

        // Returns the picketlink configuration file path including protocol.
        private val configurationFilePath: URL by lazy { Paths.get(configFile).toUri().toURL() }

        private fun isFileExists(filePath: String): Boolean {
            val file = File(filePath)
            return file.exists() && file.canRead()
        }

        @Throws(ConfigurationException::class)
        private fun readConfigurationFile(): InputStream? {
            if (configFile == null || !isFileExists(configFile)) {
                log.info("picketlink.xml can not be found: {}", configFile)
                return null
            }

            return try {
                val configurationFileURL: URL = Thread.currentThread()
                        .contextClassLoader.getResource(configFile) ?: configurationFilePath

                configurationFileURL.openStream()
            } catch (e: Exception) {
                throw RuntimeException(
                        "The file could not be loaded: $configFile", e)
            }

        }
    }
}
