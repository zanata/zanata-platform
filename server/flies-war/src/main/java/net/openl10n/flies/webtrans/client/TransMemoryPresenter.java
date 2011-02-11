package net.openl10n.flies.webtrans.client;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import net.openl10n.flies.webtrans.client.events.TransUnitSelectionEvent;
import net.openl10n.flies.webtrans.client.events.TransUnitSelectionHandler;
import net.openl10n.flies.webtrans.client.rpc.CachingDispatchAsync;
import net.openl10n.flies.webtrans.shared.model.TranslationMemoryItem;
import net.openl10n.flies.webtrans.shared.model.TransUnit;
import net.openl10n.flies.webtrans.shared.model.WorkspaceContext;
import net.openl10n.flies.webtrans.shared.rpc.GetTranslationMemory;
import net.openl10n.flies.webtrans.shared.rpc.GetTranslationMemoryResult;
import net.openl10n.flies.webtrans.shared.rpc.GetTranslationMemory.SearchType;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class TransMemoryPresenter extends WidgetPresenter<TransMemoryPresenter.Display>
{
   private final WorkspaceContext workspaceContext;
   private final CachingDispatchAsync dispatcher;

   public interface Display extends WidgetDisplay
   {
      HasValue<Boolean> getExactButton();

      HasClickHandlers getSearchButton();

      HasText getTmTextBox();

      void createTable(ArrayList<TranslationMemoryItem> memories);
   }

   @Inject
   public TransMemoryPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, WorkspaceContext workspaceContext)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.workspaceContext = workspaceContext;
   }

   @Override
   public Place getPlace()
   {
      return null;
   }

   @Override
   protected void onBind()
   {
      display.getSearchButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            String query = display.getTmTextBox().getText();
            GetTranslationMemory.SearchType searchType = display.getExactButton().getValue() ? SearchType.EXACT : SearchType.RAW;
            showResults(query, searchType);
         }
      });

      registerHandler(eventBus.addHandler(TransUnitSelectionEvent.getType(), new TransUnitSelectionHandler()
      {
         @Override
         public void onTransUnitSelected(TransUnitSelectionEvent event)
         {
            showResultsFor(event.getSelection());
         }
      }));
   }

   public void showResultsFor(TransUnit transUnit)
   {
      String query = transUnit.getSource();
      // Start automatically fuzzy search
      SearchType searchType = GetTranslationMemory.SearchType.FUZZY;
      display.getTmTextBox().setText("");
      showResults(query, searchType);
   }

   private void showResults(String query, GetTranslationMemory.SearchType searchType)
   {
      display.startProcessing();
      final GetTranslationMemory action = new GetTranslationMemory(query, workspaceContext.getWorkspaceId().getLocaleId(), searchType);
      dispatcher.execute(action, new AsyncCallback<GetTranslationMemoryResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.error(caught.getMessage(), caught);
         }

         @Override
         public void onSuccess(GetTranslationMemoryResult result)
         {
            ArrayList<TranslationMemoryItem> memories = result.getMemories();
            display.createTable(memories);
         }
      });
   }

   @Override
   protected void onPlaceRequest(PlaceRequest request)
   {
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void refreshDisplay()
   {
   }

   @Override
   public void revealDisplay()
   {
   }
}
