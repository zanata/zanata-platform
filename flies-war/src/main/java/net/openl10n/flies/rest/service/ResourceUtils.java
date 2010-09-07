package net.openl10n.flies.rest.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.openl10n.flies.common.LocaleId;
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
import net.openl10n.flies.model.po.PoUtility;
import net.openl10n.flies.rest.StringSet;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.extensions.PoHeader;
import net.openl10n.flies.rest.dto.extensions.PoTargetHeader;
import net.openl10n.flies.rest.dto.extensions.PotEntryHeader;
import net.openl10n.flies.rest.dto.extensions.SimpleComment;
import net.openl10n.flies.rest.dto.resource.AbstractResourceMeta;
import net.openl10n.flies.rest.dto.resource.ExtensionSet;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.TextFlow;
import net.openl10n.flies.rest.dto.resource.TextFlowTarget;
import net.openl10n.flies.rest.dto.resource.TranslationsResource;
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

   Log log = Logging.getLog(ResourceUtils.class);

   
   /**
    * Merges the list of TextFlows into the target HDocument, adding and obsoleting TextFlows as necessary.
    * @param from
    * @param to
    * @return
    */
   boolean transferFromTextFlows(List<TextFlow> from, HDocument to, StringSet enabledExtensions)
   {
      boolean changed = false;
      to.getTextFlows().clear();
      Set<String> ids = new HashSet<String>(to.getAllTextFlows().keySet());
      for (TextFlow tf : from)
      {
         HTextFlow textFlow;
         if (ids.contains(tf.getId()))
         {
            ids.remove(tf.getId());
            textFlow = to.getAllTextFlows().get(tf.getId());
            if (transferFromTextFlow(tf, textFlow, enabledExtensions) || textFlow.isObsolete())
            {
               textFlow.setRevision(to.getRevision());
               changed = true;
               log.debug("TextFlow with id {0} has changed", tf.getId());
            }
            textFlow.setObsolete(false);
         }
         else
         {
            textFlow = new HTextFlow();
            textFlow.setResId(tf.getId());
            textFlow.setRevision(to.getRevision());
            transferFromTextFlow(tf, textFlow, enabledExtensions);
            changed = true;
            log.debug("TextFlow with id {0} is new", tf.getId());
         }
         to.getTextFlows().add(textFlow);
         to.getAllTextFlows().put(textFlow.getResId(), textFlow);
      }

      // set remaining textflows to obsolete.
      for (String id : ids)
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

      return changed;
   }

   /**
    * Merges from the DTO Resource into HDocument, adding and obsoleting textflows, including metadata and the specified extensions
    * @param from
    * @param to
    * @param enabledExtensions
    * @return
    */
   public boolean transferFromResource(Resource from, HDocument to, StringSet enabledExtensions, HLocale locale)
   {
      boolean changed = false;
      changed |= transferFromResourceMetadata(from, to, enabledExtensions, locale);
      changed |= transferFromTextFlows(from.getTextFlows(), to, enabledExtensions);
      return changed;
   }

   /**
    * Transfers metadata and the specified extensions from DTO AbstractResourceMeta into HDocument
    * @param from
    * @param to
    * @param enabledExtensions
    * @return
    */
   public boolean transferFromResourceMetadata(AbstractResourceMeta from, HDocument to, StringSet enabledExtensions, HLocale locale)
   {
      boolean changed = false;

      // name
      if (!equals(from.getName(), to.getName()))
      {
         to.setName(from.getName());
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
      return changed;
   }

   /**
    * Transfers from DTO TextFlowTarget into HTextFlowTarget
    * @param from
    * @param to
    * @return
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

      return changed;
   }

   /**
    * Transfers the specified extensions from DTO AbstractResourceMeta into HDocument
    * @param from
    * @param to
    * @param enabledExtensions
    * @return
    */
   private boolean transferFromResourceExtensions(ExtensionSet<AbstractResourceMeta> from, HDocument to, StringSet enabledExtensions)
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
               poHeader = new HPoHeader();
            }
            changed |= transferFromPoHeader(poHeaderExt, poHeader);

            if (to.getPoHeader() == null && changed)
            {
               poHeader.setDocument(to);
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
    * @return
    */
   public boolean transferFromTranslationsResourceExtensions(ExtensionSet<TranslationsResource> from, HDocument to, StringSet enabledExtensions, HLocale locale)
   {
      boolean changed = false;
      if (enabledExtensions.contains(PoTargetHeader.ID))
      {
         PoTargetHeader fromTargetHeader = from.findByType(PoTargetHeader.class);
         if (fromTargetHeader != null)
         {
            log.debug("locale:" + locale.getLocaleId().getId());
            HPoTargetHeader toTargetHeader = to.getPoTargetHeaders().get(locale);
            if (toTargetHeader == null)
            {
               changed = true;
               toTargetHeader = new HPoTargetHeader();
               transferFromPoTargetHeader(fromTargetHeader, toTargetHeader);
               to.getPoTargetHeaders().put(locale, toTargetHeader);
            }
            else
            {
               changed |= transferFromPoTargetHeader(fromTargetHeader, toTargetHeader);
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
    */
   public boolean transferFromTextFlowTargetExtensions(ExtensionSet<TextFlowTarget> extensions, HTextFlowTarget hTarget, StringSet enabledExtensions)
   {
      boolean changed = false;

      if (enabledExtensions.contains(SimpleComment.ID))
      {
         SimpleComment<TextFlowTarget> comment = extensions.findByType(SimpleComment.class);
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
   private boolean transferFromComment(SimpleComment<?> from, HasSimpleComment to)
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

   private boolean transferFromTextFlowExtensions(ExtensionSet<TextFlow> from, HTextFlow to, StringSet enabledExtensions)
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
            }
            changed |= transferFromPotEntryHeader(entryHeader, hEntryHeader);
         }
      }
      if (enabledExtensions.contains(SimpleComment.ID))
      {
         SimpleComment<TextFlow> comment = from.findByType(SimpleComment.class);
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

   private boolean transferFromPoTargetHeader(PoTargetHeader from, HPoTargetHeader to)
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

   private boolean transferFromTextFlow(TextFlow from, HTextFlow to, StringSet enabledExtensions)
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

      to.setName(from.getName());
      to.setLang(from.getLocale().getLocaleId());
      to.setContentType(from.getContentType());
   }

   private void transferToPoHeader(HPoHeader from, PoHeader to)
   {
      if (from.getComment() != null)
      {
         to.setComment(from.getComment().getComment());
      }
      to.getEntries().addAll(PoUtility.headerToList(from.getEntries()));
   }

   private void transferToPoTargetHeader(HPoTargetHeader from, PoTargetHeader to)
   {
      HSimpleComment comment = from.getComment();
      if (comment != null)
      {
         to.setComment(comment.getComment());
      }
      to.getEntries().addAll(PoUtility.headerToList(from.getEntries()));

   }

   public void transferToTextFlow(HTextFlow from, TextFlow to)
   {
      to.setContent(from.getContent());
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
   }

   public void transferToResourceExtensions(HDocument from, ExtensionSet<AbstractResourceMeta> to, StringSet enabledExtensions)
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

   public void transferToTranslationsResourceExtensions(HDocument from, ExtensionSet<TranslationsResource> to, StringSet enabledExtensions, LocaleId locale)
   {
      if (enabledExtensions.contains(PoTargetHeader.ID))
      {
         PoTargetHeader poTargetHeader = new PoTargetHeader();
         HPoTargetHeader fromHeader = from.getPoTargetHeaders().get(locale);
         if (fromHeader != null)
         {
            transferToPoTargetHeader(fromHeader, poTargetHeader);
            to.add(poTargetHeader);
         }
      }
   }

   public void transferToTextFlowExtensions(HTextFlow from, ExtensionSet<TextFlow> to, StringSet enabledExtensions)
   {
      if (enabledExtensions.contains(PotEntryHeader.ID) && from.getPotEntryData() != null)
      {
         PotEntryHeader header = new PotEntryHeader();
         transferToPotEntryHeader(from.getPotEntryData(), header);
         to.add(header);

      }

      if (enabledExtensions.contains(SimpleComment.ID) && from.getComment() != null)
      {
         SimpleComment<TextFlow> comment = new SimpleComment<TextFlow>(from.getComment().getComment());
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
      // TODO decide how to handle extracted comments:
      // option 1) via the comment extension: remove extractedComment
      // from HPotEntryData.
      // option 2) allow them in addition to the comment extension: handle them
      // in transferToPotEntryHeader/transferFromPotEntryHeader.
   }

   public void transferToTextFlowTargetExtensions(HTextFlowTarget from, ExtensionSet<TextFlowTarget> to, StringSet enabledExtensions)
   {
      if (enabledExtensions.contains(SimpleComment.ID) && from.getComment() != null)
      {
         SimpleComment<TextFlowTarget> comment = new SimpleComment<TextFlowTarget>(from.getComment().getComment());
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

   public void transferToTextFlowTarget(HTextFlowTarget from, TextFlowTarget to)
   {
      to.setContent(from.getContent());
      to.setState(from.getState());
      HPerson translator = from.getLastModifiedBy();
      if (translator != null)
      {
         to.setTranslator(new Person(translator.getEmail(), translator.getName()));
      }
   }

}
