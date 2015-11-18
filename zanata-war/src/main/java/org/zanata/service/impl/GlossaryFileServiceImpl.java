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

import static org.jboss.seam.ScopeType.STATELESS;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.zanata.adapter.glossary.GlossaryCSVReader;
import org.zanata.adapter.glossary.GlossaryPoReader;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.HAccount;
import org.zanata.model.HGlossaryEntry;
import org.zanata.model.HGlossaryTerm;
import org.zanata.model.HLocale;
import org.zanata.rest.dto.GlossaryEntry;
import org.zanata.rest.dto.GlossaryResults;
import org.zanata.rest.dto.GlossaryTerm;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.service.GlossaryFileService;
import org.zanata.service.LocaleService;
import org.zanata.util.GlossaryUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@Name("glossaryFileServiceImpl")
@Scope(STATELESS)
@Slf4j
public class GlossaryFileServiceImpl implements GlossaryFileService {
    @In
    private GlossaryDAO glossaryDAO;

    @In
    private LocaleService localeServiceImpl;

    @In(required = false, value = ZanataJpaIdentityStore.AUTHENTICATED_USER, scope = ScopeType.SESSION)
    private HAccount authenticatedAccount;

    private final static int BATCH_SIZE = 50;

    @Override
    public List<List<GlossaryEntry>> parseGlossaryFile(InputStream fileContents,
            String fileName, LocaleId sourceLang, LocaleId transLang)
            throws ZanataServiceException {
        try {
            if (StringUtils.endsWithIgnoreCase(fileName, ".csv")) {
                return parseCsvFile(sourceLang, fileContents);
            } else if (StringUtils.endsWithIgnoreCase(fileName, ".po")) {
                return parsePoFile(fileContents, sourceLang, transLang);
            } else {
                throw new ZanataServiceException("Unsupported Glossary file: "
                        + fileName);
            }
        } catch (Exception e) {
            throw new ZanataServiceException("Error processing glossary file: "
                    + fileName + ". " + e.getMessage());
        }
    }

    private final static int MAX_LENGTH = 255;

    private String validateGlossaryEntry(GlossaryEntry entry) {
        if(StringUtils.length(entry.getDescription()) > MAX_LENGTH) {
            return "Glossary description too long, maximum " + MAX_LENGTH + " character";
        }
        if(StringUtils.length(entry.getPos()) > MAX_LENGTH) {
            return "Glossary part of speech too long, maximum " + MAX_LENGTH + " character";
        }
        return null;
    }

    @Override
    public GlossaryProcessed saveOrUpdateGlossary(
            List<GlossaryEntry> glossaryEntries) {
        Map<HGlossaryEntry, List<String>> results =
                new HashMap<HGlossaryEntry, List<String>>();

        int counter = 0;
        List<HGlossaryEntry> entries = Lists.newArrayList();
        List<String> warnings = Lists.newArrayList();
        for (int i = 0; i < glossaryEntries.size(); i++) {
            GlossaryEntry entry = glossaryEntries.get(i);

            String message = validateGlossaryEntry(entry);
            if(message != null) {
                warnings.add(message);
                counter++;
                if (counter == BATCH_SIZE || i == glossaryEntries.size() - 1) {
                    executeCommit();
                    counter = 0;
                }
                continue;
            }

            message = checkForDuplicateEntry(entry);
            boolean onlyTransferTransTerm = false;
            if(message != null) {
                //only update transTerm
                warnings.add(message);
                onlyTransferTransTerm = true;
            }
            HGlossaryEntry hGlossaryEntry = transferGlossaryEntryAndSave(
                    entry, onlyTransferTransTerm);
            entries.add(hGlossaryEntry);
            counter++;
            if (counter == BATCH_SIZE || i == glossaryEntries.size() - 1) {
                executeCommit();
                counter = 0;
            }
        }
        return new GlossaryProcessed(entries, warnings);
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public class GlossaryProcessed {
        private List<HGlossaryEntry> glossaryEntries;
        private List<String> warnings;
    }

    private List<List<GlossaryEntry>> parseCsvFile(LocaleId sourceLang,
            InputStream fileContents)
            throws IOException {
        GlossaryCSVReader csvReader =
                new GlossaryCSVReader(sourceLang, BATCH_SIZE);
        return csvReader.extractGlossary(new InputStreamReader(fileContents, "UTF-8"));
    }

    private List<List<GlossaryEntry>> parsePoFile(InputStream fileContents,
            LocaleId sourceLang, LocaleId transLang) throws IOException {
        if (sourceLang == null || transLang == null) {
            throw new ZanataServiceException(
                    "Mandatory fields for PO file format: Source Language and Target Language");
        }
        GlossaryPoReader poReader =
                new GlossaryPoReader(sourceLang, transLang, BATCH_SIZE);
        return poReader.extractGlossary(new InputStreamReader(fileContents));
    }

    /**
     * This force glossaryDAO to flush and commit every 50(BATCH_SIZE) records.
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
            hGlossaryEntry = glossaryDAO.getEntryByContentHash(contentHash);
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
     *
     * @param from
     */
    private String checkForDuplicateEntry(GlossaryEntry from) {
        GlossaryTerm srcTerm = getSrcGlossaryTerm(from);
        LocaleId srcLocale = from.getSrcLang();

        String contentHash = getContentHash(from);

        HGlossaryEntry sameHashEntry =
                glossaryDAO.getEntryByContentHash(contentHash);

        if(sameHashEntry == null) {
            return null;
        }
        // Different entry with same source content, pos and description
        if (!sameHashEntry.getId().equals(from.getId())) {
            return "Duplicate glossary entry in source locale '" + srcLocale
                + "' ,source content '" + srcTerm.getContent() + "' ,pos '"
                + from.getPos() + "' ,description '"
                + from.getDescription() + "'";
        }
        return null;
    }

    private String getContentHash(GlossaryEntry entry) {
        GlossaryTerm srcTerm = getSrcGlossaryTerm(entry);
        LocaleId srcLocale = entry.getSrcLang();

        return GlossaryUtil.generateHash(srcLocale, srcTerm.getContent(),
                entry.getPos(), entry.getDescription());
    }

    private HGlossaryEntry transferGlossaryEntryAndSave(GlossaryEntry from,
            boolean onlyTransferTransTerm) {
        HGlossaryEntry to =
                getOrCreateGlossaryEntry(from, getContentHash(from));

        to.setSourceRef(from.getSourceReference());
        to.setPos(from.getPos());
        to.setDescription(from.getDescription());

        TreeSet<String> warningMessage = Sets.newTreeSet();
        for (GlossaryTerm glossaryTerm : from.getGlossaryTerms()) {
            if (glossaryTerm == null || glossaryTerm.getLocale() == null) {
                continue;
            }
            if (onlyTransferTransTerm
                    && glossaryTerm.getLocale().equals(from.getSrcLang())) {
                continue;
            }

            HLocale termHLocale = localeServiceImpl.getByLocaleId(glossaryTerm
                .getLocale());

            if(termHLocale != null) {
                // check if there's existing term
                HGlossaryTerm hGlossaryTerm =
                    getOrCreateGlossaryTerm(to, termHLocale, glossaryTerm);
                hGlossaryTerm.setComment(glossaryTerm.getComment());
                hGlossaryTerm.setLastModifiedBy(authenticatedAccount
                        .getPerson());
                to.getGlossaryTerms().put(termHLocale, hGlossaryTerm);
            } else {
                warningMessage.add(glossaryTerm.getLocale().toString());
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

    private HGlossaryTerm getOrCreateGlossaryTerm(
            HGlossaryEntry hGlossaryEntry, HLocale termHLocale,
            GlossaryTerm newTerm) {
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
