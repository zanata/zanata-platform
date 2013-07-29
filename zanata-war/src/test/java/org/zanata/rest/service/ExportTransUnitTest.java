package org.zanata.rest.service;

import static org.zanata.model.tm.TransMemoryUnit.tu;
import static org.zanata.model.tm.TransMemoryUnitVariant.tuv;

import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.ws.rs.core.StreamingOutput;

import org.testng.annotations.Test;
import org.zanata.common.LocaleId;
import org.zanata.model.tm.TransMemoryUnit;
import org.zanata.model.tm.TransMemory;

import com.google.common.collect.Lists;

public class ExportTransUnitTest extends TMXStreamingOutputTest
{

   @Test
   public void exportTransMemory() throws Exception
   {
      StreamingOutput output = streamSourceContents();
      checkAllLocales(output);
   }

   private TMXStreamingOutput<TransMemoryUnit> streamSourceContents()
   {
      return new TMXStreamingOutput<TransMemoryUnit>(createTestData(), new ExportTMXTransUnitStrategy());
   }

   private @Nonnull Iterator<TransMemoryUnit> createTestData()
   {
      TransMemory tm = null;
      String fr = LocaleId.FR.getId();
      String de = LocaleId.DE.getId();
      String sourceLoc = sourceLocale.getId();
      return Lists.<TransMemoryUnit>newArrayList(
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
                  tuv(de, "<seg>TARGETde0</seg>"))).iterator();
   }

}
