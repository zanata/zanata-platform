/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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

package org.zanata.webtrans.shared.rpc;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class MergeOptions
{
   private MergeOptions(MergeOption differentProject, MergeOption differentDocument,
         MergeOption differentResId, MergeOption importedMatch)
   {
      this.setDifferentProject(differentProject);
      this.setDifferentDocument(differentDocument);
      this.setDifferentResId(differentResId);
      this.setImportedMatch(importedMatch);
   }

   private MergeOption differentProject;
   private MergeOption differentDocument;
   private MergeOption differentResId;
   private MergeOption importedMatch;

   public static MergeOptions allReject()
   {
      return new MergeOptions(MergeOption.REJECT, MergeOption.REJECT,
            MergeOption.REJECT, MergeOption.REJECT);
   }
   public static MergeOptions allIgnore()
   {
      return new MergeOptions(MergeOption.IGNORE_CHECK, MergeOption.IGNORE_CHECK,
            MergeOption.IGNORE_CHECK, MergeOption.IGNORE_CHECK);
   }
   public static MergeOptions allFuzzy()
   {
      return new MergeOptions(MergeOption.FUZZY, MergeOption.FUZZY,
            MergeOption.FUZZY, MergeOption.FUZZY);
   }

   public MergeOption getDifferentProject()
   {
      return differentProject;
   }
   public void setDifferentProject(MergeOption differentProject)
   {
      this.differentProject = differentProject;
   }
   public MergeOption getDifferentDocument()
   {
      return differentDocument;
   }
   public void setDifferentDocument(MergeOption differentDocument)
   {
      this.differentDocument = differentDocument;
   }
   public MergeOption getDifferentResId()
   {
      return differentResId;
   }
   public void setDifferentResId(MergeOption differentResId)
   {
      this.differentResId = differentResId;
   }
   public MergeOption getImportedMatch()
   {
      return importedMatch;
   }
   public void setImportedMatch(MergeOption importedMatch)
   {
      this.importedMatch = importedMatch;
   }

}
