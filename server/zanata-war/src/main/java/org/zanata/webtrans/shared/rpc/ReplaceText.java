/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.TransUnitId;

public class ReplaceText extends AbstractWorkspaceAction<ReplaceTextResult>
{
   private TransUnitId transUnitId;
   private Integer verNum;
   private String searchText;
   private String replaceText;

   @SuppressWarnings("unused")
   private ReplaceText()
   {
   }

   public ReplaceText(TransUnitId transUnitId, Integer verNum, String searchText, String replaceText)
   {
      this.transUnitId = transUnitId;
      this.verNum = verNum;
      this.searchText = searchText;
      this.replaceText = replaceText;
   }

   public TransUnitId getTransUnitId()
   {
      return transUnitId;
   }

   public Integer getVerNum()
   {
      return verNum;
   }

   public String getSearchText()
   {
      return searchText;
   }

   public String getReplaceText()
   {
      return replaceText;
   }
}
