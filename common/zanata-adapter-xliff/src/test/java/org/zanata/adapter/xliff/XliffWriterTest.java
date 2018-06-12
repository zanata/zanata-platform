package org.zanata.adapter.xliff;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.zanata.adapter.xliff.XliffCommon.ValidationType;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

public class XliffWriterTest {
    private static final String TEST_RESOURCES = "src/test/resources/";
    private static final File GENERATE_DIR = new File("target/test-output/");
    private static final String GENERATED_DOC_NAME =
            "Generated_StringResource";
    private static final String GENERATED_FILE_NAME =
            GENERATED_DOC_NAME + "_en_US.xml";
    private static final File GENERATED_FILE =
            new File(GENERATE_DIR, GENERATED_FILE_NAME);

    private XliffReader reader;

    @Before
    public void beforeMethod() {
        GENERATE_DIR.mkdirs();
        reader = new XliffReader();
    }

    @Test
    public void checkTransUnit() throws FileNotFoundException {
        prepareTemplateDoc();

        Resource doc =
                reader.extractTemplate(GENERATED_FILE, LocaleId.EN_US,
                        GENERATED_DOC_NAME, ValidationType.XSD.toString());

        TextFlow firstTextFlow = doc.getTextFlows().get(0);
        TextFlow secondTextFlow =
                doc.getTextFlows().get(doc.getTextFlows().size() - 2);
        TextFlow lastTextFlow =
                doc.getTextFlows().get(doc.getTextFlows().size() - 1);

        assertThat(firstTextFlow.getContents()).isEqualTo(asList("Translation Unit 1"));
        assertThat(secondTextFlow.getContents()).isEqualTo(asList("Translation Unit 4 (4 < 5 & 4 > 3)"));
        assertThat(lastTextFlow.getContents()).isEqualTo(asList(" Translation Unit 5 (4 < 5 & 4 > 3) "));
    }

    @Test
    public void extractSizeTest() throws FileNotFoundException {
        prepareTemplateDoc();

        Resource doc =
                reader.extractTemplate(GENERATED_FILE, LocaleId.EN_US,
                        GENERATED_DOC_NAME, ValidationType.XSD.toString());

        assertThat(doc.getTextFlows().size()).isEqualTo(7);
    }

    private void prepareTemplateDoc() throws FileNotFoundException {
        String docName = "StringResource_en_US.xml";

        File file = new File(TEST_RESOURCES, docName);
        Resource doc =
                reader.extractTemplate(file, LocaleId.EN_US, docName,
                        ValidationType.XSD.toString());
        doc.setName(GENERATED_DOC_NAME);

        // this creates generatedFile
        XliffWriter.write(GENERATE_DIR, doc, "en-US");
    }

    @Test
    public void writeXliffTranslationsWithTranslated() throws Exception {
        Resource doc = sourceDocument();
        TranslationsResource translationsResource = translatedDocument();
        File xlfFile = new File(GENERATE_DIR, "xliff_writer1.xml");
        xlfFile.delete();
        XliffWriter
                .writeFile(xlfFile, doc, "zh",
                        translationsResource, true, false);
        assertThat(xlfFile).exists();
        String contents = FileUtils.readFileToString(xlfFile, UTF_8);
        File expected = new File(TEST_RESOURCES, "xliffTranslationsWithTranslated.xlf");
        assertThat(contents).isXmlEqualToContentOf(expected);
    }

    @Test
    public void writeXliffTranslationsWithApprovedOnly() throws Exception {
        Resource doc = sourceDocument();
        TranslationsResource translationsResource = translatedDocument();
        File xlfFile = new File(GENERATE_DIR, "xliff_writer2.xml");
        xlfFile.delete();
        XliffWriter
                .writeFile(xlfFile, doc, "zh",
                        translationsResource, true, true);
        assertThat(xlfFile).exists();
        String contents = FileUtils.readFileToString(xlfFile, UTF_8);
        File expected = new File("src/test/resources/xliffTranslationsWithApprovedOnly.xlf");
        assertThat(contents).isXmlEqualToContentOf(expected);
    }

    private Resource sourceDocument() {
        Resource doc = new Resource("hello");
        doc.getTextFlows()
                .add(new TextFlow("first", LocaleId.EN_US, "first text"));
        doc.getTextFlows()
                .add(new TextFlow("second", LocaleId.EN_US, "second text"));
        return doc;
    }

    private TranslationsResource translatedDocument() {
        TranslationsResource translationsResource = new TranslationsResource();
        TextFlowTarget tft1 = new TextFlowTarget("first");
        tft1.setContents("第一个");
        tft1.setState(ContentState.Approved);
        translationsResource.getTextFlowTargets().add(tft1);
        TextFlowTarget tft2 = new TextFlowTarget("second");
        tft2.setContents("第二");
        tft2.setState(ContentState.Translated);
        translationsResource.getTextFlowTargets().add(tft2);
        return translationsResource;
    }
}
