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
package org.zanata.util;

import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;
import static java.lang.Integer.parseInt;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CleanDocumentStorageRule extends ExternalResource {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CleanDocumentStorageRule.class);

    private static String storagePath;

    public static void resetFileData() {
        String documentStoragePath = getDocumentStoragePath();
        log.debug("document storage path: {}", documentStoragePath);
        File path = new File(documentStoragePath);
        if (path.exists()) {
            try {
                FileUtils.deleteDirectory(path);
            } catch (IOException e) {
                log.error("Failed to delete", path, e);
                throw new RuntimeException("error");
            }
        }
    }

    public static String getDocumentStoragePath() {
        if (storagePath == null) {
            final Properties env = new Properties();

            // env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    org.jboss.naming.remote.client.InitialContextFactory.class
                            .getName());
            long portOffset = Integer.parseInt(
                    PropertiesHolder.getProperty("cargo.port.offset", "0"));
            String rmiPort = System.getenv("JBOSS_REMOTING_PORT");
            int rmiPortNum = rmiPort != null ? parseInt(rmiPort) : 4547;
            long realRmiPort = portOffset + rmiPortNum;
            String remoteUrl = "remote://localhost:" + realRmiPort;
            env.put(Context.PROVIDER_URL, remoteUrl);
            InitialContext remoteContext = null;
            try {
                remoteContext = new InitialContext(env);
                storagePath = (String) remoteContext
                        .lookup("zanata/files/document-storage-directory");
            } catch (NamingException e) {
                // wildfly uses 'http-remoting:' not 'remote:'
                String httpPort = System.getenv("JBOSS_HTTP_PORT");
                int httpPortNum = httpPort != null ? parseInt(httpPort) : 8180;
                long realHttpPort = httpPortNum + portOffset;
                String httpRemotingUrl =
                        "http-remoting://localhost:" + realHttpPort;
                log.warn("Unable to access {}: {}; trying {}", remoteUrl,
                        e.toString(), httpRemotingUrl);
                try {
                    env.put(Context.PROVIDER_URL, httpRemotingUrl);
                    remoteContext = new InitialContext(env);
                    storagePath = (String) remoteContext
                            .lookup("zanata/files/document-storage-directory");
                } catch (NamingException e1) {
                    // fall back option:
                    log.warn("Unable to access {}: {}", httpRemotingUrl,
                            e.toString());
                    URL testClassRoot =
                            Thread.currentThread().getContextClassLoader()
                                    .getResource("setup.properties");
                    File targetDir =
                            new File(testClassRoot.getPath()).getParentFile();
                    storagePath = new File(targetDir, "zanata-documents")
                            .getAbsolutePath();
                }
            }
        }
        return storagePath;
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        resetFileData();
    }

    @Override
    protected void after() {
        super.after();
        resetFileData();
    }
}
