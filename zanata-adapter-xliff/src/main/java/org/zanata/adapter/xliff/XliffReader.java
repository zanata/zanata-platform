package org.zanata.adapter.xliff;

import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
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
   private static final Logger log = LoggerFactory.getLogger(XliffReader.class);

   LocaleId srcLang;

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
               // FIXME it only ensures that we silently ignore extra file elements!
               break;
            }
         }
      }
      catch (XMLStreamException e)
      {
         throw new RuntimeException("Invalid XLIFF file format  ", e);
      }
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
               String content = getElementValue(xmlr, ELE_SOURCE, getContentElementList());
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
               String content = getElementValue(xmlr, ELE_TARGET, getContentElementList());
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
   private String getElementValue(XMLStreamReader reader, String elementName, Collection<String> legalElements) throws XMLStreamException
   {
      boolean keepReading = true;
      StringBuilder contents = new StringBuilder();

      reader.next();

      String localName = reader.getLocalName();
      if ((reader.isEndElement() || reader.isStartElement()) && localName.equals(elementName))
      {
         keepReading = false;
      }

      while (keepReading)
      {
         if (reader.hasText()) // if the value in element is text
         {
            contents.append(reader.getText());
         }
         else
         {
            // if value in element is a xml element; invalid text
            if (reader.isStartElement() || reader.isEndElement())
            {
               if (legalElements == null || legalElements.contains(localName))
               {
                  throw new RuntimeException("Sorry, Zanata does not support elements inside " + elementName + ": " + localName);
               }
               else
               {
                  throw new RuntimeException("Invalid XLIFF: " + localName + " is not legal inside " + elementName);
               }
            }
         }
         reader.next();
         localName = reader.getLocalName();

         if ((reader.isEndElement() || reader.isStartElement()) && localName.equals(elementName))
         {
            keepReading = false;
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
}
