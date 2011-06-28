package org.zanata.adapter.po;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.lang.StringUtils;
import org.fedorahosted.tennera.jgettext.HeaderFields;
import org.fedorahosted.tennera.jgettext.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.resources.OutputSource;
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
      OutputSource outputSource = new OutputSource(potFile);
      write(outputSource, doc, null);
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
      OutputSource outputSource = new OutputSource(poFile);
      write(outputSource, doc, targetDoc);
   }

   /**
    * Generates a pot or po file from a Resource and/or TranslationsResource,
    * using the publican directory layout. If targetDoc is non-null, a po file
    * will be generated from Resource+TranslationsResource, otherwise a pot file
    * will be generated from the Resource only.
    * 
    * @param outputSource
    * @param document
    * @param targetDoc
    * @throws IOException
    */
   private void write(OutputSource outputSource, Resource document, TranslationsResource targetDoc) throws IOException
   {
      Writer writer = createWriter(outputSource);

      PoHeader poHeader = document.getExtensions(true).findByType(PoHeader.class);
      HeaderFields hf = new HeaderFields();
      if (poHeader == null)
      {
         log.warn("No PO header in document named " + document.getName());
         setDefaultHeaderFields(hf);
      }
      else
      {
         copyToHeaderFields(hf, poHeader.getEntries());
      }
      Message headerMessage = null;
      if (targetDoc != null)
      {
         PoTargetHeader poTargetHeader = targetDoc.getExtensions(true).findByType(PoTargetHeader.class);
         if (poTargetHeader != null)
         {
            copyToHeaderFields(hf, poTargetHeader.getEntries());
            headerMessage = hf.unwrap();
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
         message.setMsgid(textFlow.getContent());
         message.setMsgstr("");
         if (targetDoc != null)
         {
            TextFlowTarget contentData = targets.get(textFlow.getId());
            if (contentData != null)
            {
               if (entryData == null)
               {
                  log.warn("Missing POT entry for text-flow ID " + textFlow.getId());
               }
               else if (!contentData.getResId().equals(textFlow.getId()))
               {
                  throw new RuntimeException("ID from target doesn't match text-flow ID");
               }
               message.setMsgstr(contentData.getContent());
               SimpleComment poComment = contentData.getExtensions().findByType(SimpleComment.class);
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
               switch (contentData.getState())
               {
               case Approved:
                  message.setFuzzy(false);
                  break;
               case NeedReview:
               case New:
                  message.setFuzzy(true);
                  break;
               }
            }
         }

         if (entryData != null)
            copyToMessage(entryData, srcComment, message);

         poWriter.write(message, writer);
         writer.write("\n");
      }
   }

   static Writer createWriter(OutputSource outputSource)
   {
      Writer writer;

      // the writer has first priority
      if (outputSource.getWriter() != null)
         writer = outputSource.getWriter();
      else if (outputSource.getOutputStream() != null)
      { // outputstream has 2nd priority
         if (outputSource.getEncoding() != null)
         {
            writer = new OutputStreamWriter(outputSource.getOutputStream(), Charset.forName(outputSource.getEncoding()));
         }
         else
         {
            writer = new OutputStreamWriter(outputSource.getOutputStream(), Charset.forName("UTF-8"));
         }
      }
      else if (outputSource.getFile() != null)
      { // file has 3rd priority
         try
         {
            OutputStream os = new BufferedOutputStream(new FileOutputStream(outputSource.getFile()));
            if (outputSource.getEncoding() != null)
            {
               writer = new OutputStreamWriter(os, Charset.forName(outputSource.getEncoding()));
            }
            else
            {
               writer = new OutputStreamWriter(os, Charset.forName("UTF-8"));
            }
         }
         catch (FileNotFoundException fnf)
         {
            throw new IllegalArgumentException("outputSource", fnf);
         }
      }
      else
         throw new IllegalArgumentException("outputSource");
      return writer;
   }

   private static void copyTargetHeaderComments(Message headerMessage, PoTargetHeader poTargetHeader)
   {
      for (String s : poTargetHeader.getComment().split("\n"))
      {
         headerMessage.addComment(s);
      }
   }

   static void setDefaultHeaderFields(HeaderFields hf)
   {
      hf.setValue("MIME-Version", "1.0");
      hf.setValue("Content-Type", "text/plain; charset=UTF-8");
      hf.setValue("Content-Transfer-Encoding", "8bit");
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
