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

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.ui.Editor;
import org.zanata.webtrans.client.ui.ToggleEditor;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class TargetContentsView extends Composite implements TargetContentsDisplay
{
   interface Binder extends UiBinder<HorizontalPanel, TargetContentsView>
   {
   }

   private static Binder binder = GWT.create(Binder.class);

   public static final int COLUMNS = 1;

   @UiField
   Grid editorGrid;
   @UiField
   VerticalPanel buttons;
   @UiField
   PushButton saveButton;
   @UiField
   PushButton fuzzyButton;
   @UiField
   PushButton cancelButton;
   @UiField
   SimplePanel undoContainer;
   @UiField
   PushButton historyButton;

   private HorizontalPanel rootPanel;
   private String findMessage;
   private ArrayList<ToggleEditor> editors;
   private Listener listener;
   private Integer verNum;
   private List<String> targets;

   public TargetContentsView()
   {
      rootPanel = binder.createAndBindUi(this);
      editorGrid.addStyleName("TableEditorCell-Target-Table");
      editorGrid.ensureDebugId("target-contents-grid");
      editorGrid.setWidth("100%");
      editors = Lists.newArrayList();
   }

   @Override
   public void showButtons(boolean displayButtons)
   {
      buttons.setVisible(isEditing() && displayButtons);
   }

   @Override
   public void focusEditor(final int currentEditorIndex)
   {
      Scheduler.get().scheduleDeferred(new Command()
      {
         @Override
         public void execute()
         {
            editors.get(currentEditorIndex).setFocus();
         }
      });
   }

   @Override
   public void addUndo(final UndoLink undoLink)
   {
      undoLink.setUndoCallback(new UndoLink.UndoCallback()
      {
         @Override
         public void preUndo()
         {
         }

         @Override
         public void postUndoSuccess()
         {
            undoContainer.remove(undoLink);
         }
      });
      undoContainer.setWidget(undoLink);
   }

   @Override
   public void setValue(TransUnit transUnit)
   {
      verNum = transUnit.getVerNum();
      targets = transUnit.getTargets();
      editors.clear();
      if (targets == null || targets.size() <= 0)
      {
         targets = Lists.newArrayList("");
      }
      editorGrid.resize(targets.size(), COLUMNS);
      int rowIndex = 0;
      for (String target : targets)
      {
         Editor editor = new Editor(target, findMessage, rowIndex, listener, transUnit.getId());
         editorGrid.setWidget(rowIndex, 0, editor);
         editors.add(editor);
         rowIndex++;
      }
      editorGrid.setStyleName(resolveStyleName(transUnit.getStatus()));
   }

   private static String resolveStyleName(ContentState status)
   {
      String styles = "TableEditorRow ";
      String state = "";
      switch (status)
      {
         case Approved:
            state = " Approved";
            break;
         case NeedReview:
            state = " Fuzzy";
            break;
         case New:
            state = " New";
            break;
      }
      styles += state + "StateDecoration";
      return styles;
   }

   @UiHandler("saveButton")
   public void onSaveAsApproved(ClickEvent event)
   {
      listener.saveAsApprovedAndMoveNext();
      event.stopPropagation();
   }

   @UiHandler("fuzzyButton")
   public void onSaveAsFuzzy(ClickEvent event)
   {
      listener.saveAsFuzzy();
      event.stopPropagation();
   }

   @UiHandler("cancelButton")
   public void onCancel(ClickEvent event)
   {
      listener.onCancel();
      event.stopPropagation();
   }

   @UiHandler("historyButton")
   public void onHistoryClick(ClickEvent event)
   {
      listener.showHistory();
   }

   @Override
   public void setFindMessage(String findMessage)
   {
      this.findMessage = findMessage;
   }

   @Override
   public ArrayList<String> getNewTargets()
   {
      ArrayList<String> result = Lists.newArrayList();
      for (ToggleEditor editor : editors)
      {
         result.add(editor.getText());
      }
      return result;
   }

   @Override
   public List<String> getCachedTargets()
   {
      return targets;
   }

   @Override
   public boolean isEditing()
   {
      for (ToggleEditor editor : editors)
      {
         if (editor.getViewMode() == ToggleEditor.ViewMode.EDIT)
         {
            return true;
         }
      }
      return false;
   }

   @Override
   public ArrayList<ToggleEditor> getEditors()
   {
      return editors;
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   @Override
   public Integer getVerNum()
   {
      return verNum;
   }

   @Override
   public void setToMode(ToggleEditor.ViewMode viewMode)
   {
      for (ToggleEditor editor : editors)
      {
         editor.setViewMode(viewMode);
         if (viewMode == ToggleEditor.ViewMode.VIEW)
         {
            editor.removeValidationMessagePanel();
         }
      }
   }

   @Override
   public Widget asWidget()
   {
      return rootPanel;
   }

   @Override
   public String toString()
   {
      return Objects.toStringHelper(this).add("editors", editors).toString();
   }
}
