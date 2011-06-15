package org.zanata.adapter.xliff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.lang.StringUtils;
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
                  TextFlow textFlow = new TextFlow();
                  extractTransUnit(xmlr, textFlow);
                  document.getTextFlows().add(textFlow);
               }
               else
               {
                  TextFlowTarget tfTarget = new TextFlowTarget();
                  extractTransUnit(xmlr, tfTarget);
                  if(!StringUtils.isEmpty(tfTarget.getContent())){
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

   
   private void extractTransUnit(XMLStreamReader xmlr, TextFlow textFlow) throws XMLStreamException
   {
      Boolean endTransUnit = false;
      textFlow.setId(getAttributeValue(xmlr, ATTRI_ID));

      while (xmlr.hasNext() && !endTransUnit)
      {
         xmlr.next();
         if (xmlr.isEndElement() && xmlr.getLocalName().equals(ELE_TRANS_UNIT))
            endTransUnit = true;
         else
         {
            if (xmlr.isStartElement() && xmlr.getLocalName().equals(ELE_SOURCE))
            {
               textFlow.setContent(getElementValue(xmlr));
            }
            else if (xmlr.isStartElement() && xmlr.getLocalName().equals(ELE_CONTEXT_GROUP))
            {
               textFlow.getExtensions(true).addAll(extractContextList(xmlr));
            }
         }
      }
      textFlow.setLang(srcLang);
   }
   
   private void extractTransUnit(XMLStreamReader xmlr, TextFlowTarget textFlow) throws XMLStreamException
   {
      Boolean endTransUnit = false;
      textFlow.setResId(getAttributeValue(xmlr, ATTRI_ID));

      while (xmlr.hasNext() && !endTransUnit)
      {
         xmlr.next();
         if (xmlr.isEndElement() && xmlr.getLocalName().equals(ELE_TRANS_UNIT))
            endTransUnit = true;
         else
         {
            if (xmlr.isStartElement() && xmlr.getLocalName().equals(ELE_TARGET))
            {
               textFlow.setContent(getElementValue(xmlr));
            }
            else if (xmlr.isStartElement() && xmlr.getLocalName().equals(ELE_CONTEXT_GROUP))
            {
               textFlow.getExtensions(true).addAll(extractContextList(xmlr));
            }
         }
      }
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
         if (xmlr.isEndElement() && xmlr.getLocalName().equals(ELE_CONTEXT_GROUP))
            endContextGroup = true;
         else
         {
            if (xmlr.isStartElement() && xmlr.getLocalName().equals(ELE_CONTEXT))
            {
               StringBuilder sb = new StringBuilder();
               sb.append(contextGroup);// context-group
               sb.append(DELIMITER);
               sb.append(getAttributeValue(xmlr, ATTRI_CONTEXT_TYPE));// context-type
               sb.append(DELIMITER);
               sb.append(getElementValue(xmlr));// value
               contextList.add(new SimpleComment(sb.toString()));
            }
         }
      }
      return contextList;
   }

   /**
    * Extract given element's value
    * 
    * @param currentCursor
    * @return
    * @throws XMLStreamException
    */
   private String getElementValue(XMLStreamReader currentCursor) throws XMLStreamException
   {
      currentCursor.next();
      if (currentCursor.hasText())
         return currentCursor.getText();

      return null;
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

   // private static String getEventTypeString(int eventType)
   // {
   // switch (eventType)
   // {
   // case XMLEvent.START_ELEMENT:
   // return "START_ELEMENT";
   // case XMLEvent.END_ELEMENT:
   // return "END_ELEMENT";
   // case XMLEvent.PROCESSING_INSTRUCTION:
   // return "PROCESSING_INSTRUCTION";
   // case XMLEvent.CHARACTERS:
   // return "CHARACTERS";
   // case XMLEvent.COMMENT:
   // return "COMMENT";
   // case XMLEvent.START_DOCUMENT:
   // return "START_DOCUMENT";
   // case XMLEvent.END_DOCUMENT:
   // return "END_DOCUMENT";
   // case XMLEvent.ENTITY_REFERENCE:
   // return "ENTITY_REFERENCE";
   // case XMLEvent.ATTRIBUTE:
   // return "ATTRIBUTE";
   // case XMLEvent.DTD:
   // return "DTD";
   // case XMLEvent.CDATA:
   // return "CDATA";
   // case XMLEvent.SPACE:
   // return "SPACE";
   // }
   // return "UNKNOWN_EVENT_TYPE , " + eventType;
   // }
}
