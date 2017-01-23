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
package org.zanata.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.EnumSet;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.client.AsyncProcessClient;
import org.zanata.rest.client.CopyTransClient;
import org.zanata.rest.client.ProjectClient;
import org.zanata.rest.client.ProjectIterationClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.dto.VersionInfo;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jayway.awaitility.Awaitility;
import com.jayway.awaitility.Duration;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ZanataRestCaller {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ZanataRestCaller.class);
    private final RestClientFactory restClientFactory;
    public static final EnumSet<ProcessStatus.ProcessStatusCode> DONE_STATUS =
            EnumSet.of(ProcessStatus.ProcessStatusCode.Failed,
                    ProcessStatus.ProcessStatusCode.Finished,
                    ProcessStatus.ProcessStatusCode.NotAccepted);
    private final String username;
    private final String apiKey;
    private final String baseUrl;

    /**
     * Rest client as user admin.
     */
    public ZanataRestCaller() {
        this("admin",
                PropertiesHolder.getProperty(Constants.zanataApiKey.value()));
    }

    /**
     * Rest client as given user.
     *
     * @param username
     *            username
     * @param apiKey
     *            user api key
     */
    public ZanataRestCaller(String username, String apiKey) {
        this.username = username;
        this.apiKey = apiKey;
        this.baseUrl =
                PropertiesHolder.getProperty(Constants.zanataInstance.value());
        VersionInfo versionInfo = VersionUtility.getAPIVersionInfo();
        try {
            restClientFactory = new RestClientFactory(new URI(baseUrl),
                    username, apiKey, versionInfo, true, false);
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    public void createProjectAndVersion(String projectSlug,
            String iterationSlug, String projectType) {
        ProjectClient projectResource =
                restClientFactory.getProjectClient(projectSlug);
        Project project = new Project();
        project.setDefaultType(projectType);
        project.setId(projectSlug);
        project.setName(projectSlug);
        projectResource.put(project);
        ProjectIteration iteration = new ProjectIteration();
        iteration.setId(iterationSlug);
        ProjectIterationClient projectIteration = restClientFactory
                .getProjectIterationClient(projectSlug, iterationSlug);
        projectIteration.put(iteration);
    }

    public void deleteSourceDoc(String projectSlug, String iterationSlug,
            String resourceName) {
        restClientFactory.getSourceDocResourceClient(projectSlug, iterationSlug)
                .deleteResource(resourceName);
    }

    public int postSourceDocResource(String projectSlug, String iterationSlug,
            Resource resource, boolean copytrans) {
        asyncPushSource(projectSlug, iterationSlug, resource, copytrans);
        return 201;
    }

    public ContainerTranslationStatistics getStatistics(String projectSlug,
            String iterationSlug, String[] locales) {
        return restClientFactory.getStatisticsClient().getStatistics(
                projectSlug, iterationSlug, false, false, locales);
    }

    public void putSourceDocResource(String projectSlug, String iterationSlug,
            String idNoSlash, Resource resource, boolean copytrans) {
        restClientFactory.getSourceDocResourceClient(projectSlug, iterationSlug)
                .putResource(idNoSlash, resource, Collections.emptySet(),
                        copytrans);
    }

    public static Resource buildSourceResource(String name,
            TextFlow... textFlows) {
        Resource resource = new Resource(name);
        resource.setRevision(0);
        resource.getTextFlows().addAll(Lists.newArrayList(textFlows));
        return resource;
    }

    public static TextFlow buildTextFlow(String resId, String... contents) {
        TextFlow textFlow = new TextFlow();
        textFlow.setId(resId);
        textFlow.setLang(LocaleId.EN_US);
        textFlow.setRevision(0);
        textFlow.setContents(contents);
        return textFlow;
    }

    public void postTargetDocResource(String projectSlug, String iterationSlug,
            String idNoSlash, LocaleId localeId,
            TranslationsResource translationsResource, String mergeType) {
        asyncPushTarget(projectSlug, iterationSlug, idNoSlash, localeId,
                translationsResource, mergeType, false);
    }

    public static TranslationsResource
            buildTranslationResource(TextFlowTarget... targets) {
        TranslationsResource resource = new TranslationsResource();
        resource.setRevision(0);
        resource.getTextFlowTargets().addAll(Lists.newArrayList(targets));
        return resource;
    }

    public static TextFlowTarget buildTextFlowTarget(String resId,
            String... contents) {
        TextFlowTarget target = new TextFlowTarget(resId);
        target.setRevision(0);
        target.setTextFlowRevision(0);
        target.setState(ContentState.Approved);
        target.setContents(contents);
        return target;
    }

    public void runCopyTrans(final String projectSlug,
            final String iterationSlug, final String docId) {
        final CopyTransClient copyTransClient =
                restClientFactory.getCopyTransClient();
        copyTransClient.startCopyTrans(projectSlug, iterationSlug, docId);
        log.info("copyTrans started: {}-{}-{}", projectSlug, iterationSlug,
                docId);
        Awaitility.await().pollInterval(Duration.ONE_SECOND)
                .until(() -> !copyTransClient
                        .getCopyTransStatus(projectSlug, iterationSlug, docId)
                        .isInProgress());
        log.info("copyTrans completed: {}-{}-{}", projectSlug, iterationSlug,
                docId);
    }

    public void asyncPushSource(String projectSlug, String iterationSlug,
            Resource sourceResource, boolean copyTrans) {
        AsyncProcessClient asyncProcessClient =
                restClientFactory.getAsyncProcessClient();
        ProcessStatus processStatus =
                asyncProcessClient.startSourceDocCreationOrUpdate(
                        sourceResource.getName(), projectSlug, iterationSlug,
                        sourceResource, Sets.newHashSet(), false);
        processStatus = waitUntilFinished(asyncProcessClient, processStatus);
        log.info("finished async source push ({}-{}): {}", projectSlug,
                iterationSlug, processStatus.getStatusCode());
        if (copyTrans) {
            runCopyTrans(projectSlug, iterationSlug, sourceResource.getName());
        }
    }

    private ProcessStatus waitUntilFinished(
            final AsyncProcessClient asyncProcessClient,
            ProcessStatus processStatus) {
        final String processId = processStatus.getUrl();
        Awaitility.await().pollInterval(Duration.ONE_SECOND)
                .until(() -> DONE_STATUS.contains(asyncProcessClient
                        .getProcessStatus(processId).getStatusCode()));
        if (processStatus.getStatusCode()
                .equals(ProcessStatus.ProcessStatusCode.Failed)) {
            throw new RuntimeException(
                    processStatus.getStatusCode().toString());
        }
        return processStatus;
    }

    public void asyncPushTarget(String projectSlug, String iterationSlug,
            String docId, LocaleId localeId, TranslationsResource transResource,
            String mergeType, boolean assignCreditToUploader) {
        AsyncProcessClient asyncProcessClient =
                restClientFactory.getAsyncProcessClient();
        ProcessStatus processStatus =
                asyncProcessClient.startTranslatedDocCreationOrUpdate(docId,
                        projectSlug, iterationSlug, localeId, transResource,
                        Collections.<String> emptySet(), mergeType,
                        assignCreditToUploader);
        processStatus = waitUntilFinished(asyncProcessClient, processStatus);
        log.info("finished async translation({}-{}) push: {}", projectSlug,
                iterationSlug, processStatus.getStatusCode());
    }

    private Invocation.Builder createRequest(URI uri) {
        Invocation.Builder builder =
                // having null username will bypass
                // ZanataRestSecurityInterceptor
                // clientRequest.header("X-Auth-User", null);
                new ResteasyClientBuilder().build().target(uri).request()
                        .header("X-Auth-Token", apiKey);
        return builder;
    }

    private void postTest(String path, String testClass, String methodName) {
        UriBuilder uri = UriBuilder.fromUri(baseUrl + path);
        uri.queryParam("testClass", testClass);
        uri.queryParam("method", methodName);
        Invocation.Builder request = createRequest(uri.build());
        request.post(Entity.text(""));
    }

    public void signalBeforeTest(String testClass, String methodName) {
        postTest("rest/test/remote/signal/before", testClass, methodName);
    }

    public void signalAfterTest(String testClass, String methodName) {
        postTest("rest/test/remote/signal/after", testClass, methodName);
    }

    public RestClientFactory getRestClientFactory() {
        return this.restClientFactory;
    }
}
