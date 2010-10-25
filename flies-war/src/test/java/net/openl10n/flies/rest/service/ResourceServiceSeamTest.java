package net.openl10n.flies.rest.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import javax.ws.rs.core.Response.Status;

import net.openl10n.flies.rest.StringSet;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;

import org.jboss.resteasy.client.ClientResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ResourceServiceSeamTest extends ResourceTranslationServiceSeamTest
{
   private final Logger log = LoggerFactory.getLogger(ResourceServiceSeamTest.class);
   private ResourceTestObjectFactory resourceTestFactory = new ResourceTestObjectFactory();

   @Test(dataProvider = "ResourceTestData")
   public void testPutGetResourceWithExtension(Resource sr)
   {
      log.debug("put resource:" + sr.toString());
      translationResource.putResource(sr.getName(), sr, new StringSet("gettext;comment"));
      Resource get = translationResource.getResource(sr.getName(), new StringSet("gettext;comment")).getEntity();
      ResourceTestUtil.clearRevs(sr);
      ResourceTestUtil.clearRevs(get);
      assertThat(get.toString(), is(sr.toString()));
   }

   @Test(dataProvider = "ResourceTestData")
   public void testPutGetNoExtensionResource(Resource sr)
   {
      log.debug("put resource:" + sr.toString());
      translationResource.putResource(sr.getName(), sr, null);
      Resource get = translationResource.getResource(sr.getName(), new StringSet("gettext;comment")).getEntity();
      Resource base = resourceTestFactory.getTextFlowTest();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      log.debug("expect:" + base.toString());
      log.debug("actual:" + get.toString());
      assertThat(get.toString(), is(base.toString()));
   }

   @Test(dataProvider = "ResourceTestData")
   public void testPutNoExtensionGetResource(Resource sr)
   {
      log.debug("put resource:" + sr.toString());
      translationResource.putResource(sr.getName(), sr, new StringSet("gettext;comment"));
      Resource get = translationResource.getResource(sr.getName(), null).getEntity();
      Resource base = resourceTestFactory.getTextFlowTest();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      log.debug("expect:" + base.toString());
      log.debug("actual:" + get.toString());
      assertThat(get.toString(), is(base.toString()));
   }

   @Test(dataProvider = "ResourceTestData")
   public void testPutGetResource(Resource sr)
   {
      translationResource.putResource(sr.getName(), sr, null);
      Resource base = resourceTestFactory.getTextFlowTest();
      Resource get = translationResource.getResource(sr.getName(), null).getEntity();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      assertThat(get.toString(), is(base.toString()));
   }

   @Test(dataProvider = "ResourceTestData")
   public void testPostGetResource(Resource sr)
   {
      translationResource.post(sr, null);
      Resource base = resourceTestFactory.getTextFlowTest();
      Resource get = translationResource.getResource(sr.getName(), null).getEntity();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      assertThat(get.toString(), is(base.toString()));
   }

    @Test(dataProvider = "ResourceTestData")
   public void testPostGetResourceWithExtension(Resource sr)
   {
      translationResource.post(sr, new StringSet("gettext;comment"));
      Resource get = translationResource.getResource(sr.getName(), new StringSet("gettext;comment")).getEntity();
      ResourceTestUtil.clearRevs(sr);
      ResourceTestUtil.clearRevs(get);
      log.debug("expect:" + sr.toString());
      log.debug("actual:" + get.toString());
      assertThat(get.toString(), is(sr.toString()));
   }

   @Test(dataProvider = "ResourceTestData")
   public void testPostNoExtensionGetResource(Resource sr)
   {
      log.debug("post resource:" + sr.toString());
      translationResource.post(sr, null);
      Resource get = translationResource.getResource(sr.getName(), new StringSet("gettext;comment")).getEntity();
      Resource base = resourceTestFactory.getTextFlowTest();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      log.debug("expect:" + base.toString());
      log.debug("actual:" + get.toString());
      assertThat(get.toString(), is(base.toString()));
   }

   @Test(dataProvider = "ResourceTestData")
   public void testPostGetNoExtensionResource(Resource sr)
   {
      log.debug("post resource:" + sr.toString());
      translationResource.post(sr, new StringSet("gettext;comment"));
      Resource get = translationResource.getResource(sr.getName(), null).getEntity();
      Resource base = resourceTestFactory.getTextFlowTest();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      log.debug("expect:" + base.toString());
      log.debug("actual:" + get.toString());
      assertThat(get.toString(), is(base.toString()));
   }

   @DataProvider(name = "ResourceTestData")
   public Object[][] getResourceTestData()
   {
      return new Object[][] {
 new Object[] { resourceTestFactory.getPotEntryHeaderTest() }, new Object[] { resourceTestFactory.getTextFlowCommentTest() },
      new Object[] { resourceTestFactory.getPoHeaderTest() }
, new Object[] { resourceTestFactory.getTextFlowTest() }

      };
   }

   public void testPutGetResourceMeta()
   {
      log.debug("test put get resource meta service");
      Resource res = resourceTestFactory.getTextFlowTest();
      translationResource.putResource(res.getName(), res, new StringSet("gettext;comment"));
      ResourceMeta sr = resourceTestFactory.getPoHeaderResourceMeta();
      translationResource.putResourceMeta(sr.getName(), sr, new StringSet("gettext;comment"));
      log.debug("get resource meta");
      ClientResponse<ResourceMeta> resourceGetResponse = translationResource.getResourceMeta(sr.getName(), new StringSet("gettext;comment"));
      ResourceMeta get = resourceGetResponse.getEntity();
      ResourceTestUtil.clearRevs(sr);
      ResourceTestUtil.clearRevs(get);
      assertThat(sr.toString(), is(get.toString()));
   }

   public void testPutNoExtensionGetResourceMeta()
   {
      log.debug("test put get resource meta service");
      Resource res = resourceTestFactory.getTextFlowTest();
      translationResource.putResource(res.getName(), res, null);
      ResourceMeta sr = resourceTestFactory.getPoHeaderResourceMeta();
      ResourceMeta base = resourceTestFactory.getResourceMeta();
      translationResource.putResourceMeta(sr.getName(), sr, null);
      log.debug("get resource meta");
      ClientResponse<ResourceMeta> resourceGetResponse = translationResource.getResourceMeta(sr.getName(), new StringSet("gettext;comment"));
      ResourceMeta get = resourceGetResponse.getEntity();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      assertThat(get.toString(), is(base.toString()));
   }

   public void testPutGetNoExtensionResourceMeta()
   {
      log.debug("test put get resource meta service");
      Resource res = resourceTestFactory.getTextFlowTest();
      translationResource.putResource(res.getName(), res, null);
      ResourceMeta sr = resourceTestFactory.getPoHeaderResourceMeta();
      ResourceMeta base = resourceTestFactory.getResourceMeta();
      translationResource.putResourceMeta(sr.getName(), sr, new StringSet("gettext;comment"));
      log.debug("get resource meta");
      ClientResponse<ResourceMeta> resourceGetResponse = translationResource.getResourceMeta(sr.getName(), null);
      ResourceMeta get = resourceGetResponse.getEntity();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      assertThat(get.toString(), is(base.toString()));
   }

   public void testDeleteResource()
   {
      Resource rs1 = resourceTestFactory.getTextFlowTest2();
      translationResource.post(rs1, null);
      ClientResponse<String> resourceGetResponse = translationResource.deleteResource(rs1.getName());
      assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));

      Resource rs2 = resourceTestFactory.getTextFlowTest();
      ClientResponse<String> resourceGetResponse2 = translationResource.deleteResource(rs2.getName());
      assertThat(resourceGetResponse2.getResponseStatus(), is(Status.NOT_FOUND));
   }
}
