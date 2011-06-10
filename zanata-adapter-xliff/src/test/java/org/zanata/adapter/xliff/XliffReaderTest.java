package org.zanata.adapter.xliff;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.apache.commons.lang.StringUtils;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

@Test(groups = { "unit-tests" })
public class XliffReaderTest
{
   private String testDir = "src/test/resources/";
   private XliffReader reader;

   @Test
   public void extractTemplateSizeTest() throws FileNotFoundException
   {
      Resource doc = getTemplateDoc();

      assertThat(doc.getName(), equalTo("StringResource_en_US.xml"));
      assertThat(doc.getTextFlows().size(), is(639));
   }

   @Test
   public void templateFirstAndLastTextFlowTest() throws FileNotFoundException
   {
      Resource doc = getTemplateDoc();

      TextFlow firstTextFlow = doc.getTextFlows().get(0);
      TextFlow lastTextFlow = doc.getTextFlows().get(doc.getTextFlows().size() - 1);

      assertThat(firstTextFlow.getContent(), equalTo("Queued"));
      assertThat(lastTextFlow.getContent(), equalTo("Kickstart failed."));
   }

   @Test
   public void extractTargetSizeTest() throws FileNotFoundException
   {
      Resource doc = getTemplateDoc();

      File fileTarget = new File(testDir, "/StringResource_de.xml");
      InputSource inputSource = new InputSource(new FileInputStream(fileTarget));
      TranslationsResource tr = reader.extractTarget(inputSource);
      assertThat(tr.getTextFlowTargets().size(), is(637));
   }

   @Test
   public void targetFirstAndLastTextFlowTest() throws FileNotFoundException
   {
      Resource doc = getTemplateDoc();
      
      File fileTarget = new File(testDir, "/StringResource_de.xml");
      InputSource inputSource = new InputSource(new FileInputStream(fileTarget));
      TranslationsResource tr = reader.extractTarget(inputSource);

      TextFlowTarget firstTextFlow = tr.getTextFlowTargets().get(0);
      TextFlowTarget lastTextFlow = tr.getTextFlowTargets().get(tr.getTextFlowTargets().size() - 1);
     
      assertThat(firstTextFlow.getContent(), equalTo("Wartend"));
      assertThat(lastTextFlow.getContent(), equalTo("Kickstart fehlgeschlagen."));
   }


   private Resource getTemplateDoc() throws FileNotFoundException
   {
      reader = new XliffReader();
      String docName = "StringResource_en_US.xml";

      File file = new File(testDir, "/" + docName);
      InputSource inputSource = new InputSource(new FileInputStream(file));
      return reader.extractTemplate(inputSource, LocaleId.EN_US, docName);
   }
}
