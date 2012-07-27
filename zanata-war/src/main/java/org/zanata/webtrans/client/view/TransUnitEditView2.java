package org.zanata.webtrans.client.view;

import java.util.List;

import org.zanata.webtrans.client.editor.table.SourceContentsDisplay;
import org.zanata.webtrans.client.editor.table.TargetContentsDisplay;
import org.zanata.webtrans.client.service.NavigationController;
import org.zanata.webtrans.client.service.SinglePageDataModelImpl;
import org.zanata.webtrans.client.ui.FilterViewConfirmationDisplay;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TransUnitEditView2 extends Composite implements TransUnitEditDisplay2
{
   private Grid transUnitTable = new Grid(0, 2);
   private ScrollPanel container= new ScrollPanel(transUnitTable);
   private final FilterViewConfirmationDisplay filterViewConfirmationDisplay;
   private final NavigationController navigationController;

   @Inject
   public TransUnitEditView2(FilterViewConfirmationDisplay filterViewConfirmationDisplay, NavigationController navigationController
   )
   {
      this.filterViewConfirmationDisplay = filterViewConfirmationDisplay;
      this.navigationController = navigationController;
      transUnitTable.setWidth("100%");
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
   }

   private void selectRow(int rowIndex)
   {
      navigationController.selectByRowIndex(rowIndex);
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
   public void initView(List<SourceContentsDisplay> sourceDisplays, List<TargetContentsDisplay> targetDisplays)
   {
      transUnitTable.resizeRows(sourceDisplays.size());
      for (int i = 0; i < sourceDisplays.size(); i++)
      {
         SourceContentsDisplay sourceDisplay = sourceDisplays.get(i);
         TargetContentsDisplay targetDisplay = targetDisplays.get(i);
         transUnitTable.setWidget(i, 0, sourceDisplay);
         transUnitTable.setWidget(i, 1, targetDisplay);
         transUnitTable.getCellFormatter().setVerticalAlignment(i, 0, HasVerticalAlignment.ALIGN_TOP);
         transUnitTable.getCellFormatter().setVerticalAlignment(i, 1, HasVerticalAlignment.ALIGN_TOP);
      }
      transUnitTable.getColumnFormatter().setWidth(0, "50%");
      transUnitTable.getColumnFormatter().setWidth(1, "50%");
   }

   @Override
   public Widget asWidget()
   {
      return container;
   }


}
