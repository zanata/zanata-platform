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
import com.google.common.base.Objects;

public class GetTransUnitsNavigation extends AbstractWorkspaceAction<GetTransUnitsNavigationResult>
{

   private static final long serialVersionUID = 1L;

   private Long id;
   private String phrase;
   private boolean isFuzzyState, isNewState, isApprovedState;

   @SuppressWarnings("unused")
   private GetTransUnitsNavigation()
   {
   }

   private GetTransUnitsNavigation(Long id, String phrase, boolean isNewState, boolean isFuzzyState, boolean isApprovedState)
   {
      this.id = id;
      this.phrase = phrase;
      this.isNewState = isNewState;
      this.isFuzzyState = isFuzzyState;
      this.isApprovedState = isApprovedState;
   }

   public static GetTransUnitsNavigation newAction(GetTransUnitActionContext context)
   {
      return new GetTransUnitsNavigation(context.getDocumentId().getId(), context.getFindMessage(), context.isFilterUntranslated(), context.isFilterNeedReview(), context.isFilterTranslated());
   }

   public Long getId()
   {
      return id;
   }

   public String getPhrase()
   {
      return this.phrase;
   }

   public boolean isFuzzyState()
   {
      return isFuzzyState;
   }

   public boolean isNewState()
   {
      return isNewState;
   }

   public boolean isApprovedState()
   {
      return isApprovedState;
   }

   @Override
   public String toString()
   {
      // @formatter:off
      return Objects.toStringHelper(this).
            add("id", id).
            add("phrase", phrase).
            add("isFuzzyState", isFuzzyState).
            add("isNewState", isNewState).
            add("isApprovedState", isApprovedState).
            toString();
      // @formatter:on
   }
}