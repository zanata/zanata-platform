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
package org.zanata.service;

import org.zanata.model.HDocument;
import org.zanata.process.ProcessHandle;
import org.zanata.rest.dto.resource.Resource;

import java.util.Set;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public interface DocumentService
{
   /**
    * Creates or Updates a document.
    *
    * @param projectSlug The document's project id.
    * @param iterationSlug The document's project iteration id.
    * @param sourceDoc The document to save. (If the document's name matches a docId already stored, it will be overwritten)
    * @param extensions Document extensions to save.
    * @param copyTrans Whether to copy translations from other projects or not. A true value does not guarantee that
    *                  this will happen, it is only a suggestion.
    * @param lock If true, no other document save will be allowed for the same document until this invocation has
    *             finished.
    * @return The created / updated document
    */
   public HDocument saveDocument( String projectSlug, String iterationSlug, Resource sourceDoc,
                                  Set<String> extensions, boolean copyTrans, boolean lock );

   /**
    * Creates or Updates a document.
    *
    * @param projectSlug The document's project id.
    * @param iterationSlug The document's project iteration id.
    * @param sourceDoc The document to save. (If the document's name matches a docId already stored, it will be overwritten)
    * @param extensions Document extensions to save.
    * @param copyTrans Whether to copy translations from other projects or not. A true value does not guarantee that
    *                  this will happen, it is only a suggestion.
    * @return The created / updated document
    */
   public HDocument saveDocument( String projectSlug, String iterationSlug, Resource sourceDoc,
                                  Set<String> extensions, boolean copyTrans);

   /**
    * Makes a document obsolete.
    *
    * @param document The document to make obsolete.
    */
   public void makeObsolete( HDocument document );
}
