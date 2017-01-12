/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
package org.zanata.client.commands.glossary.push;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.adapter.glossary.AbstractGlossaryPushReader;
import org.zanata.adapter.glossary.GlossaryCSVReader;
import org.zanata.adapter.glossary.GlossaryPoReader;
import org.zanata.client.commands.ConfigurableCommand;
import org.zanata.client.commands.OptionsUtil;
import org.zanata.common.LocaleId;
import org.zanata.rest.client.GlossaryClient;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.dto.GlossaryEntry;

import com.google.common.collect.Lists;

import javax.ws.rs.client.ResponseProcessingException;

import static org.zanata.client.commands.glossary.push.GlossaryPushOptions.DEFAULT_SOURCE_LANG;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class GlossaryPushCommand extends
        ConfigurableCommand<GlossaryPushOptions> {
    private static final Logger log = LoggerFactory
            .getLogger(GlossaryPushCommand.class);

    private static final Map<String, AbstractGlossaryPushReader>
        glossaryReaders = new HashMap<String, AbstractGlossaryPushReader>();
    private final GlossaryClient client;

    public GlossaryPushCommand(GlossaryPushOptions opts,
            RestClientFactory clientFactory) {
        super(opts, clientFactory);
        client = getClientFactory().getGlossaryClient();
    }

    public GlossaryPushCommand(GlossaryPushOptions opts) {
        this(opts, OptionsUtil.createClientFactory(opts));

        LocaleId srcLocaleId = new LocaleId(DEFAULT_SOURCE_LANG);
        String transLang = getOpts().getTransLang();
        LocaleId transLocaleId = StringUtils.isNotBlank(transLang)
                ? new LocaleId(transLang) : null;
        glossaryReaders.put("po", new GlossaryPoReader(
                srcLocaleId, transLocaleId));
        glossaryReaders.put("csv", new GlossaryCSVReader(srcLocaleId));
    }

    private AbstractGlossaryPushReader getReader(String fileExtension) {
        AbstractGlossaryPushReader reader = glossaryReaders.get(fileExtension);
        if (reader == null) {
            throw new RuntimeException("unknown file type: " + fileExtension);
        }
        return reader;
    }

    private String validateFileExtensionWithTransLang() throws RuntimeException {
        String fileExtension =
                FilenameUtils.getExtension(getOpts().getFile().getName());

        if (fileExtension.equals("po")
                && StringUtils.isBlank(getOpts().getTransLang())) {
            throw new RuntimeException(
                    "Option '--trans-lang' is required for this file type.");
        }
        return fileExtension;
    }

    @Override
    public void run() throws Exception {

        log.info("Server: {}", getOpts().getUrl());
        log.info("Username: {}", getOpts().getUsername());
        log.info("Source language: {}", DEFAULT_SOURCE_LANG);
        log.info("Translation language: {}", getOpts().getTransLang());
        if (StringUtils.isNotBlank(getOpts().getProject())) {
            log.info("Project: {}", getOpts().getProject());
        }
        log.info("Glossary file: {}", getOpts().getFile());
        log.info("Batch size: {}", getOpts().getBatchSize());

        File glossaryFile = getOpts().getFile();

        if (glossaryFile == null) {
            throw new RuntimeException(
                    "Option '--file' is required.");
        }
        if (!glossaryFile.exists()) {
            throw new RuntimeException("File '" + glossaryFile
                    + "' does not exist. Check '--file' option");
        }

        if (getOpts().getBatchSize() <= 0) {
            throw new RuntimeException("Option '--batch-size' needs to be 1 or more.");
        }

        String fileExtension = validateFileExtensionWithTransLang();

        String project = getOpts().getProject();
        String qualifiedName;
        try {
            qualifiedName = StringUtils.isBlank(project)
                    ? client.getGlobalQualifiedName()
                    : client.getProjectQualifiedName(project);
        } catch (ResponseProcessingException rpe) {
            if (rpe.getResponse().getStatus() == 404) {
                log.error("Project {} not found", project);
                return;
            } else {
                throw rpe;
            }
        }
        AbstractGlossaryPushReader reader = getReader(fileExtension);

        log.info("Pushing glossary document [{}] to server",
                glossaryFile.getName());

        Reader inputStreamReader =
                new InputStreamReader(new FileInputStream(glossaryFile),
                        "UTF-8");
        BufferedReader br = new BufferedReader(inputStreamReader);

        Map<LocaleId, List<GlossaryEntry>> glossaries =
                reader.extractGlossary(br, qualifiedName);

        int totalEntries = 0;
        for (Map.Entry<LocaleId, List<GlossaryEntry>> entries : glossaries
                .entrySet()) {
            totalEntries = totalEntries + entries.getValue().size();
            log.info("Total entries:" + totalEntries);
        }

        int totalDone = 0;
        for (Map.Entry<LocaleId, List<GlossaryEntry>> entry : glossaries
                .entrySet()) {
            List<List<GlossaryEntry>> batches =
                    Lists.partition(entry.getValue(), getOpts().getBatchSize());
            for (List<GlossaryEntry> batch : batches) {
                client.post(batch, entry.getKey(), qualifiedName);
                totalDone = totalDone + batch.size();
                log.info("Pushed " + totalDone + " of " + totalEntries
                        + " entries");
            }
        }
    }
}
