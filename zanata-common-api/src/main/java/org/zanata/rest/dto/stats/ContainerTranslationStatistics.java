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
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.zanata.rest.dto.Link;
import org.zanata.rest.dto.Links;

/**
 * Generic Container for translation statistics.
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@XmlType(name = "containerTranslationStatisticsType", propOrder = {"refs", "stats", "detailedStats"})
@XmlRootElement(name = "containerStats")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder( { "id", "refs", "stats", "detailedStats" })
public class ContainerTranslationStatistics implements Serializable
{
   private static final long serialVersionUID = 1L;
   private String id;
   private Links refs;
   private List<ContainerTranslationStatistics> detailedStats;
   private List<TranslationStatistics> stats;


   public ContainerTranslationStatistics()
   {
   }

   /**
    * Identifier for the container (i.e. Project, Project Iteration, Document, etc).
    */
   @XmlAttribute
   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }

   /**
    * References to related elements (i.e. Reference to the container).
    */
   @XmlElementWrapper(name = "refs")
   @XmlElement(name = "containerRef")
   public Links getRefs()
   {
      return refs;
   }

   public void setRefs(Links refs)
   {
      this.refs = refs;
   }
   
   /**
    * Actual translation statistics.
    */
   @XmlElementWrapper(name = "stats")
   @XmlElement(name = "stat")
   public List<TranslationStatistics> getStats()
   {
      return stats;
   }

   /**
    * Detailed Statistics if so requested.
    */
   @XmlElementWrapper(name = "detailedStats")
   @XmlElement(name = "containerStats")
   public List<ContainerTranslationStatistics> getDetailedStats()
   {
      return detailedStats;
   }

   public void setStats(List<TranslationStatistics> stats)
   {
      this.stats = stats;
   }

   public void setDetailedStats(List<ContainerTranslationStatistics> detailedStats)
   {
      this.detailedStats = detailedStats;
   }

   public void addRef( Link newRef )
   {
      if( this.refs == null )
      {
         this.refs = new Links();
      }
      this.refs.add( newRef );
   }

   public void addDetailedStats( ContainerTranslationStatistics newDetailedStats )
   {
      if( this.detailedStats == null )
      {
         this.detailedStats = new ArrayList<ContainerTranslationStatistics>();
      }
      this.detailedStats.add(newDetailedStats);
   }

   /**
    * Finds a specific translation for a locale and detail level.
    *
    * @return The specified translation statistics element, or null if one
    *         cannot be found.
    */
   public TranslationStatistics getStats(String localeId, TranslationStatistics.StatUnit unit)
   {
      if (this.stats != null)
      {
         for (TranslationStatistics stat : this.stats)
         {
            if (stat.getLocale().equals(localeId) && stat.getUnit() == unit)
            {
               return stat;
            }
         }
      }
      return null;
   }

   public void addStats(TranslationStatistics newStats)
   {
      if (this.stats == null)
      {
         this.stats = new ArrayList<TranslationStatistics>();
      }
      this.stats.add(newStats);
   }

   @Override
   public String toString()
   {
      final StringBuilder sb = new StringBuilder("ContainerTranslationStatistics{");
      sb.append("id='").append(id).append('\'');
      sb.append(", refs=").append(refs);
      sb.append(", detailedStats=").append(detailedStats);
      sb.append('}');
      return sb.toString();
   }

   public void copyFrom(ContainerTranslationStatistics other)
   {
      this.stats = other.stats;
      this.detailedStats = other.detailedStats;
      this.refs = other.refs;
   }
}
