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

package org.zanata.client.commands;

import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.pull.PullCommand;
import org.zanata.client.commands.pull.PullOptions;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.etag.ETagCache;
import org.zanata.client.etag.ETagCacheReaderWriter;
import org.zanata.client.exceptions.ConfigException;
import org.zanata.common.LocaleId;
import org.zanata.rest.client.RestClientFactory;
import org.zanata.rest.client.SourceDocResourceClient;
import org.zanata.rest.client.StatisticsResourceClient;
import org.zanata.rest.client.TransDocResourceClient;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.dto.stats.ContainerTranslationStatistics;
import org.zanata.rest.dto.stats.TranslationStatistics;
import org.zanata.util.PathUtil;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public abstract class PushPullCommand<O extends PushPullOptions> extends
        ConfigurableProjectCommand<O> {
    private static final Logger log = LoggerFactory
            .getLogger(PushPullCommand.class);

    protected static final String PROJECT_TYPE_OFFLINE_PO = "offlinepo";

    protected ETagCache eTagCache;
    private Marshaller marshaller;
    private String modulePrefix;
    protected SourceDocResourceClient sourceDocResourceClient;
    protected TransDocResourceClient transDocResourceClient;
    protected final StatisticsResourceClient statsClient;

    public PushPullCommand(O opts, RestClientFactory clientFactory) {
        super(opts, clientFactory);
        this.modulePrefix =
                opts.getEnableModules() ? getOpts().getCurrentModule()
                        + opts.getModuleSuffix() : "";
        this.loadETagCache();
        sourceDocResourceClient =
                getClientFactory().getSourceDocResourceClient(opts.getProj(),
                        opts.getProjectVersion());
        transDocResourceClient =
                getClientFactory().getTransDocResourceClient(opts.getProj(),
                        opts.getProjectVersion());
        statsClient = getClientFactory().getStatisticsClient();
    }

    public PushPullCommand(O opts) {
        this(opts, OptionsUtil.createClientFactory(
                opts));
    }

    protected void confirmWithUser(String message) throws IOException {
        if (getOpts().isInteractiveMode()) {
            Console console = System.console();
            if (console == null) {
                throw new RuntimeException(
                        "console not available: please run Maven from a console, or use batch mode option (-B)");
            }
            console.printf(message + "\nAre you sure (y/n)? ");
            expectYes(console);
        }
    }

    protected void debug(Object jaxbElement) {
        try {
            if (getOpts().isDebugSet()) {
                StringWriter writer = new StringWriter();
                getMarshaller().marshal(jaxbElement, writer);
                log.debug("{}", writer);
            }
        } catch (JAXBException e) {
            log.debug(e.toString(), e);
        }
    }

    /**
     * @return
     * @throws JAXBException
     */
    private Marshaller getMarshaller() throws JAXBException {
        if (marshaller == null) {
            JAXBContext jc =
                    JAXBContext.newInstance(Resource.class,
                            TranslationsResource.class);
            marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        }
        return marshaller;
    }

    protected String qualifiedDocName(String localDocName) {
        String qualifiedDocName = modulePrefix + localDocName;
        return qualifiedDocName;
    }

    protected String unqualifiedDocName(String qualifiedDocName) {
        assert qualifiedDocName.startsWith(modulePrefix);
        return qualifiedDocName.substring(modulePrefix.length());
    }

    protected boolean belongsToCurrentModule(String qualifiedDocName) {
        return qualifiedDocName.startsWith(modulePrefix);
    }

    protected List<String> getQualifiedDocNamesForCurrentModuleFromServer() {
        List<ResourceMeta> remoteDocList =
                getDocListForProjectIterationFromServer();
        List<String> docNames = new ArrayList<String>();
        for (ResourceMeta doc : remoteDocList) {
            // NB ResourceMeta.name = HDocument.docId
            String qualifiedDocName = doc.getName();
            if (getOpts().getEnableModules()) {
                if (belongsToCurrentModule(qualifiedDocName)) {
                    docNames.add(qualifiedDocName);
                } else {
                    log.debug("found extra-modular document: {}",
                            qualifiedDocName);
                }
            } else {
                docNames.add(qualifiedDocName);
            }
        }
        return docNames;
    }

    // TODO use a cache which will be accessible to all invocations
    protected List<ResourceMeta> getDocListForProjectIterationFromServer() {
        return sourceDocResourceClient.getResourceMeta(null);
    }

    /**
     * Filters the project's list of locales
     *
     * @param projectLocales
     *            locales defined by the project on the server or in zanata.xml
     *            (only for backward compatibility)
     * @param locales
     *            locales requested by the user (eg Maven param, command line
     *            option)
     * @return the filtered list of locales
     * @throws ConfigException
     *             if one of the requested locales was not found on the server
     *             or in zanata.xml (only for backward compatibility)
     */
    public static LocaleList getLocaleMapList(LocaleList projectLocales,
            String[] locales) {
        if (locales == null || locales.length <= 0) {
            return projectLocales;
        } else {
            // filter the locales that are specified in both the global config
            // and the parameter list
            LocaleList effectiveLocales = new LocaleList();
            for (String locale : locales) {
                boolean foundLocale = false;
                for (LocaleMapping lm : projectLocales) {
                    if (lm.getLocale().equals(locale)
                            || (lm.getMapFrom() != null && lm.getMapFrom()
                                    .equals(locale))) {
                        effectiveLocales.add(lm);
                        foundLocale = true;
                        break;
                    }
                }

                if (!foundLocale) {
                    throw new ConfigException("Specified locale '" + locale
                            + "' was not found! Available locales: "
                            + projectLocales);
                }
            }
            return effectiveLocales;
        }
    }

    protected void loadETagCache() {
        try {
            String location =
                    ".zanata-cache" + File.separator + "etag-cache.xml";
            if (modulePrefix != null && !modulePrefix.trim().isEmpty()) {
                location = modulePrefix + File.separator + location;
            }
            eTagCache =
                    ETagCacheReaderWriter.readCache(new FileInputStream(
                            location));
        } catch (Exception e) {
            // could not read for some reason, use a new one
            eTagCache = new ETagCache();
        }
    }

    protected void storeETagCache() {
        try {
            String location =
                    ".zanata-cache" + File.separator + "etag-cache.xml";
            if (modulePrefix != null && !modulePrefix.trim().isEmpty()) {
                location = modulePrefix + File.separator + location;
            }

            File targetFile = new File(location);
            if (!targetFile.exists()) {
                PathUtil.makeDirs(targetFile.getParentFile());
            }
            ETagCacheReaderWriter.writeCache(this.eTagCache,
                    new FileOutputStream(location));
        } catch (IOException e) {
            log.warn("Could not create Zanata ETag cache file. Will proceed without it.");
        }
    }

    protected Map<String, Map<LocaleId, TranslatedPercent>> getDocsTranslatedPercent(
            LocaleList locales) {
        ContainerTranslationStatistics statistics =
                getDetailStatisticsForProjectVersion(locales);
        List<ContainerTranslationStatistics> statsPerDoc =
                statistics.getDetailedStats();
        ImmutableMap.Builder<String, Map<LocaleId, TranslatedPercent>> docIdToStatsBuilder =
                ImmutableMap.builder();
        for (ContainerTranslationStatistics docStats : statsPerDoc) {
            String docId = docStats.getId();
            List<TranslationStatistics> statsPerLocale = docStats.getStats();
            ImmutableMap.Builder<LocaleId, TranslatedPercent> localeToStatsBuilder =
                    ImmutableMap.builder();

            for (TranslationStatistics statsForSingleLocale : statsPerLocale) {
                // TODO server statistics API should return locale with alias
                TranslatedPercent translatedPercent =
                        new TranslatedPercent(statsForSingleLocale.getTotal(),
                                statsForSingleLocale.getTranslatedOnly(),
                                statsForSingleLocale.getApproved());

                localeToStatsBuilder.put(
                        new LocaleId(statsForSingleLocale.getLocale()),
                        translatedPercent);
            }
            Map<LocaleId, TranslatedPercent> localeStats =
                    localeToStatsBuilder.build();
            docIdToStatsBuilder.put(docId, localeStats);
        }
        return docIdToStatsBuilder.build();
    }

    @VisibleForTesting
    protected ContainerTranslationStatistics getDetailStatisticsForProjectVersion(
            LocaleList locales) {
        String[] localesOnServer = new String[locales.size()];
        for (int i = 0; i < locales.size(); i++) {
             localesOnServer[i] = locales.get(i).getLocale();
        }
        return statsClient
                    .getStatistics(getOpts().getProj(),
                            getOpts().getProjectVersion(), true, false, localesOnServer);
    }

    /**
     * this stats map will have docId as key, the value is another map with
     * localeId as key and translated percent as value.
     * It's optional if we require statistics to determine which file to pull.
     * In cases where statistics is not required,
     * i.e. pull source only or minimum percent is set to 0, this will be
     * Optional.absence().
     *
     * @param pullTarget whether we need to pull translation target
     * @param locales
     * @return either detailed document statistics or optional.absence()
     */
    protected Optional<Map<String, Map<LocaleId, TranslatedPercent>>> prepareStatsIfApplicable(
            boolean pullTarget, LocaleList locales) {

        Optional<Map<String, Map<LocaleId, TranslatedPercent>>> optionalStats =
                Optional.absent();
        O opts = getOpts();
        if (pullTarget && opts instanceof PullOptions &&
                ((PullOptions) opts).getMinDocPercent() > 0) {
            optionalStats = Optional.of(getDocsTranslatedPercent(locales));
        }
        return optionalStats;
    }

    protected boolean shouldPullThisLocale(
            Optional<Map<String, Map<LocaleId, TranslatedPercent>>> optionalStats,
            String localDocName, LocaleId serverLocale) {
        int minDocPercent = ((PullOptions) getOpts()).getMinDocPercent();
        if (log.isDebugEnabled() && optionalStats.isPresent()) {
            log.debug("{} for locale {} is translated {}%", localDocName,
                    serverLocale, optionalStats.get()
                            .get(localDocName).get(serverLocale)
                    .getTranslatedPercent());
        }
        return !optionalStats.isPresent()
                || optionalStats.get().get(localDocName).get(serverLocale)
                        .isAboveThreshold(minDocPercent);
    }

    protected static class TranslatedPercent {
        private final double translatedPercent;
        private final long total;
        private final long translated;
        private final long approved;

        public TranslatedPercent(long total, long translated, long approved) {
            this.total = total;
            this.translated = translated;
            this.approved = approved;
            if (total == 0) {
                // in some case the document has no content, we want to pull the
                // translation file regardless
                translatedPercent = 100;
            } else {
                translatedPercent = (translated + approved) * 100.0 / total;

            }
        }

        public boolean isAboveThreshold(int minimumPercent) {
            // if minimum percent is 100, we will compare exact number so that
            // rounding issue won't affect the result
            if (minimumPercent == 100) {
                return total == translated + approved;
            } else {
                return translatedPercent >= minimumPercent;
            }
        }

        public double getTranslatedPercent() {
            return translatedPercent;
        }
    }
}
