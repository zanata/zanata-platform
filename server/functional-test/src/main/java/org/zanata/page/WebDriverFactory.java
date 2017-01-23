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
import java.lang.reflect.Method;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Supplier;
import java.util.logging.Level;
import com.google.common.reflect.AbstractInvocationHandler;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.filters.ResponseFilterAdapter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
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
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.util.PropertiesHolder;
import com.google.common.base.Strings;
import org.zanata.util.ScreenshotDirForTest;
import org.zanata.util.TestEventForScreenshotListener;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import static java.lang.reflect.Proxy.newProxyInstance;
import static org.zanata.util.Constants.webDriverType;
import static org.zanata.util.Constants.webDriverWait;
import static org.zanata.util.Constants.zanataInstance;

public enum WebDriverFactory {
    INSTANCE;
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(WebDriverFactory.class);

    private static final ThreadLocal<SimpleDateFormat> TIME_FORMAT =
            new ThreadLocal<SimpleDateFormat>() {

                @Override
                protected SimpleDateFormat initialValue() {
                    return new SimpleDateFormat("HH:mm:ss.SSS");
                }
            };
    // can reuse, share globally
    private static ObjectMapper mapper = new ObjectMapper();
    private static final boolean useProxy = true;
    @Nullable
    private EventFiringWebDriver driver;
    @Nonnull
    private DswidParamChecker dswidParamChecker;
    private DriverService driverService;
    private TestEventForScreenshotListener screenshotListener;
    private int webdriverWait;
    private final String[] ignoredLogPatterns =
            { ".*/org.richfaces/jquery.js .* \'webkit(?:Cancel)?RequestAnimationFrame\' is vendor-specific. Please use the standard \'(?:request|cancel)AnimationFrame\' instead.",
                    ".*/org.richfaces/jquery.js .* \'webkitMovement[XY]\' is deprecated. Please use \'movement[XY]\' instead.",
                    "http://example.com/piwik/piwik.js .*" };
    @Nullable
    private WebDriverEventListener logListener;
    @Nullable
    private BrowserMobProxy proxy;
    // we can't declare this as a field because it is needed during init

    private static final Logger log() {
        return LoggerFactory.getLogger(WebDriverFactory.class);
    }

    public synchronized EventFiringWebDriver getDriver() {
        if (driver == null) {
            driver = createDriver();
        }
        return driver;
    }

    private JavascriptExecutor getExecutor() {
        return (JavascriptExecutor) getDriver();
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
            throw new UnsupportedOperationException(
                    "only support chrome and firefox driver");

        }
        Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        webdriverWait = Integer
                .parseInt(PropertiesHolder.getProperty(webDriverWait.value()));
        dswidParamChecker = new DswidParamChecker(newDriver);
        newDriver.register(dswidParamChecker.getEventListener());
        return newDriver;
    }

    /**
     * List the WebDriver log types
     *
     * LogType.CLIENT doesn't seem to log anything LogType.DRIVER,
     * LogType.PERFORMANCE are too verbose LogType PROFILER and LogType.SERVER
     * don't seem to work
     *
     * @return String array of log types
     */
    private String[] getLogTypes() {
        return new String[] { LogType.BROWSER };
    }

    /**
     * Retrieves all the outstanding WebDriver logs of the specified type.
     *
     * @param type
     *            a log type from org.openqa.selenium.logging.LogType (but they
     *            don't all seem to work)
     */
    public LogEntries getLogs(String type) {
        return getDriver().manage().logs().get(type);
    }

    private String toString(long timestamp, String text,
            @Nullable String json) {
        String time = TIME_FORMAT.get().format(new Date(timestamp));
        return time + " " + text + (json != null ? ": " + json : "");
    }

    private boolean ignorable(String message) {
        for (String ignorable : ignoredLogPatterns) {
            if (message.matches(ignorable)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Logs all the outstanding WebDriver logs of the specified type.
     *
     * @param type
     *            a log type from org.openqa.selenium.logging.LogType (but they
     *            don't all seem to work)
     * @throws WebDriverLogException
     *             exception containing the first warning/error message, if any
     */
    private void logLogs(String type, boolean throwIfWarn) {
        WebDriver driver = getDriver();
        @Nullable
        WebDriverLogException firstException = null;
        String logName = WebDriverFactory.class.getName() + "." + type;
        Logger log = LoggerFactory.getLogger(logName);
        int logCount = 0;
        for (LogEntry logEntry : getLogs(type)) {
            ++logCount;
            Level level;
            long time = logEntry.getTimestamp();
            String text;
            String json;
            String msg = logEntry.getMessage();
            if (msg.startsWith("{")) {
                // looks like it might be json
                json = msg;
                try {
                    JsonNode message = mapper.readValue(json, ObjectNode.class)
                            .path("message");
                    String levelString = message.path("level").asText();
                    level = toLogLevel(levelString);
                    text = message.path("text").asText();
                } catch (Exception e) {
                    log.warn("unable to parse as json: " + json, e);
                    level = logEntry.getLevel();
                    text = msg;
                    json = null;
                }
            } else {
                level = logEntry.getLevel();
                text = msg;
                json = null;
            }
            String logString = toString(time, text, json);
            if (level.intValue() >= Level.SEVERE.intValue()) {
                log.error(logString);
                // If firstException was a warning, replace it with this error.
                if ((firstException == null || !firstException.isErrorLog())
                        && !ignorable(msg)) {
                    // We only throw this if throwIfWarn is true
                    firstException = new WebDriverLogException(level, logString,
                            driver.getPageSource());
                }
            } else if (level.intValue() >= Level.WARNING.intValue()) {
                log.warn(logString);
                if ((firstException == null) && !ignorable(msg)) {
                    // We only throw this if throwIfWarn is true
                    firstException =
                            new WebDriverLogException(logEntry.getLevel(),
                                    logString, driver.getPageSource());
                }
            } else if (level.intValue() >= Level.INFO.intValue()) {
                log.info(logString);
            } else {
                log.debug(logString);
            }
        }
        if (throwIfWarn && firstException != null) {
            throw firstException;
        }
    }

    private Level toLogLevel(String jsLevel) {
        String upperCase = jsLevel.toUpperCase();
        if (upperCase.equals("WARN")) {
            return Level.WARNING;
        }
        return Level.parse(upperCase);
    }

    /**
     * Dump any outstanding browser logs to the main log.
     *
     * @throws WebDriverLogException
     *             exception containing the first error message, if any
     */
    public void logLogs() {
        // DeltaSpike's LAZY mode uses client-side redirects, which cause
        // other scripts to abort loading in strange ways when dswid is
        // missing/wrong. However, the server code currently preserves dswid
        // whenever possible, and DswidParamChecker aims to make sure it
        // continues to, so we can treat JS warnings/errors as failures.
        logLogs(false);
    }

    /**
     * Dump any outstanding browser logs to the main log.
     *
     * @throws WebDriverLogException
     *             exception containing the first warning/error message, if any
     */
    public void logLogs(boolean throwIfWarn) {
        for (String type : getLogTypes()) {
            logLogs(type, throwIfWarn);
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
        EventFiringWebDriver driver = getDriver();
        if (screenshotListener == null
                && ScreenshotDirForTest.isScreenshotEnabled()) {
            screenshotListener = new TestEventForScreenshotListener(driver);
        }
        driver.register(screenshotListener);
        screenshotListener.updateTestID(testName);
    }

    @ParametersAreNonnullByDefault
    public void registerLogListener() {
        if (logListener == null) {
            logListener = (WebDriverEventListener) newProxyInstance(
                    WebDriverEventListener.class.getClassLoader(),
                    new Class<?>[] { WebDriverEventListener.class },
                    new AbstractInvocationHandler() {

                        @Override
                        protected Object handleInvocation(Object proxy,
                                Method method, Object[] args) throws Throwable {
                            logLogs();
                            return null;
                        }
                    });
        }
        getDriver().register(logListener);
    }

    public void unregisterLogListener() {
        getDriver().unregister(logListener);
    }

    public void unregisterScreenshotListener() {
        log.info("Deregistering screenshot module...");
        getDriver().unregister(screenshotListener);
    }

    public void injectScreenshot(String tag) {
        if (null != screenshotListener) {
            screenshotListener.customEvent(tag);
        }
    }

    private EventFiringWebDriver configureChromeDriver() {
        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY,
                PropertiesHolder.getProperty("webdriver.log"));
        driverService = ChromeDriverService.createDefaultService();
        DesiredCapabilities capabilities = DesiredCapabilities.chrome();
        String chromeBin =
                PropertiesHolder.properties.getProperty("webdriver.chrome.bin");
        log().info("Setting chrome.binary: {}", chromeBin);
        capabilities.setCapability("chrome.binary", chromeBin);
        ChromeOptions options = new ChromeOptions();
        URL url = Thread.currentThread().getContextClassLoader()
                .getResource("zanata-testing-extension/chrome/manifest.json");
        assert url != null : "can\'t find extension (check testResource config in pom.xml)";
        String extensionDir =
                new File(url.getPath()).getParentFile().getAbsolutePath();
        options.addArguments("load-extension=" + extensionDir);
        log().info("Adding Chrome extension: {}", extensionDir);
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        enableLogging(capabilities);
        if (useProxy) {
            // start the proxy
            proxy = new BrowserMobProxyServer();
            proxy.start(0);
            proxy.addFirstHttpFilterFactory(
                    new ResponseFilterAdapter.FilterSource(
                            (response, contents, messageInfo) -> {
                                // TODO fail test if response >= 500?
                                if (response.getStatus().code() >= 400) {
                                    log.warn("Response {} for URI {}",
                                            response.getStatus(),
                                            messageInfo.getOriginalRequest()
                                                    .getUri());
                                } else {
                                    log.info("Response {} for URI {}",
                                            response.getStatus(),
                                            messageInfo.getOriginalRequest()
                                                    .getUri());
                                }
                            }, 0));
            Proxy seleniumProxy = ClientUtil.createSeleniumProxy(proxy);
            capabilities.setCapability(CapabilityType.PROXY, seleniumProxy);
            log().info("Setting proxy: {}", seleniumProxy.getHttpProxy());
        }
        try {
            driverService.start();
        } catch (IOException e) {
            throw new RuntimeException("fail to start chrome driver service");
        }
        return new EventFiringWebDriver(new Augmenter().augment(
                new RemoteWebDriver(driverService.getUrl(), capabilities)));
    }

    private EventFiringWebDriver configureFirefoxDriver() {
        final String pathToFirefox = Strings.emptyToNull(
                PropertiesHolder.properties.getProperty("firefox.path"));
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
        if (!Strings.isNullOrEmpty(
                System.getProperty("webdriver.firefox.profile"))) {
            throw new RuntimeException("webdriver.firefox.profile is ignored");
            // TODO - look at FirefoxDriver.getProfile().
        }
        final FirefoxProfile firefoxProfile = new FirefoxProfile();
        firefoxProfile.setAlwaysLoadNoFocusLib(true);
        firefoxProfile.setEnableNativeEvents(true);
        firefoxProfile.setAcceptUntrustedCertificates(true);
        // TODO port zanata-testing-extension to firefox
        // File file = new File("extension.xpi");
        // firefoxProfile.addExtension(file);
        return firefoxProfile;
    }

    public void testEntry() {
        clearDswid();
    }

    public void testExit() {
        clearDswid();
    }

    private void clearDswid() {
        // clear the browser's memory of the dswid
        getExecutor().executeScript("window.name = \'\'");
        dswidParamChecker.clear();
    }

    public <T> T ignoringDswid(Supplier<T> supplier) {
        dswidParamChecker.stopChecking();
        try {
            return supplier.get();
        } finally {
            dswidParamChecker.startChecking();
        }
    }

    public void ignoringDswid(Runnable r) {
        dswidParamChecker.stopChecking();
        try {
            r.run();
        } finally {
            dswidParamChecker.startChecking();
        }
    }

    public synchronized void killWebDriver() {
        // If webdriver is running, kill it
        if (driver != null) {
            try {
                log.info("Quitting webdriver.");
                driver.quit();
                driver = null;
            } catch (Throwable e) {
                // Ignoring driver tear down errors.
            }
        }
        if (driverService != null && driverService.isRunning()) {
            driverService.stop();
            driverService = null;
        }
        if (proxy != null) {
            proxy.abort();
            proxy = null;
        }
    }

    private class ShutdownHook extends Thread {

        public void run() {
            killWebDriver();
        }
    }
}
