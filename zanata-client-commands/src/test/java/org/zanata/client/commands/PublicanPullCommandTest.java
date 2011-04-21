package org.zanata.client.commands;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Response.Status;


import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.commands.PublicanPullCommand;
import org.zanata.client.commands.PublicanPullOptions;
import org.zanata.client.commands.PublicanPullOptionsImpl;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.RestUtil;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.client.ITranslationResources;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;

@Test(groups = "unit-tests")
public class PublicanPullCommandTest
{
   IMocksControl control = EasyMock.createControl();
   @SuppressWarnings("rawtypes")
   private Collection mocks = new ArrayList();
   ITranslationResources mockTranslationResources = createMock(ITranslationResources.class);

   public PublicanPullCommandTest() throws Exception
   {
   }

   // keeps breaking the build because of expected, uncalled finalize
   // methods(?!)
   @Test(enabled = false)
   public void publicanPullPo() throws Exception
   {
      publicanPush(false, false);
   }

   // keeps breaking the build because of expected, uncalled finalize
   // methods(?!)
   @Test(enabled = false)
   public void publicanPullPotAndPo() throws Exception
   {
      publicanPush(true, false);
   }

   @Test
   public void publicanPullPotAndPoWithLocaleMapping() throws Exception
   {
      publicanPush(true, true);
   }

   @BeforeMethod
   void beforeMethod()
   {
      control.reset();
   }

   @AfterMethod
   void afterMethod()
   {
      mocks.clear();
   }

   <T> T createMock(Class<T> toMock)
   {
      T mock = control.createMock(toMock);
      // We keep a ref to the mock so that it won't be GCed and finalize()d,
      // which really messes up expectations. See
      // https://sourceforge.net/tracker/index.php?func=detail&aid=2710478&group_id=82958&atid=567837
      mocks.add(mock);
      return mock;
   }

   private void publicanPush(boolean exportPot, boolean mapLocale) throws Exception
   {
      PublicanPullOptions opts = new PublicanPullOptionsImpl();
      String projectSlug = "project";
      opts.setProj(projectSlug);
      String versionSlug = "1.0";
      opts.setProjectVersion(versionSlug);
      opts.setDstDir(new File("target/test-output/test2"));
      opts.setExportPot(exportPot);
      opts.setProjectConfig("src/test/resources/test2/zanata.xml");
      OptionsUtil.applyConfigFiles(opts);
      if (mapLocale)
      {
         LocaleList locales = new LocaleList();
         locales.add(new LocaleMapping("ja", "ja-JP"));
         opts.setLocales(locales);
      }

      List<ResourceMeta> resourceMetaList = new ArrayList<ResourceMeta>();
      resourceMetaList.add(new ResourceMeta("RPM"));
      resourceMetaList.add(new ResourceMeta("sub/RPM"));
      mockExpectGetListAndReturnResponse(resourceMetaList);

      final ClientResponse<String> mockOKResponse = createMock(ClientResponse.class);
      EasyMock.expect(mockOKResponse.getStatus()).andReturn(200).anyTimes();
      Resource rpmResource = new Resource("RPM");
      mockExpectGetResourceAndReturnResponse(rpmResource);
      Resource subRpmResource = new Resource("sub/RPM");
      mockExpectGetResourceAndReturnResponse(subRpmResource);
      // StringSet extensionSet = new StringSet("gettext;comment");

      LocaleId expectedLocale;
      if (mapLocale)
         expectedLocale = new LocaleId("ja");
      else
         expectedLocale = new LocaleId("ja-JP");
      TranslationsResource rpmTransJa = new TranslationsResource();
      mockExpectGetTranslationsAndReturnResponse("RPM", expectedLocale, rpmTransJa);
      mockExpectGetTranslationsAndReturnResponse("sub/RPM", expectedLocale, null);
      ZanataProxyFactory mockRequestFactory = EasyMock.createNiceMock(ZanataProxyFactory.class);

      control.replay();
      ZanataCommand cmd = new PublicanPullCommand(opts, mockRequestFactory, mockTranslationResources, new URI("http://example.com/"));
      cmd.run();
      control.verify();
   }

   private void mockExpectGetListAndReturnResponse(List<ResourceMeta> entity)
   {
      ClientResponse<List<ResourceMeta>> mockResponse = createMock(ClientResponse.class);
      EasyMock.expect(mockTranslationResources.get(null)).andReturn(mockResponse);
      EasyMock.expect(mockResponse.getStatus()).andReturn(200);
      EasyMock.expect(mockResponse.getEntity()).andReturn(entity);
   }

   private void mockExpectGetResourceAndReturnResponse(Resource entity)
   {
      String id = entity.getName();
      ClientResponse<Resource> mockResponse = createMock(ClientResponse.class);
      String docUri = RestUtil.convertToDocumentURIId(id);
      StringSet ext = new StringSet("comment;gettext");
      EasyMock.expect(mockTranslationResources.getResource(docUri, ext)).andReturn(mockResponse);
      EasyMock.expect(mockResponse.getStatus()).andReturn(200);
      EasyMock.expect(mockResponse.getEntity()).andReturn(entity);
   }

   private void mockExpectGetTranslationsAndReturnResponse(String id, LocaleId locale, TranslationsResource entity)
   {
      ClientResponse<TranslationsResource> mockResponse = createMock(ClientResponse.class);
      String docUri = RestUtil.convertToDocumentURIId(id);
      StringSet ext = new StringSet("comment;gettext");
      EasyMock.expect(mockTranslationResources.getTranslations(docUri, locale, ext)).andReturn(mockResponse);
      if (entity != null)
      {
         EasyMock.expect(mockResponse.getResponseStatus()).andReturn(Status.OK).anyTimes();
         EasyMock.expect(mockResponse.getStatus()).andReturn(200).anyTimes();
         EasyMock.expect(mockResponse.getEntity()).andReturn(entity).anyTimes();
      }
      else
      {
         EasyMock.expect(mockResponse.getResponseStatus()).andReturn(Status.NOT_FOUND).anyTimes();
         EasyMock.expect(mockResponse.getStatus()).andReturn(404).anyTimes();
      }
   }

}
