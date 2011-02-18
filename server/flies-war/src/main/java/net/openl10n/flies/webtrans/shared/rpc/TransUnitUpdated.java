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
package net.openl10n.flies.webtrans.shared.rpc;

import net.openl10n.flies.common.ContentState;
import net.openl10n.flies.webtrans.shared.model.DocumentId;
import net.openl10n.flies.webtrans.shared.model.TransUnit;


//@ExposeEntity 
public class TransUnitUpdated implements SessionEventData, HasTransUnitUpdatedData
{

   private static final long serialVersionUID = 1L;

   private DocumentId documentId;
   private int wordCount;
   private ContentState previousStatus;
   private TransUnit tu;


   // for ExposeEntity
   public TransUnitUpdated()
   {
   }

   public TransUnitUpdated(DocumentId documentId, int wordCount, ContentState previousStatus, TransUnit tu)
   {
      this.documentId = documentId;
      this.wordCount = wordCount;
      this.previousStatus = previousStatus;
      this.tu = tu;
   }

   @Override
   public DocumentId getDocumentId()
   {
      return documentId;
   }

   public void setDocumentId(DocumentId documentId)
   {
      this.documentId = documentId;
   }


   @Override
   public ContentState getPreviousStatus()
   {
      return previousStatus;
   }

   public void setPreviousStatus(ContentState previousStatus)
   {
      this.previousStatus = previousStatus;
   }


   public int getWordCount()
   {
      return wordCount;
   }

   @Override
   public TransUnit getTransUnit()
   {
      return tu;
   }

}
