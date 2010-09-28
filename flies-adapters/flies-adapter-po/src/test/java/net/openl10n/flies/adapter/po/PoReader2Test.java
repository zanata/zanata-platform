package net.openl10n.flies.adapter.po;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.TextFlow;
import net.openl10n.flies.rest.dto.resource.TextFlowTarget;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;

@Test(groups = { "unit-tests" })
public class PoReader2Test
{

   private static final Logger log = LoggerFactory.getLogger(PoReader2Test.class);

   LocaleId ja = new LocaleId("ja-JP");

   @Test
   public void extractTemplateThenAdd2Targets() throws IOException, JAXBException
   {
      String testDir = "src/test/resources/";


      InputSource inputSource = new InputSource(new File(testDir, "pot/RPM.pot").toURI().toString());
      inputSource.setEncoding("utf8");

      PoReader2 poReader = new PoReader2();

      System.out.println("parsing template");
      Resource doc = poReader.extractTemplate(inputSource, LocaleId.EN_US, "doc1");
      assertThat(doc.getTextFlows().size(), is(137));
      String locale = "ja-JP";
      inputSource = new InputSource(new File(testDir, locale + "/RPM.po").toURI().toString());
      inputSource.setEncoding("utf8");
      System.out.println("extracting target: " + locale);
      TranslationsResource targetDoc = poReader.extractTarget(inputSource, doc);
      List<TextFlowTarget> textFlowTargets = targetDoc.getTextFlowTargets();
      assertThat(textFlowTargets.size(), is(137));
      TextFlowTarget target = textFlowTargets.iterator().next();
      assertThat(target, notNullValue());

      JAXBContext jaxbContext = JAXBContext.newInstance(Resource.class, TranslationsResource.class);
      Marshaller m = jaxbContext.createMarshaller();
      m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

      System.out.println("marshalling source doc");
      {
         StringWriter writer = new StringWriter();
         m.marshal(doc, writer);
         log.debug("{}", writer);
      }

      System.out.println("marshalling target doc");
      {
         StringWriter writer = new StringWriter();
         m.marshal(targetDoc, writer);
         log.debug("{}", writer);
      }

      List<TextFlow> resources = doc.getTextFlows();

      TextFlow tf1 = resources.get(3);
      assertThat(tf1.getContent(), equalTo("Important"));
      TextFlowTarget tfTarget = textFlowTargets.get(3);
      assertThat(tfTarget.getContent(), equalTo("キーのインポート"));

      // TODO test PO headers and attributes
   }
}
