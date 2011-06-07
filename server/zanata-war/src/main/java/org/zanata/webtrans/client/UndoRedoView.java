/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client;

import org.zanata.webtrans.client.editor.table.NavigationMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class UndoRedoView extends Composite implements UndoRedoPresenter.Display
{
   private static UndoRedoViewUiBinder uiBinder = GWT.create(UndoRedoViewUiBinder.class);

   interface UndoRedoViewUiBinder extends UiBinder<Widget, UndoRedoView>
   {
   }

   @UiField
   Image undo, redo, undoDisabled, redoDisabled;

   @UiField(provided = true)
   Resources resources;

   @Inject
   public UndoRedoView(final NavigationMessages messages, final Resources resources)
   {
      this.resources = resources;
      initWidget(uiBinder.createAndBindUi(this));

      undo.setTitle(messages.actionToolTip(messages.undoLabel(), ""));
      redo.setTitle(messages.actionToolTip(messages.redoLabel(), ""));
      undoDisabled.setTitle(messages.actionToolTip(messages.undoLabel(), ""));
      redoDisabled.setTitle(messages.actionToolTip(messages.redoLabel(), ""));
   }

   @Override
   public HasClickHandlers getUndoButton()
   {
      return undo;
   }

   @Override
   public HasClickHandlers getRedoButton()
   {
      return redo;
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   public void disableUndo()
   {
      undo.setVisible(false);
      undoDisabled.setVisible(true);
   }

   public void enableUndo()
   {
      undo.setVisible(true);
      undoDisabled.setVisible(false);
   }

   public void disableRedo()
   {
      redo.setVisible(false);
      redoDisabled.setVisible(true);
   }

   public void enableRedo()
   {
      redo.setVisible(true);
      redoDisabled.setVisible(false);
   }

}
