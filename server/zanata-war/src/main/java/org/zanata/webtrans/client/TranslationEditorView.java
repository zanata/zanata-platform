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
package org.zanata.webtrans.client;

import org.zanata.webtrans.client.editor.HasTranslationStats;
import org.zanata.webtrans.client.ui.HasPager;
import org.zanata.webtrans.client.ui.Pager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TranslationEditorView extends Composite implements TranslationEditorPresenter.Display
{

   private static TranslationEditorViewUiBinder uiBinder = GWT.create(TranslationEditorViewUiBinder.class);

   interface TranslationEditorViewUiBinder extends UiBinder<Widget, TranslationEditorView>
   {
   }

   @UiField
   FlowPanel transUnitNavigationContainer, undoRedoContainer;

   @UiField
   LayoutPanel editor;

   @UiField(provided = true)
   TransUnitCountBar transUnitCountBar;

   @UiField(provided = true)
   Pager pager;

   @UiField(provided = true)
   Resources resources;

   @Inject
   public TranslationEditorView(final WebTransMessages messages, final Resources resources)
   {
      this.resources = resources;
      this.transUnitCountBar = new TransUnitCountBar(messages, true);
      this.pager = new Pager(messages, resources);

      initWidget(uiBinder.createAndBindUi(this));
   }

   @Override
   public void setEditorView(Widget editor)
   {
      this.editor.clear();
      this.editor.add(editor);

   }

   @Override
   public void setTransUnitNavigation(Widget navigationWidget)
   {
      transUnitNavigationContainer.clear();
      transUnitNavigationContainer.add(navigationWidget);
   }

   @Override
   public void setUndoRedo(Widget undoRedoWidget)
   {
      undoRedoContainer.clear();
      undoRedoContainer.add(undoRedoWidget);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public HasTranslationStats getTransUnitCount()
   {
      return transUnitCountBar;
   }

   @Override
   public HasPager getPageNavigation()
   {
      return pager;
   }

   @Override
   public boolean isPagerFocused()
   {
      return pager.isFocused();
   }
}
