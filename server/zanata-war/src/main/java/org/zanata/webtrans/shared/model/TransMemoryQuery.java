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

package org.zanata.webtrans.shared.model;

import java.util.ArrayList;

import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class TransMemoryQuery implements IsSerializable
{
   private SearchType searchType;
   private ArrayList<String> queries;

   @SuppressWarnings("unused")
   private TransMemoryQuery()
   {
   }

   public TransMemoryQuery(String query, SearchType searchType)
   {
      this.searchType = searchType;
      this.queries = new ArrayList<String>(1);
      this.queries.add(query);
      if (searchType == SearchType.FUZZY_PLURAL)
      {
         throw new RuntimeException("Can't use FUZZY_PLURAL SearchType with a single query string");
      }
   }

   public TransMemoryQuery(ArrayList<String> queries, SearchType searchType)
   {
      this.searchType = searchType;
      this.queries = queries;
      if (searchType != SearchType.FUZZY_PLURAL)
      {
         throw new RuntimeException("SearchType must be FUZZY_PLURAL when using multiple query strings");
      }
   }

   public ArrayList<String> getQueries()
   {
      return queries;
   }

   public SearchType getSearchType()
   {
      return searchType;
   }

   @Override
   public String toString()
   {
      return "TransMemoryQuery [searchType=" + searchType + ", queries=" + queries + "]";
   }

   @Override
   public int hashCode()
   {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((queries == null) ? 0 : queries.hashCode());
      result = prime * result + ((searchType == null) ? 0 : searchType.hashCode());
      return result;
   }

   @Override
   public boolean equals(Object obj)
   {
      if (this == obj)
      {
         return true;
      }
      if (obj == null)
      {
         return false;
      }
      if (!(obj instanceof TransMemoryQuery))
      {
         return false;
      }
      TransMemoryQuery other = (TransMemoryQuery) obj;
      if (queries == null)
      {
         if (other.queries != null)
         {
            return false;
         }
      }
      else if (!queries.equals(other.queries))
      {
         return false;
      }
      if (searchType != other.searchType)
      {
         return false;
      }
      return true;
   }

}
