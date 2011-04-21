package org.zanata.adapter.properties;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;



import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.zanata.adapter.properties.PropReader;

public class PropReaderTests
{
   // private static final Logger log =
   // LoggerFactory.getLogger(PropReaderTests.class);

   @BeforeClass
   public static void setupContainer()
   {
      // ProjectPackage.registerPartFactory(PoHeaderPart.FACTORY);
      // ProjectPackage.registerPartFactory(PotHeaderPart.FACTORY);
      // ProjectPackage.registerPartFactory(PotEntriesDataPart.FACTORY);
   }

   @SuppressWarnings("deprecation")
   PropReader propReader = new PropReader();

   @Before
   public void setup() throws IOException
   {
   }

   @Test
   public void roundtripPropsToDocXmlToProps() throws Exception
   {
      // Document docOut = new Document("test.properties",
      // ContentType.TextPlain);
      // InputStream testStream = getResourceAsStream("test.properties");
      //
      // propReader.extractTemplate(docOut, new InputSource(testStream));
      // String[] locales = new String[] { "fr" };
      // for (String locale : locales)
      // {
      // InputStream targetStream = getResourceAsStream("test_" + locale +
      // ".properties");
      // propReader.extractTarget(docOut, new InputSource(targetStream), new
      // LocaleId(locale), ContentState.New);
      // }
      // JAXBContext jc = JAXBContext.newInstance(Document.class);
      // // JAXBContext jc = JAXBContext.newInstance(Document.class.getPackage()
      // // .getName());
      // Marshaller marshal = jc.createMarshaller();
      // StringWriter sw = new StringWriter();
      // marshal.marshal(docOut, sw);
      // marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      // log.debug("{}", sw);
      //
      // Unmarshaller unmarshal = jc.createUnmarshaller();
      // Document docIn = (Document) unmarshal.unmarshal(new
      // StringReader(sw.toString()));
      //
      // PropWriter.write(docIn, new File("target/test-output"), true);

      // TODO check output files against input
   }

   @SuppressWarnings("unused")
   private InputStream getResourceAsStream(String relativeResourceName) throws FileNotFoundException
   {
      InputStream stream = PropReaderTests.class.getResourceAsStream(relativeResourceName);
      if (stream == null)
         throw new FileNotFoundException(relativeResourceName);
      return stream;
   }
}
