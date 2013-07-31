package org.zanata.rest.service;

import static org.zanata.common.ContentState.Approved;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.ws.rs.core.StreamingOutput;

import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.model.SimpleSourceContents;
import org.zanata.model.SimpleTargetContents;
import org.zanata.model.SourceContents;
import org.zanata.model.TargetContents;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ExportSourceContentsTest extends TMXStreamingOutputTest
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
      StreamingOutput output = streamSourceContents(LocaleId.FR);
      checkFrench(output);
   }

   @Test
   public void exportGerman() throws Exception
   {
      StreamingOutput output = streamSourceContents(LocaleId.DE);
      checkGerman(output);
   }

   @Override
   boolean expectAttributes()
   {
      return false;
   }

   @Override
   boolean expectProperties()
   {
      return false;
   }
   

   private TMXStreamingOutput<SourceContents> streamSourceContents(LocaleId targetLocale)
   {
      return new TMXStreamingOutput<SourceContents>(createTestData(), new TranslationsExportTMXStrategy(targetLocale));
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
                  toMap(new SimpleTargetContents(fr, Approved, "\ntargetFR0  ", "targetFR1"),
                        // NULL contents to be skipped:
                        new SimpleTargetContents(de, Approved, "target\0DE0", "targetDE1")),
                  sourceLocale,
                  "\nsource0  ", "source1"
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

}
