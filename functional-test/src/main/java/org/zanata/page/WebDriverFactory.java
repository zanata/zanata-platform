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
import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxBinary;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.os.CommandLine;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.service.DriverService;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.zanata.util.PropertiesHolder;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.zanata.util.ScreenshotDirForTest;
import org.zanata.util.TestEventForScreenshotListener;

import static org.zanata.util.Constants.chrome;
import static org.zanata.util.Constants.firefox;
import static org.zanata.util.Constants.webDriverType;
import static org.zanata.util.Constants.webDriverWait;
import static org.zanata.util.Constants.zanataInstance;

@Slf4j
public enum WebDriverFactory {
    INSTANCE;

    private volatile WebDriver driver = createDriver();
    private DriverService driverService;
    private TestEventForScreenshotListener eventListener;
    private int webdriverWait;

    public WebDriver getDriver() {
        return driver;
    }

    public WebDriver createDriver() {
        WebDriver driver = createPlainDriver();
        webdriverWait = Integer.parseInt(PropertiesHolder
                .getProperty(webDriverWait.value()));
        driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        return driver;
    }

    public String getHostUrl() {
        return PropertiesHolder.getProperty(zanataInstance.value());
    }

    public int getWebDriverWait() {
        return webdriverWait;
    }

    public void updateListenerTestName(String testName) {
        if (eventListener == null && ScreenshotDirForTest.isScreenshotEnabled()) {
            eventListener  = new TestEventForScreenshotListener(driver);
        }
        enableScreenshots();
        eventListener.updateTestID(testName);
    }

    private WebDriver enableScreenshots() {
        log.debug("Enabling screenshot module...");
        return EventFiringWebDriver.class.cast(driver).register(eventListener);
    }

    public void unregisterScreenshot() {
        EventFiringWebDriver.class.cast(driver).unregister(eventListener);
    }

    private WebDriver createPlainDriver() {
        String driverType =
                PropertiesHolder.getProperty(webDriverType.value(), chrome.value());
        if (driverType.equalsIgnoreCase(chrome.value())) {
            return configureChromeDriver();
        } else if (driverType.equalsIgnoreCase(firefox.value())) {
            return configureFirefoxDriver();
        } else {
            throw new UnsupportedOperationException("only support chrome and firefox driver");
        }
    }

    private WebDriver configureChromeDriver() {
        // TODO can we use this? it will use less code, but it will use DISPLAY rather than webdriver.display
//        System.setProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY,
//                getChromeDriver().getAbsolutePath()));
//        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY,
//                PropertiesHolder.getProperty("webdriver.log"));
//        driverService = ChromeDriverService.createDefaultService();

        @SuppressWarnings("deprecation")
        File chromeDriver = getChromeDriver();
        driverService =
                new ChromeDriverService.Builder()
                        .usingDriverExecutable(
                                chromeDriver)
                        .usingAnyFreePort()
                        .withEnvironment(
                                ImmutableMap
                                        .of("DISPLAY",
                                                PropertiesHolder.properties
                                                        .getProperty("webdriver.display")))
                        .withLogFile(
                                new File(PropertiesHolder.properties
                                        .getProperty("webdriver.log"))).build();

        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        capabilities
                .setCapability("chrome.binary", PropertiesHolder.properties
                        .getProperty("webdriver.chrome.bin"));
        try {
            driverService.start();
        } catch (IOException e) {
            throw new RuntimeException("fail to start chrome driver service");
        }
        return new EventFiringWebDriver(
                new Augmenter().augment(new RemoteWebDriver(driverService
                        .getUrl(),
                                capabilities)));
    }

    /**
     * Returns a File pointing to a chromedriver binary, searching the PATH
     * if necessary. If the property 'webdriver.chrome.driver' points to an
     * executable, uses that.  Otherwise, searches PATH for the specified
     * executable.  Searches for 'chromedriver' if the property is null or
     * empty.
     * @return a File pointing to the executable
     * @deprecated use ChromeDriverService.createDefaultService() if you can
     */
    @Deprecated
    private File getChromeDriver() {
        String exeName = PropertiesHolder.getProperty("webdriver.chrome.driver");
        if (exeName == null || exeName.isEmpty()) {
            exeName = CommandLine.find("chromedriver");
        } else if (!new File(exeName).canExecute()) {
            exeName = CommandLine.find(exeName);
        }
        if (exeName == null) {
            throw new RuntimeException("Please ensure chromedriver is on " +
                    "your system PATH or specified by the property " +
                    "'webdriver.chrome.driver'.  Get it here: " +
                    "http://chromedriver.storage.googleapis.com/index.html");
        }
        return new File(exeName);
    }

    private WebDriver configureFirefoxDriver() {
        final String pathToFirefox =
                Strings.emptyToNull(PropertiesHolder.properties
                        .getProperty("firefox.path"));

        FirefoxBinary firefoxBinary = null;
        if (pathToFirefox != null) {
            firefoxBinary = new FirefoxBinary(new File(pathToFirefox));
        } else {
            firefoxBinary = new FirefoxBinary();
        }
        /*
         * TODO: Evaluate current timeout Timeout the connection in 30 seconds
         * firefoxBinary.setTimeout(TimeUnit.SECONDS.toMillis(30));
         */
        firefoxBinary.setEnvironmentProperty("DISPLAY",
                PropertiesHolder.properties.getProperty("webdriver.display"));
        return new FirefoxDriver(firefoxBinary, makeFirefoxProfile());
    }

    private FirefoxProfile makeFirefoxProfile() {
        if (!Strings.isNullOrEmpty(System
                .getProperty("webdriver.firefox.profile"))) {
            throw new RuntimeException("webdriver.firefox.profile is ignored");
            // TODO - look at FirefoxDriver.getProfile().
        }
        final FirefoxProfile firefoxProfile = new FirefoxProfile();

        /*
         * TODO: Evaluate need for this Disable unnecessary connection to
         * sb-ssl.google.com
         * firefoxProfile.setPreference("browser.safebrowsing.malware.enabled",
         * false);
         */

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
