/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.rest.service;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.custommonkey.xmlunit.XMLAssert.assertXpathNotExists;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;

import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.io.output.StringBuilderWriter;
import org.apache.commons.io.output.WriterOutputStream;
import org.custommonkey.xmlunit.NamespaceContext;
import org.custommonkey.xmlunit.SimpleNamespaceContext;
import org.custommonkey.xmlunit.Validator;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.XpathEngine;
import org.custommonkey.xmlunit.exceptions.XpathException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zanata.common.LocaleId;

import com.google.common.collect.ImmutableMap;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
abstract class TMXStreamingOutputTest
{
   static
   {
      // Tell XMLUnit about the offical xml: namespace (as used in xml:lang)
      NamespaceContext ctx = new SimpleNamespaceContext(ImmutableMap.of("xml", "http://www.w3.org/XML/1998/namespace"));
      XMLUnit.setXpathNamespaceContext(ctx);
   }

   LocaleId sourceLocale = LocaleId.EN;
   XpathEngine simpleXpathEngine = XMLUnit.newXpathEngine();

   abstract boolean expectAttributes();
   abstract boolean expectProperties();

   void checkAllLocales(StreamingOutput output) throws IOException, SAXException, XpathException
   {
      Document doc = writeToXmlWithValidation(output);

      assertContainsFrenchTUs(doc);
      assertContainsGermanTUs(doc);
   }

   void checkFrench(StreamingOutput output) throws IOException, SAXException, XpathException
   {
      Document doc = writeToXmlWithValidation(output);

      assertContainsFrenchTUs(doc);
      assertTUAbsent("doc1", "resId1", doc);
      assertLangAbsent("de", doc);
   }

   void checkGerman(StreamingOutput output) throws IOException, SAXException, XpathException
   {
      Document doc = writeToXmlWithValidation(output);

      assertContainsGermanTUs(doc);
      assertTUAbsent("doc0", "resId1", doc);
      assertTUAbsent("doc1", "resId0", doc);

      assertLangAbsent("fr", doc);
   }

   @SuppressWarnings("deprecation")
   void assertSingleTU(String docId, String resId, Document doc) throws XpathException, SAXException, IOException
   {
      String xpathTU = "//tu[@tuid='"+docId+":"+resId+"']";
      NodeList nodeList = simpleXpathEngine.getMatchingNodes(xpathTU, doc);
      int matches = nodeList.getLength();
      assertEquals("Should be one tu node per docId:resId", 1, matches);
      Node tuNode = nodeList.item(0);
      Node srclang = tuNode.getAttributes().getNamedItem("srclang");
      assertEquals(sourceLocale.getId(), srclang.getNodeValue());

      String xpathTUV = xpathTU+"/tuv[@xml:lang='"+sourceLocale.getId()+"']";
      NodeList tuvNodes = simpleXpathEngine.getMatchingNodes(xpathTUV, doc);
      int tuvMatches = tuvNodes.getLength();
      assertEquals("Should be exactly one tuv node for srclang", 1, tuvMatches);

      if (expectAttributes())
      {
         String xpathAttr = xpathTU+"/@creationid";
         assertXpathEvaluatesTo("TU_CREATOR", xpathAttr, doc);
      }
      if (expectProperties())
      {
         String xpathProp = xpathTU+"/prop[@type='custom_property']/text()";
         assertXpathEvaluatesTo("property_value", xpathProp, doc);
      }
   }

   void assertTUContainsSegment(String segmentText, String docId, String resId, String lang, Document doc) throws XpathException, SAXException, IOException
   {
      String xpathTUV = "//tu[@tuid='"+docId+":"+resId+"']/tuv[@xml:lang='"+lang+"']";
      if (expectAttributes())
      {
         String xpathAttr = xpathTUV+"/@creationid";
         assertXpathEvaluatesTo("TUV_CREATOR", xpathAttr, doc);
      }
      if (expectProperties())
      {
         String xpathProp = xpathTUV+"/prop[@type='custom_property']/text()";
         assertXpathEvaluatesTo("property_value", xpathProp, doc);
      }

      assertXpathEvaluatesTo(segmentText, xpathTUV + "/seg/text()", doc);
   }

   static void assertTUAbsent(String docId, String resId, Document doc) throws XpathException, SAXException, IOException
   {
      assertXpathNotExists("//tu[@tuid='"+docId+":"+resId+"']", doc);
   }

   static void assertLangAbsent(String lang, Document doc) throws XpathException, SAXException, IOException
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
      System.out.println(xml);
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
      // Invalid: Okapi puts seg before note and prop when outputting tuv!
      assertTrue(v.toString(), v.isValid());
   }

   void assertContainsFrenchTUs(Document doc) throws XpathException, SAXException, IOException
   {
      assertSingleTU("doc0", "resId0", doc);
      assertTUContainsSegment("source0", "doc0", "resId0", "en", doc);
      assertTUContainsSegment("targetFR0", "doc0", "resId0", "fr", doc);
      assertSingleTU("doc0", "resId1", doc);
      assertTUContainsSegment("SOURCE0", "doc0", "resId1", "en", doc);
      assertTUContainsSegment("TARGETfr0", "doc0", "resId1", "fr", doc);

      assertSingleTU("doc1", "resId0", doc);
      assertTUContainsSegment("\nsource0  ", "doc1", "resId0", "en", doc);
      assertTUContainsSegment("\ntargetFR0  ", "doc1", "resId0", "fr", doc);
   }

   void assertContainsGermanTUs(Document doc) throws XpathException, SAXException, IOException
   {
      assertSingleTU("doc0", "resId0", doc);
      assertTUContainsSegment("source0", "doc0", "resId0", "en", doc);
      assertTUContainsSegment("targetDE0", "doc0", "resId0", "de", doc);

      assertSingleTU("doc1", "resId1", doc);
      assertTUContainsSegment("SOURCE0", "doc1", "resId1", "en", doc);
      assertTUContainsSegment("TARGETde0", "doc1", "resId1", "de", doc);
   }

}
