package org.zanata.adapter.properties;

import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.util.InvalidPropertiesFormatException;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.testng.Assert.*;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TranslationsResource;

public class PropReaderTests
{
   private static final Logger log = LoggerFactory.getLogger(PropReaderTests.class);
   private static final String TEST_OUTPUT_DIR_STRING = "target/test-output";
   private static final File TEST_OUTPUT_DIR = new File(TEST_OUTPUT_DIR_STRING);

   @SuppressWarnings("deprecation")
   PropReader propReader;

   @BeforeMethod
   public void resetReader()
   {
      propReader = new PropReader();
   }

   @Test
   public void roundtripSrcPropsToDocXmlToProps() throws Exception
   {
      String docName = "test.properties";

      Resource srcDoc = new Resource("test");
      InputStream testStream = getResourceAsStream(docName);

      propReader.extractTemplate(srcDoc, testStream);
      JAXBContext jc = JAXBContext.newInstance(Resource.class);
      Marshaller marshal = jc.createMarshaller();
      StringWriter sw = new StringWriter();
      marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshal.marshal(srcDoc, sw);
      log.debug("{}", sw);

      Unmarshaller unmarshal = jc.createUnmarshaller();
      Resource docIn = (Resource) unmarshal.unmarshal(new StringReader(sw.toString()));

      PropWriter.write(docIn, TEST_OUTPUT_DIR);

      assertInputAndOutputDocContentSame(docName);
   }

   @Test
   public void roundtripTransPropsToDocXmlToProps() throws Exception
   {
      String locale = "fr";
      String docName = "test_fr.properties";
      InputStream targetStream = getResourceAsStream(docName);
      TranslationsResource transDoc = new TranslationsResource();
      propReader.extractTarget(transDoc, targetStream, new LocaleId(locale), ContentState.New);

      JAXBContext jc = JAXBContext.newInstance(TranslationsResource.class);
      Marshaller marshal = jc.createMarshaller();
      StringWriter sw = new StringWriter();
      marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshal.marshal(transDoc, sw);
      log.debug("{}", sw);

      Unmarshaller unmarshal = jc.createUnmarshaller();
      TranslationsResource docIn = (TranslationsResource) unmarshal.unmarshal(new StringReader(sw.toString()));

      PropWriter.write(docIn, TEST_OUTPUT_DIR, "test", locale);
      
      assertInputAndOutputDocContentSame(docName);
   }

   /**
    * Asserts that the content of the input document and output document are
    * identical. Assumes filename is the same for both documents and that the
    * output file is in TEST_OUTPUT_DIR.
    * 
    * @param docName the name for both input and output files
    * @throws FileNotFoundException
    * @throws IOException
    * @throws MalformedURLException
    */
   private void assertInputAndOutputDocContentSame(String docName) throws FileNotFoundException, IOException, MalformedURLException
   {
      File newFile = new File(TEST_OUTPUT_DIR.getPath() + File.separator + docName);
      InputStream newStream = newFile.toURI().toURL().openStream();
      InputStream origStream = getResourceAsStream(docName);

      String origContent = IOUtils.toString(origStream);
      String newContent = IOUtils.toString(newStream);

      // note: this does not allow for differences in whitespace, so if tests
      // start failing this should be updated to use a less strict comparison
      assertEquals(newContent, origContent, "output file should be the same as the input file");
   }

   private InputStream getResourceAsStream(String relativeResourceName) throws FileNotFoundException
   {
      InputStream stream = PropReaderTests.class.getResourceAsStream(relativeResourceName);
      if (stream == null)
         throw new FileNotFoundException(relativeResourceName);
      return stream;
   }
   
   @Test
   public void extractTemplateRemovesNonTranslateableRegions() throws IOException
   {
      Resource srcDoc = new Resource("test");
      InputStream testStream = getResourceAsStream("test_non_trans.properties");
      propReader.extractTemplate(srcDoc, testStream);

      List<TextFlow> textFlows = srcDoc.getTextFlows();

      assertEquals(textFlows.size(), 2, "Unexpected number of textflows");
      assertEquals(textFlows.get(0).getId(), "HELLO", "Unexpected textflow id");
      assertEquals(textFlows.get(1).getId(), "GOODBYE", "Unexpected textflow id");
      // TODO also check comments?
   }

   @Test
   public void extractTemplateNestedNonTranslatableRegions() throws Exception
   {
      Resource srcDoc = new Resource("test");
      InputStream testStream = getResourceAsStream("test_non_trans_nested.properties");
      propReader.extractTemplate(srcDoc, testStream);

      List<TextFlow> textFlows = srcDoc.getTextFlows();

      assertEquals(textFlows.size(), 2, "Unexpected number of textflows");
      assertEquals(textFlows.get(0).getId(), "HELLO", "Unexpected textflow id");
      assertEquals(textFlows.get(1).getId(), "GOODBYE", "Unexpected textflow id");
      // TODO also check comments?
   }

   @Test(expectedExceptions = InvalidPropertiesFormatException.class)
   public void extractTemplateNonTranslatableMismatchException() throws IOException, InvalidPropertiesFormatException
   {
      Resource srcDoc = new Resource("test");
      InputStream testStream = getResourceAsStream("test_non_trans_mismatch.properties");
      propReader.extractTemplate(srcDoc, testStream);
   }
}
