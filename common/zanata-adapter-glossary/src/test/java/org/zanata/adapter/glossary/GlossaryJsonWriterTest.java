package org.zanata.adapter.glossary;

import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.service.GlossaryResource;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class GlossaryJsonWriterTest extends AbstractGlossaryWriterTest {

    @Test
    public void glossaryWriteTest() throws IOException {
        GlossaryJsonWriter writer = new GlossaryJsonWriter();
        String filePath = "target/output.json";

        FileWriter fileWriter = new FileWriter(filePath);
        LocaleId srcLocale = LocaleId.EN_US;

        List<GlossaryEntry> entries = new ArrayList<>();
        GlossaryEntry entry1 =
            generateGlossaryEntry(srcLocale, "pos", "desc");
        entry1.setExternalId("1.content-en-us");
        entry1.getGlossaryTerms().add(generateGlossaryTerm("1.content-en-us", LocaleId.EN_US));
        entry1.getGlossaryTerms().add(generateGlossaryTerm("1.content-de", LocaleId.DE));
        entry1.getGlossaryTerms().add(generateGlossaryTerm("1.content-es", LocaleId.ES));
        entries.add(entry1);

        GlossaryEntry entry2 =
            generateGlossaryEntry(srcLocale, "pos", "desc");
        entry2.setExternalId("2.content-en-us");
        entry2.getGlossaryTerms().add(generateGlossaryTerm("2.content-en-us", LocaleId.EN_US));
        entry2.getGlossaryTerms().add(generateGlossaryTerm("2.content-de", LocaleId.DE));
        entry2.getGlossaryTerms().add(generateGlossaryTerm("2.content-es", LocaleId.ES));
        entries.add(entry2);

        GlossaryEntry entry3 =
            generateGlossaryEntry(srcLocale, "pos", "desc");
        entry3.setExternalId("3.content-en-us");
        entry3.getGlossaryTerms().add(generateGlossaryTerm("3.content-en-us", LocaleId.EN_US));
        entry3.getGlossaryTerms().add(generateGlossaryTerm("3.content-de", LocaleId.DE));
        entry3.getGlossaryTerms().add(generateGlossaryTerm("3.content-es", LocaleId.ES));
        entries.add(entry3);

        List<LocaleId> transLocales = new ArrayList<>();
        transLocales.add(LocaleId.DE);
        transLocales.add(LocaleId.ES);

        writer.write(fileWriter, entries, srcLocale, transLocales);

        GlossaryJsonReader reader = new GlossaryJsonReader(srcLocale);
        File sourceFile = new File(filePath);

        Reader inputStreamReader =
                new InputStreamReader(new FileInputStream(sourceFile), "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);

        Map<LocaleId, List<GlossaryEntry>> glossaries = reader
                .extractGlossary(br, GlossaryResource.GLOBAL_QUALIFIED_NAME);
        br.close();
        assertThat(glossaries).hasSize(1);
        assertThat(glossaries.get(LocaleId.EN_US)).hasSize(3);
        for (int entry = 0; entry < 3; ++entry) {
            GlossaryEntry thisEntry = glossaries.get(LocaleId.EN_US).get(entry);
            assertThat(thisEntry.getGlossaryTerms().get(0).getContent()
                    .equals(String.valueOf(entry).concat(".content-en-us")));
        }
    }
}
