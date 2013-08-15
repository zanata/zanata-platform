package org.zanata.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;

import java.io.File;
import java.io.IOException;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class CleanDocumentStorageRule extends ExternalResource {

    public static void resetFileData() {
        File path =
                new File(
                        PropertiesHolder
                                .getProperty("document.storage.directory"));
        if (path.exists()) {
            try {
                FileUtils.deleteDirectory(path);
            } catch (IOException e) {
                log.error("Failed to delete", path, e);
                throw new RuntimeException("error");
            }
        }
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
