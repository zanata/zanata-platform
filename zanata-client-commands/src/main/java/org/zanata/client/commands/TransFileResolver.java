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
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import org.zanata.client.config.FileMappingRule;
import org.zanata.client.config.LocaleMapping;
import org.zanata.common.ProjectType;

import java.io.File;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkState;
import static org.zanata.client.commands.Messages._;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransFileResolver {
    private final ConfigurableProjectOptions opts;
    private Map<ProjectType, FileMappingRule> defaultProjectTypeToRules =
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

    public File getTransFile(QualifiedSrcDocName qualifiedSrcDocName,
            LocaleMapping localeMapping) {
        Optional<File> fileOptional =
                tryGetTransFileFromProjectMappingRules(qualifiedSrcDocName,
                        localeMapping);
        if (fileOptional.isPresent()) {
            return fileOptional.get();
        } else {
            ProjectType projectType = getProjectType();
            return getDefaultTransFileFromProjectType(qualifiedSrcDocName,
                    localeMapping, projectType);
        }
    }

    public File getTransFile(UnqualifiedSrcDocName unqualifiedSrcDocName,
            LocaleMapping localeMapping) {
        QualifiedSrcDocName qualifiedSrcDocName =
                unqualifiedSrcDocName.toQualifiedDocName(getProjectType());
        return getTransFile(qualifiedSrcDocName, localeMapping);
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
            QualifiedSrcDocName qualifiedSrcDocName, LocaleMapping localeMapping,
            ProjectType projectType) {
        FileMappingRule rule = defaultProjectTypeToRules.get(projectType);
        checkState(rule != null, _("no.default.mapping"), projectType);
        String relativePath = new FileMappingRuleParser(rule, projectType, opts)
                .getRelativePathFromRule(qualifiedSrcDocName, localeMapping);
        return new File(opts.getTransDir(), relativePath);
    }

    private Optional<File> tryGetTransFileFromProjectMappingRules(
            QualifiedSrcDocName qualifiedSrcDocName, LocaleMapping localeMapping) {
        List<FileMappingRule> fileMappingRules = opts.getFileMappingRules();
        // TODO may need to sort the rules. put rules without pattern to last
        for (FileMappingRule rule : fileMappingRules) {
            FileMappingRuleParser parser = new FileMappingRuleParser(rule,
                    getProjectType(), opts);
            if (parser.isApplicable(qualifiedSrcDocName)) {
                String relativePath = parser
                        .getRelativePathFromRule(qualifiedSrcDocName,
                                localeMapping);
                return Optional.of(new File(opts.getTransDir(), relativePath));
            }
        }
        return Optional.absent();
    }

    /**
     * Represents document name with extension.
     */
    public static class QualifiedSrcDocName {
        private final String fullName;
        private final String extension;

        private QualifiedSrcDocName(String fullName) {
            this.fullName = fullName;
            extension = Files.getFileExtension(fullName).toLowerCase();
        }
        public static QualifiedSrcDocName from(String qualifiedName) {
            String extension = Files.getFileExtension(qualifiedName);
            Preconditions.checkArgument(!Strings.isNullOrEmpty(extension), "expect a qualified document name (with extension)");
            return new QualifiedSrcDocName(qualifiedName);
        }
        public static QualifiedSrcDocName from(String unqualifiedName, String extension) {
            return new QualifiedSrcDocName(unqualifiedName + "." + extension);
        }

        public String getFullName() {
            return fullName;
        }

        public String getExtension() {
            return extension;
        }
    }

    /**
     * Represents document name without extension.
     */
    public static class UnqualifiedSrcDocName {
        private final String name;
        private UnqualifiedSrcDocName(String name) {
            this.name = name;
        }
        public static UnqualifiedSrcDocName from(String docName) {
            String extension = Files.getFileExtension(docName);
            Preconditions.checkArgument(Strings.isNullOrEmpty(extension), "expect an unqualified document name (without extension)");
            return new UnqualifiedSrcDocName(docName);
        }
        public QualifiedSrcDocName toQualifiedDocName(ProjectType projectType) {
            switch (projectType) {
                case Utf8Properties:
                case Properties:
                    return QualifiedSrcDocName.from(name, "properties");
                case Gettext:
                case Podir:
                    return QualifiedSrcDocName.from(name, "pot");
                case Xliff:
                case Xml:
                    return QualifiedSrcDocName.from(name, "xml");
                case File:
                    throw new IllegalArgumentException("You can not using unqualified document name in file type project");
            }
            throw new IllegalStateException("Can not convert unqualified document name for this project type: " + projectType);
        }
    }
}
