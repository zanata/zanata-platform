/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.util

import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext

import javax.naming.Context
import javax.naming.InitialContext
import javax.naming.NamingException
import java.io.File
import java.io.IOException
import java.util.Properties
import java.lang.Integer.parseInt

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class CleanDocumentStorageExtension : BeforeEachCallback, AfterEachCallback {

    override fun beforeEach(context: ExtensionContext) {
        resetFileData()
    }

    override fun afterEach(context: ExtensionContext) {
        resetFileData()
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(CleanDocumentStorageExtension::class.java)

        private var storagePath: String? = null

        fun resetFileData() {
            val documentStoragePath = documentStoragePath
            log.debug("document storage path: {}", documentStoragePath)
            val path = File(documentStoragePath)
            if (path.exists()) {
                try {
                    FileUtils.deleteDirectory(path)
                } catch (e: IOException) {
                    throw RuntimeException("Error: Failed to delete $path", e)
                }

            }
        }

        // env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
        // wildfly uses 'http-remoting:' not 'remote:'
        // fall back option:
        val documentStoragePath: String
            get() {
                if (storagePath == null) {
                    val env = Properties()
                    env[Context.INITIAL_CONTEXT_FACTORY] = org.jboss.naming.remote.client.InitialContextFactory::class.java
                            .name
                    val portOffset = Integer.parseInt(
                            PropertiesHolder.getProperty("cargo.port.offset", "0")).toLong()
                    val rmiPort = System.getenv("JBOSS_REMOTING_PORT")
                    val rmiPortNum = if (rmiPort != null) parseInt(rmiPort) else 4547
                    val realRmiPort = portOffset + rmiPortNum
                    val remoteUrl = "remote://localhost:$realRmiPort"
                    env[Context.PROVIDER_URL] = remoteUrl
                    var remoteContext: InitialContext?
                    try {
                        remoteContext = InitialContext(env)
                        storagePath = remoteContext
                                .lookup("zanata/files/document-storage-directory") as String
                    } catch (e: NamingException) {
                        val httpPort = System.getenv("JBOSS_HTTP_PORT")
                        val httpPortNum = if (httpPort != null) parseInt(httpPort) else 8180
                        val realHttpPort = httpPortNum + portOffset
                        val httpRemotingUrl = "http-remoting://localhost:$realHttpPort"
                        log.warn("Unable to access {}: {}; trying {}", remoteUrl,
                                e.toString(), httpRemotingUrl)
                        try {
                            env[Context.PROVIDER_URL] = httpRemotingUrl
                            remoteContext = InitialContext(env)
                            storagePath = remoteContext
                                    .lookup("zanata/files/document-storage-directory") as String
                        } catch (e1: NamingException) {
                            log.warn("Unable to access {}: {}", httpRemotingUrl,
                                    e.toString())
                            val testClassRoot = Thread.currentThread().contextClassLoader
                                    .getResource("setup.properties")
                            val targetDir = File(testClassRoot!!.path).parentFile
                            storagePath = File(targetDir, "zanata-documents")
                                    .absolutePath
                        }

                    }

                }
                return storagePath!!
            }
    }
}
