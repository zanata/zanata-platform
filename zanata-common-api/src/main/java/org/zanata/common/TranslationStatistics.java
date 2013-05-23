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
package org.zanata.common;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.annotate.JsonWriteNullProperties;

/**
 * Translation statistics. Contains actual numbers and other information about
 * the state of translation.
 * 
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@XmlType(name = "translationStatistics", propOrder = { "total", "untranslated", "needReview", "translated", "approved", "rejected", "readyForReview", "fuzzy", "unit", "locale", "lastTranslated" })
@XmlRootElement(name = "translationStats")
@JsonIgnoreProperties(value = { "percentTranslated", "percentNeedReview", "percentUntranslated" }, ignoreUnknown = true)
@JsonPropertyOrder({ "total", "untranslated", "needReview", "translated", "approved", "rejected", "readyForReview", "fuzzy", "unit", "locale", "lastTranslated" })
@JsonWriteNullProperties(false)
public class TranslationStatistics implements Serializable
{
   private static final long serialVersionUID = 1L;
   private StatUnit unit;
   private AbstractTranslationCount translationCount;
   private String locale;
   private double remainingHours;
   private String lastTranslated;

   /**
    * This is for marshalling purpose only.
    */
   public TranslationStatistics()
   {
      this(StatUnit.MESSAGE);
   }

   public TranslationStatistics(StatUnit statUnit)
   {
      unit = statUnit;
      if (unit == StatUnit.MESSAGE)
      {
         translationCount = new TransUnitWords(0, 0, 0);
      }
      else
      {
         translationCount = new TransUnitCount(0, 0, 0);
      }
   }

   public TranslationStatistics(TransUnitCount unitCount, String locale)
   {
      translationCount = unitCount;
      this.unit = StatUnit.MESSAGE;
      this.locale = locale;
   }

   public TranslationStatistics(TransUnitWords wordCount, String locale)
   {
      translationCount = wordCount;
      this.unit = StatUnit.WORD;
      this.locale = locale;

      double untransHours = wordCount.getUntranslated() / 250.0;
      double fuzzyHours = wordCount.getNeedReview() / 500.0;
      double translatedHours = wordCount.getTranslated() / 500.0;
      remainingHours = untransHours + fuzzyHours + translatedHours;
   }

   /**
    * Number of untranslated elements.
    */
   @XmlAttribute
   public long getUntranslated()
   {
      return translationCount.getUntranslated();
   }

   public void setUntranslated(long untranslated)
   {
      translationCount.set(ContentState.New, (int) untranslated);
   }

   /**
    * Number of elements that need review (i.e. Fuzzy or Rejected).
    */
   public long getDraft()
   {
      return translationCount.getNeedReview() + translationCount.getRejected();
   }

   /**
    * This is for REST backward compatibility.
    * @return Number of elements that need review (i.e. Fuzzy or Rejected)
    */
   @XmlAttribute
   protected long getNeedReview()
   {
      return getDraft();
   }

   /**
    * This will only return fuzzy translation.
    * @return
    */
   @XmlAttribute
   public long getFuzzy()
   {
      return translationCount.getNeedReview();
   }
   
   /**
    * This is for REST backward compatibility.
    * @return Number of translated and approved elements.
    */
   @XmlAttribute
   protected long getTranslated()
   {
      return translationCount.getTranslated() + translationCount.getApproved();
   }

   /**
    * @return number of translated but not yet approved elements.
    */
   @XmlAttribute
   public long getReadyForReview()
   {
      return translationCount.getTranslated();
   }
  
   /**
   * @return Number of approved elements.
   */
   @XmlAttribute
   public long getApproved()
   {
      return translationCount.getApproved();
   }

   @XmlAttribute
   public long getRejected()
   {
      return translationCount.getRejected();
   }

   /**
    * Total number of elements.
    */
   @XmlAttribute
   public long getTotal()
   {
      return translationCount.getTotal();
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

   @XmlAttribute
   public String getLastTranslated()
   {
      return lastTranslated;
   }

   public void setLastTranslated(String lastTranslated)
   {
      this.lastTranslated = lastTranslated;
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
         double per = 100 * getApproved() / total;
         return (int) Math.ceil(per);
      }
   }

   @XmlTransient
   public int getPercentDraft()
   {
      long total = getTotal();
      if (total <= 0)
      {
         return 0;
      }
      else
      {
         double per = 100 * getDraft() / total;
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

   public void setRemainingHours(double remainingHours)
   {
      this.remainingHours = remainingHours;
   }

   @XmlTransient
   public double getRemainingHours()
   {
      return remainingHours;
   }

   public void add(TranslationStatistics other)
   {
      translationCount.add(other.translationCount);
   }

   public void increment(ContentState state, long count)
   {
      translationCount.increment(state, (int) count);
   }

   public void decrement(ContentState state, long count)
   {
      translationCount.decrement(state, (int) count);
   }

   public enum StatUnit
   {
      /** Statistics are measured in words. */
      WORD,
      /** Statistics are measured in messages (i.e. entries, text flows) */
      MESSAGE;

   }
}
