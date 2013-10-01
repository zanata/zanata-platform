package org.zanata.rest.service;

import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.jboss.resteasy.client.ClientResponse;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.DTOUtil;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.impl.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TranslationServiceRestTest extends
        ResourceTranslationServiceRestTest {
    private static final LocaleId FR = new LocaleId("fr");
    private static final LocaleId DE = new LocaleId("de");
    private final Logger log = LoggerFactory
            .getLogger(TranslationServiceRestTest.class);
    private TranslationsResourceTestObjectFactory transTestFactory =
            new TranslationsResourceTestObjectFactory();
    private ResourceTestObjectFactory resourceTestFactory =
            new ResourceTestObjectFactory();

    @DataProvider(name = "TranslationTestData")
    public Object[][] getTestData() {
        // @formatter:off
      return new Object[][]
         {
            new Object[] { "getTestObject", transTestFactory.getTestObject() },
            new Object[] { "getPoTargetHeaderTextFlowTargetTest", transTestFactory.getPoTargetHeaderTextFlowTargetTest() },
            new Object[] { "getTextFlowTargetCommentTest", transTestFactory.getTextFlowTargetCommentTest() },
            new Object[] { "getAllExtension", transTestFactory.getAllExtension() }
         };
      // @formatter:on
    }

    @Override
    protected void prepareResources() {
        MockitoAnnotations.initMocks(this);
        SeamAutowire seamAutowire = getSeamAutowire();
        seamAutowire.use("entityManager", getEm()).use("session", getSession())
                .use("identity", Mockito.mock(ZanataIdentity.class))
                .useImpl(LocaleServiceImpl.class)
                .useImpl(CopyTransServiceImpl.class)
                .useImpl(DocumentServiceImpl.class)
                .useImpl(TranslationServiceImpl.class)
                .useImpl(ValidationServiceImpl.class);

        SourceDocResourceService sourceDocResourceService =
                seamAutowire.autowire(SourceDocResourceService.class);
        TranslatedDocResourceService translatedDocResourceService =
                seamAutowire.autowire(TranslatedDocResourceService.class);

        resources.add(sourceDocResourceService);
        resources.add(translatedDocResourceService);
    }

    @Test
    public void testDeleteTranslation() {
        Resource res = resourceTestFactory.getTextFlowTest();
        sourceDocResource.putResource(res.getName(), res, new StringSet(
                "gettext;comment"));
        TranslationsResource sr = transTestFactory.getTestObject();
        translationResource.putTranslations(res.getName(), DE, sr,
                new StringSet("gettext;comment"));
        ClientResponse<String> resourceGetResponse =
                translationResource.deleteTranslations(res.getName(), DE);
        assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));

        ClientResponse<String> resourceGetResponse2 =
                translationResource.deleteTranslations("test2", FR);
        assertThat(resourceGetResponse2.getResponseStatus(),
                is(Status.NOT_FOUND));
    }

    @Test(dataProvider = "TranslationTestData")
    public void testPutGetTranslation(String desc, TranslationsResource sr) {
        sr = cloneDTO(sr);
        Resource res = resourceTestFactory.getTextFlowTest();
        sourceDocResource.putResource(res.getName(), res, new StringSet(
                "gettext;comment"));
        log.debug("successful put resource:" + res.getName());
        translationResource.putTranslations(res.getName(), DE, sr,
                new StringSet("gettext;comment"));
        TranslationsResource get =
                translationResource.getTranslations(res.getName(), DE,
                        new StringSet("gettext;comment")).getEntity();
        log.debug("expect:" + sr.toString());
        log.debug("actual:" + get.toString());
        ResourceTestUtil.clearRevs(sr);
        ResourceTestUtil.clearRevs(get);
        ResourceTestUtil.clearPoTargetHeaders(sr, get);
        assertThat(sr.toString(), is(get.toString()));
    }

    @Test(dataProvider = "TranslationTestData")
    public void testPutGetTranslationNoExtension(String desc,
            TranslationsResource sr) {
        sr = cloneDTO(sr);
        Resource res = resourceTestFactory.getTextFlowTest();
        sourceDocResource.putResource(res.getName(), res, new StringSet(
                "gettext;comment"));
        log.debug("successful put resource:" + res.getName());
        translationResource.putTranslations(res.getName(), DE, sr, null);
        TranslationsResource get =
                translationResource.getTranslations(res.getName(), DE, null)
                        .getEntity();
        TranslationsResource base = transTestFactory.getTestObject();
        ResourceTestUtil.clearRevs(base);
        ResourceTestUtil.clearRevs(get);
        log.debug("expect:" + base.toString());
        log.debug("actual:" + get.toString());
        ResourceTestUtil.clearPoTargetHeaders(base, get);
        assertThat(base.toString(), is(get.toString()));
    }

    @Test(dataProvider = "TranslationTestData",
            dependsOnMethods = { "testPutGetTranslation" })
    public void testPutNoExtensionGetTranslation(String desc,
            TranslationsResource sr) {
        sr = cloneDTO(sr);
        Resource res = resourceTestFactory.getTextFlowTest();
        sourceDocResource.putResource(res.getName(), res, new StringSet(
                "gettext;comment"));
        log.debug("successful put resource:" + res.getName());
        translationResource.putTranslations(res.getName(), DE, sr, null);
        TranslationsResource get =
                translationResource.getTranslations(res.getName(), DE,
                        new StringSet("gettext;comment")).getEntity();
        TranslationsResource base = transTestFactory.getTestObject();
        ResourceTestUtil.clearRevs(base);
        ResourceTestUtil.clearRevs(get);
        log.debug("expect:" + base.toString());
        log.debug("actual:" + get.toString());
        ResourceTestUtil.clearPoTargetHeaders(base, get);
        assertThat(base.toString(), is(get.toString()));
    }

    @Test(dataProvider = "TranslationTestData")
    public void testPutGetNoExtensionTranslation(String desc,
            TranslationsResource sr) {
        sr = cloneDTO(sr);
        Resource res = resourceTestFactory.getTextFlowTest();
        sourceDocResource.putResource(res.getName(), res, new StringSet(
                "gettext;comment"));
        log.debug("successful put resource:" + res.getName());
        translationResource.putTranslations(res.getName(), DE, sr,
                new StringSet("gettext;comment"));
        TranslationsResource get =
                translationResource.getTranslations(res.getName(), DE, null)
                        .getEntity();
        TranslationsResource base = transTestFactory.getTestObject();
        ResourceTestUtil.clearRevs(base);
        ResourceTestUtil.clearRevs(get);
        log.debug("expect:" + base.toString());
        log.debug("actual:" + get.toString());
        ResourceTestUtil.clearPoTargetHeaders(base, get);
        assertThat(base.toString(), is(get.toString()));
    }

    private <T> T cloneDTO(T dto) {
        try {
            return (T) DTOUtil.toObject(DTOUtil.toXML(dto), dto.getClass());
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }
    }

}
