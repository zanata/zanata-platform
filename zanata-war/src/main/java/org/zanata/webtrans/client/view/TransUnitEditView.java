package org.zanata.webtrans.client.view;

import java.util.List;

import org.zanata.webtrans.client.editor.table.SourceContentsDisplay;
import org.zanata.webtrans.client.editor.table.TargetContentsDisplay;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.FilterViewConfirmationDisplay;
import org.zanata.webtrans.client.ui.LoadingPanel;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TransUnitEditView extends Composite implements TransUnitEditDisplay
{
   private static TransUnitEditViewUiBinder uiBinder = GWT.create(TransUnitEditViewUiBinder.class);

   @UiField
   Grid transUnitTable;
   @UiField
   Styles style;
   @UiField
   WebTransMessages messages;
   @UiField
   ScrollPanel root;

   private final FilterViewConfirmationDisplay filterViewConfirmationDisplay;
   private final LoadingPanel loadingPanel;
   private final Label noContentLabel = new Label();
   private Listener listener;

   @Inject
   public TransUnitEditView(FilterViewConfirmationDisplay filterViewConfirmationDisplay, LoadingPanel loadingPanel)
   {
      this.filterViewConfirmationDisplay = filterViewConfirmationDisplay;
      this.loadingPanel = loadingPanel;
      initWidget(uiBinder.createAndBindUi(this));
      noContentLabel.setText(messages.noContent());
      noContentLabel.setStyleName(style.noContent());

      transUnitTable.addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            HTMLTable.Cell cellForEvent = transUnitTable.getCellForEvent(event);
            if (cellForEvent != null)
            {
               selectRow(cellForEvent.getRowIndex());
            }
         }
      });
      transUnitTable.resize(0, 2);
      transUnitTable.getColumnFormatter().setWidth(0, "50%");
      transUnitTable.getColumnFormatter().setWidth(1, "50%");
   }

   private void selectRow(int rowIndex)
   {
      listener.onRowSelected(rowIndex);
   }

   @Override
   public void addFilterConfirmationHandler(FilterViewConfirmationDisplay.Listener listener)
   {
      filterViewConfirmationDisplay.setListener(listener);
   }

   @Override
   public void showFilterConfirmation()
   {
      filterViewConfirmationDisplay.center();
   }

   @Override
   public void hideFilterConfirmation()
   {
      filterViewConfirmationDisplay.hide();
   }

   @Override
   public void buildTable(List<SourceContentsDisplay> sourceDisplays, List<TargetContentsDisplay> targetDisplays)
   {
      showEmptyContentIfNoData(sourceDisplays.size());

      transUnitTable.resizeRows(sourceDisplays.size());
      for (int i = 0; i < sourceDisplays.size(); i++)
      {
         SourceContentsDisplay sourceDisplay = sourceDisplays.get(i);
         TargetContentsDisplay targetDisplay = targetDisplays.get(i);
         transUnitTable.setWidget(i, 0, sourceDisplay);
         transUnitTable.setWidget(i, 1, targetDisplay);
         HTMLTable.CellFormatter cellFormatter = transUnitTable.getCellFormatter();
         cellFormatter.setVerticalAlignment(i, 0, HasVerticalAlignment.ALIGN_TOP);
         cellFormatter.setVerticalAlignment(i, 1, HasVerticalAlignment.ALIGN_TOP);
         cellFormatter.setStyleName(i, 0, style.cellFormat());
         cellFormatter.setStyleName(i, 1, style.cellFormat());

      }
      applyRowStyle();
   }

   private void showEmptyContentIfNoData(int dataSize)
   {
      if (dataSize == 0)
      {
         root.setWidget(noContentLabel);
      }
      else
      {
         root.setWidget(transUnitTable);
      }
   }

   private void applyRowStyle()
   {
      HTMLTable.RowFormatter rowFormatter = transUnitTable.getRowFormatter();
      for (int i = 0; i < transUnitTable.getRowCount(); i++)
      {
         if ((i % 2) == 0)
         {
            rowFormatter.setStyleName(i, style.evenRow());
         }
         else
         {
            rowFormatter.setStyleName(i, style.oddRow());
         }
      }
   }

   @Override
   public void applySelectedStyle(int rowIndex)
   {
      HTMLTable.RowFormatter rowFormatter = transUnitTable.getRowFormatter();
      for (int i = 0; i < transUnitTable.getRowCount(); i++)
      {
         if (i == rowIndex)
         {
            rowFormatter.addStyleName(i, style.selected());
         }
         else
         {
            rowFormatter.removeStyleName(i, style.selected());
         }
      }
   }

   @Override
   public void setRowSelectionListener(Listener listener)
   {
      this.listener = listener;
   }

   @Override
   public void showLoading()
   {
      loadingPanel.center();
      transUnitTable.setVisible(false);
   }

   @Override
   public void hideLoading()
   {
      loadingPanel.hide();
      transUnitTable.setVisible(true);
   }

   interface Styles extends CssResource
   {
      String oddRow();

      String evenRow();

      String cellFormat();

      String table();

      String selected();

      String noContent();
   }

   interface TransUnitEditViewUiBinder extends UiBinder<Widget, TransUnitEditView>
   {
   }
}
