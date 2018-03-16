package org.zanata.rest.service;

import static org.zanata.common.ContentState.Approved;

import java.util.Iterator;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.ws.rs.core.StreamingOutput;

import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.model.ITextFlow;
import org.zanata.model.ITextFlowTarget;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ExportTranslationsTest extends TMXStreamingOutputTest {

    private final char deviceControlCharacter = 0x12;

    @Test
    public void exportAllLocales() throws Exception {
        StreamingOutput output = streamSourceContents(null, null);
        checkAllLocales(output);
    }

    @Test
    public void exportFrench() throws Exception {
        StreamingOutput output = streamSourceContents(LocaleId.EN_US, LocaleId.FR);
        checkFrench(output);
    }

    @Test
    public void exportGerman() throws Exception {
        StreamingOutput output = streamSourceContents(LocaleId.EN_US, LocaleId.DE);
        checkGerman(output);
    }

    @Override
    boolean expectAttributes() {
        return false;
    }

    @Override
    boolean expectProperties() {
        return false;
    }

    private TMXStreamingOutput<ITextFlow> streamSourceContents(
            LocaleId srcLocale, LocaleId targetLocale) {
        return TMXStreamingOutput.testInstance(createTestData(),
                new TranslationsTMXExportStrategy<>(srcLocale, targetLocale));
    }

    private @Nonnull
    Iterator<ITextFlow> createTestData() {
        LocaleId fr = LocaleId.FR;
        LocaleId de = LocaleId.DE;
        return Lists.<ITextFlow> newArrayList(
                new SimpleTextFlow("doc0:resId0", toMap(
                        new SimpleTextFlowTarget(fr, Approved, "targetFR0",
                                "targetFR1"), new SimpleTextFlowTarget(de,
                                Approved, "targetDE0", "targetDE1")),
                        sourceLocale, "source0", "source1"),
                new SimpleTextFlow("doc0:resId1",
                        toMap(new SimpleTextFlowTarget(fr, Approved,
                                "TARGETfr0", "TARGETfr1")), sourceLocale,
                        "SOURCE0", "SOURCE1"),
                new SimpleTextFlow("doc1:resId0", toMap(
                        new SimpleTextFlowTarget(fr, Approved, "\ntargetFR0  ",
                                "targetFR1"),
                        // NULL contents to be skipped:
                        new SimpleTextFlowTarget(de, Approved, "target\0DE0",
                                "targetDE1")), sourceLocale, "\nsource0  ",
                        "source1"),
                new SimpleTextFlow("doc1:resId1",
                        toMap(new SimpleTextFlowTarget(de, Approved,
                                // Control characters are discarded
                                "TARGETde0".concat(String
                                        .valueOf(deviceControlCharacter)),
                                "TARGETde1")), sourceLocale,
                        "SOURCE0", "SOURCE1"),
                new SimpleTextFlow("doc1:resId2",
                        toMap(new SimpleTextFlowTarget(de, Approved,
                                "TARGETde0", "TARGETde1")), sourceLocale,
                        // NULL contents to be skipped:
                        "SOURCE\00", "SOURCE1")).iterator();
    }

    private Map<LocaleId, ITextFlowTarget> toMap(
            SimpleTextFlowTarget... targetContents) {
        Map<LocaleId, ITextFlowTarget> map = Maps.newHashMap();
        for (SimpleTextFlowTarget target : targetContents) {
            map.put(target.getLocaleId(), target);
        }
        return map;
    }

}
