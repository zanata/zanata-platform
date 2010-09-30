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
package net.openl10n.flies.client.config;

import java.io.Serializable;
import java.net.URL;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Representation of the root node of Flies configuration
 * 
 * @author Sean Flanigan <sflaniga@redhat.com>
 * 
 */
@XmlType(name = "configType", propOrder = { "url", "project", "projectVersion", "locales" })
@XmlRootElement(name = "config")
public class FliesConfig implements Serializable
{
   private static final long serialVersionUID = 1L;
   private LocaleList locales = new LocaleList();
   private String project;
   private URL url;
   private String projectVersion;

   public FliesConfig()
   {
   }

   @XmlElementWrapper(name = "locales", required = false)
   @XmlElementRef(type = Locale.class, name = "locale")
   public LocaleList getLocales()
   {
      return locales;
   }

   public void setLocales(LocaleList locales)
   {
      this.locales = locales;
   }

   @XmlElement(name = "project")
   public String getProject()
   {
      return project;
   }

   public void setProject(String project)
   {
      this.project = project;
   }

   @XmlElement(name = "url")
   public URL getUrl()
   {
      return url;
   }

   public void setUrl(URL url)
   {
      this.url = url;
   }

   @XmlElement(name = "project-version")
   public String getProjectVersion()
   {
      return projectVersion;
   }

   public void setProjectVersion(String version)
   {
      this.projectVersion = version;
   }

}
