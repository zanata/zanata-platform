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
package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.DisplaySouthPanelEvent;
import org.zanata.webtrans.client.events.PageChangeEvent;
import org.zanata.webtrans.client.events.PageChangeEventHandler;
import org.zanata.webtrans.client.events.PageCountChangeEvent;
import org.zanata.webtrans.client.events.PageCountChangeEventHandler;
import org.zanata.webtrans.client.events.RefreshPageEvent;
import org.zanata.webtrans.client.view.TranslationEditorDisplay;

import com.google.inject.Inject;

public class TranslationEditorPresenter extends WidgetPresenter<TranslationEditorDisplay> implements PageChangeEventHandler, PageCountChangeEventHandler, TranslationEditorDisplay.Listener
{
   private final TransUnitNavigationPresenter transUnitNavigationPresenter;
   private final TransFilterPresenter transFilterPresenter;
   private final TransUnitChangeSourceLangPresenter transUnitSourceLangPresenter;
   private final TransUnitsTablePresenter transUnitsTablePresenter;
   private final EditorKeyShortcuts editorKeyShortcuts;

   @Inject
   public TranslationEditorPresenter(TranslationEditorDisplay display, EventBus eventBus, TransUnitNavigationPresenter transUnitNavigationPresenter, TransFilterPresenter transFilterPresenter, TransUnitsTablePresenter transUnitsTablePresenter, TransUnitChangeSourceLangPresenter transUnitSourceLangPresenter, EditorKeyShortcuts editorKeyShortcuts)
   {
      super(display, eventBus);
      this.transUnitNavigationPresenter = transUnitNavigationPresenter;
      this.transFilterPresenter = transFilterPresenter;
      this.transUnitSourceLangPresenter = transUnitSourceLangPresenter;
      this.transUnitsTablePresenter = transUnitsTablePresenter;
      this.editorKeyShortcuts = editorKeyShortcuts;

      display.setListener(this);
   }

   @Override
   protected void onBind()
   {
      transFilterPresenter.bind();
      display.setFilterView(transFilterPresenter.getDisplay().asWidget());

      transUnitsTablePresenter.bind();
      display.setEditorView(transUnitsTablePresenter.getDisplay().asWidget());      
      transUnitSourceLangPresenter.bind();
      display.setSourceLangView(transUnitSourceLangPresenter.getDisplay().asWidget());
      transUnitNavigationPresenter.bind();
      display.setTransUnitNavigation(transUnitNavigationPresenter.getDisplay().asWidget());

      registerHandler(eventBus.addHandler(PageChangeEvent.TYPE, this));
      registerHandler(eventBus.addHandler(PageCountChangeEvent.TYPE, this));
   }

   @Override
   public void onPageChange(PageChangeEvent event)
   {
      display.getPageNavigation().setValue(event.getPageNumber());
   }

   @Override
   public void onPageCountChange(PageCountChangeEvent event)
   {
      display.getPageNavigation().setPageCount(event.getPageCount());
   }

   @Override
   public void refreshCurrentPage()
   {
      eventBus.fireEvent(RefreshPageEvent.REFRESH_CODEMIRROR_EVENT);
   }

   @Override
   protected void onUnbind()
   {
      transFilterPresenter.unbind();
      transUnitSourceLangPresenter.unbind();
      transUnitsTablePresenter.unbind();
      transUnitNavigationPresenter.unbind();
   }

   @Override
   public void onRevealDisplay()
   {
   }

   public boolean isTransFilterFocused()
   {
      return transFilterPresenter.isFocused();
   }

   @Override
   public void onResizeClicked()
   {
      eventBus.fireEvent(new DisplaySouthPanelEvent(display.getAndToggleResizeButton()));
   }

   @Override
   public void onPagerFocused()
   {
      editorKeyShortcuts.enableNavigationContext();
   }

   @Override
   public void onPagerBlurred()
   {
      editorKeyShortcuts.enableEditContext();
   }

   @Override
   public void onPagerValueChanged(Integer pageNumber)
   {
      transUnitsTablePresenter.goToPage(pageNumber);
   }

   public void setReadOnly(boolean isReadOnly)
   {
      display.getResizeButton().setVisible(isReadOnly);
   }

}
