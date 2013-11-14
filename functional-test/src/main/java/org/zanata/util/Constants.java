/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.util;

import com.google.common.base.Objects;

import java.util.HashMap;
import java.util.Map;

public enum Constants {
    // constants used by page and workflow objects
    propFile("setup.properties"), zanataInstance("zanata.instance.url"),
    projectsLink("Projects"), webDriverType("webdriver.type"), chrome, firefox,
    htmlUnit, sampleProjects("zanata.sample.projects.basedir"), zanataApiKey(
            "zanata.apikey");

    public static final int FIFTY_SEC = 50000;
    private String value;

    private Constants(String value) {
        this.value = value;
    }

    private Constants() {
        this(null);
        value = name();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", name())
                .add("value", value).toString();
    }

    public String value() {
        return value;
    }

    public static Map<String, String> projectTypeOptions() {
        Map<String, String> projectTypeOptions = new HashMap<String, String>();
        projectTypeOptions.put("None", "-- No selection --");
        projectTypeOptions.put("File",
                "File. For plain text, LibreOffice, InDesign.");
        projectTypeOptions.put("Gettext",
                "Gettext. For gettext software strings.");
        projectTypeOptions.put("Podir", "Podir. For publican/docbook strings.");
        projectTypeOptions.put("Properties",
                "Properties. For Java properties files.");
        projectTypeOptions.put("Utf8Properties",
                "Utf8Properties. For UTF8-encoded Java properties.");
        projectTypeOptions.put("Xliff", "Xliff. For supported XLIFF files.");
        projectTypeOptions.put("Xml", "Xml. For XML from the Zanata REST API.");
        return projectTypeOptions;
    }
}
