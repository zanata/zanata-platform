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
package org.zanata.webtrans.shared.rpc;

import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;

public class ReplaceText extends UpdateTransUnit
{
   private static final long serialVersionUID = 1L;

   private String searchText;
   private String replaceText;
   private boolean caseSensitive;

   @SuppressWarnings("unused")
   private ReplaceText()
   {
      super();
   }

   public ReplaceText(TransUnit transUnit, String searchText, String replaceText, boolean isCaseSensitive)
   {
      super(new TransUnitUpdateRequest(transUnit.getId(), transUnit.getTargets(), transUnit.getStatus(), transUnit.getVerNum()));
      caseSensitive = isCaseSensitive;
      this.searchText = searchText;
      this.replaceText = replaceText;
   }

   public String getSearchText()
   {
      return searchText;
   }

   public String getReplaceText()
   {
      return replaceText;
   }

   public boolean isCaseSensitive()
   {
      return caseSensitive;
   }
}
