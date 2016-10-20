package org.zanata.client.commands.push;

import static org.apache.commons.io.FileUtils.listFiles;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.TransFileResolver;
import org.zanata.client.commands.DocNameWithoutExt;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class GettextPushStrategy extends AbstractGettextPushStrategy {
    private static final Logger log = LoggerFactory
            .getLogger(GettextPushStrategy.class);

    @Override
    List<LocaleMapping> findLocales(String srcDocName) {
        // find all .po basenames in this dir and subdirs
        Collection<File> transFilesOnDisk =
                listFiles(getOpts().getTransDir(), new String[] { "po" }, true);

        final LocaleList localeListInConfig = getOpts().getLocaleMapList();

        if (localeListInConfig == null || localeListInConfig.isEmpty()) {
            log.warn("No locale list in configuration (check your server settings)");
            return Collections.emptyList();
        }

        final DocNameWithoutExt docNameWithoutExt =
                DocNameWithoutExt.from(srcDocName);
        List<File> transFilesDestinations =
                Lists.transform(localeListInConfig,
                        new LocaleMappingToTransFile(docNameWithoutExt,
                                getOpts()));
        // we remove all the ones that WILL be mapped and treated as
        // translation files
        transFilesOnDisk.removeAll(transFilesDestinations);
        // for all remaining po files we give a warning
        for (File transFile : transFilesOnDisk) {
            log.warn(
                    "Skipping file {}; no locale entry found from project config",
                    transFile);
        }
        return localeListInConfig;
    }

    private static class LocaleMappingToTransFile implements
            Function<LocaleMapping, File> {
        private final DocNameWithoutExt docNameWithoutExt;
        private TransFileResolver transFileResolver;

        public LocaleMappingToTransFile(
                DocNameWithoutExt docNameWithoutExt, PushOptions opts) {
            this.docNameWithoutExt = docNameWithoutExt;
            transFileResolver = new TransFileResolver(opts);
        }

        @Override
        public File apply(LocaleMapping localeMapping) {
            return transFileResolver.getTransFile(docNameWithoutExt,
                    localeMapping);
        }
    }
}
