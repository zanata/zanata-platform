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

import org.zanata.webtrans.client.editor.filter.TransFilterPresenter;
import org.zanata.webtrans.client.events.PageChangeEvent;
import org.zanata.webtrans.client.events.PageChangeEventHandler;
import org.zanata.webtrans.client.events.PageCountChangeEvent;
import org.zanata.webtrans.client.events.PageCountChangeEventHandler;
import org.zanata.webtrans.client.ui.HasPager;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

public class TranslationEditorPresenter extends WidgetPresenter<TranslationEditorPresenter.Display> implements PageChangeEventHandler, PageCountChangeEventHandler
{

   public interface Display extends WidgetDisplay
   {

      void setEditorView(Widget widget);

      void setTransUnitNavigation(Widget widget);

      void setFilterView(Widget filterView);

      HasPager getPageNavigation();

      boolean isPagerFocused();

   }

   private final TransUnitNavigationPresenter transUnitNavigationPresenter;
   private final TransFilterPresenter transFilterPresenter;
   private final TransUnitEditPresenter transUnitEditPresenter;


   @Inject
   public TranslationEditorPresenter(Display display, EventBus eventBus, TransUnitNavigationPresenter transUnitNavigationPresenter, TransFilterPresenter transFilterPresenter, TransUnitEditPresenter transUnitEditPresenter)
   {
      super(display, eventBus);
      this.transUnitNavigationPresenter = transUnitNavigationPresenter;
      this.transFilterPresenter = transFilterPresenter;
      this.transUnitEditPresenter = transUnitEditPresenter;
   }

   @Override
   protected void onBind()
   {
      transFilterPresenter.bind();
      display.setFilterView(transFilterPresenter.getDisplay().asWidget());

      transUnitEditPresenter.bind();
      display.setEditorView(transUnitEditPresenter.getDisplay().asWidget());

      transUnitNavigationPresenter.bind();
      display.setTransUnitNavigation(transUnitNavigationPresenter.getDisplay().asWidget());


      registerHandler(display.getPageNavigation().addValueChangeHandler(new ValueChangeHandler<Integer>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Integer> event)
         {
            transUnitEditPresenter.goToPage(event.getValue());
         }
      }));
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
   protected void onUnbind()
   {
      transFilterPresenter.unbind();
      transUnitEditPresenter.unbind();
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

   public void openEditorOnSelectedRow()
   {
//      tableEditorPresenter.getDisplay().gotoRow(tableEditorPresenter.getSelectedRowIndex(), true);
//      tableEditorPresenter.getDisplay().getTargetCellEditor().showEditors(tableEditorPresenter.getSelectedRowIndex(), 0);

   }
}
