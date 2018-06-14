package org.zanata.adapter.xliff;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.FileNotFoundException;

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
    private String testDir = "src/test/resources/";
    private String generateDir = "target/xliffGenerate/";
    private String generatedDocName = "Generated_StringResource";
    private String generatedDocFileName = generatedDocName + "_en_US.xml";

    private XliffReader reader;

    @Before
    public void beforeMethod() {
        reader = new XliffReader();
    }

    @Test
    public void checkTransUnit() throws FileNotFoundException {
        prepareTemplateDoc();

        File generatedFile = new File(generateDir, "/" + generatedDocFileName);
        Resource doc =
                reader.extractTemplate(generatedFile, LocaleId.EN_US,
                        generatedDocName, ValidationType.XSD.toString());

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

        File generatedFile = new File(generateDir, "/" + generatedDocFileName);
        Resource doc =
                reader.extractTemplate(generatedFile, LocaleId.EN_US,
                        generatedDocName, ValidationType.XSD.toString());

        assertThat(doc.getTextFlows().size()).isEqualTo(7);
    }

    private void prepareTemplateDoc() throws FileNotFoundException {
        String docName = "StringResource_en_US.xml";

        File file = new File(testDir, "/" + docName);
        Resource doc =
                reader.extractTemplate(file, LocaleId.EN_US, docName,
                        ValidationType.XSD.toString());
        doc.setName(generatedDocName);

        XliffWriter.write(new File(generateDir), doc, "en-US");
    }

    @Test
    public void testWriteXliff() throws Exception {

        Resource doc = new Resource("hello");
        doc.getTextFlows()
                .add(new TextFlow("first", LocaleId.EN_US, "first text"));
        TranslationsResource translationsResource = new TranslationsResource();

        TextFlowTarget target = new TextFlowTarget("first");
        target.setContents("第一个");
        target.setState(ContentState.Translated);
        translationsResource.getTextFlowTargets().add(target);
        XliffWriter
                .writeFile(new File(generateDir, "xliff_writer.xml"), doc, "zh",
                        translationsResource, true);
    }
}
