package org.zanata.adapter.glossary;

import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.service.GlossaryResource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class GlossaryCSVWriterTest extends AbstractGlossaryWriterTest {

    @Test
    public void glossaryWriteTest1() throws IOException {
        GlossaryCSVWriter writer = new GlossaryCSVWriter();
        String filePath = "target/output.csv";

        FileWriter fileWriter = new FileWriter(filePath);
        LocaleId srcLocale = LocaleId.EN_US;

        List<GlossaryEntry> entries = new ArrayList<>();
        GlossaryEntry entry1 =
            generateGlossaryEntry(srcLocale, "pos", "desc");
        entry1.getGlossaryTerms().add(generateGlossaryTerm("1.content-en-us", LocaleId.EN_US));
        entry1.getGlossaryTerms().add(generateGlossaryTerm("1.content-de", LocaleId.DE));
        entry1.getGlossaryTerms().add(generateGlossaryTerm("1.content-es", LocaleId.ES));
        entries.add(entry1);

        GlossaryEntry entry2 =
            generateGlossaryEntry(srcLocale, "pos", "desc");
        entry2.getGlossaryTerms().add(generateGlossaryTerm("2.content-en-us", LocaleId.EN_US));
        entry2.getGlossaryTerms().add(generateGlossaryTerm("2.content-de", LocaleId.DE));
        entry2.getGlossaryTerms().add(generateGlossaryTerm("2.content-es", LocaleId.ES));
        entries.add(entry2);

        GlossaryEntry entry3 =
            generateGlossaryEntry(srcLocale, "pos", "desc");
        entry3.getGlossaryTerms().add(generateGlossaryTerm("3.content-en-us", LocaleId.EN_US));
        entry3.getGlossaryTerms().add(generateGlossaryTerm("3.content-de", LocaleId.DE));
        entry3.getGlossaryTerms().add(generateGlossaryTerm("3.content-es", LocaleId.ES));
        entries.add(entry3);

        List<LocaleId> transLocales = new ArrayList<>();
        transLocales.add(LocaleId.DE);
        transLocales.add(LocaleId.ES);

        writer.write(fileWriter, entries, srcLocale, transLocales);

        GlossaryCSVReader reader = new GlossaryCSVReader(srcLocale);
        File sourceFile = new File(filePath);

        Reader inputStreamReader =
            new InputStreamReader(new FileInputStream(sourceFile), "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);

        Map<LocaleId, List<GlossaryEntry>> glossaries = reader
                .extractGlossary(br, GlossaryResource.GLOBAL_QUALIFIED_NAME);
        br.close();
        assertThat(glossaries).hasSize(2);
        assertThat(glossaries.get(LocaleId.DE)).hasSize(3);
    }
}
