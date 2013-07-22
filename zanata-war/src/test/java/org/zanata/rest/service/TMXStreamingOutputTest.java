package org.zanata.rest.service;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.zanata.common.ContentState.Approved;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.WriterOutputStream;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.Validator;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.testng.annotations.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zanata.common.LocaleId;
import org.zanata.model.SimpleSourceContents;
import org.zanata.model.SimpleTargetContents;
import org.zanata.model.SourceContents;
import org.zanata.model.TargetContents;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class TMXStreamingOutputTest 
{
   static
   {
      // Tell XMLUnit about the offical xml: namespace (as used in xml:lang)
      NamespaceContext ctx = new SimpleNamespaceContext(ImmutableMap.of("xml", "http://www.w3.org/XML/1998/namespace"));
      XMLUnit.setXpathNamespaceContext(ctx);
   }
   private LocaleId sourceLocale = LocaleId.EN;

   @Test
   public void exportAllLocales() throws Exception
   {
      LocaleId targetLocale = null;
      StreamingOutput output = new TMXStreamingOutput(createTestData(), targetLocale, new ExportTUStrategy(targetLocale));

      Document doc = writeToXmlWithValidation(output);

      assertContainsFrenchTUs(doc);
      assertContainsGermanTUs(doc);
   }

   @Test
   public void exportFrench() throws Exception
   {
      LocaleId targetLocale = LocaleId.FR;
      StreamingOutput output = new TMXStreamingOutput(createTestData(), targetLocale, new ExportTUStrategy(targetLocale));

      Document doc = writeToXmlWithValidation(output);

      assertContainsFrenchTUs(doc);
      assertTUAbsent("doc1", "resId1", doc);
      assertLangAbsent("de", doc);
   }

   @Test
   public void exportGerman() throws Exception
   {
      LocaleId targetLocale = LocaleId.DE;
      StreamingOutput output = new TMXStreamingOutput(createTestData(), targetLocale, new ExportTUStrategy(targetLocale));

      Document doc = writeToXmlWithValidation(output);

      assertContainsGermanTUs(doc);
      assertTUAbsent("doc0", "resId1", doc);
      assertTUAbsent("doc1", "resId0", doc);

      assertLangAbsent("fr", doc);
   }

   private @Nonnull Iterator<SourceContents> createTestData()
   {
      LocaleId fr = LocaleId.FR;
      LocaleId de = LocaleId.DE;
      return Lists.<SourceContents>newArrayList(
            new SimpleSourceContents(
                  "doc0:resId0",
                  toMap(new SimpleTargetContents(fr, Approved, "targetFR0", "targetFR1"),
                        new SimpleTargetContents(de, Approved, "targetDE0", "targetDE1")),
                  sourceLocale,
                  "source0", "source1"
                  ),
            new SimpleSourceContents(
                  "doc0:resId1",
                  toMap(new SimpleTargetContents(fr, Approved, "TARGETfr0", "TARGETfr1")),
                  sourceLocale,
                  "SOURCE0", "SOURCE1"
                  ),
            new SimpleSourceContents(
                  "doc1:resId0",
                  toMap(new SimpleTargetContents(fr, Approved, "targetFR0", "targetFR1"),
                        // NULL contents to be skipped:
                        new SimpleTargetContents(de, Approved, "target\0DE0", "targetDE1")),
                  sourceLocale,
                  "source0", "source1"
                  ),
            new SimpleSourceContents(
                  "doc1:resId1",
                  toMap(new SimpleTargetContents(de, Approved, "TARGETde0", "TARGETde1")),
                  sourceLocale,
                  "SOURCE0", "SOURCE1"
                  ),
            new SimpleSourceContents(
                  "doc1:resId2",
                  toMap(new SimpleTargetContents(de, Approved, "TARGETde0", "TARGETde1")),
                  sourceLocale,
                  // NULL contents to be skipped:
                  "SOURCE\00", "SOURCE1"
                  )).iterator();
   }

   private Map<LocaleId, TargetContents> toMap(SimpleTargetContents... targetContents)
   {
      Map<LocaleId, TargetContents> map = Maps.newHashMap();
      for (SimpleTargetContents target : targetContents)
      {
         map.put(target.getLocaleId(), target);
      }
      return map;
   }

   private void assertContainsFrenchTUs(Document doc) throws XpathException, SAXException, IOException
   {
      assertSingleTU("doc0", "resId0", doc);
      assertTUContainsSegment("source0", "doc0", "resId0", "en", doc);
      assertTUContainsSegment("targetFR0", "doc0", "resId0", "fr", doc);
      assertSingleTU("doc0", "resId1", doc);
      assertTUContainsSegment("SOURCE0", "doc0", "resId1", "en", doc);
      assertTUContainsSegment("TARGETfr0", "doc0", "resId1", "fr", doc);

      assertSingleTU("doc1", "resId0", doc);
      assertTUContainsSegment("source0", "doc1", "resId0", "en", doc);
      assertTUContainsSegment("targetFR0", "doc1", "resId0", "fr", doc);
   }

   private void assertContainsGermanTUs(Document doc) throws XpathException, SAXException, IOException
   {
      assertSingleTU("doc0", "resId0", doc);
      assertTUContainsSegment("source0", "doc0", "resId0", "en", doc);
      assertTUContainsSegment("targetDE0", "doc0", "resId0", "de", doc);

      assertSingleTU("doc1", "resId1", doc);
      assertTUContainsSegment("SOURCE0", "doc1", "resId1", "en", doc);
      assertTUContainsSegment("TARGETde0", "doc1", "resId1", "de", doc);
   }

   @SuppressWarnings("deprecation") // Eclipse seems to confuse org.junit with junit.framework
   private void assertSingleTU(String docId, String resId, Document doc) throws XpathException, SAXException, IOException
   {
      String xpath = "//tu[@tuid='"+docId+":"+resId+"']";
      XpathEngine simpleXpathEngine = XMLUnit.newXpathEngine();
      NodeList nodeList = simpleXpathEngine.getMatchingNodes(xpath, doc);
      int matches = nodeList.getLength();
      assertEquals("Should be only one tu node per docId:resId", 1, matches);
      Node srclang = nodeList.item(0).getAttributes().getNamedItem("srclang");
      assertEquals(sourceLocale.getId(), srclang.getNodeValue());
   }

   private static void assertTUContainsSegment(String segmentText, String docId, String resId, String lang, Document doc) throws XpathException, SAXException, IOException
   {
      assertXpathEvaluatesTo(segmentText, "//tu[@tuid='"+docId+":"+resId+"']/tuv[@xml:lang='"+lang+"']/seg/text()", doc);
   }

   private static void assertTUAbsent(String docId, String resId, Document doc) throws XpathException, SAXException, IOException
   {
      assertXpathNotExists("//tu[@tuid='"+docId+":"+resId+"']", doc);
   }

   private static void assertLangAbsent(String lang, Document doc) throws XpathException, SAXException, IOException
   {
      assertXpathNotExists("//tuv[@xml:lang='"+lang+"']", doc);
   }

   private Document writeToXmlWithValidation(StreamingOutput output) throws IOException, SAXException
   {
      StringBuilderWriter sbWriter = new StringBuilderWriter();
      WriterOutputStream writerOutputStream = new WriterOutputStream(sbWriter);
      output.write(writerOutputStream);
      writerOutputStream.close();
      String xml = sbWriter.toString();
      assertValidTMX(xml);
      Document doc = XMLUnit.buildControlDocument(xml);
      return doc;
   }

   private void assertValidTMX(String xml) throws MalformedURLException, SAXException
   {
      StringReader reader = new StringReader(xml);
      String systemID = getClass().getResource("/org/zanata/xml/tmx14.dtd").toString();
      String doctype = "tmx";
      Validator v = new Validator(reader, systemID, doctype);
      assertTrue(v.toString(), v.isValid());
   }
}
