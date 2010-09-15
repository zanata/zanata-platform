package net.openl10n.flies.adapter.po;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.resources.LocaleInputSourcePair;
import net.openl10n.flies.rest.dto.deprecated.Document;
import net.openl10n.flies.rest.dto.deprecated.SimpleComment;
import net.openl10n.flies.rest.dto.deprecated.TextFlow;
import net.openl10n.flies.rest.dto.deprecated.TextFlowTarget;
import net.openl10n.flies.rest.dto.po.HeaderEntry;
import net.openl10n.flies.rest.dto.po.PoHeader;
import net.openl10n.flies.rest.dto.po.PoTargetHeader;
import net.openl10n.flies.rest.dto.po.PoTargetHeaders;
import net.openl10n.flies.rest.dto.po.PotEntryData;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.tennera.jgettext.HeaderFields;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.xml.sax.InputSource;

import com.google.common.collect.ImmutableSet;

@Deprecated
public class PoReader
{

   public static final ContentType PO_CONTENT_TYPE = new ContentType("application/x-gettext");

   public static final LocaleId PO_SOURCE_LANGUAGE = LocaleId.EN_US;

   public static final ImmutableSet<String> POT_HEADER_FIELDS = PoReader2.POT_HEADER_FIELDS;

   public static final ImmutableSet<String> PO_HEADER_FIELDS = PoReader2.PO_HEADER_FIELDS;

   public PoReader()
   {
   }

   public void extractTarget(Document document, InputSource inputSource, LocaleId targetLocaleId)
   {
      MessageStreamParser messageParser = PoReader2.createParser(inputSource);

      List<TextFlow> resources = document.getTextFlows();
      Map<String, TextFlow> resourceMap = new HashMap<String, TextFlow>(resources.size());
      for (TextFlow res : resources)
      {
         resourceMap.put(res.getContent(), res);
      }

      while (messageParser.hasNext())
      {
         Message message = messageParser.next();

         if (message.isHeader())
         {
            // add target header data
            PoTargetHeader poHeader = new PoTargetHeader();
            extractPoHeader(message, poHeader);
            poHeader.setTargetLanguage(targetLocaleId);
            PoTargetHeaders targetHeaders = document.getOrAddExtension(PoTargetHeaders.class);
            targetHeaders.getHeaders().add(poHeader);
         }
         else if (message.isObsolete())
         {
            // TODO append obsolete
         }
         else if (message.isPlural())
         {
            // TODO skip for now
         }
         else
         {
            TextFlow tf = resourceMap.get(message.getMsgid());
            if (tf != null)
            {
               String id = PoReader2.createId(message);

               matchIdOrFail(tf.getId(), id);

               // add the target content (msgstr)
               TextFlowTarget tfTarget = new TextFlowTarget(tf, targetLocaleId);
               tfTarget.setContent(message.getMsgstr());
               tfTarget.setState(PoReader2.getContentState(message));
               tf.addTarget(tfTarget);

               // add the PO comment
               tfTarget.getExtensions().add(new SimpleComment(StringUtils.join(message.getComments(), "\n")));
            }
            else
            {
               // TODO append obsolete

            }
         }
      }

   }

   public void extractTarget(Document document, LocaleInputSourcePair localeInputSourcePair)
   {
      extractTarget(document, localeInputSourcePair.getInputSource(), localeInputSourcePair.getLocaleId());
   }

   public void extractTargets(Document documentPart, LocaleInputSourcePair... localeInputSourcePairs)
   {
      // TODO: parsing in parallel might be faster than one-by-one.
      for (LocaleInputSourcePair localeInputSourcePair : localeInputSourcePairs)
      {
         extractTarget(documentPart, localeInputSourcePair);
      }
   }

   private static void extractPotHeader(Message message, PoHeader potHeader)
   {
      potHeader.getComment().setValue(StringUtils.join(message.getComments(), "\n"));

      HeaderFields hf = HeaderFields.wrap(message);
      for (String key : hf.getKeys())
      {
         String val = hf.getValue(key);
         if (POT_HEADER_FIELDS.contains(key))
         {
            potHeader.getEntries().add(new HeaderEntry(key, val));
         }
         // we add any custom fields to the PO only, not the POT
      }
   }

   private static void extractPoHeader(Message message, PoHeader poHeader)
   {
      poHeader.getComment().setValue(StringUtils.join(message.getComments(), "\n"));

      HeaderFields hf = HeaderFields.wrap(message);
      for (String key : hf.getKeys())
      {
         String val = hf.getValue(key);
         if (PO_HEADER_FIELDS.contains(key))
         {
            poHeader.getEntries().add(new HeaderEntry(key, val));
         }
         else if (!POT_HEADER_FIELDS.contains(key))
         {
            // we add any custom fields to the PO only, not the POT
            // TODO this should be configurable
            poHeader.getEntries().add(new HeaderEntry(key, val));
         }
      }
   }

   public void extractTemplate(Document document, InputSource inputSource, LocaleId sourceLocaleId)
   {
      MessageStreamParser messageParser = PoReader2.createParser(inputSource);

      document.setLang(sourceLocaleId);
      document.setContentType(PO_CONTENT_TYPE);
      List<TextFlow> resources = document.getTextFlows();

      boolean headerFound = false;
      while (messageParser.hasNext())
      {
         Message message = messageParser.next();

         if (message.isHeader())
         {
            if (headerFound)
               throw new IllegalStateException("found a second header!");
            headerFound = true;

            // store POT data
            PoHeader potHeader = new PoHeader();
            extractPotHeader(message, potHeader);
            document.getExtensions(true).add(potHeader);

         }
         else if (message.isObsolete())
         {
            // TODO append obsolete
         }
         else if (message.isPlural())
         {
            // TODO skip for now
         }
         else
         {
            String id = PoReader2.createId(message);
            // add the content (msgid)
            TextFlow tf = new TextFlow(id, sourceLocaleId);
            tf.setContent(message.getMsgid());
            resources.add(tf);

            // add the entry header POT fields
            tf.getExtensions().add(createFromMessage(id, message));
         }

      }
   }

   private static PotEntryData createFromMessage(String id, Message message)
   {
      PotEntryData data = new PotEntryData(id);
      if (message.getMsgctxt() != null)
      {
         data.setContext(message.getMsgctxt());
      }
      data.getExtractedComment().setValue(StringUtils.join(message.getExtractedComments(), "\n"));
      data.getFlags().addAll(message.getFormats());
      data.getReferences().addAll(message.getSourceReferences());
      return data;
   }

   public void extractTemplate(Document document, InputSource inputSource)
   {
      extractTemplate(document, inputSource, PO_SOURCE_LANGUAGE);
   }

   public void extractTemplate(Document document, LocaleInputSourcePair localeInputSourcePair)
   {
      extractTemplate(document, localeInputSourcePair.getInputSource(), localeInputSourcePair.getLocaleId());
   }

   private static void matchIdOrFail(String id, String id2)
   {
      if (!id.equals(id2))
      {
         throw new RuntimeException("id matching failed!");
      }
   }

}
