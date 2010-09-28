package net.openl10n.flies.adapter.po;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.common.ContentType;
import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.rest.dto.extensions.comment.SimpleComment;
import net.openl10n.flies.rest.dto.extensions.gettext.HeaderEntry;
import net.openl10n.flies.rest.dto.extensions.gettext.PoHeader;
import net.openl10n.flies.rest.dto.extensions.gettext.PoTargetHeader;
import net.openl10n.flies.rest.dto.extensions.gettext.PotEntryHeader;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.TextFlow;
import net.openl10n.flies.rest.dto.resource.TextFlowTarget;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang.StringUtils;
import org.fedorahosted.tennera.jgettext.HeaderFields;
import org.fedorahosted.tennera.jgettext.Message;
import org.fedorahosted.tennera.jgettext.catalog.parse.MessageStreamParser;
import org.xml.sax.InputSource;

import com.google.common.collect.ImmutableSet;

public class PoReader2
{

   public static final ContentType PO_CONTENT_TYPE = new ContentType("application/x-gettext");

   public static final ImmutableSet<String> POT_HEADER_FIELDS = ImmutableSet.of(HeaderFields.KEY_ProjectIdVersion, HeaderFields.KEY_ReportMsgidBugsTo, HeaderFields.KEY_PotCreationDate, HeaderFields.KEY_MimeVersion, HeaderFields.KEY_ContentType, HeaderFields.KEY_ContentTransferEncoding);

   public static final ImmutableSet<String> PO_HEADER_FIELDS = ImmutableSet.of(HeaderFields.KEY_PoRevisionDate, HeaderFields.KEY_LastTranslator, HeaderFields.KEY_LanguageTeam, HeaderFields.KEY_Language, "Plural-Forms", "X-Generator");

   // useful for testing
   private static final boolean GENERATE_OPAQUE_IDS = false;

   public PoReader2()
   {
   }

   public TranslationsResource extractTarget(InputSource inputSource, Resource srcDoc)
   {
      TranslationsResource document = new TranslationsResource();
      MessageStreamParser messageParser = createParser(inputSource);

      List<TextFlow> resources = srcDoc.getTextFlows();
      List<String> textFlowIds = new ArrayList<String>();
      for (TextFlow res : resources)
      {
         textFlowIds.add(res.getId());
      }
      Map<String, TextFlowTarget> targets = new HashMap<String, TextFlowTarget>();

      boolean headerFound = false;
      while (messageParser.hasNext())
      {
         Message message = messageParser.next();

         if (message.isHeader())
         {
            if (headerFound)
               throw new IllegalStateException("found a second header!");
            headerFound = true;

            // add target header data
            PoTargetHeader poHeader = new PoTargetHeader();
            extractPoHeader(message, poHeader);
            document.getExtensions(true).add(poHeader);
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
            String id = createId(message);
            if (!textFlowIds.contains(id))
            {
               // TODO append obsolete
            }
            else
            {
               // add the target content (msgstr)
               TextFlowTarget tfTarget = new TextFlowTarget();
               tfTarget.setResId(id);

               tfTarget.setContent(message.getMsgstr());
               tfTarget.setState(getContentState(message));

               // add the PO comment
               tfTarget.getExtensions(true).add(new SimpleComment(StringUtils.join(message.getComments(), "\n")));
               targets.put(id, tfTarget);
            }
         }
      }
      // this ensures that the TextFlowTargets have the same order as the
      // TextFlows in the Document:
      for (String id : textFlowIds)
      {
         TextFlowTarget tfTarget = targets.get(id);
         document.getTextFlowTargets().add(tfTarget);
      }

      return document;
   }

   private static void extractPotHeader(Message message, PoHeader potHeader)
   {
      potHeader.setComment(StringUtils.join(message.getComments(), "\n"));

      HeaderFields hf = HeaderFields.wrap(message);
      for (String key : hf.getKeys())
      {
         String val = hf.getValue(key);
         if (POT_HEADER_FIELDS.contains(key))
         {
            potHeader.getEntries().add(new HeaderEntry(key, val));
         }
         // we add any custom fields to the PO only, not the POT
         // TODO this should be configurable
      }
   }

   private static void extractPoHeader(Message message, PoTargetHeader poHeader)
   {
      poHeader.setComment(StringUtils.join(message.getComments(), "\n"));

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

   public Resource extractTemplate(InputSource inputSource, LocaleId sourceLocaleId, String docName)
   {
      Resource document = new Resource(docName);
      MessageStreamParser messageParser = createParser(inputSource);

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
            String id = createId(message);
            // add the content (msgid)
            TextFlow tf = new TextFlow(id, sourceLocaleId);
            tf.setContent(message.getMsgid());
            resources.add(tf);

            // add the entry header POT fields
            tf.getExtensions(true).add(createPotEntryHeader(message));
            tf.getExtensions().add(createSimpleComment(message));
         }
      }
      return document;
   }

   private static PotEntryHeader createPotEntryHeader(Message message)
   {
      PotEntryHeader data = new PotEntryHeader();
      if (message.getMsgctxt() != null)
      {
         data.setContext(message.getMsgctxt());
      }
      data.getFlags().addAll(message.getFormats());
      data.getReferences().addAll(message.getSourceReferences());
      return data;
   }

   private static SimpleComment createSimpleComment(Message message)
   {
      String comment = StringUtils.join(message.getExtractedComments(), "\n");
      SimpleComment result = new SimpleComment(comment);
      return result;
   }

   static MessageStreamParser createParser(InputSource inputSource)
   {
      MessageStreamParser messageParser;
      if (inputSource.getCharacterStream() != null)
         messageParser = new MessageStreamParser(inputSource.getCharacterStream());
      else if (inputSource.getByteStream() != null)
      {
         if (inputSource.getEncoding() != null)
            messageParser = new MessageStreamParser(inputSource.getByteStream(), Charset.forName(inputSource.getEncoding()));
         else
            messageParser = new MessageStreamParser(inputSource.getByteStream(), Charset.forName("UTF-8"));
      }
      else if (inputSource.getSystemId() != null)
      {
         try
         {
            URL url = new URL(inputSource.getSystemId());

            if (inputSource.getEncoding() != null)
               messageParser = new MessageStreamParser(url.openStream(), Charset.forName(inputSource.getEncoding()));
            else
               messageParser = new MessageStreamParser(url.openStream(), Charset.forName("UTF-8"));
         }
         catch (IOException e)
         {
            // TODO throw stronger typed exception
            throw new RuntimeException("failed to get input from url in inputSource", e);
         }
      }
      else
         // TODO throw stronger typed exception
         throw new RuntimeException("not a valid inputSource");

      return messageParser;
   }

   static ContentState getContentState(Message message)
   {
      if (message.getMsgstr() == null || message.getMsgstr().isEmpty())
         return ContentState.New;
      else if (message.isFuzzy())
         return ContentState.NeedReview;
      else
         return ContentState.Approved;
   }

   static String createId(Message message)
   {
      String sep;
      if (GENERATE_OPAQUE_IDS)
         sep = "\u0000";
      else
         sep = ":";
      String hashBase = message.getMsgctxt() == null ? message.getMsgid() : message.getMsgctxt() + sep + message.getMsgid();
      if (GENERATE_OPAQUE_IDS)
         return generateHash(hashBase);
      else
         return hashBase;
   }

   public static String generateHash(String key)
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

}
