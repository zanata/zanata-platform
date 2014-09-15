package org.zanata.util;

import com.google.common.base.Throwables;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class CleanDocumentStorageRule extends ExternalResource {

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
//            env.put(Context.URL_PKG_PREFIXES, "org.jboss.ejb.client.naming");
            env.put(Context.INITIAL_CONTEXT_FACTORY,
                    org.jboss.naming.remote.client.InitialContextFactory.class
                            .getName());
            long portOffset = Integer.parseInt(
                PropertiesHolder.getProperty("cargo.port.offset", "0"));
            long rmiPort = 4447 + portOffset;
            env.put(Context.PROVIDER_URL, "remote://localhost:" + rmiPort);
            InitialContext remoteContext = null;
            try {
                remoteContext = new InitialContext(env);
                storagePath =
                    (String) remoteContext
                        .lookup("zanata/files/document-storage-directory");
            }
            catch (NamingException e) {
                try {
                    // wildfly uses 'http-remoting:' not 'remote:'
                    rmiPort = 8080+portOffset;
                    env.put(Context.PROVIDER_URL, "http-remoting://localhost:" + rmiPort);
                    remoteContext = new InitialContext(env);
                    storagePath =
                            (String) remoteContext
                                    .lookup("zanata/files/document-storage-directory");
                } catch (NamingException e1) {
                    // fall back option:
                    URL testClassRoot =
                            Thread.currentThread().getContextClassLoader()
                                    .getResource("setup.properties");
                    File targetDir =
                            new File(testClassRoot.getPath()).getParentFile();
                    storagePath =
                            new File(targetDir, "zanata-documents")
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
