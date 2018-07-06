package org.zanata.service.impl;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.hibernate.Session;
import org.jglue.cdiunit.AdditionalClasses;
import org.jglue.cdiunit.InRequestScope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.ZanataJpaTest;
import org.zanata.async.handle.MachineTranslationPrefillTaskHandle;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.config.MTServiceToken;
import org.zanata.config.MTServiceURL;
import org.zanata.config.MTServiceUser;
import org.zanata.dao.TextFlowDAO;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;
import org.zanata.rest.dto.MachineTranslationPrefill;
import org.zanata.rest.service.MachineTranslationsManager;
import org.zanata.service.LocaleService;
import org.zanata.service.TranslationService;
import org.zanata.service.VersionStateCache;
import org.zanata.service.mt.TextFlowsToMTDoc;
import org.zanata.servlet.annotations.ContextPath;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.test.CdiUnitRunner;
import org.zanata.transaction.TransactionUtil;
import org.zanata.transaction.TransactionUtilForUnitTest;
import org.zanata.util.UrlUtil;
import org.zanata.webtrans.shared.model.ProjectIterationId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.google.common.base.Joiner;

@RunWith(CdiUnitRunner.class)
@AdditionalClasses({
        TextFlowsToMTDoc.class,
        TextFlowDAO.class,
        TextFlowTargetDAO.class,
        UrlUtil.class })
public class MachineTranslationServiceImplTest extends ZanataJpaTest {
    private static final int NUM_OF_TEXTFLOWS = 110;

    // TODO use http://wiremock.org/docs/junit-rule/
    private WireMockServer wireMockServer;
    @Produces
    @MTServiceURL
    URI mtServiceURL() {
        return URI.create("http://localhost:" + wireMockServer.port());
    }
    @Produces
    @MTServiceUser
    String mtUser = "test";
    @Produces
    @MTServiceToken
    String mtToken = "testkey";
    @Produces
    @ServerPath
    String serverPath = "http://zanata";
    @Produces
    @ContextPath
    String contextPath = "";
    @Produces
    @Named("dswidQuery")
    String dswidQuery = "";
    @Produces
    @Named("dswidParam")
    String dswidParam = "";
    @Produces
    @Mock
    WindowContext windowContext;

    @Produces
    @Mock
    LocaleService localeService;
    private HLocale targetLocale;
    private HLocale sourceLocale;
    private MachineTranslationPrefillTaskHandle taskHandle;

    @Captor
    private ArgumentCaptor<List<TransUnitUpdateRequest>> transUnitUpdateRequestCaptor;

    @Produces
    TransactionUtil transactionUtil() {
        return new TransactionUtilForUnitTest(getEm());
    }

    @Produces
    @Mock
    TranslationService translationService;
    @Produces
    @Mock
    VersionStateCache versionStateCache;

    @Produces
    Session session() {
        return getSession();
    }

    @Produces
    EntityManager entityManager() {
        return getEm();
    }

    @Inject
    private MachineTranslationServiceImpl service;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        sourceLocale = new HLocale(LocaleId.EN_US);
        getEm().persist(sourceLocale);
        targetLocale = new HLocale(LocaleId.DE);
        getEm().persist(targetLocale);
        taskHandle = new MachineTranslationPrefillTaskHandle(
                new MachineTranslationsManager.MachineTranslationsForVersionTaskKey(
                        new ProjectIterationId("project",
                                "master",
                                ProjectType.Gettext)));
        wireMockServer =
                new WireMockServer(wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor("localhost", wireMockServer.port());
    }

    @After
    public void tearDown() {
        wireMockServer.stop();
    }

    @Test
    @InRequestScope
    public void willCompleteIfNoDocumentInProjectVersion()
            throws Exception {
        HProjectIteration version = makeProjectVersion("project", "master");

        Future<Void> future =
                service.prefillProjectVersionWithMachineTranslation(version.getId(),
                        new MachineTranslationPrefill(LocaleId.DE,
                                ContentState.NeedReview),
                        taskHandle);
        // not running via AsyncMethodInterceptor, so we don't need to wait
        future.get(0, SECONDS);
        assertThat(future.isDone()).isTrue();
    }

    private HProjectIteration makeProjectVersion(String projectSlug, String versionSlug) {
        HProject project = new HProject();
        project.setName("test project");
        project.setSlug(projectSlug);
        getEm().persist(project);

        HProjectIteration version = new HProjectIteration();
        version.setSlug(versionSlug);
        project.addIteration(version);
        getEm().persist(version);
        return version;
    }

    @Test
    @InRequestScope
    public void canGetTranslationFromMT()
            throws Exception {
        HProjectIteration version = makeProjectVersion("project", "master");
        // given 3 docs in the version
        int numOfDocs = 3;
        for (int i = 0; i < numOfDocs; i++) {
            HDocument doc =
                    new HDocument("pot/message" + i, ContentType.PO, sourceLocale);
            doc.setProjectIteration(version);
            version.getDocuments().put(doc.getDocId(), doc);
            getEm().persist(doc);
            // each document has the same number of text flows
            for (int j = 0; j < NUM_OF_TEXTFLOWS; j++) {
                HTextFlow textFlow =
                        new HTextFlow(doc, "resId" + j, "content" + j);
                doc.getTextFlows().add(textFlow);
                getEm().persist(textFlow);
            }
        }

        when(localeService.getByLocaleId(targetLocale.getLocaleId())).thenReturn(targetLocale);

        stubForMachineTranslationRequest();

//        ListStubMappingsResult listStubMappingsResult =
//                WireMock.listAllStubMappings();
//        System.out.println(listStubMappingsResult);

        Future<Void> future =
                service.prefillProjectVersionWithMachineTranslation(version.getId(),
                        new MachineTranslationPrefill(LocaleId.DE,
                                ContentState.NeedReview), taskHandle);

        future.get(5, SECONDS);

        // we have 3 docs and each has 110 text flows
        // we translate 100 per batch, thus 2 batches per doc
        // and the total number of batches is 6
        verify(translationService, times(6)).translate(Mockito.eq(targetLocale.getLocaleId()), transUnitUpdateRequestCaptor.capture());
        List<List<TransUnitUpdateRequest>> allRequests =
                transUnitUpdateRequestCaptor.getAllValues();
        List<TransUnitUpdateRequest> flattened =
                allRequests.stream().flatMap(Collection::stream)
                        .collect(Collectors.toList());
        assertThat(flattened).hasSize(NUM_OF_TEXTFLOWS * numOfDocs);
    }

    private void stubForMachineTranslationRequest() {
        // we construct an array of translations
        String[] allTranslations = new String[NUM_OF_TEXTFLOWS];
        Arrays.fill(allTranslations, "{\"value\": \"Hallo Welt\", \"type\": \"text/html\", \"metadata\": null}");
        String joinedContent = Joiner.on(",").join(allTranslations);
        stubFor(post(anyUrl())
//                .withHeader("Accept", equalTo("application/json"))
//                .withHeader("Content-Type", equalTo("application/json"))
                .withHeader("X-Auth-User", equalTo(mtUser))
                .withHeader("X-Auth-Token", equalTo(mtToken))
                .withQueryParam("toLocaleCode", equalTo(targetLocale.getLocaleId().getId()))
                .withRequestBody(containing("url"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{" +
                                "    \"url\": \"http://example.com\"," +
                                "    \"contents\": [" +
                                joinedContent +
                                "    ]," +
                                "    \"localeCode\": \"de\"," +
                                "    \"backendId\": \"GOOGLE\"," +
                                "    \"warnings\": []" +
                                "}")));
    }
}
