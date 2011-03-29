package net.openl10n.flies.client.commands;

import static org.easymock.EasyMock.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import net.openl10n.flies.client.config.LocaleList;
import net.openl10n.flies.client.config.LocaleMapping;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.StringSet;
import net.openl10n.flies.rest.client.FliesClientRequestFactory;
import net.openl10n.flies.rest.client.ITranslationResources;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.ResourceMeta;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "unit-tests")
public class PublicanPushCommandTest
{
   IMocksControl control = EasyMock.createControl();
   ITranslationResources mockTranslationResources = control.createMock(ITranslationResources.class);

   public PublicanPushCommandTest() throws Exception
   {
   }

   @Test
   public void publicanPushPot() throws Exception
   {
      publicanPush(false, false);
   }

   @Test
   public void publicanPushPotAndPo() throws Exception
   {
      publicanPush(true, false);
   }

   @Test
   public void publicanPushPotAndPoWithLocaleMapping() throws Exception
   {
      publicanPush(true, true);
   }

   @BeforeMethod
   void beforeMethod()
   {
      control.reset();
   }

   private void publicanPush(boolean importPo, boolean mapLocale) throws Exception
   {
      PublicanPushOptionsImpl opts = new PublicanPushOptionsImpl();
      opts.setInteractiveMode(false);
      String projectSlug = "project";
      opts.setProj(projectSlug);
      String versionSlug = "1.0";
      opts.setProjectVersion(versionSlug);
      opts.setSrcDir(new File("src/test/resources/test1"));
      opts.setImportPo(importPo);
      OptionsUtil.applyConfigFiles(opts);
      if (mapLocale)
      {
         LocaleList locales = new LocaleList();
         locales.add(new LocaleMapping("ja", "ja-JP"));
         opts.setLocales(locales);
      }

      List<ResourceMeta> resourceMetaList = new ArrayList<ResourceMeta>();
      resourceMetaList.add(new ResourceMeta("obsolete"));
      resourceMetaList.add(new ResourceMeta("RPM"));
      mockExpectGetListAndReturnResponse(resourceMetaList);

      final ClientResponse<String> mockOKResponse = control.createMock(ClientResponse.class);
      EasyMock.expect(mockOKResponse.getStatus()).andReturn(200).anyTimes();
      EasyMock.expect(mockTranslationResources.deleteResource("obsolete")).andReturn(mockOKResponse);
      StringSet extensionSet = new StringSet("gettext;comment");
      EasyMock.expect(mockTranslationResources.putResource(eq("RPM"), (Resource) notNull(), eq(extensionSet), eq(true))).andReturn(mockOKResponse);
      EasyMock.expect(mockTranslationResources.putResource(eq("sub,RPM"), (Resource) notNull(), eq(extensionSet), eq(true))).andReturn(mockOKResponse);

      if (importPo)
      {
         LocaleId expectedLocale;
         if (mapLocale)
            expectedLocale = new LocaleId("ja");
         else
            expectedLocale = new LocaleId("ja-JP");
         EasyMock.expect(mockTranslationResources.putTranslations(eq("RPM"), eq(expectedLocale), (TranslationsResource) notNull(), eq(extensionSet))).andReturn(mockOKResponse);
      }
      FliesClientRequestFactory mockRequestFactory = EasyMock.createNiceMock(FliesClientRequestFactory.class);

      control.replay();
      ZanataCommand cmd = new PublicanPushCommand(opts, mockRequestFactory, mockTranslationResources, new URI("http://example.com/"));
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

}
