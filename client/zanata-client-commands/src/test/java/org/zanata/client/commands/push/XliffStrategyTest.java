package org.zanata.client.commands.push;

import static org.easymock.EasyMock.createMock;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.easymock.EasyMock;
import org.junit.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.rest.dto.resource.Resource;

@Test(groups = "unit-tests")
public class XliffStrategyTest
{
   LocaleList locales = new LocaleList();
   XliffStrategy xliffStrategy;

   PushOptions mockPushOption;

   private final File sourceDir = new File("src/test/resources/xliffDir");

   @BeforeTest
   public void prepare()
   {
      locales.add(new LocaleMapping("de"));
      locales.add(new LocaleMapping("fr"));
   }

   @Test
   public void findDocNamesTest() throws IOException
   {
      XliffStrategy xliffStrategy = new XliffStrategy();

      mockPushOption = createMock("mockPushOption", PushOptions.class);
      EasyMock.expect(mockPushOption.getSourcePattern()).andReturn("*StringResource_en_US*");
      EasyMock.expect(mockPushOption.getLocales()).andReturn(locales).anyTimes();
      EasyMock.expect(mockPushOption.getSourceLang()).andReturn("en-US").anyTimes();

      xliffStrategy.setPushOptions(mockPushOption);
      EasyMock.replay(mockPushOption);
      Set<String> localDocNames = xliffStrategy.findDocNames(sourceDir);

      EasyMock.verify(mockPushOption);
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
      EasyMock.expect(mockPushOption.getSourcePattern()).andReturn("*StringResource_en_US*");
      EasyMock.expect(mockPushOption.getLocales()).andReturn(locales).anyTimes();
      EasyMock.expect(mockPushOption.getSourceLang()).andReturn("en-US").anyTimes();

      xliffStrategy.setPushOptions(mockPushOption);
      EasyMock.replay(mockPushOption);
      Set<String> localDocNames = xliffStrategy.findDocNames(sourceDir);
      List<Resource> resourceList = new ArrayList<Resource>();
      for (String docName : localDocNames)
      {
         System.out.println("loadSrcDocTest || Source doc name || " + docName);
         resourceList.add(xliffStrategy.loadSrcDoc(sourceDir, docName));
      }
      System.out.println("loadSrcDocTest || Total source docs:" + localDocNames.size());
      EasyMock.verify(mockPushOption);

      Assert.assertEquals(2, resourceList.size());
   }
}
