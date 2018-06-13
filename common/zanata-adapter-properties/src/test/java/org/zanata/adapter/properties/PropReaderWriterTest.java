package org.zanata.adapter.properties;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.deleteIfExists;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class PropReaderWriterTest {
    private static final Logger log = LoggerFactory
            .getLogger(PropReaderWriterTest.class);
    private static final String TEST_OUTPUT_DIR_STRING = "target/test-output";
    private static final File TEST_OUTPUT_DIR =
            new File(TEST_OUTPUT_DIR_STRING);
    private static final String SYSTEM_LINE_ENDING = System
            .getProperty("line.separator");
    PropReader propReader;
    String locale = "fr";

    @Before
    public void resetReader() {
        propReader =
                new PropReader(PropWriter.CHARSET.Latin1, new LocaleId(locale),
                        ContentState.Translated);
    }

    @Test
    public void roundtripSrcPropsToDocXmlToProps() throws Exception {
        String docName = "test.properties";
        Resource srcDoc = new Resource("test");
        InputStream testStream = getResourceAsStream(docName);

        propReader.extractTemplate(srcDoc, testStream);
        JAXBContext jc = JAXBContext.newInstance(Resource.class);
        Marshaller marshal = jc.createMarshaller();
        StringWriter sw = new StringWriter();
        marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshal.marshal(srcDoc, sw);
        log.debug("{}", sw);

        Unmarshaller unmarshal = jc.createUnmarshaller();
        Resource docIn =
                (Resource) unmarshal.unmarshal(new StringReader(sw.toString()));

        PropWriter.writeSource(docIn, TEST_OUTPUT_DIR, PropWriter.CHARSET.Latin1);

        assertInputAndOutputDocContentSame(docName);
    }

    @Test
    public void roundtripTransPropsToDocXmlToProps() throws Exception {
        String docName = "test_fr.properties";
        InputStream targetStream = getResourceAsStream(docName);
        TranslationsResource transDoc = new TranslationsResource();
        propReader.extractTarget(transDoc, targetStream, new Resource());

        JAXBContext jc = JAXBContext.newInstance(TranslationsResource.class);
        Marshaller marshal = jc.createMarshaller();
        StringWriter sw = new StringWriter();
        marshal.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshal.marshal(transDoc, sw);
        log.debug("{}", sw);

        Unmarshaller unmarshal = jc.createUnmarshaller();
        TranslationsResource docIn =
                (TranslationsResource) unmarshal.unmarshal(new StringReader(sw
                        .toString()));

        PropWriter.writeTranslations(null, docIn, TEST_OUTPUT_DIR,
            "test", locale, PropWriter.CHARSET.Latin1, false, false);

        assertInputAndOutputDocContentSame(docName);
    }

    /**
     * Asserts that the content of the input document and output document are
     * identical. Assumes filename is the same for both documents and that the
     * output file is in TEST_OUTPUT_DIR.
     *
     * @param docName
     *            the name for both input and output files
     * @throws FileNotFoundException
     * @throws IOException
     * @throws MalformedURLException
     */
    private void assertInputAndOutputDocContentSame(String docName)
            throws IOException {
        File newFile =
                new File(TEST_OUTPUT_DIR.getPath() + File.separator + docName);
        InputStream newStream = newFile.toURI().toURL().openStream();
        InputStream origStream = getResourceAsStream(docName);

        String origContent = IOUtils.toString(origStream, UTF_8);
        String newlineAdjustedOrigContent =
                origContent.replaceAll("\n", SYSTEM_LINE_ENDING);
        String newContent = IOUtils.toString(newStream, UTF_8);

        // note: this does not allow for differences in whitespace, so if tests
        // start failing this should be updated to use a less strict comparison
        assertThat(newContent).isEqualTo(newlineAdjustedOrigContent);
    }

    private InputStream getResourceAsStream(String relativeResourceName)
            throws FileNotFoundException {
        InputStream stream =
                getClass().getResourceAsStream(relativeResourceName);
        if (stream == null)
            throw new FileNotFoundException(relativeResourceName);
        return stream;
    }

    @Test
    public void extractTemplateRemovesNonTranslatableRegions()
            throws IOException {
        Resource srcDoc = new Resource("test");
        InputStream testStream =
                getResourceAsStream("test_non_trans.properties");
        propReader.extractTemplate(srcDoc, testStream);

        List<TextFlow> textFlows = srcDoc.getTextFlows();

        assertThat(textFlows).hasSize(2);
        assertThat(textFlows.get(0).getId()).isEqualTo("HELLO");
        assertThat(textFlows.get(1).getId()).isEqualTo("GOODBYE");
        // TODO also check comments?
    }

    @Test
    public void extractTemplateNestedNonTranslatableRegions() throws Exception {
        Resource srcDoc = new Resource("test");
        InputStream testStream =
                getResourceAsStream("test_non_trans_nested.properties");
        propReader.extractTemplate(srcDoc, testStream);

        List<TextFlow> textFlows = srcDoc.getTextFlows();

        assertThat(textFlows).hasSize(2);
        assertThat(textFlows.get(0).getId()).isEqualTo("HELLO");
        assertThat(textFlows.get(1).getId()).isEqualTo("GOODBYE");
        // TODO also check comments?
    }

    @Test(expected = InvalidPropertiesFormatException.class)
    public void extractTemplateNonTranslatableMismatchException()
            throws Exception {
        Resource srcDoc = new Resource("test");
        InputStream testStream =
                getResourceAsStream("test_non_trans_mismatch.properties");
        propReader.extractTemplate(srcDoc, testStream);
        fail("expected exception");
    }

    @Test
    public void writeTranslatedAndApproved() throws Exception {
        Resource srcDoc = sourceDocWithTwoEntries();
        TranslationsResource doc = targetDocumentWithOneTranslatedOneApproved();
        Path file = createTempFile(null, null);
        try {
            boolean approvedOnly = false;
            PropWriter.writeTranslationsFile(srcDoc, doc,
                    file.toFile(), PropWriter.CHARSET.UTF8, false, approvedOnly);
            assertThat(file).hasContent("hello=bon jour\ngoodbye=au revoir\n");
        } finally {
            deleteIfExists(file);
        }
    }

    @Test
    public void writeApprovedOnly() throws Exception {
        Resource srcDoc = sourceDocWithTwoEntries();
        TranslationsResource doc = targetDocumentWithOneTranslatedOneApproved();
        Path file = createTempFile(null, null);
        try {
            boolean approvedOnly = true;
            PropWriter.writeTranslationsFile(srcDoc, doc,
                    file.toFile(), PropWriter.CHARSET.UTF8, false, approvedOnly);
            // hello is only Translated, so it should be left out
            assertThat(file).hasContent("goodbye=au revoir\n");
        } finally {
            deleteIfExists(file);
        }
    }

    @Test
    public void writeApprovedOnlyWithSkeleton() throws Exception {
        Resource srcDoc = sourceDocWithTwoEntries();
        TranslationsResource doc = targetDocumentWithOneTranslatedOneApproved();
        Path file = createTempFile(null, null);
        try {
            boolean approvedOnly = true;
            PropWriter.writeTranslationsFile(srcDoc, doc,
                    file.toFile(), PropWriter.CHARSET.UTF8, true, approvedOnly);
            // hello is only Translated, so it should be a skeleton
            assertThat(file).hasContent("hello=\ngoodbye=au revoir\n");
        } finally {
            deleteIfExists(file);
        }
    }

    public Resource sourceDocWithTwoEntries() {
        Resource srcDoc = new Resource("test");
        srcDoc.getTextFlows().add(new TextFlow("hello"));
        srcDoc.getTextFlows().add(new TextFlow("goodbye"));
        return srcDoc;
    }

    public TranslationsResource targetDocumentWithOneTranslatedOneApproved() {
        TranslationsResource doc = new TranslationsResource();
        List<TextFlowTarget> targets = doc.getTextFlowTargets();
        TextFlowTarget tft1 = new TextFlowTarget("hello");
        tft1.setContents("bon jour");
        tft1.setState(ContentState.Translated);
        targets.add(tft1);
        TextFlowTarget tft2 = new TextFlowTarget("goodbye");
        tft2.setContents("au revoir");
        tft2.setState(ContentState.Approved);
        targets.add(tft2);
        return doc;
    }
}
