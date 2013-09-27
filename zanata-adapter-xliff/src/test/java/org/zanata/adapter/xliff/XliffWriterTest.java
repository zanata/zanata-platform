package org.zanata.adapter.xliff;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.io.File;
import java.io.FileNotFoundException;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.adapter.xliff.XliffCommon.ValidationType;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;

@Test(groups = { "unit-tests" })
public class XliffWriterTest {
    private String testDir = "src/test/resources/";
    private String generateDir = "target/xliffGenerate/";
    private String generatedDocName = "Generated_StringResource";
    private String generatedDocFileName = generatedDocName + "_en_US.xml";

    private XliffReader reader;

    @BeforeMethod
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

        assertThat(firstTextFlow.getContents(),
                equalTo(asList("Translation Unit 1")));
        assertThat(secondTextFlow.getContents(),
                equalTo(asList("Translation Unit 4 (4 < 5 & 4 > 3)")));
        assertThat(lastTextFlow.getContents(),
                equalTo(asList(" Translation Unit 5 (4 < 5 & 4 > 3) ")));
    }

    @Test
    public void extractSizeTest() throws FileNotFoundException {
        prepareTemplateDoc();

        File generatedFile = new File(generateDir, "/" + generatedDocFileName);
        Resource doc =
                reader.extractTemplate(generatedFile, LocaleId.EN_US,
                        generatedDocName, ValidationType.XSD.toString());

        assertThat(doc.getTextFlows().size(), is(7));
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
}
