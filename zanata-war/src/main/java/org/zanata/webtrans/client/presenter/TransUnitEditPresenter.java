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

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEventHandler;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.TransUnitsDataModel;
import org.zanata.webtrans.client.editor.table.GetTransUnitActionContext;
import org.zanata.webtrans.client.service.NavigationController;
import org.zanata.webtrans.client.editor.table.TargetContentsPresenter;
import org.zanata.webtrans.client.events.DocumentSelectionEvent;
import org.zanata.webtrans.client.events.DocumentSelectionHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NavTransUnitHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.view.TransUnitEditDisplay;
import org.zanata.webtrans.client.view.TransUnitListDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitUpdateRequest;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import org.zanata.webtrans.shared.rpc.UpdateTransUnitResult;
import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransUnitEditPresenter extends WidgetPresenter<TransUnitEditDisplay> implements DocumentSelectionHandler,
      SelectionChangeEvent.Handler,
      WorkspaceContextUpdateEventHandler,
      NavTransUnitHandler,
      LoadingStateChangeEvent.Handler,
      TransUnitSaveEventHandler
{

   private final TransUnitEditDisplay display;
   private final EventBus eventBus;
   private final NavigationController navigationController;
   private final TransUnitListDisplay transUnitListDisplay;
   private final SourceContentsPresenter sourceContentsPresenter;
   private final TargetContentsPresenter targetContentsPresenter;
   private final CachingDispatchAsync dispatcher;
   private final TableEditorMessages messages;
   private final TransUnitsDataModel dataModel;

   private TransUnit selectedTransUnit = null;

   //TODO too many constructor dependency
   @Inject
   public TransUnitEditPresenter(TransUnitEditDisplay display, EventBus eventBus, NavigationController navigationController,
                                 TransUnitListDisplay transUnitListDisplay,
                                 SourceContentsPresenter sourceContentsPresenter,
                                 TargetContentsPresenter targetContentsPresenter,
                                 CachingDispatchAsync dispatcher,
                                 WorkspaceContext workspaceContext,
                                 TableEditorMessages messages)
   {
      super(display, eventBus);
      this.display = display;
      this.eventBus = eventBus;
      this.navigationController = navigationController;
      this.transUnitListDisplay = transUnitListDisplay;
      this.sourceContentsPresenter = sourceContentsPresenter;
      this.targetContentsPresenter = targetContentsPresenter;
      this.dispatcher = dispatcher;
      this.messages = messages;

      initViewOnWorkspaceContext(workspaceContext.isReadOnly());

      dataModel = navigationController.getDataModel();
      dataModel.addDataDisplay(transUnitListDisplay);
   }

   private void initViewOnWorkspaceContext(boolean readOnly)
   {
      if (readOnly)
      {
         display.init(transUnitListDisplay, null, null);
      }
      else
      {
         display.init(transUnitListDisplay, sourceContentsPresenter.getDisplay(), targetContentsPresenter.getDisplay());
      }
   }

   @Override
   protected void onBind()
   {
      eventBus.addHandler(DocumentSelectionEvent.getType(), this);
      eventBus.addHandler(NavTransUnitEvent.getType(), this);
      eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), this);
      eventBus.addHandler(TransUnitSaveEvent.TYPE, this);
      transUnitListDisplay.addLoadingStateChangeHandler(this);
      dataModel.addSelectionChangeHandler(this);
   }

   @Override
   protected void onUnbind()
   {
   }

   @Override
   protected void onRevealDisplay()
   {
   }

   @Override
   public void onDocumentSelected(DocumentSelectionEvent event)
   {
      GetTransUnitActionContext context = GetTransUnitActionContext.of(event.getDocumentId()).setCount(10);
      //here it loads trans unit for a document from server
      navigationController.init(context);
   }

   @Override
   public void onSelectionChange(SelectionChangeEvent event)
   {
      selectedTransUnit = dataModel.getSelectedOrNull();
      if (selectedTransUnit != null)
      {
         Log.debug("selected: " + selectedTransUnit.getId());
         sourceContentsPresenter.setValue(selectedTransUnit);
         targetContentsPresenter.setValue(selectedTransUnit, null);
         sourceContentsPresenter.selectedSource();
         targetContentsPresenter.showEditors(0);
      }
   }

   @Override
   public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
   {
      initViewOnWorkspaceContext(event.isReadOnly());
   }

   @Override
   public void onNavTransUnit(NavTransUnitEvent event)
   {
      navigationController.navigateTo(event.getRowType());
   }

   @Override
   public void onLoadingStateChanged(LoadingStateChangeEvent event)
   {
      if (event.getLoadingState() == LoadingStateChangeEvent.LoadingState.LOADED)
      {
         Log.debug("finish loading. scroll to selected");
         display.scrollToRow(dataModel.getSelectedOrNull());
      }
   }

   @Override
   public void onTransUnitSave(TransUnitSaveEvent event)
   {
      targetContentsPresenter.setToViewMode();
      TransUnitUpdated.UpdateType updateType = event.getStatus() == ContentState.Approved ? TransUnitUpdated.UpdateType.WebEditorSave : TransUnitUpdated.UpdateType.WebEditorSaveFuzzy;
      Log.debug("row updated, calculated update type: " + updateType);
      final UpdateTransUnit updateTransUnit = new UpdateTransUnit(new TransUnitUpdateRequest(selectedTransUnit.getId(), event.getTargets(), event.getStatus(), selectedTransUnit.getVerNum()), updateType);
      eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, messages.notifySaving()));
      dispatcher.execute(updateTransUnit, new AsyncCallback<UpdateTransUnitResult>()
      {
         @Override
         public void onFailure(Throwable e)
         {
            Log.error("UpdateTransUnit failure ", e);
            String message = e.getLocalizedMessage();
            saveFailure(message);
         }

         @Override
         public void onSuccess(UpdateTransUnitResult result)
         {
            // FIXME check result.success
            TransUnit updatedTU = result.getUpdateInfoList().get(0).getTransUnit();
            if (result.isSingleSuccess() && dataModel.update(updatedTU))
            {
               eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, messages.notifyUpdateSaved()));
               navigationController.navigateTo(NavTransUnitEvent.NavigationType.NextEntry);
               targetContentsPresenter.showEditors(0);
            }
            else
            {
               // TODO localised message
               saveFailure("row " + selectedTransUnit.getRowIndex());
            }
         }
      });
   }

   private void saveFailure(String message)
   {
      eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Error, messages.notifyUpdateFailed(message)));
      targetContentsPresenter.showEditors(0);
   }
}
