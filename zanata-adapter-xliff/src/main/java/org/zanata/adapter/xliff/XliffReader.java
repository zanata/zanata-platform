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

import com.google.common.base.Charsets;

/**
 * @author aeng
 *
 */
public class XliffReader extends XliffCommon {
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

    private void extractXliff(File file, Resource document,
            TranslationsResource transDoc) throws FileNotFoundException {

        if (validationType == ValidationType.XSD) {
            InputSource inputSource =
                    new InputSource(new FileInputStream(file));
            inputSource.setEncoding("utf8");
            validateXliffFile(new StreamSource(file));
        }

        try {
            xmlif.setProperty(XMLInputFactory.IS_COALESCING, true); // decode
                                                                    // entities
                                                                    // into one
                                                                    // string

            InputSource inputSource =
                    new InputSource(new FileInputStream(file));
            inputSource.setEncoding("utf8");
            final XMLStreamReader xmlr =
                    xmlif.createXMLStreamReader(inputSource.getByteStream());
            while (xmlr.hasNext()) {
                xmlr.next();

                if (xmlr.getEventType() == XMLEvent.COMMENT) {
                    // at the moment, ignore comments
                    // extractComment(xmlr);
                } else if (xmlr.isStartElement()
                        && getLocalName(xmlr).equals(ELE_FILE)) {
                    // srcLang is passed as en-us by default
                    // srcLang = new LocaleId(getAttributeValue(xmlr,
                    // ATTRI_SOURCE_LANGUAGE));
                } else if (xmlr.isStartElement()
                        && getLocalName(xmlr).equals(ELE_TRANS_UNIT)) {
                    if (document != null) {
                        TextFlow textFlow = extractTransUnit(xmlr);
                        if (textFlow != null) {
                            document.getTextFlows().add(textFlow);
                        }
                    } else {
                        TextFlowTarget tfTarget = extractTransUnitTarget(xmlr);
                        List<String> contents = tfTarget.getContents();
                        boolean targetEmpty =
                                contents.isEmpty()
                                        || StringUtils.isEmpty(contents.get(0));
                        if (!targetEmpty) {
                            tfTarget.setState(ContentState.Translated);
                            transDoc.getTextFlowTargets().add(tfTarget);
                        }
                    }
                } else if (xmlr.isEndElement()
                        && getLocalName(xmlr).equals(ELE_FILE)) {
                    // this is to ensure only 1 <file> element in each xliff
                    // document
                    // FIXME it only ensures that we silently ignore extra file
                    // elements!
                    break;
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
            String localName = getLocalName(xmlr);
            boolean endElement = xmlr.isEndElement();
            if (endElement && localName.equals(ELE_TRANS_UNIT)) {
                endTransUnit = true;
            } else {
                boolean startElement = xmlr.isStartElement();
                if (startElement && localName.equals(ELE_SOURCE)) {
                    String content =
                            getElementValue(xmlr, ELE_SOURCE,
                                    getContentElementList());
                    textFlow.setContents(content);
                } else if (startElement && localName.equals(ELE_CONTEXT_GROUP)) {
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

        Boolean endTransUnit = false;
        textFlowTarget.setResId(getAttributeValue(xmlr, ATTRI_ID));

        while (xmlr.hasNext() && !endTransUnit) {
            xmlr.next();
            boolean endElement = xmlr.isEndElement();
            String localName = getLocalName(xmlr);
            if (endElement && localName.equals(ELE_TRANS_UNIT)) {
                endTransUnit = true;
            } else {
                if (xmlr.isStartElement() && localName.equals(ELE_TARGET)) {
                    String content =
                            getElementValue(xmlr, ELE_TARGET,
                                    getContentElementList());
                    textFlowTarget.setContents(asList(content));
                } else if (xmlr.isStartElement()
                        && localName.equals(ELE_CONTEXT_GROUP)) {
                    textFlowTarget.getExtensions(true).addAll(
                            extractContextList(xmlr));
                }
            }
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
            xmlr.next();// move to context tag
            String localName = getLocalName(xmlr);
            boolean endElement = xmlr.isEndElement();
            if (endElement && localName.equals(ELE_CONTEXT_GROUP))
                endContextGroup = true;
            else {
                boolean startElement = xmlr.isStartElement();
                if (startElement && localName.equals(ELE_CONTEXT)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(contextGroup);// context-group
                    sb.append(DELIMITER);
                    sb.append(getAttributeValue(xmlr, ATTRI_CONTEXT_TYPE));// context-type
                    sb.append(DELIMITER);
                    sb.append(getElementValue(xmlr, ELE_CONTEXT, null));// value
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

        String localName = getLocalName(reader);
        if ((reader.isEndElement() || reader.isStartElement())
                && localName.equals(elementName)) {
            keepReading = false;
        }

        while (keepReading) {
            if (reader.hasText()) { // if the value in element is text
                contents.append(reader.getText());
            } else {
                // if value in element is a xml element; invalid text
                if (reader.isStartElement() || reader.isEndElement()) {
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
            localName = getLocalName(reader);

            if ((reader.isEndElement() || reader.isStartElement())
                    && localName.equals(elementName)) {
                keepReading = false;
            }
        }
        return contents.toString();
    }

    private static String getLocalName(XMLStreamReader xmlr) {
        if (xmlr.isCharacters())
            return "";
        return xmlr.getLocalName();
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
