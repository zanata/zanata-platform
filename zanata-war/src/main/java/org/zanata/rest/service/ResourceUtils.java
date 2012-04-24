package org.zanata.rest.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.tennera.jgettext.HeaderFields;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.common.MergeType;
import org.zanata.common.ResourceType;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.HasSimpleComment;
import org.zanata.model.po.HPoHeader;
import org.zanata.model.po.HPoTargetHeader;
import org.zanata.model.po.HPotEntryData;
import org.zanata.rest.dto.Person;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.AbstractResourceMetaExtension;
import org.zanata.rest.dto.extensions.gettext.HeaderEntry;
import org.zanata.rest.dto.extensions.gettext.PoHeader;
import org.zanata.rest.dto.extensions.gettext.PoTargetHeader;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.extensions.gettext.TextFlowExtension;
import org.zanata.rest.dto.extensions.gettext.TextFlowTargetExtension;
import org.zanata.rest.dto.extensions.gettext.TranslationsResourceExtension;
import org.zanata.rest.dto.resource.AbstractResourceMeta;
import org.zanata.rest.dto.resource.ExtensionSet;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.StringUtil;

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
   private static final String ZANATA_GENERATOR_PREFIX = "Zanata";
   private static final String ZANATA_TAG = "#zanata";
   private static final String PO_DATE_FORMAT = "yyyy-MM-dd hh:mmZ";
   private static final String PO_DEFAULT_CONTENT_TYPE = "text/plain; charset=UTF-8";

   /**
    * PO Header entries
    */
   private static final String LAST_TRANSLATOR_HDR = "Last-Translator";
   private static final String PO_REVISION_DATE_HDR = HeaderFields.KEY_PoRevisionDate;
   private static final String LANGUAGE_TEAM_HDR = HeaderFields.KEY_LanguageTeam;
   private static final String X_GENERATOR_HDR = "X-Generator";
   private static final String LANGUAGE_HDR = HeaderFields.KEY_Language;
   private static final String CONTENT_TYPE_HDR = HeaderFields.KEY_ContentType;
   private static final String PLURAL_FORMS_HDR = "Plural-Forms";

   private final static Pattern NPLURALS_TAG_PATTERN = Pattern.compile("nplurals=");
   private final static Pattern NPLURALS_PATTERN = Pattern.compile("nplurals=[0-9]+");

   private static final Log log = Logging.getLog(ResourceUtils.class);

   //   private static int MAX_TARGET_CONTENTS = 6;

   private Properties pluralForms = new Properties();

   @PostConstruct
   public void create()
   {
      try
      {
         pluralForms.load(this.getClass().getClassLoader().getResourceAsStream("pluralforms.properties"));
      }
      catch (IOException e)
      {
         log.error("There was an error loading plural forms.", e);
      }
   }

   /**
    * Merges the list of TextFlows into the target HDocument, adding and
    * obsoleting TextFlows as necessary.
    * 
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
            {// content has changed
               textFlow.setRevision(nextDocRev);
               changed = true;
               for (HTextFlowTarget targ : textFlow.getTargets().values())
               {
                  // if (targ.getState() != ContentState.New)
                  if (targ.getState() == ContentState.Approved)
                  {
                     targ.setState(ContentState.NeedReview);
                     targ.setVersionNum(targ.getVersionNum() + 1);
                  }
               }
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
    * Merges from the DTO Resource into HDocument, adding and obsoleting
    * textflows, including metadata and the specified extensions
    * 
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
    * Transfers metadata and the specified extensions from DTO
    * AbstractResourceMeta into HDocument
    * 
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
    * 
    * @param from
    * @param to
    * @return
    * @todo merge with {@link #transferFromTextFlowTargetExtensions}
    */
   public boolean transferFromTextFlowTarget(TextFlowTarget from, HTextFlowTarget to)
   {
      boolean changed = false;
      if (!equals(from.getContents(), to.getContents()))
      {
         to.setContents(from.getContents());
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
    * Transfers the specified extensions from DTO AbstractResourceMeta into
    * HDocument
    * 
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
    * Transfers enabled extensions from TranslationsResource into HDocument for
    * a single locale
    * 
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
               transferFromPoTargetHeader(fromTargetHeader, toTargetHeader, MergeType.IMPORT); // return
                                                                                               // value
                                                                                               // not
                                                                                               // needed
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
    * 
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
    * Transfers from DTO SimpleComment to a Hibernate object's "comment"
    * property
    * 
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

      List<String> flagList = from.getFlags();
      String flags = StringUtil.concat(from.getFlags(), ',');
      if (flagList.isEmpty())
      {
         flags = null;
      }
      if (!equals(flags, to.getFlags()))
      {
         changed = true;
         to.setFlags(flags);
      }

      List<String> refList = from.getReferences();
      String refs = StringUtil.concat(from.getReferences(), ',');
      if (refList.isEmpty())
      {
         refs = null;
      }
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
    * 
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
      if (!equals(from.getContents(), to.getContents()))
      {
         to.setContents(from.getContents());
         changed = true;
      }
      if (!equals(from.isPlural(), to.isPlural()))
      {
         to.setPlural(from.isPlural());
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
    * @param hTargets
    * @param locale
    * @see #transferToTranslationsResourceExtensions
    * @see #transferFromPoTargetHeader
    */
   private void transferToPoTargetHeader(HPoTargetHeader from, PoTargetHeader to, List<HTextFlowTarget> hTargets, HLocale locale)
   {
      pullPoTargetComment(from, to, hTargets);
      to.getEntries().addAll(this.headerToList(from.getEntries()));
      populateHeaderEntries(to.getEntries(), hTargets, locale);
   }

   /**
    * Transforms a set of header entries from a String to a list of POJOs.
    * 
    * @param entries The header entries' string.
    */
   private List<HeaderEntry> headerToList(final String entries)
   {
      return PoUtility.headerToList(entries);
   }

   /**
    * Populates a list of header entries with values stored in the system. For
    * certain headers, the original value will remain if present.
    * 
    * @param headerEntries The header entries to be populated.
    * @param hTargets The Text Flow Targets that the header applies to.
    * @param locale The locale that is bein
    */
   private void populateHeaderEntries(final List<HeaderEntry> headerEntries, final List<HTextFlowTarget> hTargets, final HLocale locale)
   {
      final Map<String, HeaderEntry> containedHeaders = new LinkedHashMap<String, HeaderEntry>(headerEntries.size());
      HTextFlowTarget lastTranslatedTarget = this.getLastTranslatedTarget(hTargets);

      // Collect the existing header entries
      for (HeaderEntry entry : headerEntries)
      {
         containedHeaders.put(entry.getKey(), entry);
      }

      // Add / Replace headers
      Date revisionDate = this.getRevisionDate(headerEntries, lastTranslatedTarget);
      HeaderEntry headerEntry = containedHeaders.get(PO_REVISION_DATE_HDR);
      if (headerEntry == null)
      {
         headerEntry = new HeaderEntry(PO_REVISION_DATE_HDR, this.toPoHeaderString(revisionDate));
         headerEntries.add(headerEntry);
      }
      else
      {
         headerEntry.setValue(this.toPoHeaderString(revisionDate));
      }

      headerEntry = containedHeaders.get(LAST_TRANSLATOR_HDR);
      if (headerEntry == null)
      {
         headerEntry = new HeaderEntry(LAST_TRANSLATOR_HDR, this.getLastTranslator(lastTranslatedTarget, headerEntries));
         headerEntries.add(headerEntry);
      }
      else
      {
         headerEntry.setValue(this.getLastTranslator(lastTranslatedTarget, headerEntries));
      }

      headerEntry = containedHeaders.get(LANGUAGE_TEAM_HDR);
      if (headerEntry == null)
      {
         headerEntry = new HeaderEntry(LANGUAGE_TEAM_HDR, this.getLanguageTeam(locale));
         headerEntries.add(headerEntry);
      }
      else
      {
         // Keep the original value if provided
      }

      headerEntry = containedHeaders.get(LANGUAGE_HDR);
      if (headerEntry == null)
      {
         headerEntry = new HeaderEntry(LANGUAGE_HDR, this.getLanguage(locale));
         headerEntries.add(headerEntry);
      }
      else
      {
         headerEntry.setValue(this.getLanguage(locale));
      }

      headerEntry = containedHeaders.get(X_GENERATOR_HDR);
      if (headerEntry == null)
      {
         headerEntry = new HeaderEntry(X_GENERATOR_HDR, this.getSystemVersion());
         headerEntries.add(headerEntry);
      }
      else
      {
         headerEntry.setValue(this.getSystemVersion());
      }

      headerEntry = containedHeaders.get(CONTENT_TYPE_HDR);
      if (headerEntry == null)
      {
         headerEntry = new HeaderEntry(CONTENT_TYPE_HDR, PO_DEFAULT_CONTENT_TYPE);
         headerEntries.add(headerEntry);
      }
      else
      {
         headerEntry.setValue(PO_DEFAULT_CONTENT_TYPE);
      }

      headerEntry = containedHeaders.get(PLURAL_FORMS_HDR);
      if (headerEntry == null)
      {
         headerEntry = new HeaderEntry(PLURAL_FORMS_HDR, this.getPluralForms(locale));
         headerEntries.add(headerEntry);
      }
      else
      {
         // Keep the original if provided
      }
   }

   /**
    * Finds and returns the Revision Date stored in a PO file's header entries.
    * 
    * @param headerEntries A single PO file's header entries.
    * @return The Revision Date header value, or null if no such header is found
    *         or the date cannot be parsed.
    */
   private Date getHeaderRevisionDate(final List<HeaderEntry> headerEntries)
   {
      Date poFileRevisionDate = null;

      for (HeaderEntry entry : headerEntries)
      {
         if (entry.getKey().equalsIgnoreCase(PO_REVISION_DATE_HDR))
         {
            SimpleDateFormat dateFormat = new SimpleDateFormat(PO_DATE_FORMAT);
            try
            {
               poFileRevisionDate = dateFormat.parse(entry.getValue());
            }
            catch (ParseException e)
            {
               // found the header but date could not be parsed
            }

            break;
         }
      }

      return poFileRevisionDate;
   }

   private String getHeaderLastTranslator(final List<HeaderEntry> headerEntries)
   {
      for (HeaderEntry entry : headerEntries)
      {
         if (entry.getKey().equalsIgnoreCase(LAST_TRANSLATOR_HDR))
         {
            return entry.getValue();
         }
      }

      return "";
   }

   /**
    * Returns a PO file's Revision Date based on the values stored in the file's
    * header and in the last translated target. If the system cannot determine a
    * suitable Revision date, a null value is returned.
    */
   private Date getRevisionDate(final List<HeaderEntry> headerEntries, final HTextFlowTarget lastTranslated)
   {
      Date poFileRevisionDate = this.getHeaderRevisionDate(headerEntries);
      Date translationsRevisionDate = null;

      if (lastTranslated != null)
      {
         translationsRevisionDate = lastTranslated.getLastChanged();
      }

      if (translationsRevisionDate != null)
      {
         if (poFileRevisionDate != null)
         {
            return translationsRevisionDate.after(poFileRevisionDate) ? translationsRevisionDate : poFileRevisionDate;
         }
         else
         {
            return translationsRevisionDate;
         }
      }
      else
      {
         return poFileRevisionDate == null ? null : poFileRevisionDate;
      }
   }

   /**
    * @param translations A list of Translations for a document.
    * @return The most recently translated target. If there are more than one,
    *         this method will return one of those, no assurances o
    */
   private HTextFlowTarget getLastTranslatedTarget(final List<HTextFlowTarget> translations)
   {
      Date lastUpdate = new Date(Long.MIN_VALUE);
      HTextFlowTarget lastTranslated = null;

      for (HTextFlowTarget trans : translations)
      {
         if (trans.getLastModifiedBy() != null && trans.getLastChanged().after(lastUpdate))
         {
            lastTranslated = trans;
            lastUpdate = trans.getLastChanged();
         }
      }

      return lastTranslated;
   }

   /**
    * Gets the last translator header value for a set of header entries and the
    * last translated target.
    * 
    * @param lastTranslated The most currently translated target.
    * @param headerEntries The PO header entries.
    * @return A string with the value of the last translator.
    */
   private String getLastTranslator(final HTextFlowTarget lastTranslated, final List<HeaderEntry> headerEntries)
   {
      Date headerRevisionDate = this.getHeaderRevisionDate(headerEntries);
      String lastTranslator = this.getHeaderLastTranslator(headerEntries);

      if (lastTranslated != null)
      {
         HPerson lastModifiedBy = lastTranslated.getLastModifiedBy();
         Date lastModifiedDate = lastTranslated.getLastChanged();

         // Last translated target is more recent than the Revision Date on the
         // Header
         if (lastModifiedBy != null && lastModifiedDate != null && lastModifiedDate.after(headerRevisionDate))
         {
            lastTranslator = lastModifiedBy.getName() + " <" + lastModifiedBy.getEmail() + ">";
         }
         else if (lastModifiedBy != null && lastModifiedDate == null)
         {
            lastTranslator = lastModifiedBy.getName() + " <" + lastModifiedBy.getEmail() + ">";
         }
      }

      return lastTranslator;
   }

   /**
    * Returns a string representation of a Date for use in a PO file header.
    * 
    * @param aDate Date object to include in the Header
    * @return A string with the value of the date suitable for a PO file header.
    */
   private String toPoHeaderString(Date aDate)
   {
      if (aDate != null)
      {
         SimpleDateFormat dateFormat = new SimpleDateFormat(PO_DATE_FORMAT);
         return dateFormat.format(aDate);
      }
      else
      {
         return "";
      }
   }

   /**
    * Returns the Language Team PO file header for a given locale.
    */
   private String getLanguageTeam(final HLocale hLocale)
   {
      return hLocale.retrieveDisplayName();
   }

   /**
    * Retrieves the language PO file header for a given locale.
    * 
    * @param translations
    */
   private String getLanguage(final HLocale locale)
   {
      return locale.getLocaleId().toString();
   }

   /**
    * Returns the application version.
    */
   private String getSystemVersion()
   {
      try
      {
         return ZANATA_GENERATOR_PREFIX + " " + ((ApplicationConfiguration) Component.getInstance(ApplicationConfiguration.class, ScopeType.APPLICATION)).getVersion();
      }
      catch (Exception e)
      {
         return ZANATA_GENERATOR_PREFIX + " UNKNOWN";
      }
   }

   /**
    * Returns the appropriate plural form for a given Locale.
    */
   private String getPluralForms(HLocale locale)
   {
      LocaleId localeId = locale.getLocaleId();
      String javaLocale = localeId.toJavaName().toLowerCase();

      if (pluralForms.containsKey(javaLocale))
      {
         return pluralForms.getProperty(javaLocale);
      }

      // Try out every combination. e.g: for xxx_yyy_zzz, try xxx_yyyy_zzz, then
      // xxx_yyy, then xxx
      while (javaLocale.indexOf('_') > 0)
      {
         javaLocale = javaLocale.substring(0, javaLocale.lastIndexOf('_'));

         if (pluralForms.containsKey(javaLocale))
         {
            return pluralForms.getProperty(javaLocale);
         }
      }

      // Not found, return null
      return null;
   }

   public int getNumPlurals(HDocument document, HLocale hLocale)
   {
      HPoTargetHeader headers = document.getPoTargetHeaders().get(hLocale);
      String headerEntries = headers != null ? headers.getEntries() : "";
      return getNPluralForms(headerEntries, hLocale);
   }

   private int getNPluralForms(String entries, HLocale targetLocale)
   {
      int nPlurals = 1;

      try
      {
         Properties headerList = new Properties();
         String pluralForms;
         if (entries != null && !entries.isEmpty())
         {
            headerList.load(new StringReader(entries));
            if (headerList.containsKey(PLURAL_FORMS_HDR))
            {
               pluralForms = headerList.getProperty(PLURAL_FORMS_HDR);
            }
            else
            {
               pluralForms = getPluralForms(targetLocale);
            }
         }
         else
         {
            pluralForms = getPluralForms(targetLocale);
         }

         Matcher nPluralsMatcher = NPLURALS_PATTERN.matcher(pluralForms);
         String nPluralsString = "";
         while (nPluralsMatcher.find())
         {
            nPluralsString = nPluralsMatcher.group();
            Matcher nPluralsValueMatcher = NPLURALS_TAG_PATTERN.matcher(nPluralsString);
            nPluralsString = nPluralsValueMatcher.replaceAll("");
            break;
         }
         if (nPluralsString != null && !nPluralsString.isEmpty())
         {
            nPlurals = Integer.parseInt(nPluralsString);
         }
      }
      catch (Exception e)
      {
         log.error("Error getting nPlurals:" + entries);
      }

      //      nPlurals = (nPlurals > MAX_TARGET_CONTENTS || nPlurals < 1) ? 1 : nPlurals;
      return nPlurals;
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
      // order by year, then alphabetically
      Set<TranslatorCredit> zanataCredits = new TreeSet<TranslatorCredit>();
      for (HTextFlowTarget tft : hTargets)
      {
         HPerson person = tft.getLastModifiedBy();
         if (person != null)
         {
            Calendar lastChanged = Calendar.getInstance();
            lastChanged.setTime(tft.getLastChanged());
            int year = lastChanged.get(Calendar.YEAR);
            TranslatorCredit credit = new TranslatorCredit();
            credit.setEmail(person.getEmail());
            credit.setName(person.getName());
            credit.setYear(year);
            zanataCredits.add(credit);
         }
      }
      for (TranslatorCredit credit : zanataCredits)
      {
         if (sb.length() != 0)
            sb.append(NEWLINE);
         sb.append(credit);
         sb.append(' ');
         sb.append(ZANATA_TAG);
      }

      toHeader.setComment(sb.toString());
   }

   public void transferToTextFlow(HTextFlow from, TextFlow to)
   {
      to.setContents(from.getContents());
      to.setRevision(from.getRevision());
      to.setPlural(from.isPlural());

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
    * @return true only if extensions were found
    */
   public boolean transferToTranslationsResourceExtensions(HDocument from, ExtensionSet<TranslationsResourceExtension> to, Set<String> enabledExtensions, HLocale locale, List<HTextFlowTarget> hTargets)
   {
      boolean found = false;
      if (enabledExtensions.contains(PoTargetHeader.ID))
      {
         log.debug("PoTargetHeader requested");
         PoTargetHeader poTargetHeader = new PoTargetHeader();
         HPoTargetHeader fromHeader = from.getPoTargetHeaders().get(locale);
         if (fromHeader != null)
         {
            found = true;
            log.debug("PoTargetHeader found");
         }
         else
         {
            // If no header is found, use a default empty header for generation
            // purposes
            fromHeader = new HPoTargetHeader();
            fromHeader.setEntries("");
         }
         transferToPoTargetHeader(fromHeader, poTargetHeader, hTargets, locale);
         to.add(poTargetHeader);
      }
      return found;
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

      List<String> flags = new ArrayList<String>(0);
      if (from.getFlags() != null)
      {
         flags = StringUtil.split(from.getFlags(), ",");
      }
      to.getFlags().addAll(flags);

      List<String> refs = new ArrayList<String>(0);
      if (from.getReferences() != null)
      {
         refs = StringUtil.split(from.getReferences(), ",");
      }
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
      to.setContents(from.getContents());
      to.setState(from.getState());
      to.setRevision(from.getVersionNum());
      to.setTextFlowRevision(from.getTextFlowRevision());
      HPerson translator = from.getLastModifiedBy();
      if (translator != null)
      {
         to.setTranslator(new Person(translator.getEmail(), translator.getName()));
      }
   }

   public Resource buildResource(HDocument document)
   {
      Set<String> extensions = new HashSet<String>();
      extensions.add("gettext");
      extensions.add("comment");

      Resource entity = new Resource(document.getDocId());
      this.transferToResource(document, entity);
      // handle extensions
      this.transferToResourceExtensions(document, entity.getExtensions(true), extensions);

      for (HTextFlow htf : document.getTextFlows())
      {
         TextFlow tf = new TextFlow(htf.getResId(), document.getLocale().getLocaleId());
         this.transferToTextFlowExtensions(htf, tf.getExtensions(true), extensions);
         this.transferToTextFlow(htf, tf);
         entity.getTextFlows().add(tf);
      }

      return entity;
   }

   /**
    * 
    * @param transRes
    * @param document
    * @param locale
    * @param enabledExtensions
    * @param hTargets
    * @return true only if some data was found (non-New translations, or some
    *         metadata extensions)
    */
   public boolean transferToTranslationsResource(TranslationsResource transRes, HDocument document, HLocale locale, Set<String> enabledExtensions, List<HTextFlowTarget> hTargets)
   {
      boolean found = this.transferToTranslationsResourceExtensions(document, transRes.getExtensions(true), enabledExtensions, locale, hTargets);

      for (HTextFlowTarget hTarget : hTargets)
      {
         if (hTarget.getState() != ContentState.New)
         {
            found = true;
         }
         TextFlowTarget target = new TextFlowTarget();
         target.setResId(hTarget.getTextFlow().getResId());
         this.transferToTextFlowTarget(hTarget, target);
         this.transferToTextFlowTargetExtensions(hTarget, target.getExtensions(true), enabledExtensions);
         transRes.getTextFlowTargets().add(target);
      }
      return found;
   }

}
