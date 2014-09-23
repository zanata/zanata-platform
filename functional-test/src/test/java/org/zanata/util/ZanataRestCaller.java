package org.zanata.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.EnumSet;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.IAsynchronousProcessResource;
import org.zanata.rest.client.ICopyTransResource;
import org.zanata.rest.client.IProjectIterationResource;
import org.zanata.rest.client.IProjectResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.CopyTransStatus;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.dto.VersionInfo;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class ZanataRestCaller {

    @Getter
    private final ZanataProxyFactory zanataProxyFactory;

    /**
     * Rest client as user admin.
     */
    public ZanataRestCaller() {
        this("admin", PropertiesHolder.getProperty(Constants.zanataApiKey
                .value()));
    }

    /**
     * Rest client as given user.
     * @param username username
     * @param apiKey user api key
     */
    public ZanataRestCaller(String username, String apiKey) {
        try {
            VersionInfo versionInfo = VersionUtility.getAPIVersionInfo();
            String baseUrl =
                    PropertiesHolder.getProperty(Constants.zanataInstance
                            .value());
            zanataProxyFactory =
                    new ZanataProxyFactory(new URI(baseUrl), username, apiKey,
                            versionInfo, false, false);
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }

    public void createProjectAndVersion(String projectSlug,
            String iterationSlug, String projectType) {
        IProjectResource projectResource =
                zanataProxyFactory.getProject(projectSlug);
        Project project = new Project();
        project.setDefaultType(projectType);
        project.setId(projectSlug);
        project.setName(projectSlug);

        ClientResponse response = projectResource.put(project);
        checkStatusAndReleaseConnection(response);

        ProjectIteration iteration = new ProjectIteration();
        iteration.setId(iterationSlug);
        IProjectIterationResource projectIteration =
                zanataProxyFactory.getProjectIteration(projectSlug,
                        iterationSlug);

        ClientResponse iterationResponse = projectIteration.put(iteration);
        checkStatusAndReleaseConnection(iterationResponse);
    }

    public void deleteSourceDoc(String projectSlug, String iterationSlug,
            String resourceName) {
        ClientResponse<String> response = zanataProxyFactory
                .getSourceDocResource(projectSlug, iterationSlug)
                .deleteResource(resourceName);
        checkStatusAndReleaseConnection(response);
    }

    public int postSourceDocResource(String projectSlug, String iterationSlug,
            Resource resource, boolean copytrans) {
        ClientResponse<String> response = zanataProxyFactory
                .getSourceDocResource(projectSlug, iterationSlug).post(
                        resource,
                        Collections.<String>emptySet(), copytrans);
        int status = response.getStatus();
        response.releaseConnection();
        return status;
    }

    public ContainerTranslationStatistics getStatistics(String projectSlug, String iterationSlug,
            String[] locales) {
        return zanataProxyFactory.getStatisticsResource()
                .getStatistics(projectSlug, iterationSlug, false, false,
                        locales);
    }

    public void putSourceDocResource(String projectSlug, String iterationSlug,
            String idNoSlash, Resource resource, boolean copytrans) {
        ClientResponse<String> response =
                zanataProxyFactory.getSourceDocResource(projectSlug,
                        iterationSlug).putResource(idNoSlash, resource,
                        Collections.<String>emptySet(), copytrans);
        checkStatusAndReleaseConnection(response);
    }

    public static int checkStatusAndReleaseConnection(
            Response response) {
        ClientResponse<?> clientResponse = (ClientResponse<?>) response;
        ClientUtility.checkResult(clientResponse);
        clientResponse.releaseConnection();
        return response.getStatus();
    }

    public static int getStatusAndReleaseConnection(Response response) {
        ((BaseClientResponse) response).releaseConnection();
        return response.getStatus();
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

        ClientResponse<String> response = zanataProxyFactory
                .getTranslatedDocResource(projectSlug, iterationSlug)
                .putTranslations(idNoSlash, localeId,
                        translationsResource,
                        Collections.<String>emptySet(), mergeType);
        checkStatusAndReleaseConnection(response);
    }

    public static TranslationsResource buildTranslationResource(TextFlowTarget... targets) {
        TranslationsResource resource = new TranslationsResource();
        resource.setRevision(0);
        resource.getTextFlowTargets().addAll(Lists.newArrayList(targets));
        return resource;
    }

    public static TextFlowTarget buildTextFlowTarget(String resId, String... contents) {
        TextFlowTarget target = new TextFlowTarget(resId);
        target.setRevision(0);
        target.setTextFlowRevision(0);
        target.setState(ContentState.Approved);
        target.setContents(contents);
        return target;
    }

    public void runCopyTrans(String projectSlug, String iterationSlug,
            String docId) {
        ICopyTransResource resource =
                zanataProxyFactory.getCopyTransResource();
        CopyTransStatus copyTransStatus =
                resource.startCopyTrans(projectSlug,
                        iterationSlug, docId);
        log.info("copyTrans started: {}-{}-{}", projectSlug, iterationSlug, docId);
        while (copyTransStatus.isInProgress()) {
            try {
                Thread.sleep(1000);
                log.debug("copyTrans completion: {}", copyTransStatus.getPercentageComplete());
                copyTransStatus = resource.getCopyTransStatus(projectSlug, iterationSlug, docId);
            }
            catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }
        log.info("copyTrans completed: {}-{}-{}", projectSlug, iterationSlug, docId);
    }

    public void asyncPushSource(String projectSlug, String iterationSlug,
            Resource sourceResource, boolean copyTrans) {
        IAsynchronousProcessResource resource =
                zanataProxyFactory.getAsynchronousProcessResource();
        ProcessStatus processStatus = resource.startSourceDocCreationOrUpdate(
                sourceResource.getName(), projectSlug, iterationSlug,
                sourceResource,
                Sets.<String>newHashSet(), false);
        processStatus = waitUntilFinished(resource, processStatus);
        log.info("finished async source push ({}-{}): {}", projectSlug,
                iterationSlug, processStatus.getStatusCode());
        if (copyTrans) {
            log.info("start copyTrans for {} - {}", projectSlug, iterationSlug);
            ICopyTransResource copyTransResource =
                    zanataProxyFactory.getCopyTransResource();
            CopyTransStatus copyTransStatus =
                    copyTransResource
                            .startCopyTrans(projectSlug, iterationSlug,
                                    sourceResource.getName());
            while (copyTransStatus.isInProgress()) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    throw Throwables.propagate(e);
                }
                copyTransStatus = copyTransResource.getCopyTransStatus(projectSlug, iterationSlug, sourceResource.getName());
            }
            log.info("finish copyTrans for {} - {}", projectSlug, iterationSlug);
        }
    }

    private ProcessStatus waitUntilFinished(
            IAsynchronousProcessResource resource,
            ProcessStatus processStatus) {
        EnumSet<ProcessStatus.ProcessStatusCode> doneStatus =
                EnumSet.of(ProcessStatus.ProcessStatusCode.Failed,
                        ProcessStatus.ProcessStatusCode.Finished,
                        ProcessStatus.ProcessStatusCode.NotAccepted);
        String processId = processStatus.getUrl();
        while (!doneStatus.contains(processStatus.getStatusCode())) {
            log.debug("{} percent completed {}, messages: {}", processId,
                    processStatus.getPercentageComplete(),
                    processStatus.getMessages());
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
            processStatus = resource.getProcessStatus(processId);
        }
        if (processStatus.getStatusCode().equals(
                ProcessStatus.ProcessStatusCode.Failed)) {
            throw new RuntimeException(processStatus.getStatusCode().toString());
        }
        return processStatus;
    }

    public void asyncPushTarget(String projectSlug, String iterationSlug,
            String docId, LocaleId localeId, TranslationsResource transResource,
            String mergeType) {
        IAsynchronousProcessResource resource =
                zanataProxyFactory.getAsynchronousProcessResource();
        ProcessStatus processStatus =
                resource.startTranslatedDocCreationOrUpdate(docId, projectSlug,
                        iterationSlug, localeId, transResource,
                        Collections.<String>emptySet(), mergeType);
        processStatus = waitUntilFinished(resource, processStatus);
        log.info("finished async translation({}-{}) push: {}", projectSlug,
                iterationSlug, processStatus.getStatusCode());
    }
}
