package org.zanata.util;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import org.jboss.resteasy.client.ClientResponse;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.client.ClientUtility;
import org.zanata.rest.client.ICopyTransResource;
import org.zanata.rest.client.IProjectIterationResource;
import org.zanata.rest.client.IProjectResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.CopyTransStatus;
import org.zanata.rest.dto.Project;
import org.zanata.rest.dto.ProjectIteration;
import org.zanata.rest.dto.VersionInfo;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class ZanataRestCaller {

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
        ClientUtility.checkResult(response);
        response.releaseConnection();

        ProjectIteration iteration = new ProjectIteration();
        iteration.setId(iterationSlug);
        IProjectIterationResource projectIteration =
                zanataProxyFactory.getProjectIteration(projectSlug,
                        iterationSlug);

        ClientResponse iterationResponse = projectIteration.put(iteration);
        ClientUtility.checkResult(iterationResponse);
        iterationResponse.releaseConnection();
    }

    public void deleteSourceDoc(String projectSlug, String iterationSlug,
            String resourceName) {
        ClientResponse<String> response = zanataProxyFactory
                .getSourceDocResource(projectSlug, iterationSlug)
                .deleteResource(resourceName);
        ClientUtility.checkResult(response);
        response.releaseConnection();
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

    public void putSourceDocResource(String projectSlug, String iterationSlug,
            String idNoSlash, Resource resource, boolean copytrans) {
        ClientResponse<String> response =
                zanataProxyFactory.getSourceDocResource(projectSlug,
                        iterationSlug).putResource(idNoSlash, resource,
                        Collections.<String>emptySet(), copytrans);
        ClientUtility.checkResult(response);
        response.releaseConnection();
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
            TranslationsResource translationsResource) {

        ClientResponse<String> response = zanataProxyFactory
                .getTranslatedDocResource(projectSlug, iterationSlug)
                .putTranslations(idNoSlash, localeId,
                        translationsResource,
                        Collections.<String>emptySet(), "auto");
        ClientUtility.checkResult(response);
        response.releaseConnection();
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
        while (!copyTransStatus.isInProgress()) {
            try {
                Thread.sleep(1000);
                log.info("copyTrans completion: {}", copyTransStatus.getPercentageComplete());
                copyTransStatus = resource.getCopyTransStatus(projectSlug, iterationSlug, docId);
            }
            catch (InterruptedException e) {
                throw Throwables.propagate(e);
            }
        }
    }
}
