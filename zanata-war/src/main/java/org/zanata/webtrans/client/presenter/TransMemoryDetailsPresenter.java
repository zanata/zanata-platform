package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.rpc.AbstractAsyncCallback;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.TransMemoryDetailsDisplay;
import org.zanata.webtrans.shared.model.TransMemoryDetails;
import org.zanata.webtrans.shared.model.TransMemoryResultItem;
import org.zanata.webtrans.shared.rpc.GetTransMemoryDetailsAction;
import org.zanata.webtrans.shared.rpc.TransMemoryDetailsList;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class TransMemoryDetailsPresenter extends WidgetPresenter<TransMemoryDetailsDisplay> implements TransMemoryDetailsDisplay.Listener
{
   private final CachingDispatchAsync dispatcher;

   private TransMemoryDetailsList tmDetails;

   @Inject
   public TransMemoryDetailsPresenter(TransMemoryDetailsDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher)
   {
      super(display, eventBus);
      this.dispatcher = dispatcher;
      display.setListener(this);
   }

   @Override
   protected void onBind()
   {
   }

   @Override
   protected void onUnbind()
   {
   }

   public void show(final TransMemoryResultItem item)
   {
      // request TM details from the server
      dispatcher.execute(new GetTransMemoryDetailsAction(item.getSourceIdList()), new AbstractAsyncCallback<TransMemoryDetailsList>()
      {
         @Override
         public void onSuccess(TransMemoryDetailsList result)
         {
            tmDetails = result;
            display.clearSourceAndTarget();
            display.setSource(item.getSourceContents());
            display.setTarget(item.getTargetContents());
            display.clearDocs();
            for (TransMemoryDetails detailsItem : tmDetails.getItems())
            {
               String docText = detailsItem.getProjectName() + '/' + detailsItem.getIterationName() + '/' + detailsItem.getDocId();
               display.addDoc(docText);
            }
            selectDoc(0);

            display.show();
         }
      });
   }

   @Override
   public void onRevealDisplay()
   {
   }

   @Override
   public void dismissTransMemoryDetails()
   {
      display.hide();
   }

   @Override
   public void onDocumentListBoxChanged()
   {
      selectDoc(display.getSelectedDocumentIndex());
   }

   protected void selectDoc(int selected)
   {
      String sourceComment = "";
      String targetComment = "";
      String project = "";
      String iteration = "";
      String doc = "";
      String lastModifiedBy = "";
      String lastModifiedDate = "";

      if (selected >= 0)
      {
         TransMemoryDetails item = tmDetails.getItems().get(selected);
         sourceComment = item.getSourceComment();
         targetComment = item.getTargetComment();
         project = item.getProjectName();
         iteration = item.getIterationName();
         doc = item.getDocId();
         lastModifiedBy = item.getLastModifiedBy();
         lastModifiedDate = item.getLastModifiedDate();
      }

      display.setSourceComment(sourceComment);
      display.setTargetComment(targetComment);
      display.setProjectIterationName(project + " / " + iteration);
      display.setDocumentName(doc);

      if (!Strings.isNullOrEmpty(lastModifiedBy))
      {
         display.setLastModified("Last modified by " + lastModifiedBy + " on " + lastModifiedDate);
      }
      else
      {
         display.setLastModified("Last modified on " + lastModifiedDate);
      }
   }

   /**
    * For testing only
    * @param details transMemoryDetailsList
    */
   protected void setStatForTesting(TransMemoryDetailsList details)
   {
      if (!GWT.isClient())
      {
         tmDetails = details;
      }
   }
}
