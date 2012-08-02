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
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.service.NavigationController;
import org.zanata.webtrans.client.service.SinglePageDataModel;
import org.zanata.webtrans.client.service.TransUnitSaveService;
import org.zanata.webtrans.client.service.TranslatorInteractionService;
import org.zanata.webtrans.client.ui.FilterViewConfirmationDisplay;
import org.zanata.webtrans.client.view.TransUnitEditDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.inject.Inject;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;
import static org.zanata.webtrans.client.events.NotificationEvent.Severity.*;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransUnitEditPresenter extends WidgetPresenter<TransUnitEditDisplay> implements
      TransUnitSelectionHandler,
      WorkspaceContextUpdateEventHandler,
      NavTransUnitHandler,
      FindMessageHandler,
      FilterViewEventHandler,
      FilterViewConfirmationDisplay.Listener,
      SinglePageDataModel.PageDataChangeListener,
      TransUnitEditDisplay.Listener
{

   private final TransUnitEditDisplay display;
   private final UserWorkspaceContext userWorkspaceContext;
   private final EventBus eventBus;
   private final NavigationController navigationController;
   private final SourceContentsPresenter sourceContentsPresenter;
   private final TargetContentsPresenter targetContentsPresenter;
   private final TranslatorInteractionService translatorService;

   //state we need to keep track of
   private FilterViewEvent filterOptions = FilterViewEvent.DEFAULT;

   private final SinglePageDataModel pageModel;

   @Inject
   public TransUnitEditPresenter(TransUnitEditDisplay display, EventBus eventBus, NavigationController navigationController,
                                 SourceContentsPresenter sourceContentsPresenter,
                                 TargetContentsPresenter targetContentsPresenter,
                                 TranslatorInteractionService translatorService,
                                 TransUnitSaveService transUnitSaveService,
                                 UserWorkspaceContext userWorkspaceContext)
   {
      super(display, eventBus);
      this.display = display;
      this.display.setRowSelectionListener(this);

      this.userWorkspaceContext = userWorkspaceContext;
      this.display.addFilterConfirmationHandler(this);
      this.eventBus = eventBus;
      this.navigationController = navigationController;
      this.sourceContentsPresenter = sourceContentsPresenter;
      this.targetContentsPresenter = targetContentsPresenter;
      this.translatorService = translatorService;

      // we register it here because we can't use eager singleton on it (it references TargetContentsPresenter). And if it's not eagerly created, it won't get created at all!!
      eventBus.addHandler(TransUnitSaveEvent.TYPE, transUnitSaveService);
//      initViewOnWorkspaceContext(userWorkspaceContext.hasReadOnlyAccess());

      pageModel = navigationController.getDataModel();
      pageModel.addDataChangeListener(this);
   }

   //TODO read only is not handled
   private void initViewOnWorkspaceContext(boolean readOnly)
   {
      display.buildTable(sourceContentsPresenter.getDisplays(), targetContentsPresenter.getDisplays());
   }

   @Override
   protected void onBind()
   {
      eventBus.addHandler(NavTransUnitEvent.getType(), this);
      eventBus.addHandler(WorkspaceContextUpdateEvent.getType(), this);
      eventBus.addHandler(FindMessageEvent.getType(), this);
      eventBus.addHandler(FilterViewEvent.getType(), this);
      eventBus.addHandler(TransUnitSelectionEvent.getType(), this);
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
      TransUnit selectedTransUnit = pageModel.getSelectedOrNull();
      if (selectedTransUnit != null)
      {
         Log.debug("selected id: " + selectedTransUnit.getId());
         sourceContentsPresenter.setSelectedSource(pageModel.getCurrentRow());
         targetContentsPresenter.showEditors(pageModel.getCurrentRow(), selectedTransUnit.getId());
         translatorService.transUnitSelected(selectedTransUnit);
      }
   }

   public void goToPage(int pageNumber)
   {
      pageModel.savePendingChangeIfApplicable(targetContentsPresenter.getNewTargets());
      navigationController.gotoPage(pageNumber - 1, false);
   }

   @Override
   public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event)
   {
      userWorkspaceContext.setProjectActive(event.isProjectActive());
      initViewOnWorkspaceContext(userWorkspaceContext.hasReadOnlyAccess());
   }

   @Override
   public void onNavTransUnit(NavTransUnitEvent event)
   {
      pageModel.savePendingChangeIfApplicable(targetContentsPresenter.getNewTargets());
      navigationController.navigateTo(event.getRowType());
   }

   @Override
   public void onFindMessage(FindMessageEvent event)
   {
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
      TransUnit selectedOrNull = pageModel.getSelectedOrNull();
      if (selectedOrNull == null)
      {
         return;
      }
      eventBus.fireEvent(new TransUnitSaveEvent(targetContentsPresenter.getNewTargets(), status, selectedOrNull.getId(), selectedOrNull.getVerNum()));
      hideFilterConfirmationAndDoFiltering();
   }

   @Override
   public void discardChangesAndFilter()
   {
      targetContentsPresenter.setValue(pageModel.getSelectedOrNull());
      hideFilterConfirmationAndDoFiltering();
   }

   @Override
   public void cancelFilter()
   {
      eventBus.fireEvent(new FilterViewEvent(filterOptions.isFilterTranslated(), filterOptions.isFilterNeedReview(), filterOptions.isFilterUntranslated(), true));
      display.hideFilterConfirmation();
   }

   @Override
   public void showDataForCurrentPage(List<TransUnit> transUnits)
   {
      sourceContentsPresenter.showData(transUnits);
      targetContentsPresenter.showData(transUnits);
      display.buildTable(sourceContentsPresenter.getDisplays(), targetContentsPresenter.getDisplays());
   }

   @Override
   public void refreshView(int rowIndexOnPage, TransUnit updatedTransUnit, EditorClientId editorClientId)
   {
      TransUnit selected = pageModel.getSelectedOrNull();
      boolean setFocus = false;
      if (selected != null && Objects.equal(selected.getId(), updatedTransUnit.getId()))
      {
         //updatedTU is our active row
         if (!Objects.equal(editorClientId, translatorService.getCurrentEditorClientId()))
         {
            //TODO current edit has happened. What's the best way to show it to user.
            Log.info("detect concurrent edit. Closing editor");
            // TODO localise
            eventBus.fireEvent(new NotificationEvent(Warning, "Concurrent edit detected. Reset value for current row"));
         }
         else if (updatedTransUnit.getStatus() == ContentState.NeedReview)
         {
            //same user and state is fuzzy means this user is trying to save as fuzzy.
            setFocus = true;
         }
      }
      targetContentsPresenter.updateRow(rowIndexOnPage, updatedTransUnit);
      if (setFocus)
      {
         targetContentsPresenter.setFocus();
      }
   }

   @Override
   public void onRowSelected(int rowIndex)
   {
      pageModel.savePendingChangeIfApplicable(targetContentsPresenter.getNewTargets());
      navigationController.selectByRowIndex(rowIndex);
   }
}
