package org.zanata.webtrans.client.view;

import java.util.List;

import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.CellTableResources;
import org.zanata.webtrans.client.ui.TextContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitProvidesKey;
import org.zanata.webtrans.shared.util.StringNotEmptyPredicate;
import com.google.common.collect.Iterables;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;

public class ReviewView2 extends Composite implements ReviewDisplay
{
   private static final int PAGE_SIZE = 50;
   private static final CellTableResources CELL_TABLE_RESOURCES = GWT.create(CellTableResources.class);
   private static final AbstractCell<List<String>> STRING_LIST_CELL = new AbstractCell<List<String>>()
   {
      @Override
      public void render(Context context, List<String> contents, SafeHtmlBuilder sb)
      {
         Iterable<String> notEmptyContents = Iterables.filter(contents, StringNotEmptyPredicate.INSTANCE);
         SafeHtml safeHtml = TextContentsDisplay.asSyntaxHighlight(notEmptyContents).toSafeHtml();
         sb.appendHtmlConstant(safeHtml.asString());
      }
   };

   private static ReviewViewUiBinder ourUiBinder = GWT.create(ReviewViewUiBinder.class);
   @UiField
   WebTransMessages messages;

   private final CellTable<TransUnit> table;
   private Listener listener;
   private final VerticalPanel root;

   public ReviewView2()
   {
      root = ourUiBinder.createAndBindUi(this);

      table = createTable();
      SimplePager pager = new SimplePager();
      pager.setDisplay(table);

      root.add(table);
      root.add(pager);
   }

   @Override
   public Widget asWidget()
   {
      return root;
   }

//   @Override
   public void setSelectionModel(SelectionModel<TransUnit> multiSelectionModel)
   {
      table.setSelectionModel(multiSelectionModel, DefaultSelectionEventManager.<TransUnit>createCheckboxManager());
      Column<TransUnit, Boolean> checkboxColumn = createCheckboxColumn(multiSelectionModel);
      table.insertColumn(0, checkboxColumn);
      table.setColumnWidth(checkboxColumn, 10, Style.Unit.PX);
   }

//   @Override
   public void setDataProvider(ListDataProvider<TransUnit> dataProvider)
   {
      dataProvider.addDataDisplay(table);
   }

   @Override
   public void setListener(Listener listener)
   {
      this.listener = listener;
   }

   private CellTable<TransUnit> createTable()
   {
      CellTable<TransUnit> transUnitTable = new CellTable<TransUnit>(PAGE_SIZE, CELL_TABLE_RESOURCES, TransUnitProvidesKey.KEY_PROVIDER);
      transUnitTable.setEmptyTableWidget(new Label(messages.noContent()));
      transUnitTable.setLoadingIndicator(new Label(messages.loading()));

      Column<TransUnit, List<String>> sourceColumn = createSourceColumn();
      Column<TransUnit, List<String>> targetColumn = createTargetColumn();
//      Column<TransUnit, String> modifiedByColumn = createModifiedByColumn();
//      Column<TransHistoryItem, String> modifiedDateColumn = createModifiedDateColumn();


      transUnitTable.addColumn(sourceColumn, messages.source());
      transUnitTable.setColumnWidth(sourceColumn, 50, Style.Unit.PCT);

      transUnitTable.addColumn(targetColumn, messages.target());
      transUnitTable.setColumnWidth(targetColumn, 50, Style.Unit.PCT);

//      transUnitTable.addColumn(modifiedByColumn, messages.modifiedBy());
//      transUnitTable.setColumnWidth(modifiedByColumn, 10, Style.Unit.PCT);

//      transUnitTable.addColumn(modifiedDateColumn, messages.modifiedDate());
//      transUnitTable.setColumnWidth(modifiedDateColumn, 20, Style.Unit.PCT);

      return transUnitTable;
   }

   private static Column<TransUnit, List<String>> createSourceColumn()
   {
      return new Column<TransUnit, List<String>>(STRING_LIST_CELL)
      {
         @Override
         public List<String> getValue(TransUnit transUnit)
         {
            return transUnit.getSources();
         }
      };
   }

   private static Column<TransUnit, List<String>> createTargetColumn()
   {
      return new Column<TransUnit, List<String>>(STRING_LIST_CELL)
      {
         @Override
         public List<String> getValue(TransUnit transUnit)
         {
            return transUnit.getTargets();
         }

         @Override
         public String getCellStyleNames(Cell.Context context, TransUnit transUnit)
         {
            //TODO do we need state decorator since we are working on approved strings only
//            String styleNames = Strings.nullToEmpty(super.getCellStyleNames(context, transUnit));
//            if (transUnit.getStatus() == ContentState.Approved)
//            {
//               styleNames += " ApprovedStateDecoration";
//            }
//            else if (transUnit.getStatus() == ContentState.NeedReview)
//            {
//               styleNames += " FuzzyStateDecoration";
//            }
//            return styleNames;
            return null;
         }
      };
   }

   private static Column<TransUnit, Boolean> createCheckboxColumn(final SelectionModel<TransUnit> selectionModel)
   {
      return new Column<TransUnit, Boolean>(new CheckboxCell(true, false))
      {
         @Override
         public Boolean getValue(TransUnit object) {
            // Get the value from the selection model.
            return selectionModel.isSelected(object);
         }
      };
   }

   interface ReviewViewUiBinder extends UiBinder<VerticalPanel, ReviewView2>
   {
   }
}