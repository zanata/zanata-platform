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

import java.util.ArrayList;
import java.util.List;

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
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.service.NavigationController;
import org.zanata.webtrans.client.service.SinglePageDataModel;
import org.zanata.webtrans.client.service.TransUnitSaveService;
import org.zanata.webtrans.client.service.TranslatorInteractionService;
import org.zanata.webtrans.client.ui.FilterViewConfirmationDisplay;
import org.zanata.webtrans.client.view.TransUnitEditDisplay2;
import org.zanata.webtrans.client.view.TransUnitListDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.auth.Identity;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import static org.zanata.webtrans.client.events.NotificationEvent.Severity.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransUnitEditPresenter extends WidgetPresenter<TransUnitEditDisplay2> implements
      TransUnitSelectionHandler,
      WorkspaceContextUpdateEventHandler,
      NavTransUnitHandler,
      LoadingStateChangeEvent.Handler,
      TransUnitSaveEventHandler,
      FindMessageHandler,
      FilterViewEventHandler,
      FilterViewConfirmationDisplay.Listener,
//      TransUnitUpdatedEventHandler,
      SinglePageDataModel.PageDataChangeListener
{

   private final TransUnitEditDisplay2 display;
   private final UserWorkspaceContext userWorkspaceContext;
   private final Identity identity;
   private final TableEditorMessages messages;
   private final EventBus eventBus;
   private final NavigationController navigationController;
   private final TransUnitListDisplay transUnitListDisplay;
   private final SourceContentsPresenter sourceContentsPresenter;
   private final TargetContentsPresenter targetContentsPresenter;
   private final TransUnitSaveService saveService;
   private final TranslatorInteractionService translatorService;

   //state we need to keep track of
   private FilterViewEvent filterOptions = FilterViewEvent.DEFAULT;
   private FindMessageEvent findMessage = FindMessageEvent.DEFAULT;

   private final SinglePageDataModel pageModel;

   //TODO too many constructor dependency
   @Inject
   public TransUnitEditPresenter(TransUnitEditDisplay2 display, EventBus eventBus, NavigationController navigationController,
                                 TransUnitListDisplay transUnitListDisplay,
                                 SourceContentsPresenter sourceContentsPresenter,
                                 TargetContentsPresenter targetContentsPresenter,
                                 TransUnitSaveService saveService,
                                 TranslatorInteractionService translatorService,
                                 UserWorkspaceContext userWorkspaceContext,
                                 Identity identity,
                                 TableEditorMessages messages)
   {
      super(display, eventBus);
      this.display = display;
      this.userWorkspaceContext = userWorkspaceContext;
      this.identity = identity;
      this.messages = messages;
      this.display.addFilterConfirmationHandler(this);
      this.eventBus = eventBus;
      this.navigationController = navigationController;
      this.transUnitListDisplay = transUnitListDisplay;
      this.sourceContentsPresenter = sourceContentsPresenter;
      this.targetContentsPresenter = targetContentsPresenter;
      this.saveService = saveService;
      this.translatorService = translatorService;

      //FIXME this is hardcoded
      sourceContentsPresenter.initWidgets(5);
      targetContentsPresenter.initWidgets(5);
      initViewOnWorkspaceContext(userWorkspaceContext.hasReadOnlyAccess());

      pageModel = navigationController.getDataModel();
      pageModel.addDataChangeListener(this);
   }

   //TODO read only is not handled
   private void initViewOnWorkspaceContext(boolean readOnly)
   {
      display.initView(sourceContentsPresenter.getDisplays(), targetContentsPresenter.getDisplays());
   }

   @Override
   protected void onBind()
   {
      eventBus.addHandler(NavTransUnitEvent.getType(), this);
      eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), this);
      eventBus.addHandler(TransUnitSaveEvent.TYPE, this);
      eventBus.addHandler(FindMessageEvent.getType(), this);
      eventBus.addHandler(FilterViewEvent.getType(), this);
//      eventBus.addHandler(TransUnitUpdatedEvent.getType(), this);
      eventBus.addHandler(TransUnitSelectionEvent.getType(), this);
      transUnitListDisplay.addLoadingStateChangeHandler(this);
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
   public void onTransUnitSelected(TransUnitSelectionEvent event)
   {
      TransUnit oldSelection = pageModel.getOldSelectionOrNull();
      ArrayList<String> currentEditorValues = targetContentsPresenter.getNewTargets();
      if (oldSelection != null && currentEditorValues != null && !Objects.equal(currentEditorValues, oldSelection.getTargets()))
      {
         savePendingChangeBeforeShowingNewSelection(oldSelection, currentEditorValues);
      }
      else
      {
         showSelection();
      }
   }

   private void showSelection()
   {
      TransUnit selectedTransUnit = pageModel.getSelectedOrNull();
      if (selectedTransUnit != null)
      {
         Log.info("selected id: " + selectedTransUnit.getId());
         sourceContentsPresenter.setSelectedSource(pageModel.getCurrentRow());
         targetContentsPresenter.showEditors(pageModel.getCurrentRow());
         translatorService.transUnitSelected(selectedTransUnit);
      }
   }

   private void savePendingChangeBeforeShowingNewSelection(TransUnit old, ArrayList<String> newTargets)
   {
      saveService.saveTranslation(old, newTargets, ContentState.Approved, new TransUnitSaveService.SaveResultCallback()
      {
         @Override
         public void onSaveSuccess(TransUnit updatedTU)
         {
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
      saveService.saveTranslation(pageModel.getSelectedOrNull(), targetContentsPresenter.getNewTargets(), ContentState.Approved, new TransUnitSaveService.SaveResultCallback()
      {
         @Override
         public void onSaveSuccess(TransUnit updatedTU)
         {
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
      userWorkspaceContext.setProjectActive(event.isProjectActive());
      if (userWorkspaceContext.hasReadOnlyAccess())
      {
         eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, messages.notifyReadOnlyWorkspace()));
      }
      else
      {
         eventBus.fireEvent(new NotificationEvent(NotificationEvent.Severity.Info, messages.notifyEditableWorkspace()));
      }
      initViewOnWorkspaceContext(userWorkspaceContext.hasReadOnlyAccess());
   }

   @Override
   public void onNavTransUnit(NavTransUnitEvent event)
   {
      TransUnit selected = pageModel.getSelectedOrNull();
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
//         display.scrollToRow(dataModel.getSelectedOrNull());
      }
   }

   @Override
   public void onTransUnitSave(final TransUnitSaveEvent event)
   {
      TransUnit selected = pageModel.getSelectedOrNull();
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
            if (event.andMove())
            {
               Log.info("save success and now move to " + event.getNavigationType());
               navigationController.navigateTo(event.getNavigationType());
            }
         }

         @Override
         public void onSaveFail()
         {
            //TODO implement
//            targetContentsPresenter.showEditors(pageModel.getCurrentRow());
         }
      });
   }

   @Override
   public void onFindMessage(FindMessageEvent event)
   {
      findMessage = event;
      sourceContentsPresenter.highlightSearch(event.getMessage());
      targetContentsPresenter.highlightSearch(event.getMessage());
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
      TransUnit current = pageModel.getSelectedOrNull();
      ArrayList<String> editorValues = targetContentsPresenter.getNewTargets();
      return current != null && !Objects.equal(current.getTargets(), editorValues);
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
      saveService.saveTranslation(pageModel.getSelectedOrNull(), targetContentsPresenter.getNewTargets(), status, new TransUnitSaveService.SaveResultCallback()
      {
         @Override
         public void onSaveSuccess(TransUnit updatedTU)
         {
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
      targetContentsPresenter.setValue(pageModel.getSelectedOrNull(), findMessage.getMessage());
      hideFilterConfirmationAndDoFiltering();
   }

   @Override
   public void cancelFilter()
   {
      eventBus.fireEvent(new FilterViewEvent(filterOptions.isFilterTranslated(), filterOptions.isFilterNeedReview(), filterOptions.isFilterUntranslated(), true));
      display.hideFilterConfirmation();
   }

//   @Override
//   public void onTransUnitUpdated(TransUnitUpdatedEvent event)
//   {
//      TransUnit selectedTransUnit = pageModel.getSelectedOrNull();
//      if (selectedTransUnit == null)
//      {
//         return;
//      }
//      TransUnit updatedTransUnit = event.getUpdateInfo().getTransUnit();
//      if (Objects.equal(selectedTransUnit.getId(), updatedTransUnit.getId()) && !Objects.equal(event.getEditorClientId(), identity.getEditorClientId()))
//      {
//         //TODO current edit has happened. What's the best way to show it to user.
//         Log.info("detect concurrent edit. Closing editor");
//         // TODO localise
//         eventBus.fireEvent(new NotificationEvent(Warning, "Concurrent edit detected. Reset value for current row"));
//         targetContentsPresenter.setToViewMode();
////         targetContentsPresenter.setValue(updatedTransUnit, findMessage.getMessage());
//         targetContentsPresenter.showEditors(pageModel.getCurrentRow());
//      }
//   }

   @Override
   public void showDataForCurrentPage(List<TransUnit> transUnits)
   {
      sourceContentsPresenter.showData(transUnits);
      targetContentsPresenter.showData(transUnits);
   }

   @Override
   public void refreshView(int rowIndexOnPage, TransUnit updatedTransUnit, EditorClientId editorClientId)
   {
      TransUnit selected = pageModel.getSelectedOrNull();
      boolean setFocus = false;
      if (selected != null && Objects.equal(selected.getId(), updatedTransUnit.getId()))
      {
         //updatedTU is our active row
         if (!Objects.equal(editorClientId, identity.getEditorClientId()))
         {
            //TODO current edit has happened. What's the best way to show it to user.
            Log.info("detect concurrent edit. Closing editor");
            // TODO localise
            eventBus.fireEvent(new NotificationEvent(Warning, "Concurrent edit detected. Reset value for current row"));
         }
         else if (updatedTransUnit.getStatus() == ContentState.NeedReview)
         {
            //same user and state is fuzzy
            setFocus = true;
         }
      }
      targetContentsPresenter.updateRow(rowIndexOnPage, updatedTransUnit);
      if (setFocus)
      {
         targetContentsPresenter.showEditors(pageModel.getCurrentRow());
      }
   }
}
