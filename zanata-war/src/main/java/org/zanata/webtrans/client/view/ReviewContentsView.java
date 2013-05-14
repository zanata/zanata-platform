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
package org.zanata.webtrans.client.view;

import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.ui.CodeMirrorReadOnlyWidget;
import org.zanata.webtrans.client.ui.HasUpdateValidationWarning;
import org.zanata.webtrans.client.ui.ReviewContentWidget;
import org.zanata.webtrans.client.ui.ReviewContentWrapper;
import org.zanata.webtrans.client.ui.ValidationMessagePanelView;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ReviewContentsView extends Composite implements ReviewContentsDisplay
{
   private static final int COLUMNS = 1;
   private static Binder binder = GWT.create(Binder.class);

   private final HorizontalPanel root;
   @UiField
   Grid editorGrid;
   @UiField(provided = true)
   ValidationMessagePanelView validationPanel;
   @UiField
   InlineLabel acceptIcon;
   @UiField
   InlineLabel rejectIcon;
   private List<ReviewContentWrapper> editors = Lists.newArrayList();
   private TransUnitId id;
   private Listener listener;

   @Inject
   public ReviewContentsView(Provider<ValidationMessagePanelView> validationMessagePanelViewProvider)
   {
      validationPanel = validationMessagePanelViewProvider.get();
      root = binder.createAndBindUi(this);
      editorGrid.addStyleName("TableEditorCell-Target-Table");
//      editorGrid.ensureDebugId("review-contents-grid");
      editorGrid.setWidth("100%");
   }

   @Override
   public void setValueAndCreateNewEditors(TransUnit transUnit)
   {
      id = transUnit.getId();
      editors.clear();
      List<String> cachedTargets = transUnit.getTargets();
      editorGrid.resize(cachedTargets.size(), COLUMNS);
      int rowIndex = 0;
      for (String target : cachedTargets)
      {
         ReviewContentWidget contentWidget = new ReviewContentWidget();
         contentWidget.setText(target);

         editorGrid.setWidget(rowIndex, 0, contentWidget);
         editors.add(contentWidget);
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
         case Reviewed:
            state = " Reviewed";
            break;
         case Rejected:
            state = " Rejected";
            break;
      }
      styles += state + "StateDecoration";
      return styles;
   }

   @UiHandler("acceptIcon")
   public void onAccept(ClickEvent event)
   {
      listener.acceptTranslation(id);
      event.stopPropagation();
   }

   @UiHandler("rejectIcon")
   public void onReject(ClickEvent event)
   {
      listener.rejectTranslation(id);
      event.stopPropagation();
   }

   @Override
   public void updateValidationWarning(List<String> errors)
   {
      validationPanel.updateValidationWarning(errors);
   }

   @Override
   public List<ReviewContentWrapper> getEditors()
   {
      return editors;
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   @Override
   public Widget asWidget()
   {
      return root;
   }

   @Override
   public void refresh()
   {
      for (NeedsRefresh editor : editors)
      {
         editor.refresh();
      }
   }

   @Override
   public TransUnitId getId()
   {
      return id;
   }

   interface Binder extends UiBinder<HorizontalPanel, ReviewContentsView>
   {
   }
}
