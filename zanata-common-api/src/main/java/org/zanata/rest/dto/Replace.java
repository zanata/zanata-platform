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
package org.zanata.rest.dto;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.zanata.common.Namespaces;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
@XmlRootElement(name = "replace", namespace = Namespaces.ZANATA_OLD)
@XmlType(name = "replaceType")
@JsonPropertyOrder({ "sel", "value" })
public class Replace implements Serializable
{
   private static final long serialVersionUID = 1L;
   private String sel;
   private String value;

   public Replace()
   {
   }

   public Replace(String fieldName, String value)
   {
      this.sel = fieldName;
      this.value = value;
   }

   @XmlAttribute(name = "sel", required = true)
   public String getSel()
   {
      return sel;
   }

   public void setSel(String sel)
   {
      this.sel = sel;
   }

   @XmlValue
   public String getValue()
   {
      return value;
   }

   public void setValue(String value)
   {
      this.value = value;
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
      result = prime * result + ((sel == null) ? 0 : sel.hashCode());
      result = prime * result + ((value == null) ? 0 : value.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      Replace other = (Replace) obj;
      if (sel == null)
      {
         if (other.sel != null)
            return false;
      }
      else if (!sel.equals(other.sel))
         return false;
      if (value == null)
      {
         if (other.value != null)
            return false;
      }
      else if (!value.equals(other.value))
         return false;
      return true;
   }

}
