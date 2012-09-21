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
import org.zanata.webtrans.client.presenter.TargetContentsPresenter;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEventHandler;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.ui.GoToRowLink;
import org.zanata.webtrans.client.ui.UndoLink;
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
public class TransUnitSaveService implements TransUnitSaveEventHandler
{
   private final TableEditorMessages messages;
   private final EventBus eventBus;
   private final CachingDispatchAsync dispatcher;
   private final Provider<UndoLink> undoLinkProvider;
   private final TargetContentsPresenter targetContentsPresenter;
   private final NavigationService navigationService;
   private final Provider<GoToRowLink> goToRowLinkProvider;

   @Inject
   public TransUnitSaveService(EventBus eventBus, CachingDispatchAsync dispatcher, Provider<UndoLink> undoLinkProvider, TargetContentsPresenter targetContentsPresenter, TableEditorMessages messages, NavigationService navigationService, Provider<GoToRowLink> goToRowLinkProvider)
   {
      this.messages = messages;
      this.eventBus = eventBus;
      this.dispatcher = dispatcher;
      this.undoLinkProvider = undoLinkProvider;
      this.targetContentsPresenter = targetContentsPresenter;
      this.navigationService = navigationService;
      this.goToRowLinkProvider = goToRowLinkProvider;
   }

   @Override
   public void onTransUnitSave(final TransUnitSaveEvent event)
   {
      if (stateHasNotChanged(event))
      {
         Log.info("NO OP! state has not changed for " + event.getTransUnitId());
         return;
      }

      final TransUnitId id = event.getTransUnitId();
      TransUnitUpdated.UpdateType updateType = workoutUpdateType(event.getStatus());

      final UpdateTransUnit updateTransUnit = new UpdateTransUnit(new TransUnitUpdateRequest(id, event.getTargets(), event.getAdjustedStatus(), event.getVerNum()), updateType);
      Log.info("about to save translation: " + updateTransUnit);
      dispatcher.execute(updateTransUnit, new UpdateTransUnitCallback(event, id));
   }

   private boolean stateHasNotChanged(TransUnitSaveEvent event)
   {
      TransUnit transUnit = navigationService.getByIdOrNull(event.getTransUnitId());
      if (transUnit == null)
      {
         return false;
      }
      Log.info(transUnit.getId() + " old contents: " + transUnit.getTargets() + " state: " + transUnit.getStatus());
      return Objects.equal(transUnit.getStatus(), event.getAdjustedStatus()) && Objects.equal(transUnit.getTargets(), event.getTargets());
   }

   private TransUnitUpdated.UpdateType workoutUpdateType(ContentState status)
   {
      return status == ContentState.Approved ? TransUnitUpdated.UpdateType.WebEditorSave : TransUnitUpdated.UpdateType.WebEditorSaveFuzzy;
   }

   private void saveFailure(String message, TransUnitId id)
   {
      GoToRowLink goToRowLink = goToRowLinkProvider.get();
      goToRowLink.prepare("", id);
      eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, messages.notifyUpdateFailed(message), goToRowLink));
   }

   private class UpdateTransUnitCallback implements AsyncCallback<UpdateTransUnitResult>
   {
      private final TransUnitSaveEvent event;
      private final TransUnitId id;

      public UpdateTransUnitCallback(TransUnitSaveEvent event, TransUnitId id)
      {
         this.event = event;
         this.id = id;
      }

      @Override
      public void onFailure(Throwable e)
      {
         // reset back the value
         targetContentsPresenter.updateTargets(event.getTransUnitId(), event.getOldContents());
         Log.error("UpdateTransUnit failure ", e);
         String message = e.getMessage();
         saveFailure(message, id);
      }

      @Override
      public void onSuccess(UpdateTransUnitResult result)
      {
         // FIXME check result.success
         TransUnit updatedTU = result.getUpdateInfoList().get(0).getTransUnit();
         Log.debug("save resulted TU: " + updatedTU.debugString());
         if (result.isSingleSuccess())
         {
            eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, messages.notifyUpdateSaved(updatedTU.getRowIndex(), updatedTU.getId().toString())));
            int rowIndexOnPage = navigationService.findRowIndexById(updatedTU.getId());
            if (rowIndexOnPage != NavigationService.UNSELECTED)
            {
               UndoLink undoLink = undoLinkProvider.get();
               undoLink.prepareUndoFor(result);
               targetContentsPresenter.addUndoLink(rowIndexOnPage, undoLink);
            }
         }
         else
         {
            saveFailure("id " + id, id);
         }
      }
   }
}
