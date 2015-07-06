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
package org.zanata.page;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogEntry;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.util.PropertiesHolder;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;
import org.zanata.util.ScreenshotDirForTest;
import org.zanata.util.TestEventForScreenshotListener;

import static org.zanata.util.Constants.webDriverType;
import static org.zanata.util.Constants.webDriverWait;
import static org.zanata.util.Constants.zanataInstance;

@Slf4j
public enum WebDriverFactory {
    INSTANCE;

    private volatile EventFiringWebDriver driver = createDriver();
    private DriverService driverService;
    private TestEventForScreenshotListener eventListener;
    private int webdriverWait;

    public WebDriver getDriver() {
        return driver;
    }

    private EventFiringWebDriver createDriver() {
        String driverType = PropertiesHolder.getProperty(webDriverType.value());
        EventFiringWebDriver newDriver;
        switch (driverType.toLowerCase()) {
            case "chrome":
                newDriver = configureChromeDriver();
                break;
            case "firefox":
                newDriver = configureFirefoxDriver();
                break;
            default:
                throw new UnsupportedOperationException("only support chrome " +
                        "and firefox driver");
        }
        webdriverWait = Integer.parseInt(PropertiesHolder
                .getProperty(webDriverWait.value()));
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        return newDriver;
    }

    /**
     * List the WebDriver log types
     *
     * LogType.CLIENT doesn't seem to log anything
     * LogType.DRIVER, LogType.PERFORMANCE are too verbose
     * LogType PROFILER and LogType.SERVER don't seem to work
     *
     * @return String array of log types
     */
    private String[] getLogTypes() {
        return new String[]{
                LogType.BROWSER
        };
    }

    /**
     * Retrieves all the outstanding WebDriver logs of the specified type.
     * @param type a log type from org.openqa.selenium.logging.LogType
     *             (but they don't all seem to work)
     */
    public LogEntries getLogs(String type) {
        return getDriver().manage().logs().get(type);
    }

    /**
     * Logs all the outstanding WebDriver logs of the specified type.
     * @param type a log type from org.openqa.selenium.logging.LogType
     *             (but they don't all seem to work)
     */
    public void logLogs(String type) {
        String logName = WebDriverFactory.class.getName() + "." + type;
        Logger log = LoggerFactory.getLogger(logName);
        int logCount = 0;
        for (LogEntry logEntry : getLogs(type)) {
            ++logCount;
            if (logEntry.getLevel().intValue() >= Level.SEVERE.intValue()) {
                log.error(logEntry.toString());
            } else if (logEntry.getLevel().intValue() >= Level.WARNING.intValue()) {
                log.warn(logEntry.toString());
            } else {
                log.info(logEntry.toString());
            }
        }
        if (logCount == 0) {
            log.info("no messages found for LogType.{}", type);
        }
    }

    public void logLogs() {
        for (String type : getLogTypes()) {
            logLogs(type);
        }
    }

    public String getHostUrl() {
        return PropertiesHolder.getProperty(zanataInstance.value());
    }

    public int getWebDriverWait() {
        return webdriverWait;
    }

    public void registerScreenshotListener(String testName) {
        log.info("Enabling screenshot module...");
        if (eventListener == null && ScreenshotDirForTest.isScreenshotEnabled()) {
            eventListener  = new TestEventForScreenshotListener(driver);
        }
        driver.register(eventListener);
        eventListener.updateTestID(testName);
    }

    public void unregisterScreenshotListener() {
        log.info("Deregistering screenshot module...");
        driver.unregister(eventListener);
    }

    private EventFiringWebDriver configureChromeDriver() {
        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY,
                PropertiesHolder.getProperty("webdriver.log"));
        driverService = ChromeDriverService.createDefaultService();
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities.setCapability("chrome.binary", PropertiesHolder.properties
                        .getProperty("webdriver.chrome.bin"));
        enableLogging(capabilities);

        try {
            driverService.start();
        } catch (IOException e) {
            throw new RuntimeException("fail to start chrome driver service");
        }
        return new EventFiringWebDriver(new Augmenter().augment(
                new RemoteWebDriver(driverService.getUrl(), capabilities)));
    }

    private EventFiringWebDriver configureFirefoxDriver() {
        final String pathToFirefox =
                Strings.emptyToNull(PropertiesHolder.properties
                        .getProperty("firefox.path"));

        FirefoxBinary firefoxBinary;
        if (pathToFirefox != null) {
            firefoxBinary = new FirefoxBinary(new File(pathToFirefox));
        } else {
            firefoxBinary = new FirefoxBinary();
        }
        DesiredCapabilities capabilities = DesiredCapabilities.firefox();
        enableLogging(capabilities);
        return new EventFiringWebDriver(new FirefoxDriver(firefoxBinary,
                makeFirefoxProfile(), capabilities));
    }

    private void enableLogging(DesiredCapabilities capabilities) {
        LoggingPreferences logs = new LoggingPreferences();
        for (String type : getLogTypes()) {
            logs.enable(type, Level.INFO);
        }
        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logs);
    }

    private FirefoxProfile makeFirefoxProfile() {
        if (!Strings.isNullOrEmpty(System
                .getProperty("webdriver.firefox.profile"))) {
            throw new RuntimeException("webdriver.firefox.profile is ignored");
            // TODO - look at FirefoxDriver.getProfile().
        }
        final FirefoxProfile firefoxProfile = new FirefoxProfile();
        firefoxProfile.setAlwaysLoadNoFocusLib(true);
        firefoxProfile.setEnableNativeEvents(true);
        firefoxProfile.setAcceptUntrustedCertificates(true);
        return firefoxProfile;
    }

    private class ShutdownHook extends Thread {
        public void run() {
            // If webdriver is running quit.
            WebDriver driver = getDriver();
            if (driver != null) {
                try {
                    log.info("Quitting webdriver.");
                    driver.quit();
                } catch (Throwable e) {
                    // Ignoring driver tear down errors.
                }
            }
            if (driverService != null && driverService.isRunning()) {
                driverService.stop();
            }
        }
    }
}
