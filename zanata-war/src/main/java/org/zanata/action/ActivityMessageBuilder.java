/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.action;

import org.zanata.common.ActivityType;
import org.zanata.model.Activity;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ActivityMessageBuilder
{
   public final static String getHtmlMessage(Activity activity)
   {
      StringBuilder sb = new StringBuilder();
      if(activity.getActionType() == ActivityType.UPDATE_TRANSLATION)
      {
         
      } 
      else if(activity.getActionType() == ActivityType.REVIEWED_TRANSLATION)
      {
//      Reviewed <strong>537 words</strong> to Japanese in <a href="#">Zanata v3.0</a>, finishing on <a href="#">string #867</a>
         sb.append("Reviewed ");
      }
      else if(activity.getActionType() == ActivityType.UPLOAD_SOURCE_DOCUMENT)
      {
         
      }
      else if(activity.getActionType() == ActivityType.UPLOAD_TRANSLATION_DOCUMENT)
      {
         
      }
      return sb.toString();
   }
}
