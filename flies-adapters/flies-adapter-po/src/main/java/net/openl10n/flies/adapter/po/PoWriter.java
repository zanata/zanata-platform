package net.openl10n.flies.adapter.po;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import net.openl10n.flies.common.LocaleId;
import net.openl10n.flies.resources.OutputSource;
import net.openl10n.flies.rest.dto.deprecated.Document;
import net.openl10n.flies.rest.dto.deprecated.IExtensible;
import net.openl10n.flies.rest.dto.deprecated.SimpleComment;
import net.openl10n.flies.rest.dto.deprecated.TextFlow;
import net.openl10n.flies.rest.dto.deprecated.TextFlowTarget;
import net.openl10n.flies.rest.dto.deprecated.TextFlowTargets;
import net.openl10n.flies.rest.dto.po.PoHeader;
import net.openl10n.flies.rest.dto.po.PoTargetHeader;
import net.openl10n.flies.rest.dto.po.PoTargetHeaders;
import net.openl10n.flies.rest.dto.po.PotEntryData;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.tennera.jgettext.HeaderFields;
import org.fedorahosted.tennera.jgettext.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class PoWriter
{
   private static final Logger log = LoggerFactory.getLogger(PoWriter.class);
   private final org.fedorahosted.tennera.jgettext.PoWriter poWriter = new org.fedorahosted.tennera.jgettext.PoWriter();

   public PoWriter()
   {
   }

   public void write(final Document doc, final File baseDir, boolean includePot) throws IOException
   {
      Set<LocaleId> targetLangs = new HashSet<LocaleId>();
      for (TextFlow resource : doc.getTextFlows())
      {
         for (TextFlowTarget target : getTargets(resource))
         {
            targetLangs.add(target.getLang());
         }
      }
      if (includePot)
      {
         // write the POT file to pot/$name.pot
         File potDir = new File(baseDir, "pot");
         potDir.mkdirs();
         File potFile = new File(potDir, doc.getName() + ".pot");
         OutputSource outputSource = new OutputSource(potFile);
         write(doc, outputSource, null);
      }
      // write the PO files to $locale/$name.po
      for (LocaleId locale : targetLangs)
      {
         File localeDir = new File(baseDir, locale.toString());
         localeDir.mkdirs();
         File poFile = new File(localeDir, doc.getName() + ".po");
         OutputSource outputSource = new OutputSource(poFile);
         write(doc, outputSource, locale);
      }
   }

   public void write(final Document document, OutputSource outputSource, LocaleId locale) throws IOException
   {
      if (document == null)
         throw new IllegalArgumentException("document");

      Writer writer = PoWriter2.createWriter(outputSource);

      // if(!document.getTargetLanguages().contains(locale))
      // throw new RuntimeException("could not find target locale");

      PoHeader poHeader = document.getExtension(PoHeader.class);
      HeaderFields hf = new HeaderFields();
      if (poHeader == null)
      {
         log.warn("No PO header in document with ID " + document.getId());
         PoWriter2.setDefaultHeaderFields(hf);
      }
      else
      {
         PoWriter2.copyToHeaderFields(hf, poHeader.getEntries());
      }
      Message headerMessage = null;
      if (locale != null)
      {
         PoTargetHeaders poTargetHeaders = document.getExtension(PoTargetHeaders.class);
         if (poTargetHeaders != null)
         {
            PoTargetHeader poTargetHeader = poTargetHeaders.getByLocale(locale);
            if (poTargetHeader != null)
            {
               PoWriter2.copyToHeaderFields(hf, poTargetHeader.getEntries());
               headerMessage = hf.unwrap();
               copyTargetHeaderComments(headerMessage, poTargetHeader);
            }
         }
      }
      if (headerMessage == null)
      {
         headerMessage = hf.unwrap();
      }
      poWriter.write(headerMessage, writer);
      writer.write("\n");

      // first write header
      for (TextFlow textFlow : document.getTextFlows())
      {

         PotEntryData entryData = textFlow.getExtension(PotEntryData.class);
         Message message = new Message();
         message.setMsgid(textFlow.getContent());
         message.setMsgstr("");
         if (locale != null)
         {
            TextFlowTargets entryTargets = textFlow.getExtension(TextFlowTargets.class);
            if (entryTargets != null)
            {
               TextFlowTarget contentData = entryTargets.getByLocale(locale);
               if (contentData != null)
               {
                  if (entryData == null)
                  {
                     log.warn("Missing POT entry for text-flow ID " + textFlow.getId());
                  }
                  else if (!entryData.getId().equals(textFlow.getId()))
                  {
                     throw new RuntimeException("ID from POT entry doesn't match text-flow ID");
                  }
                  else if (!contentData.getId().equals(textFlow.getId()))
                  {
                     throw new RuntimeException("ID from target doesn't match text-flow ID");
                  }
                  message.setMsgstr(contentData.getContent());
                  SimpleComment poComment = contentData.getExtension(SimpleComment.class);
                  if (poComment != null)
                  {
                     String[] comments = poComment.getValue().split("\n");
                     if (comments.length == 1 && comments[0].isEmpty())
                     {

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
         }

         if (entryData != null)
            copyToMessage(entryData, message);

         poWriter.write(message, writer);
         writer.write("\n");
      }
   }

   private void copyTargetHeaderComments(Message headerMessage, PoTargetHeader poTargetHeader)
   {
      for (String s : poTargetHeader.getComment().getValue().split("\n"))
      {
         headerMessage.addComment(s);
      }
   }

   private static void copyToMessage(PotEntryData data, Message message)
   {
      String context = data.getContext();
      if (context != null)
         message.setMsgctxt(context);
      String[] comments = StringUtils.splitPreserveAllTokens(data.getExtractedComment().getValue(), "\n");
      if (!(comments.length == 1 && comments[0].isEmpty()))
      {
         for (String comment : comments)
         {
            message.addExtractedComment(comment);
         }
      }
      for (String flag : data.getFlags())
      {
         message.addFormat(flag);
      }
      for (String ref : data.getReferences())
      {
         message.addSourceReference(ref);
      }
   }

   private static Set<TextFlowTarget> getTargets(IExtensible resource)
   {
      TextFlowTargets targets = resource.getExtension(TextFlowTargets.class);
      if (targets != null)
      {
         return targets.getTargets();
      }
      else
      {
         return Collections.EMPTY_SET;
      }
   }
}
