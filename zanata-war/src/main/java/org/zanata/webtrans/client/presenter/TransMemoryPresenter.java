package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.events.TransMemoryShorcutCopyHandler;
import org.zanata.webtrans.client.events.TransMemoryShortcutCopyEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.TranslationMemoryDisplay;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class TransMemoryPresenter extends WidgetPresenter<TranslationMemoryDisplay> implements TranslationMemoryDisplay.Listener, TransUnitSelectionHandler, TransMemoryShorcutCopyHandler, UserConfigChangeHandler
{
   private final UserWorkspaceContext userWorkspaceContext;
   private final CachingDispatchAsync dispatcher;
   private final TransMemoryDetailsPresenter tmInfoPresenter;
   private final TransMemoryMergePresenter transMemoryMergePresenter;
   private final KeyShortcutPresenter keyShortcutPresenter;
   private final WebTransMessages messages;
   private final UserConfigHolder configHolder;

   // states
   private boolean isFocused;
   private GetTranslationMemory lastRequest = null;
   private GetTranslationMemory submittedRequest = null;
   private List<TransMemoryResultItem> currentResult;

   @Inject
   public TransMemoryPresenter(TranslationMemoryDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher, final WebTransMessages messages, TransMemoryDetailsPresenter tmInfoPresenter, UserWorkspaceContext userWorkspaceContext, TransMemoryMergePresenter transMemoryMergePresenter, KeyShortcutPresenter keyShortcutPresenter, UserConfigHolder configHolder)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.userWorkspaceContext = userWorkspaceContext;
      this.tmInfoPresenter = tmInfoPresenter;
      this.transMemoryMergePresenter = transMemoryMergePresenter;
      this.keyShortcutPresenter = keyShortcutPresenter;
      this.messages = messages;
      this.configHolder = configHolder;
      currentResult = new ArrayList<TransMemoryResultItem>();

      display.setDisplayMode(configHolder.getState().getTransMemoryDisplayMode());
   }

   @Override
   protected void onBind()
   {
      display.getSearchType().setValue(SearchType.FUZZY);

      // @formatter:off
      keyShortcutPresenter.register(KeyShortcut.Builder.builder()
            .addKey(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER))
            .setContext(ShortcutContext.TM)
            .setDescription(messages.searchTM())
            .setHandler(new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            fireSearchEvent();
         }
      }).build());
      // @formatter:on

      registerHandler(eventBus.addHandler(TransUnitSelectionEvent.getType(), this));
      registerHandler(eventBus.addHandler(TransMemoryShortcutCopyEvent.getType(), this));
      registerHandler(eventBus.addHandler(UserConfigChangeEvent.TYPE, this));

      display.setListener(this);
   }

   @Override
   public void onTMMergeClick()
   {
      transMemoryMergePresenter.prepareTMMerge();
   }

   @Override
   public void onDiffModeChanged()
   {
      if (currentResult != null && !currentResult.isEmpty())
      {
         display.redrawTable(currentResult);
      }
   }

   @Override
   public void onUserConfigChanged(UserConfigChangeEvent event)
   {
      if (event == UserConfigChangeEvent.EDITOR_CONFIG_CHANGE_EVENT)
      {
         display.setDisplayMode(configHolder.getState().getTransMemoryDisplayMode());
         onDiffModeChanged();
      }
   }

   @Override
   public void showDiffLegend(boolean show)
   {
      display.showDiffLegend(show);
   }

   @Override
   public void showTMDetails(TransMemoryResultItem object)
   {
      tmInfoPresenter.show(object);
   }

   @Override
   public void fireCopyEvent(TransMemoryResultItem object)
   {
      eventBus.fireEvent(new CopyDataToEditorEvent(object.getTargetContents()));
   }

   @Override
   public void fireSearchEvent()
   {
      String query = display.getTmTextBox().getText();
      createTMRequest(new TransMemoryQuery(query, display.getSearchType().getValue()));
   }

   public void createTMRequestForTransUnit(TransUnit transUnit)
   {
      // Start automatically fuzzy search
      SearchType searchType = SearchType.FUZZY_PLURAL;
      createTMRequest(new TransMemoryQuery(transUnit.getSources(), searchType));
   }

   private void createTMRequest(TransMemoryQuery query)
   {
      final GetTranslationMemory action = new GetTranslationMemory(query, userWorkspaceContext.getWorkspaceContext().getWorkspaceId().getLocaleId(), userWorkspaceContext.getSelectedDoc().getSourceLocale());
      scheduleTMRequest(action);
   }

   /**
    * Create a translation memory request. The request will be sent immediately
    * if the server is not processing another TM request, otherwise it will
    * block. NB: If this request is blocked, it will be discarded if another
    * request arrives before the server finishes.
    * 
    * @param action
    */
   private void scheduleTMRequest(GetTranslationMemory action)
   {
      lastRequest = action;
      if (submittedRequest == null)
      {
         submitTMRequest(action);
      }
      else
      {
         Log.debug("blocking TM request until outstanding request returns");
      }
   }

   private void submitTMRequest(GetTranslationMemory action)
   {
      display.startProcessing();
      Log.debug("submitting TM request");
      dispatcher.execute(action, new AsyncCallback<GetTranslationMemoryResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.error(caught.getMessage(), caught);
            display.stopProcessing(false);
            submittedRequest = null;
         }

         @Override
         public void onSuccess(GetTranslationMemoryResult result)
         {
            if (result.getRequest().equals(lastRequest))
            {
               Log.debug("received TM result for query");
               displayTMResult(result);
               lastRequest = null;
            }
            else
            {
               Log.debug("ignoring old TM result for query");
               display.stopProcessing(false);
            }
            submittedRequest = null;
            if (lastRequest != null)
            {
               // submit the waiting request
               submitTMRequest(lastRequest);
            }
         }
      });
      submittedRequest = action;
   }

   private void displayTMResult(GetTranslationMemoryResult result)
   {
      List<String> queries = submittedRequest.getQuery().getQueries();

      if (!result.getMemories().isEmpty())
      {
         display.renderTable(result.getMemories(), queries);
         currentResult = result.getMemories();
         display.stopProcessing(true);
      }
      else
      {
         display.stopProcessing(false);
      }
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

   public boolean isFocused()
   {
      return isFocused;
   }

   @Override
   public void onTransUnitSelected(TransUnitSelectionEvent event)
   {
      createTMRequestForTransUnit(event.getSelection());
   }

   @Override
   public void onTransMemoryCopy(TransMemoryShortcutCopyEvent event)
   {
      if (userWorkspaceContext.hasWriteAccess())
      {
         TransMemoryResultItem item = getTMResultOrNull(event);
         if (item != null)
         {
            Log.debug("Copy from translation memory:" + (event.getIndex() + 1));
            eventBus.fireEvent(new CopyDataToEditorEvent(item.getTargetContents()));
         }
      }
   }

   private TransMemoryResultItem getTMResultOrNull(TransMemoryShortcutCopyEvent event)
   {
      int index = event.getIndex();
      return index >= 0 && index < currentResult.size() ? currentResult.get(index) : null;
   }

   @Override
   public void clearContent()
   {
      display.getTmTextBox().setText("");
      display.clearTableContent();
      currentResult.clear();
   }

   @Override
   public void onFocus(boolean isFocused)
   {
      keyShortcutPresenter.setContextActive(ShortcutContext.TM, isFocused);
      keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, !isFocused);
      keyShortcutPresenter.setContextActive(ShortcutContext.Edit, !isFocused);
      this.isFocused = isFocused;
   }

   /**
    * for testing
    * @param currentResult current TM result
    */
   protected void setStatesForTesting(List<TransMemoryResultItem> currentResult, GetTranslationMemory submittedRequest)
   {
      if (!GWT.isClient())
      {
         this.currentResult = currentResult;
         this.submittedRequest = submittedRequest;
      }
   }
}
