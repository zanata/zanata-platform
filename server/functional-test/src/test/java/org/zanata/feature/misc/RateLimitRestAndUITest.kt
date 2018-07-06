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
package org.zanata.feature.misc

import com.google.common.collect.ImmutableList
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.junit.jupiter.api.Test
import org.zanata.feature.Trace
import org.zanata.feature.testharness.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.util.Constants
import org.zanata.util.PropertiesHolder
import org.zanata.util.SampleDataExtension
import org.zanata.util.ZanataRestCaller
import org.zanata.workflow.LoginWorkFlow
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.MediaType
import java.util.Collections
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Disabled

/**
 * @author Patrick Huang
 * [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@Trace(summary = "The system can be set to rate consecutive REST access calls")
@DetailedTest
class RateLimitRestAndUITest : ZanataTestCase() {
    private val maxConcurrentPathParam = "c/max.concurrent.req.per.apikey"
    private val maxActivePathParam = "c/max.active.req.per.apikey"

    @Test
    fun canConfigureRateLimitByWebUI() {
        var serverConfigPage = LoginWorkFlow().signIn("admin", "admin")
                .goToAdministration().goToServerConfigPage()
        assertThat(serverConfigPage.maxConcurrentRequestsPerApiKey)
                .isEqualTo(SampleDataExtension.CONCURRENT_RATE_LIMIT.toString())
        assertThat(serverConfigPage.maxActiveRequestsPerApiKey)
                .isEqualTo(SampleDataExtension.ACTIVE_RATE_LIMIT.toString())
        val administrationPage = serverConfigPage.inputMaxConcurrent(5).inputMaxActive(3).save()
                .goToAdministration()
        // RHBZ1160651
        // assertThat(administrationPage.getNotificationMessage())
        // .isEqualTo("Configuration was successfully updated.");
        serverConfigPage = administrationPage.goToServerConfigPage()
        assertThat(serverConfigPage.maxActiveRequestsPerApiKey)
                .isEqualTo("3")
        assertThat(serverConfigPage.maxConcurrentRequestsPerApiKey)
                .isEqualTo("5")
    }

    @Test
    fun canCallServerConfigurationRestService() {
        val clientRequest = clientRequestAsAdminWithQueryParam(
                "rest/configurations/$maxConcurrentPathParam", "configValue", 1)
        // can put
        clientRequest.put(null)
        // can get single configuration
        val rateLimitConfig = clientRequestAsAdmin(
                "rest/configurations/$maxConcurrentPathParam")
                .get(String::class.java)
        assertThat(rateLimitConfig).contains("max.concurrent.req.per.apikey")
        assertThat(rateLimitConfig).contains("<value>1</value>")
        // can get all configurations
        val configurations = clientRequestAsAdmin("rest/configurations/").get(String::class.java)
        log.info("result {}", configurations)
        assertThat(configurations).isNotNull()
    }

    @Test
    fun serverConfigurationRestServiceOnlyAvailableToAdmin() {
        // all request should be rejected
        val response = clientRequestAsTranslator("rest/configurations/").get()
        assertThat(response.status).isEqualTo(403)
        val response1 = clientRequestAsTranslator(
                "rest/configurations/c/email.admin.addr").get()
        assertThat(response1.status).isEqualTo(403)
        val request = clientRequestAsTranslator(
                "rest/configurations/c/email.admin.addr")
        val response2 = request.put(Entity.json("admin@email.com"))
        assertThat(response2.status).isEqualTo(403)
    }

    @Test
    fun canOnlyDealWithKnownConfiguration() {
        val clientRequest = clientRequestAsAdmin("rest/configurations/c/abc")
        val response = clientRequest.put(Entity.json(""))
        assertThat(response.status).isEqualTo(400)
        val getResponse = clientRequestAsAdmin("rest/configurations/c/abc").get()
        assertThat(getResponse.status).isEqualTo(404)
    }

    @Test
    @Disabled("ZNTA-434")
    fun canLimitConcurrentRestRequestsPerAPIKey() {
        // translator creates the project/version
        val projectSlug = "project"
        val iterationSlug = "version"
        ZanataRestCaller(TRANSLATOR, TRANSLATOR_API)
                .createProjectAndVersion(projectSlug, iterationSlug, "gettext")
        val configRequest = clientRequestAsAdminWithQueryParam(
                "rest/configurations/$maxConcurrentPathParam", "configValue", 2)
        configRequest.put(null).close()
        // prepare to fire multiple REST requests
        val atomicInteger = AtomicInteger(1)
        // requests from translator user
        val translatorThreads = 3
        val translatorTask = Callable {
            invokeRestService(
                    ZanataRestCaller(TRANSLATOR, TRANSLATOR_API),
                    projectSlug, iterationSlug, atomicInteger)
        }
        val translatorTasks = Collections.nCopies(translatorThreads, translatorTask)
        // requests from admin user
        val adminThreads = 2
        val adminTask = Callable {
            invokeRestService(ZanataRestCaller(), projectSlug,
                    iterationSlug, atomicInteger)
        }
        val adminTasks = Collections.nCopies(adminThreads, adminTask)
        val executorService = Executors.newFixedThreadPool(translatorThreads + adminThreads)
        val tasks = ImmutableList.builder<Callable<Int>>()
                .addAll(translatorTasks).addAll(adminTasks).build()
        val futures = executorService.invokeAll(tasks)
        val result = getResultStatusCodes(futures)
        // 1 request from translator should get 429 and fail
        log.info("result: {}", result)
        assertThat(result).contains(201, 201, 201, 201, 429)
    }

    @Test
    fun exceptionWillReleaseSemaphore() {
        // Given: max active is set to 1
        val configRequest = clientRequestAsAdminWithQueryParam(
                "rest/configurations/$maxActivePathParam", "configValue", 1)
        configRequest.put(null).close()
        // When: multiple requests that will result in a mapped exception
        val clientRequest = clientRequestAsAdmin(
                "rest/test/data/sample/dummy?exception=org.zanata.rest.NoSuchEntityException")
        clientRequest.get().close()
        clientRequest.get().close()
        clientRequest.get().close()
        clientRequest.get().close()
        // Then: request that result in exception should still release
        // semaphore. i.e. no permit leak
        assertThat(1).isEqualTo(1)
    }

    @Test
    fun unmappedExceptionWillAlsoReleaseSemaphore() {
        // Given: max active is set to 1
        val configRequest = clientRequestAsAdminWithQueryParam(
                "rest/configurations/$maxActivePathParam", "configValue", 1)
        configRequest.put(null).close()
        // When: multiple requests that will result in an unmapped exception
        val clientRequest = clientRequestAsAdmin(
                "rest/test/data/sample/dummy?exception=java.lang.RuntimeException")
        clientRequest.get().close()
        clientRequest.get().close()
        clientRequest.get().close()
        clientRequest.get().close()
        // Then: request that result in exception should still release
        // semaphore. i.e. no permit leak
        assertThat(1).isEqualTo(1)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(RateLimitRestAndUITest::class.java)

        private val TRANSLATOR = "translator"
        private val TRANSLATOR_API = PropertiesHolder.getProperty(Constants.zanataTranslatorKey.value())

        private fun clientRequestAsAdmin(path: String): Invocation.Builder {
            return ResteasyClientBuilder().build()
                    .target(PropertiesHolder
                            .getProperty(Constants.zanataInstance.value()) + path)
                    .request(MediaType.APPLICATION_XML_TYPE)
                    .header("X-Auth-User", "admin")
                    .header("X-Auth-Token",
                            PropertiesHolder
                                    .getProperty(Constants.zanataApiKey.value()))
                    .header("Content-Type", "application/xml")
        }

        private fun clientRequestAsAdminWithQueryParam(path: String, paramName: String, paramValue: Any): Invocation.Builder {
            return ResteasyClientBuilder().build()
                    .target(PropertiesHolder
                            .getProperty(Constants.zanataInstance.value()) + path)
                    .queryParam(paramName, paramValue)
                    .request(MediaType.APPLICATION_XML_TYPE)
                    .header("X-Auth-User", "admin")
                    .header("X-Auth-Token",
                            PropertiesHolder
                                    .getProperty(Constants.zanataApiKey.value()))
                    .header("Content-Type", "application/xml")
        }

        private fun clientRequestAsTranslator(path: String): Invocation.Builder {
            return ResteasyClientBuilder().build()
                    .target(PropertiesHolder
                            .getProperty(Constants.zanataInstance.value()) + path)
                    .request(MediaType.APPLICATION_XML_TYPE)
                    .header("X-Auth-User", TRANSLATOR)
                    .header("X-Auth-Token", TRANSLATOR_API)
        }

        private fun invokeRestService(restCaller: ZanataRestCaller,
                                      projectSlug: String, iterationSlug: String,
                                      atomicInteger: AtomicInteger): Int {
            val counter = atomicInteger.getAndIncrement()
            return restCaller.postSourceDocResource(projectSlug, iterationSlug,
                    ZanataRestCaller.buildSourceResource("doc$counter",
                            ZanataRestCaller.buildTextFlow("res$counter",
                                    "content$counter")),
                    false)
        }

        private fun getResultStatusCodes(futures: List<Future<Int>>): List<Int> {
            return futures.stream().map<Int> { input ->
                try {
                    input.get()
                } catch (e: Exception) {
                    // by using filter we lose RESTEasy's exception
                    // translation
                    val message = e.message!!.toLowerCase()
                    if (message.matches(".+429.+too many concurrent request.+".toRegex())) {
                        listOf(429)
                    }
                    throw RuntimeException(e)
                }
            }.collect(Collectors.toList())
        }
    }
}
