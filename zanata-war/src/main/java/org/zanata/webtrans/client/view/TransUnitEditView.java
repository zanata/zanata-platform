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
import org.zanata.webtrans.shared.model.TransUnit;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransUnitEditView extends Composite implements TransUnitEditDisplay
{
   private static TransUnitTableViewUiBinder uiBinder = GWT.create(TransUnitTableViewUiBinder.class);

   private TransUnitListDisplay displayTable;

   private ScrollPanel rootPanel;
   private final DialogBox editorDialog;

   public TransUnitEditView()
   {
      rootPanel = uiBinder.createAndBindUi(this);

      //editor popup
      editorDialog = new DialogBox(true, true);
      editorDialog.setWidth("100%");
      //TODO caption can be used to display current editing translator or some other information
      editorDialog.setText("Translation Unit Editor");

   }

   @Override
   public void setDisplayTable(TransUnitListDisplay displayTable)
   {
      rootPanel.setWidget(displayTable);
      this.displayTable = displayTable;
   }

   @Override
   public void scrollToRow(TransUnit selected)
   {
      int selectedRowAbsoluteTop = displayTable.getSelectedRowAbsoluteTop(selected);
      int absoluteTop = rootPanel.getAbsoluteTop();

      Log.debug("absolute top: " + absoluteTop + " selection top:" + selectedRowAbsoluteTop);
      //TODO this will scroll to top and cell table header won't be visible.
      rootPanel.setVerticalScrollPosition(selectedRowAbsoluteTop - absoluteTop + rootPanel.getVerticalScrollPosition());
   }

   @Override
   public void openEditor(SourceContentsDisplay sourceDisplay, TargetContentsDisplay targetDisplay)
   {
      HorizontalPanel editorMain = new HorizontalPanel();
      editorMain.setWidth("100%");
      editorMain.add(sourceDisplay);
      editorMain.add(targetDisplay);
      editorDialog.setWidget(editorMain);
      editorDialog.center();
   }

   @Override
   public Widget asWidget()
   {
      return rootPanel;
   }

   interface TransUnitTableViewUiBinder extends UiBinder<ScrollPanel, TransUnitEditView>
   {
   }
}