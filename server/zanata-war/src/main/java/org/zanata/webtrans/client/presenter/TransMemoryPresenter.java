package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.TransMemoryCopyEvent;
import org.zanata.webtrans.client.events.TransMemoryShorcutCopyHandler;
import org.zanata.webtrans.client.events.TransMemoryShortcutCopyEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory.SearchType;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class TransMemoryPresenter extends WidgetPresenter<TransMemoryPresenter.Display>
{
   private final WorkspaceContext workspaceContext;
   private final CachingDispatchAsync dispatcher;
   private GetTranslationMemory currentRequest;

   @Inject
   private TransMemoryDetailsPresenter tmInfoPresenter;

   public interface Display extends WidgetDisplay
   {
      HasClickHandlers getSearchButton();

      HasValue<SearchType> getSearchType();

      HasText getTmTextBox();

      void reloadData(String query, ArrayList<TranslationMemoryGlossaryItem> memories);

      void startProcessing();

      void stopProcessing();

      boolean isFocused();

      String getSource(int index);

      String getTarget(int index);

      @SuppressWarnings("rawtypes")
      Column getDetailsColumn();

      @SuppressWarnings("rawtypes")
      Column getCopyColumn();

      void renderTable();
   }

   @Inject
   public TransMemoryPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, WorkspaceContext workspaceContext)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.workspaceContext = workspaceContext;

      display.renderTable();
   }

   @Override
   protected void onBind()
   {
      display.getSearchType().setValue(SearchType.FUZZY);
      display.getSearchButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            String query = display.getTmTextBox().getText();
            showResults(query, display.getSearchType().getValue());
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

      registerHandler(eventBus.addHandler(TransMemoryShortcutCopyEvent.getType(), new TransMemoryShorcutCopyHandler()
      {
         @Override
         public void onTransMemoryCopy(TransMemoryShortcutCopyEvent event)
         {
            if (!workspaceContext.isReadOnly())
            {
               String source = display.getSource(event.getIndex());
               String target = display.getTarget(event.getIndex());
               if (source != null && target != null)
               {
                  eventBus.fireEvent(new TransMemoryCopyEvent(source, target));
               }
            }
         }
      }));

      display.getDetailsColumn().setFieldUpdater(new FieldUpdater<TranslationMemoryGlossaryItem, ImageResource>()
      {
         @Override
         public void update(int index, TranslationMemoryGlossaryItem object, ImageResource value)
         {
            tmInfoPresenter.show(object);
         }
      });

      display.getCopyColumn().setFieldUpdater(new FieldUpdater<TranslationMemoryGlossaryItem, String>()
      {
         @Override
         public void update(int index, TranslationMemoryGlossaryItem object, String value)
         {
            eventBus.fireEvent(new TransMemoryCopyEvent(object.getSource(), object.getTarget()));
         }
      });
   }

   public void showResultsFor(TransUnit transUnit)
   {
      String query = transUnit.getSource();
      // Start automatically fuzzy search
      SearchType searchType = GetTranslationMemory.SearchType.FUZZY;
      display.getTmTextBox().setText("");
      showResults(query, searchType);
   }

   private void showResults(final String query, GetTranslationMemory.SearchType searchType)
   {
      display.startProcessing();
      final GetTranslationMemory action = new GetTranslationMemory(query, workspaceContext.getWorkspaceId().getLocaleId(), searchType);
      currentRequest = action;
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
            if (!result.getRequest().equals(currentRequest))
            {
               Log.debug("ignoring old TM result for query: " + result.getRequest().getQuery());
               return;
            }
            Log.debug("received TM result for query: " + currentRequest.getQuery());
            display.getTmTextBox().setText(currentRequest.getQuery());
            display.getSearchType().setValue(currentRequest.getSearchType());
            ArrayList<TranslationMemoryGlossaryItem> memories = result.getMemories();
            display.reloadData(query, memories);
            currentRequest = null;
         }
      });
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }
}
