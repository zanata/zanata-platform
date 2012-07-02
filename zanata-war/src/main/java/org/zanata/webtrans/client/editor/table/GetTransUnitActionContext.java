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

package org.zanata.webtrans.client.editor.table;

import org.zanata.webtrans.shared.model.DocumentId;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.common.base.Objects;

/**
 * This class is immutable and all the mutator methods will return a new instance of it.
 * This is so that it can be shared by multiple objects to keep track of states easily.
 *
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class GetTransUnitActionContext
{
   private final DocumentId documentId;
   private String findMessage;
   private int offset = 0;
   private int count = 10; // default page count
   private boolean filterTranslated;
   private boolean filterNeedReview;
   private boolean filterUntranslated;
   private TransUnitId targetTransUnitId;

   private GetTransUnitActionContext(DocumentId documentId)
   {
      this.documentId = documentId;
   }

   private GetTransUnitActionContext(GetTransUnitActionContext other)
   {
      documentId = other.getDocumentId();
      findMessage = other.getFindMessage();
      offset = other.getOffset();
      count = other.getCount();
      filterTranslated = other.isFilterTranslated();
      filterNeedReview = other.isFilterNeedReview();
      filterUntranslated = other.isFilterUntranslated();
      targetTransUnitId = other.getTargetTransUnitId();
   }

   public static GetTransUnitActionContext of(DocumentId documentId)
   {
      return new GetTransUnitActionContext(documentId);
   }

   public static GetTransUnitActionContext of(DocumentId documentId, String findMessage)
   {
      return new GetTransUnitActionContext(documentId).setFindMessage(findMessage);
   }

   public DocumentId getDocumentId()
   {
      return documentId;
   }

   public String getFindMessage()
   {
      return findMessage;
   }

   public GetTransUnitActionContext setFindMessage(String findMessage)
   {
      GetTransUnitActionContext result = new GetTransUnitActionContext(this);
      result.findMessage = findMessage;
      return result;
   }

   public boolean isFilterTranslated()
   {
      return filterTranslated;
   }

   public GetTransUnitActionContext setFilterTranslated(boolean filterTranslated)
   {
      GetTransUnitActionContext result = new GetTransUnitActionContext(this);
      result.filterTranslated = filterTranslated;
      return result;
   }

   public boolean isFilterNeedReview()
   {
      return filterNeedReview;
   }

   public GetTransUnitActionContext setFilterNeedReview(boolean filterNeedReview)
   {
      GetTransUnitActionContext result = new GetTransUnitActionContext(this);
      result.filterNeedReview = filterNeedReview;
      return result;
   }

   public boolean isFilterUntranslated()
   {
      return filterUntranslated;
   }

   public GetTransUnitActionContext setFilterUntranslated(boolean filterUntranslated)
   {
      GetTransUnitActionContext result = new GetTransUnitActionContext(this);
      result.filterUntranslated = filterUntranslated;
      return result;
   }

   public TransUnitId getTargetTransUnitId()
   {
      return targetTransUnitId;
   }

   public GetTransUnitActionContext setTargetTransUnitId(TransUnitId targetTransUnitId)
   {
      GetTransUnitActionContext result = new GetTransUnitActionContext(this);
      result.targetTransUnitId = targetTransUnitId;
      return result;
   }

   public int getOffset()
   {
      return offset;
   }

   public GetTransUnitActionContext setOffset(int offset)
   {
      GetTransUnitActionContext result = new GetTransUnitActionContext(this);
      result.offset = offset;
      return result;
   }

   public int getCount()
   {
      return count;
   }

   public GetTransUnitActionContext setCount(int count)
   {
      GetTransUnitActionContext result = new GetTransUnitActionContext(this);
      result.count = count;
      return result;
   }

   @Override
   public String toString()
   {
      // @formatter:off
      return Objects.toStringHelper(this).
            add("documentId", documentId).
            add("findMessage", findMessage).
            add("offset", offset).
            add("count", count).
            add("filterTranslated", filterTranslated).
            add("filterNeedReview", filterNeedReview).
            add("filterUntranslated", filterUntranslated).
            add("targetTransUnitId", targetTransUnitId).
            toString();
      // @formatter:on
   }

   /**
    * Detects whether context has changed so that we need to reload translation unit list
    * @param newContext new context compare to current
    * @return true if we should reload list
    */
   public boolean needReloadList(GetTransUnitActionContext newContext)
   {
      return needReloadNavigationIndex(newContext) || count != newContext.count || offset != newContext.offset;
   }

   /**
    * Detects whether context has changed so that we need to reload navigation indexes
    * @param newContext new context compare to current
    * @return true if we should reload navigation index
    */
   public boolean needReloadNavigationIndex(GetTransUnitActionContext newContext)
   {
      if (this == newContext)
      {
         return false;
      }

      // @formatter:off
      return filterNeedReview != newContext.filterNeedReview
            || filterTranslated != newContext.filterTranslated
            || filterUntranslated != newContext.filterUntranslated
            || !documentId.equals(newContext.documentId)
            || !Objects.equal(findMessage, newContext.findMessage);
      // @formatter:on
   }
}
