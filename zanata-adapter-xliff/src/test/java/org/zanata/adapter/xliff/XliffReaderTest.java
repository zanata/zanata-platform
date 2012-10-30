package org.zanata.adapter.xliff;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.zanata.adapter.xliff.XliffCommon.CHECK;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

@Test(groups = { "unit-tests" })
public class XliffReaderTest
{
   private static final String TEST_DIR = "src/test/resources/";
   private static final String RESOURCE_DIR = "src/main/resources/";
   private static final String DOC_NAME = "StringResource_en_US.xml";
   private XliffReader reader;

   @BeforeTest
   public void resetReader()
   {
      reader = new XliffReader();
      reader.setSchemaLocation(RESOURCE_DIR + "/schema/xliff-core-1.1.xsd");
   }

   @Test
   public void extractTemplateSizeTest() throws FileNotFoundException
   {
      Resource doc = getTemplateDoc();

      assertThat(doc.getName(), equalTo(DOC_NAME));
      assertThat(doc.getTextFlows().size(), is(6));
   }

   @Test
   public void templateFirstAndSecondLastTextFlowTest() throws FileNotFoundException
   {
      Resource doc = getTemplateDoc();

      TextFlow firstTextFlow = doc.getTextFlows().get(0);
      TextFlow lastTextFlow = doc.getTextFlows().get(doc.getTextFlows().size() - 2);

      assertThat(firstTextFlow.getContents(), equalTo(asList("Translation Unit 1")));
      assertThat(lastTextFlow.getContents(), equalTo(asList("Translation Unit 4 (4 &lt; 5 &amp; 4 &gt; 3)")));
   }

   @Test
   public void extractTargetSizeTest() throws FileNotFoundException
   {
      File fileTarget = new File(TEST_DIR, "/StringResource_de.xml");
      InputSource inputSource = new InputSource(new FileInputStream(fileTarget));
      TranslationsResource tr = reader.extractTarget(inputSource);
      // the file contains 4 trans-units, but one has no target element
      assertThat(tr.getTextFlowTargets().size(), is(4));
   }

   @Test
   public void targetFirstAndLastTextFlowTest() throws FileNotFoundException
   {
      File fileTarget = new File(TEST_DIR, "/StringResource_de.xml");
      InputSource inputSource = new InputSource(new FileInputStream(fileTarget));
      TranslationsResource tr = reader.extractTarget(inputSource);

      TextFlowTarget firstTextFlow = tr.getTextFlowTargets().get(0);
      TextFlowTarget lastTextFlow = tr.getTextFlowTargets().get(tr.getTextFlowTargets().size() - 2);
     
      assertThat(firstTextFlow.getContents(), equalTo(asList("Translation 1")));
      assertThat(lastTextFlow.getContents(), equalTo(asList("Translation 4 (4 &lt; 5 &amp; 4 &gt; 3)")));
   }
   
   @Test
   public void leadingEndingWhiteSpaceTargetTest() throws FileNotFoundException
   {
      File fileTarget = new File(TEST_DIR, "/StringResource_de.xml");
      InputSource inputSource = new InputSource(new FileInputStream(fileTarget));
      TranslationsResource tr = reader.extractTarget(inputSource);

      TextFlowTarget lastTextFlow = tr.getTextFlowTargets().get(tr.getTextFlowTargets().size() - 1);
      assertThat(lastTextFlow.getContents(), equalTo(asList(" Leading and trailing white space ")));
      assertThat(lastTextFlow.getContents(), not(equalTo(asList("Leading and trailing white space"))));
      assertThat(lastTextFlow.getContents(), not(equalTo(asList(" Leading and trailing white space"))));
      assertThat(lastTextFlow.getContents(), not(equalTo(asList("Leading and trailing white space "))));
   }
   
   @Test
   public void leadingEndingWhiteSpaceSourceTest() throws FileNotFoundException
   {
      File fileTarget = new File(TEST_DIR, "/StringResource_de.xml");
      InputSource inputSource = new InputSource(new FileInputStream(fileTarget));
      Resource resource = reader.extractTemplate(inputSource, LocaleId.EN_US, null);

      TextFlow tf = resource.getTextFlows().get(resource.getTextFlows().size() - 1);
      assertThat(tf.getContents(), equalTo(asList(" Translation Unit 5 (4 &lt; 5 &amp; 4 &gt; 3) ")));
      assertThat(tf.getContents(), not(equalTo(asList("Translation Unit 5 (4 &lt; 5 &amp; 4 &gt; 3)"))));
      assertThat(tf.getContents(), not(equalTo(asList(" Translation Unit 5 (4 &lt; 5 &amp; 4 &gt; 3)"))));
      assertThat(tf.getContents(), not(equalTo(asList("Translation Unit 5 (4 &lt; 5 &amp; 4 &gt; 3) "))));
   }

   @Test(expectedExceptions = RuntimeException.class)
   public void invalidSourceContentElementSchemaCheckTest() throws FileNotFoundException
   {
      reader.setCheck(CHECK.Validate);
      // expect SAXParseException with tu:transunit2 - source

      File fileTarget = new File(TEST_DIR, "/StringResource_de2.xml");
      InputSource inputSource = new InputSource(new FileInputStream(fileTarget));
      Resource resource = reader.extractTemplate(inputSource, LocaleId.EN_US, null);
   }

   @Test(expectedExceptions = RuntimeException.class)
   public void invalidTargetContentElementSchemaCheckTest() throws FileNotFoundException
   {
      reader.setCheck(CHECK.Validate);
      // expect SAXParseException with tu:transunit1 - target
      File fileTarget = new File(TEST_DIR, "/StringResource_de2.xml");
      InputSource inputSource = new InputSource(new FileInputStream(fileTarget));
      TranslationsResource tr = reader.extractTarget(inputSource);
   }

   @Test(expectedExceptions = RuntimeException.class)
   public void invalidSourceContentElementQuickCheckTest() throws FileNotFoundException
   {
      reader.setCheck(CHECK.Quick);

      File fileTarget = new File(TEST_DIR, "/StringResource_de2.xml");
      InputSource inputSource = new InputSource(new FileInputStream(fileTarget));
      Resource resource = reader.extractTemplate(inputSource, LocaleId.EN_US, null);
   }

   @Test(expectedExceptions = RuntimeException.class)
   public void invalidSourceContentElementTestNoCheck() throws FileNotFoundException
   {
      reader.setCheck(CHECK.None);

      File fileTarget = new File(TEST_DIR, "/StringResource_de2.xml");
      InputSource inputSource = new InputSource(new FileInputStream(fileTarget));
      Resource resource = reader.extractTemplate(inputSource, LocaleId.EN_US, null);

      TextFlow tf3 = resource.getTextFlows().get(2);
      TextFlow tf2 = resource.getTextFlows().get(1);
      assertThat(tf3.getContents(), equalTo(asList("Translation Unit<br></br>2")));
      assertThat(tf2.getContents(), equalTo(asList("Translation Unit 1<g></g>")));
   }


   private Resource getTemplateDoc() throws FileNotFoundException
   {
      File file = new File(TEST_DIR, File.separator + DOC_NAME);
      InputSource inputSource = new InputSource(new FileInputStream(file));
      return reader.extractTemplate(inputSource, LocaleId.EN_US, DOC_NAME);
   }
}
