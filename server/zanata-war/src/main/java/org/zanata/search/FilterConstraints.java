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
package org.zanata.search;

//TODO could make a hierarchy of filter constraints, which could be consumed
//by a hierarchy of filters. For the moment there aren't enough uses to
//justify this. May want to add document(someDocument) to these constraints
//so that only one search method is needed on the interface.

/**
 * Specifies a set of constraints to be applied by a filter.
 * 
 * @author David Mason, damason@redhat.com
 */
public class FilterConstraints
{
   private String searchString;

   private boolean searchInSource;
   private boolean searchInTarget;

   private boolean includeNew;
   private boolean includeFuzzy;
   private boolean includeApproved;

   private FilterConstraints(String searchString, boolean searchInSource, boolean searchInTarget, boolean includeNew, boolean includeFuzzy, boolean includeApproved)
   {
      this.searchInSource = searchInSource;
      this.searchInTarget = searchInTarget;
      this.includeNew = includeNew;
      this.includeFuzzy = includeFuzzy;
      this.includeApproved = includeApproved;
      this.searchString = searchString;
   }

   /**
    * Create a chainable filter constraints that specifies a search in both
    * source and target, including all content states.
    * 
    * Use chainable methods to alter these constraints
    * 
    * @param searchString the string to search for in source and target
    * @return the new {@link FilterConstraints}
    */
   public static FilterConstraints filterBy(String searchString)
   {
      return new FilterConstraints(searchString, true, true, true, true, true);
   }

   public static FilterConstraints keepAll()
   {
      return new FilterConstraints("", true, true, true, true, true);
   }
   
   public static FilterConstraints keepNone()
   {
      return new FilterConstraints("", false, false, false, false, false);
   }

   //chainable setters

   public FilterConstraints filterTarget()
   {
      searchInTarget = true;
      return this;
   }

   public FilterConstraints ignoreTarget()
   {
      searchInTarget = false;
      return this;
   }

   public FilterConstraints filterSource()
   {
      searchInSource = true;
      return this;
   }

   public FilterConstraints ignoreSource()
   {
      searchInSource = false;
      return this;
   }

   public FilterConstraints includeNew()
   {
      includeNew = true;
      return this;
   }

   public FilterConstraints excludeNew()
   {
      includeNew = false;
      return this;
   }

   public FilterConstraints includeFuzzy()
   {
      includeFuzzy = true;
      return this;
   }

   public FilterConstraints excludeFuzzy()
   {
      includeFuzzy = false;
      return this;
   }

   public FilterConstraints includeApproved()
   {
      includeApproved = true;
      return this;
   }

   public FilterConstraints excludeApproved()
   {
      includeApproved = false;
      return this;
   }


   //getters

   public String getSearchString()
   {
      return searchString;
   }

   public boolean isSearchInSource()
   {
      return searchInSource;
   }

   public boolean isSearchInTarget()
   {
      return searchInTarget;
   }

   public boolean isIncludeNew()
   {
      return includeNew;
   }

   public boolean isIncludeFuzzy()
   {
      return includeFuzzy;
   }

   public boolean isIncludeApproved()
   {
      return includeApproved;
   }

}
