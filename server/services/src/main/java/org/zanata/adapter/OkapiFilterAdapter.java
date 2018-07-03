/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.adapter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.okapi.common.Event;
import net.sf.okapi.common.EventType;
import net.sf.okapi.common.IParameters;
import net.sf.okapi.common.exceptions.OkapiIOException;
import net.sf.okapi.common.filters.IFilter;
import net.sf.okapi.common.filterwriter.GenericContent;
import net.sf.okapi.common.filterwriter.IFilterWriter;
import net.sf.okapi.common.resource.RawDocument;
import net.sf.okapi.common.resource.StartSubDocument;
import net.sf.okapi.common.resource.TextUnit;

import org.jetbrains.annotations.NotNull;
import org.zanata.adapter.TranslatableSeparator.SplitString;
import org.zanata.common.ContentState;
import org.zanata.common.ContentType;
import org.zanata.common.HasContents;
import org.zanata.common.LocaleId;
import org.zanata.exception.FileFormatAdapterException;
import org.zanata.model.HDocument;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TextFlow;
import org.zanata.rest.dto.resource.TextFlowTarget;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.util.FileUtil;
import org.zanata.util.HashUtil;
import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

import static net.sf.okapi.common.LocaleId.fromString;
import static org.zanata.util.OkapiUtil.toOkapiLocale;

/**
 * An adapter that uses a provided {@link IFilter} implementation to parse
 * documents.
 *
 * @author David Mason, <a
 *         href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class OkapiFilterAdapter implements FileFormatAdapter {
    /**
     * Determines how TextFlow ids are assigned for Okapi TextUnits
     */
    public enum IdSource {
        /**
         * Use ID of TextUnit as is (only unique if no sub-documents).
         *
         * Note: if the underlying filter gives inconsistent ids (e.g.
         * positional identifiers when paragraphs are inserted),
         * Zanata's merge algorithm is too weak to handle this well.
         */
        textUnitId,
        /**
         * use 'name' attribute of TextUnit, if any. Not guaranteed to be
         * unique.
         */
        textUnitName,
        /**
         * use a hash of string content (similar to resId for gettext projects).
         * Not guaranteed to be unique.
         */
        contentHash,
        /**
         * Concatenate name of sub-document and ID of TextUnit. Should be unique
         * (assuming sub-document names are).
         *
         * Note: if the underlying filter gives inconsistent ids (e.g.
         * positional identifiers when paragraphs are inserted),
         * Zanata's merge algorithm is too weak to handle this well.
         */
        subDocNameAndTextUnitId
    };

    private final IFilter filter;
    private final IdSource idSource;
    private boolean requireFileOutput;
    private boolean separateNonTranslatable;

    /**
     * Create an adapter that will use the specified {@link IdSource} as
     * TextFlow id.
     *
     * @param filter
     *            {@link IFilter} used to parse the document
     * @param idSource
     *            determines how ids are assigned to TextFlows. The chosen
     *            source should only produce duplicate ids when source content
     *            is identical.
     */
    public OkapiFilterAdapter(IFilter filter, IdSource idSource) {
        this(filter, idSource, false);
    }

    /**
     * Create an adapter that will use the specified {@link IdSource} as
     * TextFlow id.
     *
     * @param filter
     *            {@link IFilter} used to parse the document
     * @param idSource
     *            determines how ids are assigned to TextFlows The chosen source
     *            should only produce duplicate ids when source content is
     *            identical.
     * @param requireFileOutput
     *            true if filter requires a file on disk rather than just a
     *            stream. Causes a temp file to be created when parsing.
     */
    public OkapiFilterAdapter(IFilter filter, IdSource idSource,
            boolean requireFileOutput) {
        this(filter, idSource, requireFileOutput, false);
    }

    public OkapiFilterAdapter(IFilter filter, IdSource idSource,
            boolean requireFileOutput, boolean separateNonTranslatable) {
        this.filter = filter;
        this.idSource = idSource;
        this.requireFileOutput = requireFileOutput;
        this.separateNonTranslatable = separateNonTranslatable;
    }

    protected IFilter getFilter() {
        return filter;
    }

    @Override
    public Resource parseDocumentFile(ParserOptions options)
            throws FileFormatAdapterException, IllegalArgumentException {
        Resource document = new Resource();
        document.setLang(options.getLocale());
        document.setContentType(ContentType.TextPlain);

        List<TextFlow> resources = document.getTextFlows();
        Map<String, HasContents> addedResources =
                new HashMap<String, HasContents>();

        RawDocument rawDoc =
                new RawDocument(options.getRawFile(), "UTF-8",
                        fromString("en"));
        if (rawDoc.getTargetLocale() == null) {
            rawDoc.setTargetLocale(net.sf.okapi.common.LocaleId.EMPTY);
        }
        updateParams(options.getParams());
        try {
            filter.open(rawDoc);
            String subDocName = "";
            while (filter.hasNext()) {
                Event event = filter.next();
                if (event.getEventType() == EventType.START_SUBDOCUMENT) {
                    StartSubDocument startSubDoc =
                            (StartSubDocument) event.getResource();
                    subDocName = stripPath(startSubDoc.getName());
                } else if (event.getEventType() == EventType.TEXT_UNIT) {
                    TextUnit tu = (TextUnit) event.getResource();
                    if (!tu.getSource().isEmpty() && tu.isTranslatable()) {
                        String content = getTranslatableText(tu);
                        if (!content.isEmpty()) {
                            TextFlow tf = processTextFlow(tu, content,
                                    subDocName, options.getLocale());
                            if (shouldAdd(tf.getId(), tf, addedResources)) {
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

    protected TextFlow processTextFlow(TextUnit tu, String content, String subDocName, LocaleId sourceLocale) {
        TextFlow tf = new TextFlow(getIdFor(tu, content,
                subDocName), sourceLocale);
        tf.setPlural(false);
        tf.setContents(content);
        return tf;
    }

    protected String getTranslatableText(TextUnit tu) {
        String letterCodedText =
                GenericContent.fromFragmentToLetterCoded(tu.getSource()
                        .getFirstContent(), true);
        if (separateNonTranslatable) {
            return getPartitionedText(letterCodedText).getStr();
        } else {
            return letterCodedText;
        }
    }

    /**
     * Separates translatable text from surrounding non-translatable text.
     *
     * @param tu
     */
    private SplitString getPartitionedText(TextUnit tu) {
        return TranslatableSeparator.separate(GenericContent
                .fromFragmentToLetterCoded(tu.getSource().getFirstContent(),
                        true));
    }

    /**
     * Separates translatable text from surrounding non-translatable text.
     *
     * @param letterCodedText
     */
    private SplitString getPartitionedText(String letterCodedText) {
        return TranslatableSeparator.separate(letterCodedText);
    }

    /**
     * Check whether a TextFlow or TextFlowTarget should be added given the
     * current rules and state.
     *
     * @param id
     *            of the source string
     * @param hc
     *            the TextFlow or TextFlowTarget to add
     * @param addedResources
     *            record of the strings that have been added so far.
     * @return true if a string with the same id does not exist in
     *         addedResources
     * @throws FileFormatAdapterException
     *             if a duplicate is found when elideDuplicates is false, or if
     *             duplicates do not have identical contents.
     */
    private boolean shouldAdd(String id, HasContents hc,
            Map<String, HasContents> addedResources)
            throws FileFormatAdapterException {
        if (addedResources.containsKey(id)) {
            if (!hc.getContents().equals(addedResources.get(id).getContents())) {
                throw new FileFormatAdapterException(
                        "Same id but different contents for text flow, "
                                + "not suitable for eliding.");
            }
            return false;
        }
        return true;
    }

    private String stripPath(String name) {
        if (name.contains("/") && !name.endsWith("/")) {
            return name.substring(name.lastIndexOf('/') + 1);
        } else {
            return name;
        }
    }

    @NotNull
    @Override
    public TranslationsResource parseTranslationFile(ParserOptions options) {
        RawDocument rawDoc =
                new RawDocument(options.getRawFile(), "UTF-8",
                        net.sf.okapi.common.LocaleId.fromString("en"));
        if (rawDoc.getTargetLocale() == null) {
            try {
                rawDoc.setTargetLocale(toOkapiLocale(options.getLocale()));
            } catch (IllegalArgumentException e) {
                throw new FileFormatAdapterException(
                    "Unable to parse translation file", e);
            }
        }
        return parseTranslationFile(rawDoc, options.getParams());
    }

    protected TranslationsResource parseTranslationFile(RawDocument rawDoc,
            String params) {
        TranslationsResource transRes = new TranslationsResource();
        List<TextFlowTarget> translations = transRes.getTextFlowTargets();

        Map<String, HasContents> addedResources =
                new HashMap<String, HasContents>();
        updateParams(params);
        try {
            filter.open(rawDoc);
            String subDocName = "";
            while (filter.hasNext()) {
                Event event = filter.next();
                if (event.getEventType() == EventType.START_SUBDOCUMENT) {
                    StartSubDocument startSubDoc =
                            (StartSubDocument) event.getResource();
                    subDocName = stripPath(startSubDoc.getName());
                } else if (event.getEventType() == EventType.TEXT_UNIT) {
                    TextUnit tu = (TextUnit) event.getResource();
                    if (!tu.getSource().isEmpty() && tu.isTranslatable()) {
                        String content = getTranslatableText(tu);
                        if (!content.isEmpty()) {
                            TextFlowTarget tft =
                                    new TextFlowTarget(getIdFor(tu, content, subDocName));
                            tft.setContents(content);
                            tft.setState(ContentState.NeedReview);
                            if (shouldAdd(tft.getResId(), tft, addedResources)) {
                                addedResources.put(tft.getResId(), tft);
                                translations.add(tft);
                            }

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

    @Override
    public void writeTranslatedFile(@NotNull OutputStream output,
            @NotNull WriterOptions options, boolean approvedOnly)
            throws FileFormatAdapterException, IllegalArgumentException {
        Map<String, TextFlowTarget> translations =
                transformToMapByResId(
                    options.getTranslatedDoc().getTranslation().getTextFlowTargets());

        try {
            net.sf.okapi.common.LocaleId localeId = toOkapiLocale(options.getTranslatedDoc().getLocale());

            IFilterWriter writer = filter.createFilterWriter();
            writer.setOptions(localeId, getOutputEncoding());

            ParserOptions sourceOptions = options.getSourceParserOptions();

            if (requireFileOutput) {
                writeTranslatedFileWithFileOutput(output, sourceOptions.getRawFile(),
                    translations, localeId, writer, sourceOptions.getParams(), approvedOnly);
            } else {
                writer.setOutput(output);
                generateTranslatedFile(sourceOptions.getRawFile(), translations, localeId,
                    writer, sourceOptions.getParams(), approvedOnly);
            }
        } catch (IllegalArgumentException e) {
            throw new FileFormatAdapterException(
                "Unable to generate translated file", e);
        }
    }

    /**
     * Transform list of TextFlowTarget to map with TextFlowTarget.resId as key
     *
     * @param targets
     * @return
     */
    private Map<String, TextFlowTarget> transformToMapByResId(
        List<TextFlowTarget> targets) {
        Map<String, TextFlowTarget> resIdTargetMap = Maps.newHashMap();

        for (TextFlowTarget target: targets) {
            resIdTargetMap.put(target.getResId(), target);
        }
        return resIdTargetMap;
    }

    protected String getOutputEncoding() {
        return Charsets.UTF_8.name();
    }

    private void writeTranslatedFileWithFileOutput(OutputStream output,
            URI originalFile, Map<String, TextFlowTarget> translations,
            net.sf.okapi.common.LocaleId localeId, IFilterWriter writer,
            String params, boolean approvedOnly) {
        File tempFile = null;

        try {
            tempFile = File.createTempFile("filename", "extension");
            writer.setOutput(tempFile.getCanonicalPath());
            generateTranslatedFile(originalFile, translations, localeId,
                    writer, params, approvedOnly);

            FileUtil.writeFileToOutputStream(tempFile, output);
        } catch (IOException|SecurityException e) {
            // FIXME log
            throw new FileFormatAdapterException(
                    "Unable to generate translated file", e);
        } finally {
            FileUtil.tryDeleteFile(tempFile);
        }

    }

    protected void generateTranslatedFile(URI originalFile,
            Map<String, TextFlowTarget> translations,
            net.sf.okapi.common.LocaleId localeId, IFilterWriter writer,
            String params, boolean approvedOnly) {
        RawDocument rawDoc =
                new RawDocument(originalFile, "UTF-8",
                        fromString("en"));
        if (rawDoc.getTargetLocale() == null) {
            rawDoc.setTargetLocale(localeId);
        }
        updateParams(params);
        try {
            filter.open(rawDoc);
            String subDocName = "";
            while (filter.hasNext()) {
                Event event = filter.next();
                if (event.getEventType() == EventType.START_SUBDOCUMENT) {
                    StartSubDocument startSubDoc =
                            (StartSubDocument) event.getResource();
                    subDocName = stripPath(startSubDoc.getName());
                } else if (event.getEventType() == EventType.TEXT_UNIT) {
                    TextUnit tu = (TextUnit) event.getResource();
                    if (!tu.getSource().isEmpty() && tu.isTranslatable()) {
                        String translatable = getTranslatableText(tu);

                        if (!translatable.isEmpty()) {
                            TextFlowTarget tft =
                                    translations.get(getIdFor(tu,
                                            translatable, subDocName));
                            if (tft != null && usable(tft.getState(), approvedOnly)) {
                                String translated = tft.getContents().get(0);
                                translated =
                                        getFullTranslationText(tu, translated);
                                tu.setTargetContent(localeId, GenericContent
                                        .fromLetterCodedToFragment(
                                                translated, tu.getSource()
                                                .getFirstContent()
                                                .clone(), true, true));
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

    private String getFullTranslationText(TextUnit tu, String translated) {
        if (separateNonTranslatable) {
            SplitString partitionedContent = getPartitionedText(tu);
            return partitionedContent.getPre() + translated
                    + partitionedContent.getSuf();
        } else {
            return translated;
        }
    }

    /**
     * Return the id for a TextUnit based on id assignment rules. This method
     * can be overridden for more complex id assignment.
     *
     * @param tu for which to get id
     * @return the id for the given tu
     */
    protected String getIdFor(TextUnit tu, String subDocName) {
        return getIdFor(tu, tu.getSource().toString(), subDocName);
    }

    protected String getIdFor(TextUnit tu, String content, String subDocName) {
        switch (idSource) {
        case contentHash:
            return HashUtil.generateHash(content);
        case textUnitName:
            return tu.getName();
        case subDocNameAndTextUnitId:
            return subDocName + ":" + tu.getId();
        case textUnitId:
        default:
            return tu.getId();
        }
    }

    private void updateParams(String params) {
        filter.getParameters().reset();
        updateParamsWithDefaults(filter.getParameters());
        if (!params.isEmpty()) filter.getParameters().fromString(params);
    }

    protected void updateParamsWithDefaults(IParameters params) {
        // default empty implementation is provided so that subclasses are not
        // forced to override when defaults are not needed.
    }

    @NotNull
    @Override
    public String generateTranslationFilename(@NotNull HDocument document,
            @NotNull String locale) throws IllegalArgumentException {
        return FileFormatAdapter.DefaultImpls.generateTranslationFilename(this, document, locale);
    }

}
