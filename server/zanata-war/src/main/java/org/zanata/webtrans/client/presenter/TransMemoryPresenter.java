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
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory.SearchType;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
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

      void createTable(String query, ArrayList<TranslationMemoryGlossaryItem> memories);
      
      void startProcessing();
      
      void stopProcessing();

      boolean isFocused();
      
      String getSource(int index);
      
      String getTarget(int index);

      void setCopyLinksVisible(boolean visible);
   }

   @Inject
   public TransMemoryPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, WorkspaceContext workspaceContext)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.workspaceContext = workspaceContext;
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

      registerHandler(eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), new WorkspaceContextUpdateEventHandler()
      {
         @Override
         public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
         {
            display.setCopyLinksVisible(!event.isReadOnly());
         }
      }));

      display.setCopyLinksVisible(!workspaceContext.isReadOnly());
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
            ArrayList<TranslationMemoryGlossaryItem> memories = result.getMemories();
            display.createTable(query, memories);
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
