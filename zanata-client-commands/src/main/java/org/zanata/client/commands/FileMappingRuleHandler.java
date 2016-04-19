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
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleMapping;
import org.zanata.client.util.FileUtil;
import org.zanata.common.DocumentType;
import org.zanata.common.ProjectType;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Parse translation file mapping rule as well as applying the rule to get the
 * final path of a translation file.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class FileMappingRuleHandler {
    private static final Logger log =
            LoggerFactory.getLogger(FileMappingRuleHandler.class);
    private final FileMappingRule mappingRule;
    private final ProjectType projectType;
    private final ConfigurableProjectOptions opts;

    public FileMappingRuleHandler(FileMappingRule rule, ProjectType projectType,
            ConfigurableProjectOptions opts) {
        this.projectType = projectType;
        this.opts = opts;
        this.mappingRule = rule;
    }

    public static boolean isRuleValid(String rule) {
        return rule.contains(Placeholders.locale.holder)
                || rule.contains(Placeholders.localeWithUnderscore.holder);
    }

    /**
     * Check whether the parsed rule is applicable to a source document.
     *
     * @param docNameWithExt
     *            source document name with extension
     * @return true if this parsed rule is applicable
     */
    public boolean isApplicable(DocNameWithExt docNameWithExt) {
        if (Strings.isNullOrEmpty(mappingRule.getPattern())) {
            return matchFileExtensionWithProjectType(docNameWithExt);
        }
        PathMatcher matcher =
            FileSystems.getDefault().getPathMatcher("glob:" + mappingRule.getPattern());
        // this will help when docNameWithExt has just file name i.e.
        // test.odt whereas pattern is defined as **/*.odt
        File srcFile =
                new File(opts.getSrcDir(),
                        docNameWithExt.getFullName());
        log.debug("trying to match pattern: {} to file: {}",
                mappingRule.getPattern(), srcFile.getPath());
        return matcher.matches(Paths.get(srcFile.getPath()));
    }

    private boolean matchFileExtensionWithProjectType(
            DocNameWithExt docNameWithExt) {
        List<DocumentType> documentTypes = projectType.getSourceFileTypes();
        for (DocumentType docType: documentTypes) {
            if (docType.getSourceExtensions().contains(
                    docNameWithExt.getExtension())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Apply the rule and return relative path of the translation file.
     *
     * @param docNameWithExt
     *            source document name with extension
     * @param localeMapping
     *            locale mapping
     * @return relative path (relative to trans-dir) for the translation file
     */
    public String getRelativeTransFilePathForSourceDoc(
            DocNameWithExt docNameWithExt,
            @Nonnull LocaleMapping localeMapping, Optional<String> translationFileExtension) {
        EnumMap<Placeholders, String> map =
                parseToMap(docNameWithExt.getFullName(), localeMapping, translationFileExtension);

        String transFilePath = mappingRule.getRule();
        for (Map.Entry<Placeholders, String> entry : map.entrySet()) {
            transFilePath =
                    transFilePath.replace(entry.getKey().holder,
                            entry.getValue());
            log.debug("replaced with {}, now is: {}", entry.getKey(),
                    transFilePath);
        }
        return FileUtil.simplifyPath(transFilePath);
    }

    @VisibleForTesting
    protected static EnumMap<Placeholders, String> parseToMap(
            @Nonnull String sourceFile, @Nonnull LocaleMapping localeMapping,
            Optional<String> translationFileExtension) {
        EnumMap<Placeholders, String> parts =
                new EnumMap<Placeholders, String>(Placeholders.class);
        File file = new File(sourceFile);

        String extension =
            translationFileExtension.isPresent() ? translationFileExtension.get()
                        : FilenameUtils.getExtension(sourceFile);
        String filename = FilenameUtils.removeExtension(file.getName());
        parts.put(Placeholders.extension, extension);
        parts.put(Placeholders.filename, filename);
        parts.put(Placeholders.locale, localeMapping.getLocalLocale());
        parts.put(Placeholders.localeWithUnderscore,
                localeMapping.getLocalLocale().replaceAll("\\-", "_"));
        String pathname = Strings.nullToEmpty(file.getParent());
        parts.put(Placeholders.path, FileUtil.simplifyPath(pathname));
        log.debug("parsed parts: {}", parts);
        return parts;
    }

    static enum Placeholders {
        path("{path}"),
        filename("{filename}"),
        locale("{locale}"),
        localeWithUnderscore("{locale_with_underscore}"),
        extension("{extension}");
        private final String holder;

        Placeholders(String holder) {
            this.holder = holder;
        }

        static List<String> allHolders() {
            return Lists.transform(Lists.newArrayList(
                    values()), new Function<Placeholders, String>() {
                @Override
                public String apply(Placeholders input) {
                    // this is just to make findbugs happy
                    return input == null ? "" : input.holder;
                }
            });
        }

        String holder() {
            return holder;
        }
    }
}
