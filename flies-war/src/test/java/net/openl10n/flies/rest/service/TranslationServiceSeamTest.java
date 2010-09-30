package net.openl10n.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.Response.Status;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.StringSet;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

@Test(groups = { "seam-tests" })
public class TranslationServiceSeamTest extends ResourceTranslationServiceSeamTest
{
   private static final LocaleId FR = new LocaleId("fr");
   private static final LocaleId DE = new LocaleId("de");
   private final Logger log = LoggerFactory.getLogger(TranslationServiceSeamTest.class);
   private TranslationsResourceTestObjectFactory transTestFactory = new TranslationsResourceTestObjectFactory();
   private ResourceTestObjectFactory resourceTestFactory = new ResourceTestObjectFactory();

   @DataProvider(name = "TranslationTestData")
   public Object[][] getTestData()
   {
      return new Object[][] {
 new Object[] { transTestFactory.getTestObject() }, new Object[] { transTestFactory.getPoTargetHeaderTextFlowTargetTest() }, new Object[] { transTestFactory.getTextFlowTargetCommentTest() },
      new Object[] { transTestFactory.getAllExtension() }

      };
   }

   @Test
   public void testDeleteTranslation()
   {
      Resource res = resourceTestFactory.getTextFlowTest();
      translationResource.putResource(res.getName(), res, new StringSet("gettext;comment"));
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
      translationResource.putResource(res.getName(), res, new StringSet("gettext;comment"));
      log.debug("successful put resource:" + res.getName());
      translationResource.putTranslations(res.getName(), DE, sr, new StringSet("gettext;comment"));
      TranslationsResource get = translationResource.getTranslations(res.getName(), DE, new StringSet("gettext;comment")).getEntity();
      log.debug("expect:" + sr.toString());
      log.debug("actual:" + get.toString());
      assertThat(get.toString(), is(sr.toString()));
   }

   @Test(dataProvider = "TranslationTestData")
   public void testPutGetTranslationNoExtension(TranslationsResource sr)
   {
      Resource res = resourceTestFactory.getTextFlowTest();
      translationResource.putResource(res.getName(), res, new StringSet("gettext;comment"));
      log.debug("successful put resource:" + res.getName());
      translationResource.putTranslations(res.getName(), DE, sr, null);
      TranslationsResource get = translationResource.getTranslations(res.getName(), DE, null).getEntity();
      TranslationsResource base = transTestFactory.getTestObject();
      log.debug("expect:" + base.toString());
      log.debug("actual:" + get.toString());
      assertThat(get.toString(), is(base.toString()));
   }

   @Test(dataProvider = "TranslationTestData")
   public void testPutNoExtensionGetTranslation(TranslationsResource sr)
   {
      Resource res = resourceTestFactory.getTextFlowTest();
      translationResource.putResource(res.getName(), res, new StringSet("gettext;comment"));
      log.debug("successful put resource:" + res.getName());
      translationResource.putTranslations(res.getName(), DE, sr, null);
      TranslationsResource get = translationResource.getTranslations(res.getName(), DE, new StringSet("gettext;comment")).getEntity();
      TranslationsResource base = transTestFactory.getTestObject();
      log.debug("expect:" + base.toString());
      log.debug("actual:" + get.toString());
      assertThat(get.toString(), is(base.toString()));
   }

   @Test(dataProvider = "TranslationTestData")
   public void testPutGetNoExtensionTranslation(TranslationsResource sr)
   {
      Resource res = resourceTestFactory.getTextFlowTest();
      translationResource.putResource(res.getName(), res, new StringSet("gettext;comment"));
      log.debug("successful put resource:" + res.getName());
      translationResource.putTranslations(res.getName(), DE, sr, new StringSet("gettext;comment"));
      TranslationsResource get = translationResource.getTranslations(res.getName(), DE, null).getEntity();
      TranslationsResource base = transTestFactory.getTestObject();
      log.debug("expect:" + base.toString());
      log.debug("actual:" + get.toString());
      assertThat(get.toString(), is(base.toString()));
   }

}
