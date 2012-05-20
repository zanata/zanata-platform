package org.zanata.webtrans.client.ui;

import java.util.Collection;
import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.presenter.SearchResultsPresenter.TransUnitReplaceInfo;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.DefaultSelectionEventManager.BlacklistEventTranslator;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;

public class SearchResultsDocumentTable extends CellTable<TransUnitReplaceInfo>
{
   private static final int REPLACE_COLUMN_INDEX = 3;

   private static CellTableResources cellTableResources;
   private org.zanata.webtrans.client.resources.Resources resources;
   private WebTransMessages messages;

   private static DefaultSelectionEventManager<TransUnitReplaceInfo> selectionManager = null;

   private static String highlightString = null;

   private TextColumn<TransUnitReplaceInfo> rowIndexColumn;
   private Column<TransUnitReplaceInfo, List<String>> sourceColumn;
   private Column<TransUnitReplaceInfo, List<String>> targetColumn;
   private ReplaceActionColumn replaceButtonColumn;


   public static void setHighlightString(String highlightString)
   {
      SearchResultsDocumentTable.highlightString = highlightString;
   }


   public SearchResultsDocumentTable(Delegate<TransUnitReplaceInfo> replaceDelegate,
         Delegate<TransUnitReplaceInfo> undoDelegate,
         final SelectionModel<TransUnitReplaceInfo> selectionModel,
         ValueChangeHandler<Boolean> selectAllHandler,
         final WebTransMessages messages,
         org.zanata.webtrans.client.resources.Resources resources)
   {
      super(15, getCellTableResources());

      this.messages = messages;
      this.resources = resources;

      rowIndexColumn = buildRowIndexColumn();
      sourceColumn = buildSourceColumn();
      targetColumn = buildTargetColumn();
      replaceButtonColumn = new ReplaceActionColumn(replaceDelegate, undoDelegate);

      setWidth("100%", true);

      addColumn(rowIndexColumn, messages.rowIndex());
      addColumn(sourceColumn, messages.source());
      addColumn(targetColumn, messages.target());
      addColumn(replaceButtonColumn, messages.actions());

      setColumnWidth(rowIndexColumn, 70.0, Unit.PX);
      setColumnWidth(sourceColumn, 50.0, Unit.PCT);
      setColumnWidth(targetColumn, 50.0, Unit.PCT);
      setColumnWidth(replaceButtonColumn, 100.0, Unit.PX);

      sourceColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
      targetColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);

      setSelectionModel(selectionModel, getSelectionManager());

      addStyleName("projectWideSearchResultsDocumentBody");
   }


   /**
    * @return a column that displays the 1-based index of the text flow
    */
   private static TextColumn<TransUnitReplaceInfo> buildRowIndexColumn()
   {
      return new TextColumn<TransUnitReplaceInfo>()
      {
         @Override
         public String getValue(TransUnitReplaceInfo tu)
         {
            return Integer.toString(tu.getTransUnit().getRowIndex() + 1);
         }
      };
   }


   /**
    * @return a column that displays the source contents for the text flow
    */
   private static Column<TransUnitReplaceInfo, List<String>> buildSourceColumn()
   {
      return new Column<TransUnitReplaceInfo, List<String>>(new AbstractCell<List<String>>()
      {
         @Override
         public void render(Context context, List<String> contents, SafeHtmlBuilder sb)
         {

            for (String source : notEmptyContents(contents))
            {
               HighlightingLabel label = new HighlightingLabel(source);
               if (!Strings.isNullOrEmpty(highlightString))
               {
                  label.highlightSearch(highlightString);
               }
               appendContent(sb, label.getElement().getString());
            }
         }
      })
      {
         @Override
         public List<String> getValue(TransUnitReplaceInfo info)
         {
            return info.getTransUnit().getSources();
         }
      };
   }

   /**
    * @return a column that displays the target contents for the text flow
    */
   private static Column<TransUnitReplaceInfo, List<String>> buildTargetColumn()
   {
      return new Column<TransUnitReplaceInfo, List<String>>(new AbstractCell<List<String>>()
      {
         @Override
         public void render(Context context, List<String> contents, SafeHtmlBuilder sb)
         {

            for (String target : notEmptyContents(contents))
            {
               // TODO switch to diff mode if this is a preview or there has
               // been a replacement
               HighlightingLabel label = new HighlightingLabel(target);
               if (!Strings.isNullOrEmpty(highlightString))
               {
                  label.highlightSearch(highlightString);
               }
               appendContent(sb, label.getElement().getString());
            }
         }
      })
      {

         @Override
         public List<String> getValue(TransUnitReplaceInfo info)
         {
            return info.getTransUnit().getTargets();
         }

         @Override
         public String getCellStyleNames(Context context, TransUnitReplaceInfo info)
         {
            String styleNames = Strings.nullToEmpty(super.getCellStyleNames(context, info));
            if (info.getTransUnit().getStatus() == ContentState.Approved)
            {
               styleNames += " ApprovedStateDecoration";
            }
            else if (info.getTransUnit().getStatus() == ContentState.NeedReview)
            {
               styleNames += " FuzzyStateDecoration";
            }
            return styleNames;
         }
      };
   }


   private class ReplaceActionColumn extends Column<TransUnitReplaceInfo, TransUnitReplaceInfo>
   {

      public ReplaceActionColumn(Delegate<TransUnitReplaceInfo> replaceDelegate, Delegate<TransUnitReplaceInfo> undoDelegate)
      {
         super(new ReplaceActionCell(replaceDelegate, undoDelegate));
         this.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
         this.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
      }

      @Override
      public TransUnitReplaceInfo getValue(TransUnitReplaceInfo info)
      {
         return info;
      }
   }

   private class ReplaceActionCell extends ActionCell<TransUnitReplaceInfo>
   {
      private Delegate<TransUnitReplaceInfo> undoDelegate;
      private final SafeHtml replacingHtml;
      private final SafeHtml undoHtml;
      private final SafeHtml undoingHtml;

      public ReplaceActionCell(Delegate<TransUnitReplaceInfo> actionDelegate, Delegate<TransUnitReplaceInfo> undoDelegate) {
         super(SafeHtmlUtils.fromString(messages.replace()), actionDelegate);
         this.undoDelegate = undoDelegate;
         ImageResourceRenderer renderer = new ImageResourceRenderer();
         SafeHtml spinner = renderer.render(resources.spinner());
         replacingHtml = new SafeHtmlBuilder().append(spinner)
               .appendHtmlConstant("<br/>")
               .appendHtmlConstant(messages.replacing())
               .toSafeHtml();
         this.undoHtml = new SafeHtmlBuilder()
               .appendHtmlConstant(messages.replaced())
               .appendHtmlConstant("<button type=\"button\" tabindex=\"-1\">")
               .appendHtmlConstant(messages.undo())
               .appendHtmlConstant("</button>")
               .toSafeHtml();
         this.undoingHtml = new SafeHtmlBuilder().append(spinner)
               .appendHtmlConstant("<br/>")
               .appendHtmlConstant(messages.undoInProgress())
               .toSafeHtml();
      }

      @Override
      public void render(com.google.gwt.cell.client.Cell.Context context, TransUnitReplaceInfo value, SafeHtmlBuilder sb)
      {
         switch (value.getState())
         {
         case Replaceable:
            super.render(context, value, sb);
            break;
         case Replacing:
            sb.append(replacingHtml);
            break;
         case Replaced:
            sb.append(undoHtml);
            break;
         case Undoing:
            sb.append(undoingHtml);
            break;
         }
      }

      @Override
      protected void onEnterKeyDown(Context context, Element parent, TransUnitReplaceInfo value, NativeEvent event, ValueUpdater<TransUnitReplaceInfo> valueUpdater) {
         switch (value.getState())
         {
         case Replaceable:
            super.onEnterKeyDown(context, parent, value, event, valueUpdater);
            break;
         case Replaced:
            undoDelegate.execute(value);
            break;
         }
         // else ignore (is processing)
      };
   }

   private static DefaultSelectionEventManager<TransUnitReplaceInfo> getSelectionManager()
   {
      if (selectionManager == null)
      {
         selectionManager = buildSelectionManager();
      }
      return selectionManager;
   }

   /**
    * build a selection manager that toggles selection when the row is clicked,
    * but suppresses selection when clicks are in the action button column to
    * prevent undesired selections when using action buttons
    * 
    * @return the selection manager
    */
   private static DefaultSelectionEventManager<TransUnitReplaceInfo> buildSelectionManager()
   {
      BlacklistEventTranslator<TransUnitReplaceInfo> selectionEventTranslator = new BlacklistEventTranslator<TransUnitReplaceInfo>()
      {
         @Override
         public SelectAction translateSelectionEvent(CellPreviewEvent<TransUnitReplaceInfo> event)
         {
            return isColumnBlacklisted(event.getColumn()) ? SelectAction.IGNORE : SelectAction.TOGGLE;
         }
      };
      selectionEventTranslator.setColumnBlacklisted(REPLACE_COLUMN_INDEX, true);
      return DefaultSelectionEventManager.<TransUnitReplaceInfo> createCustomManager(selectionEventTranslator);
   }

   private static CellTableResources getCellTableResources()
   {
      if (cellTableResources == null)
      {
         cellTableResources = GWT.create(CellTableResources.class);
      }
      return cellTableResources;
   }

   private static void appendContent(SafeHtmlBuilder sb, String content)
   {
      sb.appendHtmlConstant("<div class='translationContainer' style='border-bottom: dotted 1px grey;'>").appendHtmlConstant(content).appendHtmlConstant("</div>");
   }

   private static Collection<String> notEmptyContents(List<String> contents)
   {
      return Collections2.filter(contents, new Predicate<String>()
      {
         @Override
         public boolean apply(String input)
         {
            return !Strings.isNullOrEmpty(input);
         }
      });
   }

}
