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

import java.util.List;
import java.util.Set;

import org.zanata.webtrans.client.editor.HasPageNavigation;
import org.zanata.webtrans.client.presenter.SourceContentsPresenter;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.LoadingPanel;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.HasSelectionHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.gen2.table.client.FixedWidthGridBulkRenderer;
import com.google.gwt.gen2.table.client.PagingScrollTable;
import com.google.gwt.gen2.table.client.ScrollTable;
import com.google.gwt.gen2.table.client.SelectionGrid.SelectionPolicy;
import com.google.gwt.gen2.table.event.client.HasPageChangeHandlers;
import com.google.gwt.gen2.table.event.client.HasPageCountChangeHandlers;
import com.google.gwt.gen2.table.event.client.RowSelectionEvent;
import com.google.gwt.gen2.table.event.client.RowSelectionHandler;
import com.google.gwt.gen2.table.event.client.TableEvent.Row;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TableEditorView extends PagingScrollTable<TransUnit> implements TableEditorPresenter.Display, HasSelectionHandlers<TransUnit>, HasPageNavigation
{

   private final RedirectingCachedTableModel<TransUnit> cachedTableModel;
   private final TableEditorTableDefinition tableDefinition;
   private int cachedPages = 2;
   private LoadingPanel loadingPanel;

   public void setFindMessage(String findMessage)
   {
      this.tableDefinition.setFindMessage(findMessage);
   }

   @Inject
   public TableEditorView(NavigationMessages messages, UserWorkspaceContext userWorkspaceContext, final WebTransMessages webTransMessages, final Resources resources, final SourceContentsPresenter sourceContentsPresenter, TargetContentsPresenter targetContentsPresenter)
   {
      this(messages, new RedirectingTableModel<TransUnit>(), userWorkspaceContext, sourceContentsPresenter, targetContentsPresenter);
      loadingPanel = new LoadingPanel(webTransMessages, resources);
      loadingPanel.hide();
   }

   private TableEditorView(NavigationMessages messages, RedirectingTableModel<TransUnit> tableModel, UserWorkspaceContext userWorkspaceContext, final SourceContentsPresenter sourceContentsPresenter, TargetContentsPresenter targetContentsPresenter)
   {
      this(new RedirectingCachedTableModel<TransUnit>(tableModel), new TableEditorTableDefinition(messages, new RedirectingCachedTableModel<TransUnit>(tableModel), sourceContentsPresenter, userWorkspaceContext.hasReadOnlyAccess(), targetContentsPresenter));
   }

   private TableEditorView(RedirectingCachedTableModel<TransUnit> tableModel, TableEditorTableDefinition tableDefinition)
   {
      super(tableModel, tableDefinition);
      this.cachedTableModel = tableModel;
      this.tableDefinition = tableDefinition;
      setStylePrimaryName("TableEditorWrapper");
      setSize("100%", "100%");
      setPageSize(10);
      setEmptyTableWidget(new HTML("There is no data to display"));

      // Setup the bulk renderer
      FixedWidthGridBulkRenderer<TransUnit> bulkRenderer = new FixedWidthGridBulkRenderer<TransUnit>(getDataTable(), this);
      setBulkRenderer(bulkRenderer);

      // Setup the formatting
      setCellPadding(3);
      setCellSpacing(0);
      setResizePolicy(ScrollTable.ResizePolicy.FIXED_WIDTH);

      getDataTable().setStylePrimaryName("TableEditor");
      getDataTable().setSelectionPolicy(SelectionPolicy.ONE_ROW);
      getDataTable().setCellPadding(3);
      getDataTable().setWidth("100%");
      getDataTable().addRowSelectionHandler(new RowSelectionHandler()
      {
         @Override
         public void onRowSelection(RowSelectionEvent event)
         {
            if (!event.getSelectedRows().isEmpty())
            {
               Row row = event.getSelectedRows().iterator().next();
               TransUnit tu = getRowValue(row.getRowIndex());
               SelectionEvent.fire(TableEditorView.this, tu);
            }
         }
      });
   }

   @Override
   public List<TransUnit> getRowValues()
   {
      return super.getRowValues();
   }

   @Override
   public InlineTargetCellEditor getTargetCellEditor()
   {
      return tableDefinition.getTargetCellEditor();
   }

   @Override
   public void setPageSize(int pageSize)
   {
      super.setPageSize(pageSize);
      cachedTableModel.setPostCachedRowCount(pageSize * cachedPages);
      cachedTableModel.setPreCachedRowCount(pageSize * cachedPages);
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   private int ignoreStopProcessingCount = 0;

   @Override
   public void ignoreStopProcessing()
   {
      ignoreStopProcessingCount++;
   }

   @Override
   public void startProcessing()
   {
      loadingPanel.center();
      setVisible(false);
   }

   @Override
   public void stopProcessing()
   {
      if (ignoreStopProcessingCount == 0)
      {
         setVisible(true);
         loadingPanel.hide();
      }
      else
      {
         ignoreStopProcessingCount--;
      }
   }

   @Override
   public boolean isProcessing()
   {
      return loadingPanel.isShowing();
   }

   @Override
   public HandlerRegistration addSelectionHandler(SelectionHandler<TransUnit> handler)
   {
      return addHandler(handler, SelectionEvent.getType());
   }

   @Override
   public HasSelectionHandlers<TransUnit> getSelectionHandlers()
   {
      return this;
   }

   @Override
   public HasPageChangeHandlers getPageChangeHandlers()
   {
      return this;
   }

   @Override
   public HasPageCountChangeHandlers getPageCountChangeHandlers()
   {
      return this;
   }

   public boolean isFirstPage()
   {
      return getCurrentPage() == 0;
   }

   public boolean isLastPage()
   {
      return getCurrentPage() == getPageCount() - 1;
   }

   public void setCachedPages(int cachedPages)
   {
      this.cachedPages = cachedPages;
   }

   @Override
   public RedirectingCachedTableModel<TransUnit> getTableModel()
   {
      return cachedTableModel;
   }

   @Override
   public void setTableModelHandler(TableModelHandler<TransUnit> handler)
   {
      cachedTableModel.getTableModel().setTableModelHandler(handler);
   }

   @Override
   public void gotoRow(int row, boolean andEdit)
   {
      if (row < getDataTable().getRowCount())
      {
         getDataTable().selectRow(row, true);
         scrollIntoView(getDataTable().getWidget(row, TableEditorTableDefinition.TARGET_COL).getElement());
         if (andEdit)
         {
            editCell(row, TableEditorTableDefinition.TARGET_COL);
         }
      }
   }

   @Override
   public int getSelectedRowNumber()
   {
      Set<Integer> selectedRows = super.getDataTable().getSelectedRows();
      if (selectedRows.isEmpty())
      {
         return 0;
      }
      else
      {
         return selectedRows.iterator().next();
      }
   }

   @Override
   public TransUnit getTransUnitValue(int row)
   {
      TransUnit rowValue = getRowValue(row);
      Log.info("getting transunit [" + (rowValue != null) + "] on row:" + row);
      return rowValue;
   }

   @Override
   public void gotoPage(int page, boolean forced)
   {
      super.gotoPage(page, forced);
      gotoRow(0, false);
   }

   @Override
   public void setTransUnitDetails(TransUnit selectedTransUnit)
   {
      this.tableDefinition.setTransUnitDetails(selectedTransUnit);

   }

   private void setSourceColumnBorder(int row, String color, String borderStyle, String borderWidth)
   {
      getDataTable().getCellFormatter().getElement(row, 0).getStyle().setProperty("borderTopColor", color);
      getDataTable().getCellFormatter().getElement(row, 0).getStyle().setProperty("borderTopStyle", borderStyle);
      getDataTable().getCellFormatter().getElement(row, 0).getStyle().setProperty("borderTopWidth", borderWidth);

      getDataTable().getCellFormatter().getElement(row, 0).getStyle().setProperty("borderLeftColor", color);
      getDataTable().getCellFormatter().getElement(row, 0).getStyle().setProperty("borderLeftStyle", borderStyle);
      getDataTable().getCellFormatter().getElement(row, 0).getStyle().setProperty("borderLeftWidth", borderWidth);

      getDataTable().getCellFormatter().getElement(row, 0).getStyle().setProperty("borderBottomColor", color);
      getDataTable().getCellFormatter().getElement(row, 0).getStyle().setProperty("borderBottomStyle", borderStyle);
      getDataTable().getCellFormatter().getElement(row, 0).getStyle().setProperty("borderBottomWidth", borderWidth);
   }

   private void setTargetColumnBorder(int row, String color, String borderStyle, String borderWidth)
   {
      getDataTable().getCellFormatter().getElement(row, 1).getStyle().setProperty("borderTopColor", color);
      getDataTable().getCellFormatter().getElement(row, 1).getStyle().setProperty("borderTopStyle", borderStyle);
      getDataTable().getCellFormatter().getElement(row, 1).getStyle().setProperty("borderTopWidth", borderWidth);

      getDataTable().getCellFormatter().getElement(row, 1).getStyle().setProperty("borderRightColor", color);
      getDataTable().getCellFormatter().getElement(row, 1).getStyle().setProperty("borderRightStyle", borderStyle);
      getDataTable().getCellFormatter().getElement(row, 1).getStyle().setProperty("borderRightWidth", borderWidth);

      getDataTable().getCellFormatter().getElement(row, 1).getStyle().setProperty("borderBottomColor", color);
      getDataTable().getCellFormatter().getElement(row, 1).getStyle().setProperty("borderBottomStyle", borderStyle);
      getDataTable().getCellFormatter().getElement(row, 1).getStyle().setProperty("borderBottomWidth", borderWidth);
   }

   @Override
   public void updateRowBorder(int row, String color)
   {
      if (row < getDataTable().getRowCount())
      {
         setSourceColumnBorder(row, color, "solid", "2px");
         setTargetColumnBorder(row, color, "solid", "2px");
      }
   }

   @Override
   public void resetRowBorder(int row)
   {
      if (row < getDataTable().getRowCount())
      {
         setSourceColumnBorder(row, "", "", "");
         setTargetColumnBorder(row, "", "", "");
      }
   }

   /*
    * Overrides DOM.scrollIntoView() - focus on element's bottom right corner
    * This implementation focus on element's top left corner
    */
   public static native void scrollIntoView(Element elem) /*-{
                                                          var left = elem.offsetLeft, top = elem.offsetTop;
                                                          var width = elem.offsetWidth, height = elem.offsetHeight;

                                                          if (elem.parentNode != elem.offsetParent) {
                                                          left -= elem.parentNode.offsetLeft;
                                                          top -= elem.parentNode.offsetTop;
                                                          }

                                                          var cur = elem.parentNode;
                                                          while (cur && (cur.nodeType == 1)) {
                                                          if (left + width > cur.scrollLeft + cur.clientWidth) {
                                                          cur.scrollLeft = (left + width) - cur.clientWidth;
                                                          }
                                                          if (left < cur.scrollLeft) {
                                                          cur.scrollLeft = left;
                                                          }
                                                          if (top + height > cur.scrollTop + cur.clientHeight) {
                                                          cur.scrollTop = (top + height) - cur.clientHeight;
                                                          }
                                                          if (top < cur.scrollTop) {
                                                          cur.scrollTop = top;
                                                          }

                                                          var offsetLeft = cur.offsetLeft, offsetTop = cur.offsetTop;
                                                          if (cur.parentNode != cur.offsetParent) {
                                                          offsetLeft -= cur.parentNode.offsetLeft;
                                                          offsetTop -= cur.parentNode.offsetTop;
                                                          }

                                                          left += offsetLeft - cur.scrollLeft;
                                                          top += offsetTop - cur.scrollTop;
                                                          cur = cur.parentNode;
                                                          }
                                                          }-*/;
}
