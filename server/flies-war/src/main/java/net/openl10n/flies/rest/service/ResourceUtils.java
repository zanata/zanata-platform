package net.openl10n.flies.rest.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import net.openl10n.flies.common.ResourceType;
import net.openl10n.flies.model.HDocument;
import net.openl10n.flies.model.HLocale;
import net.openl10n.flies.model.HPerson;
import net.openl10n.flies.model.HSimpleComment;
import net.openl10n.flies.model.HTextFlow;
import net.openl10n.flies.model.HTextFlowTarget;
import net.openl10n.flies.model.HasSimpleComment;
import net.openl10n.flies.model.po.HPoHeader;
import net.openl10n.flies.model.po.HPoTargetHeader;
import net.openl10n.flies.model.po.HPotEntryData;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.extensions.comment.SimpleComment;
import net.openl10n.flies.rest.dto.extensions.gettext.AbstractResourceMetaExtension;
import net.openl10n.flies.rest.dto.extensions.gettext.PoHeader;
import net.openl10n.flies.rest.dto.extensions.gettext.PoTargetHeader;
import net.openl10n.flies.rest.dto.extensions.gettext.PotEntryHeader;
import net.openl10n.flies.rest.dto.extensions.gettext.TextFlowExtension;
import net.openl10n.flies.rest.dto.extensions.gettext.TextFlowTargetExtension;
import net.openl10n.flies.rest.dto.extensions.gettext.TranslationsResourceExtension;
import net.openl10n.flies.rest.dto.resource.AbstractResourceMeta;
import net.openl10n.flies.rest.dto.resource.ExtensionSet;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.TextFlow;
import net.openl10n.flies.rest.dto.resource.TextFlowTarget;
import net.openl10n.flies.util.StringUtil;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

@Name("resourceUtils")
@Scope(ScopeType.STATELESS)
@AutoCreate
@BypassInterceptors
public class ResourceUtils
{
   /**
    * Newline character used for multi-line comments
    */
   private static final char NEWLINE = '\n';

   private static final String ZANATA_TAG = "#zanata";

   private static final Log log = Logging.getLog(ResourceUtils.class);

   
   /**
    * Merges the list of TextFlows into the target HDocument, adding and obsoleting TextFlows as necessary.
    * @param from
    * @param to
    * @return
    */
   boolean transferFromTextFlows(List<TextFlow> from, HDocument to, Set<String> enabledExtensions, int nextDocRev)
   {
      boolean changed = false;
      to.getTextFlows().clear();
      Set<String> incomingIds = new HashSet<String>();
      Set<String> previousIds = new HashSet<String>(to.getAllTextFlows().keySet());
      for (TextFlow tf : from)
      {
         if (!incomingIds.add(tf.getId()))
         {
            Response response = Response.status(Status.BAD_REQUEST).entity("encountered TextFlow with duplicate ID " + tf.getId()).build();
            throw new WebApplicationException(response);
         }
         HTextFlow textFlow;
         if (previousIds.contains(tf.getId()))
         {
            previousIds.remove(tf.getId());
            textFlow = to.getAllTextFlows().get(tf.getId());
            textFlow.setObsolete(false);
            // avoid changing revision when resurrecting an unchanged TF
            if (transferFromTextFlow(tf, textFlow, enabledExtensions))
            {
               textFlow.setRevision(nextDocRev);
               changed = true;
               log.debug("TextFlow with id {0} has changed", tf.getId());
            }
         }
         else
         {
            textFlow = new HTextFlow();
            textFlow.setDocument(to);
            textFlow.setResId(tf.getId());
            textFlow.setRevision(nextDocRev);
            transferFromTextFlow(tf, textFlow, enabledExtensions);
            changed = true;
            log.debug("TextFlow with id {0} is new", tf.getId());
         }
         to.getTextFlows().add(textFlow);
         to.getAllTextFlows().put(textFlow.getResId(), textFlow);
      }

      // set remaining textflows to obsolete.
      for (String id : previousIds)
      {
         HTextFlow textFlow = to.getAllTextFlows().get(id);
         if (!textFlow.isObsolete())
         {
            changed = true;
            log.debug("TextFlow with id {0} is now obsolete", id);
            textFlow.setRevision(to.getRevision());
            textFlow.setObsolete(true);
         }
      }
      if (changed)
         to.setRevision(nextDocRev);
      return changed;
   }

   /**
    * Merges from the DTO Resource into HDocument, adding and obsoleting textflows, including metadata and the specified extensions
    * @param from
    * @param to
    * @param enabledExtensions
    * @return
    */
   public boolean transferFromResource(Resource from, HDocument to, Set<String> enabledExtensions, HLocale locale, int nextDocRev)
   {
      boolean changed = false;
      changed |= transferFromResourceMetadata(from, to, enabledExtensions, locale, nextDocRev);
      changed |= transferFromTextFlows(from.getTextFlows(), to, enabledExtensions, nextDocRev);
      return changed;
   }

   /**
    * Transfers metadata and the specified extensions from DTO AbstractResourceMeta into HDocument
    * @param from
    * @param to
    * @param enabledExtensions
    * @return
    */
   public boolean transferFromResourceMetadata(AbstractResourceMeta from, HDocument to, Set<String> enabledExtensions, HLocale locale, int nextDocRev)
   {
      boolean changed = false;

      // name
      if (!equals(from.getName(), to.getDocId()))
      {
         to.setFullPath(from.getName());
         changed = true;
      }

      // locale
      if (!equals(from.getLang(), to.getLocale().getLocaleId()))
      {
         log.debug("locale:" + from.getLang());
         to.setLocale(locale);
         changed = true;
      }

      // contentType
      if (!equals(from.getContentType(), to.getContentType()))
      {
         to.setContentType(from.getContentType());
         changed = true;
      }
      // handle extensions
      changed |= transferFromResourceExtensions(from.getExtensions(true), to, enabledExtensions);
      if (changed)
         to.setRevision(nextDocRev);
      return changed;
   }

   /**
    * Transfers from DTO TextFlowTarget into HTextFlowTarget
    * @param from
    * @param to
    * @return
    * @todo merge with {@link #transferFromTextFlowTargetExtensions}
    */
   public boolean transferFromTextFlowTarget(TextFlowTarget from, HTextFlowTarget to)
   {
      boolean changed = false;
      if (!equals(from.getContent(), to.getContent()))
      {
         to.setContent(from.getContent());
         changed = true;
      }
      if (!equals(from.getState(), to.getState()))
      {
         to.setState(from.getState());
         changed = true;
      }
      if (changed)
      {
         to.setVersionNum(to.getVersionNum() + 1);
      }
      return changed;
   }

   /**
    * Transfers the specified extensions from DTO AbstractResourceMeta into HDocument
    * @param from
    * @param to
    * @param enabledExtensions
    * @return
    * @see #transferFromResource
    */
   private boolean transferFromResourceExtensions(ExtensionSet<AbstractResourceMetaExtension> from, HDocument to, Set<String> enabledExtensions)
   {
      boolean changed = false;

      if (enabledExtensions.contains(PoHeader.ID))
      {
         PoHeader poHeaderExt = from.findByType(PoHeader.class);
         if (poHeaderExt != null)
         {
            HPoHeader poHeader = to.getPoHeader();
            if (poHeader == null)
            {
               log.debug("create a new HPoHeader");
               poHeader = new HPoHeader();
            }
            changed |= transferFromPoHeader(poHeaderExt, poHeader);

            if (to.getPoHeader() == null && changed)
            {
               to.setPoHeader(poHeader);
            }

         }
      }

      return changed;
   }

   /**
    * Transfers enabled extensions from TranslationsResource into HDocument for a single locale 
    * @param from
    * @param to
    * @param enabledExtensions
    * @param locale
    * @param mergeType 
    * @return
    * @see #transferToTranslationsResourceExtensions
    */
   public boolean transferFromTranslationsResourceExtensions(ExtensionSet<TranslationsResourceExtension> from, HDocument to, Set<String> enabledExtensions, HLocale locale, MergeType mergeType)
   {
      boolean changed = false;
      if (enabledExtensions.contains(PoTargetHeader.ID))
      {
         PoTargetHeader fromTargetHeader = from.findByType(PoTargetHeader.class);
         if (fromTargetHeader != null)
         {
            log.debug("found PO header for locale: {0}", locale);
            HPoTargetHeader toTargetHeader = to.getPoTargetHeaders().get(locale);
            if (toTargetHeader == null)
            {
               changed = true;
               toTargetHeader = new HPoTargetHeader();
               toTargetHeader.setTargetLanguage(locale);
               toTargetHeader.setDocument(to);
               transferFromPoTargetHeader(fromTargetHeader, toTargetHeader, MergeType.IMPORT); // return value not needed
               to.getPoTargetHeaders().put(locale, toTargetHeader);
            }
            else
            {
               changed |= transferFromPoTargetHeader(fromTargetHeader, toTargetHeader, mergeType);
            }
         }
         else
         {
            changed |= to.getPoTargetHeaders().remove(locale) != null;
         }
      }
      return changed;
   }

   /**
    * Transfers enabled extensions from DTO TextFlowTarget to HTextFlowTarget
    * @param extensions
    * @param hTarget
    * @param enabledExtensions
    * @return
    * @todo merge with {@link #transferFromTextFlowTarget}
    */
   public boolean transferFromTextFlowTargetExtensions(ExtensionSet<TextFlowTargetExtension> extensions, HTextFlowTarget hTarget, Set<String> enabledExtensions)
   {
      boolean changed = false;
      if (enabledExtensions.contains(SimpleComment.ID))
      {
         SimpleComment comment = extensions.findByType(SimpleComment.class);
         if (comment != null)
         {
            changed |= transferFromComment(comment, hTarget);
         }
      }

      return changed;

   }
   
   /**
    * Transfers from DTO SimpleComment to a Hibernate object's "comment" property
    * @param from
    * @param to
    * @return
    */
   private boolean transferFromComment(SimpleComment from, HasSimpleComment to)
   {
      HSimpleComment hComment = to.getComment();

      if (hComment == null)
      {
         hComment = new HSimpleComment();
      }
      if (!equals(from.getValue(), hComment.getComment()))
      {
         hComment.setComment(from.getValue());
         to.setComment(hComment);
         return true;
      }
      return false;
   }

   private boolean transferFromTextFlowExtensions(ExtensionSet<TextFlowExtension> from, HTextFlow to, Set<String> enabledExtensions)
   {
      boolean changed = false;
      if (enabledExtensions.contains(PotEntryHeader.ID))
      {
         PotEntryHeader entryHeader = from.findByType(PotEntryHeader.class);
         if (entryHeader != null)
         {
            HPotEntryData hEntryHeader = to.getPotEntryData();

            if (hEntryHeader == null)
            {
               changed = true;
               hEntryHeader = new HPotEntryData();
               to.setPotEntryData(hEntryHeader);
               log.debug("set potentryheader");
            }
            changed |= transferFromPotEntryHeader(entryHeader, hEntryHeader);
         }
      }
      if (enabledExtensions.contains(SimpleComment.ID))
      {
         SimpleComment comment = from.findByType(SimpleComment.class);
         if (comment != null)
         {
            HSimpleComment hComment = to.getComment();

            if (hComment == null)
            {
               hComment = new HSimpleComment();
            }
            if (!equals(comment.getValue(), hComment.getComment()))
            {
               changed = true;
               hComment.setComment(comment.getValue());
               to.setComment(hComment);
               log.debug("set comment:{0}", comment.getValue());
            }
         }
      }

      return changed;

   }   

   /**
    * @see #transferToPotEntryHeader(HPotEntryData, PotEntryHeader)
    * @param from
    * @param to
    * @return
    */
   private boolean transferFromPotEntryHeader(PotEntryHeader from, HPotEntryData to)
   {
      boolean changed = false;

      if (!equals(from.getContext(), to.getContext()))
      {
         changed = true;
         to.setContext(from.getContext());
      }

      String flags = StringUtil.concat(from.getFlags(), ',');
      if (!equals(flags, to.getFlags()))
      {
         changed = true;
         to.setFlags(flags);
      }

      String refs = StringUtil.concat(from.getReferences(), ',');
      if (!equals(refs, to.getReferences()))
      {
         changed = true;
         to.setReferences(refs);
      }

      return changed;
   }

   /**
    * 
    * @param from
    * @param to
    * @param mergeType 
    * @return
    * @see #transferFromTranslationsResourceExtensions
    * @see #transferToPoTargetHeader
    */
   private boolean transferFromPoTargetHeader(PoTargetHeader from, HPoTargetHeader to, MergeType mergeType)
   {
      boolean changed = pushPoTargetComment(from, to, mergeType);

      // TODO we should probably block PoHeader/POT-specific entries
      // ie POT-Creation-Date, Project-Id-Version, Report-Msgid-Bugs-To
      String entries = PoUtility.listToHeader(from.getEntries());
      if (!equals(entries, to.getEntries()))
      {
         to.setEntries(entries);
         changed = true;
      }

      return changed;
   }

   /**
    * 
    * @param fromHeader
    * @param toHeader
    * @param mergeType 
    * @return
    * @see #pullPoTargetComment
    */
   protected boolean pushPoTargetComment(PoTargetHeader fromHeader, HPoTargetHeader toHeader, MergeType mergeType)
   {
      boolean changed = false;
      HSimpleComment hComment = toHeader.getComment();
      if (hComment == null)
      {
         hComment = new HSimpleComment();
      }
      String fromComment = fromHeader.getComment();
      String toComment = hComment.getComment();
      if (!equals(fromComment, toComment))
      {
         // skip #zanata lines
         List<String> fromLines = splitLines(fromComment, ZANATA_TAG);
         StringBuilder sb = new StringBuilder(fromComment.length());
         switch (mergeType)
         {
         case IMPORT:
            for (String line : fromLines)
            {
               if (sb.length() != 0)
                  sb.append(NEWLINE);
               sb.append(line);
               changed = true;
            }
            break;

         default: // AUTO or anything else will merge comments
            // to merge, we just append new lines, skip old lines
            List<String> toLines = Collections.emptyList();
            if (toComment != null)
            {
               sb.append(toComment);
               toLines = splitLines(toComment, null);
            }

            for (String line : fromLines)
            {
               if (!toLines.contains(line))
               {
                  if (sb.length() != 0)
                     sb.append(NEWLINE);
                  sb.append(line);
                  changed = true;
               }
            }
            break;
         }
         if (changed)
         {
            hComment.setComment(sb.toString());
            toHeader.setComment(hComment);
         }
      }
      return changed;
   }
   
   /**
    * splits s into lines, skipping any which contain tagToSkip
    * @param s
    * @param tagToSkip
    * @return
    */
   static List<String> splitLines(String s, String tagToSkip)
   {
      if (s.isEmpty())
         return Collections.emptyList();
      try
      {
         List<String> lineList = new ArrayList<String>(s.length() / 40);
         BufferedReader reader = new BufferedReader(new StringReader(s));
         String line;
         while ((line = reader.readLine()) != null)
         {
            if (tagToSkip == null || !line.contains(tagToSkip))
            {
               lineList.add(line);
            }
         }
         return lineList;
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   private boolean transferFromPoHeader(PoHeader from, HPoHeader to)
   {
      boolean changed = false;

      HSimpleComment comment = to.getComment();
      if (comment == null)
      {
         comment = new HSimpleComment();
      }
      if (!equals(from.getComment(), comment.getComment()))
      {
         changed = true;
         comment.setComment(from.getComment());
         to.setComment(comment);
      }

      String entries = PoUtility.listToHeader(from.getEntries());
      if (!equals(entries, to.getEntries()))
      {
         to.setEntries(entries);
         changed = true;
      }

      return changed;

   }

   public static <T> boolean equals(T a, T b)
   {
      if (a == null && b == null)
      {
         return true;
      }
      if (a == null || b == null)
      {
         return false;
      }

      return a.equals(b);
   }

   private boolean transferFromTextFlow(TextFlow from, HTextFlow to, Set<String> enabledExtensions)
   {
      boolean changed = false;
      if (!equals(from.getContent(), to.getContent()))
      {
         to.setContent(from.getContent());
         changed = true;
      }

      // TODO from.getLang()

      transferFromTextFlowExtensions(from.getExtensions(true), to, enabledExtensions);

      return changed;
   }

   public void transferToResource(HDocument from, Resource to)
   {
      transferToAbstractResourceMeta(from, to);
   }

   private void transferToPoHeader(HPoHeader from, PoHeader to)
   {
      if (from.getComment() != null)
      {
         to.setComment(from.getComment().getComment());
      }
      to.getEntries().addAll(PoUtility.headerToList(from.getEntries()));
   }

   /**
    * 
    * @param from
    * @param to
    * @see #transferToTranslationsResourceExtensions
    * @see #transferFromPoTargetHeader
    */
   private void transferToPoTargetHeader(HPoTargetHeader from, PoTargetHeader to, List<HTextFlowTarget> hTargets)
   {
      pullPoTargetComment(from, to, hTargets);
      to.getEntries().addAll(PoUtility.headerToList(from.getEntries()));

   }

   /**
    * 
    * @param fromHeader
    * @param toHeader
    * @see #pushPoTargetComment
    */
   protected void pullPoTargetComment(HPoTargetHeader fromHeader, PoTargetHeader toHeader, List<HTextFlowTarget> hTargets)
   {
      StringBuilder sb = new StringBuilder();
      HSimpleComment comment = fromHeader.getComment();
      if (comment != null)
      {
         sb.append(comment.getComment());
      }
      // generate #zanata credit comments
      // TODO order by year, then alphabetically
      Set<String> zanataCredits = new LinkedHashSet<String>();
      for(HTextFlowTarget tft : hTargets)
      {
         HPerson person = tft.getLastModifiedBy();
         Calendar lastChanged = Calendar.getInstance();
         lastChanged.setTime(tft.getLastChanged());
         int year = lastChanged.get(Calendar.YEAR);
         String credit = person.getName() + " " + "<" + person.getEmail() + ">, " + year + ". " + ZANATA_TAG;
         zanataCredits.add(credit);
      }
      for(String credit : zanataCredits)
      {
         if (sb.length() != 0)
            sb.append(NEWLINE);
         sb.append(credit);
      }
      
      toHeader.setComment(sb.toString());
   }

   public void transferToTextFlow(HTextFlow from, TextFlow to)
   {
      to.setContent(from.getContent());
      to.setRevision(from.getRevision());
      // TODO HTextFlow should have a lang
      // to.setLang(from.get)
   }

   public void transferToAbstractResourceMeta(HDocument from, AbstractResourceMeta to)
   {
      to.setContentType(from.getContentType());
      to.setLang(from.getLocale().getLocaleId());
      to.setName(from.getDocId());
      // TODO ADD support within the hibernate model for multiple resource types
      to.setType(ResourceType.FILE);
      to.setRevision(from.getRevision());
   }

   public void transferToResourceExtensions(HDocument from, ExtensionSet<AbstractResourceMetaExtension> to, Set<String> enabledExtensions)
   {
      if (enabledExtensions.contains(PoHeader.ID))
      {
         PoHeader poHeaderExt = new PoHeader();
         if (from.getPoHeader() != null)
         {
            transferToPoHeader(from.getPoHeader(), poHeaderExt);
            to.add(poHeaderExt);
         }
      }
   }

   /**
    * 
    * @param from
    * @param to
    * @param enabledExtensions
    * @param locale
    * @see #transferFromTranslationsResourceExtensions
    */
   public void transferToTranslationsResourceExtensions(HDocument from, ExtensionSet<TranslationsResourceExtension> to, Set<String> enabledExtensions, HLocale locale, List<HTextFlowTarget> hTargets)
   {
      if (enabledExtensions.contains(PoTargetHeader.ID))
      {
         log.debug("PoTargetHeader requested");
         PoTargetHeader poTargetHeader = new PoTargetHeader();
         HPoTargetHeader fromHeader = from.getPoTargetHeaders().get(locale);
         if (fromHeader != null)
         {
            log.debug("PoTargetHeader found");
            transferToPoTargetHeader(fromHeader, poTargetHeader, hTargets);
            to.add(poTargetHeader);
         }
      }
   }

   public void transferToTextFlowExtensions(HTextFlow from, ExtensionSet<TextFlowExtension> to, Set<String> enabledExtensions)
   {
      if (enabledExtensions.contains(PotEntryHeader.ID) && from.getPotEntryData() != null)
      {
         PotEntryHeader header = new PotEntryHeader();
         transferToPotEntryHeader(from.getPotEntryData(), header);
         log.debug("set header:{0}", from.getPotEntryData());
         to.add(header);

      }

      if (enabledExtensions.contains(SimpleComment.ID) && from.getComment() != null)
      {
         SimpleComment comment = new SimpleComment(from.getComment().getComment());
         log.debug("set comment:{0}", from.getComment().getComment());
         to.add(comment);
      }

   }

   /**
    * @see #transferFromPotEntryHeader(PotEntryHeader, HPotEntryData)
    * @param from
    * @param to
    */
   private void transferToPotEntryHeader(HPotEntryData from, PotEntryHeader to)
   {
      to.setContext(from.getContext());
      List<String> flags = StringUtil.split(from.getFlags(), ",");
      to.getFlags().addAll(flags);
      List<String> refs = StringUtil.split(from.getReferences(), ",");
      to.getReferences().addAll(refs);
   }

   /**
    * 
    * @param from
    * @param to
    * @param enabledExtensions
    * @todo merge with {@link #transferToTextFlowTarget}
    */
   public void transferToTextFlowTargetExtensions(HTextFlowTarget from, ExtensionSet<TextFlowTargetExtension> to, Set<String> enabledExtensions)
   {
      if (enabledExtensions.contains(SimpleComment.ID) && from.getComment() != null)
      {
         SimpleComment comment = new SimpleComment(from.getComment().getComment());
         to.add(comment);
      }
   }

   public String encodeDocId(String id)
   {
      String other = StringUtils.replace(id, "/", ",");
      try
      {
         other = URLEncoder.encode(other, "UTF-8");
         return StringUtils.replace(other, "%2C", ",");
      }
      catch (UnsupportedEncodingException e)
      {
         throw new RuntimeException(e);
      }
   }

   public String decodeDocId(String id)
   {
      try
      {
         String other = URLDecoder.decode(id, "UTF-8");
         return StringUtils.replace(other, ",", "/");
      }
      catch (UnsupportedEncodingException e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * 
    * @param from
    * @param to
    * @todo merge with {@link #transferToTextFlowTargetExtensions}
    */
   public void transferToTextFlowTarget(HTextFlowTarget from, TextFlowTarget to)
   {
      to.setContent(from.getContent());
      to.setState(from.getState());
      to.setTextFlowRevision(from.getTextFlowRevision());
      HPerson translator = from.getLastModifiedBy();
      if (translator != null)
      {
         to.setTranslator(new Person(translator.getEmail(), translator.getName()));
      }
   }

}
