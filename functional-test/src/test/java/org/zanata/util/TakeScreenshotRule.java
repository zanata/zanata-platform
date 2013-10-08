package org.zanata.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.zanata.page.WebDriverFactory;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class TakeScreenshotRule extends ExternalResource {

    private String testDisplayName;

    @Override
    public Statement apply(Statement base, Description description) {
        testDisplayName = description.getDisplayName();
        return super.apply(base, description);
    }

    @Override
    protected void before() throws Throwable {
        super.before();
        WebDriverFactory.INSTANCE.updateListenerTestName(testDisplayName);
        String date = new Date().toString();
        log.debug("[TEST] {}:{}", testDisplayName, date);
    }

    @Override
    protected void after() {
        super.after();
        File baseDir = new File(WebDriverFactory.getScreenshotBaseDir());
        try {
            FileUtils.deleteDirectory(baseDir);
        }
        catch (IOException e) {
            log.warn("error deleting screenshot base directory: {}",
                e.getMessage());
        }
    }
}
