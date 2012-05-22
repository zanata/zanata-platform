package org.zanata.webtrans.client.ui;

import java.util.Collection;
import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.presenter.ReplacementState;
import org.zanata.webtrans.client.presenter.TransUnitReplaceInfo;
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
   private Column<TransUnitReplaceInfo, TransUnitReplaceInfo> targetColumn;
   private ReplaceActionColumn replaceButtonColumn;


   public static void setHighlightString(String highlightString)
   {
      SearchResultsDocumentTable.highlightString = highlightString;
   }


   public SearchResultsDocumentTable(Delegate<TransUnitReplaceInfo> previewDelegate,
         Delegate<TransUnitReplaceInfo> replaceDelegate,
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
      replaceButtonColumn = new ReplaceActionColumn(previewDelegate, replaceDelegate, undoDelegate);

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
   private static Column<TransUnitReplaceInfo, TransUnitReplaceInfo> buildTargetColumn()
   {
      return new Column<TransUnitReplaceInfo, TransUnitReplaceInfo>(new AbstractCell<TransUnitReplaceInfo>()
      {
         @Override
         public void render(Context context, TransUnitReplaceInfo info, SafeHtmlBuilder sb)
         {
            // TODO for replaced targets, highlight replacement term or show diff
            List<String> contents = info.getTransUnit().getTargets();
            if (info.getState() == ReplacementState.PreviewAvailable)
            {
               for (int i = 0; i < contents.size(); i++)
               {
                  DiffMatchPatchLabel label = new DiffMatchPatchLabel();
                  label.setOriginal(contents.get(i));
                  label.setText(info.getPreview().getContents().get(i));
                  appendContent(sb, label.getElement().getString());
               }
            }
            else
            {

               for (String target : contents)
               {
                  HighlightingLabel label = new HighlightingLabel(target);
                  if (!Strings.isNullOrEmpty(highlightString))
                  {
                     label.highlightSearch(highlightString);
                  }
                  appendContent(sb, label.getElement().getString());
               }
            }
         }
      })
      {

         @Override
         public TransUnitReplaceInfo getValue(TransUnitReplaceInfo info)
         {
            return info;
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

      public ReplaceActionColumn(Delegate<TransUnitReplaceInfo> previewDelegate, Delegate<TransUnitReplaceInfo> replaceDelegate, Delegate<TransUnitReplaceInfo> undoDelegate)
      {
         super(new ReplaceActionCell(previewDelegate, replaceDelegate, undoDelegate));
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
      private Delegate<TransUnitReplaceInfo> previewDelegate;

      private SafeHtml spinner;
      private final SafeHtml previewHtml;
      private final SafeHtml fetchingPreviewHtml;
      private final SafeHtml replacingHtml;
      private final SafeHtml undoHtml;
      private final SafeHtml undoingHtml;

      public ReplaceActionCell(Delegate<TransUnitReplaceInfo> previewDelegate, Delegate<TransUnitReplaceInfo> replaceDelegate, Delegate<TransUnitReplaceInfo> undoDelegate) {
         super(SafeHtmlUtils.fromString(messages.replace()), replaceDelegate);
         this.previewDelegate = previewDelegate;
         this.undoDelegate = undoDelegate;
         spinner = new ImageResourceRenderer().render(resources.spinner());
         fetchingPreviewHtml = buildProcessingIndicator(messages.fetchingPreview());
         replacingHtml = buildProcessingIndicator(messages.replacing());
         undoingHtml = buildProcessingIndicator(messages.undoInProgress());
         previewHtml = buildButtonHtml("", messages.fetchPreview());
         undoHtml = buildButtonHtml(messages.replaced(), messages.undo());
      }

      public SafeHtml buildButtonHtml(String message, String buttonLabel)
      {
         return new SafeHtmlBuilder()
               .appendHtmlConstant(message)
               .appendHtmlConstant("<button type=\"button\" tabindex=\"-1\">")
               .appendHtmlConstant(buttonLabel)
               .appendHtmlConstant("</button>")
               .toSafeHtml();
      }

      public SafeHtml buildProcessingIndicator(String message)
      {
         return new SafeHtmlBuilder()
               .append(spinner)
               .appendHtmlConstant("<br/>")
               .appendHtmlConstant(message)
               .toSafeHtml();
      }

      @Override
      public void render(com.google.gwt.cell.client.Cell.Context context, TransUnitReplaceInfo value, SafeHtmlBuilder sb)
      {
         switch (value.getState())
         {
         case FetchingPreview:
            sb.append(fetchingPreviewHtml);
            break;
         case Replacing:
            sb.append(replacingHtml);
            break;
         case Undoing:
            sb.append(undoingHtml);
            break;
         case PreviewAvailable:
            super.render(context, value, sb);
            break;
            // TODO these two cases will depend on quick-replace mode
         case Replaceable:
            sb.append(previewHtml);
            break;
         case Replaced:
            sb.append(undoHtml);
            break;
         }
      }

      @Override
      protected void onEnterKeyDown(Context context, Element parent, TransUnitReplaceInfo value, NativeEvent event, ValueUpdater<TransUnitReplaceInfo> valueUpdater) {
         switch (value.getState())
         {
         case Replaceable:
            previewDelegate.execute(value);
            break;
         case Replaced:
            undoDelegate.execute(value);
            break;
         case PreviewAvailable:
            super.onEnterKeyDown(context, parent, value, event, valueUpdater);
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
