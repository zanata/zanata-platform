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
package org.zanata.client.config;

import java.io.File;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;

/**
 * Representation of the root node of a project configuration
 *
 * @author Sean Flanigan <sflaniga@redhat.com>
 *
 */
@XmlType(name = "configType", propOrder = { "url", "project", "projectVersion",
        "projectType", "srcDir", "transDir", "includes", "excludes", "hooks", "locales" })
@XmlRootElement(name = "config")
public class ZanataConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    private LocaleList locales = new LocaleList();
    private String project;
    private URL url;
    private String projectType;
    private String projectVersion;
    // default to current directory
    private String srcDir = ".";
    private String transDir = ".";
    private String includes;
    private String excludes;
    private List<CommandHook> hooks = new ArrayList<CommandHook>();
    private transient Splitter splitter = Splitter.on(",").omitEmptyStrings()
            .trimResults();

    public ZanataConfig() {
    }

    @XmlElementWrapper(name = "locales", required = false)
    @XmlElementRef(type = LocaleMapping.class, name = "locale")
    public LocaleList getLocales() {
        return locales;
    }

    public void setLocales(LocaleList locales) {
        this.locales = locales;
    }

    @XmlElementWrapper(name = "hooks", required = false)
    @XmlElement(name = "hook")
    public List<CommandHook> getHooks() {
        return hooks;
    }

    public void setHooks(List<CommandHook> commandHooks) {
        this.hooks = commandHooks;
    }

    @XmlElement(name = "project")
    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    @XmlElement(name = "url")
    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    @XmlElement(name = "project-type")
    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String type) {
        this.projectType = type;
    }

    @XmlElement(name = "project-version")
    public String getProjectVersion() {
        return projectVersion;
    }

    public void setProjectVersion(String version) {
        this.projectVersion = version;
    }

    @XmlElement(name = "src-dir")
    public String getSrcDir() {
        return srcDir;
    }

    public void setSrcDir(String srcDir) {
        this.srcDir = srcDir;
    }

    @XmlElement(name = "trans-dir")
    public String getTransDir() {
        return transDir;
    }

    public void setTransDir(String transDir) {
        this.transDir = transDir;
    }

    @XmlElement
    public String getIncludes() {
        return includes;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    @XmlElement
    public String getExcludes() {
        return excludes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    @XmlTransient
    public File getSrcDirAsFile() {
        return new File(srcDir);
    }

    @XmlTransient
    public File getTransDirAsFile() {
        return new File(transDir);
    }

    @XmlTransient
    public ImmutableList<String> getIncludesAsList() {
        if (includes != null) {
            return ImmutableList.copyOf(splitter.split(includes));
        }
        return ImmutableList.of();
    }

    @XmlTransient
    public ImmutableList<String> getExcludesAsList() {
        if (excludes != null) {
            return ImmutableList.copyOf(splitter.split(excludes));
        }
        return ImmutableList.of();
    }
}
