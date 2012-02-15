package org.zanata.webtrans.client.view;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.TransMemoryCopyEvent;
import org.zanata.webtrans.client.presenter.TransMemoryDetailsPresenter;
import org.zanata.webtrans.client.presenter.TransMemoryPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.DiffMatchPatchLabel;
import org.zanata.webtrans.client.ui.DocumentNode;
import org.zanata.webtrans.client.ui.EnumListBox;
import org.zanata.webtrans.client.ui.HighlightingLabel;
import org.zanata.webtrans.client.ui.SearchTypeRenderer;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory.SearchType;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.ClickableTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;

public class TransMemoryView extends Composite implements TransMemoryPresenter.Display
{

   private static final int CELL_PADDING = 5;
   private static final int HEADER_ROW = 0;
   private static final int SOURCE_COL = 0;
   private static final int TARGET_COL = 1;
   private static final int SIMILARITY_COL = 2;
   private static final int INFO_COL = 3;
   private static final int ACTION_COL = 4;

   private static TransMemoryViewUiBinder uiBinder = GWT.create(TransMemoryViewUiBinder.class);

   interface TransMemoryViewUiBinder extends UiBinder<Widget, TransMemoryView>
   {
   }

   @UiField
   TextBox tmTextBox;

   @UiField
   Button searchButton;

   @UiField(provided = true)
   ValueListBox<SearchType> searchType;

   @UiField
   Button clearButton;

   // @UiField
   // FlexTable resultTable;

   @UiField(provided = true)
   CellTable<TranslationMemoryGlossaryItem> tmTable;

   @Inject
   private EventBus eventBus;

   @Inject
   private TransMemoryDetailsPresenter tmInfoPresenter;

   private static Resources resources;
   private boolean isFocused;
   private boolean showCopyLinks;

   private List<TranslationMemoryGlossaryItem> cachedMem = new ArrayList<TranslationMemoryGlossaryItem>();

   private UiMessages messages;
   private ListDataProvider<TranslationMemoryGlossaryItem> dataProvider;

   @Inject
   public TransMemoryView(final UiMessages messages, SearchTypeRenderer searchTypeRenderer, Resources resources)
   {
      this.resources = resources;
      this.messages = messages;

      searchType = new EnumListBox<SearchType>(SearchType.class, searchTypeRenderer);
      initTable();
      dataProvider = new ListDataProvider<TranslationMemoryGlossaryItem>();
      dataProvider.addDataDisplay(tmTable);
      initWidget(uiBinder.createAndBindUi(this));

      clearButton.setText(messages.clearButtonLabel());
      searchButton.setText(messages.searchButtonLabel());
      showCopyLinks = true;
   }

   @UiHandler("tmTextBox")
   void onTmTextBoxKeyUp(KeyUpEvent event)
   {
      if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER)
      {
         searchButton.click();
      }
   }

   @UiHandler("tmTextBox")
   public void onTmTextBoxFocus(FocusEvent event)
   {
      isFocused = true;
   }

   @UiHandler("tmTextBox")
   public void onTmTextBoxBlur(BlurEvent event)
   {
      isFocused = false;
   }

   @UiHandler("clearButton")
   void onClearButtonClicked(ClickEvent event)
   {
      tmTextBox.setText("");
      clearResults();
   }

   @Override
   public Button getSearchButton()
   {
      return searchButton;
   }

   @Override
   public HasValue<SearchType> getSearchType()
   {
      return searchType;
   }

   public TextBox getTmTextBox()
   {
      return tmTextBox;
   }

   @Override
   public Widget asWidget()
   {
      return this;
   }

   @Override
   public void startProcessing()
   {
      clearResults();
      dataProvider.getList().clear();
      dataProvider.refresh();
      // resultTable.setWidget(0, 0, new Label("Loading..."));
   }

   @Override
   public void stopProcessing()
   {
   }


   private static class DiffMatchPatchLabelCell extends AbstractCell<DiffMatchPatchLabel>
   {
      @Override
      public void render(Context context, DiffMatchPatchLabel value, SafeHtmlBuilder sb)
      {
         sb.appendHtmlConstant(value.getElement().getString());
      }
   }

   private static class HighlightingLabelCell extends AbstractCell<HighlightingLabel>
   {
      @Override
      public void render(Context context, HighlightingLabel value, SafeHtmlBuilder sb)
      {
         sb.appendHtmlConstant(value.getElement().getString());
      }
   }

   private static class DetailsCell extends ButtonCell
   {
      @Override
      public void render(Context context, SafeHtml data, SafeHtmlBuilder sb)
      {
         if (data != null)
         {
            SafeHtml html = SafeHtmlUtils.fromTrustedString(AbstractImagePrototype.create(resources.informationImage()).getHTML());
            sb.append(html);
         }
      }
   };

   private static Column<TranslationMemoryGlossaryItem, DiffMatchPatchLabel> sourceColumn()
   {
      Column<TranslationMemoryGlossaryItem, DiffMatchPatchLabel> sourceColumn = new Column<TranslationMemoryGlossaryItem, DiffMatchPatchLabel>(new DiffMatchPatchLabelCell())
      {
         @Override
         public DiffMatchPatchLabel getValue(TranslationMemoryGlossaryItem object)
         {
            DiffMatchPatchLabel label = new DiffMatchPatchLabel("", object.getSource());
            return label;
         }
      };
      return sourceColumn;
   }

   private static Column<TranslationMemoryGlossaryItem, HighlightingLabel> targetColumn()
   {
      Column<TranslationMemoryGlossaryItem, HighlightingLabel> targetColumn = new Column<TranslationMemoryGlossaryItem, HighlightingLabel>(new HighlightingLabelCell())
      {
         @Override
         public HighlightingLabel getValue(TranslationMemoryGlossaryItem object)
         {
            HighlightingLabel label = new HighlightingLabel(object.getTarget());
            return label;
         }
      };
      return targetColumn;
   }

   private static Column<TranslationMemoryGlossaryItem, String> detailsColumn()
   {
      Column<TranslationMemoryGlossaryItem, String> detailsColumn = new Column<TranslationMemoryGlossaryItem, String>(new ButtonCell())
      {
         @Override
         public String getValue(TranslationMemoryGlossaryItem object)
         {
            return "Details";
         }
      };
      return detailsColumn;
   }
   
   private static Column<TranslationMemoryGlossaryItem, String> copyColumn()
   {
      Column<TranslationMemoryGlossaryItem, String> copyColumn = new Column<TranslationMemoryGlossaryItem, String>(new ButtonCell())
      {
         @Override
         public String getValue(TranslationMemoryGlossaryItem object)
         {
            return "Copy";
         }
      };
      return copyColumn;
   }

   private void initTable()
   {
      tmTable = new CellTable<TranslationMemoryGlossaryItem>();
      tmTable.addColumn(sourceColumn(), "Source");
      tmTable.addColumn(targetColumn(), "Target");

      TextColumn<TranslationMemoryGlossaryItem> similarityColumn = new TextColumn<TranslationMemoryGlossaryItem>()
      {
         @Override
         public String getValue(TranslationMemoryGlossaryItem object)
         {
            return object.getSimilarityPercent() + "%";
         }
      };
      tmTable.addColumn(similarityColumn, "Similarity");
      
      Column<TranslationMemoryGlossaryItem, String> detailsColumn = detailsColumn();
      detailsColumn.setFieldUpdater(new FieldUpdater<TranslationMemoryGlossaryItem, String>()
      {
         @Override
         public void update(int index, TranslationMemoryGlossaryItem object, String value)
         {
            tmInfoPresenter.show(object);
         }
      });
      tmTable.addColumn(detailsColumn);

      Column<TranslationMemoryGlossaryItem, String> copyColumn = copyColumn();
      copyColumn.setFieldUpdater(new FieldUpdater<TranslationMemoryGlossaryItem, String>()
      {
         @Override
         public void update(int index, TranslationMemoryGlossaryItem object, String value)
         {
            eventBus.fireEvent(new TransMemoryCopyEvent(object.getSource(), object.getTarget()));
            Log.info("TransMemoryCopyEvent event is sent. (" + object.getTarget() + ")");
         }
      });
      tmTable.addColumn(copyColumn);
   }
   @Override
   public void createTable(String query, ArrayList<TranslationMemoryGlossaryItem> memories)
   {
      dataProvider.getList().clear();
      cachedMem.clear();

      for (final TranslationMemoryGlossaryItem memory : memories)
      {
         dataProvider.getList().add(memory);
      }
      dataProvider.refresh();
   }

   private void addColumn(String columnHeading, int pos)
   {
      Label widget = new Label(columnHeading);
      widget.setWidth("100%");
      widget.addStyleName("TransMemoryTableColumnHeader");
      // resultTable.setWidget(HEADER_ROW, pos, widget);
   }

   public void clearResults()
   {
      // resultTable.removeAllRows();
   }

   @Override
   public boolean isFocused()
   {
      return isFocused;
   }

   @Override
   public String getSource(int index)
   {
      try
      {
         return cachedMem.get(index).getSource();
      }
      catch (IndexOutOfBoundsException e)
      {
         return null;
      }
   }

   @Override
   public String getTarget(int index)
   {
      try
      {
         return cachedMem.get(index).getTarget();
      }
      catch (IndexOutOfBoundsException e)
      {
         return null;
      }
   }

   @Override
   public void setCopyLinksVisible(boolean visible)
   {
      showCopyLinks = visible;
      // could refresh display here
   }
}
