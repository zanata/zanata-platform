package org.zanata.rest.service.raw;

import com.google.common.collect.Lists;
import org.fedorahosted.tennera.jgettext.HeaderFields;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.*;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.HeaderEntry;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PoTargetHeader;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.*;
import org.zanata.rest.service.ResourceTestUtil;
import org.zanata.rest.service.ResourceUtils;
import org.zanata.util.UrlUtil;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.zanata.util.RawRestTestUtils.jaxbMarhsal;
import static org.zanata.util.RawRestTestUtils.jsonUnmarshal;

public class TranslationResourceRestITCase extends SourceAndTranslationResourceRestBase {
    private static final Logger log = LoggerFactory
            .getLogger(TranslationResourceRestITCase.class);

    private static final LocaleId DE = LocaleId.fromJavaName("de");
    private static final LocaleId FR = LocaleId.fromJavaName("fr");

    private static final String DOC2_NAME = "test.properties";
    private static final String DOC1_NAME = "foo.properties";

    StringSet extGettextComment = new StringSet("gettext;comment");
    StringSet extComment = new StringSet("comment");

    @Test
    @RunAsClient
    public void fetchEmptyListOfResources() {
        doGetandAssertThatResourceListContainsNItems(0);
    }

    @Test
    @RunAsClient
    public void createEmptyResource() {
        Resource sr = createSourceResource("my.txt");

        postResource(sr);
        doGetandAssertThatResourceListContainsNItems(1);
    }

    @Test
    @RunAsClient
    public void createResourceWithContentUsingPost() {
        Resource sr = createSourceResource("my.txt");

        TextFlow stf = new TextFlow("tf1", LocaleId.EN, "tf1");
        sr.getTextFlows().add(stf);

        postResource(sr);
        Resource gotSr = getResource("my.txt");

        assertThat(gotSr.getTextFlows().size()).isEqualTo(1);
        assertThat(gotSr.getTextFlows().get(0).getContents()).isEqualTo(asList("tf1"));
    }

    @Test
    @RunAsClient
    public void createResourceWithContentUsingPut() {
        Resource sr = createSourceResource("my.txt");

        TextFlow stf = new TextFlow("tf1", LocaleId.EN, "tf1");
        sr.getTextFlows().add(stf);

        putResourceWithDocId(sr);

        Resource gotSr = getResource("my.txt");
        assertThat(gotSr.getTextFlows().size()).isEqualTo(1);
        assertThat(gotSr.getTextFlows().get(0).getContents()).isEqualTo(asList("tf1"));

    }

    private void putResourceWithDocId(Resource sr) {
        Response response =
                getSourceDocResource()
                        .putResourceWithDocId(sr, "my.txt", null, false);
        assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
    }

    @Test
    @RunAsClient
    public void createPoResourceWithPoHeader() {
        String docName = "my.txt";
        Resource sr = createSourceResource(docName);

        TextFlow stf = new TextFlow("tf1", LocaleId.EN, "tf1");
        sr.getTextFlows().add(stf);

        // @formatter:off
        /*
        TODO: move this into an AbstractResourceMeta test (PoHeader is valid for source documents, not target)

        PoHeader poHeaderExt = new PoHeader("comment", new HeaderEntry("h1", "v1"), new HeaderEntry("h2", "v2"));
        sr.getExtensions(true).add(poHeaderExt);

        */
        // @formatter:on

        postResource(sr);
        doGetandAssertThatResourceListContainsNItems(1);

        Resource gotSr = getResource(docName);
        assertThat(gotSr.getTextFlows().size()).isEqualTo(1);
        assertThat(gotSr.getTextFlows().get(0).getContents()).isEqualTo(asList("tf1"));

        // @formatter:off
        /*
        TODO: move this into an AbstractResourceMeta test

        assertThat(gotSr.getExtensions().size(), is(1));
        PoHeader gotPoHeader = gotSr.getExtensions().findByType(PoHeader.class);
        assertThat(gotPoHeader, notNullValue());
        assertThat(poHeaderExt.getComment(), is(gotPoHeader.getComment()));
        assertThat(poHeaderExt.getEntries(), is(gotPoHeader.getEntries()));
        */
        // @formatter:on
    }

    private void postResource(Resource sr) {
        Response postResponse =
                getSourceDocResource().post(sr, null, true);
        // new StringSet(PoHeader.ID));
        assertThat(postResponse.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
    }

    // NB this test breaks in Maven if the dev profile is active (because of the
    // imported testdata)
    @Test
    @RunAsClient
    public void publishTranslations() {
        createResourceWithContentUsingPut();

        TranslationsResource entity = getTranslationsResource();

        LocaleId de_DE = new LocaleId("de");
        Response response =
                getTransResource()
                        .putTranslationsWithDocId(de_DE, entity, "my.txt", null,
                                "auto");

        assertResponseStatusEqualOK(response);

        TranslationsResource entity2 = getTranslationsResource(de_DE, "my.txt", ContentState.Translated);
        Response getResponse;
        assertThat(entity2.getTextFlowTargets().size()).isEqualTo(entity
                .getTextFlowTargets().size());

        entity.getTextFlowTargets().clear();
        // push an empty document
        response =
                getTransResource()
                        .putTranslationsWithDocId(de_DE, entity, "my.txt", null,
                                MergeType.IMPORT.toString());
        assertResponseStatusEqualOK(response);

        getResponse =
                getTransResource()
                        .getTranslationsWithDocId(de_DE, "my.txt", null, false, ContentState.Translated.toString(),
                                null);
        assertResponseStatusEqualOK(getResponse);
    }

    private static void assertResponseStatusEqualOK(Response getResponse) {
        assertThat(getResponse.getStatus()).isEqualTo(Status.OK.getStatusCode());
    }

    private void createResourceWithTwoTextFlows() {
        Resource sr = createSourceResource("my.txt");

        TextFlow stf = new TextFlow("tf1", LocaleId.EN, "tf1");
        sr.getTextFlows().add(stf);

        TextFlow stf2 = new TextFlow("tf2", LocaleId.EN, "tf2");
        sr.getTextFlows().add(stf2);

        putResourceWithDocId(sr);
    }

    @Test
    @RunAsClient
    public void publishOnlyOneTranslation() {
        createResourceWithTwoTextFlows();

        TranslationsResource entity = getTranslationsResource();

        LocaleId de_DE = new LocaleId("de");

        Response response =
                getTransResource()
                        .putTranslationsWithDocId(de_DE, entity, "my.txt", null,
                                "auto");

        assertResponseStatusEqualOK(response);

        TranslationsResource entity2 = getTranslationsResource(de_DE, "my.txt", ContentState.Approved);
        assertThat(entity2.getTextFlowTargets().size()).isEqualTo(1);
    }

    @NotNull
    private TranslationsResource getTranslationsResource() {
        TranslationsResource entity = new TranslationsResource();
        TextFlowTarget target = new TextFlowTarget();
        target.setResId("tf1");
        target.setContents("hello world");
        target.setState(ContentState.Translated);
        entity.getTextFlowTargets().add(target);
        return entity;
    }

    @Test
    @RunAsClient
    public void publishTranslationsGetApproved() {
        createResourceWithContentUsingPut();

        TranslationsResource entity = getTranslationsResource();

        LocaleId de_DE = new LocaleId("de");
        Response response =
                getTransResource()
                        .putTranslationsWithDocId(de_DE, entity, "my.txt", null,
                                "auto");

        assertResponseStatusEqualOK(response);

        TranslationsResource entity2 = getTranslationsResource(de_DE, "my.txt", ContentState.Approved);
        Response getResponse;
        assertThat(entity2.getTextFlowTargets().size()).isEqualTo(entity
                .getTextFlowTargets().size());

        entity.getTextFlowTargets().clear();
        // push an empty document
        response =
                getTransResource()
                        .putTranslationsWithDocId(de_DE, entity, "my.txt", null,
                                MergeType.IMPORT.toString());
        assertResponseStatusEqualOK(response);

        getResponse =
                getTransResource()
                        .getTranslationsWithDocId(de_DE, "my.txt", null, false, ContentState.Approved.toString(),
                                null);
        assertResponseStatusEqualOK(getResponse);
    }

    private TranslationsResource getTranslationsResource(LocaleId de_DE, String s, ContentState approved) {
        Response getResponse =
                getTransResource()
                        .getTranslationsWithDocId(de_DE, s, null, false, approved.toString(),
                                null);
        assertResponseStatusEqualOK(getResponse);
        return getTranslationsResourceFromResponse(getResponse);
    }

    @Test
    @RunAsClient
    public void getDocumentThatDoesntExist() {
        Response clientResponse =
                getSourceDocResource()
                        .getResourceWithDocId("my,doc,does,not,exist.txt", null);
        assertThat(clientResponse.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    @RunAsClient
    public void getDocument() throws Exception {
        String docName = "my/path/document.txt";
        Resource resource = createSourceDoc(docName, false);
        getSourceDocResource().putResourceWithDocId(resource, docName, null,
                false);

        Response response =
                getSourceDocResource().getResourceMetaWithDocId(docName, null);
        assertResponseStatusEqualOK(response);
        ResourceMeta doc = getResourceMetaFromResponse(response);
        assertThat(doc.getName()).isEqualTo(docName);
        assertThat(doc.getContentType()).isEqualTo(ContentType.TextPlain);
        assertThat(doc.getLang()).isEqualTo(LocaleId.EN_US);
        assertThat(doc.getRevision()).isEqualTo(1);

        /*
         * Link link = doc.getLinks().findLinkByRel(Relationships.SELF);
         * assertThat( link, notNullValue() ); assertThat(
         * URIUtil.decode(link.getHref().toString()), endsWith(url+docUri) );
         *
         * link =
         * doc.getLinks().findLinkByRel(Relationships.DOCUMENT_CONTAINER);
         * assertThat( link, notNullValue() ); assertThat(
         * link.getHref().toString(), endsWith("iterations/i/1.0") );
         */
    }

    @Test
    @RunAsClient
    public void getDocumentWithResources() throws Exception {
        LocaleId nbLocale = new LocaleId("de");
        String docName = "my/path/document.txt";
        Resource resource = createSourceDoc(docName, true);
        getSourceDocResource().putResourceWithDocId(resource, docName, null,
                false);
        TranslationsResource trans = createTargetDoc();
        getTransResource()
                .putTranslationsWithDocId(nbLocale, trans, docName, null,
                        "auto");

        {
            Resource doc = getResource(docName);
            assertThat(doc.getTextFlows().size()).isEqualTo(1);
        }

        TranslationsResource doc = getTranslationsResource(nbLocale, docName, ContentState.Translated);
        assertThat(doc.getTextFlowTargets().size())
                .as("should have one textFlow")
                .isEqualTo(1);
        TextFlowTarget tft = doc.getTextFlowTargets().get(0);

        assertThat(tft).isNotNull();
        assertThat(tft.getResId())
                .as("should have a textflow with this id")
                .isEqualTo("tf1");

        assertThat(tft)
                .as("expected de target")
                .isNotNull();
        assertThat(tft.getContents())
                .as("expected translation for de")
                .isEqualTo(asList("hei verden"));
    }

    private Resource getResource(String docName) {
        Response response =
                getSourceDocResource().getResourceWithDocId(docName, null);
        assertResponseStatusEqualOK(response);

        return getResourceFromResponse(response);
    }

    @Test
    @RunAsClient
    public void putNewDocument() {
        String docName = "my/fancy/document.txt";
        Resource doc = createSourceDoc(docName, false);
        Response response = getSourceDocResource()
                .putResourceWithDocId(doc, docName, null, false);

        assertThat(response.getStatus()).isEqualTo(Status.CREATED.getStatusCode());
        assertThat(response.getMetadata().getFirst("Location").toString())
                .endsWith(
                        BASE_PATH + "?docId=" + UrlUtil.encodeString(docName));

        Response documentResponse =
                getSourceDocResource().getResourceWithDocId(docName, null);

        assertResponseStatusEqualOK(documentResponse);

        doc = getResourceFromResponse(documentResponse);
        assertThat(doc.getRevision()).isEqualTo(1);

        /*
         * Link link = doc.getLinks().findLinkByRel(Relationships.SELF);
         * assertThat(link, notNullValue());
         * assertThat(link.getHref().toString(), endsWith(url + docUrl));
         *
         * link =
         * doc.getLinks().findLinkByRel(Relationships.DOCUMENT_CONTAINER);
         * assertThat(link, notNullValue()); assertThat(link.getType(),
         * is(MediaTypes.APPLICATION_ZANATA_PROJECT_ITERATION_XML));
         */
    }

    @Test
    @RunAsClient
    public void putDocWithDuplicateTextFlowIds() throws Exception {
        String docName = "testDoc";
        Resource doc = createSourceDoc(docName, false);
        List<TextFlow> textFlows = doc.getTextFlows();

        for (int i = 0; i < 2; i++) {
            TextFlow textFlow = new TextFlow("tf1");
            textFlow.setContents("hello world!");
            textFlows.add(textFlow);
        }
        Response response =
                getSourceDocResource()
                        .putResourceWithDocId(doc, docName, null, false);
        assertThat(response.getStatus()).isEqualTo(Status.BAD_REQUEST.getStatusCode());
        String message = response.readEntity(String.class);
        assertThat(message).contains("tf1");
    }

    @Test
    @RunAsClient
    public void putNewDocumentWithResources() throws Exception {
        String docName = "my/fancy/document.txt";
        Resource doc = createSourceDoc(docName, false);

        List<TextFlow> textFlows = doc.getTextFlows();
        textFlows.clear();

        TextFlow textFlow = new TextFlow("tf1");
        textFlow.setContents("hello world!");
        textFlows.add(textFlow);

        TextFlow tf3 = new TextFlow("tf3");
        tf3.setContents("more text");
        textFlows.add(tf3);

        // Marshaller m = null;
        // JAXBContext jc = JAXBContext.newInstance(Resource.class);
        // m = jc.createMarshaller();
        // m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        // m.marshal(doc, System.out);

        Response response = getSourceDocResource()
                .putResourceWithDocId(doc, docName, null, false);

        assertThat(response.getStatus())
                .isEqualTo(Status.CREATED.getStatusCode());
        assertThat(response.getMetadata().getFirst("Location").toString())
                .endsWith(
                        BASE_PATH + "?docId=" + UrlUtil.encodeString(docName));

        Response documentResponse =
                getSourceDocResource().getResourceWithDocId(docName, null);

        assertResponseStatusEqualOK(documentResponse);

        doc = getResourceFromResponse(documentResponse);

        assertThat(doc.getRevision()).isEqualTo(1);

        assertThat(doc.getTextFlows())
                .as("Should have textFlows")
                .isNotNull();
        assertThat(doc.getTextFlows().size())
                .as("Should have 2 textFlows")
                .isEqualTo(2);
        assertThat(doc.getTextFlows().get(0).getId())
                .as("Should have tf1 textFlow")
                .isEqualTo("tf1");
        assertThat(doc.getTextFlows().get(1).getId())
                .as("Container1 should have tf3 textFlow")
                .isEqualTo(tf3.getId());

        textFlow = doc.getTextFlows().get(0);
        textFlow.setId("tf2");

        response = getSourceDocResource().putResourceWithDocId(doc, docName,
                null, false);

        // this WAS testing for status 205
        assertThat(response.getStatus()).isEqualTo(200);

        documentResponse = getSourceDocResource()
                .getResourceWithDocId(docName, null);
        assertResponseStatusEqualOK(documentResponse);
        doc = getResourceFromResponse(documentResponse);

        assertThat(doc.getRevision()).isEqualTo(2);

        assertThat(doc.getTextFlows())
                .as("Should have textFlows")
                .isNotNull();
        assertThat(doc.getTextFlows().size())
                .as("Should have two textFlows")
                .isEqualTo(2);
        assertThat(doc.getTextFlows().get(0).getId())
                .as("should have same id")
                .isEqualTo("tf2");
    }

    @Test
    @RunAsClient
    public void getZero() throws Exception {
        expectDocs(true, false);
    }

    @Test
    @RunAsClient
    public void put1Get() throws Exception {
        getZero();
        Resource doc1 = putDoc1(false);
        doc1.setRevision(1);
        TextFlow tf1 = doc1.getTextFlows().get(0);
        tf1.setRevision(1);
        TranslationsResource target1 = putTarget1();
        TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
        tft1.setTextFlowRevision(1);
        tft1.setRevision(1);
        expectDocs(true, false, doc1);
        expectTarget1(target1);
    }

    @Test
    @RunAsClient
    public void put1Post2Get() throws Exception {
        getZero();
        Resource doc1 = putDoc1(false);
        doc1.setRevision(1);
        TextFlow tf1 = doc1.getTextFlows().get(0);
        tf1.setRevision(1);
        TranslationsResource target1 = putTarget1();
        TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
        tft1.setTextFlowRevision(1);
        tft1.setRevision(1);
        expectDocs(true, false, doc1);
        expectTarget1(target1);
        Resource doc2 = postDoc2(false);
        doc2.setRevision(1);
        TextFlow tf2 = doc2.getTextFlows().get(0);
        tf2.setRevision(1);
        TranslationsResource target2 = putTarget2();
        TextFlowTarget tft2 = target2.getTextFlowTargets().get(0);
        tft2.setTextFlowRevision(1);
        tft2.setRevision(1);
        expectDocs(true, false, doc1, doc2);
        expectTarget1(target1);
        expectTarget2(target2);
    }

    @Test
    @RunAsClient
    public void put1Post2Put1() throws Exception {
        getZero();
        Resource doc1 = putDoc1(false);
        doc1.setRevision(1);
        TextFlow tf1 = doc1.getTextFlows().get(0);
        tf1.setRevision(1);
        TranslationsResource target1 = putTarget1();
        TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
        tft1.setTextFlowRevision(1);
        Resource doc2 = postDoc2(false);
        doc2.setRevision(1);
        TextFlow tf2 = doc2.getTextFlows().get(0);
        tf2.setRevision(1);
        TranslationsResource target2 = putTarget2();
        TextFlowTarget tft2 = target2.getTextFlowTargets().get(0);
        tft2.setTextFlowRevision(1);
        expectDocs(true, false, doc1, doc2);
        tft1.setRevision(1);
        tft2.setRevision(1);
        expectTarget1(target1);
        expectTarget2(target2);
        // this put should have the effect of deleting doc2
        putDoc1(false);
        deleteDoc2();
        // should be identical to doc1 from before, including revisions
        expectDocs(true, false, doc1);
        expectTarget1(target1);
        dontExpectTarget2();
        // expectTargets(true, FR, target2);
    }

    @Test
    @RunAsClient
    public void put1Delete1Put1() throws Exception {
        getZero();
        Resource doc1 = putDoc1(false);
        doc1.setRevision(1);
        TextFlow tf1 = doc1.getTextFlows().get(0);
        tf1.setRevision(1);
        TranslationsResource target1 = putTarget1();
        TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
        tft1.setTextFlowRevision(1);
        expectDocs(true, false, doc1);
        tft1.setRevision(1);
        expectTarget1(target1);
        // doc1 becomes obsolete
        deleteDoc1();
        getZero();
        dontExpectTarget1();
        // doc1 resurrected, rev 1
        putDoc1(false);
        doc1.setRevision(1);
        tf1.setRevision(1);
        tft1.setTextFlowRevision(1);
        expectDocs(true, false, doc1);
        expectTarget1(target1);
    }

    @Test
    @RunAsClient
    public void put1Put1Again() throws Exception {
        getZero();
        Resource doc1 = putDoc1(false);
        doc1.setRevision(1);
        TextFlow tf1 = doc1.getTextFlows().get(0);
        tf1.setRevision(1);
        TranslationsResource target1 = putTarget1();
        TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
        tft1.setTextFlowRevision(1);
        tft1.setRevision(1);
        expectDocs(true, false, doc1);
        expectTarget1(target1);
        // docRev still 1
        putDoc1(false);
        doc1.setRevision(1);
        tf1.setRevision(1);
        tft1.setTextFlowRevision(1);
        expectDocs(true, false, doc1);
        expectTarget1(target1);
    }

    public void put1Put1WithAnotherTextFlow() {
        // TODO make sure tft1 is still there even though the doc rev goes up
    }

    @Test
    @RunAsClient
    public void put1Delete1Put1a() throws Exception {
        getZero();
        Resource doc1 = putDoc1(false);
        doc1.setRevision(1);
        TextFlow tf1 = doc1.getTextFlows().get(0);
        tf1.setRevision(1);
        TranslationsResource target1 = putTarget1();
        TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
        tft1.setTextFlowRevision(1);
        expectDocs(true, false, doc1);
        tft1.setRevision(1);
        expectTarget1(target1);
        // doc1 becomes obsolete
        deleteDoc1();
        getZero();
        dontExpectTarget1();
        // doc1 resurrected, rev 2
        Resource doc1a = putDoc1a(false);
        doc1a.setRevision(2);
        TextFlow tf1a = doc1a.getTextFlows().get(0);
        tf1a.setRevision(doc1a.getRevision());
        TranslationsResource target1a = putTarget1a();
        TextFlowTarget tft1a = target1a.getTextFlowTargets().get(0);
        tft1a.setTextFlowRevision(tf1a.getRevision());
        tft1a.setRevision(1);
        expectDocs(true, false, doc1a);
        dontExpectTarget1();
        expectTarget1a(target1a);
    }

    @Test
    @RunAsClient
    public void putPoPotGet() throws Exception {
        getZero();
        Resource po1 = putPo1();
        expectDocs(false, false, po1);
        TranslationsResource poTarget1 = putPoTarget1();
        expectTarget(false, po1.getName(), DE, poTarget1);
    }

    @Test
    @RunAsClient
    public void put1Put1aPut1() throws Exception {
        getZero();
        Resource doc1 = putDoc1(false);
        doc1.setRevision(1);
        TextFlow tf1 = doc1.getTextFlows().get(0);
        tf1.setRevision(1);
        TranslationsResource target1 = putTarget1();
        TextFlowTarget tft1 = target1.getTextFlowTargets().get(0);
        tft1.setTextFlowRevision(1);
        tft1.setRevision(1);
        expectDocs(true, false, doc1);
        expectTarget1(target1);
        // this should completely replace doc1's textflow FOOD with HELLO
        Resource doc1a = putDoc1a(false);
        doc1a.setRevision(2);
        TextFlow tf1a = doc1a.getTextFlows().get(0);
        tf1a.setRevision(2);
        TranslationsResource target1a = putTarget1a();
        TextFlowTarget tft1a = target1a.getTextFlowTargets().get(0);
        tft1a.setTextFlowRevision(2);
        tft1a.setRevision(1);
        expectDocs(true, false, doc1a);
        dontExpectTarget1();
        expectTarget1a(target1a);
        // same as original doc1, but different doc rev
        putDoc1(false);
        doc1.setRevision(3);
        expectDocs(true, false, doc1);
        // target 1 should be resurrected
        expectTarget1(target1);
        dontExpectTarget1a();
    }

    @Test
    @RunAsClient
    public void generatedPoHeaders() throws Exception {
        LocaleId de_DE = new LocaleId("de");
        getZero();
        // push some translations (with no headers)
        publishTranslations();
        // Get the translations with PO headers
        Response response =
                getTransResource()
                        .getTranslationsWithDocId(de_DE, "my.txt", new StringSet(
                                "gettext"), true, ContentState.Translated.toString(), null);

        TranslationsResource translations = getTranslationsResourceFromResponse(response);
        assertThat(translations.getExtensions().size()).isGreaterThan(0);

        // List of custom Zanata headers that should be present
        final String[] requiredHeaders =
                new String[] { HeaderFields.KEY_LastTranslator,
                        HeaderFields.KEY_PoRevisionDate,
                        HeaderFields.KEY_LanguageTeam, "X-Generator",
                        HeaderFields.KEY_Language };

        for (String reqHeader : requiredHeaders) {
            boolean headerFound = false;
            for (HeaderEntry entry : translations.getExtensions()
                    .findByType(PoTargetHeader.class).getEntries()) {
                if (entry.getKey().equals(reqHeader)) {
                    headerFound = true;
                }
            }
            assertThat(headerFound)
                    .as("PO Target Header '" + reqHeader
                            + "' was not present when pulling translations.")
                    .isTrue();
        }

        /**
         * Since it is a first push with no headers,
         * the Last Translator should contain ResourceUtils.COPIED_BY_ZANATA_NAME and ResourceUtils.COPIED_BY_ZANATA_NAME_Email
         * Last Revision Date header should not be empty
         */
        for (HeaderEntry entry : translations.getExtensions()
                .findByType(PoTargetHeader.class).getEntries()) {

            String value = entry.getValue().trim();
            if (entry.getKey().equals(HeaderFields.KEY_LastTranslator)) {
                assertThat(value).contains(ResourceUtils.COPIED_BY_ZANATA_NAME,
                        ResourceUtils.COPIED_BY_ZANATA_NAME_EMAIL);
            }

            if (entry.getKey().equals(HeaderFields.KEY_PoRevisionDate)) {
                assertThat(value).isNotBlank();
            }
        }
    }

    @Test
    @RunAsClient
    public void headersBeforeTranslating() throws Exception {
        LocaleId de_DE = new LocaleId("de");
        getZero();

        // Push a document with no translations
        createResourceWithContentUsingPut();

        // Get the translations with PO headers
        Response response =
                getTransResource()
                        .getTranslationsWithDocId(de_DE, "my.txt", new StringSet(
                                "gettext"), true, ContentState.Translated.toString(), null);

        TranslationsResource translations = getTranslationsResourceFromResponse(response);
        // Expecting no translations
        assertThat(translations.getTextFlowTargets().size()).isEqualTo(0);

        // Make sure the headers are populated
        PoTargetHeader header =
                translations.getExtensions(true).findByType(
                        PoTargetHeader.class);
        assertThat(header).isNotNull();
        assertThat(header.getEntries().size()).isGreaterThan(0);

        // Make sure the header values are empty since the system does not have
        // any information for them
        for (HeaderEntry entry : header.getEntries()) {
            if (entry.getKey().equals(HeaderFields.KEY_LastTranslator)) {
                assertThat(entry.getValue().trim()).isEqualTo("");
            } else if (entry.getKey().equals(HeaderFields.KEY_PoRevisionDate)) {
                assertThat(entry.getValue().trim()).isEqualTo("");
            }
        }
    }

    @Test
    @RunAsClient
    public void headersFromOriginalPush() throws Exception {
        LocaleId de_DE = new LocaleId("de");
        getZero();

        // Push a document and its translations
        createResourceWithContentUsingPut();

        TranslationsResource entity = new TranslationsResource();
        TextFlowTarget target = new TextFlowTarget();
        target.setResId("tf1");
        target.setContents("hello world");
        target.setState(ContentState.Approved);
        entity.getTextFlowTargets().add(target);

        // Future Date for the PO Revision Date Header
        Calendar poRevDate = Calendar.getInstance();
        // 1 year in the future
        poRevDate.add(Calendar.YEAR, 1);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mmZ");

        // Add initial headers to the translations
        PoTargetHeader transHeader = new PoTargetHeader();
        transHeader.getEntries().add(
                new HeaderEntry(HeaderFields.KEY_LastTranslator,
                        "Test User <test@zanata.org>"));
        // Date in the future
        transHeader.getEntries().add(
                new HeaderEntry(HeaderFields.KEY_PoRevisionDate, dateFormat
                        .format(poRevDate.getTime())));
        entity.getExtensions(true).add(transHeader);

        // Push the translations
        Response putResponse =
                getTransResource().putTranslationsWithDocId(de_DE, entity,
                        "my.txt", new StringSet("gettext"), "auto");
        assertResponseStatusEqualOK(putResponse);


        // Get the translations with PO headers
        Response transResponse =
                getTransResource()
                        .getTranslationsWithDocId(de_DE, "my.txt", new StringSet(
                                "gettext"), false, ContentState.Translated.toString(), null);
        TranslationsResource translations = getTranslationsResourceFromResponse(transResponse);

        // Make sure the headers are populated
        PoTargetHeader header =
                translations.getExtensions(true).findByType(
                        PoTargetHeader.class);
        assertThat(header).isNotNull();
        assertThat(header.getEntries().size()).isGreaterThan(0);

        // Make sure the header values are the same as the ones pushed with the
        // document
        for (HeaderEntry entry : header.getEntries()) {
            String value = entry.getValue().trim();
            if (entry.getKey().equals(HeaderFields.KEY_LastTranslator)) {
                assertThat(value).contains(ResourceUtils.COPIED_BY_ZANATA_NAME,
                        ResourceUtils.COPIED_BY_ZANATA_NAME_EMAIL);
            } else if (entry.getKey().equals(HeaderFields.KEY_PoRevisionDate)) {
                assertThat(value).isEqualTo(dateFormat.format(poRevDate.getTime()));
            }
        }
    }

    @Test
    @RunAsClient
    public void headersAfterTranslating() throws Exception {
        LocaleId de_DE = new LocaleId("de");
        getZero();

        // Push a document and its translations
        createResourceWithContentUsingPut();

        TranslationsResource entity = new TranslationsResource();
        TextFlowTarget target = new TextFlowTarget();
        target.setResId("tf1");
        target.setContents("hello world");
        target.setState(ContentState.Approved);
        entity.getTextFlowTargets().add(target);

        Response putResponse =
                getTransResource()
                        .putTranslationsWithDocId(de_DE, entity, "my.txt", null,
                                "auto");
        assertResponseStatusEqualOK(putResponse);

        // Get the translations with PO headers
        Response response =
                getTransResource()
                        .getTranslationsWithDocId(de_DE, "my.txt", new StringSet(
                                "gettext"), false, ContentState.Translated.toString(), null);

        TranslationsResource translations = getTranslationsResourceFromResponse(response);
        assertThat(translations.getTextFlowTargets().size()).isGreaterThan(0);

        // Now translate and push them again
        for (TextFlowTarget tft : translations.getTextFlowTargets()) {
            tft.setContents("Translated");
            tft.setState(ContentState.Approved);
        }

        putResponse =
                getTransResource()
                        .putTranslationsWithDocId(de_DE, translations,
                                "my.txt", null, "auto");
        assertResponseStatusEqualOK(putResponse);

        // Fetch the translations again
        response =
                getTransResource()
                        .getTranslationsWithDocId(de_DE, "my.txt", new StringSet(
                                "gettext"), false, ContentState.Translated.toString(), null);

        translations = getTranslationsResourceFromResponse(response);
        assertThat(translations.getTextFlowTargets().size()).isGreaterThan(0);

        // Make sure the headers are now populated
        PoTargetHeader header =
                translations.getExtensions(true).findByType(
                        PoTargetHeader.class);
        assertThat(header).isNotNull();
        assertThat(header.getEntries().size()).isGreaterThan(0);
    }

//    @Test
//    public void getBadProject() throws Exception {
//        ISourceDocResource badSourceResource =
//                getClientRequestFactory().createProxy(ISourceDocResource.class,
//                        createBaseURI(BAD_RESOURCE_PATH));
//        ClientResponse<List<ResourceMeta>> response =
//                badSourceResource.get(null);
//        assertThat(response.getStatus(), is(404));
//    }

    // END of tests

    private void expectDocs(boolean checkRevs, boolean checkLinksIgnored,
            Resource... docs) {
        expectResourceMetas(checkRevs, docs);
        expectResources(checkRevs, docs);
    }

    private void expectResourceMetas(boolean checkRevs,
            AbstractResourceMeta... docs) {
        Response response =
                getSourceDocResource().get(null);

        assertThat(response.getStatus()).isEqualTo(200);
        String entityString = response.readEntity(String.class);
        List<ResourceMeta> actualDocs = Lists.newArrayList(jsonUnmarshal(
                entityString, ResourceMeta[].class));
        assertThat(actualDocs).isNotNull();
        Map<String, AbstractResourceMeta> expectedDocs =
                new HashMap<>();
        for (AbstractResourceMeta doc : docs) {
            expectedDocs.put(doc.getName(), doc);
        }
        // Set<String> actualDocVals = new TreeSet<String>();
        Map<String, AbstractResourceMeta> actualDocsMap =
                new HashMap<>();
        for (ResourceMeta doc : actualDocs) {
            actualDocsMap.put(doc.getName(), doc);
            log.debug("actual doc: " + doc.toString());
            AbstractResourceMeta expectedDoc = expectedDocs.get(doc.getName());
            if (checkRevs)
                assertThat(doc.getRevision()).isEqualTo(expectedDoc.getRevision());
        }
        assertThat(actualDocsMap.keySet()).isEqualTo(expectedDocs.keySet());
    }

    private void expectResources(boolean checkRevs, Resource... docs) {
        for (Resource expectedDoc : docs) {
            Response response =
                    getSourceDocResource().getResourceWithDocId(expectedDoc.getName(),
                            extGettextComment);
            assertThat(response.getStatus()).isEqualTo(200);
            Resource actualDoc = getResourceFromResponse(response);
            if (!checkRevs) {
                ResourceTestUtil.clearRevs(expectedDoc);
                ResourceTestUtil.clearRevs(actualDoc);
            }
            createExtensionSets(expectedDoc);
            createExtensionSets(actualDoc);
            assertThat(actualDoc).isEqualTo(expectedDoc);
        }
    }

    protected void createExtensionSets(Resource resource) {
        resource.getExtensions(true);
        for (TextFlow tf : resource.getTextFlows()) {
            tf.getExtensions(true);
        }
    }

    private void dontExpectTarget(String id, LocaleId locale) {
        Response response =
                getTransResource()
                        .getTranslationsWithDocId(locale, id, null, false, ContentState.Translated.toString(),
                                null);
        assertThat(response.getStatus()).isEqualTo(404);
    }

    private void expectTarget(boolean checkRevs, String id, LocaleId locale,
            TranslationsResource expectedDoc) {
        Response response =
                getTransResource()
                        .getTranslationsWithDocId(locale, id, extGettextComment,
                                false, ContentState.Translated.toString(), null);
        assertThat(response.getStatus()).isEqualTo(200);
        TranslationsResource actualDoc = getTranslationsResourceFromResponse(response);
        actualDoc.getLinks(true).clear();
        actualDoc.getExtensions(true);

        for (TextFlowTarget tft : expectedDoc.getTextFlowTargets()) {
            tft.getExtensions(true);
        }

        for (TextFlowTarget tft : actualDoc.getTextFlowTargets()) {
            tft.getExtensions(true);
        }

        expectedDoc.getLinks(true).clear();
        expectedDoc.getExtensions(true);
        if (!checkRevs) {
            ResourceTestUtil.clearRevs(actualDoc);
            ResourceTestUtil.clearRevs(expectedDoc);
        }

        // Clear Po Headers since Zanata will generate custom ones
        ResourceTestUtil.clearPoTargetHeaders(actualDoc, expectedDoc);

        assertThat(jaxbMarhsal(actualDoc)).isEqualTo(jaxbMarhsal(expectedDoc));
    }

    private Resource createSourceDoc(String name, boolean withTextFlow) {
        Resource resource = new Resource();
        resource.setContentType(ContentType.TextPlain);
        resource.setLang(LocaleId.EN_US);
        resource.setName(name);
        resource.setType(ResourceType.DOCUMENT);

        if (withTextFlow)
            resource.getTextFlows().add(
                    new TextFlow("tf1", LocaleId.EN_US, "hello world"));
        return resource;
    }

    private TranslationsResource createTargetDoc() {
        TranslationsResource trans = new TranslationsResource();
        TextFlowTarget target = new TextFlowTarget();
        target.setContents("hei verden");
        target.setDescription("translation of hello world");
        target.setResId("tf1");
        target.setState(ContentState.Approved);
        trans.getTextFlowTargets().add(target);
        return trans;
    }

    private Resource createSourceResource(String name) {
        Resource sr = new Resource(name);
        sr.setContentType(ContentType.TextPlain);
        sr.setLang(LocaleId.EN);
        sr.setType(ResourceType.FILE);
        return sr;
    }

    private void doGetandAssertThatResourceListContainsNItems(int n) {
        Response resources =
                getSourceDocResource().get(null);
        assertResponseStatusEqualOK(resources);
        String entityString = resources.readEntity(String.class);
        ResourceMeta[] resourceMetas =
                jsonUnmarshal(entityString, ResourceMeta[].class);
        assertThat(resourceMetas.length).isEqualTo(n);
    }

    private Resource newDoc(String id, TextFlow... textFlows) {
        Resource doc = new Resource(id);
        doc.setLang(LocaleId.EN);
        doc.setContentType(ContentType.TextPlain);
        doc.setType(ResourceType.FILE);
        doc.setRevision(null);
        for (TextFlow textFlow : textFlows) {
            doc.getTextFlows().add(textFlow);
        }
        return doc;
    }

    private TextFlow newTextFlow(String id, String sourceContent,
            String sourceComment) {
        TextFlow textFlow = new TextFlow(id, LocaleId.EN);
        textFlow.setContents(sourceContent);
        if (sourceComment != null)
            getOrAddComment(textFlow).setValue(sourceComment);
        return textFlow;
    }

    private TextFlowTarget newTextFlowTarget(String id, String targetContent,
            String targetComment) {
        TextFlowTarget target = new TextFlowTarget();
        target.setResId(id);
        target.setState(ContentState.Approved);
        target.setContents(targetContent);
        if (targetComment != null)
            getOrAddComment(target).setValue(targetComment);
        return target;
    }

    private SimpleComment getOrAddComment(TextFlow tf) {
        return tf.getExtensions(true).findOrAddByType(SimpleComment.class);
    }

    private SimpleComment getOrAddComment(TextFlowTarget tft) {
        return tft.getExtensions(true).findOrAddByType(SimpleComment.class);
    }

    private Resource putPo1() {
        String id = "foo.pot";
        TextFlow textflow = newTextFlow("FOOD", "Slime Mould", "POT comment");
        PotEntryHeader poData =
                textflow.getExtensions(true).findOrAddByType(
                        PotEntryHeader.class);
        poData.setContext("context");
        List<String> flags = poData.getFlags();
        flags.add("no-c-format");
        flags.add("flag2");
        List<String> refs = poData.getReferences();
        refs.add("ref1.xml:7");
        refs.add("ref1.xml:21");

        Resource doc = newDoc(id, textflow);
        PoHeader poHeader = new PoHeader();
        poHeader.setComment("poheader comment");
        List<HeaderEntry> poEntries = poHeader.getEntries();
        poEntries.add(new HeaderEntry("Project-Id-Version", "en"));
        poEntries.add(new HeaderEntry("Content-Type",
                "application/x-publican; charset=UTF-8\n"));
        doc.getExtensions(true).add(poHeader);

        log.debug("{}", doc);
        Response response =
                getSourceDocResource()
                        .putResourceWithDocId(doc, id, extGettextComment, false);
        assertThat(response.getStatus()).isIn(200, 201);
        return doc;
    }

    private TranslationsResource putPoTarget1() {
        String id = "foo.pot";
        TranslationsResource tr = new TranslationsResource();
        TextFlowTarget target =
                newTextFlowTarget("FOOD", "Sauerkraut", "translator comment");
        tr.getTextFlowTargets().add(target);

        PoTargetHeader targetHeader = new PoTargetHeader();
        targetHeader.setComment("target comment");
        List<HeaderEntry> entries = targetHeader.getEntries();
        entries.add(new HeaderEntry("Project-Id-Version", "ja"));
        tr.getExtensions(true).add(targetHeader);

        getTransResource()
                .putTranslationsWithDocId(DE, tr, id, extGettextComment, "auto");

        return tr;
    }

    private Resource putDoc1(boolean putTarget) {
        String id = DOC1_NAME;
        Resource doc =
                newDoc(id,
                        newTextFlow("FOOD", "Slime Mould",
                                "slime mould comment"));
        Response response = getSourceDocResource()
                .putResourceWithDocId(doc, id, extComment, false);
        assertThat(response.getStatus()).isIn(200, 201);

        if (putTarget)
            putTarget1();

        return doc;
    }

    protected TranslationsResource putTarget1() {
        String id = DOC1_NAME;
        TranslationsResource tr = new TranslationsResource();
        TextFlowTarget target = newTextFlowTarget("FOOD", "Sauerkraut", null);
        tr.getTextFlowTargets().add(target);
        getTransResource()
                .putTranslationsWithDocId(DE, tr, id, extGettextComment, "auto");
        return tr;
    }

    private void dontExpectTarget1() {
        String id = DOC1_NAME;
        dontExpectTarget(id, DE);
    }

    private void expectTarget1(TranslationsResource target1) {
        String id = DOC1_NAME;
        expectTarget(true, id, DE, target1);
    }

    private Resource putDoc1a(boolean putTarget) {
        String id = DOC1_NAME;
        Resource doc = newDoc(id, newTextFlow("HELLO", "Hello World", null));
        Response response = getSourceDocResource()
                .putResourceWithDocId(doc, id, extComment, false);
        assertThat(response.getStatus()).isIn(200, 201);

        if (putTarget)
            putTarget1a();

        return doc;
    }

    protected TranslationsResource putTarget1a() {
        String id = DOC1_NAME;
        TranslationsResource tr = new TranslationsResource();
        TextFlowTarget target =
                newTextFlowTarget("HELLO", "Bonjour le Monde",
                        "bon jour comment");
        tr.getTextFlowTargets().add(target);
        getTransResource()
                .putTranslationsWithDocId(FR, tr, id, extGettextComment, "auto");
        return tr;
    }

    private void dontExpectTarget1a() {
        dontExpectTarget(DOC1_NAME, FR);
    }

    private void expectTarget1a(TranslationsResource target1a) {
        expectTarget(true, DOC1_NAME, FR, target1a);
    }

    private void deleteDoc1() {
        deleteDoc(DOC1_NAME);
    }

    private void deleteDoc2() {
        deleteDoc(DOC2_NAME);
    }

    protected void deleteDoc(String id) {
        Response response = getSourceDocResource().deleteResourceWithDocId(id);
        assertThat(response.getStatus()).isIn(200);
    }

    private Resource postDoc2(boolean putTarget) {
        Resource doc =
                newDoc(DOC2_NAME, newTextFlow("HELLO", "Hello World", "hello comment"));
        Response response = getSourceDocResource().post(doc, extComment, true);
        assertThat(response.getStatus()).isIn(201);

        if (putTarget)
            putTarget2();

        return doc;
    }

    protected TranslationsResource putTarget2() {
        TranslationsResource tr = new TranslationsResource();
        TextFlowTarget target =
                newTextFlowTarget("HELLO", "Bonjour le Monde", null);
        tr.getTextFlowTargets().add(target);
        getTransResource()
                .putTranslationsWithDocId(FR, tr, DOC2_NAME, extGettextComment, "auto");
        return tr;
    }

    private void dontExpectTarget2() {
        dontExpectTarget(DOC2_NAME, FR);
    }

    private void expectTarget2(TranslationsResource target2) {
        expectTarget(true, DOC2_NAME, FR, target2);
    }
}
