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

package org.zanata.client.commands.push;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConsoleInteractorImpl;
import org.zanata.client.commands.FileMappingRuleHandler;
import org.zanata.client.commands.QualifiedSrcDocName;
import org.zanata.client.commands.TransFileResolver;
import org.zanata.client.commands.UnqualifiedSrcDocName;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.ProjectType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

public class GettextDirStrategy extends AbstractGettextPushStrategy {
    private static final Logger log = LoggerFactory
            .getLogger(GettextDirStrategy.class);

    @Override
    List<LocaleMapping> findLocales() {

        List<LocaleMapping> localesFoundOnDisk = Lists.newArrayList();

        LocaleList localeListInConfig = getOpts().getLocaleMapList();
        if (localeListInConfig == null || localeListInConfig.isEmpty()) {
            log.warn("No locale list in configuration (check zanata.xml)");
            return localesFoundOnDisk;
        }

        Set<String> srcDocNames = getSrcDocNames();
        for (LocaleMapping loc : localeListInConfig) {
            if (hasTranslationFileForLocale(loc, srcDocNames)) {
                localesFoundOnDisk.add(loc);
            } else {
                log.warn(
                        "configured locale {} not found; no translation file (local mapping {}) exist",
                        loc.getLocale(), loc.getLocalLocale());
            }
        }

        if (localesFoundOnDisk.size() == 0) {
            log.warn(
                    "'pushType' is set to '{}', but none of the configured locale "
                            + "files was found (check zanata.xml)", getOpts()
                            .getPushType());
        }

        return localeListInConfig;
    }

    @VisibleForTesting
    protected void setLocalSrcDocNames(Set<String> srcDocNames) {
        super.localSrcDocNames = srcDocNames;
    }

    private boolean hasTranslationFileForLocale(LocaleMapping loc,
            Set<String> srcDocNames) {
        for (String srcDocName : srcDocNames) {
            File transFile = new TransFileResolver(getOpts()).getTransFile(
                    UnqualifiedSrcDocName.from(srcDocName), loc);
            if (transFile.exists()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void checkSrcFileNames(String projectType, String[] srcFiles,
            boolean isInteractive) {

        boolean potentialProblem = checkForPotPrefix(srcFiles);
        if (potentialProblem) {
            String warningMsg =
                    "Found source file path starting with pot, perhaps you want to set source directory to pot?";
            ConsoleInteractorImpl console =
                    new ConsoleInteractorImpl();
            log.warn(warningMsg);
            if (isInteractive) {
                console.printfln(warningMsg);
                console.printf(
                        "Are you sure source directory [%s] is correct (y/n)?",
                        getOpts().getSrcDir());
                console.expectYes();
            }
        }
    }

    private boolean checkForPotPrefix(String[] srcFiles) {
        for (String src : srcFiles) {
            boolean potentialProblem =
                    new File(src).getPath()
                            .startsWith("pot" + File.separator);
            if (potentialProblem) {
                return true;
            }
        }
        return false;
    }

}
