package org.zanata.client.commands;

import static org.easymock.EasyMock.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;


import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.commands.PublicanPushCommand;
import org.zanata.client.commands.PublicanPushOptionsImpl;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.client.ITranslationResources;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;

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
         EasyMock.expect(mockTranslationResources.putTranslations(eq("RPM"), eq(expectedLocale), (TranslationsResource) notNull(), eq(extensionSet), eq("auto"))).andReturn(mockOKResponse);
      }
      ZanataProxyFactory mockRequestFactory = EasyMock.createNiceMock(ZanataProxyFactory.class);

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
