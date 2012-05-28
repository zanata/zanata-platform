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

import java.util.List;
import java.util.Map;
import java.util.Set;

import net.customware.gwt.dispatch.shared.Result;

import org.zanata.webtrans.shared.model.TransUnit;

/**
 * @see GetProjectTransUnitLists
 * @author David Mason, damason@redhat.com
 *
 */
public class GetProjectTransUnitListsResult implements Result
{
   private static final long serialVersionUID = 1L;

   private GetProjectTransUnitLists searchAction;

   /**
    * Document id, matching TransUnits
    */
   private Map<Long, List<TransUnit>> documents;

   /**
    * Document id, full document name+path
    */
   private Map<Long, String> docPaths;

   @SuppressWarnings("unused")
   private GetProjectTransUnitListsResult()
   {
   }

   public GetProjectTransUnitListsResult(GetProjectTransUnitLists searchAction, Map<Long, String> docPaths, Map<Long, List<TransUnit>> documents)
   {
      this.searchAction = searchAction;
      this.docPaths = docPaths;
      this.documents = documents;
   }

   public GetProjectTransUnitLists getSearchAction()
   {
      return searchAction;
   }

   public List<TransUnit> getUnits(Long documentId)
   {
      return documents.get(documentId);
   }

   public String getDocPath(Long docId)
   {
      return docPaths.get(docId);
   }

   public Set<Long> getDocumentIds()
   {
      return docPaths.keySet();
   }

}
