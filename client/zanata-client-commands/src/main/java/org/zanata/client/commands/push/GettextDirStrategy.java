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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.ConsoleInteractorImpl;
import org.zanata.client.commands.TransFileResolver;
import org.zanata.client.commands.DocNameWithoutExt;
import org.zanata.client.config.LocaleList;
import org.zanata.client.config.LocaleMapping;

import com.google.common.collect.Lists;

public class GettextDirStrategy extends AbstractGettextPushStrategy {
    private static final Logger log = LoggerFactory
            .getLogger(GettextDirStrategy.class);

    @Override
    List<LocaleMapping> findLocales(String srcDocName) {

        List<LocaleMapping> localesFoundOnDisk = Lists.newArrayList();

        LocaleList localeListInConfig = getOpts().getLocaleMapList();
        if (localeListInConfig == null || localeListInConfig.isEmpty()) {
            log.warn("No locale list in configuration (check your server settings)");
            return localesFoundOnDisk;
        }

        for (LocaleMapping loc : localeListInConfig) {
            if (hasTranslationFileForLocale(loc, srcDocName)) {
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
                            + "files was found (check your server settings)", getOpts()
                            .getPushType());
        }

        return localesFoundOnDisk;
    }

    private boolean hasTranslationFileForLocale(LocaleMapping loc,
            String srcDocName) {
        File transFile = new TransFileResolver(getOpts()).getTransFile(
                DocNameWithoutExt.from(srcDocName), loc);
        return transFile.exists();
    }

    @Override
    protected void checkSrcFileNames(String projectType, String[] srcFiles,
            boolean isInteractive) {

        boolean potentialProblem = checkForPotPrefix(srcFiles);
        if (potentialProblem) {
            String warningMsg =
                    "Found source file path starting with pot, perhaps you want to set source directory to pot?";
            ConsoleInteractorImpl console =
                    new ConsoleInteractorImpl(getOpts());
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
