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
import org.zanata.model.tm.TMMetadataType;
import org.zanata.model.tm.TMXMetadataHelper;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.tm.TransMemory;
import org.zanata.model.tm.TransMemoryUnitVariant;
import org.zanata.util.TMXConstants;
import org.zanata.util.TMXParseException;

import com.google.common.collect.Lists;

public class ExportTransMemoryTest extends TMXStreamingOutputTest
{

   @Test
   public void exportTransMemory() throws Exception
   {
      StreamingOutput output = streamSourceContents();
      checkAllLocales(output);
   }

   @Override
   boolean expectAttributes()
   {
      return true;
   }

   @Override
   boolean expectProperties()
   {
      return true;
   }
   
   private TMXStreamingOutput<TransMemoryUnit> streamSourceContents() throws TMXParseException
   {
      return TMXStreamingOutput.testInstance(createTestData(), new TransMemoryTMXExportStrategy(createTM()));
   }

   private TransMemory createTM() throws TMXParseException
   {
      Date now = new Date();
      TransMemory tm = new TransMemory();
      Element headerElem = newTmxElement("header");
      headerElem.addAttribute(new Attribute("adminlang", "en-US"));
      headerElem.addAttribute(new Attribute("datatype", "unknown"));
      headerElem.addAttribute(new Attribute("o-tmf", "OTMF"));
      headerElem.addAttribute(new Attribute("segtype", "paragraph"));
      addCustomProperty(headerElem, "prop1", "propval1");
      addCustomProperty(headerElem, "prop2", "propval2");
      TMXMetadataHelper.setMetadata(tm, headerElem);
      tm.setSourceLanguage(sourceLocale.getId());
      tm.setCreationDate(now);
      tm.setLastChanged(now);
      return tm;
   }

   private @Nonnull Iterator<TransMemoryUnit> createTestData() throws TMXParseException
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
                  "<seg>\nsource0  </seg>",
                  tuv(fr, "<seg>\ntargetFR0  </seg>")),
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

   private static void addMetadata(ArrayList<TransMemoryUnit> tuList, String sourceLoc) throws TMXParseException
   {
      for (TransMemoryUnit tu : tuList)
      {
         Element tuElem = newTmxElement("tu");
         tuElem.addAttribute(new Attribute("creationid", "TU_CREATOR"));
         addCustomProperty(tuElem, "prop1", "propval1");
         addCustomProperty(tuElem, "prop2", "propval2");
         TMXMetadataHelper.setMetadata(tu, tuElem, sourceLoc);
         for (TransMemoryUnitVariant tuv : tu.getTransUnitVariants().values())
         {
            Element tuvElem = newTmxElement("tuv");
            tuvElem.addAttribute(new Attribute("creationid", "TUV_CREATOR"));
            addCustomProperty(tuvElem, "prop1", "propval1");
            addCustomProperty(tuvElem, "prop2", "propval2");
            TMXMetadataHelper.setMetadata(tuv, tuvElem);
         }
      }
   }

   private static void addCustomProperty(Element elem, String propType, String value)
   {
      Element prop = newTmxElement("prop");
      prop.addAttribute(new Attribute("type", propType));
      prop.appendChild(value);
      elem.appendChild(prop);
   }

   private static Element newTmxElement(String localName)
   {
      return new Element("tmx:"+localName, TMXConstants.TMX14_NAMESPACE);
   }

}
