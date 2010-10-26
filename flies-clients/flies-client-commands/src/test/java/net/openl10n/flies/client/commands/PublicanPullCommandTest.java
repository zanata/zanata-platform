package net.openl10n.flies.client.commands;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import net.openl10n.flies.client.config.LocaleList;
import net.openl10n.flies.client.config.LocaleMapping;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.RestUtil;
import net.openl10n.flies.rest.StringSet;
import net.openl10n.flies.rest.client.FliesClientRequestFactory;
import net.openl10n.flies.rest.client.ITranslationResources;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "unit-tests")
public class PublicanPullCommandTest
{
   IMocksControl control = EasyMock.createControl();
   ITranslationResources mockTranslationResources = control.createMock(ITranslationResources.class);

   public PublicanPullCommandTest() throws Exception
   {
   }

   @Test
   public void publicanPullPo() throws Exception
   {
      publicanPush(false, false);
   }

   @Test
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

   private void publicanPush(boolean exportPot, boolean mapLocale) throws Exception
   {
      PublicanPullOptions opts = new PublicanPullOptionsImpl();
      String projectSlug = "project";
      opts.setProj(projectSlug);
      String versionSlug = "1.0";
      opts.setProjectVersion(versionSlug);
      opts.setDstDir(new File("target/test-output/test2"));
      opts.setExportPot(exportPot);
      opts.setProjectConfig("src/test/resources/test2/flies.xml");
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

      final ClientResponse<String> mockOKResponse = control.createMock(ClientResponse.class);
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
      FliesClientRequestFactory mockRequestFactory = EasyMock.createNiceMock(FliesClientRequestFactory.class);

      control.replay();
      FliesCommand cmd = new PublicanPullCommand(opts, mockRequestFactory, mockTranslationResources, new URI("http://example.com/"));
      cmd.run();
      control.verify();
   }

   private void mockExpectGetListAndReturnResponse(List<ResourceMeta> entity)
   {
      ClientResponse<List<ResourceMeta>> mockResponse = control.createMock(ClientResponse.class);
      EasyMock.expect(mockTranslationResources.get(null)).andReturn(mockResponse);
      EasyMock.expect(mockResponse.getStatus()).andReturn(200);
      EasyMock.expect(mockResponse.getEntity()).andReturn(entity);
   }

   private void mockExpectGetResourceAndReturnResponse(Resource entity)
   {
      String id = entity.getName();
      ClientResponse<Resource> mockResponse = control.createMock(ClientResponse.class);
      String docUri = RestUtil.convertToDocumentURIId(id);
      StringSet ext = new StringSet("comment;gettext");
      EasyMock.expect(mockTranslationResources.getResource(docUri, ext)).andReturn(mockResponse);
      EasyMock.expect(mockResponse.getStatus()).andReturn(200);
      EasyMock.expect(mockResponse.getEntity()).andReturn(entity);
   }

   private void mockExpectGetTranslationsAndReturnResponse(String id, LocaleId locale, TranslationsResource entity)
   {
      ClientResponse<TranslationsResource> mockResponse = control.createMock(ClientResponse.class);
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
