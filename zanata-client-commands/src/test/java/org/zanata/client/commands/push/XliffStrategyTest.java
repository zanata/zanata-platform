package org.zanata.client.commands.push;

import static org.easymock.EasyMock.eq;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.NotFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.easymock.EasyMock;
import org.easymock.IMocksControl;
import org.junit.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.zanata.client.commands.push.PushCommand.TranslationResourcesVisitor;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

@Test(groups = "unit-tests")
public class XliffStrategyTest
{
   LocaleList locales = new LocaleList();
   XliffStrategy xliffStrategy;

   IMocksControl control = EasyMock.createControl();
   PushOptions mockPushOption;

   private final File sourceDir = new File("src/test/resources/xliffDir");

   @BeforeTest
   public void prepare()
   {
      locales.add(new LocaleMapping("de"));
      locales.add(new LocaleMapping("fr"));
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

   @Test
   public void findDocNamesTest() throws IOException
   {
      XliffStrategy xliffStrategy = new XliffStrategy();

      mockPushOption = createMock("mockPushOption", PushOptions.class);
      EasyMock.expect(mockPushOption.getIncludeFilePattern()).andReturn("*StringResource_en_US*");
      EasyMock.expect(mockPushOption.getLocales()).andReturn(locales).anyTimes();
      EasyMock.expect(mockPushOption.getSourceLang()).andReturn("en-US").anyTimes();

      xliffStrategy.setPushOptions(mockPushOption);
      EasyMock.replay(mockPushOption);
     
      AndFileFilter fileFilter = new AndFileFilter();
      WildcardFileFilter includeFilter = new WildcardFileFilter(mockPushOption.getIncludeFilePattern());
      fileFilter.addFileFilter(includeFilter);

      Set<String> localDocNames = xliffStrategy.findDocNames(sourceDir, fileFilter);

      control.verify();
      for (String docName : localDocNames)
         System.out.println("findDocNamesTest || Source doc name || " + docName);
      System.out.println("findDocNamesTest || Total source docs || " + localDocNames.size());
      Assert.assertEquals(2, localDocNames.size());
   }

   @Test
   public void loadSrcDocTest() throws IOException
   {
      XliffStrategy xliffStrategy = new XliffStrategy();

      mockPushOption = createMock("mockPushOption", PushOptions.class);
      EasyMock.expect(mockPushOption.getIncludeFilePattern()).andReturn("*StringResource_en_US*");
      EasyMock.expect(mockPushOption.getTransDir()).andReturn(sourceDir).anyTimes();
      EasyMock.expect(mockPushOption.getLocales()).andReturn(locales).anyTimes();
      EasyMock.expect(mockPushOption.getSourceLang()).andReturn("en-US").anyTimes();

      xliffStrategy.setPushOptions(mockPushOption);
      EasyMock.replay(mockPushOption);

      AndFileFilter fileFilter = new AndFileFilter();
      WildcardFileFilter includeFilter = new WildcardFileFilter(mockPushOption.getIncludeFilePattern());
      fileFilter.addFileFilter(includeFilter);

      Set<String> localDocNames = xliffStrategy.findDocNames(sourceDir, fileFilter);
      List<Resource> resourceList = new ArrayList<Resource>();
      for (String docName : localDocNames)
      {
         Resource srcDoc = xliffStrategy.loadSrcDoc(sourceDir, docName);
         resourceList.add(srcDoc);

         TranslationResourcesVisitor visitor = EasyMock.createMock("visitor" + resourceList.size(), TranslationResourcesVisitor.class);
         LocaleMapping loc;
         // each src file in test has one trans file ('de' or 'fr'):
         if (srcDoc.getName().equals("dir1/StringResource"))
         {
            loc = new LocaleMapping("de");
         }
         else
         {
            loc = new LocaleMapping("fr");
         }
         visitor.visit(eq(loc), EasyMock.anyObject(TranslationsResource.class));
         EasyMock.replay(visitor);
         xliffStrategy.visitTranslationResources(docName, srcDoc, visitor);
         EasyMock.verify(visitor);
      }
      System.out.println("loadSrcDocTest || Total source docs:" + localDocNames.size());
      control.verify();

      Assert.assertEquals(2, resourceList.size());
   }

   @Test
   public void loadSrcDocTestWithExcludeOption() throws IOException
   {
      XliffStrategy xliffStrategy = new XliffStrategy();

      mockPushOption = createMock("mockPushOption", PushOptions.class);
      EasyMock.expect(mockPushOption.getIncludeFilePattern()).andReturn("*StringResource_en_US*");
      EasyMock.expect(mockPushOption.getTransDir()).andReturn(sourceDir).anyTimes();
      EasyMock.expect(mockPushOption.getLocales()).andReturn(locales).anyTimes();
      EasyMock.expect(mockPushOption.getSourceLang()).andReturn("en-US").anyTimes();
      EasyMock.expect(mockPushOption.getExcludeFilePattern()).andReturn("*StringResource*").anyTimes();

      xliffStrategy.setPushOptions(mockPushOption);
      EasyMock.replay(mockPushOption);

      AndFileFilter fileFilter = new AndFileFilter();
      WildcardFileFilter includeFilter = new WildcardFileFilter(mockPushOption.getIncludeFilePattern());
      fileFilter.addFileFilter(includeFilter);

      NotFileFilter excludeFilter = new NotFileFilter(new WildcardFileFilter(mockPushOption.getExcludeFilePattern()));
      fileFilter.addFileFilter(excludeFilter);

      Set<String> localDocNames = xliffStrategy.findDocNames(sourceDir, fileFilter);
      List<Resource> resourceList = new ArrayList<Resource>();
      for (String docName : localDocNames)
      {
         Resource srcDoc = xliffStrategy.loadSrcDoc(sourceDir, docName);
         resourceList.add(srcDoc);

         TranslationResourcesVisitor visitor = EasyMock.createMock("visitor" + resourceList.size(), TranslationResourcesVisitor.class);
         LocaleMapping loc;
         // each src file in test has one trans file ('de' or 'fr'):
         if (srcDoc.getName().equals("dir1/StringResource"))
         {
            loc = new LocaleMapping("de");
         }
         else
         {
            loc = new LocaleMapping("fr");
         }
         visitor.visit(eq(loc), EasyMock.anyObject(TranslationsResource.class));
         EasyMock.replay(visitor);
         xliffStrategy.visitTranslationResources(docName, srcDoc, visitor);
         EasyMock.verify(visitor);
      }
      System.out.println("loadSrcDocTestWithExclude || Total source docs:" + localDocNames.size());
      control.verify();

      Assert.assertEquals(0, resourceList.size());
   }
}
