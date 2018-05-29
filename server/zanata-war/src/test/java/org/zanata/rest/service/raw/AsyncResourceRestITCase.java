package org.zanata.rest.service.raw;

import com.jayway.awaitility.Duration;
import org.dbunit.operation.DatabaseOperation;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.junit.Test;
import org.zanata.common.ContentState;
import org.zanata.rest.ResourceRequestEnvironment;
import org.zanata.common.LocaleId;
import org.zanata.provider.DBUnitProvider.DataSetOperation;
import org.zanata.rest.ResourceRequest;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.AsynchronousProcessResource;
import org.zanata.rest.service.ResourceTestUtil;
import org.zanata.test.TranslationsResourceTestData;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import static com.jayway.awaitility.Awaitility.waitAtMost;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.rest.dto.ProcessStatus.ProcessStatusCode.Failed;
import static org.zanata.test.ResourceTestData.getTestDocWithTextFlow;
import static org.zanata.util.RawRestTestUtils.*;

/**
 * @Auther pahuang
 */
public class AsyncResourceRestITCase extends SourceAndTranslationResourceRestBase {
    private AsynchronousProcessClient asynchronousProcessResource;

    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(AsyncResourceRestITCase.class);

    private static final EnumSet<ProcessStatus.ProcessStatusCode> DONE_STATUS =
            EnumSet.of(Failed,
                    ProcessStatus.ProcessStatusCode.Finished,
                    ProcessStatus.ProcessStatusCode.NotAccepted);

    private StringSet extGettextComment = new StringSet("gettext;comment");

    private static final String DOCUMENTS_DATA_DBUNIT_XML =
            "org/zanata/test/model/DocumentsData.dbunit.xml";
    private static final String TEXT_FLOW_DATA_DB_UNIT_XML =
            "org/zanata/test/model/TextFlowTestData.dbunit.xml";
    @Override
    protected void prepareDBUnitOperations() {
        super.prepareDBUnitOperations();

        addBeforeTestOperation(new DataSetOperation(DOCUMENTS_DATA_DBUNIT_XML,
                DatabaseOperation.CLEAN_INSERT));

        addBeforeTestOperation(new DataSetOperation(TEXT_FLOW_DATA_DB_UNIT_XML,
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    @RunAsClient
    @Deprecated
    public void testPutGetResourceWithExtensionDeprecated() throws Exception {
        final Resource resource = getTestDocWithTextFlow();

        ProcessStatus status = getAsynchronousProcessResource().startSourceDocCreationOrUpdate(resource.getName(), "sample-project", "1.0",
                resource, extGettextComment, true);

        waitUntilFinished(status);

        Response resourceResponse = getSourceDocResource().getResource(resource.getName(), extGettextComment);

        Resource serverResource = getResourceFromResponse(resourceResponse);

        ResourceTestUtil.clearRevs(resource);
        ResourceTestUtil.clearRevs(serverResource);

        log.debug("expect:" + resource.toString());
        log.debug("actual:" + serverResource.toString());

        assertThat(serverResource.toString()).isEqualTo(resource.toString());
     }

    @Test
    @RunAsClient
    public void testPutGetResourceWithExtension() throws Exception {
        final Resource resource = getTestDocWithTextFlow();

        ProcessStatus status = getAsynchronousProcessResource().startSourceDocCreationOrUpdateWithDocId("sample-project", "1.0", resource, extGettextComment, resource.getName());

        waitUntilFinished(status);

        Response resourceResponse = getSourceDocResource().getResourceWithDocId(resource.getName(), extGettextComment);

        Resource serverResource = getResourceFromResponse(resourceResponse);

        ResourceTestUtil.clearRevs(resource);
        ResourceTestUtil.clearRevs(serverResource);

        log.debug("expect:" + resource.toString());
        log.debug("actual:" + serverResource.toString());

        assertThat(serverResource.toString()).isEqualTo(resource.toString());
    }

    @Test
    @RunAsClient
    @Deprecated
    public void testPutGetTranslationWithExtensionDeprecate() {
        final TranslationsResource uploadedResource =
                TranslationsResourceTestData.getTestTextFlowTargetComment();

        ProcessStatus status = getAsynchronousProcessResource().startTranslatedDocCreationOrUpdate("my,path,document.txt", "sample-project", "1.0", LocaleId.EN,
                uploadedResource, extGettextComment, "auto", false);

        waitUntilFinished(status);

        Response getResponse = getTransResource()
                .getTranslationsWithDocId(LocaleId.DE, "my/path/document.txt", null, false, ContentState.Translated.toString(), null);

        assertThat(getResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        TranslationsResource serverResource = getTranslationsResourceFromResponse(getResponse);
        assertThat(serverResource.getTextFlowTargets().size()).isEqualTo(uploadedResource.getTextFlowTargets().size());
    }

    @Test
    @RunAsClient
    public void testPutGetTranslationWithExtension() {
        final TranslationsResource uploadedResource =
                TranslationsResourceTestData.getTestTextFlowTargetComment();

        ProcessStatus status = getAsynchronousProcessResource().startTranslatedDocCreationOrUpdateWithDocId("sample-project", "1.0", LocaleId.DE, uploadedResource,
                "my/path/document.txt", extGettextComment, "auto", false);

        waitUntilFinished(status);

        Response getResponse = getTransResource()
                        .getTranslationsWithDocId(LocaleId.DE, "my/path/document.txt", null, false, ContentState.Translated.toString(), null);

        assertThat(getResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        TranslationsResource serverResource = getTranslationsResourceFromResponse(getResponse);
        assertThat(serverResource.getTextFlowTargets().size()).isEqualTo(uploadedResource.getTextFlowTargets().size());
    }

    private void waitUntilFinished(
            final ProcessStatus status) {
        final AtomicReference<String> processId = new AtomicReference<>(null);

        assertThat(status.getUrl()).isNotNull();
        processId.set(status.getUrl());

        waitAtMost(Duration.TEN_SECONDS).catchUncaughtExceptions()
                .pollInterval(Duration.FIVE_HUNDRED_MILLISECONDS)
                .until(asyncPushFinishCallable(processId.get()));
    }

    private Callable<Boolean> asyncPushFinishCallable(final String processId) {
        return () -> {
            ProcessStatus processStatus = getAsynchronousProcessResource().getProcessStatus(processId);
            ProcessStatus.ProcessStatusCode statusCode = processStatus.getStatusCode();

            return DONE_STATUS.contains(statusCode);
        };
    }

    public AsynchronousProcessClient getAsynchronousProcessResource() {
        if (asynchronousProcessResource == null) {
            asynchronousProcessResource = new AsynchronousProcessClient();
        }
        return asynchronousProcessResource;
    }

    public class AsynchronousProcessClient implements AsynchronousProcessResource {
        @Override
        public ProcessStatus startSourceDocCreation(String idNoSlash, String projectSlug, String iterationSlug, Resource resource, Set<String> extensions, boolean copytrans) {
            throw new UnsupportedOperationException(
                    "Not supported. Use startSourceDocCreationOrUpdate instead.");
        }

        @Override
        public ProcessStatus startSourceDocCreationOrUpdate(String idNoSlash, String projectSlug, String iterationSlug, Resource resource, Set<String> extensions, boolean copytrans) {
            Response response = new PutResourceRequest(
                    getRestEndpointUrl("/async/projects/p/" + projectSlug + "/iterations/i/" + iterationSlug + "/r/" + idNoSlash),
                    "PUT", getAuthorizedEnvironment(), resource) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget
                            .queryParam("ext", extensions.toArray())
                            .queryParam("copyTrans", copytrans)
                            .request(MediaType.APPLICATION_XML_TYPE);
                }
            }.runWithResult();

            return getProcessStatusFromResponse(response);
        }

        @Override
        public ProcessStatus startSourceDocCreationOrUpdateWithDocId(String projectSlug, String iterationSlug, Resource resource, Set<String> extensions, String docId) {
            Response response = new PutResourceRequest(
                    getRestEndpointUrl("/async/projects/p/" + projectSlug + "/iterations/i/" + iterationSlug + "/resource"),
                    "PUT", getAuthorizedEnvironment(), resource) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget
                            .queryParam("docId", docId)
                            .queryParam("ext", extensions.toArray())
                            .request(MediaType.APPLICATION_XML_TYPE);
                }
            }.runWithResult();

            return getProcessStatusFromResponse(response);
        }

        @Override
        public ProcessStatus startTranslatedDocCreationOrUpdate(String idNoSlash, String projectSlug, String iterationSlug, LocaleId locale, TranslationsResource translatedDoc, Set<String> extensions, String merge, boolean assignCreditToUploader) {
            Response response = new PutTranslationsResourceRequest(
                    getRestEndpointUrl("/async/projects/p/" + projectSlug + "/iterations/i/" + iterationSlug + "/r/" + idNoSlash + "/translations/" + locale.toString()),
                    "PUT", getAuthorizedEnvironment(), translatedDoc) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget
                            .queryParam("ext", extensions.toArray())
                            .queryParam("merge", merge)
                            .queryParam("assignCreditToUploader", String.valueOf(assignCreditToUploader))
                            .request(MediaType.APPLICATION_XML_TYPE);
                }
            }.runWithResult();

            return getProcessStatusFromResponse(response);        }

        @Override
        public ProcessStatus startTranslatedDocCreationOrUpdateWithDocId(String projectSlug, String iterationSlug, LocaleId locale, TranslationsResource translatedDoc, String docId, Set<String> extensions, String merge, boolean assignCreditToUploader) {
            Response response = new PutTranslationsResourceRequest(
                    getRestEndpointUrl("/async/projects/p/" + projectSlug + "/iterations/i/" + iterationSlug + "/resource/translations/" + locale.toString()),
                    "PUT", getAuthorizedEnvironment(), translatedDoc) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget
                            .queryParam("docId", docId)
                            .queryParam("ext", extensions.toArray())
                            .queryParam("merge", merge)
                            .queryParam("assignCreditToUploader", String.valueOf(assignCreditToUploader))
                            .request(MediaType.APPLICATION_XML_TYPE);
                }
            }.runWithResult();

            return getProcessStatusFromResponse(response);
        }

        @Override
        public ProcessStatus getProcessStatus(String processId) {
            Response response = new ResourceRequest(
                    getRestEndpointUrl("/async/" + processId),
                    "GET", getAuthorizedEnvironment()) {
                @Override
                protected Invocation.Builder prepareRequest(
                        ResteasyWebTarget webTarget) {
                    return webTarget
                            .request().header(HttpHeaders.ACCEPT,
                                    MediaType.APPLICATION_XML_TYPE);
                }

                @Override
                protected void onResponse(Response response) {
                }
            }.runWithResult();

            return getProcessStatusFromResponse(response);
        }

        private ProcessStatus getProcessStatusFromResponse(Response response) {
            String entityString = response.readEntity(String.class);
            assertJaxbUnmarshal(entityString, ProcessStatus.class);
            ProcessStatus status =
                    jaxbUnmarshal(entityString, ProcessStatus.class);

            return status;
        }
    }

    private abstract class PutResourceRequest extends ResourceRequest {
        private final Resource resource ;

        public PutResourceRequest(String resourceUrl, String method, Resource resource) {
            super(resourceUrl, method);

            this.resource = resource;
        }

        protected PutResourceRequest(String resourceUrl, String method,
                                     ResourceRequestEnvironment environment, Resource resource) {
            super(resourceUrl, method, environment);

            this.resource = resource;
        }
        @Override
        public Response invokeWithResponse(
                Invocation.Builder builder) {
            Entity<String> entity = Entity
                    .entity(jaxbMarhsal(resource), MediaType.APPLICATION_XML_TYPE);

            return builder.buildPut(entity).invoke();
        }

        @Override
        protected void onResponse(Response response) {
            // No processing required when putting a translations resource
        }
    }

    private abstract class PutTranslationsResourceRequest extends ResourceRequest {
        private final TranslationsResource resource ;

        public PutTranslationsResourceRequest(String resourceUrl, String method, TranslationsResource resource) {
            super(resourceUrl, method);

            this.resource = resource;
        }

        protected PutTranslationsResourceRequest(String resourceUrl, String method,
                                     ResourceRequestEnvironment environment, TranslationsResource resource) {
            super(resourceUrl, method, environment);

            this.resource = resource;
        }
        @Override
        public Response invokeWithResponse(
                Invocation.Builder builder) {
            Entity<String> entity = Entity
                    .entity(jaxbMarhsal(resource), MediaType.APPLICATION_XML_TYPE);

            return builder.buildPut(entity).invoke();
        }

        @Override
        protected void onResponse(Response response) {
            // No processing required when putting a translations resource
        }
    }
}
