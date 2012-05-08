package org.zanata.rest.service;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.seam.security.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.impl.CopyTransServiceImpl;
import org.zanata.service.impl.DocumentServiceImpl;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.service.impl.TranslationServiceImpl;

import javax.ws.rs.core.Response.Status;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class TranslationServiceRestTest extends ResourceTranslationServiceRestTest
{
   private static final LocaleId FR = new LocaleId("fr");
   private static final LocaleId DE = new LocaleId("de");
   private final Logger log = LoggerFactory.getLogger(TranslationServiceRestTest.class);
   private TranslationsResourceTestObjectFactory transTestFactory = new TranslationsResourceTestObjectFactory();
   private ResourceTestObjectFactory resourceTestFactory = new ResourceTestObjectFactory();
   private IMocksControl mockControl = EasyMock.createControl();
   private Identity mockIdentity = mockControl.createMock(ZanataIdentity.class);
   private SeamAutowire seam = SeamAutowire.instance();

   @DataProvider(name = "TranslationTestData")
   public Object[][] getTestData()
   {
      return new Object[][] {
 new Object[] { transTestFactory.getTestObject() }, new Object[] { transTestFactory.getPoTargetHeaderTextFlowTargetTest() }, new Object[] { transTestFactory.getTextFlowTargetCommentTest() },
      new Object[] { transTestFactory.getAllExtension() }

      };
   }

   @Override
   protected void prepareResources()
   {
      seam.reset();
      seam.ignoreNonResolvable()
            .use("session", getSession())
            .use("identity", mockIdentity)
            .useImpl(LocaleServiceImpl.class)
            .useImpl(CopyTransServiceImpl.class)
            .useImpl(DocumentServiceImpl.class)
            .useImpl(TranslationServiceImpl.class);

      SourceDocResourceService sourceDocResourceService = seam.autowire(SourceDocResourceService.class);
      TranslatedDocResourceService translatedDocResourceService = seam.autowire(TranslatedDocResourceService.class);

      resources.add(sourceDocResourceService);
      resources.add(translatedDocResourceService);
   }

   @Test
   public void testDeleteTranslation()
   {
      Resource res = resourceTestFactory.getTextFlowTest();
      sourceDocResource.putResource(res.getName(), res, new StringSet("gettext;comment"));
      TranslationsResource sr = transTestFactory.getTestObject();
      translationResource.putTranslations(res.getName(), DE, sr, new StringSet("gettext;comment"));
      ClientResponse<String> resourceGetResponse = translationResource.deleteTranslations(res.getName(), DE);
      assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));

      ClientResponse<String> resourceGetResponse2 = translationResource.deleteTranslations("test2", FR);
      assertThat(resourceGetResponse2.getResponseStatus(), is(Status.NOT_FOUND));
   }

   @Test(dataProvider = "TranslationTestData")
   public void testPutGetTranslation(TranslationsResource sr)
   {
      Resource res = resourceTestFactory.getTextFlowTest();
      sourceDocResource.putResource(res.getName(), res, new StringSet("gettext;comment"));
      log.debug("successful put resource:" + res.getName());
      translationResource.putTranslations(res.getName(), DE, sr, new StringSet("gettext;comment"));
      TranslationsResource get = translationResource.getTranslations(res.getName(), DE, new StringSet("gettext;comment")).getEntity();
      log.debug("expect:" + sr.toString());
      log.debug("actual:" + get.toString());
      ResourceTestUtil.clearRevs(sr);
      ResourceTestUtil.clearRevs(get);
      ResourceTestUtil.clearPoTargetHeaders(sr, get);
      assertThat(sr.toString(), is(get.toString()));
   }

   @Test(dataProvider = "TranslationTestData")
   public void testPutGetTranslationNoExtension(TranslationsResource sr)
   {
      Resource res = resourceTestFactory.getTextFlowTest();
      sourceDocResource.putResource(res.getName(), res, new StringSet("gettext;comment"));
      log.debug("successful put resource:" + res.getName());
      translationResource.putTranslations(res.getName(), DE, sr, null);
      TranslationsResource get = translationResource.getTranslations(res.getName(), DE, null).getEntity();
      TranslationsResource base = transTestFactory.getTestObject();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      log.debug("expect:" + base.toString());
      log.debug("actual:" + get.toString());
      ResourceTestUtil.clearPoTargetHeaders(base, get);
      assertThat(base.toString(), is(get.toString()));
   }

   @Test(dataProvider = "TranslationTestData")
   public void testPutNoExtensionGetTranslation(TranslationsResource sr)
   {
      Resource res = resourceTestFactory.getTextFlowTest();
      sourceDocResource.putResource(res.getName(), res, new StringSet("gettext;comment"));
      log.debug("successful put resource:" + res.getName());
      translationResource.putTranslations(res.getName(), DE, sr, null);
      TranslationsResource get = translationResource.getTranslations(res.getName(), DE, new StringSet("gettext;comment")).getEntity();
      TranslationsResource base = transTestFactory.getTestObject();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      log.debug("expect:" + base.toString());
      log.debug("actual:" + get.toString());
      ResourceTestUtil.clearPoTargetHeaders(base, get);
      assertThat(base.toString(), is(get.toString()));
   }

   @Test(dataProvider = "TranslationTestData")
   public void testPutGetNoExtensionTranslation(TranslationsResource sr)
   {
      Resource res = resourceTestFactory.getTextFlowTest();
      sourceDocResource.putResource(res.getName(), res, new StringSet("gettext;comment"));
      log.debug("successful put resource:" + res.getName());
      translationResource.putTranslations(res.getName(), DE, sr, new StringSet("gettext;comment"));
      TranslationsResource get = translationResource.getTranslations(res.getName(), DE, null).getEntity();
      TranslationsResource base = transTestFactory.getTestObject();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      log.debug("expect:" + base.toString());
      log.debug("actual:" + get.toString());
      ResourceTestUtil.clearPoTargetHeaders(base, get);
      assertThat(base.toString(), is(get.toString()));
   }

}
