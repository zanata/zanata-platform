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

package org.zanata.webtrans.client.ui;

import java.util.List;

import org.zanata.webtrans.shared.model.TransUnitUpdateInfo;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.google.gwt.user.client.ui.HasText;
import com.google.inject.ImplementedBy;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@ImplementedBy(RevertTransUnitUpdateLink.class)
public interface UndoLink extends InlineLink, HasText
{
   /**
    * Give the UpdateTransUnitResult object returned from trans unit update handler, then it will create a click handler.
    *
    * @param updateTransUnitResult result from update translation rpc call.
    *
    * @see org.zanata.webtrans.server.rpc.UpdateTransUnitHandler
    * @see org.zanata.webtrans.server.rpc.ReplaceTextHandler
    * @see org.zanata.webtrans.server.rpc.TransMemoryMergeHandler
    */
   void prepareUndoFor(UpdateTransUnitResult updateTransUnitResult);

   void setUndoCallback(UndoCallback callback);

   void removeFromParent();

   interface UndoCallback
   {
      void preUndo();
      void postUndoSuccess();
   }
}
