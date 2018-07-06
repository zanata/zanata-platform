package org.zanata.adapter.po;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.fedorahosted.tennera.jgettext.Message;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

public class PoReader2Test {
    private static final Logger log = LoggerFactory
            .getLogger(PoReader2Test.class);

    private final PoReader2 poReader = new PoReader2();

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Resource getTemplate() throws IOException {
        InputSource inputSource = getTestInputSource("pot/RPM.pot");
        inputSource.setEncoding("utf8");

        log.debug("parsing template");
        Resource doc =
                poReader.extractTemplate(inputSource, LocaleId.EN_US, "doc1");
        assertThat(doc.getTextFlows()).hasSize(137);
        return doc;
    }

    @Test
    public void extractTarget() throws Exception {
        InputSource inputSource;
        Resource doc = getTemplate();
        String locale = "ja-JP";
        inputSource = getTestInputSource(locale + "/RPM.po");
        inputSource.setEncoding("utf8");
        log.debug("extracting target: " + locale);
        TranslationsResource targetDoc = poReader.extractTarget(inputSource);
        List<TextFlowTarget> textFlowTargets = targetDoc.getTextFlowTargets();
        assertThat(textFlowTargets).hasSize(137);
        TextFlowTarget target = textFlowTargets.iterator().next();
        assertThat(target).isNotNull();

        JAXBContext jaxbContext =
                JAXBContext.newInstance(Resource.class,
                        TranslationsResource.class);
        Marshaller m = jaxbContext.createMarshaller();
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        log.debug("marshalling source doc");
        {
            StringWriter writer = new StringWriter();
            m.marshal(doc, writer);
            log.debug("{}", writer);
        }

        log.debug("marshalling target doc");
        {
            StringWriter writer = new StringWriter();
            m.marshal(targetDoc, writer);
            log.debug("{}", writer);
        }

        List<TextFlow> resources = doc.getTextFlows();

        TextFlow tf1 = resources.get(3);
        assertThat(tf1.getContents()).isEqualTo(asList("Important"));
        TextFlowTarget tfTarget = textFlowTargets.get(3);
        assertThat(tfTarget.getContents()).isEqualTo(asList("キーのインポート"));

        // TODO test PO headers and attributes
    }

    @Test
    public void extractTemplate() throws Exception {
        getTemplate();
    }

    @Test
    public void extractInvalidTemplate() throws Exception {
        InputSource inputSource = getTestInputSource("pot/invalid.pot");
        inputSource.setEncoding("utf8");

        exception.expect(RuntimeException.class);
        exception.expectMessage("unsupported charset");
        poReader.extractTemplate(inputSource, LocaleId.EN_US, "doc1");
    }

    @Test
    public void shouldRejectIllegalCharset() throws Exception {
        String locale = "ja-JP";
        InputSource inputSource = getTestInputSource(locale + "/invalid.po");
        inputSource.setEncoding("utf8");
        log.debug("extracting target: " + locale);

        exception.expect(RuntimeException.class);
        exception.expectMessage("unsupported charset");
        poReader.extractTarget(inputSource);
    }

    private InputSource getTestInputSource(String resourceName) throws IOException {
        InputStream stream =
                getClass().getResourceAsStream("/" + resourceName);
        if (stream == null) {
            throw new IOException("resource not found: " + resourceName);
        }
        return new InputSource(stream);
    }

    @Test
    public void testContentStateApprovedSingle() {
        Message m = new Message();
        m.setMsgstr("s");
        ContentState actual1 = PoReader2.getContentState(m);
        assertThat(actual1).isEqualTo(ContentState.Translated);
    }

    @Test
    public void testContentStateApprovedPlural1() {
        Message m = new Message();
        m.setMsgidPlural("plural");
        m.addMsgstrPlural("s0", 0);
        ContentState actual1 = PoReader2.getContentState(m);
        assertThat(actual1).isEqualTo(ContentState.Translated);
    }

    @Test
    public void testContentStateApprovedPlural2() {
        Message m = new Message();
        m.setMsgidPlural("plural");
        m.addMsgstrPlural("s0", 0);
        m.addMsgstrPlural("s1", 1);
        ContentState actual1 = PoReader2.getContentState(m);
        assertThat(actual1).isEqualTo(ContentState.Translated);
    }

    @Test
    public void testContentStateNewSingle1() {
        Message m = new Message();
        m.setMsgstr("");
        ContentState actual1 = PoReader2.getContentState(m);
        assertThat(actual1).isEqualTo(ContentState.New);
    }

    @Test
    public void testContentStateNewSingle2() {
        Message m = new Message();
        ContentState actual1 = PoReader2.getContentState(m);
        assertThat(actual1).isEqualTo(ContentState.New);
    }

    @Test
    public void testContentStateNewPlural1() {
        Message m = new Message();
        m.setMsgidPlural("plural");
        m.addMsgstrPlural("", 0);
        ContentState actual1 = PoReader2.getContentState(m);
        assertThat(actual1).isEqualTo(ContentState.New);
    }

    @Test
    public void testContentStateNewPlural2() {
        Message m = new Message();
        m.setMsgidPlural("plural");
        m.addMsgstrPlural("", 0);
        m.addMsgstrPlural("s1", 1);
        ContentState actual1 = PoReader2.getContentState(m);
        assertThat(actual1).isEqualTo(ContentState.New);
    }

    @Test
    public void testContentStateNewPlural3() {
        Message m = new Message();
        m.setMsgidPlural("plural");
        ContentState actual1 = PoReader2.getContentState(m);
        assertThat(actual1).isEqualTo(ContentState.New);
    }

    @Test
    public void testContentStateNewPlural4() {
        Message m = new Message();
        m.setMsgidPlural("plural");
        m.addMsgstrPlural("", 0);
        m.addMsgstrPlural("", 1);
        ContentState actual1 = PoReader2.getContentState(m);
        assertThat(actual1).isEqualTo(ContentState.New);
    }

    // FIXME test where plurals < nplurals
    // @Test
    // public void testContentStateNewPluralTooFew()
    // {
    // // TODO set nplurals=2
    // Message m = new Message();
    // m.setMsgidPlural("plural");
    // m.addMsgstrPlural("s0", 0);
    // ContentState actual1 = PoReader2.getContentState(m);
    // assertThat(actual1, is(ContentState.New));
    // }

    @Test
    public void testContentStateNeedReviewSingle() {
        Message m = new Message();
        m.setFuzzy(true);
        m.setMsgstr("s");
        ContentState actual1 = PoReader2.getContentState(m);
        assertThat(actual1).isEqualTo(ContentState.NeedReview);
    }

    @Test
    public void testContentStateNeedReviewPlural1() {
        Message m = new Message();
        m.setFuzzy(true);
        m.setMsgidPlural("plural");
        m.addMsgstrPlural("s", 0);
        ContentState actual1 = PoReader2.getContentState(m);
        assertThat(actual1).isEqualTo(ContentState.NeedReview);
    }

    @Test
    public void testContentStateNeedReviewPlural2() {
        Message m = new Message();
        m.setFuzzy(true);
        m.setMsgidPlural("plural");
        m.addMsgstrPlural("", 0);
        m.addMsgstrPlural("s1", 1);
        ContentState actual1 = PoReader2.getContentState(m);
        assertThat(actual1).isEqualTo(ContentState.NeedReview);
    }

    @Test
    public void testContentStateNeedReviewPlural3() {
        Message m = new Message();
        m.setFuzzy(true);
        m.setMsgidPlural("plural");
        m.addMsgstrPlural("s0", 0);
        m.addMsgstrPlural("s1", 1);
        ContentState actual1 = PoReader2.getContentState(m);
        assertThat(actual1).isEqualTo(ContentState.NeedReview);
    }

}
