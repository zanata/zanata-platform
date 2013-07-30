package org.zanata.rest.service;

import static org.zanata.model.tm.TransMemoryUnit.tu;
import static org.zanata.model.tm.TransMemoryUnitVariant.tuv;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.ws.rs.core.StreamingOutput;
import javax.xml.XMLConstants;

import nu.xom.Attribute;
import nu.xom.Element;

import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.model.tm.TMXMetadataHelper;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.tm.TransMemory;
import org.zanata.model.tm.TransMemoryUnitVariant;
import org.zanata.util.TMXUtils;

import com.google.common.collect.Lists;

public class ExportTransUnitTest extends TMXStreamingOutputTest
{

   @Test
   @org.junit.Test
   public void exportTransMemory() throws Exception
   {
      StreamingOutput output = streamSourceContents();
      checkAllLocales(output);
   }

   private TMXStreamingOutput<TransMemoryUnit> streamSourceContents()
   {
      return new TMXStreamingOutput<TransMemoryUnit>(createTestData(), new TransMemoryExportTMXStrategy(createTM()));
   }

   private TransMemory createTM()
   {
      Date now = new Date();
      TransMemory tm = new TransMemory();
      Element headerElem = newTmxElement("header");
      headerElem.addAttribute(new Attribute("xml:lang", XMLConstants.XML_NS_URI, sourceLocale.getId()));
      addCustomProperty(headerElem);
      TMXMetadataHelper.setMetadata(tm, headerElem);
      tm.setSourceLanguage(sourceLocale.getId());
      tm.setCreationDate(now);
      tm.setLastChanged(now);
      return tm;
   }

   private @Nonnull Iterator<TransMemoryUnit> createTestData()
   {
      TransMemory tm = null;
      String fr = LocaleId.FR.getId();
      String de = LocaleId.DE.getId();
      String sourceLoc = sourceLocale.getId();
      ArrayList<TransMemoryUnit> tuList = Lists.<TransMemoryUnit>newArrayList(
            tu(
                  tm,
                  "doc0:resId0",
                  "doc0:resId0",
                  sourceLoc,
                  "<seg>source0</seg>",
                  tuv(fr, "<seg>targetFR0</seg>"),
                  tuv(de, "<seg>targetDE0</seg>")),
            tu(
                  tm,
                  "doc0:resId1",
                  "doc0:resId1",
                  sourceLoc,
                  "<seg>SOURCE0</seg>",
                  tuv(fr, "<seg>TARGETfr0</seg>")),
            tu(
                  tm,
                  "doc1:resId0",
                  "doc1:resId0",
                  sourceLoc,
                  "<seg>source0</seg>",
                  tuv(fr, "<seg>targetFR0</seg>")),
            tu(
                  tm,
                  "doc1:resId1",
                  "doc1:resId1",
                  sourceLoc,
                  "<seg>SOURCE0</seg>",
                  tuv(de, "<seg>TARGETde0</seg>")));
      addMetadata(tuList, sourceLoc);
      return tuList.iterator();
   }

   private static void addMetadata(ArrayList<TransMemoryUnit> tuList, String sourceLoc)
   {
      for (TransMemoryUnit tu : tuList)
      {
         Element tuElem = newTmxElement("tu");
         tuElem.addAttribute(new Attribute("creationid", "TU_CREATOR"));
         addCustomProperty(tuElem);
         TMXMetadataHelper.setMetadata(tu, tuElem, sourceLoc);
         for (TransMemoryUnitVariant tuv : tu.getTransUnitVariants().values())
         {
            Element tuvElem = newTmxElement("tuv");
            tuvElem.addAttribute(new Attribute("creationid", "TUV_CREATOR"));
            addCustomProperty(tuvElem);
            TMXMetadataHelper.setMetadata(tuv, tuvElem);
         }
      }
   }

   private static void addCustomProperty(Element elem)
   {
      Element prop = newTmxElement("prop");
      prop.addAttribute(new Attribute("type", "custom_property"));
      prop.appendChild("property_value");
      elem.appendChild(prop);
   }

   private static Element newTmxElement(String localName)
   {
      return new Element("tmx:"+localName, TMXUtils.TMX14_NAMESPACE);
   }

}
