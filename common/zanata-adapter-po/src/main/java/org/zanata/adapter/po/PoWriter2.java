package org.zanata.adapter.po;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.tennera.jgettext.HeaderFields;
import org.fedorahosted.tennera.jgettext.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.common.ContentState;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.HeaderEntry;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PoTargetHeader;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.PathUtil;

public class PoWriter2
{
   private static final Logger log = LoggerFactory.getLogger(PoWriter2.class);
   private final org.fedorahosted.tennera.jgettext.PoWriter poWriter = new org.fedorahosted.tennera.jgettext.PoWriter();

   public PoWriter2()
   {
   }

   private void mkdirs(File dir) throws IOException
   {
      if (!dir.exists())
      {
         if (!dir.mkdirs())
            throw new IOException("unable to create output directory: " + dir);
      }
   }

   /**
    * Generates a pot file from Resource (document), using the publican
    * directory layout.
    * 
    * @param baseDir
    * @param doc
    * @throws IOException
    */
   public void writePot(File baseDir, Resource doc) throws IOException
   {
      // write the POT file to pot/$name.pot
      File potDir = new File(baseDir, "pot");
      writePotToDir(potDir, doc);
   }

   /**
    * Generates a pot file from Resource (document), in the specified directory.
    * 
    * @param potDir
    * @param doc
    * @throws IOException
    */
   public void writePotToDir(File potDir, Resource doc) throws IOException
   {
      // write the POT file to $potDir/$name.pot
      File potFile = new File(potDir, doc.getName() + ".pot");
      PathUtil.makeParents(potFile);
      FileWriter fWriter = new FileWriter(potFile);
      write(fWriter, "UTF-8", doc, null);
   }

   /**
    * Generates a po file from a Resource and a TranslationsResource, using the
    * publican directory layout.
    * 
    * @param baseDir
    * @param doc
    * @param locale
    * @param targetDoc
    * @throws IOException
    */
   public void writePo(File baseDir, Resource doc, String locale, TranslationsResource targetDoc) throws IOException
   {
      // write the PO file to $locale/$name.po
      File localeDir = new File(baseDir, locale);
      File poFile = new File(localeDir, doc.getName() + ".po");
      mkdirs(poFile.getParentFile());
      FileWriter fWriter = new FileWriter(poFile);
      write(fWriter, "UTF-8", doc, targetDoc);
   }
   
   /**
    * Generates a po file from a Resource and a TranslationsResource, writing it
    * directly to an output stream.
    * 
    * @param stream
    * @param doc
    * @param targetDoc
    * @throws IOException
    */
   public void writePo(OutputStream stream, String charset, Resource doc, TranslationsResource targetDoc) throws IOException
   {
      OutputStreamWriter osWriter = new OutputStreamWriter(stream, charset);
      write(osWriter, charset, doc, targetDoc);
   }

   /**
    * Generates a pot or po file from a Resource and/or TranslationsResource,
    * using the publican directory layout. If targetDoc is non-null, a po file
    * will be generated from Resource+TranslationsResource, otherwise a pot file
    * will be generated from the Resource only.
    * 
    * @param writer
    * @param document
    * @param targetDoc
    * @throws IOException
    */
   private void write(Writer writer, String charset, Resource document, TranslationsResource targetDoc) throws IOException
   {
      PoHeader poHeader = document.getExtensions(true).findByType(PoHeader.class);
      HeaderFields hf = new HeaderFields();
      if (poHeader == null)
      {
         log.warn("No PO header in document named " + document.getName());
      }
      else
      {
         copyToHeaderFields(hf, poHeader.getEntries());
      }
      setEncodingHeaderFields(hf, charset);
      Message headerMessage = null;
      if (targetDoc != null)
      {
         PoTargetHeader poTargetHeader = targetDoc.getExtensions(true).findByType(PoTargetHeader.class);
         if (poTargetHeader != null)
         {
            copyToHeaderFields(hf, poTargetHeader.getEntries());
            headerMessage = hf.unwrap();
            headerMessage.setFuzzy(false); // By default, header message unwraps as fuzzy, so avoid it
            copyTargetHeaderComments(headerMessage, poTargetHeader);
         }
      }
      if (headerMessage == null)
      {
         headerMessage = hf.unwrap();
      }
      poWriter.write(headerMessage, writer);
      writer.write("\n");
      Map<String, TextFlowTarget> targets = null;
      if (targetDoc != null)
      {
         targets = new HashMap<String, TextFlowTarget>();
         for (TextFlowTarget target : targetDoc.getTextFlowTargets())
         {
            targets.put(target.getResId(), target);
         }
      }

      // first write header
      for (TextFlow textFlow : document.getTextFlows())
      {

         PotEntryHeader entryData = textFlow.getExtensions(true).findByType(PotEntryHeader.class);
         SimpleComment srcComment = textFlow.getExtensions().findByType(SimpleComment.class);
         Message message = new Message();
         List<String> tfContents = textFlow.getContents();
         switch (tfContents.size())
         {
         case 2:
            message.setMsgidPlural(tfContents.get(1));
            // fall through...
         case 1:
            message.setMsgid(tfContents.get(0));
            break;
         default:
            throw new RuntimeException("POT format only supports 2 plural forms: resId=" + textFlow.getId());
         }

         message.setMsgstr("");
         if (targetDoc != null)
         {
            TextFlowTarget tfTarget = targets.get(textFlow.getId());
            if (tfTarget != null)
            {
               if (entryData == null)
               {
                  log.warn("Missing POT entry for text-flow ID " + textFlow.getId());
               }
               else if (!tfTarget.getResId().equals(textFlow.getId()))
               {
                  throw new RuntimeException("ID from target doesn't match text-flow ID");
               }
               List<String> tftContents = tfTarget.getContents();

               if (message.isPlural())
               {
                  for (int i = 0; i < tftContents.size(); i++)
                  {
                     message.addMsgstrPlural(tftContents.get(i), i);
                  }
               }
               else
               {
                  if (tftContents.size() != 1)
                  {
                     throw new RuntimeException("plural forms not enabled for this text flow: resId=" + textFlow.getId());
                  }
                  message.setMsgstr(tftContents.get(0));
               }

               SimpleComment poComment = tfTarget.getExtensions().findByType(SimpleComment.class);
               if (poComment != null)
               {
                  String[] comments = poComment.getValue().split("\n");
                  if (comments.length == 1 && comments[0].isEmpty())
                  {
                     // nothing
                  }
                  else
                  {
                     for (String comment : comments)
                     {
                        message.getComments().add(comment);
                     }
                  }
               }
               if (tfTarget.getState() == ContentState.NeedReview)
               {
                  message.setFuzzy(true);
               }
            }
         }

         if (entryData != null)
            copyToMessage(entryData, srcComment, message);

         poWriter.write(message, writer);
         writer.write("\n");
      }
   }

   private static void copyTargetHeaderComments(Message headerMessage, PoTargetHeader poTargetHeader)
   {
      for (String s : poTargetHeader.getComment().split("\n"))
      {
         headerMessage.addComment(s);
      }
   }

   static void setEncodingHeaderFields(HeaderFields hf, String charset)
   {
      hf.setValue(HeaderFields.KEY_MimeVersion, "1.0");
      hf.setValue(HeaderFields.KEY_ContentTransferEncoding, "8bit");

      String ct, contentType = hf.getValue(HeaderFields.KEY_ContentType);
      if (contentType == null)
      {
         ct = "text/plain; charset=" + charset;
      }
      else
      {
         ct = contentType.replaceFirst("charset=[^;]*", "charset=" + charset);
      }
      hf.setValue(HeaderFields.KEY_ContentType, ct);
   }

   static void copyToHeaderFields(HeaderFields hf, final List<HeaderEntry> entries)
   {
      for (HeaderEntry e : entries)
      {
         hf.setValue(e.getKey(), e.getValue());
      }
   }

   private static void copyToMessage(PotEntryHeader data, SimpleComment simpleComment, Message message)
   {
      if (data != null)
      {
         String context = data.getContext();
         if (context != null)
            message.setMsgctxt(context);
         for (String flag : data.getFlags())
         {
            message.addFormat(flag);
         }
         for (String ref : data.getReferences())
         {
            message.addSourceReference(ref);
         }
      }
      if (simpleComment != null)
      {
         String[] comments = StringUtils.splitPreserveAllTokens(simpleComment.getValue(), "\n");
         if (!(comments.length == 1 && comments[0].isEmpty()))
         {
            for (String comment : comments)
            {
               message.addExtractedComment(comment);
            }
         }
      }
   }

}
