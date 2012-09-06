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
import org.zanata.webtrans.client.ui.ValidationMessagePanelView;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;

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
   HTMLPanel buttons;

   @UiField(provided = true)
   ValidationMessagePanelView validationPanel;
   @UiField
   InlineLabel saveIcon;
   @UiField
   InlineLabel fuzzyIcon;
   @UiField
   InlineLabel cancelIcon;
   @UiField
   InlineLabel historyIcon;
   @UiField
   Styles style;
   @UiField
   SimplePanel undoContainer;

   private HorizontalPanel rootPanel;
   private String findMessage;
   private ArrayList<ToggleEditor> editors;
   private Listener listener;

   private TransUnitId transUnitId;
   private Integer verNum;
   private List<String> cachedTargets;

   @Inject
   public TargetContentsView(Provider<ValidationMessagePanelView> validationMessagePanelViewProvider)
   {
      validationPanel = validationMessagePanelViewProvider.get();
      rootPanel = binder.createAndBindUi(this);
      editorGrid.addStyleName("TableEditorCell-Target-Table");
      editorGrid.ensureDebugId("target-contents-grid");
      editorGrid.setWidth("100%");
      editors = Lists.newArrayList();
   }

   @Override
   public void showButtons(boolean displayButtons)
   {
      buttons.setVisible(displayButtons);
      for (ToggleEditor editor : editors)
      {
         editor.showCopySourceButton(displayButtons);
      }
   }

   @Override
   public void focusEditor(final int currentEditorIndex)
   {
      Scheduler.get().scheduleDeferred(new Command()
      {
         @Override
         public void execute()
         {
            if(editors.size() > currentEditorIndex)
            {
               editors.get(currentEditorIndex).setFocus();
            }
         }
      });
   }

   @Override
   public void addUndo(final UndoLink undoLink)
   {
      undoLink.setLinkStyle("icon-undo " + style.button());
      undoLink.setUndoCallback(new UndoLink.UndoCallback()
      {
         @Override
         public void preUndo()
         {
            undoLink.setLinkStyle("icon-progress " + style.button());
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
      cachedTargets = transUnit.getTargets();
      transUnitId = transUnit.getId();

      editors.clear();
      if (cachedTargets == null)
      {
         cachedTargets = Lists.newArrayList("");
      }
      editorGrid.resize(cachedTargets.size(), COLUMNS);
      int rowIndex = 0;
      for (String target : cachedTargets)
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

   @UiHandler("saveIcon")
   public void onSaveAsApproved(ClickEvent event)
   {
      listener.saveAsApprovedAndMoveNext(transUnitId);
      event.stopPropagation();
   }

   @UiHandler("fuzzyIcon")
   public void onSaveAsFuzzy(ClickEvent event)
   {
      listener.saveAsFuzzy(transUnitId);
      event.stopPropagation();
   }

   @UiHandler("cancelIcon")
   public void onCancel(ClickEvent event)
   {
      listener.onCancel(transUnitId);
      event.stopPropagation();
   }

   @UiHandler("historyIcon")
   public void onHistoryClick(ClickEvent event)
   {
      listener.showHistory(transUnitId);
      event.stopPropagation();
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
      return cachedTargets;
   }

   @Override
   public TransUnitId getId()
   {
      return transUnitId;
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
   public void updateCachedAndInEditorTargets(List<String> targets)
   {
      cachedTargets = ImmutableList.copyOf(targets);
      if (!getNewTargets().equals(cachedTargets))
      {
         for (int i = 0; i < targets.size(); i++)
         {
            String target = targets.get(i);
            editors.get(i).setTextAndValidate(target);
         }
      }
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
      }
      if (viewMode == ToggleEditor.ViewMode.VIEW)
      {
         validationPanel.setVisible(false);
      }
   }

   @Override
   public void updateValidationWarning(List<String> errors)
   {
      validationPanel.updateValidationWarning(errors);
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

   interface Styles extends CssResource
   {

      String button();

      String targetContentsCell();

      String editorGridWrapper();
   }
}
