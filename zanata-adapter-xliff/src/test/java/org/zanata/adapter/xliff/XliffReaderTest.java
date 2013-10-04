package org.zanata.adapter.xliff;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import java.io.File;
import java.io.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.zanata.adapter.xliff.XliffCommon.ValidationType;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

@Test(groups = { "unit-tests" })
public class XliffReaderTest {
    private static final Logger log = LoggerFactory
            .getLogger(XliffReaderTest.class);

    private static final String TEST_DIR = "src/test/resources/";
    private static final String DOC_NAME = "StringResource_en_US.xml";
    private XliffReader reader;

    @BeforeTest
    public void resetReader() {
        reader = new XliffReader();
    }

    @Test
    public void extractTemplateSizeTest() throws FileNotFoundException {
        Resource doc = getTemplateDoc();

        assertThat(doc.getName(), equalTo(DOC_NAME));
        assertThat(doc.getTextFlows().size(), is(7));
    }

    @Test
    public void templateFirstAndSecondLastTextFlowTest()
            throws FileNotFoundException {
        Resource doc = getTemplateDoc();

        TextFlow firstTextFlow = doc.getTextFlows().get(0);
        TextFlow lastTextFlow =
                doc.getTextFlows().get(doc.getTextFlows().size() - 2);

        assertThat(firstTextFlow.getContents(),
                equalTo(asList("Translation Unit 1")));
        assertThat(lastTextFlow.getContents(),
                equalTo(asList("Translation Unit 4 (4 < 5 & 4 > 3)")));
    }

    @Test
    public void extractTargetSizeTest() throws FileNotFoundException {
        File fileTarget = new File(TEST_DIR, "/StringResource_de.xml");
        TranslationsResource tr = reader.extractTarget(fileTarget);
        // the file contains 4 trans-units, but one has no target element
        assertThat(tr.getTextFlowTargets().size(), is(4));
    }

    @Test
    public void targetFirstAndLastTextFlowTest() throws FileNotFoundException {
        File fileTarget = new File(TEST_DIR, "/StringResource_de.xml");
        TranslationsResource tr = reader.extractTarget(fileTarget);

        TextFlowTarget firstTextFlow = tr.getTextFlowTargets().get(0);
        TextFlowTarget lastTextFlow =
                tr.getTextFlowTargets().get(tr.getTextFlowTargets().size() - 2);

        assertThat(firstTextFlow.getContents(),
                equalTo(asList("Translation 1")));
        assertThat(lastTextFlow.getContents(),
                equalTo(asList("Translation 4 (4 < 5 & 4 > 3)")));
    }

    @Test
    public void leadingEndingWhiteSpaceTargetTest()
            throws FileNotFoundException {
        File fileTarget = new File(TEST_DIR, "/StringResource_de.xml");
        TranslationsResource tr = reader.extractTarget(fileTarget);

        TextFlowTarget lastTextFlow =
                tr.getTextFlowTargets().get(tr.getTextFlowTargets().size() - 1);
        assertThat(lastTextFlow.getContents(),
                equalTo(asList(" Leading and trailing white space ")));
        assertThat(lastTextFlow.getContents(),
                not(equalTo(asList("Leading and trailing white space"))));
        assertThat(lastTextFlow.getContents(),
                not(equalTo(asList(" Leading and trailing white space"))));
        assertThat(lastTextFlow.getContents(),
                not(equalTo(asList("Leading and trailing white space "))));
    }

    @Test
    public void leadingEndingWhiteSpaceSourceTest()
            throws FileNotFoundException {
        File fileTarget = new File(TEST_DIR, "/StringResource_de.xml");
        Resource resource =
                reader.extractTemplate(fileTarget, LocaleId.EN_US, null,
                        ValidationType.XSD.toString());

        TextFlow tf =
                resource.getTextFlows().get(resource.getTextFlows().size() - 1);
        assertThat(tf.getContents(),
                equalTo(asList(" Translation Unit 5 (4 < 5 & 4 > 3) ")));
        assertThat(tf.getContents(),
                not(equalTo(asList("Translation Unit 5 (4 < 5 & 4 > 3)"))));
        assertThat(tf.getContents(),
                not(equalTo(asList(" Translation Unit 5 (4 < 5 & 4 > 3)"))));
        assertThat(tf.getContents(),
                not(equalTo(asList("Translation Unit 5 (4 < 5 & 4 > 3) "))));
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = ".*br is not legal.*")
    public void invalidSourceContentElementTest() throws FileNotFoundException {
        // expect RuntimeException with tu:transunit2 - source
        File fileTarget =
                new File(TEST_DIR, "/StringResource_source_invalid.xml");
        reader.extractTemplate(fileTarget, LocaleId.EN_US, null,
                ValidationType.CONTENT.toString());
        assert false;
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "Invalid XLIFF file format")
    public void invalidSourceContentElementTest2() throws FileNotFoundException {
        // expect RuntimeException with tu:transunit2 - source
        File fileTarget =
                new File(TEST_DIR, "/StringResource_source_invalid.xml");
        reader.extractTemplate(fileTarget, LocaleId.EN_US, null,
                ValidationType.XSD.toString());
        assert false;
    }

    @Test(
            expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = ".*does not support elements.*: g.*")
    public
            void unsupportedSourceContentElementTest()
                    throws FileNotFoundException {
        // expect RuntimeException with tu:transunit2 - source
        File fileTarget =
                new File(TEST_DIR, "/StringResource_source_unsupported.xml");
        reader.extractTemplate(fileTarget, LocaleId.EN_US, null,
                ValidationType.CONTENT.toString());
        assert false;
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "Invalid XLIFF file format")
    public void unsupportedSourceContentElementTest2()
            throws FileNotFoundException {
        // expect RuntimeException with tu:transunit2 - source
        File fileTarget =
                new File(TEST_DIR, "/StringResource_source_unsupported.xml");
        reader.extractTemplate(fileTarget, LocaleId.EN_US, null,
                ValidationType.XSD.toString());
        assert false;
    }

    @Test(expectedExceptions = RuntimeException.class,
            expectedExceptionsMessageRegExp = "Invalid XLIFF: "
                    + "anIllegalTag is not legal inside target")
    public void invalidTargetContentElementTest() throws FileNotFoundException {
        // expect RuntimeException with tu:transunit1 - target
        File fileTarget =
                new File(TEST_DIR, "/StringResource_target_invalid.xml");
        Resource resource =
                reader.extractTemplate(fileTarget, LocaleId.EN_US, null,
                        ValidationType.CONTENT.toString());
        assert resource != null;
        reader.extractTarget(fileTarget);
        assert false;
    }

    private Resource getTemplateDoc() throws FileNotFoundException {
        File file = new File(TEST_DIR, File.separator + DOC_NAME);
        return reader.extractTemplate(file, LocaleId.EN_US, DOC_NAME,
                ValidationType.XSD.toString());
    }
}
