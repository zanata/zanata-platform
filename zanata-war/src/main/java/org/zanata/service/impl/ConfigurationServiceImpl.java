/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.join;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.ApplicationConfiguration;
import org.zanata.common.Namespaces;
import org.zanata.common.ProjectType;
import org.zanata.dao.ProjectIterationDAO;
import org.zanata.model.HLocale;
import org.zanata.service.ConfigurationService;
import org.zanata.service.LocaleService;

@Named("configurationServiceImpl")
@javax.enterprise.context.Dependent
public class ConfigurationServiceImpl implements ConfigurationService {
    private static final String FILE_NAME = "zanata.xml";

    private static final String PROJECT_TYPE_OFFLINE_PO = "offlinepo";

    @Inject
    private LocaleService localeServiceImpl;

    @Inject
    private ProjectIterationDAO projectIterationDAO;

    @Inject
    private ApplicationConfiguration applicationConfiguration;

    @Override
    public String getGeneralConfig(String projectSlug, String versionSlug) {
        return new ConfigBuilder(projectSlug, versionSlug).getConfig();
    }

    @Override
    public String getSingleLocaleConfig(String projectSlug, String versionSlug,
            HLocale locale) {
        return new SingleLocaleConfigBuilder(projectSlug, versionSlug, locale)
                .getConfig();
    }

    @Override
    public String getConfigForOfflineTranslation(String projectSlug,
            String versionSlug, HLocale locale) {
        return new OfflineTranslationConfigBuilder(projectSlug, versionSlug,
                locale).getConfig();
    }

    @Override
    public String getConfigurationFileName() {
        return FILE_NAME;
    }

    private class ConfigBuilder {

        private final String projectSlug;
        private final String versionSlug;

        private StringBuilder doc;

        public ConfigBuilder(String projectSlug, String versionSlug) {
            this.projectSlug = projectSlug;
            this.versionSlug = versionSlug;
        }

        public String getConfig() {
            doc =
                    new StringBuilder(
                            "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                                    + "<config xmlns=\""
                                    + Namespaces.ZANATA_CONFIG + "\">\n")
                            .append(indent(tag("url",
                                    applicationConfiguration.getServerPath()
                                            + "/")))
                            .append("\n")
                            .append(indent(tag("project", projectSlug)))
                            .append("\n")
                            .append(indent(tag("project-version", versionSlug)))
                            .append("\n")
                            .append(makeProjectTypeSection(getProjectType()))
                            .append("\n");
            doc.append("\n").append("</config>\n");
            return doc.toString();
        }

        protected String indent(String line) {
            return "  " + line;
        }

        protected String tag(String tagName, String content) {
            return "<" + tagName + ">" + content + "</" + tagName + ">";
        }

        protected String makeProjectTypeSection(ProjectType projectType) {
            if (projectType != null) {
                return indent(tag("project-type", projectType.toString()
                        .toLowerCase()));
            } else {
                return indent(comment(tag("project-type",
                        join(ProjectType.values(), "|").toLowerCase())));
            }
        }

        private String comment(String content) {
            return "<!-- " + content + " -->";
        }

        private ProjectType getProjectType() {
            return projectIterationDAO.getBySlug(projectSlug, versionSlug)
                    .getProjectType();
        }

    }

    private class SingleLocaleConfigBuilder extends ConfigBuilder {

        private final HLocale locale;

        public SingleLocaleConfigBuilder(String projectSlug,
                String versionSlug, HLocale locale) {
            super(projectSlug, versionSlug);
            this.locale = locale;
        }
    }

    private class OfflineTranslationConfigBuilder extends
            SingleLocaleConfigBuilder {

        public OfflineTranslationConfigBuilder(String projectSlug,
                String versionSlug, HLocale locale) {
            super(projectSlug, versionSlug, locale);
        }

        @Override
        protected String makeProjectTypeSection(ProjectType projectType) {
            if (projectType == ProjectType.Podir) {
                return super.makeProjectTypeSection(projectType);
            }

            if (projectType == ProjectType.Gettext) {
                return "  <!-- NB project-type set to 'podir' to allow offline translations to be\n"
                        + "       uploaded, but original was 'gettext' -->\n"
                        + indent(tag("project-type", ProjectType.Podir
                                .toString().toLowerCase()));
            }

            return "  <!-- NB project-type set to 'offlinepo' to allow offline po translation\n"
                    + "       from non-po documents, project-type on server is '"
                    + String.valueOf(projectType).toLowerCase()
                    + "' -->\n"
                    + indent(tag("project-type", PROJECT_TYPE_OFFLINE_PO));
        }

    }

}
