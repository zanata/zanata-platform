/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.ui;

import java.util.Collection;
import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.presenter.PreviewState;
import org.zanata.webtrans.client.presenter.TransUnitReplaceInfo;
import org.zanata.webtrans.client.resources.WebTransMessages;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Collections2;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.ActionCell.Delegate;
import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.ImageResourceRenderer;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.DefaultSelectionEventManager.BlacklistEventTranslator;
import com.google.gwt.view.client.DefaultSelectionEventManager.SelectAction;
import com.google.gwt.view.client.SelectionModel;

/**
 * Displays search results for a single document.
 * 
 * @author David Mason, <a href="mailto:damason@redhat.com">damason@redhat.com</a>
 *
 */
public class SearchResultsDocumentTable extends CellTable<TransUnitReplaceInfo>
{
   private static final int PREVIEW_COLUMN_INDEX = 4;
   private static final int REPLACE_COLUMN_INDEX = 5;

   private static CellTableResources cellTableResources;
   private WebTransMessages messages;
   private static SafeHtml spinner;

   private static DefaultSelectionEventManager<TransUnitReplaceInfo> selectionManager = null;

   private static String highlightString = null;
   private static boolean requirePreview = true;

   private CheckColumn checkboxColumn;
   private TextColumn<TransUnitReplaceInfo> rowIndexColumn;
   private Column<TransUnitReplaceInfo, List<String>> sourceColumn;
   private Column<TransUnitReplaceInfo, TransUnitReplaceInfo> targetColumn;
   private ActionColumn previewButtonColumn;
   private ActionColumn replaceButtonColumn;
   
   private CheckboxHeader checkboxColumnHeader;


   public static void setHighlightString(String highlightString)
   {
      SearchResultsDocumentTable.highlightString = highlightString;
   }

   public static void setRequirePreview(boolean required)
   {
      requirePreview = required;
   }

   /**
    * Create a standard result document table with no action buttons.
    * 
    * Clicks on any cells will toggle selection.
    * 
    * @param selectionModel
    * @param selectAllHandler handler for events for the selection column header
    *           checkbox
    * @param messages
    */
   public SearchResultsDocumentTable(final SelectionModel<TransUnitReplaceInfo> selectionModel,
         ValueChangeHandler<Boolean> selectAllHandler,
         final WebTransMessages messages)
   {
      super(15, getCellTableResources());

      this.messages = messages;

      setWidth("100%", true);
      setSelectionModel(selectionModel, getSelectionManager());
      addStyleName("projectWideSearchResultsDocumentBody");

      checkboxColumn = new CheckColumn(selectionModel);
      checkboxColumnHeader = new CheckboxHeader();
      checkboxColumnHeader.addValueChangeHandler(selectAllHandler);
      rowIndexColumn = buildRowIndexColumn();
      sourceColumn = buildSourceColumn();
      targetColumn = buildTargetColumn();

      addColumn(checkboxColumn, checkboxColumnHeader);
      addColumn(rowIndexColumn, messages.rowIndex());
      addColumn(sourceColumn, messages.source());
      addColumn(targetColumn, messages.target());

      setColumnWidth(checkboxColumn, 50.0, Unit.PX);
      setColumnWidth(rowIndexColumn, 70.0, Unit.PX);
      setColumnWidth(sourceColumn, 50.0, Unit.PCT);
      setColumnWidth(targetColumn, 50.0, Unit.PCT);

      sourceColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
      targetColumn.setVerticalAlignment(HasVerticalAlignment.ALIGN_TOP);
   }

   /**
    * Create a result document table with action buttons for each row.
    * 
    * Action button display is based on
    * {@link TransUnitReplaceInfo#getPreviewState()} and
    * {@link TransUnitReplaceInfo#getReplaceState()}, but Click and Enter-Key
    * events will be generated regardless of display, so delegates must check
    * for appropriate states.
    * 
    * Clicks on action button cells will not change selection. Clicks on other
    * cells will toggle selection.
    * 
    * @param previewDelegate handles clicks of 'preview' button
    * @param replaceDelegate handles clicks of 'replace' button
    * @param undoDelegate handles clicks of 'undo' button
    * @param selectionModel
    * @param selectAllHandler handles events for the selection column header
    *           checkbox
    * @param messages
    * @param resources
    * 
    * @see #SearchResultsDocumentTable(SelectionModel, ValueChangeHandler,
    *      WebTransMessages)
    */
   public SearchResultsDocumentTable(Delegate<TransUnitReplaceInfo> previewDelegate,
         Delegate<TransUnitReplaceInfo> replaceDelegate,
         Delegate<TransUnitReplaceInfo> undoDelegate,
         final SelectionModel<TransUnitReplaceInfo> selectionModel,
         ValueChangeHandler<Boolean> selectAllHandler,
         final WebTransMessages messages,
         org.zanata.webtrans.client.resources.Resources resources)
   {
      this(selectionModel, selectAllHandler, messages);

      if (spinner == null)
      {
         spinner = new ImageResourceRenderer().render(resources.spinner());
      }

      previewButtonColumn = new ActionColumn(new PreviewActionCell(previewDelegate));
      replaceButtonColumn = new ActionColumn(new ReplaceActionCell(replaceDelegate, undoDelegate));

      // preview header refers to both these columns
      addColumn(previewButtonColumn, messages.actions());
      addColumn(replaceButtonColumn);

      setColumnWidth(previewButtonColumn, 85.0, Unit.PX);
      setColumnWidth(replaceButtonColumn, 85.0, Unit.PX);
   }

   // TODO add focus tracking field to allow current-document type interactions
   // listeners may need to be attached in view

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
            if (info.getPreviewState() == PreviewState.Show)
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

   private class CheckColumn extends Column<TransUnitReplaceInfo, Boolean>
   {

      private SelectionModel<TransUnitReplaceInfo> selectionModel;

      public CheckColumn(SelectionModel<TransUnitReplaceInfo> selectionModel)
      {
         super(new CheckboxCell(true, false));
         this.selectionModel = selectionModel;
      }

      @Override
      public Boolean getValue(TransUnitReplaceInfo info)
      {
         return selectionModel.isSelected(info);
      }

   }

   private class CheckboxHeader extends Header<Boolean> implements HasValue<Boolean> {

      private boolean checked;
      private HandlerManager handlerManager;

      public CheckboxHeader()
      {
         super(new CheckboxCell());
         checked = false;
      }

      // This method is invoked to pass the value to the CheckboxCell's render method
      @Override
      public Boolean getValue()
      {
         return checked;
      }

      @Override
      public void onBrowserEvent(Context context, Element elem, NativeEvent nativeEvent)
      {
         int eventType = Event.as(nativeEvent).getTypeInt();
         if (eventType == Event.ONCHANGE)
         {
            nativeEvent.preventDefault();
            //use value setter to easily fire change event to handlers
            setValue(!checked, true);
         }
      }

      @Override
      public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Boolean> handler)
      {
         return ensureHandlerManager().addHandler(ValueChangeEvent.getType(), handler);
      }

      @Override
      public void fireEvent(GwtEvent<?> event)
      {
         ensureHandlerManager().fireEvent(event);
      }

      @Override
      public void setValue(Boolean value)
      {
         checked = value;
      }

      @Override
      public void setValue(Boolean value, boolean fireEvents)
      {
         checked = value;
         if (fireEvents)
         {
            ValueChangeEvent.fire(this, value);
         }
      }

      private HandlerManager ensureHandlerManager()
      {
         if (handlerManager == null)
         {
            handlerManager = new HandlerManager(this);
         }
         return handlerManager;
      }
   }

   private class ActionColumn extends Column<TransUnitReplaceInfo, TransUnitReplaceInfo>
   {

      public ActionColumn(ActionCell<TransUnitReplaceInfo> actionCell)
      {
         super(actionCell);
         this.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
         this.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
         this.setCellStyleNames("projectWideSearchResultsActionColumn");
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

      private final SafeHtml disabledReplaceHtml;
      private final SafeHtml replacingHtml;
      private final SafeHtml undoHtml;
      private final SafeHtml undoingHtml;

      public ReplaceActionCell(Delegate<TransUnitReplaceInfo> replaceDelegate, Delegate<TransUnitReplaceInfo> undoDelegate) {
         super(SafeHtmlUtils.fromString(messages.replace()), replaceDelegate);
         this.undoDelegate = undoDelegate;
         disabledReplaceHtml = buildButtonHtml("", messages.replace(), messages.previewRequiredBeforeReplace(), false);
         replacingHtml = buildProcessingIndicator(messages.replacing());
         undoingHtml = buildProcessingIndicator(messages.undoInProgress());
         undoHtml = buildButtonHtml(messages.replaced(), messages.undo());
      }

      @Override
      public void render(com.google.gwt.cell.client.Cell.Context context, TransUnitReplaceInfo value, SafeHtmlBuilder sb)
      {
         switch (value.getReplaceState())
         {
         case NotReplaced:
            if (!requirePreview || value.getPreviewState() == PreviewState.Show || value.getPreviewState() == PreviewState.Hide)
            {
               super.render(context, value, sb);
            }
            else
            {
               sb.append(disabledReplaceHtml);
            }
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
      protected void onEnterKeyDown(Context context, Element parent, TransUnitReplaceInfo value, NativeEvent event, ValueUpdater<TransUnitReplaceInfo> valueUpdater)
      {
         switch (value.getReplaceState())
         {
         case NotReplaced:
            if (!requirePreview || value.getPreviewState() == PreviewState.Show || value.getPreviewState() == PreviewState.Hide)
            {
               super.onEnterKeyDown(context, parent, value, event, valueUpdater);
            }
            break;
         case Replaced:
            undoDelegate.execute(value);
            break;
         }
         // else ignore (is processing)
      };
   }

   private class PreviewActionCell extends ActionCell<TransUnitReplaceInfo>
   {
      private Delegate<TransUnitReplaceInfo> previewDelegate;

      private final SafeHtml fetchingPreviewHtml;
      private final SafeHtml hidePreviewHtml;

      public PreviewActionCell(Delegate<TransUnitReplaceInfo> previewDelegate) {
         super(SafeHtmlUtils.fromString(messages.fetchPreview()), previewDelegate);
         this.previewDelegate = previewDelegate;
         fetchingPreviewHtml = buildProcessingIndicator(messages.fetchingPreview());
         hidePreviewHtml = buildButtonHtml("", messages.hidePreview());
      }

      @Override
      public void render(com.google.gwt.cell.client.Cell.Context context, TransUnitReplaceInfo value, SafeHtmlBuilder sb)
      {

         switch (value.getPreviewState())
         {
         case NotFetched:
         case Hide:
            super.render(context, value, sb);
            break;
         case Fetching:
            sb.append(fetchingPreviewHtml);
            break;
         case Show:
            sb.append(hidePreviewHtml);
            break;
         case NotAllowed:
            // empty cell
            break;
         }
      }

      @Override
      protected void onEnterKeyDown(Context context, Element parent, TransUnitReplaceInfo value, NativeEvent event, ValueUpdater<TransUnitReplaceInfo> valueUpdater) {
         // delegate is responsible for taking appropriate action
         previewDelegate.execute(value);
      };
   }

   private static SafeHtml buildButtonHtml(String message, String buttonLabel)
   {
      return buildButtonHtml(message, buttonLabel, "", true);
   }

   private static SafeHtml buildButtonHtml(String message, String buttonLabel, String title, boolean enabled)
   {
      SafeHtmlBuilder sb = new  SafeHtmlBuilder()
            .appendHtmlConstant(message)
            .appendHtmlConstant("<button type=\"button\" tabindex=\"-1\"");
      if (!enabled)
      {
         sb.appendHtmlConstant(" title=\"")
            .appendEscaped(title)
            .appendHtmlConstant("\" disabled=\"disabled\"");
      }
      return sb
            .appendHtmlConstant(">")
            .appendEscaped(buttonLabel)
            .appendHtmlConstant("</button>")
            .toSafeHtml();
   }

   private static SafeHtml buildProcessingIndicator(String message)
   {
      return new SafeHtmlBuilder()
            .append(spinner)
            .appendHtmlConstant("<br/>")
            .appendHtmlConstant(message)
            .toSafeHtml();
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
      selectionEventTranslator.setColumnBlacklisted(PREVIEW_COLUMN_INDEX, true);
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

   public HasValue<Boolean> getCheckbox()
   {
      return checkboxColumnHeader;
   }
}
