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
package org.zanata.feature.concurrentedit

import java.util.Collections
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Collectors
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.MediaType

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.junit.BeforeClass
import org.junit.Test
import org.junit.experimental.categories.Category
import org.zanata.feature.Trace
import org.zanata.feature.testharness.TestPlan.DetailedTest
import org.zanata.feature.testharness.ZanataTestCase
import org.zanata.rest.dto.resource.Resource
import org.zanata.util.Constants
import org.zanata.util.PropertiesHolder
import org.zanata.util.ZanataRestCaller

import org.assertj.core.api.Assertions.assertThat
import org.zanata.util.ZanataRestCaller.buildSourceResource
import org.zanata.util.ZanataRestCaller.buildTextFlow

/**
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@Category(DetailedTest::class)
class ConcurrentAccessTest : ZanataTestCase() {

    @Trace(summary = "The system will handle concurrent document " +
            "creation gracefully")
    @Test(timeout = MAX_SHORT_TEST_DURATION.toLong())
    fun concurrentDocumentCreationWillNotCauseHibernateException() {
        val projectSlug = "project"
        val iterationSlug = "master"
        val counter = AtomicInteger(1)
        ZanataRestCaller().createProjectAndVersion(projectSlug,
                iterationSlug, "gettext")

        val threadCount = 5
        val task = Callable {
            val suffix = counter.getAndIncrement()
            ZanataRestCaller().postSourceDocResource(
                    projectSlug, iterationSlug, buildResource(suffix),
                    false)
        }
        val tasks = Collections.nCopies(threadCount, task)
        val executorService = Executors.newFixedThreadPool(threadCount)

        val futures = executorService.invokeAll(tasks)

        val result = getStatusCodes(futures)

        val expectedReturnCode = Collections.nCopies(threadCount, 201)
        assertThat(result).isEqualTo(expectedReturnCode)
    }

    companion object {

        @BeforeClass
        // Need to ensure that the correct concurrent slots are available
        fun beforeClass() {
            val path = "rest/configurations/c/max.concurrent.req.per.apikey"
            val configRequest = clientRequestAsAdmin(path)
            configRequest.put(Entity.json("6"))
        }

        private fun buildResource(suffix: Int): Resource {
            return buildSourceResource("doc$suffix",
                    buildTextFlow("res$suffix", "content$suffix"))
        }

        private fun getStatusCodes(futures: List<Future<Int>>): MutableList<Int> {
            return futures.stream()
                    .map { it -> getResult(it) }
                    .collect(Collectors.toList())
        }

        private fun getResult(input: Future<Int>): Int? {
            try {
                return input.get()
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            } catch (e: ExecutionException) {
                throw RuntimeException(e)
            }

        }

        private fun clientRequestAsAdmin(path: String): Invocation.Builder {
            return ResteasyClientBuilder()
                    .build()
                    .target(PropertiesHolder.getProperty(
                            Constants.zanataInstance.value()) + path)
                    .request(MediaType.APPLICATION_XML_TYPE)
                    .header("X-Auth-User", "admin")
                    .header("X-Auth-Token", PropertiesHolder.getProperty(
                            Constants.zanataApiKey.value()))
        }
    }
}
