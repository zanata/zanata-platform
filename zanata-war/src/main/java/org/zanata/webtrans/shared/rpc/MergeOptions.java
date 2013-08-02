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
   private MergeOptions(MergeRule differentProject, MergeRule differentDocument,
         MergeRule differentResId, MergeRule importedMatch)
   {
      this.setDifferentProject(differentProject);
      this.setDifferentDocument(differentDocument);
      this.setDifferentResId(differentResId);
      this.setImportedMatch(importedMatch);
   }

   private MergeRule differentProject;
   private MergeRule differentDocument;
   private MergeRule differentResId;
   private MergeRule importedMatch;

   public static MergeOptions allReject()
   {
      return new MergeOptions(MergeRule.REJECT, MergeRule.REJECT,
            MergeRule.REJECT, MergeRule.REJECT);
   }
   public static MergeOptions allIgnore()
   {
      return new MergeOptions(MergeRule.IGNORE_CHECK, MergeRule.IGNORE_CHECK,
            MergeRule.IGNORE_CHECK, MergeRule.IGNORE_CHECK);
   }
   public static MergeOptions allFuzzy()
   {
      return new MergeOptions(MergeRule.FUZZY, MergeRule.FUZZY,
            MergeRule.FUZZY, MergeRule.FUZZY);
   }

   public MergeRule getDifferentProject()
   {
      return differentProject;
   }
   public void setDifferentProject(MergeRule differentProject)
   {
      this.differentProject = differentProject;
   }
   public MergeRule getDifferentDocument()
   {
      return differentDocument;
   }
   public void setDifferentDocument(MergeRule differentDocument)
   {
      this.differentDocument = differentDocument;
   }
   public MergeRule getDifferentResId()
   {
      return differentResId;
   }
   public void setDifferentResId(MergeRule differentResId)
   {
      this.differentResId = differentResId;
   }
   public MergeRule getImportedMatch()
   {
      return importedMatch;
   }
   public void setImportedMatch(MergeRule importedMatch)
   {
      this.importedMatch = importedMatch;
   }

}
