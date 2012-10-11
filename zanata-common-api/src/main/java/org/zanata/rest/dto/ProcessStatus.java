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
package org.zanata.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Generic type to represent the status of a process.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@XmlRootElement(name = "processStatus")
@XmlType(name = "processStatusType")
public class ProcessStatus
{
   private String url;

   private int percentageComplete;

   private boolean inProgress;

   private String message;

   private boolean error;


   @XmlElement(required = true)
   public String getUrl()
   {
      return url;
   }

   public void setUrl(String url)
   {
      this.url = url;
   }

   @XmlElement(required = true)
   public int getPercentageComplete()
   {
      return percentageComplete;
   }

   public void setPercentageComplete(int percentageComplete)
   {
      this.percentageComplete = percentageComplete;
   }

   @XmlElement(required = true)
   public boolean isInProgress()
   {
      return inProgress;
   }

   public void setInProgress(boolean inProgress)
   {
      this.inProgress = inProgress;
   }

   @XmlElement
   public String getMessage()
   {
      return message;
   }

   public void setMessage(String message)
   {
      this.message = message;
   }

   @XmlElement
   public boolean isError()
   {
      return error;
   }

   public void setError(boolean error)
   {
      this.error = error;
   }
}
