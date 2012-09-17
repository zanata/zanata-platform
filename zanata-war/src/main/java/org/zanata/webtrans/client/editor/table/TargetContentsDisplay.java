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
package org.zanata.webtrans.client.editor.table;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.webtrans.client.ui.HasUpdateValidationWarning;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.shared.model.HasTransUnitId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.IsWidget;

public interface TargetContentsDisplay extends WidgetDisplay, IsWidget, HasTransUnitId, HasUpdateValidationWarning
{
   void showButtons(boolean displayButtons);

   void focusEditor(int currentEditorIndex);

   void addUndo(UndoLink undoLink);

   void setValue(TransUnit transUnit);

   Integer getVerNum();

   void setToMode(ToggleEditor.ViewMode viewMode);

   void highlightSearch(String findMessage);

   List<String> getCachedTargets();

   List<String> getNewTargets();

   ArrayList<ToggleEditor> getEditors();

   void setListener(Listener listener);

   /**
    * Update cached targets so that any following navigation event won't cause another pending save event.
    * If the save failed, TransUnitSaveService will revert the value back to what it was.
    * @param targets targets
    */
   void updateCachedAndInEditorTargets(List<String> targets);

   interface Listener
   {
      void validate(ToggleEditor editor);

      void saveAsApprovedAndMoveNext(TransUnitId transUnitId);

      void copySource(ToggleEditor editor, TransUnitId id);

      void onCancel(TransUnitId transUnitId);

      void saveAsFuzzy(TransUnitId transUnitId);

      boolean isDisplayButtons();

      boolean isReadOnly();

      void showHistory(TransUnitId transUnitId);

      void onFocus(TransUnitId id, int editorIndex);

      boolean isUsingCodeMirror();
   }
}
