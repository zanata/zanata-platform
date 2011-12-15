package org.zanata.client.commands.push;

import static org.easymock.EasyMock.*;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.client.commands.ConfigurableProjectCommand;
import org.zanata.client.commands.DummyResponse;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.ITranslationResources;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;

@Test(groups = "unit-tests")
public class PushCommandTest
{
   IMocksControl control = EasyMock.createControl();
   ITranslationResources mockTranslationResources = createMock("mockTranslationResources", ITranslationResources.class);

   public PushCommandTest() throws Exception
   {
   }

   @Test
   public void pushSrc() throws Exception
   {
      push(false, false);
   }

   @Test
   public void pushSrcAndTarget() throws Exception
   {
      push(true, false);
   }

   @Test
   public void pushSrcAndTargetWithLocaleMapping() throws Exception
   {
      push(true, true);
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
   
   private void push(boolean pushTrans, boolean mapLocale) throws Exception
   {
      PushOptionsImpl opts = new PushOptionsImpl();
      opts.setInteractiveMode(false);
      String projectSlug = "project";
      opts.setProj(projectSlug);
      String versionSlug = "1.0";
      opts.setProjectVersion(versionSlug);
      opts.setSrcDir(new File("src/test/resources/test1/pot"));
      opts.setPushTrans(pushTrans);
      opts.transDir = new File("src/test/resources/test1");
      opts.setProjectType("podir");
      opts.copyTrans = true;
      opts.includes = new ArrayList<String>();
      opts.excludes = new ArrayList<String>();
      opts.sourceLang = "en-US";
      opts.mergeType = "auto";
      OptionsUtil.applyConfigFiles(opts);
      LocaleList locales = new LocaleList();
      if (mapLocale)
      {
         locales.add(new LocaleMapping("ja", "ja-JP"));
      }
      else
      {
         locales.add(new LocaleMapping("ja-JP"));
      }
      opts.setLocales(locales);

      List<ResourceMeta> resourceMetaList = new ArrayList<ResourceMeta>();
      resourceMetaList.add(new ResourceMeta("obsolete"));
      resourceMetaList.add(new ResourceMeta("RPM"));
      EasyMock.expect(mockTranslationResources.get(null)).andReturn(new DummyResponse<List<ResourceMeta>>(Status.OK, resourceMetaList));

      final ClientResponse<String> okResponse = new DummyResponse<String>(Status.OK, null);
      EasyMock.expect(mockTranslationResources.deleteResource("obsolete")).andReturn(okResponse);
      StringSet extensionSet = new StringSet("gettext;comment");
      EasyMock.expect(mockTranslationResources.putResource(eq("RPM"), (Resource) notNull(), eq(extensionSet), eq(true))).andReturn(okResponse);
      EasyMock.expect(mockTranslationResources.putResource(eq("sub,RPM"), (Resource) notNull(), eq(extensionSet), eq(true))).andReturn(okResponse);

      if (pushTrans)
      {
         LocaleId expectedLocale;
         if (mapLocale)
            expectedLocale = new LocaleId("ja");
         else
            expectedLocale = new LocaleId("ja-JP");
         EasyMock.expect(mockTranslationResources.putTranslations(eq("RPM"), eq(expectedLocale), (TranslationsResource) notNull(), eq(extensionSet), eq("auto"))).andReturn(okResponse);
      }
      ZanataProxyFactory mockRequestFactory = EasyMock.createNiceMock(ZanataProxyFactory.class);

      control.replay();
      ZanataCommand cmd = new PushCommand(opts, mockRequestFactory, mockTranslationResources, new URI("http://example.com/"));
      cmd.run();
      control.verify();
   }

}
