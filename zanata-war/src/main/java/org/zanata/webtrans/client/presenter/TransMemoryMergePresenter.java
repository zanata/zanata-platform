/*
 * Copyright 2012, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.webtrans.client.presenter;

import java.util.Collection;
import java.util.List;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.editor.table.TableEditorPresenter;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.TransMemoryMergePopupPanelDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.MergeOption;
import org.zanata.webtrans.shared.rpc.RevertTransUnitUpdates;
import org.zanata.webtrans.shared.rpc.TransMemoryMerge;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import static org.zanata.webtrans.client.events.NotificationEvent.Severity.Error;
import static org.zanata.webtrans.client.events.NotificationEvent.Severity.Info;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransMemoryMergePresenter extends WidgetPresenter<TransMemoryMergePopupPanelDisplay> implements TransMemoryMergePopupPanelDisplay.Listener
{

   private TransMemoryMergePopupPanelDisplay display;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final TableEditorPresenter tableEditorPresenter;
   private final UiMessages messages;
   private final WebTransMessages webTransMessages;

   @Inject
   public TransMemoryMergePresenter(TransMemoryMergePopupPanelDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher, TableEditorPresenter tableEditorPresenter, UiMessages messages, WebTransMessages webTransMessages)
   {
      super(display, eventBus);
      this.display = display;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.tableEditorPresenter = tableEditorPresenter;
      this.messages = messages;
      this.webTransMessages = webTransMessages;
      display.setListener(this);
   }

   @Override
   public void proceedToMergeTM(String percentage, MergeOption differentProjectOption, MergeOption differentDocumentOption, MergeOption differentResIdOption)
   {
      Collection<TransUnit> newItems = getUntranslatedItems();

      if (newItems.isEmpty())
      {
         eventBus.fireEvent(new NotificationEvent(Info, messages.noTranslationToMerge()));
         display.hide();
         return;
      }

      display.showProcessing();
      TransMemoryMerge action = prepareTMMergeAction(newItems, percentage, differentProjectOption, differentDocumentOption, differentResIdOption);
      dispatcher.execute(action, new AsyncCallback<UpdateTransUnitResult>()
      {
         @Override
         public void onFailure(Throwable caught)
         {
            Log.warn("TM merge failed", caught);
            eventBus.fireEvent(new NotificationEvent(Error, messages.mergeTMFailed()));
            display.hide();
         }

         @Override
         public void onSuccess(UpdateTransUnitResult result)
         {
            NotificationEvent event = new NotificationEvent(Info, messages.mergeTMSuccess());
            event.appendInlineLinkToMessage(webTransMessages.undo(), new RevertTransUnitUpdateClickHandler(result));
            eventBus.fireEvent(event);
            display.hide();
         }
      });
   }

   private Collection<TransUnit> getUntranslatedItems()
   {
      List<TransUnit> currentItems = tableEditorPresenter.getDisplay().getRowValues();
      return Collections2.filter(currentItems, new Predicate<TransUnit>()
      {
         @Override
         public boolean apply(TransUnit input)
         {
            return input.getStatus() == ContentState.New;
         }
      });
   }

   private TransMemoryMerge prepareTMMergeAction(Collection<TransUnit> untranslatedTUs, String percentage, MergeOption differentProjectOption, MergeOption differentDocumentOption, MergeOption differentResIdOption)
   {
      Integer threshold = Integer.valueOf(percentage);

      List<TransUnitUpdateRequest> updateRequests = Lists.newArrayList(Collections2.transform(untranslatedTUs, new Function<TransUnit, TransUnitUpdateRequest>()
      {
         @Override
         public TransUnitUpdateRequest apply(TransUnit from)
         {
            return new TransUnitUpdateRequest(from.getId(), null, null, from.getVerNum());
         }
      }));
      return new TransMemoryMerge(threshold, updateRequests, differentProjectOption, differentDocumentOption, differentResIdOption);
   }

   @Override
   public void cancelMergeTM()
   {
      display.hide();
   }

   public void prepareTMMerge()
   {
      display.showForm();
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
   protected void onRevealDisplay()
   {
   }

   private class RevertTransUnitUpdateClickHandler implements ClickHandler
   {
      private final RevertTransUnitUpdates revertAction;

      RevertTransUnitUpdateClickHandler(UpdateTransUnitResult updateTransUnitResult)
      {
         this.revertAction = new RevertTransUnitUpdates(updateTransUnitResult.getUpdateInfoList());
      }

      @Override
      public void onClick(ClickEvent event)
      {
         dispatcher.execute(revertAction, new AsyncCallback<UpdateTransUnitResult>()
         {
            @Override
            public void onFailure(Throwable caught)
            {
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, webTransMessages.undoFailure()));
            }

            @Override
            public void onSuccess(UpdateTransUnitResult result)
            {
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, webTransMessages.undoSuccess()));
            }
         });
      }
   }
}
