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

import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;

import com.google.common.base.Objects;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMerge extends AbstractWorkspaceAction<UpdateTransUnitResult>
{
   private static final long serialVersionUID = 1L;
   private int thresholdPercent;
   private List<TransUnitUpdateRequest> updateRequests;
   private MergeOption differentProjectOption;
   private MergeOption differentDocumentOption;
   private MergeOption differentContextOption;
   private MergeOption importedMatchOption;

   @SuppressWarnings("unused")
   TransMemoryMerge()
   {
   }

   public TransMemoryMerge(int threshold, List<TransUnitUpdateRequest> updateRequests, MergeOption differentProjectOption,
                           MergeOption differentDocumentOption, MergeOption differentContextOption,
                           MergeOption importedMatchOption)
   {
      thresholdPercent = threshold;
      this.updateRequests = updateRequests;
      this.differentProjectOption = differentProjectOption;
      this.differentDocumentOption = differentDocumentOption;
      this.differentContextOption = differentContextOption;
      this.importedMatchOption = importedMatchOption;
   }

   public int getThresholdPercent()
   {
      return thresholdPercent;
   }

   public MergeOption getDifferentProjectOption()
   {
      return differentProjectOption;
   }

   public MergeOption getDifferentDocumentOption()
   {
      return differentDocumentOption;
   }

   public MergeOption getDifferentContextOption()
   {
      return differentContextOption;
   }

   public MergeOption getImportedMatchOption()
   {
      return importedMatchOption;
   }

   public List<TransUnitUpdateRequest> getUpdateRequests()
   {
      return updateRequests;
   }

   @Override
   public String toString()
   {
      // @formatter:off
      return Objects.toStringHelper(this).
            add("thresholdPercent", thresholdPercent).
            add("updateRequests", updateRequests).
            add("differentProjectOption", differentProjectOption).
            add("differentDocumentOption", differentDocumentOption).
            add("differentContextOption", differentContextOption).
            toString();
      // @formatter:on
   }
}
