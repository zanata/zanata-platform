package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.GlossaryDetails;
import org.zanata.webtrans.shared.model.TranslationMemoryGlossaryItem;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsAction;
import org.zanata.webtrans.shared.rpc.GetGlossaryDetailsResult;

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
   private final CachingDispatchAsync dispatcher;

   public interface Display extends WidgetDisplay
   {
      void hide();

      void show();

      HasText getSourceText();

      HasText getTargetText();

      HasText getSourceComment();

      HasText getTargetComment();

      HasChangeHandlers getEntryListBox();

      int getSelectedDocumentIndex();

      HasClickHandlers getDismissButton();

      void clearEntries();

      void addEntry(String text);
   }

   GetGlossaryDetailsResult glossaryDetails;

   @Inject
   public GlossaryDetailsPresenter(final Display display, EventBus eventBus, CachingDispatchAsync dispatcher)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;

      registerHandler(display.getDismissButton().addClickHandler(new ClickHandler()
      {
         @Override
         public void onClick(ClickEvent event)
         {
            display.hide();
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

   public void show(final TranslationMemoryGlossaryItem item)
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
               String entryText = "Entry #" + i + ": " + detailsItem.getSrcLocale() + '/' + detailsItem.getSourceRef();
               display.addEntry(entryText);
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
      if (selected >= 0)
      {
         GlossaryDetails item = glossaryDetails.getGlossaryDetails().get(selected);

         for (String srcComment : item.getSourceComment())
         {
            srcComments.append(srcComment);
            srcComments.append("\n");
         }

         for (String targetComment : item.getTargetComment())
         {
            targetComments.append(targetComment);
            targetComments.append("\n");
         }
      }
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
