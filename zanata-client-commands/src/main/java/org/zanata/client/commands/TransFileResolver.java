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

import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.ProjectType;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static org.zanata.client.commands.Messages.get;

/**
 * Resolve translation file destination. It will first try to apply file mapping
 * rules. If no rule is applicable, it will fall back to default mapping rules
 * for given project type.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransFileResolver {
    private static final Logger log =
            LoggerFactory.getLogger(TransFileResolver.class);
    private final ConfigurableProjectOptions opts;
    private static final Map<ProjectType, FileMappingRule>
            PROJECT_TYPE_FILE_MAPPING_RULES =
            ImmutableMap
                    .<ProjectType, FileMappingRule> builder()
                    .put(ProjectType.File, new FileMappingRule(
                            "{locale}/{path}/{filename}.{extension}"))
                    .put(ProjectType.Gettext, new FileMappingRule(
                            "{path}/{locale_with_underscore}.po"))
                    .put(ProjectType.Podir, new FileMappingRule(
                            "{locale}/{path}/{filename}.po"))
                    .put(ProjectType.Properties, new FileMappingRule(
                            "{path}/{filename}_{locale_with_underscore}.{extension}"))
                    .put(ProjectType.Utf8Properties, new FileMappingRule(
                            "{path}/{filename}_{locale_with_underscore}.{extension}"))
                    .put(ProjectType.Xliff, new FileMappingRule(
                            "{path}/{filename}_{locale_with_underscore}.{extension}"))
                    .put(ProjectType.Xml, new FileMappingRule(
                            "{path}/{filename}_{locale_with_underscore}.{extension}"))
                    .build();

    public TransFileResolver(ConfigurableProjectOptions opts) {
        this.opts = opts;
    }

    /**
     * Determines where to store the translation file for a given source
     * document and locale mapping.
     *
     * @param docNameWithExt
     *            source document name with extension
     * @param localeMapping
     *            locale mapping
     * @return translation destination
     */
    public File resolveTransFile(DocNameWithExt docNameWithExt,
            LocaleMapping localeMapping, Optional<String> translationFileExtension) {
        Optional<File> fileOptional =
                tryGetTransFileFromProjectMappingRules(docNameWithExt,
                        localeMapping, translationFileExtension);
        if (fileOptional.isPresent()) {
            return fileOptional.get();
        } else {
            ProjectType projectType = getProjectType();
            return getDefaultTransFileFromProjectType(docNameWithExt,
                    localeMapping, projectType, translationFileExtension);
        }
    }

    /**
     * Determines where to store the translation file for a given source
     * document and locale mapping.
     *
     * @param docNameWithoutExt
     *            source document name without extension
     * @param localeMapping
     *            locale mapping
     * @return translation destination
     */
    public File getTransFile(DocNameWithoutExt docNameWithoutExt,
            LocaleMapping localeMapping) {
        DocNameWithExt docNameWithExt =
                docNameWithoutExt.toDocNameWithExt(getProjectType());
        return resolveTransFile(docNameWithExt, localeMapping,
            Optional.<String>absent());
    }

    private ProjectType getProjectType() {
        try {
            return ProjectType.getValueOf(
                    opts.getProjectType());
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private File getDefaultTransFileFromProjectType(
            DocNameWithExt docNameWithExt, LocaleMapping localeMapping,
            ProjectType projectType, Optional<String> translationFileExtension) {
        FileMappingRule rule = PROJECT_TYPE_FILE_MAPPING_RULES.get(projectType);
        checkState(rule != null, get("no.default.mapping"), projectType);
        String relativePath = new FileMappingRuleHandler(rule, projectType, opts)
                .getRelativeTransFilePathForSourceDoc(docNameWithExt,
                        localeMapping, translationFileExtension);
        return new File(opts.getTransDir(), relativePath);
    }

    private Optional<File> tryGetTransFileFromProjectMappingRules(
            DocNameWithExt docNameWithExt, LocaleMapping localeMapping,
            Optional<String> translationFileExtension) {
        List<FileMappingRule> fileMappingRules = opts.getFileMappingRules();
        // TODO may need to sort the rules. put rules without pattern to last
        for (FileMappingRule rule : fileMappingRules) {
            FileMappingRuleHandler handler = new FileMappingRuleHandler(rule,
                    getProjectType(), opts);
            if (handler.isApplicable(docNameWithExt)) {
                String relativePath = handler
                        .getRelativeTransFilePathForSourceDoc(
                                docNameWithExt,
                                localeMapping, translationFileExtension);
                return Optional.of(new File(opts.getTransDir(), relativePath));
            }
        }
        if (fileMappingRules.size() > 0) {
            log.warn(
                    "None of the file mapping rule is applicable for {}. Please make sure your mapping is correct.",
                    docNameWithExt.getFullName());
        }
        return Optional.absent();
    }

}
