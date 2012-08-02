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
package org.zanata.rest.dto.stats;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import lombok.Getter;
import lombok.Setter;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@XmlType(name = "translationStatistics",
         propOrder = {"total", "untranslated", "needReview", "translated", "unit", "locale"})
@XmlRootElement(name = "translationStats")
public class TranslationStatistics implements Serializable
{

   public enum StatUnit
   {
      WORD,
      MESSAGE;
   }

   private long translated;
   private long needReview;
   private long untranslated;
   private long total;
   private StatUnit unit;
   private String locale;

   @XmlAttribute
   public long getTranslated()
   {
      return translated;
   }

   public void setTranslated(long translated)
   {
      this.translated = translated;
   }

   @XmlAttribute
   public long getNeedReview()
   {
      return needReview;
   }

   public void setNeedReview(long needReview)
   {
      this.needReview = needReview;
   }

   @XmlAttribute
   public long getUntranslated()
   {
      return untranslated;
   }

   public void setUntranslated(long untranslated)
   {
      this.untranslated = untranslated;
   }

   @XmlAttribute
   public long getTotal()
   {
      return total;
   }

   public void setTotal(long total)
   {
      this.total = total;
   }

   @XmlAttribute
   public StatUnit getUnit()
   {
      return unit;
   }

   public void setUnit(StatUnit unit)
   {
      this.unit = unit;
   }

   @XmlAttribute
   public String getLocale()
   {
      return locale;
   }

   public void setLocale(String locale)
   {
      this.locale = locale;
   }
}
