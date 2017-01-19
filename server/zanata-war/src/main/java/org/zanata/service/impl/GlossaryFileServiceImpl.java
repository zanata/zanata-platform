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
package org.zanata.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import javax.annotation.Nullable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.adapter.glossary.GlossaryCSVReader;
import org.zanata.adapter.glossary.GlossaryPoReader;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.Glossary;
import org.zanata.model.HAccount;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.model.WebHook;
import org.zanata.model.type.WebhookType;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.rest.dto.QualifiedName;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.GlossaryFileService;
import org.zanata.service.LocaleService;
import org.zanata.util.GlossaryUtil;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Named("glossaryFileServiceImpl")
@RequestScoped
public class GlossaryFileServiceImpl implements GlossaryFileService {

    private static final Logger log =
            LoggerFactory.getLogger(GlossaryFileServiceImpl.class);
    @Inject
    private GlossaryDAO glossaryDAO;
    @Inject
    private LocaleService localeServiceImpl;
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;
    private static final int BATCH_SIZE = 50;
    private static final int MAX_LENGTH_CHAR = 255;

    @Override
    public Map<LocaleId, List<GlossaryEntry>> parseGlossaryFile(
            InputStream inputStream, String fileName, LocaleId sourceLang,
            @Nullable LocaleId transLang, String qualifiedName)
            throws ZanataServiceException {
        try {
            if (FilenameUtils.getExtension(fileName).equals("csv")) {
                return parseCsvFile(sourceLang, qualifiedName, inputStream);
            } else if (FilenameUtils.getExtension(fileName).equals("po")) {
                return parsePoFile(inputStream, sourceLang, transLang,
                        qualifiedName);
            }
            throw new ZanataServiceException(
                    "Unsupported Glossary file: " + fileName);
        } catch (Exception e) {
            throw new ZanataServiceException("Error processing glossary file: "
                    + fileName + ". " + e.getMessage());
        }
    }

    @Override
    public GlossaryProcessed saveOrUpdateGlossary(
            List<GlossaryEntry> dtoEntries, Optional<LocaleId> transLocaleId) {
        int counter = 0;
        List<HGlossaryEntry> entries = Lists.newArrayList();
        List<String> warnings = Lists.newArrayList();
        for (int i = 0; i < dtoEntries.size(); i++) {
            GlossaryEntry entry = dtoEntries.get(i);
            Optional<String> message = validateGlossaryEntry(entry);
            if (message.isPresent()) {
                warnings.add(message.get());
                counter++;
                if (isExecuteCommit(counter, i, dtoEntries.size())) {
                    counter = 0;
                }
                continue;
            }
            message = checkForDuplicateEntry(entry);
            boolean onlyTransferTransTerm = false;
            if (message.isPresent()) {
                // only update transTerm
                warnings.add(message.get());
                onlyTransferTransTerm = true;
            }
            HGlossaryEntry hGlossaryEntry = transferGlossaryEntryAndSave(entry,
                    transLocaleId, onlyTransferTransTerm);
            entries.add(hGlossaryEntry);
            counter++;
            if (isExecuteCommit(counter, i, dtoEntries.size())) {
                counter = 0;
            }
        }
        return new GlossaryProcessed(entries, warnings);
    }

    /**
     * Run {@link #executeCommit} when - counter equals to {@link #BATCH_SIZE}
     * or - currentIndex equals to totalSize (last record)
     */
    private boolean isExecuteCommit(int counter, int currentIndex,
            int totalSize) {
        if (counter == BATCH_SIZE || currentIndex == totalSize - 1) {
            executeCommit();
            return true;
        }
        return false;
    }

    /**
     * Return error message when
     *
     * @param entry#description
     *            length not over {@link #MAX_LENGTH_CHAR}
     * @param entry#pos
     *            length not over {@link #MAX_LENGTH_CHAR} Source term content
     *            not empty
     */
    private Optional<String> validateGlossaryEntry(GlossaryEntry entry) {
        if (StringUtils.length(entry.getDescription()) > MAX_LENGTH_CHAR) {
            return Optional.of("Glossary description too long, maximum "
                    + MAX_LENGTH_CHAR + " character");
        }
        if (StringUtils.length(entry.getPos()) > MAX_LENGTH_CHAR) {
            return Optional.of("Glossary part of speech too long, maximum "
                    + MAX_LENGTH_CHAR + " character");
        }
        Optional<GlossaryTerm> srcTerm = getSourceTerm(entry);
        if (!srcTerm.isPresent()) {
            return Optional.of("No source term (" + entry.getSrcLang()
                    + ") found in Glossary entry.");
        }
        if (StringUtils.isBlank(srcTerm.get().getContent())) {
            return Optional.of("Source term content cannot be empty.");
        }
        return Optional.empty();
    }

    private Optional<GlossaryTerm> getSourceTerm(GlossaryEntry entry) {
        for (GlossaryTerm term : entry.getGlossaryTerms()) {
            if (term.getLocale().equals(entry.getSrcLang())) {
                return Optional.of(term);
            }
        }
        return Optional.empty();
    }

    public class GlossaryProcessed {
        private List<HGlossaryEntry> glossaryEntries;
        private List<String> warnings;

        public List<HGlossaryEntry> getGlossaryEntries() {
            return this.glossaryEntries;
        }

        public List<String> getWarnings() {
            return this.warnings;
        }

        public void
                setGlossaryEntries(final List<HGlossaryEntry> glossaryEntries) {
            this.glossaryEntries = glossaryEntries;
        }

        public void setWarnings(final List<String> warnings) {
            this.warnings = warnings;
        }

        @java.beans.ConstructorProperties({ "glossaryEntries", "warnings" })
        public GlossaryProcessed(final List<HGlossaryEntry> glossaryEntries,
                final List<String> warnings) {
            this.glossaryEntries = glossaryEntries;
            this.warnings = warnings;
        }
    }

    private Map<LocaleId, List<GlossaryEntry>> parseCsvFile(LocaleId sourceLang,
            String qualifiedName, InputStream inputStream) throws IOException {
        GlossaryCSVReader csvReader = new GlossaryCSVReader(sourceLang);
        return csvReader.extractGlossary(new InputStreamReader(inputStream,
                Charsets.UTF_8.displayName()), qualifiedName);
    }

    private Map<LocaleId, List<GlossaryEntry>> parsePoFile(
            InputStream inputStream, LocaleId sourceLang, LocaleId transLang,
            String qualifiedName) throws IOException {
        if (sourceLang == null || transLang == null) {
            throw new ZanataServiceException(
                    "Mandatory fields for PO file format: Source Language and Target Language");
        }
        GlossaryPoReader poReader = new GlossaryPoReader(sourceLang, transLang);
        Reader reader = new BufferedReader(new InputStreamReader(inputStream,
                Charsets.UTF_8.displayName()));
        return poReader.extractGlossary(reader, qualifiedName);
    }
    // TODO does that mean the reads aren't in transactions?
    // TODO use Transactional at class level?

    /**
     * This force glossaryDAO to flush and commit on every {@link #BATCH_SIZE}
     * records.
     */
    @Transactional
    private void executeCommit() {
        glossaryDAO.flush();
        glossaryDAO.clear();
    }

    private HGlossaryEntry getOrCreateGlossaryEntry(GlossaryEntry from,
            String contentHash) {
        LocaleId srcLocale = from.getSrcLang();
        Long id = from.getId();
        HGlossaryEntry hGlossaryEntry;
        if (id != null) {
            hGlossaryEntry = glossaryDAO.findById(id);
        } else {
            hGlossaryEntry = glossaryDAO.getEntryByContentHash(contentHash,
                    from.getQualifiedName().getName());
        }
        if (hGlossaryEntry == null) {
            hGlossaryEntry = new HGlossaryEntry();
            HLocale srcHLocale = localeServiceImpl.getByLocaleId(srcLocale);
            hGlossaryEntry.setSrcLocale(srcHLocale);
            hGlossaryEntry.setSourceRef(from.getSourceReference());
        }
        return hGlossaryEntry;
    }

    /**
     * Check if request save/update entry have duplication with same source
     * content, pos, and description
     */
    private Optional<String> checkForDuplicateEntry(GlossaryEntry from) {
        GlossaryTerm srcTerm = getSrcGlossaryTerm(from);
        LocaleId srcLocale = from.getSrcLang();
        String contentHash = getContentHash(from);
        HGlossaryEntry sameHashEntry = glossaryDAO.getEntryByContentHash(
                contentHash, from.getQualifiedName().getName());
        if (sameHashEntry == null) {
            return Optional.empty();
        }
        // Different entry with same source content, pos and description
        if (!sameHashEntry.getId().equals(from.getId())) {
            return Optional.of("Duplicate glossary entry in source locale \'"
                    + srcLocale + "\' ,source content \'" + srcTerm.getContent()
                    + "\' ,pos \'" + from.getPos() + "\' ,description \'"
                    + from.getDescription() + "\'");
        }
        return Optional.empty();
    }

    private String getContentHash(GlossaryEntry entry) {
        GlossaryTerm srcTerm = getSrcGlossaryTerm(entry);
        LocaleId srcLocale = entry.getSrcLang();
        return GlossaryUtil.generateHash(srcLocale, srcTerm.getContent(),
                entry.getPos(), entry.getDescription());
    }

    private HGlossaryEntry transferGlossaryEntryAndSave(GlossaryEntry from,
            Optional<LocaleId> transLocaleId, boolean onlyTransferTransTerm) {
        HGlossaryEntry to =
                getOrCreateGlossaryEntry(from, getContentHash(from));
        to.setSourceRef(from.getSourceReference());
        to.setPos(from.getPos());
        to.setDescription(from.getDescription());
        String qualifiedName = GlossaryUtil.GLOBAL_QUALIFIED_NAME;
        if (from.getQualifiedName() != null
                && StringUtils.isNotBlank(from.getQualifiedName().getName())) {
            qualifiedName = from.getQualifiedName().getName();
        }
        Glossary glossary =
                glossaryDAO.getGlossaryByQualifiedName(qualifiedName);
        if (glossary == null) {
            glossary = new Glossary(qualifiedName);
            glossaryDAO.persistGlossary(glossary);
            executeCommit();
        }
        to.setGlossary(glossary);
        TreeSet<String> warningMessage = Sets.newTreeSet();
        List<GlossaryTerm> filteredTerms =
                from.getGlossaryTerms().stream().filter(term -> {
                    if (term == null || term.getLocale() == null) {
                        return false;
                    }
                    if (onlyTransferTransTerm
                            && term.getLocale().equals(from.getSrcLang())) {
                        return false;
                    }
                    if (onlyTransferTransTerm && transLocaleId.isPresent()
                            && !term.getLocale().equals(transLocaleId.get())) {
                        return false;
                    }
                    return true;
                }).collect(Collectors.toList());
        for (GlossaryTerm term : filteredTerms) {
            HLocale termHLocale =
                    localeServiceImpl.getByLocaleId(term.getLocale());
            if (termHLocale != null) {
                // check if there's existing term
                HGlossaryTerm hGlossaryTerm =
                        getOrCreateGlossaryTerm(to, termHLocale, term);
                hGlossaryTerm.setComment(term.getComment());
                hGlossaryTerm
                        .setLastModifiedBy(authenticatedAccount.getPerson());
                to.getGlossaryTerms().put(termHLocale, hGlossaryTerm);
            } else {
                warningMessage.add(term.getLocale().toString());
            }
        }
        if (!warningMessage.isEmpty()) {
            log.warn(
                    "Language {} is not enabled in Zanata. Term in the language will be ignored.",
                    StringUtils.join(warningMessage, ","));
        }
        glossaryDAO.makePersistent(to);
        return to;
    }

    private HGlossaryTerm getOrCreateGlossaryTerm(HGlossaryEntry hGlossaryEntry,
            HLocale termHLocale, GlossaryTerm newTerm) {
        HGlossaryTerm hGlossaryTerm =
                hGlossaryEntry.getGlossaryTerms().get(termHLocale);
        if (hGlossaryTerm == null) {
            hGlossaryTerm = new HGlossaryTerm(newTerm.getContent());
            hGlossaryTerm.setLocale(termHLocale);
            hGlossaryTerm.setGlossaryEntry(hGlossaryEntry);
        } else if (!hGlossaryTerm.getContent().equals(newTerm.getContent())) {
            hGlossaryTerm.setContent(newTerm.getContent());
        }
        return hGlossaryTerm;
    }

    private GlossaryTerm getSrcGlossaryTerm(GlossaryEntry entry) {
        for (GlossaryTerm term : entry.getGlossaryTerms()) {
            if (term.getLocale().equals(entry.getSrcLang())) {
                return term;
            }
        }
        return null;
    }
}
