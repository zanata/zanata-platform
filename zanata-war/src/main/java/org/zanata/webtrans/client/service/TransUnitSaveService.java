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

package org.zanata.webtrans.client.service;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.CheckStateHasChangedEvent;
import org.zanata.webtrans.client.events.CheckStateHasChangedHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEventHandler;
import org.zanata.webtrans.client.presenter.DocumentListPresenter;
import org.zanata.webtrans.client.presenter.TargetContentsPresenter;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.GoToRowLink;
import org.zanata.webtrans.client.ui.UndoLink;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Singleton
public class TransUnitSaveService implements TransUnitSaveEventHandler, CheckStateHasChangedHandler
{
   private final TableEditorMessages messages;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final Provider<UndoLink> undoLinkProvider;
   private final TargetContentsPresenter targetContentsPresenter;
   private final DocumentListPresenter documentListPresenter;
   private final NavigationService navigationService;
   private final Provider<GoToRowLink> goToRowLinkProvider;
   private final SaveEventQueue queue;

   @Inject
   public TransUnitSaveService(EventBus eventBus, CachingDispatchAsync dispatcher, Provider<UndoLink> undoLinkProvider, DocumentListPresenter documentListPresenter, TargetContentsPresenter targetContentsPresenter, TableEditorMessages messages, NavigationService navigationService, Provider<GoToRowLink> goToRowLinkProvider, SaveEventQueue queue)
   {
      this.messages = messages;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.undoLinkProvider = undoLinkProvider;
      this.targetContentsPresenter = targetContentsPresenter;
      this.documentListPresenter = documentListPresenter;
      this.navigationService = navigationService;
      this.goToRowLinkProvider = goToRowLinkProvider;
      this.queue = queue;
   }

   public void init()
   {
      eventBus.addHandler(TransUnitSaveEvent.TYPE, this);
      eventBus.addHandler(CheckStateHasChangedEvent.TYPE, this);
   }

   @Override
   public void onTransUnitSave(TransUnitSaveEvent event)
   {
      TransUnitId idToSave = event.getTransUnitId();
      Log.info("TU save event: new[" + event.getTargets() + "] + old: [" + event.getOldContents());
      if (stateHasNotChanged(event))
      {
         Log.info("NO OP! state has not changed for " + idToSave);
         return;
      }

      queue.push(event);
      if (queue.isSaving(idToSave))
      {
         Log.info(idToSave + " has saving event. Put in queue and return.");
         return;
      }

      performSave(idToSave);
   }

   private void performSave(TransUnitId idToSave)
   {
      TransUnitSaveEvent forSaving = queue.getNextPendingForSaving(idToSave);
      if (forSaving == null)
      {
         Log.info("no pending save for " + idToSave);
         return;
      }

      targetContentsPresenter.setEditingState(idToSave, TargetContentsDisplay.EditingState.SAVING);
      TransUnitUpdated.UpdateType updateType = workoutUpdateType(forSaving.getStatus());

      UpdateTransUnit updateTransUnit = new UpdateTransUnit(new TransUnitUpdateRequest(idToSave, forSaving.getTargets(), forSaving.getAdjustedStatus(), forSaving.getVerNum()), updateType);
      Log.info("about to save translation: " + updateTransUnit);
      dispatcher.execute(updateTransUnit, new UpdateTransUnitCallback(forSaving, documentListPresenter.getCurrentDocument(), idToSave));
   }

   /**
    * Display confirmation dialog box if new status of TU has been changed to
    * approved without any content changes.
    */
   @Override
   public void onCheckStateHasChanged(CheckStateHasChangedEvent event)
   {
      TransUnit transUnit = navigationService.getByIdOrNull(event.getTransUnitId());
      if (transUnit == null)
      {
         return;
      }

      boolean targetChanged = !Objects.equal(transUnit.getTargets(), event.getTargets());
      boolean targetUnchangedButCanSaveAsApproved = (event.getAdjustedState() == ContentState.Translated) && !Objects.equal(transUnit.getStatus(), event.getAdjustedState());

      if (targetChanged)
      {
         targetContentsPresenter.saveAsApprovedAndMoveNext(event.getTransUnitId());
      }
      else if (targetUnchangedButCanSaveAsApproved)
      {
         targetContentsPresenter.showSaveAsApprovedConfirmation(event.getTransUnitId());
      }
      else
      {
         eventBus.fireEvent(NavTransUnitEvent.NEXT_ENTRY_EVENT);
      }
   }
   
   private boolean stateHasNotChanged(TransUnitSaveEvent event)
   {
      TransUnit transUnit = navigationService.getByIdOrNull(event.getTransUnitId());
      if (transUnit == null)
      {
         return false;
      }
      Log.info("id:" + transUnit.getId() + " old contents: " + transUnit.getTargets() + " state: " + transUnit.getStatus());
      return Objects.equal(transUnit.getStatus(), event.getAdjustedStatus()) && Objects.equal(transUnit.getTargets(), event.getTargets());
   }

   private TransUnitUpdated.UpdateType workoutUpdateType(ContentState status)
   {
      return status == ContentState.NeedReview ? TransUnitUpdated.UpdateType.WebEditorSaveFuzzy : TransUnitUpdated.UpdateType.WebEditorSave;
   }

   private class UpdateTransUnitCallback implements AsyncCallback<UpdateTransUnitResult>
   {
      private final TransUnitSaveEvent event;
      private final TransUnitId id;
      private final GoToRowLink goToRowLink;

      public UpdateTransUnitCallback(TransUnitSaveEvent event, DocumentInfo docInfo, TransUnitId id)
      {
         this.event = event;
         this.id = id;
         goToRowLink = goToRowLinkProvider.get();
         goToRowLink.prepare("", docInfo, id);
      }

      @Override
      public void onFailure(Throwable e)
      {
         Log.error("UpdateTransUnit failure ", e);
         saveFailure();
      }

      @Override
      public void onSuccess(UpdateTransUnitResult result)
      {
         TransUnit updatedTU = result.getUpdateInfoList().get(0).getTransUnit();
         Log.debug("save resulted TU: " + updatedTU.debugString());
         if (result.isSingleSuccess())
         {
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, messages.notifyUpdateSaved(updatedTU.getRowIndex(), updatedTU.getId().toString()), goToRowLink));
            int rowIndexOnPage = navigationService.findRowIndexById(updatedTU.getId());
            if (rowIndexOnPage != NavigationService.UNDEFINED)
            {
               UndoLink undoLink = undoLinkProvider.get();
               undoLink.prepareUndoFor(result);
               targetContentsPresenter.addUndoLink(rowIndexOnPage, undoLink);
               navigationService.updateDataModel(updatedTU);
               targetContentsPresenter.confirmSaved(updatedTU);
               targetContentsPresenter.setFocus();
            }
            queue.removeSaved(event, updatedTU.getVerNum());
         }
         else
         {
            saveFailure();
         }
         if (queue.hasPending())
         {
            performSave(id);
         }
      }

      private void saveFailure()
      {
         queue.removeAllPending(event.getTransUnitId());
         targetContentsPresenter.setEditingState(event.getTransUnitId(), TargetContentsDisplay.EditingState.UNSAVED);
         eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, messages.notifyUpdateFailed("id " + id), goToRowLink));
      }
   }
}
