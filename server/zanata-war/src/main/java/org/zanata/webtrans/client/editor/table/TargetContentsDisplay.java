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
package org.zanata.webtrans.client.editor.table;

import java.util.List;

import org.zanata.webtrans.client.ui.Editor;
import org.zanata.webtrans.client.ui.ToggleEditor;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

public interface TargetContentsDisplay extends WidgetDisplay, Iterable<ToggleEditor>
{

   ToggleEditor openEditorAndCloseOthers(ToggleEditor currentEditor);

   interface Listener
   {
      void validate(ToggleEditor editor);

      void saveAsApproved(ToggleEditor editor);

      void copySource(ToggleEditor editor);

      void toggleView(ToggleEditor editor);

      void setValidationMessagePanel(ToggleEditor editor);

      void onCancel(ToggleEditor editor);

      void saveAsFuzzy(ToggleEditor editor);
   }

   void setTargets(List<String> targets);

   void setFindMessage(String findMessage);

   List<String> getNewTargets();

   void setToView();

   boolean isEditing();
    
   List<ToggleEditor> getEditors();

   void setListener(Listener listener);
}
