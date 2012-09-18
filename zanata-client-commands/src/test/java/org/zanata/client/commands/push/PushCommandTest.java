package org.zanata.client.commands.push;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.notNull;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response.Status;

import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.jboss.resteasy.client.ClientResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.client.commands.DummyResponse;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.client.commands.ZanataCommand;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.LocaleId;
import org.zanata.rest.StringSet;
import org.zanata.rest.client.ISourceDocResource;
import org.zanata.rest.client.ITranslatedDocResource;
import org.zanata.rest.client.ZanataProxyFactory;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

@Test(groups = "unit-tests")
public class PushCommandTest
{
   IMocksControl control = EasyMock.createControl();
   ISourceDocResource mockSourceDocResource = createMock("mockSourceDocResource", ISourceDocResource.class);
   ITranslatedDocResource mockTranslationResources = createMock("mockTranslationResources", ITranslatedDocResource.class);

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

   @Test
   public void testSplitTranslationResource() throws Exception
   {
      int batchSize = 100;
      int listSize = 500;

      checkSplitResult(listSize, batchSize);

      batchSize = 50;
      listSize = 500;

      checkSplitResult(listSize, batchSize);
   }

   @Test
   public void testSplitTranslationResourceWithMod() throws Exception
   {
      int batchSize = 100;
      int listSize = 505;

      checkSplitResult(listSize, batchSize);

      batchSize = 100;
      listSize = 510;

      checkSplitResult(listSize, batchSize);
   }

   private void checkSplitResult(int listSize, int batchSize) throws Exception
   {
      PushCommand cmd = generatePushCommand(true, true);
      TranslationsResource transRes = new TranslationsResource();
      for (int i = 0; i < listSize; i++)
      {
         transRes.getTextFlowTargets().add(new TextFlowTarget(String.valueOf(i)));
      }

      List<TranslationsResource> list = cmd.splitIntoBatch(transRes, batchSize);

      int expectListSize = listSize / batchSize;
      if (listSize % batchSize != 0)
      {
         expectListSize = expectListSize + 1;
      }

      int expectLastTftSize = listSize % batchSize;
      if (expectLastTftSize == 0)
      {
         expectLastTftSize = batchSize;
      }

      assertEquals(list.size(), expectListSize);
      assertEquals(list.get(0).getTextFlowTargets().size(), batchSize);
      assertEquals(list.get(list.size() - 1).getTextFlowTargets().size(), expectLastTftSize);
   }

   private PushCommand generatePushCommand(boolean pushTrans, boolean mapLocale) throws Exception
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
      opts.batchSize = 100;
      LocaleList locales = new LocaleList();
      if (mapLocale)
      {
         locales.add(new LocaleMapping("ja", "ja-JP"));
      }
      else
      {
         locales.add(new LocaleMapping("ja-JP"));
      }
      opts.setLocaleMapList(locales);
      OptionsUtil.applyConfigFiles(opts);

      ZanataProxyFactory mockRequestFactory = EasyMock.createNiceMock(ZanataProxyFactory.class);

      return new PushCommand(opts, mockRequestFactory, mockSourceDocResource, mockTranslationResources, new URI("http://example.com/"));
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
      List<ResourceMeta> resourceMetaList = new ArrayList<ResourceMeta>();
      resourceMetaList.add(new ResourceMeta("obsolete"));
      resourceMetaList.add(new ResourceMeta("RPM"));
      EasyMock.expect(mockSourceDocResource.get(null)).andReturn(new DummyResponse<List<ResourceMeta>>(Status.OK, resourceMetaList));

      final ClientResponse<String> okResponse = new DummyResponse<String>(Status.OK, null);
      EasyMock.expect(mockSourceDocResource.deleteResource("obsolete")).andReturn(okResponse);
      StringSet extensionSet = new StringSet("gettext;comment");
      EasyMock.expect(mockSourceDocResource.putResource(eq("RPM"), (Resource) notNull(), eq(extensionSet), eq(true))).andReturn(okResponse);
      EasyMock.expect(mockSourceDocResource.putResource(eq("sub,RPM"), (Resource) notNull(), eq(extensionSet), eq(true))).andReturn(okResponse);

      if (pushTrans)
      {
         LocaleId expectedLocale;
         if (mapLocale)
         {
            expectedLocale = new LocaleId("ja");
         }
         else
         {
            expectedLocale = new LocaleId("ja-JP");
         }
         EasyMock.expect(mockTranslationResources.putTranslations(eq("RPM"), eq(expectedLocale), (TranslationsResource) notNull(), eq(extensionSet), eq("auto"))).andReturn(okResponse);
      }
      control.replay();
      ZanataCommand cmd = generatePushCommand(pushTrans, mapLocale);
      cmd.run();
      control.verify();
   }

}
