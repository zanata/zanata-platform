package org.zanata.webtrans.client.ui;

import java.util.Collections;
import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import com.google.common.base.Strings;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.AbstractDataProvider;
import com.google.gwt.view.client.Range;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class TranslationHistoryView extends DialogBox implements TranslationHistoryDisplay
{
   public static final int PAGE_SIZE = 5;
   private final CellTable<TransHistoryItem> historyTable;
   private static CellTableResources cellTableResources = GWT.create(CellTableResources.class);
   private AbstractDataProvider<TransHistoryItem> dataProvider;

   @Inject
   public TranslationHistoryView(WebTransMessages messages)
   {
      super(true, true);
      setGlassEnabled(true);
      //TODO localise
      getCaption().setText("Translation History");

      historyTable = new CellTable<TransHistoryItem>(PAGE_SIZE, cellTableResources, HISTORY_ITEM_PROVIDES_KEY);
      historyTable.setLoadingIndicator(new Label(messages.loading()));
      historyTable.setEmptyTableWidget(new Label(messages.noContent()));
      Column<TransHistoryItem, String> verColumn = createVersionColumn();
      Column<TransHistoryItem, List<String>> contentsColumn = createContentsColumn();
      Column<TransHistoryItem, String> modifiedByColumn = createModifiedByColumn();
      Column<TransHistoryItem, String> modifiedDateColumn = createModifiedDateColumn();
      historyTable.addColumn(verColumn, messages.versionNumber());
      historyTable.addColumn(contentsColumn, messages.target());
      historyTable.addColumn(modifiedByColumn, messages.modifiedBy());
      historyTable.addColumn(modifiedDateColumn, messages.modifiedDate());

      SimplePager simplePager = new SimplePager();
      simplePager.setDisplay(historyTable);

      VerticalPanel container = new VerticalPanel();
      container.add(historyTable);
      container.add(simplePager);
      setWidget(container);
   }

   @Override
   public void setDataProvider(AbstractDataProvider<TransHistoryItem> dataProvider)
   {
      this.dataProvider = dataProvider;
      dataProvider.addDataDisplay(historyTable);
   }

   @Override
   public void resetPage()
   {
      historyTable.setPageStart(0);
   }

   @Override
   public void hide()
   {
      dataProvider.removeDataDisplay(historyTable);
      super.hide();
   }

   private static Column<TransHistoryItem, String> createVersionColumn()
   {
      return new Column<TransHistoryItem, String>(new TextCell())
      {
         @Override
         public String getValue(TransHistoryItem historyItem)
         {
            return historyItem.getVersionNum();
         }
      };
   }

   private static Column<TransHistoryItem, List<String>> createContentsColumn()
   {
      return new Column<TransHistoryItem, List<String>>(new AbstractCell<List<String>>()
      {
         @Override
         public void render(Context context, List<String> contents, SafeHtmlBuilder sb)
         {

            for (String source : contents)
            {
               HighlightingLabel label = new HighlightingLabel(source);
               appendContent(sb, label.getElement().getString());
            }
         }
      })
      {
         @Override
         public List<String> getValue(TransHistoryItem historyItem)
         {
            return historyItem.getContents();
         }

         @Override
         public String getCellStyleNames(Cell.Context context, TransHistoryItem historyItem)
         {
            String styleNames = Strings.nullToEmpty(super.getCellStyleNames(context, historyItem));
            if (historyItem.getStatus() == ContentState.Approved)
            {
               styleNames += " ApprovedStateDecoration";
            }
            else if (historyItem.getStatus() == ContentState.NeedReview)
            {
               styleNames += " FuzzyStateDecoration";
            }
            return styleNames;
         }
      };
   }

   private static Column<TransHistoryItem, String> createModifiedByColumn()
   {
      return new Column<TransHistoryItem, String>(new TextCell())
      {
         @Override
         public String getValue(TransHistoryItem historyItem)
         {
            return historyItem.getModifiedBy();
         }
      };
   }

   private static Column<TransHistoryItem, String> createModifiedDateColumn()
   {
      return new Column<TransHistoryItem, String>(new TextCell())
      {
         @Override
         public String getValue(TransHistoryItem historyItem)
         {
            return historyItem.getModifiedDate();
         }
      };
   }

   private static void appendContent(SafeHtmlBuilder sb, String content)
   {
      sb.appendHtmlConstant("<div class='translationContainer' style='border-bottom: dotted 1px grey;'>").appendHtmlConstant(content).appendHtmlConstant("</div>");
   }
}