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
package org.zanata.service;

import java.util.List;

import org.zanata.model.HTextFlow;
import org.zanata.search.FilterConstraints;
import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.WorkspaceId;



/**
 * Provides methods to retrieve filtered lists of text flows
 * 
 * @author David Mason, damason@redhat.com
 */
public interface TextFlowSearchService
{

   // TODO webtrans.shared.model objects encapsulate some checks and are concise
   // for these methods, but are less general so using strings may be more
   // appropriate.
   //Changes signatures when a final decision is made on this

   /**
    * Find matching textflows within a specified document
    * 
    * @param workspace
    * @param doc
    * @param searchTerm to filter content by, empty to return all textflows in the document
    * @param constraints determine fields that will be checked for searchTerm
    * @return
    */
   List<HTextFlow> findTextFlows(WorkspaceId workspace, DocumentId doc, FilterConstraints constraints);

   /**
    * Find matching textflows within a project iteration for a locale
    * 
    * @param workspace
    * @param searchTerm to filter content by, empty to return all textflows in the project version
    * @param constraints determine fields that will be checked for searchTerm
    * @return
    */
   List<HTextFlow> findTextFlows(WorkspaceId workspace, FilterConstraints constraints);

}
