package org.zanata.webtrans.client.presenter;

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.NotificationEvent.Severity;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsAction;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsResult;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermAction;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;

public class GlossaryDetailsPresenter extends WidgetPresenter<GlossaryDetailsPresenter.Display>
{
   public interface Display extends WidgetDisplay
   {
      void hide();

      void show();

      HasText getSourceText();

      HasText getTargetText();

      void setSourceComment(List<String> comments);

      void setTargetComment(List<String> comments);

      HasText getSourceLabel();

      HasText getTargetLabel();

      HasText getSrcRef();

      HasChangeHandlers getEntryListBox();

      int getSelectedDocumentIndex();

      HasClickHandlers getDismissButton();

      HasClickHandlers getSaveButton();

      void clearEntries();

      void addEntry(String text);

      HasText getLastModified();

      HasClickHandlers getAddNewCommentButton();

      void addRowIntoTargetComment(int row, String comment);

      HasText getNewCommentText();

      int getTargetCommentRowCount();

      List<String> getCurrentTargetComments();

      void showLoading(boolean visible);

      void setHasUpdateAccess(boolean hasGlossaryUpdateAccess);
   }

   private GetGlossaryDetailsResult glossaryDetails;

   private GlossaryDetails selectedDetailEntry;

   private final UiMessages messages;

   private final CachingDispatchAsync dispatcher;

   private final UserWorkspaceContext userWorkspaceContext;

   private HasGlossaryEvent glossaryListener;

   @Inject
   public GlossaryDetailsPresenter(final Display display, final EventBus eventBus, final UiMessages messages, final CachingDispatchAsync dispatcher, final UserWorkspaceContext userWorkspaceContext)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.messages = messages;
      this.userWorkspaceContext = userWorkspaceContext;
      
      registerHandler(display.getDismissButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            display.hide();
            selectedDetailEntry = null;
         }
      }));

      registerHandler(display.getSaveButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            if (selectedDetailEntry != null && userWorkspaceContext.hasGlossaryUpdateAccess())
            {
               // check if there's any changes on the target term or the target
               // comments and save
               if (!display.getTargetText().getText().equals(selectedDetailEntry.getTarget()))
               {
                  display.showLoading(true);
                  UpdateGlossaryTermAction action = new UpdateGlossaryTermAction(selectedDetailEntry, display.getTargetText().getText(), display.getCurrentTargetComments());

                  dispatcher.execute(action, new AsyncCallback<UpdateGlossaryTermResult>()
                  {
                     @Override
                     public void onFailure(Throwable caught)
                     {
                        Log.error(caught.getMessage(), caught);
                        eventBus.fireEvent(new NotificationEvent(Severity.Error, messages.saveGlossaryFailed()));
                        display.showLoading(false);
                     }

                     @Override
                     public void onSuccess(UpdateGlossaryTermResult result)
                     {
                        Log.info("Glossary term updated:" + result.getDetail().getTarget());
                        glossaryListener.fireSearchEvent();
                        selectedDetailEntry = result.getDetail();
                        populateDisplayData();
                        display.showLoading(false);
                     }
                  });
               }
            }
         }
      }));

      registerHandler(display.getAddNewCommentButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            if (!Strings.isNullOrEmpty(display.getNewCommentText().getText()) && userWorkspaceContext.hasGlossaryUpdateAccess())
            {
               display.addRowIntoTargetComment(display.getTargetCommentRowCount(), display.getNewCommentText().getText());
               display.getNewCommentText().setText("");
            }
         }
      }));

      registerHandler(display.getEntryListBox().addChangeHandler(new ChangeHandler()
      {
         @Override
         public void onChange(ChangeEvent event)
         {
            selectEntry(display.getSelectedDocumentIndex());
         }
      }));
      
      display.setHasUpdateAccess(userWorkspaceContext.hasGlossaryUpdateAccess());
   }

   public void show(final GlossaryResultItem item)
   {
      // request glossary details from the server
      dispatcher.execute(new GetGlossaryDetailsAction(item.getSourceIdList()), new AsyncCallback<GetGlossaryDetailsResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.error(caught.getMessage(), caught);
         }

         @Override
         public void onSuccess(GetGlossaryDetailsResult result)
         {
            glossaryDetails = result;
            display.getSourceText().setText(item.getSource());
            display.getTargetText().setText(item.getTarget());
            display.clearEntries();

            int i = 1;
            for (GlossaryDetails detailsItem : result.getGlossaryDetails())
            {
               display.getSourceLabel().setText(messages.glossarySourceTermLabel(detailsItem.getSrcLocale().toString()));
               display.getTargetLabel().setText(messages.glossaryTargetTermLabel(detailsItem.getTargetLocale().toString()));
               display.addEntry(messages.entriesLabel(i));
               i++;
            }
            selectEntry(0);
            display.show();
         }
      });
   }

   private void populateDisplayData()
   {
      display.getSrcRef().setText(selectedDetailEntry.getSourceRef());
      display.setSourceComment(selectedDetailEntry.getSourceComment());
      display.setTargetComment(selectedDetailEntry.getTargetComment());
      display.getLastModified().setText(messages.lastModifiedOn(selectedDetailEntry.getLastModified()));
   }

   private void selectEntry(int selected)
   {
      if (selected >= 0)
      {
         selectedDetailEntry = glossaryDetails.getGlossaryDetails().get(selected);
      }
      populateDisplayData();
   }

   @Override
   protected void onBind()
   {
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   public void onRevealDisplay()
   {
   }

   public void setGlossaryListener(HasGlossaryEvent glossaryListener)
   {
      this.glossaryListener = glossaryListener;
   }
}
