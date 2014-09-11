/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleMapping;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FileMappingRuleParser {
    private static final Logger log =
            LoggerFactory.getLogger(FileMappingRuleParser.class);
    private final FileMappingRule mappingRule;

    public FileMappingRuleParser(FileMappingRule rule) {
        Preconditions.checkArgument(isRuleValid(rule.getRule()));
        this.mappingRule = rule;
    }

    @VisibleForTesting
    protected static boolean isRuleValid(String rule) {
        return rule.contains(Placeholders.locale.holder)
                || rule.contains(Placeholders.localeWithUnderscore.holder);
    }

    public boolean isApplicable(String qualifiedDocName) {
        boolean ruleValid = isRuleValid(mappingRule.getRule());
        if (!ruleValid) {
            return false;
        }
        PathMatcher matcher =
            FileSystems.getDefault().getPathMatcher("glob:" + mappingRule.getPattern());
        return matcher.matches(Paths.get(qualifiedDocName));
    }

    public String getRelativePathFromRule(@Nonnull String sourceFile,
            @Nonnull LocaleMapping localeMapping) {
        EnumMap<Placeholders, String> map =
                parseToMap(sourceFile, localeMapping);

        String temp = mappingRule.getRule();
        for (Map.Entry<Placeholders, String> entry : map.entrySet()) {
            temp = temp.replace(entry.getKey().holder, entry.getValue());
            log.debug("replaced with {}, now is: {}", entry.getKey(), temp);
        }
        return Files.simplifyPath(temp);
    }

    private EnumMap<Placeholders, String> parseToMap(
            @Nonnull String sourceFile, @Nonnull LocaleMapping originalLocale) {
        EnumMap<Placeholders, String> parts =
                new EnumMap<Placeholders, String>(Placeholders.class);
        File file = new File(sourceFile);
        String extension = Files.getFileExtension(sourceFile);
        String filename =
                file.getName().replaceFirst("\\." + extension + "$", "");
        parts.put(Placeholders.extension, extension);
        parts.put(Placeholders.filename, filename);
        parts.put(Placeholders.locale, originalLocale.getLocalLocale());
        parts.put(Placeholders.localeWithUnderscore,
                originalLocale.getLocalLocale().replaceAll("\\-", "_"));
        parts.put(Placeholders.path, Files.simplifyPath(file.getParent()));
        log.debug("parsed parts: {}", parts);
        return parts;
    }

    enum Placeholders {
        path("{path}"),
        filename("{filename}"),
        locale("{locale}"),
        localeWithUnderscore("{locale_with_underscore}"),
        extension("{extension}");
        private final String holder;

        Placeholders(String holder) {
            this.holder = holder;
        }
    }
}
