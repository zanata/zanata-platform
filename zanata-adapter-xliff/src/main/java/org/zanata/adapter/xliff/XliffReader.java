package org.zanata.adapter.xliff;

import static java.util.Arrays.asList;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.stax.StAXSource;
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

/**
 * @author aeng
 * 
 */
public class XliffReader extends XliffCommon
{
   private final SchemaFactory factory = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
   private final XMLInputFactory xmlif = XMLInputFactory.newInstance();

   private LocaleId srcLang;
   private ValidationType validationType;

   public Resource extractTemplate(File file, LocaleId sourceLocaleId, String docName, String validationType) throws FileNotFoundException
   {
      Resource document = new Resource(docName);
      document.setContentType(ContentType.TextPlain);
      document.setLang(sourceLocaleId);
      srcLang = sourceLocaleId;
      this.validationType = ValidationType.valueOf(validationType.toUpperCase());
      extractXliff(file, document, null);
      return document;
   }

   public TranslationsResource extractTarget(File file) throws FileNotFoundException
   {
      TranslationsResource document = new TranslationsResource();
      extractXliff(file, null, document);
      return document;
   }

   /*
    * Validate xliff file against schema version 1.1
    */
   private void validateXliffFile(InputSource inputSource)
   {
      try
      {
         final XMLStreamReader xmlr = xmlif.createXMLStreamReader(inputSource.getByteStream());
         final Source schemaSource = new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("schema/xliff-core-1.1.xsd"));

         factory.setResourceResolver(new LSResourceResolver()
         {
            @Override
            public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI)
            {
               InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("schema/" + systemId);
               return new Input(publicId, systemId, resourceAsStream);
            }
         });

         Schema schema = factory.newSchema(schemaSource);
         Validator validator = schema.newValidator();
         validator.validate(new StAXSource(xmlr));

         xmlr.close();

      }
      catch (XMLStreamException e)
      {
         throw new RuntimeException("Invalid XLIFF file format  ", e);
      }
      catch (SAXException saxException)
      {
         throw new RuntimeException("Invalid XLIFF file format  ", saxException);
      }
      catch (IOException ioException)
      {
         throw new RuntimeException("Invalid XLIFF file format  ", ioException);
      }
   }

   private void extractXliff(File file, Resource document, TranslationsResource transDoc) throws FileNotFoundException
   {

      if (validationType == ValidationType.XSD)
      {
         InputSource inputSource = new InputSource(new FileInputStream(file));
         inputSource.setEncoding("utf8");
         validateXliffFile(new InputSource(new FileInputStream(file)));
      }

      try
      {
         xmlif.setProperty(XMLInputFactory.IS_COALESCING, true); // decode
                                                                 // entities
                                                                 // into one
                                                                 // string

         InputSource inputSource = new InputSource(new FileInputStream(file));
         inputSource.setEncoding("utf8");
         final XMLStreamReader xmlr = xmlif.createXMLStreamReader(inputSource.getByteStream());
         while (xmlr.hasNext())
         {
            xmlr.next();

            if (xmlr.getEventType() == XMLEvent.COMMENT)
            {
               // at the moment, ignore comments
               // extractComment(xmlr);
            }
            else if (xmlr.isStartElement() && xmlr.getLocalName().equals(ELE_FILE))
            {
               // srcLang is passed as en-us by default
               // srcLang = new LocaleId(getAttributeValue(xmlr,
               // ATTRI_SOURCE_LANGUAGE));
            }
            else if (xmlr.isStartElement() && xmlr.getLocalName().equals(ELE_TRANS_UNIT))
            {
               if (document != null)
               {
                  TextFlow textFlow = extractTransUnit(xmlr);
                  if (textFlow != null)
                  {
                     document.getTextFlows().add(textFlow);
                  }
               }
               else
               {
                  TextFlowTarget tfTarget = extractTransUnitTarget(xmlr);
                  List<String> contents = tfTarget.getContents();
                  boolean targetEmpty = contents.isEmpty() || StringUtils.isEmpty(contents.get(0));
                  if (!targetEmpty)
                  {
                     tfTarget.setState(ContentState.Approved);
                     transDoc.getTextFlowTargets().add(tfTarget);
                  }
               }
            }
            else if (xmlr.isEndElement() && xmlr.getLocalName().equals(ELE_FILE))
            {
               // this is to ensure only 1 <file> element in each xliff document
               break;
            }
         }
         xmlr.close();
      }
      catch (XMLStreamException e)
      {
         throw new RuntimeException("Invalid XLIFF file format  ", e);
      }
   }

   // Text,
   // Zero, one or more of the following elements: <g>, <x/>, <bx/>, <ex/>,
   // <bpt> , <ept>, <ph>, <it> , <mrk>, in any order.

   // private final static String xmlTagRegex = "(<.[^(><.)]+>)";
   private final static String xmlTagRegex = "<[/]?[a-z]+[0-9]*[/]?>";
   private final static Pattern xmlTagPattern = Pattern.compile(xmlTagRegex);

   private String extractAndValidateContent(XMLStreamReader xmlr, String endElement, String id) throws XMLStreamException
   {
      String content = getElementValue(xmlr, endElement);
      if (!StringUtils.isEmpty(content))
      {
         Matcher matcher = xmlTagPattern.matcher(content);

         while (matcher.find())
         {
            if (!getContentElementList().contains(matcher.group()))
            {
               throw new RuntimeException("Invalid XLIFF file format: unknown element in -id:" + id + " -content:" + content + " -element:" + matcher.group());
            }
         }
      }
      return content;
   }

   private TextFlow extractTransUnit(XMLStreamReader xmlr) throws XMLStreamException
   {
      TextFlow textFlow = new TextFlow();

      Boolean endTransUnit = false;
      String id = getAttributeValue(xmlr, ATTRI_ID);
      textFlow.setId(id);

      while (xmlr.hasNext() && !endTransUnit)
      {
         xmlr.next();
         String localName = xmlr.getLocalName();
         boolean endElement = xmlr.isEndElement();
         if (endElement && localName.equals(ELE_TRANS_UNIT))
         {
            endTransUnit = true;
         }
         else
         {
            boolean startElement = xmlr.isStartElement();
            if (startElement && localName.equals(ELE_SOURCE))
            {
               String content = extractAndValidateContent(xmlr, ELE_SOURCE, id);
               textFlow.setContents(content);
            }
            else if (startElement && localName.equals(ELE_CONTEXT_GROUP))
            {
               textFlow.getExtensions(true).addAll(extractContextList(xmlr));
            }
         }
      }
      textFlow.setLang(srcLang);

      return textFlow;
   }

   private TextFlowTarget extractTransUnitTarget(XMLStreamReader xmlr) throws XMLStreamException
   {
      TextFlowTarget textFlowTarget = new TextFlowTarget();

      Boolean endTransUnit = false;
      textFlowTarget.setResId(getAttributeValue(xmlr, ATTRI_ID));

      while (xmlr.hasNext() && !endTransUnit)
      {
         xmlr.next();
         boolean endElement = xmlr.isEndElement();
         String localName = xmlr.getLocalName();
         if (endElement && localName.equals(ELE_TRANS_UNIT))
         {
            endTransUnit = true;
         }
         else
         {
            if (xmlr.isStartElement() && localName.equals(ELE_TARGET))
            {
               String content = extractAndValidateContent(xmlr, ELE_TARGET, textFlowTarget.getResId());
               textFlowTarget.setContents(asList(content));
            }
            else if (xmlr.isStartElement() && localName.equals(ELE_CONTEXT_GROUP))
            {
               textFlowTarget.getExtensions(true).addAll(extractContextList(xmlr));
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
   private ExtensionSet<SimpleComment> extractContextList(XMLStreamReader xmlr) throws XMLStreamException
   {
      ExtensionSet<SimpleComment> contextList = new ExtensionSet<SimpleComment>();
      Boolean endContextGroup = false;
      String contextGroup = getAttributeValue(xmlr, ATTRI_NAME);

      while (xmlr.hasNext() && !endContextGroup)
      {
         xmlr.next();// move to context tag
         String localName = xmlr.getLocalName();
         boolean endElement = xmlr.isEndElement();
         if (endElement && localName.equals(ELE_CONTEXT_GROUP))
            endContextGroup = true;
         else
         {
            boolean startElement = xmlr.isStartElement();
            if (startElement && localName.equals(ELE_CONTEXT))
            {
               StringBuilder sb = new StringBuilder();
               sb.append(contextGroup);// context-group
               sb.append(DELIMITER);
               sb.append(getAttributeValue(xmlr, ATTRI_CONTEXT_TYPE));// context-type
               sb.append(DELIMITER);
               sb.append(getElementValue(xmlr, ELE_CONTEXT));// value
               contextList.add(new SimpleComment(sb.toString()));
            }
         }
      }
      return contextList;
   }

   // Escape html character
   private String escapeHTML(String text)
   {
      return text.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
   }

   /**
    * Extract given element's value
    * 
    * @param currentCursor
    * @return
    * @throws XMLStreamException
    */
   private String getElementValue(XMLStreamReader currentCursor, String endElement) throws XMLStreamException
   {
      boolean loop = true;
      StringBuilder contents = new StringBuilder();

      currentCursor.next();

      if ((currentCursor.isEndElement() || currentCursor.isStartElement()) && currentCursor.getLocalName().equals(endElement))
      {
         loop = false;
      }

      while (loop)
      {
         if (currentCursor.hasText()) // if the value in element is text
         {
            // make sure all the values are properly xml encoded/escaped
            contents.append(escapeHTML(currentCursor.getText()));
         }
         else
         {
            // if value in element is a xml element; invalid text
            if (currentCursor.isStartElement())
            {
               contents.append("<" + currentCursor.getLocalName() + ">");
            }
            else if (currentCursor.isEndElement())
            {
               contents.append("</" + currentCursor.getLocalName() + ">");
            }
         }
         currentCursor.next();

         if ((currentCursor.isEndElement() || currentCursor.isStartElement()) && currentCursor.getLocalName().equals(endElement))
         {
            loop = false;
         }
      }
      return contents.toString();
   }

   /**
    * Extract given attribute's value
    * 
    * @param xmlr
    * @param attrKey
    * @return
    */
   private String getAttributeValue(XMLStreamReader xmlr, String attrKey)
   {
      int count = xmlr.getAttributeCount();

      if (count > 0)
      {
         for (int i = 0; i < count; i++)
         {
            if (xmlr.getAttributeLocalName(i).equals(attrKey))
               return xmlr.getAttributeValue(i);
         }
      }
      return null;
   }

   public class Input implements LSInput
   {

      private String publicId;

      private String systemId;

      public String getPublicId()
      {
         return publicId;
      }

      public void setPublicId(String publicId)
      {
         this.publicId = publicId;
      }

      public String getBaseURI()
      {
         return null;
      }

      public InputStream getByteStream()
      {
         return null;
      }

      public boolean getCertifiedText()
      {
         return false;
      }

      public Reader getCharacterStream()
      {
         return null;
      }

      public String getEncoding()
      {
         return null;
      }

      public String getStringData()
      {
         synchronized (inputStream)
         {
            try
            {
               byte[] input = new byte[inputStream.available()];
               inputStream.read(input);
               String contents = new String(input);
               return contents;
            }
            catch (IOException e)
            {
               e.printStackTrace();
               System.out.println("Exception " + e);
               return null;
            }
         }
      }

      public void setBaseURI(String baseURI)
      {
      }

      public void setByteStream(InputStream byteStream)
      {
      }

      public void setCertifiedText(boolean certifiedText)
      {
      }

      public void setCharacterStream(Reader characterStream)
      {
      }

      public void setEncoding(String encoding)
      {
      }

      public void setStringData(String stringData)
      {
      }

      public String getSystemId()
      {
         return systemId;
      }

      public void setSystemId(String systemId)
      {
         this.systemId = systemId;
      }

      public BufferedInputStream getInputStream()
      {
         return inputStream;
      }

      public void setInputStream(BufferedInputStream inputStream)
      {
         this.inputStream = inputStream;
      }

      private BufferedInputStream inputStream;

      public Input(String publicId, String sysId, InputStream input)
      {
         this.publicId = publicId;
         this.systemId = sysId;
         this.inputStream = new BufferedInputStream(input);
      }
   }
}
