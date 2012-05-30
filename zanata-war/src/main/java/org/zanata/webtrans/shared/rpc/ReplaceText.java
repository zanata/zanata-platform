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

import java.util.Collections;
import java.util.List;

import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated.UpdateType;

public class ReplaceText extends UpdateTransUnit
{
   private static final long serialVersionUID = 1L;

   private String searchText;
   private String replaceText;
   private boolean caseSensitive;

   private ReplaceText()
   {
      super(UpdateType.ReplaceText);
   }

   public ReplaceText(TransUnit transUnit, String searchText, String replaceText, boolean isCaseSensitive)
   {
      this(Collections.singletonList(transUnit), searchText, replaceText, isCaseSensitive);
   }

   public ReplaceText(List<TransUnit> transUnits, String searchText, String replaceText, boolean isCaseSensitive)
   {
      this();
      caseSensitive = isCaseSensitive;
      this.searchText = searchText;
      this.replaceText = replaceText;
      for (TransUnit tu : transUnits)
      {
         addTransUnit(new TransUnitUpdateRequest(tu.getId(), tu.getTargets(), tu.getStatus(), tu.getVerNum()));
      }
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
