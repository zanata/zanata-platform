package org.zanata.rest.service;

import org.jboss.resteasy.client.ClientResponse;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.zanata.rest.StringSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.impl.CopyTransServiceImpl;
import org.zanata.service.impl.DocumentServiceImpl;
import org.zanata.service.impl.LocaleServiceImpl;

import javax.ws.rs.core.Response.Status;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ResourceServiceRestTest extends ResourceTranslationServiceRestTest
{
   private final Logger log = LoggerFactory.getLogger(ResourceServiceRestTest.class);
   private ResourceTestObjectFactory resourceTestFactory = new ResourceTestObjectFactory();
   @Mock
   private ZanataIdentity mockIdentity;

   @Override
   protected void prepareResources()
   {
      MockitoAnnotations.initMocks(this);

      SeamAutowire seamAutowire = getSeamAutowire();
      seamAutowire
          .use("session", getSession())
          .use("entityManager", getEm())
          .use("identity", mockIdentity)
          .useImpl(LocaleServiceImpl.class)
          .useImpl(CopyTransServiceImpl.class)
          .useImpl(DocumentServiceImpl.class);

      SourceDocResourceService sourceDocResourceService = seamAutowire.autowire(SourceDocResourceService.class);
      TranslatedDocResourceService translatedDocResourceService = seamAutowire.autowire(TranslatedDocResourceService.class);

      resources.add(sourceDocResourceService);
      resources.add(translatedDocResourceService);
   }

   @Test(dataProvider = "ResourceTestData")
   public void testPutGetResourceWithExtension(Resource sr)
   {
      log.debug("put resource:" + sr.toString());
      sourceDocResource.putResource(sr.getName(), sr, new StringSet("gettext;comment"));
      Resource get = sourceDocResource.getResource(sr.getName(), new StringSet("gettext;comment")).getEntity();
      ResourceTestUtil.clearRevs(sr);
      ResourceTestUtil.clearRevs(get);
      assertThat(get.toString(), is(sr.toString()));
   }

   @Test(dataProvider = "ResourceTestData")
   public void testPutGetNoExtensionResource(Resource sr)
   {
      log.debug("put resource:" + sr.toString());
      sourceDocResource.putResource(sr.getName(), sr, null);
      Resource get = sourceDocResource.getResource(sr.getName(), new StringSet("gettext;comment")).getEntity();
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
      sourceDocResource.putResource(sr.getName(), sr, new StringSet("gettext;comment"));
      Resource get = sourceDocResource.getResource(sr.getName(), null).getEntity();
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
      sourceDocResource.putResource(sr.getName(), sr, null);
      Resource base = resourceTestFactory.getTextFlowTest();
      Resource get = sourceDocResource.getResource(sr.getName(), null).getEntity();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      assertThat(get.toString(), is(base.toString()));
   }

   @Test(dataProvider = "ResourceTestData")
   public void testPostGetResource(Resource sr)
   {
      sourceDocResource.post(sr, null, true);
      Resource base = resourceTestFactory.getTextFlowTest();
      Resource get = sourceDocResource.getResource(sr.getName(), null).getEntity();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      assertThat(get.toString(), is(base.toString()));
   }

    @Test(dataProvider = "ResourceTestData")
   public void testPostGetResourceWithExtension(Resource sr)
   {
      sourceDocResource.post(sr, new StringSet("gettext;comment"), true);
      Resource get = sourceDocResource.getResource(sr.getName(), new StringSet("gettext;comment")).getEntity();
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
      sourceDocResource.post(sr, null, true);
      Resource get = sourceDocResource.getResource(sr.getName(), new StringSet("gettext;comment")).getEntity();
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
      sourceDocResource.post(sr, new StringSet("gettext;comment"), true);
      Resource get = sourceDocResource.getResource(sr.getName(), null).getEntity();
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
      sourceDocResource.putResource(res.getName(), res, new StringSet("gettext;comment"));
      ResourceMeta sr = resourceTestFactory.getPoHeaderResourceMeta();
      sourceDocResource.putResourceMeta(sr.getName(), sr, new StringSet("gettext;comment"));
      log.debug("get resource meta");
      ClientResponse<ResourceMeta> resourceGetResponse = sourceDocResource.getResourceMeta(sr.getName(), new StringSet("gettext;comment"));
      ResourceMeta get = resourceGetResponse.getEntity();
      ResourceTestUtil.clearRevs(sr);
      ResourceTestUtil.clearRevs(get);
      assertThat(sr.toString(), is(get.toString()));
   }

   public void testPutNoExtensionGetResourceMeta()
   {
      log.debug("test put get resource meta service");
      Resource res = resourceTestFactory.getTextFlowTest();
      sourceDocResource.putResource(res.getName(), res, null);
      ResourceMeta sr = resourceTestFactory.getPoHeaderResourceMeta();
      ResourceMeta base = resourceTestFactory.getResourceMeta();
      sourceDocResource.putResourceMeta(sr.getName(), sr, null);
      log.debug("get resource meta");
      ClientResponse<ResourceMeta> resourceGetResponse = sourceDocResource.getResourceMeta(sr.getName(), new StringSet("gettext;comment"));
      ResourceMeta get = resourceGetResponse.getEntity();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      assertThat(get.toString(), is(base.toString()));
   }

   public void testPutGetNoExtensionResourceMeta()
   {
      log.debug("test put get resource meta service");
      Resource res = resourceTestFactory.getTextFlowTest();
      sourceDocResource.putResource(res.getName(), res, null);
      ResourceMeta sr = resourceTestFactory.getPoHeaderResourceMeta();
      ResourceMeta base = resourceTestFactory.getResourceMeta();
      sourceDocResource.putResourceMeta(sr.getName(), sr, new StringSet("gettext;comment"));
      log.debug("get resource meta");
      ClientResponse<ResourceMeta> resourceGetResponse = sourceDocResource.getResourceMeta(sr.getName(), null);
      ResourceMeta get = resourceGetResponse.getEntity();
      ResourceTestUtil.clearRevs(base);
      ResourceTestUtil.clearRevs(get);
      assertThat(get.toString(), is(base.toString()));
   }

   public void testDeleteResource()
   {
      Resource rs1 = resourceTestFactory.getTextFlowTest2();
      sourceDocResource.post(rs1, null, true);
      ClientResponse<String> resourceGetResponse = sourceDocResource.deleteResource(rs1.getName());
      assertThat(resourceGetResponse.getResponseStatus(), is(Status.OK));

      Resource rs2 = resourceTestFactory.getTextFlowTest();
      ClientResponse<String> resourceGetResponse2 = sourceDocResource.deleteResource(rs2.getName());
      assertThat(resourceGetResponse2.getResponseStatus(), is(Status.NOT_FOUND));
   }
}
