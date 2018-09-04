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
package org.zanata.util

import java.net.URI
import java.net.URISyntaxException
import java.util.EnumSet
import java.util.concurrent.CompletableFuture
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder
import org.zanata.common.ContentState
import org.zanata.common.LocaleId
import org.zanata.rest.client.AsyncProcessClient
import org.zanata.rest.client.RestClientFactory
import org.zanata.rest.dto.ProcessStatus
import org.zanata.rest.dto.ProcessStatus.ProcessStatusCode
import org.zanata.rest.dto.Project
import org.zanata.rest.dto.ProjectIteration
import org.zanata.rest.dto.resource.Resource
import org.zanata.rest.dto.resource.TextFlow
import org.zanata.rest.dto.resource.TextFlowTarget
import org.zanata.rest.dto.resource.TranslationsResource
import org.zanata.rest.dto.stats.ContainerTranslationStatistics
import com.google.common.collect.Lists
import com.google.common.collect.Sets
import com.jayway.awaitility.Awaitility
import com.jayway.awaitility.Duration
import javax.ws.rs.client.Entity
import javax.ws.rs.client.Invocation
import javax.ws.rs.core.UriBuilder

import org.zanata.rest.dto.ProcessStatus.ProcessStatusCode.Failed

/**
 * @author Patrick Huang
 * [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
class ZanataRestCaller
/**
 * Rest client as given user.
 *
 * @param username
 * username
 * @param apiKey
 * user api key
 */
@JvmOverloads constructor(username: String = "admin", private val apiKey: String = PropertiesHolder.getProperty(Constants.zanataApiKey.value())) {
    private val restClientFactory: RestClientFactory
    private val baseUrl: String = PropertiesHolder.getProperty(Constants.zanataInstance.value())

    init {
        val versionInfo = VersionUtility.getAPIVersionInfo()
        try {
            restClientFactory = RestClientFactory(URI(baseUrl),
                    username, apiKey, versionInfo, true, false)
        } catch (e: URISyntaxException) {
            throw RuntimeException(e)
        }

    }

    fun createProjectAndVersion(projectSlug: String,
                                iterationSlug: String, projectType: String) {
        val projectResource = restClientFactory.getProjectClient(projectSlug)
        val project = Project()
        project.defaultType = projectType
        project.id = projectSlug
        project.name = projectSlug
        projectResource.put(project)
        val iteration = ProjectIteration()
        iteration.id = iterationSlug
        val projectIteration = restClientFactory
                .getProjectIterationClient(projectSlug, iterationSlug)
        projectIteration.put(iteration)
    }

    fun deleteSourceDoc(projectSlug: String, iterationSlug: String,
                        resourceName: String) {
        restClientFactory.getSourceDocResourceClient(projectSlug, iterationSlug)
                .deleteResource(resourceName)
    }

    fun postSourceDocResource(projectSlug: String, iterationSlug: String,
                              resource: Resource, copytrans: Boolean): Int {
        asyncPushSource(projectSlug, iterationSlug, resource, copytrans)
        return 201
    }

    @Suppress("unused")
    fun getStatistics(projectSlug: String,
                      iterationSlug: String, locales: Array<String>): ContainerTranslationStatistics {
        return restClientFactory.statisticsClient.getStatistics(
                projectSlug, iterationSlug, false, false, locales)
    }

    fun putSourceDocResource(projectSlug: String, iterationSlug: String,
                             id: String, resource: Resource, copytrans: Boolean) {
        restClientFactory.getSourceDocResourceClient(projectSlug, iterationSlug)
                .putResource(id, resource, emptySet(),
                        copytrans)
    }

    fun postTargetDocResource(projectSlug: String, iterationSlug: String,
                              docId: String, localeId: LocaleId,
                              translationsResource: TranslationsResource, mergeType: String) {
        asyncPushTarget(projectSlug, iterationSlug, docId, localeId,
                translationsResource, mergeType, false)
    }

    fun runCopyTrans(projectSlug: String,
                     iterationSlug: String, docId: String) {
        val copyTransClient = restClientFactory.copyTransClient
        copyTransClient.startCopyTrans(projectSlug, iterationSlug, docId)
        log.info("copyTrans started: {}-{}-{}", projectSlug, iterationSlug,
                docId)
        Awaitility.await().pollInterval(Duration.ONE_SECOND)
                .until<Any> {
                    !copyTransClient
                            .getCopyTransStatus(projectSlug, iterationSlug, docId)
                            .isInProgress
                }
        log.info("copyTrans completed: {}-{}-{}", projectSlug, iterationSlug,
                docId)
    }

    fun asyncPushSource(projectSlug: String, iterationSlug: String,
                        sourceResource: Resource, copyTrans: Boolean) {
        val asyncProcessClient = restClientFactory.asyncProcessClient
        var processStatus = asyncProcessClient.startSourceDocCreationOrUpdateWithDocId(
                projectSlug, iterationSlug,
                sourceResource, Sets.newHashSet(),
                sourceResource.name)
        processStatus = waitUntilFinished(asyncProcessClient, processStatus.url)
        log.info("finished async source push ({}-{}): {}", projectSlug,
                iterationSlug, processStatus.statusCode)
        if (copyTrans) {
            runCopyTrans(projectSlug, iterationSlug, sourceResource.name)
        }
    }

    private fun waitUntilFinished(
            asyncProcessClient: AsyncProcessClient,
            processId: String): ProcessStatus {
        val future = CompletableFuture<ProcessStatus>()
        Awaitility.await().pollInterval(Duration.ONE_SECOND)
                .until<Any> {
                    val processStatus = asyncProcessClient.getProcessStatus(processId)
                    val statusCode = processStatus.statusCode
                    future.complete(processStatus)
                    DONE_STATUS.contains(statusCode)
                }

        val status = future.getNow(null)
                ?: throw RuntimeException("timeout waiting for process status")
        if (status.statusCode == Failed) {
            val message = ("Async process failed with message " + status.messages
                    + ". Check server log for more details of process '"
                    + processId + "'.")
            throw RuntimeException(message)
        }
        return status
    }

    fun asyncPushTarget(projectSlug: String, iterationSlug: String,
                        docId: String, localeId: LocaleId, transResource: TranslationsResource,
                        mergeType: String, assignCreditToUploader: Boolean) {
        val asyncProcessClient = restClientFactory.asyncProcessClient
        var processStatus = asyncProcessClient.startTranslatedDocCreationOrUpdateWithDocId(
                projectSlug, iterationSlug, localeId, transResource,
                docId, emptySet(), mergeType,
                assignCreditToUploader)
        processStatus = waitUntilFinished(asyncProcessClient, processStatus.url)
        log.info("finished async translation({}-{}) push: {}", projectSlug,
                iterationSlug, processStatus.statusCode)
    }

    private fun createRequest(uri: URI): Invocation.Builder {
        return ResteasyClientBuilder().build().target(uri).request()
                .header("X-Auth-Token", apiKey)
    }

    private fun postTest(path: String, testClass: String, methodName: String) {
        val uri = UriBuilder.fromUri(baseUrl + path)
        uri.queryParam("testClass", testClass)
        uri.queryParam("method", methodName)
        val request = createRequest(uri.build())
        request.post(Entity.text(""))
    }

    fun signalBeforeTest(testClass: String, methodName: String) {
        postTest("rest/test/remote/signal/before", testClass, methodName)
    }

    fun signalAfterTest(testClass: String, methodName: String) {
        postTest("rest/test/remote/signal/after", testClass, methodName)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(ZanataRestCaller::class.java)
        val DONE_STATUS = EnumSet.of(Failed,
                ProcessStatusCode.Finished,
                ProcessStatusCode.NotAccepted)!!

        fun buildSourceResource(name: String,
                                vararg textFlows: TextFlow): Resource {
            val resource = Resource(name)
            resource.revision = 0
            resource.textFlows.addAll(Lists.newArrayList(*textFlows))
            return resource
        }

        fun buildTextFlow(resId: String, vararg contents: String): TextFlow {
            val textFlow = TextFlow()
            textFlow.id = resId
            textFlow.lang = LocaleId.EN_US
            textFlow.revision = 0
            textFlow.setContents(*contents)
            return textFlow
        }

        fun buildTranslationResource(vararg targets: TextFlowTarget): TranslationsResource {
            val resource = TranslationsResource()
            resource.revision = 0
            resource.textFlowTargets.addAll(Lists.newArrayList(*targets))
            return resource
        }

        fun buildTextFlowTarget(resId: String,
                                vararg contents: String): TextFlowTarget {
            val target = TextFlowTarget(resId)
            target.revision = 0
            target.textFlowRevision = 0
            target.state = ContentState.Approved
            target.setContents(*contents)
            return target
        }
    }
}
/**
 * Rest client as user admin.
 */
