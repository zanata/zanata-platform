package org.zanata.webtrans.client.presenter;

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.events.TransMemoryShorcutCopyHandler;
import org.zanata.webtrans.client.events.TransMemoryShortcutCopyEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.TransMemoryQuery;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetTranslationMemory;
import org.zanata.webtrans.shared.rpc.GetTranslationMemoryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

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
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;

public class TransMemoryPresenter extends WidgetPresenter<TransMemoryPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {
      HasClickHandlers getSearchButton();

      HasClickHandlers getClearButton();

      HasValue<SearchType> getSearchType();

      HasText getTmTextBox();

      void startProcessing();

      void stopProcessing();

      void setPageSize(int size);

      boolean isFocused();

      Column<TransMemoryResultItem, ImageResource> getDetailsColumn();

      Column<TransMemoryResultItem, String> getCopyColumn();

      void setDataProvider(ListDataProvider<TransMemoryResultItem> dataProvider);

      void setQueries(List<String> queries);

      HasClickHandlers getPrefillButton();

   }

   private final WorkspaceContext workspaceContext;
   private final CachingDispatchAsync dispatcher;
   private GetTranslationMemory submittedRequest = null;
   private GetTranslationMemory lastRequest = null;
   private TransMemoryDetailsPresenter tmInfoPresenter;
   private PrefillPresenter prefillPresenter;
   private ListDataProvider<TransMemoryResultItem> dataProvider;

   @Inject
   public TransMemoryPresenter(Display display, EventBus eventBus, CachingDispatchAsync dispatcher, TransMemoryDetailsPresenter tmInfoPresenter, WorkspaceContext workspaceContext, PrefillPresenter prefillPresenter)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.workspaceContext = workspaceContext;
      this.tmInfoPresenter = tmInfoPresenter;
      this.prefillPresenter = prefillPresenter;

      dataProvider = new ListDataProvider<TransMemoryResultItem>();
      display.setDataProvider(dataProvider);
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
            createTMRequest(new TransMemoryQuery(query, display.getSearchType().getValue()));
         }
      });

      display.getClearButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            display.getTmTextBox().setText("");
            dataProvider.getList().clear();
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
            if (!workspaceContext.isReadOnly())
            {
               TransMemoryResultItem item;
               try
               {
                  item = dataProvider.getList().get(event.getIndex());
               }
               catch (IndexOutOfBoundsException ex)
               {
                  item = null;
               }
               if (item != null)
               {
                  eventBus.fireEvent(new CopyDataToEditorEvent(item.getTargetContents()));
               }
            }
         }
      }));

      display.getDetailsColumn().setFieldUpdater(new FieldUpdater<TransMemoryResultItem, ImageResource>()
      {
         @Override
         public void update(int index, TransMemoryResultItem object, ImageResource value)
         {
            tmInfoPresenter.show(object);
         }
      });

      display.getCopyColumn().setFieldUpdater(new FieldUpdater<TransMemoryResultItem, String>()
      {
         @Override
         public void update(int index, TransMemoryResultItem object, String value)
         {
            eventBus.fireEvent(new CopyDataToEditorEvent(object.getTargetContents()));
         }
      });

      display.getPrefillButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            prefillPresenter.preparePrefill();
         }
      });
   }

   public void createTMRequestForTransUnit(TransUnit transUnit)
   {
      // Start automatically fuzzy search
      SearchType searchType = SearchType.FUZZY_PLURAL;
      createTMRequest(new TransMemoryQuery(transUnit.getSources(), searchType));
   }

   private void createTMRequest(TransMemoryQuery query)
   {
      dataProvider.getList().clear();
      display.startProcessing();
      final GetTranslationMemory action = new GetTranslationMemory(query, workspaceContext.getWorkspaceId().getLocaleId());
      scheduleTMRequest(action);
   }

   /**
    * Create a translation memory request.  The request will be sent
    * immediately if the server is not processing another TM request,
    * otherwise it will block. NB: If this request is blocked, it will be
    * discarded if another request arrives before the server finishes.
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
      Log.debug("submitting TM request");
      dispatcher.execute(action, new AsyncCallback<GetTranslationMemoryResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.error(caught.getMessage(), caught);
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
      display.setQueries(queries);
      dataProvider.getList().clear();
      for (final TransMemoryResultItem memory : result.getMemories())
      {
         dataProvider.getList().add(memory);
      }
      display.setPageSize(dataProvider.getList().size());
      dataProvider.refresh();
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
