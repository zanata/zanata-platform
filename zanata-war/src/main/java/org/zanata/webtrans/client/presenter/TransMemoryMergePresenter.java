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
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.NavigationController;
import org.zanata.webtrans.client.ui.TransMemoryMergePopupPanelDisplay;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.MergeOption;
import org.zanata.webtrans.shared.rpc.TransMemoryMerge;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;

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
   private final NavigationController navigationController;
   private final UiMessages messages;
   private final Provider<UndoLink> undoLinkProvider;
   private final WebTransMessages webTransMessages;

   @Inject
   public TransMemoryMergePresenter(TransMemoryMergePopupPanelDisplay display, EventBus eventBus, CachingDispatchAsync dispatcher, NavigationController navigationController, UiMessages messages, Provider<UndoLink> undoLinkProvider, WebTransMessages webTransMessages)
   {
      super(display, eventBus);
      this.display = display;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.navigationController = navigationController;
      this.messages = messages;
      this.undoLinkProvider = undoLinkProvider;
      this.webTransMessages = webTransMessages;
      display.setListener(this);
   }

   @Override
   public void proceedToMergeTM(int percentage, MergeOption differentProjectOption, MergeOption differentDocumentOption, MergeOption differentResIdOption)
   {
      Collection<TransUnit> items = getNotApprovedItems();

      if (items.isEmpty())
      {
         eventBus.fireEvent(new NotificationEvent(Info, messages.noTranslationToMerge()));
         display.hide();
         return;
      }

      display.showProcessing();
      TransMemoryMerge action = prepareTMMergeAction(items, percentage, differentProjectOption, differentDocumentOption, differentResIdOption);
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
         public void onSuccess(final UpdateTransUnitResult result)
         {
            if (result.getUpdateInfoList().isEmpty())
            {
               eventBus.fireEvent(new NotificationEvent(Info, messages.noTranslationToMerge()));
            }
            else
            {
               final UndoLink undoLink = undoLinkProvider.get();
               undoLink.setText(webTransMessages.undo());
               undoLink.prepareUndoFor(result);
               undoLink.setUndoCallback(new UndoLink.UndoCallback()
               {
                  @Override
                  public void preUndo()
                  {
                     undoLink.setText(webTransMessages.undoInProgress());
                  }

                  @Override
                  public void postUndoSuccess()
                  {
                     undoLink.setText(webTransMessages.undone());
                  }
               });
               NotificationEvent event = new NotificationEvent(Info, messages.mergeTMSuccess(), undoLink);
               eventBus.fireEvent(event);
            }
            display.hide();
         }
      });
   }

   private Collection<TransUnit> getNotApprovedItems()
   {
      List<TransUnit> currentItems = navigationController.getCurrentPageValues();
      return Collections2.filter(currentItems, new Predicate<TransUnit>()
      {
         @Override
         public boolean apply(TransUnit input)
         {
            return input.getStatus() != ContentState.Approved;
         }
      });
   }

   private TransMemoryMerge prepareTMMergeAction(Collection<TransUnit> untranslatedTUs, int threshold, MergeOption differentProjectOption, MergeOption differentDocumentOption, MergeOption differentResIdOption)
   {
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
}
