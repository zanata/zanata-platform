package net.openl10n.flies.rest.service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collection;
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
import net.openl10n.flies.model.po.HPoHeader;
import net.openl10n.flies.model.po.HPoTargetHeader;
import net.openl10n.flies.model.po.HPotEntryData;
import net.openl10n.flies.model.po.PoUtility;
import net.openl10n.flies.rest.StringSet;
import net.openl10n.flies.rest.dto.Person;
import net.openl10n.flies.rest.dto.extensions.PoHeader;
import net.openl10n.flies.rest.dto.extensions.PoTargetHeader;
import net.openl10n.flies.rest.dto.extensions.PoTargetHeaderEntry;
import net.openl10n.flies.rest.dto.extensions.PoTargetHeaders;
import net.openl10n.flies.rest.dto.extensions.PotEntryHeader;
import net.openl10n.flies.rest.dto.extensions.SimpleComment;
import net.openl10n.flies.rest.dto.resource.AbstractResourceMeta;
import net.openl10n.flies.rest.dto.resource.ExtensionSet;
import net.openl10n.flies.rest.dto.resource.Resource;
import net.openl10n.flies.rest.dto.resource.TextFlow;
import net.openl10n.flies.rest.dto.resource.TextFlowTarget;

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

   public boolean mergeTextFlows(List<TextFlow> from, HDocument to)
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
            if (transfer(tf, textFlow) || textFlow.isObsolete())
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
            transfer(tf, textFlow);
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

   public boolean transfer(Resource from, HDocument to, HLocale locale)
   {
      boolean changed = false;
      changed |= transfer((AbstractResourceMeta) from, to, locale);
      changed |= mergeTextFlows(from.getTextFlows(), to);
      return changed;
   }

   public boolean transfer(AbstractResourceMeta from, HDocument to, HLocale locale)
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

      return changed;
   }

   public boolean transfer(TextFlowTarget from, HTextFlowTarget to)
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

   public boolean transfer(ExtensionSet from, HDocument to, StringSet extensions)
   {
      boolean changed = false;

      if (extensions.contains(PoHeader.ID))
      {
         PoHeader poHeaderExt = from.findByType(PoHeader.class);
         if (poHeaderExt != null)
         {
            HPoHeader poHeader = to.getPoHeader();
            if (poHeader == null)
            {
               poHeader = new HPoHeader();
            }
            changed |= transfer(poHeaderExt, poHeader);

            if (to.getPoHeader() == null && changed)
            {
               poHeader.setDocument(to);
               to.setPoHeader(poHeader);
            }

         }
      }

      return changed;
   }

   public boolean transfer(ExtensionSet from, HDocument to, StringSet extensions, HLocale locale)
   {
      boolean changed = false;
      if (extensions.contains(PoTargetHeader.ID))
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
               transfer(fromTargetHeader, toTargetHeader);
               to.getPoTargetHeaders().put(locale, toTargetHeader);
            }
            else
            {
               changed |= transfer(fromTargetHeader, toTargetHeader);
            }
         }
         else
         {
            changed |= to.getPoTargetHeaders().remove(locale) != null;
         }
      }

      return changed;
   }

   public boolean transfer(ExtensionSet extensions, HTextFlowTarget hTarget, StringSet extensions2)
   {
      boolean changed = false;

      if (extensions.contains(SimpleComment.ID))
      {
         SimpleComment comment = extensions.findByType(SimpleComment.class);
         if (comment != null)
         {
            HSimpleComment hComment = hTarget.getComment();

            if (hComment == null)
            {
               hComment = new HSimpleComment();
            }
            if (!equals(comment.getValue(), hComment.getComment()))
            {
               changed = true;
               hComment.setComment(comment.getValue());
               hTarget.setComment(hComment);
            }
         }
      }

      return changed;

   }


   private boolean transfer(PoTargetHeader from, HPoTargetHeader to)
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

   private boolean transfer(PoHeader from, HPoHeader to)
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

   public boolean transfer(TextFlow from, HTextFlow to)
   {
      boolean changed = false;
      if (!equals(from.getContent(), to.getContent()))
      {
         to.setContent(from.getContent());
         changed = true;
      }

      // TODO from.getLang()

      return changed;
   }

   public void transfer(HDocument from, Resource to)
   {

      to.setName(from.getName());
      to.setLang(from.getLocale().getLocaleId());
      to.setContentType(from.getContentType());
   }

   public void transfer(HPoHeader from, PoHeader to)
   {
      if (from.getComment() != null)
      {
         to.setComment(from.getComment().getComment());
      }
      to.getEntries().addAll(PoUtility.headerToList(from.getEntries()));
   }

   private void transfer(HPoTargetHeader from, PoTargetHeader to)
   {
      HSimpleComment comment = from.getComment();
      if (comment != null)
      {
         to.setComment(comment.getComment());
      }
      to.getEntries().addAll(PoUtility.headerToList(from.getEntries()));

   }

   private void transfer(HPoTargetHeader from, PoTargetHeaderEntry to)
   {
      to.setLocale(from.getTargetLanguage().getLocaleId());
      HSimpleComment comment = from.getComment();
      if (comment != null)
      {
         to.setComment(comment.getComment());
      }
      to.getEntries().addAll(PoUtility.headerToList(from.getEntries()));

   }

   public void transfer(HTextFlow from, TextFlow to)
   {
      to.setContent(from.getContent());
      // TODO HTextFlow should have a lang
      // to.setLang(from.get)
   }

   public void transfer(HDocument from, AbstractResourceMeta to)
   {
      to.setContentType(from.getContentType());
      to.setLang(from.getLocale().getLocaleId());
      to.setName(from.getDocId());
      // TODO ADD support within the hibernate model for multiple resource types
      to.setType(ResourceType.FILE);
   }

   public void transfer(HDocument from, ExtensionSet to, StringSet extensions)
   {
      if (extensions.contains(PoHeader.ID))
      {
         PoHeader poHeaderExt = new PoHeader();
         if (from.getPoHeader() != null)
         {
            transfer(from.getPoHeader(), poHeaderExt);
            to.add(poHeaderExt);
         }
      }
   }

   public void transfer(HDocument from, ExtensionSet to, StringSet extensions, LocaleId locale)
   {
      if (extensions.contains(PoTargetHeader.ID))
      {
         PoTargetHeader poTargetHeader = new PoTargetHeader();
         HPoTargetHeader fromHeader = from.getPoTargetHeaders().get(locale);
         if (fromHeader != null)
         {
            transfer(fromHeader, poTargetHeader);
            to.add(poTargetHeader);
         }
      }
   }

   public void transfer(HDocument from, ExtensionSet to, StringSet extensions, Collection<LocaleId> locales)
   {
      if (extensions.contains(PoTargetHeaders.ID))
      {
         PoTargetHeaders poTargetHeaders = new PoTargetHeaders();
         for (LocaleId locale : locales)
         {
            HPoTargetHeader fromHeader = from.getPoTargetHeaders().get(locale);
            if (fromHeader != null)
            {
               PoTargetHeaderEntry header = new PoTargetHeaderEntry();
               transfer(fromHeader, header);
               poTargetHeaders.getHeaders().add(header);
            }
         }
         to.add(poTargetHeaders);
      }
   }

   public void transfer(HTextFlow from, ExtensionSet to, StringSet extensions)
   {
      if (extensions.contains(PotEntryHeader.ID) && from.getPotEntryData() != null)
      {
         PotEntryHeader header = new PotEntryHeader();
         transfer(from.getPotEntryData(), header);
         to.add(header);

      }

      if (extensions.contains(SimpleComment.ID) && from.getComment() != null)
      {
         SimpleComment comment = new SimpleComment();
         comment.setValue(from.getComment().getComment());
         to.add(comment);
      }

   }

   private void transfer(HPotEntryData from, PotEntryHeader to)
   {
      to.setContext(from.getContext());
      HSimpleComment comment = from.getExtractedComment();
      if (comment != null)
      {
         to.setExtractedComment(comment.getComment());
      }
   }

   public void transfer(HTextFlowTarget from, ExtensionSet to, StringSet extensions)
   {
      if (extensions.contains(SimpleComment.ID) && from.getComment() != null)
      {
         SimpleComment comment = new SimpleComment();
         comment.setValue(from.getComment().getComment());
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

   public void transfer(HTextFlowTarget from, TextFlowTarget to)
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
