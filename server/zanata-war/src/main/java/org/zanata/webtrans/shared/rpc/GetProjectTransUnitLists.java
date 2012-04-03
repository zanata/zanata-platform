/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
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


/**
 * Get all text flows from a project that contain the given search string,
 * grouped by document.
 * 
 * @author David Mason, damason@redhat.com
 * 
 */
public class GetProjectTransUnitLists extends AbstractWorkspaceAction<GetProjectTransUnitListsResult>
{
   private static final long serialVersionUID = 1L;
   private String searchString;
   private boolean caseSensitive;

   @SuppressWarnings("unused")
   private GetProjectTransUnitLists()
   {
   }

   public GetProjectTransUnitLists(String searchString, boolean caseSensitive)
   {
      this.searchString = searchString;
      this.caseSensitive = caseSensitive;
   }

   public String getSearchString()
   {
      return this.searchString;
   }

   public boolean isCaseSensitive()
   {
      return this.caseSensitive;
   }
}
