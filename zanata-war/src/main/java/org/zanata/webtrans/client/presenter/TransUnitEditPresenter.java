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
import org.zanata.webtrans.client.editor.table.TargetContentsPresenter;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FilterViewEventHandler;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.FindMessageHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.NavTransUnitHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.events.TransUnitSaveEventHandler;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.service.NavigationController;
import org.zanata.webtrans.client.service.TransUnitSaveService;
import org.zanata.webtrans.client.service.TransUnitsDataModel;
import org.zanata.webtrans.client.service.TranslatorInteractionService;
import org.zanata.webtrans.client.ui.FilterViewConfirmationDisplay;
import org.zanata.webtrans.client.view.TransUnitEditDisplay;
import org.zanata.webtrans.client.view.TransUnitListDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.WorkspaceContext;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import static org.zanata.webtrans.client.events.NotificationEvent.Severity.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransUnitEditPresenter extends WidgetPresenter<TransUnitEditDisplay> implements
      SelectionChangeEvent.Handler,
      WorkspaceContextUpdateEventHandler,
      NavTransUnitHandler,
      LoadingStateChangeEvent.Handler,
      TransUnitSaveEventHandler,
      FindMessageHandler,
      FilterViewEventHandler,
      FilterViewConfirmationDisplay.Listener,
      TransUnitUpdatedEventHandler
{

   private final TransUnitEditDisplay display;
   private final EventBus eventBus;
   private final NavigationController navigationController;
   private final TransUnitListDisplay transUnitListDisplay;
   private final SourceContentsPresenter sourceContentsPresenter;
   private final TargetContentsPresenter targetContentsPresenter;
   private final TransUnitSaveService saveService;
   private final TranslatorInteractionService translatorService;
   private final TransUnitsDataModel dataModel;

   //state we need to keep track of
   private FilterViewEvent filterOptions = FilterViewEvent.DEFAULT;
   private FindMessageEvent findMessage = FindMessageEvent.DEFAULT;

   //TODO too many constructor dependency
   @Inject
   public TransUnitEditPresenter(TransUnitEditDisplay display, EventBus eventBus, NavigationController navigationController,
                                 TransUnitListDisplay transUnitListDisplay,
                                 SourceContentsPresenter sourceContentsPresenter,
                                 TargetContentsPresenter targetContentsPresenter,
                                 TransUnitSaveService saveService,
                                 TranslatorInteractionService translatorService,
                                 WorkspaceContext workspaceContext)
   {
      super(display, eventBus);
      this.display = display;
      this.display.addFilterConfirmationHandler(this);
      this.eventBus = eventBus;
      this.navigationController = navigationController;
      this.transUnitListDisplay = transUnitListDisplay;
      this.sourceContentsPresenter = sourceContentsPresenter;
      this.targetContentsPresenter = targetContentsPresenter;
      this.saveService = saveService;
      this.translatorService = translatorService;

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
      eventBus.addHandler(NavTransUnitEvent.getType(), this);
      eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), this);
      eventBus.addHandler(TransUnitSaveEvent.TYPE, this);
      eventBus.addHandler(FindMessageEvent.getType(), this);
      eventBus.addHandler(FilterViewEvent.getType(), this);
      eventBus.addHandler(TransUnitUpdatedEvent.getType(), this);
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
   public void onSelectionChange(SelectionChangeEvent event)
   {
      if (hasTargetContentsChanged())
      {
         savePendingChangeBeforeShowingNewSelection();
      }
      else
      {
         showSelection();
      }
   }

   private void showSelection()
   {
      TransUnit selectedTransUnit = dataModel.getSelectedOrNull();
      if (selectedTransUnit != null)
      {
         Log.info("selected id: " + selectedTransUnit.getId());
         sourceContentsPresenter.setValue(selectedTransUnit);
         targetContentsPresenter.setValue(selectedTransUnit, null);
         sourceContentsPresenter.selectedSource();
         targetContentsPresenter.showEditors(0);
         translatorService.transUnitSelected(selectedTransUnit);
      }
   }

   private void savePendingChangeBeforeShowingNewSelection()
   {
      Log.debug("saving pending change: " + targetContentsPresenter.getNewTargets() + " to :" + dataModel.getStaleSelection().debugString());
      saveService.saveTranslation(dataModel.getStaleSelection(), targetContentsPresenter.getNewTargets(), ContentState.NeedReview, new TransUnitSaveService.SaveResultCallback()
      {
         @Override
         public void onSaveSuccess(TransUnit updatedTU)
         {
            dataModel.update(updatedTU);
            Log.info("pending change saved. now show selection.");
            showSelection();
         }

         @Override
         public void onSaveFail()
         {
         }
      });
   }

   public void goToPage(int pageNumber)
   {
      if (hasTargetContentsChanged())
      {
         savePendingChangeAndGoToPageNumber(pageNumber);
      }
      else
      {
         navigationController.gotoPage(pageNumber - 1, false);
      }
   }

   private void savePendingChangeAndGoToPageNumber(final int pageNumber)
   {
      Log.debug("saving pending change: " + targetContentsPresenter.getNewTargets() + " to :" + dataModel.getStaleSelection().debugString());
      saveService.saveTranslation(dataModel.getStaleSelection(), targetContentsPresenter.getNewTargets(), ContentState.NeedReview, new TransUnitSaveService.SaveResultCallback()
      {
         @Override
         public void onSaveSuccess(TransUnit updatedTU)
         {
            dataModel.update(updatedTU);
            Log.info("pending change saved. now got to page number" + pageNumber);
            navigationController.gotoPage(pageNumber - 1, false);
         }

         @Override
         public void onSaveFail()
         {
         }
      });
   }

   @Override
   public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
   {
      initViewOnWorkspaceContext(event.isReadOnly());
   }

   @Override
   public void onNavTransUnit(NavTransUnitEvent event)
   {
      TransUnit selected = dataModel.getSelectedOrNull();
      if (selected == null)
      {
         navigationController.navigateTo(event.getRowType());
      }
      else
      {
         //we want to save any pending state and then move
         onTransUnitSave(new TransUnitSaveEvent(targetContentsPresenter.getNewTargets(), selected.getStatus()).andMoveTo(event.getRowType()));
      }
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
   public void onTransUnitSave(final TransUnitSaveEvent event)
   {
      TransUnit selected = dataModel.getSelectedOrNull();
      if (selected == null)
      {
         return;
      }
      if (event == TransUnitSaveEvent.CANCEL_EDIT_EVENT)
      {
         targetContentsPresenter.setValue(selected, findMessage.getMessage());
      }
      else if (hasStateChange(selected, event.getStatus()))
      {
         proceedToSave(event, selected);
      }
      else if (event.andMove())
      {
         //nothing has changed and it's not cancelling
         navigationController.navigateTo(event.getNavigationType());
      }
   }

   private boolean hasStateChange(TransUnit old, ContentState newStatus)
   {
      //check whether target contents or status has changed
      return !(old.getStatus() == newStatus && Objects.equal(targetContentsPresenter.getNewTargets(), old.getTargets()));
   }

   private void proceedToSave(final TransUnitSaveEvent event, TransUnit selected)
   {
      if (event.getStatus() != ContentState.NeedReview)
      {
         targetContentsPresenter.setToViewMode();
      }
      saveService.saveTranslation(selected, targetContentsPresenter.getNewTargets(), event.getStatus(), new TransUnitSaveService.SaveResultCallback()
      {
         @Override
         public void onSaveSuccess(TransUnit updatedTU)
         {
            dataModel.update(updatedTU);
            if (event.andMove())
            {
               Log.info("save success and now move to " + event.getNavigationType());
               navigationController.navigateTo(event.getNavigationType());
            }
         }

         @Override
         public void onSaveFail()
         {
            targetContentsPresenter.showEditors(0);
         }
      });
   }

   @Override
   public void onFindMessage(FindMessageEvent event)
   {
      findMessage = event;
      transUnitListDisplay.setHighlightString(event.getMessage());
      sourceContentsPresenter.getDisplay().highlightSearch(event.getMessage());
      targetContentsPresenter.getDisplay().setFindMessage(event.getMessage());
   }

   @Override
   public void onFilterView(FilterViewEvent event)
   {
      filterOptions = event;
      if (!event.isCancelFilter())
      {
         if (hasTargetContentsChanged())
         {
            display.showFilterConfirmation();
         }
         else
         {
            hideFilterConfirmationAndDoFiltering();
         }
      }
   }

   private void hideFilterConfirmationAndDoFiltering()
   {
      display.hideFilterConfirmation();
      navigationController.execute(filterOptions);
   }

   private boolean hasTargetContentsChanged()
   {
      return dataModel.hasStaleData(targetContentsPresenter.getNewTargets());
   }

   @Override
   public void saveChangesAndFilter()
   {
      saveAndFilter(ContentState.Approved);
   }

   @Override
   public void saveAsFuzzyAndFilter()
   {
      saveAndFilter(ContentState.NeedReview);
   }

   private void saveAndFilter(ContentState status)
   {
      saveService.saveTranslation(dataModel.getSelectedOrNull(), targetContentsPresenter.getNewTargets(), status, new TransUnitSaveService.SaveResultCallback()
      {
         @Override
         public void onSaveSuccess(TransUnit updatedTU)
         {
            dataModel.update(updatedTU);
            hideFilterConfirmationAndDoFiltering();
         }

         @Override
         public void onSaveFail()
         {
            display.hideFilterConfirmation();
         }
      });
   }

   @Override
   public void discardChangesAndFilter()
   {
      targetContentsPresenter.setValue(dataModel.getSelectedOrNull(), findMessage.getMessage());
      hideFilterConfirmationAndDoFiltering();
   }

   @Override
   public void cancelFilter()
   {
      eventBus.fireEvent(new FilterViewEvent(filterOptions.isFilterTranslated(), filterOptions.isFilterNeedReview(), filterOptions.isFilterUntranslated(), true));
      display.hideFilterConfirmation();
   }

   @Override
   public void onTransUnitUpdated(TransUnitUpdatedEvent event)
   {
      TransUnit updatedTransUnit = event.getUpdateInfo().getTransUnit();
      if (Objects.equal(dataModel.getSelectedOrNull(), updatedTransUnit))
      {
         //TODO current edit has happened. What's the best way to show it to user.
         Log.info("detect concurrent edit. Closing editor");
         eventBus.fireEvent(new NotificationEvent(Warning, "Concurrent edit detected. Reset value for current row"));
         targetContentsPresenter.setToViewMode();
         targetContentsPresenter.setValue(updatedTransUnit, findMessage.getMessage());
         targetContentsPresenter.showEditors(0);
      }
   }
}
