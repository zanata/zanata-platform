package org.fedorahosted.flies.webtrans.client.editor.table;

import org.fedorahosted.flies.webtrans.client.editor.filter.ContentFilter;
import org.fedorahosted.flies.webtrans.client.ui.HighlightingLabel;
import org.fedorahosted.flies.webtrans.shared.model.TransUnit;
import org.gwt.mosaic.ui.client.table.AbstractColumnDefinition;
import org.gwt.mosaic.ui.client.table.CellRenderer;
import org.gwt.mosaic.ui.client.table.ColumnDefinition;
import org.gwt.mosaic.ui.client.table.DefaultTableDefinition;
import org.gwt.mosaic.ui.client.table.RowRenderer;

import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ListEditorTableDefinition extends DefaultTableDefinition<TransUnit>
{

   public static final int SOURCE_COL = 0;
   public static final int TARGET_COL = 1;

   private ContentFilter<TransUnit> contentFilter = null;

   private final RowRenderer<TransUnit> rowRenderer = new RowRenderer<TransUnit>()
   {
      @Override
      public void renderRowValue(TransUnit rowValue, AbstractRowView<TransUnit> view)
      {
         String styles = "TableEditorRow ";
         styles += view.getRowIndex() % 2 == 0 ? "odd-row" : "even-row";

         if (contentFilter != null)
         {
            styles += " content-filter";
            styles += contentFilter.accept(rowValue) ? " content-filter-match" : " content-filter-nomatch";
         }

         String state = "";
         switch (rowValue.getStatus())
         {
         case Approved:
            state = " Approved";
            break;
         case NeedReview:
            state = " Fuzzy";
            break;
         case New:
            state = " New";
            break;
         }
         styles += state + "StateDecoration";

         view.setStyleName(styles);
      }
   };

   private final AbstractColumnDefinition<TransUnit, TransUnit> sourceColumnDefinition = new AbstractColumnDefinition<TransUnit, TransUnit>()
   {
      @Override
      public TransUnit getCellValue(TransUnit rowValue)
      {
         return rowValue;
      }

      @Override
      public void setCellValue(TransUnit rowValue, TransUnit cellValue)
      {
         cellValue.setSource(rowValue.getSource());
         cellValue.setSourceComment(rowValue.getSourceComment());
      }
   };

   private final CellRenderer<TransUnit, TransUnit> sourceCellRenderer = new CellRenderer<TransUnit, TransUnit>()
   {
      @Override
      public void renderRowValue(TransUnit rowValue, ColumnDefinition<TransUnit, TransUnit> columnDef, AbstractCellView<TransUnit> view)
      {
         view.setStyleName("TableEditorCell TableEditorCell-Source");
         SourcePanel sourcePanel = new SourcePanel(rowValue, messages);
         view.setWidget(sourcePanel);
      }
   };

   private final AbstractColumnDefinition<TransUnit, TransUnit> targetColumnDefinition = new AbstractColumnDefinition<TransUnit, TransUnit>()
   {

      @Override
      public TransUnit getCellValue(TransUnit rowValue)
      {
         return rowValue;
      }

      @Override
      public void setCellValue(TransUnit rowValue, TransUnit cellValue)
      {
         cellValue.setTarget(rowValue.getTarget());
      }

   };

   private final CellRenderer<TransUnit, TransUnit> targetCellRenderer = new CellRenderer<TransUnit, TransUnit>()
   {
      @Override
      public void renderRowValue(TransUnit rowValue, ColumnDefinition<TransUnit, TransUnit> columnDef, AbstractCellView<TransUnit> view)
      {
         view.setStyleName("TableEditorCell TableEditorCell-Target");
         final Label label = new HighlightingLabel(rowValue.getTarget());
         label.setStylePrimaryName("TableEditorContent");

         // TODO label.setTitle(rowValue.getTargetComment());

         view.setWidget(label);
      }
   };

   private InlineTargetCellEditor targetCellEditor;
   private final NavigationMessages messages;

   @Inject
   public ListEditorTableDefinition(final NavigationMessages messages, InlineTargetCellEditor cellEditor)
   {
      this.messages = messages;
      setRowRenderer(rowRenderer);
      sourceColumnDefinition.setCellRenderer(sourceCellRenderer);
      targetColumnDefinition.setCellRenderer(targetCellRenderer);
      targetColumnDefinition.setCellEditor(cellEditor);

      addColumnDefinition(sourceColumnDefinition);
      addColumnDefinition(targetColumnDefinition);
   }

   public void clearContentFilter()
   {
      this.contentFilter = null;
   }

   public void setContentFilter(ContentFilter<TransUnit> contentFilter)
   {
      this.contentFilter = contentFilter;
   }

   public ContentFilter<TransUnit> getContentFilter()
   {
      return contentFilter;
   }

   public InlineTargetCellEditor getTargetCellEditor()
   {
      return targetCellEditor;
   }

}
