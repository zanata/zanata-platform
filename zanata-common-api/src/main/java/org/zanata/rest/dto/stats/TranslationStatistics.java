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
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Translation statistics. Contains actual numbers and other information about the state of
 * translation.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@XmlType(name = "translationStatistics",
         propOrder = {"total", "untranslated", "needReview", "translated", "unit", "locale"})
@XmlRootElement(name = "translationStats")
@JsonIgnoreProperties(value = {"percentTranslated", "percentNeedReview", "percentUntranslated"}, ignoreUnknown = true)
@JsonPropertyOrder( { "total", "untranslated", "needReview", "translated", "unit", "locale" })
@JsonWriteNullProperties(false)
public class TranslationStatistics implements Serializable
{

   public enum StatUnit
   {
      /** Statistics are measured in words. */
      WORD,
      /** Statistics are measured in messages (i.e. entries, text flows) */
      MESSAGE;
   }

   private long translated;
   private long needReview;
   private long untranslated;
   private long total;
   private StatUnit unit;
   private String locale;

   /**
    * Number of translated elements.
    */
   @XmlAttribute
   public long getTranslated()
   {
      return translated;
   }

   public void setTranslated(long translated)
   {
      this.translated = translated;
   }

   /**
    * Number of elements that need review (i.e. Fuzzy).
    */
   @XmlAttribute
   public long getNeedReview()
   {
      return needReview;
   }

   public void setNeedReview(long needReview)
   {
      this.needReview = needReview;
   }

   /**
    * Number of untranslated elements.
    */
   @XmlAttribute
   public long getUntranslated()
   {
      return untranslated;
   }

   public void setUntranslated(long untranslated)
   {
      this.untranslated = untranslated;
   }

   /**
    * Total number of elements.
    */
   @XmlAttribute
   public long getTotal()
   {
      return total;
   }

   public void setTotal(long total)
   {
      this.total = total;
   }

   /**
    * Element unit being used to measure the translation counts.
    */
   @XmlAttribute
   public StatUnit getUnit()
   {
      return unit;
   }

   public void setUnit(StatUnit unit)
   {
      this.unit = unit;
   }

   /**
    * Locale for the translation statistics.
    */
   @XmlAttribute
   public String getLocale()
   {
      return locale;
   }

   public void setLocale(String locale)
   {
      this.locale = locale;
   }

   @XmlTransient
   public int getPercentTranslated()
   {
      long total = getTotal();
      if (total <= 0)
      {
         return 0;
      }
      else
      {
         double per = 100 * getTranslated() / total;
         return (int) Math.ceil(per);
      }
   }

   @XmlTransient
   public int getPercentNeedReview()
   {
      long total = getTotal();
      if (total <= 0)
      {
         return 0;
      }
      else
      {
         double per = 100 * getNeedReview() / total;
         return (int) Math.ceil(per);
      }
   }

   @XmlTransient
   public int getPercentUntranslated()
   {
      long total = getTotal();
      if (total <= 0)
      {
         return 0;
      }
      else
      {
         double per = 100 * getUntranslated() / total;
         return (int) Math.ceil(per);
      }
   }

}
