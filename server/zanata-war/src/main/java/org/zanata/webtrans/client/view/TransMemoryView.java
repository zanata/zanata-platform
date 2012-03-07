package org.zanata.webtrans.client.view;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.zanata.webtrans.client.presenter.TransMemoryPresenter;
import org.zanata.webtrans.client.resources.Resources;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.DiffMatchPatchLabel;
import org.zanata.webtrans.client.ui.EnumListBox;
import org.zanata.webtrans.client.ui.HighlightingLabel;
import org.zanata.webtrans.client.ui.SearchTypeRenderer;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory.SearchType;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.ImageResourceCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.ValueListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.inject.Inject;

public class TransMemoryView extends Composite implements TransMemoryPresenter.Display
{

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

   @UiField
   ScrollPanel scrollPanel;

   CellTable<TranslationMemoryGlossaryItem> tmTable;

   private static Resources resources;
   private static String query;

   private boolean isFocused;

   private UiMessages messages;
   private ListDataProvider<TranslationMemoryGlossaryItem> dataProvider;

   private static class ClickableImageResourceCell extends ImageResourceCell
   {
      @Override
      public Set<String> getConsumedEvents()
      {
         Set<String> consumedEvents = new HashSet<String>();
         consumedEvents.add("click");
         return consumedEvents;
      }

      @Override
      public void onBrowserEvent(Context context, Element parent, ImageResource value, NativeEvent event, ValueUpdater<ImageResource> valueUpdater)
      {
         String eventType = event.getType();
         if ("click".equals(eventType))
         {
            onEnterKeyDown(context, parent, value, event, valueUpdater);
         }
      }

      @Override
      protected void onEnterKeyDown(Context context, Element parent, ImageResource value, NativeEvent event, ValueUpdater<ImageResource> valueUpdater)
      {
         if (valueUpdater != null)
         {
            valueUpdater.update(value);
         }
      }
   }

   private final Column<TranslationMemoryGlossaryItem, DiffMatchPatchLabel> sourceColumn = new Column<TranslationMemoryGlossaryItem, DiffMatchPatchLabel>(new DiffMatchPatchLabelCell())
   {
      @Override
      public DiffMatchPatchLabel getValue(TranslationMemoryGlossaryItem object)
      {
         DiffMatchPatchLabel label = new DiffMatchPatchLabel(query, object.getSource());
         return label;
      }
   };
   private final Column<TranslationMemoryGlossaryItem, HighlightingLabel> targetColumn = new Column<TranslationMemoryGlossaryItem, HighlightingLabel>(new HighlightingLabelCell())
   {
      @Override
      public HighlightingLabel getValue(TranslationMemoryGlossaryItem object)
      {
         HighlightingLabel label = new HighlightingLabel(object.getTarget());
         return label;
      }
   };
   private final TextColumn<TranslationMemoryGlossaryItem> similarityColumn = new TextColumn<TranslationMemoryGlossaryItem>()
   {
      @Override
      public String getValue(TranslationMemoryGlossaryItem object)
      {
         return object.getSimilarityPercent() + "%";
      }
   };
   private final Column<TranslationMemoryGlossaryItem, ImageResource> detailsColumn = new Column<TranslationMemoryGlossaryItem, ImageResource>(new ClickableImageResourceCell())
   {
      @Override
      public ImageResource getValue(TranslationMemoryGlossaryItem object)
      {
         return resources.informationImage();
      }
   };

   private final Column<TranslationMemoryGlossaryItem, String> copyColumn = new Column<TranslationMemoryGlossaryItem, String>(new ButtonCell())
   {
      @Override
      public String getValue(TranslationMemoryGlossaryItem object)
      {
         return "Copy";
      }
   };

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

   @Inject
   public TransMemoryView(final UiMessages messages, SearchTypeRenderer searchTypeRenderer, Resources resources)
   {
      this.resources = resources;
      this.messages = messages;

      searchType = new EnumListBox<SearchType>(SearchType.class, searchTypeRenderer);
      dataProvider = new ListDataProvider<TranslationMemoryGlossaryItem>();
      initWidget(uiBinder.createAndBindUi(this));

      clearButton.setText(messages.clearButtonLabel());
      searchButton.setText(messages.searchButtonLabel());

      renderTable();
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
      dataProvider.getList().clear();
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
      dataProvider.getList().clear();
   }

   @Override
   public void stopProcessing()
   {
   }

   @Override
   public void reloadData(String query, ArrayList<TranslationMemoryGlossaryItem> memories)
   {
      this.query = query;
      tmTable.setVisibleRangeAndClearData(tmTable.getVisibleRange(), true);
      dataProvider.getList().clear();
      for (final TranslationMemoryGlossaryItem memory : memories)
      {
         dataProvider.getList().add(memory);
      }
      tmTable.setPageSize(dataProvider.getList().size());
      dataProvider.refresh();
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
         return dataProvider.getList().get(index).getSource();
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
         return dataProvider.getList().get(index).getTarget();
      }
      catch (IndexOutOfBoundsException e)
      {
         return null;
      }
   }

   @Override
   public Column<TranslationMemoryGlossaryItem, ImageResource> getDetailsColumn()
   {
      return detailsColumn;
   }

   @Override
   public Column<TranslationMemoryGlossaryItem, String> getCopyColumn()
   {
      return copyColumn;
   }

   private void renderTable()
   {
      tmTable = new CellTable<TranslationMemoryGlossaryItem>();
      tmTable.addStyleName("tmTable");
      tmTable.addColumn(sourceColumn, messages.sourceLabel());
      tmTable.addColumn(targetColumn, messages.targetLabel());
      tmTable.addColumn(similarityColumn, messages.similarityLabel());
      tmTable.addColumn(detailsColumn);
      tmTable.addColumn(copyColumn);

      final NoSelectionModel<TranslationMemoryGlossaryItem> selectionModel = new NoSelectionModel<TranslationMemoryGlossaryItem>();
      final DefaultSelectionEventManager<TranslationMemoryGlossaryItem> manager = DefaultSelectionEventManager.createBlacklistManager(0, 1, 2);
      tmTable.setSelectionModel(selectionModel, manager);

      tmTable.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.DISABLED);

      dataProvider.addDataDisplay(tmTable);

      scrollPanel.clear();
      scrollPanel.add(tmTable);
   }
}
