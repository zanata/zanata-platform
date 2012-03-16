package org.zanata.webtrans.client.editor.table;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.shared.model.TransUnit;

import com.google.gwt.gen2.table.client.FixedWidthGridBulkRenderer;
import com.google.gwt.gen2.table.client.PagingScrollTable;
import com.google.gwt.gen2.table.client.SelectionGrid.SelectionPolicy;

public class TargetEditorView extends PagingScrollTable<TransUnit>
{
   private final TargetEditorTableDefinition targetTableDefinition;
   
   public TargetEditorView(NavigationMessages messages, EventBus eventBus, RedirectingTableModel<TransUnit> tableModel, boolean isReadOnly)
   {
      this(new RedirectingCachedTableModel<TransUnit>(tableModel), new TargetEditorTableDefinition(messages, eventBus, new RedirectingCachedTableModel<TransUnit>(tableModel), isReadOnly));
   }
   
   private TargetEditorView(RedirectingCachedTableModel<TransUnit> tableModel, TargetEditorTableDefinition targetTableDefinition)
   {
      super(tableModel, targetTableDefinition);
      this.targetTableDefinition = targetTableDefinition;
      setSize("100%", "100%");

      FixedWidthGridBulkRenderer<TransUnit> bulkRenderer = new FixedWidthGridBulkRenderer<TransUnit>(getDataTable(), this);
      setBulkRenderer(bulkRenderer);

      getDataTable().setSelectionPolicy(SelectionPolicy.ONE_ROW);
      getDataTable().setWidth("100%");
   }

}
