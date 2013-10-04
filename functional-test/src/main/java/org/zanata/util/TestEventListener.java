package org.zanata.util;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.events.WebDriverEventListener;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class TestEventListener implements WebDriverEventListener {

    private WebDriver driver;
    private String targetDir;
    private String testId = "";

    /**
     * A registered TestEventListener will perform actions on navigate,
     * click and exception events
     * @param drv the WebDriver to derive screen shots from
     * @param targetDirectory full path to screen shot storage
     */
    public TestEventListener(WebDriver drv, String targetDirectory) {
        driver = drv;
        targetDir =
                targetDirectory.endsWith("/") ? targetDirectory
                        : targetDirectory.concat("/");
        log.info("Writing screenshots to {}", targetDir);
    }

    /**
     * Update the screen shot directory/filename test ID component
     * @param testId test identifier string
     */
    public void updateTestID(String testId) {
        this.testId = testId;
    }

    private void createScreenshot(String ofType) {
        String outputDir;
        try {
            outputDir = targetDir.concat(testId);
            File testIDDir = new File(outputDir);
            testIDDir.mkdirs();
            File screenshotFile =
                    ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            FileUtils.copyFile(screenshotFile, new File(outputDir
                    + File.separator + generateFileName(ofType)));

        } catch (WebDriverException wde) {
            throw new RuntimeException("[Screenshot]: Invalid WebDriver: "
                    + wde.getMessage());
        } catch (IOException ioe) {
            throw new RuntimeException("[Screenshot]: Failed to write to "
                    + targetDir);
        } catch (NullPointerException npe) {
            throw new RuntimeException("[Screenshot]: Null Object: "
                    + npe.getMessage());
        }
    }

    private String generateFileName(String ofType) {
        return testId.concat(":").concat(String.valueOf(new Date().getTime()))
                .concat(ofType).concat(".png");
    }

    private boolean isAlertPresent(WebDriver driver) {
        try {
            driver.switchTo().alert();
            return true;
        } catch (NoAlertPresentException nape) {
            return false;
        }
    }

    @Override
    public void afterNavigateTo(String url, WebDriver driver) {
        createScreenshot("_nav");
    }

    @Override
    public void beforeClickOn(WebElement element, WebDriver driver) {
        createScreenshot("_preclick");
    }

    @Override
    public void afterClickOn(WebElement element, WebDriver driver) {
        if (isAlertPresent(driver)) {
            log.info("[Screenshot]: Prevented by Alert");
            return;
        }
        createScreenshot("_click");
    }

    @Override
    public void onException(Throwable throwable, WebDriver driver) {
        createScreenshot("_exc");
    }

    /*
     * Unused interface functions
     */
    @Override
    public void afterChangeValueOf(WebElement element, WebDriver driver) {
    }

    @Override
    public void beforeNavigateTo(String url, WebDriver driver) {
    }

    @Override
    public void beforeNavigateBack(WebDriver driver) {
    }

    @Override
    public void afterNavigateBack(WebDriver driver) {
    }

    @Override
    public void beforeNavigateForward(WebDriver driver) {
    }

    @Override
    public void afterNavigateForward(WebDriver driver) {
    }

    @Override
    public void beforeFindBy(By by, WebElement element, WebDriver driver) {
    }

    @Override
    public void afterFindBy(By by, WebElement element, WebDriver driver) {
    }

    @Override
    public void beforeChangeValueOf(WebElement element, WebDriver driver) {
    }

    @Override
    public void beforeScript(String script, WebDriver driver) {
    }

    @Override
    public void afterScript(String script, WebDriver driver) {
    }

}
