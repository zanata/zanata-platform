/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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

package org.zanata.rest.dto.resource;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.zanata.common.HasContents;
import org.zanata.common.Namespaces;

/**
 * This class contains string contents for a single translatable message.
 * It maps between the JAXB/JSON representation of <code>content</code>/<code>contents</code>
 * (in separate elements, for backwards-compatibility) and the server model
 * (which represents <code>contents</code> as an <code>ArrayList</code> to support plural forms).
 * This class is the superclass for <code>TextFlow</code> and <code>TextFlowTarget</code>.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@XmlTransient
class TextContainer implements Serializable, HasContents
{
   private static final long serialVersionUID = 1L;

   /**
    * The (non-plural) string contents associated with this TextFlow/TextFlowTarget.
    * NB: If this message has plural forms, this field will be empty.
    * @see #contents
    */
   @XmlElement(name = "content", required = false, namespace = Namespaces.ZANATA_OLD)
   @JsonProperty("content")
   private String content;

   /**
    * The plural string contents associated with this TextFlow/TextFlowTarget.
    * NB: If this message has no plural forms, this field will be empty.
    * @see #content
    */
   @XmlElementWrapper(name = "contents", namespace = Namespaces.ZANATA_OLD)
   @XmlElement(name = "content", namespace = Namespaces.ZANATA_OLD)
   @JsonProperty("contents")
   private List<String> contents;

   /**
    * @deprecated As of release 1.6, replaced by {@link #getContents()}
    * @return
    */
   @Deprecated
   @XmlTransient
   public String getContent()
   {
      if (content == null)
         return "";
      return content;
   }

   /**
    * @deprecated As of release 1.6, replaced by {@link #setContents()}
    * @return
    */
   @Deprecated
   public void setContent(String content)
   {
      this.content = content;
      this.contents = null;
   }

   /**
    * Returns the string contents associated with this TextFlow/TextFlowTarget.
    * If there are multiple elements, they represent the different plural forms of this message.
    * If there is only one element, it is a non-plural message.
    * @return
    */
   @JsonIgnore
   @XmlTransient
   public List<String> getContents()
   {
      if (content != null)
      {
         return Arrays.asList(content);
      }
      else if (contents != null)
      {
         return contents;
      }
      else
      {
         return Collections.emptyList();
      }
   }

   @JsonIgnore
   public void setContents(String... contents)
   {
      setContents(Arrays.asList(contents));
   }

   @JsonIgnore
   public void setContents(List<String> contents)
   {
      if (contents == null)
      {
         this.content = null;
         this.contents = null;
         return;
      }

      switch (contents.size())
      {
      case 0:
         this.content = null;
         this.contents = null;
         break;
      case 1:
         this.content = contents.get(0);
         this.contents = null;
         break;
      default:
         this.content = null;
         this.contents = contents;
      }
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((content == null) ? 0 : content.hashCode());
      result = prime * result + ((contents == null) ? 0 : contents.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof TextContainer))
      {
         return false;
      }
      TextContainer other = (TextContainer) obj;
      if (content == null)
      {
         if (other.content != null)
         {
            return false;
         }
      }
      else if (!content.equals(other.content))
      {
         return false;
      }
      if (contents == null)
      {
         if (other.contents != null)
         {
            return false;
         }
      }
      else if (!contents.equals(other.contents))
      {
         return false;
      }
      return true;
   }

}
