/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
package org.zanata.service;

import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.process.CopyTransProcessHandle;

public interface CopyTransService
{
   /**
    * Copies previous matching translations for the given locale into a
    * document. Translations are matching if their document id, textflow id and
    * source content are identical, and their state is approved.
    * 
    * The text flow revision for copied targets is set to the current text flow
    * revision.
    * 
    * @param document the document to copy translations into
    * @param locale the locale of translations to copy
    */
   void copyTransForLocale(HDocument document, HLocale locale);
   
   /**
    * Copies previous matching translations for all available locales into a
    * document. Translations are matching if their document id, textflow id and
    * source content are identical, and their state is approved.
    * 
    * The text flow revision for copied targets is set to the current text flow
    * revision.
    *
    * This method will use the default Copy Trans options for the document's project.
    * If not set, it will use the default global options.
    * 
    * @param document the document to copy translations into
    */
   void copyTransForDocument(HDocument document);

   /**
    * Copies previous matching translations for all available locales into a
    * document. Translations are matching if their document id, textflow id and
    * source content are identical, and their state is approved.
    *
    * The text flow revision for copied targets is set to the current text flow
    * revision.
    *
    * This method will use the Copy Trans options in the given process handle. If not
    * set, it will use the ones set in the project, and finally will default to the
    * default global options. It will also keep updating the provided process handle.
    *
    * @param document The document to copy translations into
    * @param processHandle The process handle to track updates and provide copy trans
    *                      options.
    *
    */
   void copyTransForDocument(HDocument document, CopyTransProcessHandle processHandle);

   /**
    * Copies previous matching translations for all available locales and documents
    * in a given project iteration. Translations are matching if their document id,
    * textflow id and source content are identical, and their state is approved.
    * Only performs copyTrans on non-obsolete documents.
    *
    * The text flow revision for copied targets is set to the current text flow
    * revision.
    *
    * @param iteration The project iteration to copy translations into
    */
   void copyTransForIteration( HProjectIteration iteration );

   /**
    * Copies previous matching translations for all available locales and documents
    * in a given project iteration. Translations are matching if their document id,
    * textflow id and source content are identical, and their state is approved.
    * Only performs copyTrans on non-obsolete documents.
    *
    * The text flow revision for copied targets is set to the current text flow
    * revision.
    *
    * @see CopyTransService#copyTransForIteration(org.zanata.model.HProjectIteration)
    * @param iteration The project iteration to copy translations into
    * @param procHandle The CopyTransProcessHandle to keep track of progress.
    */
   void copyTransForIteration(HProjectIteration iteration, CopyTransProcessHandle procHandle);
}
