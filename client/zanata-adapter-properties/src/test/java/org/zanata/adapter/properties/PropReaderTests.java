package org.zanata.adapter.properties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;

public class PropReaderTests
{
   private static final Logger log = LoggerFactory.getLogger(PropReaderTests.class);
   static final String ISO_8859_1 = "ISO-8859-1";

   @SuppressWarnings("deprecation")
   PropReader propReader = new PropReader();

   @Test
   public void roundtripSrcPropsToDocXmlToProps() throws Exception
   {
      Resource srcDoc = new Resource("test");
      InputStream testStream = getResourceAsStream("test.properties");

      propReader.extractTemplate(srcDoc, testStream);
      JAXBContext jc = JAXBContext.newInstance(Resource.class);
      Marshaller marshal = jc.createMarshaller();
      StringWriter sw = new StringWriter();
      marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      marshal.marshal(srcDoc, sw);
      log.debug("{}", sw);

      Unmarshaller unmarshal = jc.createUnmarshaller();
      Resource docIn = (Resource) unmarshal.unmarshal(new StringReader(sw.toString()));

      PropWriter.write(docIn, new File("target/test-output"), ISO_8859_1);

      // FIXME check output files against input
   }

   @Test
   public void roundtripTransPropsToDocXmlToProps() throws Exception
   {
      String locale = "fr";
      InputStream targetStream = getResourceAsStream("test_fr.properties");
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

      PropWriter.write(docIn, new File("target/test-output"), "test", locale, ISO_8859_1);

      // FIXME check output files against input
   }

   private InputStream getResourceAsStream(String relativeResourceName) throws FileNotFoundException
   {
      InputStream stream = PropReaderTests.class.getResourceAsStream(relativeResourceName);
      if (stream == null)
         throw new FileNotFoundException(relativeResourceName);
      return stream;
   }
}
