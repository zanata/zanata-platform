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

import org.zanata.webtrans.client.editor.table.SourceContentsDisplay;
import org.zanata.webtrans.client.editor.table.TargetContentsDisplay;
import org.zanata.webtrans.client.ui.FilterViewConfirmationDisplay;
import org.zanata.webtrans.client.ui.FilterViewConfirmationPanel;
import org.zanata.webtrans.client.ui.LoadingPanel;
import org.zanata.webtrans.shared.model.TransUnit;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransUnitEditView extends Composite implements TransUnitEditDisplay
{
   private static TransUnitTableViewUiBinder uiBinder = GWT.create(TransUnitTableViewUiBinder.class);

   private TransUnitListDisplay displayTable;

   private SplitLayoutPanel rootPanel;
   @UiField
   ScrollPanel tuTablePanel;
   @UiField
   ScrollPanel editorPanel;

   private final FilterViewConfirmationDisplay filterViewConfirmationPanel;
   private final LoadingPanel loadingPanel;

   @Inject
   public TransUnitEditView(LoadingPanel loadingPanel, FilterViewConfirmationDisplay filterViewConfirmationDisplay)
   {
      this.loadingPanel = loadingPanel;
      filterViewConfirmationPanel = filterViewConfirmationDisplay;
      rootPanel = uiBinder.createAndBindUi(this);
   }

   @Override
   public void scrollToRow(TransUnit selected)
   {
      if (selected == null)
      {
         return;
      }
      int selectedRowAbsoluteTop = displayTable.getSelectedRowAbsoluteTop(selected);
      int absoluteTop = rootPanel.getAbsoluteTop();

      Log.debug("absolute top: " + absoluteTop + " selection top:" + selectedRowAbsoluteTop);
      //TODO this will scroll to top and cell table header won't be visible.
      tuTablePanel.setVerticalScrollPosition(selectedRowAbsoluteTop - absoluteTop + tuTablePanel.getVerticalScrollPosition());
   }

   @Override
   public void showFilterConfirmation()
   {
      filterViewConfirmationPanel.center();
   }

   @Override
   public void hideFilterConfirmation()
   {
      filterViewConfirmationPanel.hide();
   }

   @Override
   public void addFilterConfirmationHandler(FilterViewConfirmationDisplay.Listener listener)
   {
      filterViewConfirmationPanel.setListener(listener);
   }

   @Override
   public void init(TransUnitListDisplay transUnitListDisplay, SourceContentsDisplay sourceContentsDisplay, TargetContentsDisplay targetContentsDisplay)
   {
      tuTablePanel.setWidget(transUnitListDisplay);
      this.displayTable = transUnitListDisplay;
      if (sourceContentsDisplay == null || targetContentsDisplay == null)
      {
         //must be readonly mode.
         rootPanel.setWidgetSize(editorPanel, 0);
         return;
      }
      HorizontalPanel hPanel = new HorizontalPanel();
      hPanel.setWidth("100%");
      hPanel.add(sourceContentsDisplay);
      hPanel.add(targetContentsDisplay);
      hPanel.setCellWidth(sourceContentsDisplay, "50%");
      hPanel.setCellWidth(targetContentsDisplay, "50%");
      editorPanel.setWidget(hPanel);
   }

   @Override
   public Widget asWidget()
   {
      return rootPanel;
   }

   interface TransUnitTableViewUiBinder extends UiBinder<SplitLayoutPanel, TransUnitEditView>
   {
   }
}