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

import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.common.base.Objects;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMerge extends AbstractWorkspaceAction<NoOpResult>
{
   private static final long serialVersionUID = 1L;
   private int thresholdPercent;
   private List<TransUnitId> unitIds;
   private MergeOption differentProjectOption;
   private MergeOption differentDocumentOption;
   private MergeOption differentResIdOption;

   @SuppressWarnings("unused")
   TransMemoryMerge()
   {
   }

   public TransMemoryMerge(int threshold, List<TransUnitId> unitIds, MergeOption differentProjectOption, MergeOption differentDocumentOption, MergeOption differentResIdOption)
   {
      thresholdPercent = threshold;
      this.unitIds = unitIds;
      this.differentProjectOption = differentProjectOption;
      this.differentDocumentOption = differentDocumentOption;
      this.differentResIdOption = differentResIdOption;
   }

   public int getThresholdPercent()
   {
      return thresholdPercent;
   }

   public List<TransUnitId> getUnitIds()
   {
      return unitIds;
   }

   public MergeOption getDifferentProjectOption()
   {
      return differentProjectOption;
   }

   public MergeOption getDifferentDocumentOption()
   {
      return differentDocumentOption;
   }

   public MergeOption getDifferentResIdOption()
   {
      return differentResIdOption;
   }

   @Override
   public String toString()
   {
      // @formatter:off
      return Objects.toStringHelper(this).
            add("thresholdPercent", thresholdPercent).
            add("unitIds", unitIds).
            add("differentProjectOption", differentProjectOption).
            add("differentDocumentOption", differentDocumentOption).
            add("differentResIdOption", differentResIdOption).
            toString();
      // @formatter:on
   }
}
