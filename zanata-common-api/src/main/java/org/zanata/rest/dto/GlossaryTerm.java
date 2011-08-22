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
package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;
import org.hibernate.validator.NotNull;
import org.zanata.common.LocaleId;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/

@XmlRootElement(name = "Term")
@JsonPropertyOrder({ "Content", "Comments" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonWriteNullProperties(false)
public class GlossaryTerm implements Serializable
{
   @NotNull
   private LocaleId locale;

   private String content;

   private String sourcereference;

   private List<String> comments;

   @XmlAttribute(name = "locale")
   @XmlJavaTypeAdapter(type = LocaleId.class, value = LocaleIdAdapter.class)
   public LocaleId getLocale()
   {
      return locale;
   }

   public void setLocale(LocaleId locale)
   {
      this.locale = locale;
   }

   @XmlElement(name = "content", required = false)
   public String getContent()
   {
      return content;
   }

   public void setContent(String content)
   {
      this.content = content;
   }

   @XmlElement(name = "sourcereference", required = false)
   public String getSourcereference()
   {
      return sourcereference;
   }

   public void setSourcereference(String ref)
   {
      this.sourcereference = ref;
   }

   @XmlElement(name = "comment")
   public List<String> getComments()
   {
      if (comments == null)
      {
         comments = new ArrayList<String>();
      }
      return comments;
   }

   public void setComments(List<String> comments)
   {
      this.comments = comments;
   }

   @Override
   public String toString()
   {
      return DTOUtil.toXML(this);
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((locale == null) ? 0 : locale.hashCode());
      result = prime * result + ((content == null) ? 0 : content.hashCode());
      result = prime * result + ((sourcereference == null) ? 0 : sourcereference.hashCode());
      result = prime * result + ((comments == null) ? 0 : comments.hashCode());
      return result;
   }

}


 