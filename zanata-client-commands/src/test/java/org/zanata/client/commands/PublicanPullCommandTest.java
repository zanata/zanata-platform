package org.zanata.client.commands;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.RestUtil;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.ITranslationResources;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;

@Test(groups = "unit-tests")
public class PublicanPullCommandTest
{
   IMocksControl control = EasyMock.createControl();
   ITranslationResources mockTranslationResources = createMock("mockTranslationResources", ITranslationResources.class);

   public PublicanPullCommandTest() throws Exception
   {
   }

   public void publicanPullPo() throws Exception
   {
      publicanPull(false, false);
   }

   public void publicanPullPotAndPo() throws Exception
   {
      publicanPull(true, false);
   }

   @Test
   public void publicanPullPotAndPoWithLocaleMapping() throws Exception
   {
      publicanPull(true, true);
   }

   @BeforeMethod
   void beforeMethod()
   {
      control.reset();
   }

   <T> T createMock(String name, Class<T> toMock)
   {
      T mock = control.createMock(name, toMock);
      return mock;
   }
   
   private void publicanPull(boolean exportPot, boolean mapLocale) throws Exception
   {
      PublicanPullOptions opts = new PublicanPullOptionsImpl();
      String projectSlug = "project";
      opts.setProj(projectSlug);
      String versionSlug = "1.0";
      opts.setProjectVersion(versionSlug);
      opts.setDstDir(new File("target/test-output/test2"));
      opts.setExportPot(exportPot);
      opts.setProjectConfig(new File("src/test/resources/test2/zanata.xml"));
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
      EasyMock.expect(mockTranslationResources.get(null)).andReturn(new DummyResponse<List<ResourceMeta>>(Status.OK, resourceMetaList));

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

   private void mockExpectGetResourceAndReturnResponse(Resource entity)
   {
      String id = entity.getName();
      String docUri = RestUtil.convertToDocumentURIId(id);
      StringSet ext = new StringSet("comment;gettext");
      EasyMock.expect(mockTranslationResources.getResource(docUri, ext)).andReturn(new DummyResponse<Resource>(Status.OK, entity));
   }

   private void mockExpectGetTranslationsAndReturnResponse(String id, LocaleId locale, TranslationsResource entity)
   {
      String docUri = RestUtil.convertToDocumentURIId(id);
      StringSet ext = new StringSet("comment;gettext");
      if (entity != null)
      {
         EasyMock.expect(mockTranslationResources.getTranslations(docUri, locale, ext)).andReturn(new DummyResponse<TranslationsResource>(Status.OK, entity));
      }
      else
      {
         EasyMock.expect(mockTranslationResources.getTranslations(docUri, locale, ext)).andReturn(new DummyResponse<TranslationsResource>(Status.NOT_FOUND, entity));
      }
   }

}
