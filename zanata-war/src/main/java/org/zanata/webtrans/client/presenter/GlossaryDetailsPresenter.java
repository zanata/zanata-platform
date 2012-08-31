package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsAction;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsResult;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermAction;
import org.zanata.webtrans.shared.rpc.UpdateGlossaryTermResult;

import com.allen_sauer.gwt.log.client.Log;
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

      HasText getSourceComment();

      HasText getTargetComment();

      HasText getSourceLabel();

      HasText getTargetLabel();

      HasText getSrcRef();

      HasChangeHandlers getEntryListBox();

      int getSelectedDocumentIndex();

      HasClickHandlers getDismissButton();

      HasClickHandlers getSaveButton();

      void clearEntries();

      void addEntry(String text);
   }

   private GetGlossaryDetailsResult glossaryDetails;

   private GlossaryDetails selectedDetailEntry;

   private final UiMessages messages;

   private final CachingDispatchAsync dispatcher;

   @Inject
   public GlossaryDetailsPresenter(final Display display, EventBus eventBus, UiMessages messages, final CachingDispatchAsync dispatcher)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      this.messages = messages;

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
            if (selectedDetailEntry != null)
            {
               // check if there's any changes and save
               if(!display.getTargetText().getText().equals(selectedDetailEntry.getTarget()))
               {
                  dispatcher.execute(new UpdateGlossaryTermAction(selectedDetailEntry.getSrcLocale(), selectedDetailEntry.getTargetLocale(), selectedDetailEntry.getSource(), display.getTargetText().getText(), selectedDetailEntry.getTargetVersionNum()), new AsyncCallback<UpdateGlossaryTermResult>()
                  {
                     @Override
                     public void onFailure(Throwable caught)
                     {
                        Log.error(caught.getMessage(), caught);
                     }
                     @Override
                     public void onSuccess(UpdateGlossaryTermResult result)
                     {
                        //Update with lastest term details in list
                     }
                  });
               }
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

   protected void selectEntry(int selected)
   {
      StringBuilder srcComments = new StringBuilder();
      StringBuilder targetComments = new StringBuilder();
      String srcRef = "";
      if (selected >= 0)
      {
         selectedDetailEntry = glossaryDetails.getGlossaryDetails().get(selected);
         srcRef = selectedDetailEntry.getSourceRef();
         for (String srcComment : selectedDetailEntry.getSourceComment())
         {
            srcComments.append(srcComment);
            srcComments.append("\n");
         }

         for (String targetComment : selectedDetailEntry.getTargetComment())
         {
            targetComments.append(targetComment);
            targetComments.append("\n");
         }
      }

      display.getSrcRef().setText(srcRef);
      display.getSourceComment().setText(srcComments.toString());
      display.getTargetComment().setText(targetComments.toString());
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
}
