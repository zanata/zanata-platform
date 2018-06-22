/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.adapter;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartGroup;
import net.sf.okapi.common.resource.TextContainer;
import net.sf.okapi.common.resource.TextUnit;
import net.sf.okapi.filters.ts.Parameters;
import net.sf.okapi.filters.ts.TsFilter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.DocumentType;
import org.zanata.common.HasContents;
import org.zanata.common.LocaleId;
import org.zanata.exception.FileFormatAdapterException;
import org.zanata.model.HDocument;
import org.zanata.rest.dto.extensions.comment.SimpleComment;
import org.zanata.rest.dto.extensions.gettext.PotEntryHeader;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Adapter to handle Qt translation (.ts) files.<br/>
 * using the Okapi {@link net.sf.okapi.filters.ts.TsFilter} class
 *
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class TSAdapter extends OkapiFilterAdapter {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(TSAdapter.class);

    public TSAdapter() {
        super(prepareFilter(), IdSource.contentHash, true);
    }

    private static TsFilter prepareFilter() {
        return new TsFilter();
    }

    // ExtraComment takes precedence
    private final String COMMENT_REGEX = "<extracomment>(.+)</extracomment>|<comment>(.+)</comment>";

    @Override
    protected void updateParamsWithDefaults(IParameters params) {
        Parameters p = (Parameters) params;
        p.setDecodeByteValues(false);
        p.setUseCodeFinder(false);
    }

    @Override
    public Resource parseDocumentFile(ParserOptions options)
            throws  FileFormatAdapterException,
                    IllegalArgumentException {
        IFilter filter = getFilter();
        Resource document = new Resource();
        document.setLang(options.getLocale());
        document.setContentType(ContentType.TextPlain);
        updateParamsWithDefaults(filter.getParameters());
        List<TextFlow> resources = document.getTextFlows();
        Map<String, HasContents> addedResources =
                new HashMap<String, HasContents>();
        RawDocument rawDoc = new RawDocument(options.getRawFile(), "UTF-8",
                net.sf.okapi.common.LocaleId.fromString("en"));
        if (rawDoc.getTargetLocale() == null) {
            rawDoc.setTargetLocale(net.sf.okapi.common.LocaleId.EMPTY);
        }
        try {
            filter.open(rawDoc);
            String subDocName = "";
            // TS can contain similar source strings in different contexts
            String context = "";
            while (filter.hasNext()) {
                Event event = filter.next();
                if (isStartContext(event)) {
                    context = getContext(event);
                } else if (isEndContext(event)) {
                    context = "";
                } else if (event.getEventType() == EventType.TEXT_UNIT) {
                    TextUnit tu = (TextUnit) event.getResource();
                    if (!tu.getSource().isEmpty() && tu.isTranslatable()) {
                        String content = getTranslatableText(tu);
                        if (!content.isEmpty()) {
                            TextFlow tf = processTextFlow(tu, context, content,
                                    subDocName, options.getLocale());
                            if (!addedResources.containsKey(tf.getId())) {
                                tf = addExtensions(tf, tu, context);
                                addedResources.put(tf.getId(), tf);
                                resources.add(tf);
                            }
                        }
                    }
                }
            }
        } catch (OkapiIOException e) {
            throw new FileFormatAdapterException("Unable to parse document", e);
        } finally {
            filter.close();
        }
        return document;
    }

    private TextFlow addExtensions(TextFlow textFlow, TextUnit textUnit,
            String context) {
        if (StringUtils.isNotBlank(context)) {
            PotEntryHeader potEntryHeader = new PotEntryHeader();
            potEntryHeader.setContext(context);
            textFlow.getExtensions(true).add(potEntryHeader);
        }
        Pattern commentPattern = Pattern.compile(COMMENT_REGEX);
        Matcher matcher =
                commentPattern.matcher(textUnit.getSkeleton().toString());
        if (matcher.find()) {
            if (StringUtils.isNotBlank(matcher.group(1))) {
                textFlow.getExtensions(true)
                        .add(new SimpleComment(matcher.group(1)));
            } else if (StringUtils.isNotBlank(matcher.group(2))) {
                textFlow.getExtensions(true)
                        .add(new SimpleComment(matcher.group(2)));
            }
        }
        return textFlow;
    }

    @Override
    public String generateTranslationFilename(@Nonnull HDocument document,
            @Nonnull String locale) {
        String srcExt = FilenameUtils.getExtension(document.getName());
        DocumentType documentType = document.getRawDocument().getType();
        String transExt = documentType.getExtensions().get(srcExt);
        if (StringUtils.isEmpty(transExt)) {
            log.warn("Adding missing TS extension to generated filename");
            return document.getName() + "_" + locale + ".ts";
        }
        return FilenameUtils.removeExtension(document.getName()) + "_" + locale
                + "." + transExt;
    }

    @Override
    protected void generateTranslatedFile(URI originalFile,
            Map<String, TextFlowTarget> translations,
            net.sf.okapi.common.LocaleId localeId, IFilterWriter writer,
            String params, boolean approvedOnly) {
        RawDocument rawDoc = new RawDocument(originalFile, "UTF-8",
                net.sf.okapi.common.LocaleId.fromString("en"));
        if (rawDoc.getTargetLocale() == null) {
            rawDoc.setTargetLocale(localeId);
        }
        List<String> encounteredIds = new ArrayList<>();
        IFilter filter = getFilter();
        updateParamsWithDefaults(filter.getParameters());
        try {
            filter.open(rawDoc);
            String subDocName = "";
            // TS can contain similar source strings in different contexts
            String context = "";
            while (filter.hasNext()) {
                Event event = filter.next();
                if (event.isDocumentPart() &&
                        event.getDocumentPart().hasProperty("language")) {
                    // TODO ZNTA-2483 change readonly language property
                } else if (isStartContext(event)) {
                    context = getContext(event);
                } else if (isEndContext(event)) {
                    context = "";
                } else if (event.getEventType() == EventType.TEXT_UNIT) {
                    TextUnit tu = (TextUnit) event.getResource();
                    if (!tu.getSource().isEmpty() && tu.isTranslatable()) {
                        String translatable = getTranslatableText(tu);
                        // Ignore if the source is empty
                        if (!translatable.isEmpty()) {
                            String id = getIdFor(tu,
                                    context.concat(translatable), subDocName);
                            TextFlowTarget tft = translations.get(id);
                            if (tft != null) {
                                if (!encounteredIds.contains(id)) {
                                    // Dismiss duplicate numerusforms
                                    encounteredIds.add(id);
                                    for (String translated : tft
                                            .getContents()) {
                                        boolean finished = usable(tft.getState(), approvedOnly);
                                        String propVal = finished ? "yes" : "no";
                                        // Okapi will map approved=no to type=unfinished in the .TS file
                                        tu.getTargetProperty(localeId, "approved").setValue(propVal);
                                        // TODO: Find a method of doing this in
                                        // one object, not a loop
                                        tu.setTargetContent(localeId,
                                                GenericContent
                                                        .fromLetterCodedToFragment(
                                                                translated,
                                                                tu.getSource()
                                                                        .getFirstContent()
                                                                        .clone(),
                                                                true, true));
                                    }
                                }
                            }
                        }
                    }
                }
                writer.handleEvent(event);
            }
        } catch (OkapiIOException e) {
            throw new FileFormatAdapterException(
                    "Unable to generate translated document from original", e);
        } finally {
            filter.close();
            writer.close();
        }
    }

    private boolean usable(ContentState state, boolean approvedOnly) {
        return state.isApproved() ||
                (!approvedOnly && state.isTranslated());
    }

    @Override
    protected String getTranslatableText(TextUnit tu) {
        return tu.getSource().getFirstContent().getText();
    }

    private String getTranslatedText(TextContainer tc) {
        return tc.getFirstContent().getText();
    }

    /**
     * Create a TextFlow from Qt ts TextUnit with context and plurals. ID is
     * derived from a concatenation of context and content.
     *
     * @param tu
     *            original textunit
     * @param context
     *            ts source context
     * @param content
     *            text unit content
     * @param subDocName
     *            subdocument name
     * @param sourceLocale
     *            source locale
     * @return
     */
    protected TextFlow processTextFlow(TextUnit tu, String context,
            String content, String subDocName, LocaleId sourceLocale) {
        TextFlow tf =
                new TextFlow(getIdFor(tu, context.concat(content), subDocName),
                        sourceLocale);
        if (tu.hasProperty("numerus") && tu.getProperty("numerus").getValue()
                .equalsIgnoreCase("yes")) {
            tf.setPlural(true);
            // Qt TS uses a single message for singular and plural form
            tf.setContents(content, content);
        } else {
            tf.setPlural(false);
            tf.setContents(content);
        }
        return tf;
    }

    @Override
    protected TranslationsResource parseTranslationFile(RawDocument rawDoc,
            String params) {
        TranslationsResource transRes = new TranslationsResource();
        List<TextFlowTarget> translations = transRes.getTextFlowTargets();
        Map<String, HasContents> addedResources =
                new HashMap<String, HasContents>();
        IFilter filter = getFilter();
        updateParamsWithDefaults(filter.getParameters());
        try {
            filter.open(rawDoc);
            String subDocName = "";
            // TS can contain similar source strings in different contexts
            String context = "";
            while (filter.hasNext()) {
                Event event = filter.next();
                if (isStartContext(event)) {
                    context = getContext(event);
                } else if (isEndContext(event)) {
                    context = "";
                } else if (event.getEventType() == EventType.TEXT_UNIT) {
                    TextUnit tu = (TextUnit) event.getResource();
                    if (!tu.getSource().isEmpty() && tu.isTranslatable()) {
                        String content = getTranslatableText(tu);
                        TextContainer translation =
                                tu.getTarget(rawDoc.getTargetLocale());
                        if (!content.isEmpty()) {
                            TextFlowTarget tft = new TextFlowTarget(getIdFor(tu,
                                    context.concat(content), subDocName));
                            // TODO: Change this
                            tft.setState(ContentState.Translated);
                            String resId = tft.getResId();
                            if (addedResources.containsKey(resId)) {
                                List<String> currentStrings =
                                        new ArrayList<>(addedResources
                                                .get(resId).getContents());
                                currentStrings
                                        .add(getTranslatedText(translation));
                                tft.setContents(currentStrings);
                            } else {
                                tft.setContents(getTranslatedText(translation));
                            }
                            addedResources.put(tft.getResId(), tft);
                            translations.add(tft);
                        }
                    }
                }
            }
        } catch (OkapiIOException e) {
            throw new FileFormatAdapterException(
                    "Unable to parse translation file", e);
        } finally {
            filter.close();
        }
        return transRes;
    }

    private boolean isStartContext(Event event) {
        return event.isStartGroup() && event.getStartGroup().toString()
                .toLowerCase().contains("<context");
    }

    private boolean isEndContext(Event event) {
        return event.isEndGroup() && event.getEndGroup().toString()
                .toLowerCase().contains("</context");
    }

    private String getContext(Event event) {
        // TODO: Numerusform bug workaround, remove when fixed
        StartGroup startGroup = event.getStartGroup();
        {
            Pattern pattern =
                    Pattern.compile("<name>(.+)</name>", Pattern.MULTILINE);
            Matcher matcher = pattern.matcher(startGroup.toString());
            if (startGroup.getName() == null && matcher.find()) {
                log.info("Qt ts context bug encountered, returning {} from {}",
                        matcher.group(1), startGroup.toString());
                return matcher.group(1);
            }
        }
        return startGroup.getName() == null ? ""
                : event.getStartGroup().getName();
    }
}
