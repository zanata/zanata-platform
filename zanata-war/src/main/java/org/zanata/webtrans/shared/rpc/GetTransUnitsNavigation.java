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
package org.zanata.webtrans.shared.rpc;


import org.zanata.webtrans.client.service.GetTransUnitActionContext;
import org.zanata.webtrans.shared.model.ContentStateGroup;

import com.google.common.base.Objects;

public class GetTransUnitsNavigation
{
   private Long id;
   private String phrase;

   private ContentStateGroup activeStates;

   @SuppressWarnings("unused")
   private GetTransUnitsNavigation()
   {
   }

   public GetTransUnitsNavigation(Long id, String phrase, ContentStateGroup activeStates)
   {
      this.id = id;
      this.phrase = phrase;
      this.activeStates = activeStates;
   }

   public GetTransUnitsNavigation(GetTransUnitActionContext context)
   {
      this(context.getDocument().getId().getId(),
           context.getFindMessage(),
           ContentStateGroup.builder()
              .includeNew(context.isFilterUntranslated())
              .includeFuzzy(context.isFilterNeedReview())
              .includeTranslated(context.isFilterTranslated())
              .includeApproved(context.isFilterApproved())
              .includeRejected(context.isFilterRejected())
              .build());
   }

   public static GetTransUnitsNavigation newAction(GetTransUnitActionContext context)
   {
      return new GetTransUnitsNavigation(context);
   }

   public Long getId()
   {
      return id;
   }

   public String getPhrase()
   {
      return this.phrase;
   }

   public ContentStateGroup getActiveStates()
   {
      return activeStates;
   }

   @Override
   public String toString()
   {
      // @formatter:off
      return Objects.toStringHelper(this).
            add("id", id).
            add("phrase", phrase).
            add("activeStates", activeStates).
            toString();
      // @formatter:on
   }
}