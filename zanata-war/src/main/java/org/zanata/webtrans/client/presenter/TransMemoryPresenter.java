package org.zanata.webtrans.client.presenter;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.events.TransMemoryShorcutCopyHandler;
import org.zanata.webtrans.client.events.TransMemoryShortcutCopyEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasAllFocusHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class TransMemoryPresenter extends WidgetPresenter<TransMemoryPresenter.Display> implements HasTMEvent
{
   public interface Display extends WidgetDisplay
   {
      HasClickHandlers getSearchButton();

      HasValue<SearchType> getSearchType();

      HasText getTmTextBox();

      HasAllFocusHandlers getFocusTmTextBox();

      void startProcessing();

      HasClickHandlers getMergeButton();
      
      HasClickHandlers getClearButton();

      HasClickHandlers getDiffLegendInfo();

      void renderTable(ArrayList<TransMemoryResultItem> memories, List<String> queries);

      void setListener(HasTMEvent listener);

      void stopProcessing(boolean showResult);

      void clearTableContent();

      void showDiffLegend();

      void hideDiffLegend();
   }

   private final UserWorkspaceContext userWorkspaceContext;
   private final CachingDispatchAsync dispatcher;

   private GetTranslationMemory submittedRequest = null;
   private GetTranslationMemory lastRequest = null;
   private TransMemoryDetailsPresenter tmInfoPresenter;
   private TransMemoryMergePresenter transMemoryMergePresenter;
   private KeyShortcutPresenter keyShortcutPresenter;

   private final WebTransMessages messages;

   private boolean isFocused;

   private ArrayList<TransMemoryResultItem> currentResult;

   @Inject
   public TransMemoryPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, final WebTransMessages messages, TransMemoryDetailsPresenter tmInfoPresenter, UserWorkspaceContext userWorkspaceContext, TransMemoryMergePresenter transMemoryMergePresenter, KeyShortcutPresenter keyShortcutPresenter)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.userWorkspaceContext = userWorkspaceContext;
      this.tmInfoPresenter = tmInfoPresenter;
      this.transMemoryMergePresenter = transMemoryMergePresenter;
      this.keyShortcutPresenter = keyShortcutPresenter;
      this.messages = messages;
      currentResult = new ArrayList<TransMemoryResultItem>();
   }

   @Override
   protected void onBind()
   {
      display.getSearchType().setValue(SearchType.FUZZY);

      keyShortcutPresenter.register(new KeyShortcut(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER), ShortcutContext.TM, messages.searchTM(), new KeyShortcutEventHandler()
      {
         @Override
         public void onKeyShortcut(KeyShortcutEvent event)
         {
            fireSearchEvent();
         }
      }));

      display.getSearchButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            fireSearchEvent();
         }
      });

      display.getClearButton().addClickHandler(new ClickHandler()
      {
         
         @Override
         public void onClick(ClickEvent event)
         {
            display.getTmTextBox().setText("");
            display.clearTableContent();
         }
      });
      
      display.getFocusTmTextBox().addFocusHandler(new FocusHandler()
      {
         @Override
         public void onFocus(FocusEvent event)
         {
            keyShortcutPresenter.setContextActive(ShortcutContext.TM, true);
            keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, false);
            keyShortcutPresenter.setContextActive(ShortcutContext.Edit, false);
            isFocused = true;
         }
      });

      display.getFocusTmTextBox().addBlurHandler(new BlurHandler()
      {
         @Override
         public void onBlur(BlurEvent event)
         {
            keyShortcutPresenter.setContextActive(ShortcutContext.TM, false);
            keyShortcutPresenter.setContextActive(ShortcutContext.Navigation, true);
            isFocused = false;
         }
      });

      display.getDiffLegendInfo().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            display.showDiffLegend();
         }
      });

      registerHandler(eventBus.addHandler(TransUnitSelectionEvent.getType(), new TransUnitSelectionHandler()
      {
         @Override
         public void onTransUnitSelected(TransUnitSelectionEvent event)
         {
            createTMRequestForTransUnit(event.getSelection());
         }
      }));

      registerHandler(eventBus.addHandler(TransMemoryShortcutCopyEvent.getType(), new TransMemoryShorcutCopyHandler()
      {
         @Override
         public void onTransMemoryCopy(TransMemoryShortcutCopyEvent event)
         {
            if (!userWorkspaceContext.hasReadOnlyAccess())
            {
               TransMemoryResultItem item;
               try
               {
                  item = currentResult.get(event.getIndex());
               }
               catch (IndexOutOfBoundsException ex)
               {
                  item = null;
               }
               if (item != null)
               {
                  Log.debug("Copy from translation memory:" + (event.getIndex() + 1));
                  eventBus.fireEvent(new CopyDataToEditorEvent(item.getTargetContents()));
               }
            }
         }
      }));

      display.getMergeButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            transMemoryMergePresenter.prepareTMMerge();
         }
      });

      display.setListener(this);
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

   private void fireSearchEvent()
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
      display.startProcessing();
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

}
