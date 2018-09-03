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
package org.zanata.page

import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.text.SimpleDateFormat
import java.util.Arrays
import java.util.Date
import java.util.HashMap
import java.util.function.Supplier
import java.util.logging.Level

import net.lightbody.bmp.BrowserMobProxy
import net.lightbody.bmp.BrowserMobProxyServer
import net.lightbody.bmp.client.ClientUtil
import net.lightbody.bmp.filters.ResponseFilterAdapter
import org.codehaus.jackson.map.ObjectMapper
import org.codehaus.jackson.node.ObjectNode
import org.openqa.selenium.JavascriptExecutor
import org.openqa.selenium.chrome.ChromeDriverService
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.firefox.FirefoxBinary
import org.openqa.selenium.firefox.FirefoxDriver
import org.openqa.selenium.firefox.FirefoxOptions
import org.openqa.selenium.firefox.FirefoxProfile
import org.openqa.selenium.logging.LogEntries
import org.openqa.selenium.logging.LogType
import org.openqa.selenium.logging.LoggingPreferences
import org.openqa.selenium.remote.Augmenter
import org.openqa.selenium.remote.CapabilityType
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import org.openqa.selenium.remote.service.DriverService
import org.openqa.selenium.support.events.EventFiringWebDriver
import org.openqa.selenium.support.events.WebDriverEventListener
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.zanata.util.PropertiesHolder
import com.google.common.base.Strings
import org.zanata.util.isScreenshotEnabled
import org.zanata.util.TestEventForScreenshotListener
import javax.annotation.ParametersAreNonnullByDefault
import java.lang.reflect.Proxy.newProxyInstance
import org.zanata.page.utility.shortenPageSource
import org.zanata.util.Constants.webDriverType
import org.zanata.util.Constants.webDriverWait
import org.zanata.util.Constants.zanataInstance

enum class WebDriverFactory {
    INSTANCE;

    private var driver: EventFiringWebDriver? = null
    private var dswidParamChecker: DswidParamChecker? = null
    private var driverService: DriverService? = null
    private var screenshotListener: TestEventForScreenshotListener? = null
    var webDriverWaitTime: Int = 0
    private val ignoredLogPatterns = arrayOf(".*/org.richfaces/jquery.js .* \'webkit(?:Cancel)?RequestAnimationFrame\' is vendor-specific. Please use the standard \'(?:request|cancel)AnimationFrame\' instead.", ".*/org.richfaces/jquery.js .* \'webkitMovement[XY]\' is deprecated. Please use \'movement[XY]\' instead.", "http://example.com/piwik/piwik.js .*")
    private var logListener: WebDriverEventListener? = null
    private var proxy: BrowserMobProxy? = null

    private val executor: JavascriptExecutor
        get() = getDriver()

    /**
     * List the WebDriver log types
     *
     * LogType.CLIENT doesn't seem to log anything LogType.DRIVER,
     * LogType.PERFORMANCE are too verbose LogType PROFILER and LogType.SERVER
     * don't seem to work
     *
     * @return String array of log types
     */
    private val logTypes: Array<String>
        get() = arrayOf(LogType.BROWSER)

    val hostUrl: String
        get() = PropertiesHolder.getProperty(zanataInstance.value())

    @Synchronized
    fun getDriver(): EventFiringWebDriver {
        if (driver == null) {
            driver = createDriver()
        }
        return driver as EventFiringWebDriver
    }

    private fun createDriver(): EventFiringWebDriver {
        val driverType = PropertiesHolder.getProperty(webDriverType.value())
        val newDriver: EventFiringWebDriver
        newDriver = when (driverType.toLowerCase()) {
            "chrome" -> configureChromeDriver()

            "firefox" -> configureFirefoxDriver()

            else -> throw UnsupportedOperationException(
                    "only support chrome and firefox driver")
        }
        Runtime.getRuntime().addShutdownHook(ShutdownHook())
        webDriverWaitTime = Integer.parseInt(
                PropertiesHolder.getProperty(webDriverWait.value()))
        dswidParamChecker = DswidParamChecker(newDriver)
        newDriver.register(dswidParamChecker!!.eventListener)
        return newDriver
    }

    /**
     * Retrieves all the outstanding WebDriver logs of the specified type.
     *
     * @param type
     * a log type from org.openqa.selenium.logging.LogType (but they
     * don't all seem to work)
     */
    private fun getLogs(type: String): LogEntries {
        return getDriver().manage().logs().get(type)
    }

    private fun toString(timestamp: Long, text: String,
                         json: String?): String {
        val time = TIME_FORMAT.get().format(Date(timestamp))
        return time + " " + text + if (json != null) ": $json" else ""
    }

    private fun ignorable(message: String): Boolean {
        for (ignorable in ignoredLogPatterns) {
            if (message.matches(ignorable.toRegex())) {
                return true
            }
        }
        return false
    }

    /**
     * Logs all the outstanding WebDriver logs of the specified type.
     *
     * @param type
     * a log type from org.openqa.selenium.logging.LogType (but they
     * don't all seem to work)
     * @throws WebDriverLogException
     * exception containing the first warning/error message, if any
     */
    private fun logLogs(type: String, throwIfWarn: Boolean) {
        var firstException: WebDriverLogException? = null
        val logName = WebDriverFactory::class.java.name + "." + type
        val log = LoggerFactory.getLogger(logName)
        for (logEntry in getLogs(type)) {
            var level: Level
            val time = logEntry.timestamp
            var text: String
            var json: String?
            val msg = logEntry.message
            if (msg.startsWith("{")) {
                // looks like it might be json
                json = msg
                try {
                    val message = mapper.readValue(json, ObjectNode::class.java)
                            .path("message")
                    val levelString = message.path("level").asText()
                    level = toLogLevel(levelString)
                    text = message.path("text").asText()
                } catch (e: Exception) {
                    log.warn("unable to parse as json: {}", e)
                    level = logEntry.level
                    text = msg
                    json = null
                }

            } else {
                level = logEntry.level
                text = msg
                json = null
            }
            val logString = toString(time, text, json)
            if (level.intValue() >= Level.SEVERE.intValue()) {
                log.error(logString)
                // If firstException was a warning, replace it with this error.
                if ((firstException == null || !firstException.isErrorLog) && !ignorable(msg)) {
                    val pageSource = getDriver().shortenPageSource()
                    // We only throw this if throwIfWarn is true
                    firstException = WebDriverLogException(level, logString,
                            pageSource)
                }
            } else if (level.intValue() >= Level.WARNING.intValue()) {
                log.warn(logString)
                if (firstException == null && !ignorable(msg)) {
                    val pageSource = getDriver().shortenPageSource()
                    // We only throw this if throwIfWarn is true
                    firstException = WebDriverLogException(logEntry.level,
                            logString, pageSource)
                }
            } else if (level.intValue() >= Level.INFO.intValue()) {
                log.info(logString)
            } else {
                log.debug(logString)
            }
        }
        if (throwIfWarn && firstException != null) {
            throw firstException
        }
    }

    private fun toLogLevel(jsLevel: String): Level {
        val upperCase = jsLevel.toUpperCase()
        return if (upperCase == "WARN") {
            Level.WARNING
        } else Level.parse(upperCase)
    }

    /**
     * Dump any outstanding browser logs to the main log.
     *
     * @throws WebDriverLogException
     * exception containing the first warning/error message, if any
     */
    @JvmOverloads
    fun logLogs(throwIfWarn: Boolean = false) {
        // DeltaSpike's LAZY mode uses client-side redirects, which cause
        // other scripts to abort loading in strange ways when dswid is
        // missing/wrong. However, the server code currently preserves dswid
        // whenever possible, and DswidParamChecker aims to make sure it
        // continues to, so we can treat JS warnings/errors as failures.
        for (type in logTypes) {
            logLogs(type, throwIfWarn)
        }
    }

    fun registerScreenshotListener(testName: String) {
        if (!isScreenshotEnabled)
            return
        log.info("Enabling screenshot module...")
        val driver = getDriver()
        if (screenshotListener == null) {
            screenshotListener = TestEventForScreenshotListener(driver)
        }
        driver.register(screenshotListener)
        screenshotListener!!.updateTestID(testName)
    }

    @ParametersAreNonnullByDefault
    fun registerLogListener() {
        if (logListener == null) {
            logListener = newProxyInstance(
                    WebDriverEventListener::class.java.classLoader,
                    arrayOf<Class<*>>(WebDriverEventListener::class.java),
                    object : InvocationHandler {
                        @Throws(Throwable::class)
                        override fun invoke(proxy: Any, method: Method,
                                            args: Array<Any>): Any? {
                            if (args.isEmpty() && method.name == "hashCode") {
                                return hashCode()
                            }
                            if (args.size == 1
                                    && method.name == "equals"
                                    && method.parameterTypes[0] == Any::class.java) {
                                val arg = args[0]
                                return if (proxy === arg) {
                                    true
                                } else isProxyOfSameInterfaces(arg, proxy.javaClass) && equals(java.lang.reflect.Proxy.getInvocationHandler(arg))
                            }
                            if (args.isEmpty() && method.name == "toString") {
                                return toString()
                            }
                            logLogs()
                            return null
                        }
                    }) as WebDriverEventListener
        }
        getDriver().register(logListener)
    }

    fun unregisterLogListener() {
        getDriver().unregister(logListener)
    }

    fun unregisterScreenshotListener() {
        log.info("Deregistering screenshot module...")
        getDriver().unregister(screenshotListener)
    }

    fun injectScreenshot(tag: String) {
        if (null != screenshotListener) {
            screenshotListener!!.customEvent(tag)
        }
    }

    private fun configureChromeDriver(): EventFiringWebDriver {
        System.setProperty(ChromeDriverService.CHROME_DRIVER_LOG_PROPERTY,
                PropertiesHolder.getProperty("webdriver.log"))
        driverService = ChromeDriverService.createDefaultService()
        val capabilities = DesiredCapabilities.chrome()
        val chromeBin = PropertiesHolder.properties.getProperty("webdriver.chrome.bin")
        log().info("Setting chrome.binary: {}", chromeBin)
        capabilities.setCapability("chrome.binary", chromeBin)

        /*
         * Disable popups, thus automatically accepting downloads, and set
         * the default destination to /tmp/
         * See https://developer.chrome.com/extensions/contentSettings#property-popups
         * and
         * https://src.chromium.org/viewvc/chrome/trunk/src/chrome/common/pref_names.cc
         */
        val prefs = HashMap<String, Any>()
        prefs["profile.default_content_settings.popups"] = 0
        prefs["download.default_directory"] = "/tmp/"

        val options = ChromeOptions()
        options.setExperimentalOption("prefs", prefs)

        val url = Thread.currentThread().contextClassLoader
                .getResource("zanata-testing-extension/chrome/manifest.json")
                ?: error("can\'t find extension (check testResource config in pom.xml)")
        val extensionDir = File(url.path).parentFile.absolutePath
        options.addArguments("load-extension=$extensionDir")
        log().info("Adding Chrome extension: {}", extensionDir)
        capabilities.setCapability(ChromeOptions.CAPABILITY, options)
        enableLogging(capabilities)
        @Suppress("ConstantConditionIf")
        if (useProxy) {
            // start the proxy
            proxy = BrowserMobProxyServer()
            proxy!!.start(0)
            proxy!!.addFirstHttpFilterFactory(
                    ResponseFilterAdapter.FilterSource(
                            { response, _, messageInfo ->
                                // TODO fail test if response >= 500?
                                if (response.status.code() >= 400) {
                                    log.warn("Response {} for URI {}",
                                            response.status,
                                            messageInfo.originalRequest
                                                    .uri)
                                } else {
                                    log.info("Response {} for URI {}",
                                            response.status,
                                            messageInfo.originalRequest
                                                    .uri)
                                }
                            }, 0))
            val seleniumProxy = ClientUtil.createSeleniumProxy(proxy!!)
            capabilities.setCapability(CapabilityType.PROXY, seleniumProxy)
            log().info("Setting proxy: {}", seleniumProxy.httpProxy)
        }
        try {
            driverService!!.start()
        } catch (e: IOException) {
            throw RuntimeException("fail to start chrome driver service")
        }

        return EventFiringWebDriver(Augmenter().augment(
                RemoteWebDriver(driverService!!.url, capabilities)))
    }

    private fun configureFirefoxDriver(): EventFiringWebDriver {
        val pathToFirefox = Strings.emptyToNull(
                PropertiesHolder.properties.getProperty("firefox.path"))
        val firefoxBinary: FirefoxBinary
        firefoxBinary = if (pathToFirefox != null) {
            FirefoxBinary(File(pathToFirefox))
        } else {
            FirefoxBinary()
        }
        val options = FirefoxOptions()
                .setBinary(firefoxBinary)
                .setProfile(makeFirefoxProfile())
        val driver = FirefoxDriver(options)
        return EventFiringWebDriver(driver)
    }

    private fun enableLogging(capabilities: DesiredCapabilities) {
        val logs = LoggingPreferences()
        for (type in logTypes) {
            logs.enable(type, Level.INFO)
        }
        capabilities.setCapability(CapabilityType.LOGGING_PREFS, logs)
    }

    private fun makeFirefoxProfile(): FirefoxProfile {
        if (!Strings.isNullOrEmpty(
                        System.getProperty("webdriver.firefox.profile"))) {
            throw RuntimeException("webdriver.firefox.profile is ignored")
            // TODO - look at FirefoxDriver.getProfile().
        }
        val firefoxProfile = FirefoxProfile()
        firefoxProfile.setAlwaysLoadNoFocusLib(true)
        firefoxProfile.setAcceptUntrustedCertificates(true)
        // TODO port zanata-testing-extension to firefox
        // File file = new File("extension.xpi");
        // firefoxProfile.addExtension(file);
        return firefoxProfile
    }

    fun testEntry() {
        clearDswid()
    }

    fun testExit() {
        clearDswid()
    }

    private fun clearDswid() {
        // clear the browser's memory of the dswid
        executor.executeScript("window.name = \'\'")
        if (dswidParamChecker != null) {
            dswidParamChecker!!.clear()
        }
    }

    fun <T> ignoringDswid(supplier: Supplier<T>): T {
        if (dswidParamChecker != null) {
            dswidParamChecker!!.stopChecking()
        }
        try {
            return supplier.get()
        } finally {
            if (dswidParamChecker != null) {
                dswidParamChecker!!.startChecking()
            }
        }
    }

    fun ignoringDswid(r: Runnable) {
        if (dswidParamChecker != null) {
            dswidParamChecker!!.stopChecking()
        }
        try {
            r.run()
        } finally {
            if (dswidParamChecker != null) {
                dswidParamChecker!!.startChecking()
            }
        }
    }

    @Synchronized
    fun killWebDriver() {
        // If webdriver is running, kill it
        if (driver != null) {
            try {
                log.info("Quitting webdriver.")
                driver!!.quit()
                driver = null
            } catch (e: Throwable) {
                // Ignoring driver tear down errors.
            }

        }
        if (driverService != null && driverService!!.isRunning) {
            driverService!!.stop()
            driverService = null
        }
        if (proxy != null) {
            proxy!!.abort()
            proxy = null
        }
    }

    private inner class ShutdownHook : Thread() {

        override fun run() {
            killWebDriver()
        }
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(WebDriverFactory::class.java)

        private val TIME_FORMAT = object : ThreadLocal<SimpleDateFormat>() {

            override fun initialValue(): SimpleDateFormat {
                return SimpleDateFormat("HH:mm:ss.SSS")
            }
        }
        // can reuse, share globally
        private val mapper = ObjectMapper()
        private const val useProxy = true
        // we can't declare this as a field because it is needed during init

        private fun log(): Logger {
            return LoggerFactory.getLogger(WebDriverFactory::class.java)
        }

        private fun isProxyOfSameInterfaces(arg: Any?, proxyClass: Class<*>): Boolean {
            return (proxyClass.isInstance(arg)
                    // Equal proxy instances should mostly be instance of proxyClass
                    // Under some edge cases (such as the proxy of JDK types serialized and then deserialized)
                    // the proxy type may not be the same.
                    // We first check isProxyClass() so that the common case of comparing with non-proxy objects
                    // is efficient.
                    || java.lang.reflect.Proxy.isProxyClass(arg!!.javaClass) && Arrays.equals(arg.javaClass.interfaces, proxyClass.interfaces))
        }
    }
}
