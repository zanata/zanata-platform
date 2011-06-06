package org.zanata.adapter.xliff;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;

import org.apache.commons.codec.binary.Hex;
import org.xml.sax.InputSource;
import org.zanata.common.ContentType;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;

public class XliffReader
{
   private static final String ELE_FILE = "file";
   private static final String ELE_TRANS_UNIT = "trans-unit";
   private static final String ELE_SOURCE = "source";
   private static final String ELE_CONTEXT_GROUP = "context-group";
   private static final String ELE_CONTEXT = "context";
   private static final String ELE_TARGET = "target";

   private static final String ATTRI_SOURCE_LANGUAGE = "source-language";
   private static final String ATTRI_ID = "id";
   private static final String ATTRI_CONTEXT_TYPE = "context-type";
   private static final String ATTRI_NAME = "name";

   private String srcLang = "";

   public Resource extractTemplate(InputSource inputSource, String docName)
   {
      Resource document = new Resource(docName);
      document.setContentType(ContentType.TextPlain);
      List<TextFlow> resources = document.getTextFlows();
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
               srcLang = getAttributeValue(xmlr, ATTRI_SOURCE_LANGUAGE);
            }
            else if (xmlr.isStartElement() && xmlr.getLocalName().equals(ELE_TRANS_UNIT))
            {
               resources.add(extractTransUnit(xmlr, new TextFlow()));
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
      return document;
   }

   public TranslationsResource extractTarget(Resource doc, InputSource inputSource)
   {
      TranslationsResource document = new TranslationsResource();
      Map<String, TextFlowTarget> targets = new HashMap<String, TextFlowTarget>();

      List<TextFlow> resources = doc.getTextFlows();
      List<String> textFlowIds = new ArrayList<String>();
      for (TextFlow res : resources)
      {
         textFlowIds.add(res.getId());
      }

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
            else if (xmlr.isStartElement())
            {
               if (xmlr.getLocalName().equals(ELE_FILE))
               {
                  srcLang = getAttributeValue(xmlr, ATTRI_SOURCE_LANGUAGE);
               }
               else if (xmlr.getLocalName().equals(ELE_TRANS_UNIT))
               {
                  TextFlowTarget tfTarget = extractTransUnit(xmlr, new TextFlowTarget());
                  targets.put(tfTarget.getResId(), tfTarget);
               }
            }
         }
         for (String id : textFlowIds)
         {
            TextFlowTarget tfTarget = targets.get(id);
            document.getTextFlowTargets().add(tfTarget);
         }
         return document;
      }
      catch (XMLStreamException e)
      {
         throw new RuntimeException("Invalid XLIFF file format  ", e);
      }
   }

   private TextFlow extractTransUnit(XMLStreamReader xmlr, TextFlow textFlow) throws XMLStreamException
   {
      Boolean endTransUnit = false;
      String id = getAttributeValue(xmlr, ATTRI_ID);
      textFlow.setId(generateHash(id));

      // Storing original trans-unit id into simpleComment in dto
      textFlow.getExtensions(true).add(new SimpleComment(id));

      // loop until end element of trans_unit
      while (xmlr.hasNext() && !endTransUnit)
      {
         xmlr.next();
         if (xmlr.isEndElement() && xmlr.getLocalName().equals(ELE_TRANS_UNIT))
         {
            endTransUnit = true;
         }
         else
         {
            if (xmlr.isStartElement() && xmlr.getLocalName().equals(ELE_SOURCE))
            {
               textFlow.setContent(getElementValue(xmlr));
            }
            else if (xmlr.isStartElement() && xmlr.getLocalName().equals(ELE_CONTEXT_GROUP))
            {
               // TODO extract context element
            }
         }
      }
      // Extract source language but ignored at the moment
      // textFlow.setLang(new LocaleId(srcLang));
      return textFlow;
   }

   private TextFlowTarget extractTransUnit(XMLStreamReader xmlr, TextFlowTarget tfTarget) throws XMLStreamException
   {
      Boolean endTransUnit = false;

      String id = getAttributeValue(xmlr, ATTRI_ID);
      tfTarget.setResId(generateHash(id));
      // Storing original trans-unit id into simpleComment in dto
      tfTarget.getExtensions(true).add(new SimpleComment(id));

      // loop until end element of trans_unit
      while (xmlr.hasNext() && !endTransUnit)
      {
         xmlr.next();
         if (xmlr.isEndElement() && xmlr.getLocalName().equals(ELE_TRANS_UNIT))
         {
            endTransUnit = true;
         }
         else
         {
            if (xmlr.isStartElement() && xmlr.getLocalName().equals(ELE_SOURCE))
            {
               tfTarget.setContent(getElementValue(xmlr));
            }
            else if (xmlr.isStartElement() && xmlr.getLocalName().equals(ELE_TARGET))
            {
               tfTarget.setDescription(getElementValue(xmlr));
            }
            else if (xmlr.isStartElement() && xmlr.getLocalName().equals(ELE_CONTEXT_GROUP))
            {
               // TODO extract context element
            }
         }
      }
      // Extract source language but ignored at the moment
      // textFlow.setLang(new LocaleId(srcLang));
      return tfTarget;
   }

   private String getElementValue(XMLStreamReader currentCursor) throws XMLStreamException
   {
      currentCursor.next();
      if (currentCursor.hasText())
      {
         return currentCursor.getText();
      }
      return null;
   }

   private String getAttributeValue(XMLStreamReader xmlr, String attrKey)
   {
      int count = xmlr.getAttributeCount();

      if (count > 0)
      {
         for (int i = 0; i < count; i++)
         {
            if (xmlr.getAttributeLocalName(i).equals(attrKey))
            {
               return xmlr.getAttributeValue(i);
            }
         }
      }
      return null;
   }

   private static String generateHash(String key)
   {
      try
      {
         MessageDigest md5 = MessageDigest.getInstance("MD5");
         md5.reset();
         return new String(Hex.encodeHex(md5.digest(key.getBytes("UTF-8"))));
      }
      catch (Exception exc)
      {
         throw new RuntimeException(exc);
      }
   }

   private static String getEventTypeString(int eventType)
   {
      switch (eventType)
      {
      case XMLEvent.START_ELEMENT:
         return "START_ELEMENT";
      case XMLEvent.END_ELEMENT:
         return "END_ELEMENT";
      case XMLEvent.PROCESSING_INSTRUCTION:
         return "PROCESSING_INSTRUCTION";
      case XMLEvent.CHARACTERS:
         return "CHARACTERS";
      case XMLEvent.COMMENT:
         return "COMMENT";
      case XMLEvent.START_DOCUMENT:
         return "START_DOCUMENT";
      case XMLEvent.END_DOCUMENT:
         return "END_DOCUMENT";
      case XMLEvent.ENTITY_REFERENCE:
         return "ENTITY_REFERENCE";
      case XMLEvent.ATTRIBUTE:
         return "ATTRIBUTE";
      case XMLEvent.DTD:
         return "DTD";
      case XMLEvent.CDATA:
         return "CDATA";
      case XMLEvent.SPACE:
         return "SPACE";
      }
      return "UNKNOWN_EVENT_TYPE , " + eventType;
   }
}
