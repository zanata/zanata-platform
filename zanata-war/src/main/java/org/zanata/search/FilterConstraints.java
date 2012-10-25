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

import java.util.List;

import org.zanata.common.ContentState;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

/**
 * Specifies a set of constraints to be applied by a filter.
 * 
 * @author David Mason, damason@redhat.com
 */
public class FilterConstraints
{
   private String searchString;

   private boolean isCaseSensitive;

   private boolean searchInSource;
   private boolean searchInTarget;

   private boolean includeNew;
   private boolean includeFuzzy;
   private boolean includeApproved;

   private FilterConstraints(String searchString, boolean caseSensitive, boolean searchInSource, boolean searchInTarget, boolean includeNew, boolean includeFuzzy, boolean includeApproved)
   {
      this.searchString = searchString;
      this.isCaseSensitive = caseSensitive;
      this.searchInSource = searchInSource;
      this.searchInTarget = searchInTarget;
      this.includeNew = includeNew;
      this.includeFuzzy = includeFuzzy;
      this.includeApproved = includeApproved;
   }

   /**
    * Create a chainable filter constraints that specifies a case-insensitive
    * search in both source and target, including all content states.
    * 
    * Use chainable methods to alter these constraints
    * 
    * @param searchString the string to search for in source and target
    * @return the new {@link FilterConstraints}
    */
   public static FilterConstraints filterBy(String searchString)
   {
      return new FilterConstraints(searchString, false, true, true, true, true, true);
   }

   public static FilterConstraints keepAll()
   {
      return new FilterConstraints("", false, true, true, true, true, true);
   }
   
   public static FilterConstraints keepNone()
   {
      return new FilterConstraints("", false, false, false, false, false, false);
   }


   //chainable setters
   // TODO use builder instead

   /**
    * Specify that search string does not require the same case as content to be considered a match
    * 
    * @return this object for chaining
    */
   public FilterConstraints ignoreCase()
   {
      isCaseSensitive = false;
      return this;
   }

   /**
    * Specify that search string must have the same case as content to be considered a match
    * 
    * @return this object for chaining
    */
   public FilterConstraints matchCase()
   {
      isCaseSensitive = true;
      return this;
   }

   /**
    * Specify search case-sensitivity
    * 
    * @param caseSensitive true if the search string must have the same case as content to be considered a match
    * @return this object for chaining
    */
   public FilterConstraints caseSensitive(boolean caseSensitive)
   {
      this.isCaseSensitive = caseSensitive;
      return this;
   }

   /**
    * Return text flows that match the search string in their target content
    * 
    * @return this object for chaining
    */
   public FilterConstraints filterTarget()
   {
      searchInTarget = true;
      return this;
   }

   /**
    * Do not search for the search string in the target
    * 
    * @return this object for chaining
    */
   public FilterConstraints ignoreTarget()
   {
      searchInTarget = false;
      return this;
   }

   /**
    * Return text flows that match the search string in their source content
    * 
    * @return this object for chaining
    */
   public FilterConstraints filterSource()
   {
      searchInSource = true;
      return this;
   }

   /**
    * Do not search for the search string in the source
    * 
    * @return this object for chaining
    */
   public FilterConstraints ignoreSource()
   {
      searchInSource = false;
      return this;
   }

   /**
    * Allow return of matching text flows with New targets
    * 
    * @return this object for chaining
    */
   public FilterConstraints includeNew()
   {
      includeNew = true;
      return this;
   }

   /**
    * Do not return any text flows with New targets
    * 
    * @return this object for chaining
    */
   public FilterConstraints excludeNew()
   {
      includeNew = false;
      return this;
   }

   /**
    * Allow return of matching text flows with Fuzzy targets
    * 
    * @return this object for chaining
    */
   public FilterConstraints includeFuzzy()
   {
      includeFuzzy = true;
      return this;
   }

   /**
    * Do not return any text flows with Fuzzy targets
    * 
    * @return this object for chaining
    */
   public FilterConstraints excludeFuzzy()
   {
      includeFuzzy = false;
      return this;
   }

   /**
    * Allow return of matching text flows with Approved targets
    * 
    * @return this object for chaining
    */
   public FilterConstraints includeApproved()
   {
      includeApproved = true;
      return this;
   }

   /**
    * Do not return any text flows with Approved targets
    * 
    * @return this object for chaining
    */
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

   public boolean isCaseSensitive()
   {
      return this.isCaseSensitive;
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

   public FilterConstraints filterByStatus(boolean newState, boolean fuzzyState, boolean approvedState)
   {
      if (approvedState == fuzzyState && approvedState == newState)
      {
         return new FilterConstraints(searchString, isCaseSensitive, isSearchInSource(), isSearchInTarget(), true, true, true);
      }
      return new FilterConstraints(searchString, isCaseSensitive, isSearchInSource(), isSearchInTarget(), newState, fuzzyState, approvedState);
   }

   public boolean isIncludeAllState()
   {
      return includeApproved && includeFuzzy && includeNew;
   }

   public List<ContentState> getContentStateAsList()
   {
      List<ContentState> result = Lists.newArrayList();
      if (includeApproved)
      {
         result.add(ContentState.Approved);
      }
      if (includeFuzzy)
      {
         result.add(ContentState.NeedReview);
      }
      if (includeNew)
      {
         result.add(ContentState.New);
      }
      return result;
   }

   @Override
   public String toString()
   {
      // @formatter:off
      return Objects.toStringHelper(this).
            add("searchString", searchString).
            add("isCaseSensitive", isCaseSensitive).
            add("searchInSource", searchInSource).
            add("searchInTarget", searchInTarget).
            add("includeNew", includeNew).
            add("includeFuzzy", includeFuzzy).
            add("includeApproved", includeApproved).
            toString();
      // @formatter:on
   }
}
