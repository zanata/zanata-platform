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
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.editor.filter.TransFilterPresenter;
import org.zanata.webtrans.client.editor.table.TableEditorPresenter;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.HasPager;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.gen2.table.event.client.PageChangeEvent;
import com.google.gwt.gen2.table.event.client.PageChangeHandler;
import com.google.gwt.gen2.table.event.client.PageCountChangeEvent;
import com.google.gwt.gen2.table.event.client.PageCountChangeHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TranslationEditorPresenter extends WidgetPresenter<TranslationEditorPresenter.Display>
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
   private final TableEditorPresenter tableEditorPresenter;
   private final TransFilterPresenter transFilterPresenter;


   @Inject
   public TranslationEditorPresenter(Display display, EventBus eventBus, final CachingDispatchAsync dispatcher, final TableEditorPresenter tableEditorPresenter, final TransUnitNavigationPresenter transUnitNavigationPresenter, final TransFilterPresenter transFilterPresenter)
   {
      super(display, eventBus);
      this.tableEditorPresenter = tableEditorPresenter;
      this.transUnitNavigationPresenter = transUnitNavigationPresenter;
      this.transFilterPresenter = transFilterPresenter;
   }

   @Override
   protected void onBind()
   {
      transFilterPresenter.bind();
      display.setFilterView(transFilterPresenter.getDisplay().asWidget());

      tableEditorPresenter.bind();
      display.setEditorView(tableEditorPresenter.getDisplay().asWidget());

      transUnitNavigationPresenter.bind();
      display.setTransUnitNavigation(transUnitNavigationPresenter.getDisplay().asWidget());

      // undoRedoPresenter.bind();
      // display.setUndoRedo(undoRedoPresenter.getDisplay().asWidget());
      // Label spacer = new Label();
      // spacer.setWidth("80px");
      // display.setUndoRedo(spacer);

      registerHandler(display.getPageNavigation().addValueChangeHandler(new ValueChangeHandler<Integer>()
      {
         @Override
         public void onValueChange(ValueChangeEvent<Integer> event)
         {
            tableEditorPresenter.getDisplay().getTargetCellEditor().savePendingChange(true);
            tableEditorPresenter.gotoPage(event.getValue() - 1, false);
         }
      }));

      // TODO this uses incubator's HandlerRegistration
      tableEditorPresenter.addPageChangeHandler(new PageChangeHandler()
      {
         @Override
         public void onPageChange(PageChangeEvent event)
         {
            display.getPageNavigation().setValue(event.getNewPage() + 1);
         }
      });

      // TODO this uses incubator's HandlerRegistration
      tableEditorPresenter.addPageCountChangeHandler(new PageCountChangeHandler()
      {
         @Override
         public void onPageCountChange(PageCountChangeEvent event)
         {
            display.getPageNavigation().setPageCount(event.getNewPageCount());
         }
      });
   }

   @Override
   protected void onUnbind()
   {
      tableEditorPresenter.unbind();
      transUnitNavigationPresenter.unbind();
   }

   @Override
   public void onRevealDisplay()
   {
   }

   public TransUnit getSelectedTransUnit()
   {
      return tableEditorPresenter.getSelectedTransUnit();
   }

   public void saveEditorPendingChange()
   {
      tableEditorPresenter.getDisplay().getTargetCellEditor().savePendingChange(true);
   }

   public boolean isEditing()
   {
      return tableEditorPresenter.getDisplay().getTargetCellEditor().isEditing();
   }

   public boolean isCancelButtonFocused()
   {
      return tableEditorPresenter.getDisplay().getTargetCellEditor().isCancelButtonFocused();
   }

   public void setCancelButtonFocused(boolean isCancelButtonFocused)
   {
      tableEditorPresenter.getDisplay().getTargetCellEditor().setCancelButtonFocused(isCancelButtonFocused);
   }

   public boolean isTransFilterFocused()
   {
      return transFilterPresenter.isFocused();
   }

   public void gotoCurrentRow()
   {
      tableEditorPresenter.gotoCurrentRow();
   }

   public void gotoPrevRow(boolean andEdit)
   {
      tableEditorPresenter.gotoPrevRow(andEdit);
   }

   public void gotoNextRow(boolean andEdit)
   {
      tableEditorPresenter.gotoNextRow(andEdit);
   }

   public void openEditorOnSelectedRow()
   {
      tableEditorPresenter.getDisplay().gotoRow(tableEditorPresenter.getSelectedRowIndex(), true);
      tableEditorPresenter.getDisplay().getTargetCellEditor().showEditors(tableEditorPresenter.getSelectedRowIndex(), 0);

   }
}
