package org.zanata.adapter.xliff;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.resource.ExtensionSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.HashUtil;

import com.google.common.base.Charsets;

/**
 * @author aeng
 *
 */
public class XliffReader extends XliffCommon {
    private static final Logger
        log = LoggerFactory.getLogger(XliffReader.class);
    private final SchemaFactory factory = SchemaFactory
            .newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
    private final XMLInputFactory xmlif = XMLInputFactory.newInstance();

    private LocaleId srcLang;
    private ValidationType validationType;

    public Resource extractTemplate(File file, LocaleId sourceLocaleId,
            String docName, String validationType) throws FileNotFoundException {
        Resource document = new Resource(docName);
        document.setContentType(ContentType.TextPlain);
        document.setLang(sourceLocaleId);
        srcLang = sourceLocaleId;
        this.validationType =
                ValidationType.valueOf(validationType.toUpperCase());
        extractXliff(file, document, null);
        return document;
    }

    public TranslationsResource extractTarget(File file)
            throws FileNotFoundException {
        TranslationsResource document = new TranslationsResource();
        extractXliff(file, null, document);
        return document;
    }

    /*
     * Validate xliff file against schema version 1.1
     */
    private void validateXliffFile(StreamSource source) {
        try {
            final Source schemaSource =
                    new StreamSource(Thread.currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream("schema/xliff-core-1.1.xsd"));

            factory.setResourceResolver(new LSResourceResolver() {
                @Override
                public LSInput resolveResource(String type,
                        String namespaceURI, String publicId, String systemId,
                        String baseURI) {
                    InputStream resourceAsStream =
                            this.getClass().getClassLoader()
                                    .getResourceAsStream("schema/" + systemId);
                    return new Input(publicId, systemId, resourceAsStream);
                }
            });

            Schema schema = factory.newSchema(schemaSource);
            Validator validator = schema.newValidator();
            validator.validate(source);
        } catch (SAXException saxException) {
            throw new RuntimeException("Invalid XLIFF file format",
                    saxException);
        } catch (IOException ioException) {
            throw new RuntimeException("Invalid XLIFF file format",
                    ioException);
        }
    }

    private void extractXliff(@Nonnull File file, @Nullable Resource document,
            @Nullable TranslationsResource transDoc)
            throws FileNotFoundException {
        assert document != null || transDoc != null;
        if (validationType == ValidationType.XSD) {
            InputSource inputSource =
                    new InputSource(new FileInputStream(file));
            inputSource.setEncoding("utf8");
            validateXliffFile(new StreamSource(file));
        }

        try {
            // decode entities into one string
            xmlif.setProperty(XMLInputFactory.IS_COALESCING, true);

            InputSource inputSource =
                    new InputSource(new FileInputStream(file));
            inputSource.setEncoding("utf8");
            final XMLStreamReader xmlr =
                    xmlif.createXMLStreamReader(inputSource.getByteStream());
            int fileCount = 0;
            boolean inFile = false;
            while (xmlr.hasNext()) {
                xmlr.next();

                // comments are ignored
                if (isStartElement(xmlr, ELE_FILE)) {
                    inFile = true;
                    // srcLang is passed as en-us by default
                    // srcLang = new LocaleId(getAttributeValue(xmlr,
                    // ATTRI_SOURCE_LANGUAGE));
                    ++fileCount;
                    if (fileCount > 1) {
                        // TODO consider aborting
                        log.warn("multiple 'file' elements not supported: ignoring the rest of the file '{}'", file);
                        break;
                    } else {
                        log.debug("start file element");
                    }
                } else if (isStartElement(xmlr, ELE_TRANS_UNIT)) {
                    if (!inFile) {
                        throw new RuntimeException(
                                "'trans-unit' must appear inside "
                                        + "'file' element: ignoring the "
                                        + "rest of the file " + file);
                    }
                    if (document != null) {
                        TextFlow textFlow = extractTransUnit(xmlr);
                        document.getTextFlows().add(textFlow);
                    } else if (transDoc != null) {
                        TextFlowTarget tfTarget = extractTransUnitTarget(xmlr);
                        // TODO should we include empty TFTs?
                        if (tfTarget.getState() != ContentState.New) {
                            transDoc.getTextFlowTargets().add(tfTarget);
                        }
                    }
                } else if (isEndElement(xmlr, ELE_FILE)) {
                    inFile = false;
                    log.debug("end file element");
                }
            }
            xmlr.close();
        } catch (XMLStreamException e) {
            throw new RuntimeException("Invalid XLIFF file format", e);
        }
    }

    private TextFlow extractTransUnit(XMLStreamReader xmlr)
            throws XMLStreamException {
        TextFlow textFlow = new TextFlow();

        Boolean endTransUnit = false;
        String id = getAttributeValue(xmlr, ATTRI_ID);
        textFlow.setId(id);

        while (xmlr.hasNext() && !endTransUnit) {
            xmlr.next();
            if (isEndElement(xmlr, ELE_TRANS_UNIT)) {
                endTransUnit = true;
            } else {
                if (isStartElement(xmlr, ELE_SOURCE)) {
                    String content =
                            getElementValue(xmlr, ELE_SOURCE,
                                    getContentElementList());
                    textFlow.setContents(content);
                } else if (isStartElement(xmlr, ELE_CONTEXT_GROUP)) {
                    textFlow.getExtensions(true).addAll(
                            extractContextList(xmlr));
                }
            }
        }
        textFlow.setLang(srcLang);

        return textFlow;
    }

    private TextFlowTarget extractTransUnitTarget(XMLStreamReader xmlr)
            throws XMLStreamException {
        TextFlowTarget textFlowTarget = new TextFlowTarget();
        textFlowTarget.setResId(getAttributeValue(xmlr, ATTRI_ID));

        String sourceContent = "";
        String targetContent = "";

        boolean endTransUnit = false;
        while (xmlr.hasNext() && !endTransUnit) {
            xmlr.next();
            if (isEndElement(xmlr, ELE_TRANS_UNIT)) {
                endTransUnit = true;
            } else {
                if (isStartElement(xmlr, ELE_SOURCE)) {
                    sourceContent =
                            getElementValue(xmlr, ELE_SOURCE,
                                    getContentElementList());
                    String sourceHash = HashUtil.sourceHash(sourceContent);
                    textFlowTarget.setSourceHash(sourceHash);
                } else if (isStartElement(xmlr, ELE_TARGET)) {
                    targetContent =
                            getElementValue(xmlr, ELE_TARGET,
                                    getContentElementList());
                    textFlowTarget.setContents(asList(targetContent));
                } else if (isStartElement(xmlr, ELE_CONTEXT_GROUP)) {
                    textFlowTarget.getExtensions(true).addAll(
                            extractContextList(xmlr));
                }
            }
        }
        if (targetContent.isEmpty()) {
            textFlowTarget.setState(ContentState.New);
        } else {
            textFlowTarget.setState(ContentState.Translated);
        }
        return textFlowTarget;
    }

    /**
     * Extract context list
     *
     * @param xmlr
     * @return
     * @throws XMLStreamException
     */
    private ExtensionSet<SimpleComment>
            extractContextList(XMLStreamReader xmlr) throws XMLStreamException {
        ExtensionSet<SimpleComment> contextList =
                new ExtensionSet<SimpleComment>();
        Boolean endContextGroup = false;
        String contextGroup = getAttributeValue(xmlr, ATTRI_NAME);

        while (xmlr.hasNext() && !endContextGroup) {
            // move to context tag
            xmlr.next();
            if (isEndElement(xmlr, ELE_CONTEXT_GROUP))
                endContextGroup = true;
            else {
                if (isStartElement(xmlr, ELE_CONTEXT)) {
                    StringBuilder sb = new StringBuilder();
                    // context-group
                    sb.append(contextGroup);
                    sb.append(DELIMITER);
                    // context-type
                    sb.append(getAttributeValue(xmlr, ATTRI_CONTEXT_TYPE));
                    sb.append(DELIMITER);
                    // value
                    sb.append(getElementValue(xmlr, ELE_CONTEXT, null));
                    contextList.add(new SimpleComment(sb.toString()));
                }
            }
        }
        return contextList;
    }

    /**
     * Extract given element's value
     *
     * @param reader
     * @return
     * @throws XMLStreamException
     */
    private String getElementValue(XMLStreamReader reader, String elementName,
            Collection<String> legalElements) throws XMLStreamException {
        boolean keepReading = true;
        StringBuilder contents = new StringBuilder();

        reader.next();

        if (isElement(reader, elementName)) {
            keepReading = false;
        }

        while (keepReading) {
            // if the value in element is text
            if (reader.hasText()) {
                contents.append(reader.getText());
            } else {
                // if value in element is a xml element; invalid text
                if (reader.isStartElement() || reader.isEndElement()) {
                    String localName = getLocalName(reader);
                    if (legalElements == null
                            || legalElements.contains(localName)) {
                        throw new RuntimeException(
                                "Sorry, Zanata does not support elements inside "
                                        + elementName + ": " + localName);
                    } else {
                        throw new RuntimeException("Invalid XLIFF: "
                                + localName + " is not legal inside "
                                + elementName);
                    }
                }
            }
            reader.next();

            if (isElement(reader, elementName)) {
                keepReading = false;
            }
        }
        return contents.toString();
    }

    private static String getLocalName(XMLStreamReader xmlr) {
        if (xmlr.isCharacters()) {
            return "";
        }
        return xmlr.getLocalName();
    }

    private static boolean isElement(
            XMLStreamReader xmlr, String elementLocalName) {
        return (xmlr.isStartElement() || xmlr.isEndElement())
            && getLocalName(xmlr).equals(elementLocalName);
    }

    private static boolean isEndElement(
            XMLStreamReader xmlr, String elementLocalName) {
        return xmlr.isEndElement()
            && getLocalName(xmlr).equals(elementLocalName);
    }

    private static boolean isStartElement(
            XMLStreamReader xmlr, String elementLocalName) {
        return xmlr.isStartElement()
            && getLocalName(xmlr).equals(elementLocalName);
    }

    /**
     * Extract given attribute's value
     *
     * @param xmlr
     * @param attrKey
     * @return
     */
    private String getAttributeValue(XMLStreamReader xmlr, String attrKey) {
        int count = xmlr.getAttributeCount();

        if (count > 0) {
            for (int i = 0; i < count; i++) {
                if (xmlr.getAttributeLocalName(i).equals(attrKey))
                    return xmlr.getAttributeValue(i);
            }
        }
        return null;
    }

    public static class Input implements LSInput {

        private String publicId;

        private String systemId;

        public String getPublicId() {
            return publicId;
        }

        public void setPublicId(String publicId) {
            this.publicId = publicId;
        }

        public String getBaseURI() {
            return null;
        }

        public InputStream getByteStream() {
            return null;
        }

        public boolean getCertifiedText() {
            return false;
        }

        public Reader getCharacterStream() {
            return null;
        }

        public String getEncoding() {
            return null;
        }

        public String getStringData() {
            synchronized (inputStream) {
                try {
                    byte[] input = new byte[inputStream.available()];
                    inputStream.read(input);
                    String contents = new String(input, Charsets.UTF_8);
                    return contents;
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Exception " + e);
                    return null;
                }
            }
        }

        public void setBaseURI(String baseURI) {
        }

        public void setByteStream(InputStream byteStream) {
        }

        public void setCertifiedText(boolean certifiedText) {
        }

        public void setCharacterStream(Reader characterStream) {
        }

        public void setEncoding(String encoding) {
        }

        public void setStringData(String stringData) {
        }

        public String getSystemId() {
            return systemId;
        }

        public void setSystemId(String systemId) {
            this.systemId = systemId;
        }

        public BufferedInputStream getInputStream() {
            return inputStream;
        }

        public void setInputStream(BufferedInputStream inputStream) {
            this.inputStream = inputStream;
        }

        private BufferedInputStream inputStream;

        public Input(String publicId, String sysId, InputStream input) {
            this.publicId = publicId;
            this.systemId = sysId;
            this.inputStream = new BufferedInputStream(input);
        }
    }
}
