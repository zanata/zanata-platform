package net.openl10n.flies.webtrans.client.editor;

import net.openl10n.flies.webtrans.client.filter.ContentFilter;
import net.openl10n.flies.webtrans.client.ui.HighlightingLabel;
import net.openl10n.flies.webtrans.shared.model.TransUnit;

import org.gwt.mosaic.ui.client.table.AbstractColumnDefinition;
import org.gwt.mosaic.ui.client.table.CellRenderer;
import org.gwt.mosaic.ui.client.table.ColumnDefinition;
import org.gwt.mosaic.ui.client.table.DefaultTableDefinition;
import org.gwt.mosaic.ui.client.table.RowRenderer;
import org.gwt.mosaic.ui.client.table.property.PreferredWidthProperty;

import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class ListEditorTableDefinition extends DefaultTableDefinition<TransUnit>
{

   public static final int SOURCE_COL = 0;
   public static final int COPY_COL = 1;
   public static final int TARGET_COL = 2;

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
         HighlightingLabel sourceLabel = new HighlightingLabel(rowValue.getSource());
         sourceLabel.setStylePrimaryName("TableEditorContent");
         view.setWidget(sourceLabel);
      }
   };

   private final AbstractColumnDefinition<TransUnit, TransUnit> copyColumnDefinition = new AbstractColumnDefinition<TransUnit, TransUnit>()
   {

      @Override
      public TransUnit getCellValue(TransUnit rowValue)
      {
         return rowValue;
      }

      @Override
      public void setCellValue(TransUnit rowValue, TransUnit cellValue)
      {
      }

   };

   
   private final CellRenderer<TransUnit, TransUnit> copyCellRenderer = new CellRenderer<TransUnit, TransUnit>()
   {
      @Override
      public void renderRowValue(TransUnit rowValue, ColumnDefinition<TransUnit, TransUnit> columnDef, AbstractCellView<TransUnit> view)
      {
         view.setStyleName("TableEditorCell TableEditorCell-Copy");         
         view.setWidget(new Button("&gt;"));
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
         view.setStyleAttribute("width", "100%");
         view.setStyleName("TableEditorCell TableEditorCell-Target");
         final Label label = new HighlightingLabel(rowValue.getTarget());
         label.setStylePrimaryName("TableEditorContent");

         view.setWidget(label);
      }
   };

   @Inject
   public ListEditorTableDefinition()
   {
      setRowRenderer(rowRenderer);
      sourceColumnDefinition.setCellRenderer(sourceCellRenderer);
      copyColumnDefinition.setMaximumColumnWidth(35);
      copyColumnDefinition.setCellRenderer(copyCellRenderer);
      targetColumnDefinition.setCellRenderer(targetCellRenderer);
      
      addColumnDefinition(sourceColumnDefinition);
      addColumnDefinition(copyColumnDefinition);
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

}
