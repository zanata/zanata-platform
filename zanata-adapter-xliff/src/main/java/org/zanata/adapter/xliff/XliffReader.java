package org.zanata.adapter.xliff;

import static java.util.Arrays.asList;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stax.StAXSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.commons.lang.StringUtils;
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
   private CHECK check = CHECK.Quick;
   private SchemaFactory factory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
   private File schemaLocation = new File("schema/xliff-core-1.1.xsd");
   private Schema schema;
   private Validator validator;

   private LocaleId srcLang;

   public Resource extractTemplate(InputSource inputSource, LocaleId sourceLocaleId, String docName)
   {
      Resource document = new Resource(docName);
      document.setContentType(ContentType.TextPlain);
      document.setLang(sourceLocaleId);
      srcLang = sourceLocaleId;
      extractXliff(inputSource, document, null);
      return document;
   }

   public TranslationsResource extractTarget(InputSource inputSource)
   {
      TranslationsResource document = new TranslationsResource();
      extractXliff(inputSource, null, document);
      return document;
   }

   /*
    * Validate xliff file against schema version 1.1
    */
   private void validateXliffFile(XMLStreamReader xmlr)
   {
      if (check == CHECK.Validate)
      {
         try
         {
            schema = factory.newSchema(schemaLocation);
            validator = schema.newValidator();
            validator.validate(new StAXSource(xmlr));
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

   }

   private void extractXliff(InputSource inputSource, Resource document, TranslationsResource transDoc)
   {
      try
      {
         XMLInputFactory xmlif = XMLInputFactory.newInstance();
         xmlif.setProperty(XMLInputFactory.IS_COALESCING, true); // decode
                                                                 // entities
                                                                 // into one
                                                                 // string

         XMLStreamReader xmlr = xmlif.createXMLStreamReader(inputSource.getByteStream());
         validateXliffFile(xmlr);

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
      if ((check == CHECK.Quick) && !StringUtils.isEmpty(content))
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

   public void setSchemaLocation(String schemaLocation)
   {
      this.schemaLocation = new File(schemaLocation);
   }

   public void setCheck(CHECK check)
   {
      this.check = check;
   }
}
