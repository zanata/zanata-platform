package org.zanata.rest.service;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.ws.rs.core.StreamingOutput;

import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.model.tm.TMTransUnitVariant;
import org.zanata.model.tm.TMTranslationUnit;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ExportTransUnitTest extends TMXStreamingOutputTest
{

   @Test
   public void exportAllLocales() throws Exception
   {
      StreamingOutput output = streamSourceContents(null);
      checkAllLocales(output);
   }

   @Test
   public void exportFrench() throws Exception
   {
      StreamingOutput output = streamSourceContents(LocaleId.FR.getId());
      checkFrench(output);
   }

   @Test
   public void exportGerman() throws Exception
   {
      StreamingOutput output = streamSourceContents(LocaleId.DE.getId());

      checkGerman(output);
   }

   private TMXStreamingOutput<TMTranslationUnit> streamSourceContents(String targetLocale)
   {
      return new TMXStreamingOutput<TMTranslationUnit>(createTestData(), new ExportTransUnitStrategy(targetLocale));
   }

   private @Nonnull Iterator<TMTranslationUnit> createTestData()
   {
      String fr = LocaleId.FR.getId();
      String de = LocaleId.DE.getId();
      String sourceLoc = sourceLocale.getId();
      return Lists.<TMTranslationUnit>newArrayList(
            new TMTranslationUnit(
                  "doc0:resId0",
                  sourceLoc,
                  "source0",
                  toMap(new TMTransUnitVariant(fr, "targetFR0"),
                        new TMTransUnitVariant(de, "targetDE0"))
                  ),
            new TMTranslationUnit(
                  "doc0:resId1",
                  sourceLoc,
                  "SOURCE0",
                  toMap(new TMTransUnitVariant(fr, "TARGETfr0"))
                  ),
            new TMTranslationUnit(
                  "doc1:resId0",
                  sourceLoc,
                  "source0",
                  toMap(new TMTransUnitVariant(fr, "targetFR0"))
                  ),
            new TMTranslationUnit(
                  "doc1:resId1",
                  sourceLoc,
                  "SOURCE0",
                  toMap(new TMTransUnitVariant(de, "TARGETde0"))
                  )).iterator();
   }

   private Map<String, TMTransUnitVariant> toMap(TMTransUnitVariant... targetContents)
   {
      Map<String, TMTransUnitVariant> map = Maps.newHashMap();
      for (TMTransUnitVariant target : targetContents)
      {
         map.put(target.getLanguage(), target);
      }
      return map;
   }

}
