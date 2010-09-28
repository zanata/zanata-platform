package net.openl10n.flies.adapter.po;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.dto.deprecated.Document;
import net.openl10n.flies.rest.dto.deprecated.TextFlow;
import net.openl10n.flies.rest.dto.deprecated.TextFlowTarget;
import net.openl10n.flies.rest.dto.deprecated.TextFlowTargets;
import net.openl10n.flies.rest.dto.po.PoHeader;
import net.openl10n.flies.rest.dto.po.PotEntryData;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;

@Test(groups = { "unit-tests" })
public class PoReaderTest
{
   private static final Logger log = LoggerFactory.getLogger(PoReaderTest.class);

   LocaleId ja = new LocaleId("ja-JP");

   @Test
   public void extractTemplateThenAdd2Targets() throws IOException, JAXBException
   {
      String testDir = "src/test/resources/";

      Document doc = new Document("doc1", "doc1.pot", "/", PoReader.PO_CONTENT_TYPE);

      InputSource inputSource = new InputSource(new File(testDir, "pot/RPM.pot").toURI().toString());
      inputSource.setEncoding("utf8");

      PoReader poReader = new PoReader();

      System.out.println("parsing template");
      poReader.extractTemplate(doc, inputSource);
      assertThat(doc.getTextFlows().size(), equalTo(137));
      String[] locales = new String[] { "ja-JP" };
      for (String locale : locales)
      {
         inputSource = new InputSource(new File(testDir, locale + "/RPM.po").toURI().toString());
         inputSource.setEncoding("utf8");
         System.out.println("extracting target: " + locale);
         poReader.extractTarget(doc, inputSource, new LocaleId(locale));
         Set<TextFlowTarget> targets = doc.getTextFlows().get(0).getTargets().getTargets();
         assertThat(targets.size(), equalTo(1));
         TextFlowTarget target = targets.iterator().next();
         assertThat(target, notNullValue());
      }

      System.out.println("marshalling");
      JAXBContext jaxbContext = JAXBContext.newInstance(Document.class, PotEntryData.class, PoHeader.class, TextFlowTargets.class);
      Marshaller m = jaxbContext.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
      {
         StringWriter writer = new StringWriter();
         m.marshal(doc, writer);
         log.debug("{}", writer);
      }

      List<TextFlow> resources = doc.getTextFlows();

      TextFlow tf1 = resources.get(3);
      assertThat(tf1.getContent(), equalTo("Important"));
      assertThat(tf1.getTarget(ja).getContent(), equalTo("キーのインポート"));

      // TODO test PO headers and attributes
   }
}
