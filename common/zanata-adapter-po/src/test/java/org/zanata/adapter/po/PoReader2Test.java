package org.zanata.adapter.po;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.fedorahosted.tennera.jgettext.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import org.xml.sax.InputSource;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

@Test(groups = { "unit-tests" })
public class PoReader2Test
{
   private static final Logger log = LoggerFactory.getLogger(PoReader2Test.class);

   LocaleId ja = new LocaleId("ja-JP");
   String testDir = "src/test/resources/";
   PoReader2 poReader = new PoReader2();

   private Resource getTemplate()
   {
      InputSource inputSource = new InputSource(new File(testDir, "pot/RPM.pot").toURI().toString());
      inputSource.setEncoding("utf8");

      System.out.println("parsing template");
      Resource doc = poReader.extractTemplate(inputSource, LocaleId.EN_US, "doc1");
      assertThat(doc.getTextFlows().size(), is(137));
      return doc;
   }

   @Test
   public void extractTarget() throws IOException, JAXBException
   {
      InputSource inputSource;
      Resource doc = getTemplate();
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
      assertThat(tf1.getContents(), equalTo(asList("Important")));
      TextFlowTarget tfTarget = textFlowTargets.get(3);
      assertThat(tfTarget.getContents(), equalTo(asList("キーのインポート")));

      // TODO test PO headers and attributes
   }

   @Test
   public void extractTemplate()
   {
      getTemplate();
   }

   @Test(expectedExceptions = { RuntimeException.class }, expectedExceptionsMessageRegExp = ".*unsupported charset.*")
   public void extractInvalidTemplate() throws IOException, JAXBException
   {
      InputSource inputSource = new InputSource(new File(testDir, "pot/invalid.pot").toURI().toString());
      inputSource.setEncoding("utf8");

      poReader.extractTemplate(inputSource, LocaleId.EN_US, "doc1");
   }

   @Test(expectedExceptions = { RuntimeException.class }, expectedExceptionsMessageRegExp = ".*unsupported charset.*")
   public void extractInvalidTarget() throws IOException, JAXBException
   {
      Resource srcDoc = getTemplate();

      String locale = "ja-JP";
      InputSource inputSource = new InputSource(new File(testDir, locale + "/invalid.po").toURI().toString());
      inputSource.setEncoding("utf8");
      System.out.println("extracting target: " + locale);

      poReader.extractTarget(inputSource, srcDoc);
   }

   public void testContentStateApprovedSingle()
   {
      Message m = new Message();
      m.setMsgstr("s");
      ContentState actual1 = PoReader2.getContentState(m);
      assertThat(actual1, is(ContentState.Approved));
   }

   public void testContentStateApprovedPlural1()
   {
      Message m = new Message();
      m.setMsgidPlural("plural");
      m.addMsgstrPlural("s0", 0);
      ContentState actual1 = PoReader2.getContentState(m);
      assertThat(actual1, is(ContentState.Approved));
   }

   public void testContentStateApprovedPlural2()
   {
      Message m = new Message();
      m.setMsgidPlural("plural");
      m.addMsgstrPlural("s0", 0);
      m.addMsgstrPlural("s1", 1);
      ContentState actual1 = PoReader2.getContentState(m);
      assertThat(actual1, is(ContentState.Approved));
   }

   public void testContentStateNewSingle1()
   {
      Message m = new Message();
      m.setMsgstr("");
      ContentState actual1 = PoReader2.getContentState(m);
      assertThat(actual1, is(ContentState.New));
   }

   public void testContentStateNewSingle2()
   {
      Message m = new Message();
      ContentState actual1 = PoReader2.getContentState(m);
      assertThat(actual1, is(ContentState.New));
   }

   public void testContentStateNewPlural1()
   {
      Message m = new Message();
      m.setMsgidPlural("plural");
      m.addMsgstrPlural("", 0);
      ContentState actual1 = PoReader2.getContentState(m);
      assertThat(actual1, is(ContentState.New));
   }

   public void testContentStateNewPlural2()
   {
      Message m = new Message();
      m.setMsgidPlural("plural");
      m.addMsgstrPlural("", 0);
      m.addMsgstrPlural("s1", 1);
      ContentState actual1 = PoReader2.getContentState(m);
      assertThat(actual1, is(ContentState.New));
   }

   public void testContentStateNewPlural3()
   {
      Message m = new Message();
      m.setMsgidPlural("plural");
      ContentState actual1 = PoReader2.getContentState(m);
      assertThat(actual1, is(ContentState.New));
   }

   public void testContentStateNewPlural4()
   {
      Message m = new Message();
      m.setMsgidPlural("plural");
      m.addMsgstrPlural("", 0);
      m.addMsgstrPlural("", 1);
      ContentState actual1 = PoReader2.getContentState(m);
      assertThat(actual1, is(ContentState.New));
   }

   // FIXME test where plurals < nplurals
   //   public void testContentStateNewPluralTooFew()
   //   {
   //      // TODO set nplurals=2
   //      Message m = new Message();
   //      m.setMsgidPlural("plural");
   //      m.addMsgstrPlural("s0", 0);
   //      ContentState actual1 = PoReader2.getContentState(m);
   //      assertThat(actual1, is(ContentState.New));
   //   }

   public void testContentStateNeedReviewSingle()
   {
      Message m = new Message();
      m.setFuzzy(true);
      m.setMsgstr("s");
      ContentState actual1 = PoReader2.getContentState(m);
      assertThat(actual1, is(ContentState.NeedReview));
   }

   public void testContentStateNeedReviewPlural1()
   {
      Message m = new Message();
      m.setFuzzy(true);
      m.setMsgidPlural("plural");
      m.addMsgstrPlural("s", 0);
      ContentState actual1 = PoReader2.getContentState(m);
      assertThat(actual1, is(ContentState.NeedReview));
   }

   public void testContentStateNeedReviewPlural2()
   {
      Message m = new Message();
      m.setFuzzy(true);
      m.setMsgidPlural("plural");
      m.addMsgstrPlural("", 0);
      m.addMsgstrPlural("s1", 1);
      ContentState actual1 = PoReader2.getContentState(m);
      assertThat(actual1, is(ContentState.NeedReview));
   }

   public void testContentStateNeedReviewPlural3()
   {
      Message m = new Message();
      m.setFuzzy(true);
      m.setMsgidPlural("plural");
      m.addMsgstrPlural("s0", 0);
      m.addMsgstrPlural("s1", 1);
      ContentState actual1 = PoReader2.getContentState(m);
      assertThat(actual1, is(ContentState.NeedReview));
   }

}

