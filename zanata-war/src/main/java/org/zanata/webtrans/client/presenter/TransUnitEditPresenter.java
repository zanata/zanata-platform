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

import static org.zanata.webtrans.client.events.NotificationEvent.Severity.Warning;

import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.editor.table.TargetContentsPresenter;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FilterViewEventHandler;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.FindMessageHandler;
import org.zanata.webtrans.client.events.LoadingEvent;
import org.zanata.webtrans.client.events.LoadingEventHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEventHandler;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.service.NavigationController;
import org.zanata.webtrans.client.service.TransUnitSaveService;
import org.zanata.webtrans.client.service.TranslatorInteractionService;
import org.zanata.webtrans.client.ui.FilterViewConfirmationDisplay;
import org.zanata.webtrans.client.view.TransUnitEditDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.inject.Inject;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TransUnitEditPresenter extends WidgetPresenter<TransUnitEditDisplay> implements
      TransUnitSelectionHandler,
      FindMessageHandler,
      FilterViewEventHandler,
      FilterViewConfirmationDisplay.Listener,
      NavigationController.PageDataChangeListener,
      TransUnitEditDisplay.Listener,
      TableRowSelectedEventHandler,
      LoadingEventHandler
{

   private final TransUnitEditDisplay display;
   private final EventBus eventBus;
   private final NavigationController navigationController;
   private final SourceContentsPresenter sourceContentsPresenter;
   private final TargetContentsPresenter targetContentsPresenter;
   private final TranslatorInteractionService translatorService;

   //state we need to keep track of
   private FilterViewEvent filterOptions = FilterViewEvent.DEFAULT;
   private TransUnitId selectedId;

   @Inject
   public TransUnitEditPresenter(TransUnitEditDisplay display, EventBus eventBus, NavigationController navigationController,
                                 SourceContentsPresenter sourceContentsPresenter,
                                 TargetContentsPresenter targetContentsPresenter,
                                 TranslatorInteractionService translatorService,
                                 TransUnitSaveService transUnitSaveService)
   {
      super(display, eventBus);
      this.display = display;
      this.display.setRowSelectionListener(this);

      this.display.addFilterConfirmationHandler(this);
      this.eventBus = eventBus;
      this.navigationController = navigationController;
      navigationController.addPageDataChangeListener(this);
      this.sourceContentsPresenter = sourceContentsPresenter;
      this.targetContentsPresenter = targetContentsPresenter;
      this.translatorService = translatorService;

      // we register it here because we can't use eager singleton on it (it references TargetContentsPresenter). And if it's not eagerly created, it won't get created at all!!
      eventBus.addHandler(TransUnitSaveEvent.TYPE, transUnitSaveService);
   }

   @Override
   protected void onBind()
   {
      eventBus.addHandler(FindMessageEvent.getType(), this);
      eventBus.addHandler(FilterViewEvent.getType(), this);
      eventBus.addHandler(TransUnitSelectionEvent.getType(), this);
      eventBus.addHandler(TableRowSelectedEvent.TYPE, this);
      eventBus.addHandler(LoadingEvent.TYPE, this);
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
      TransUnit selection = event.getSelection();
      selectedId = selection.getId();
      Log.debug("selected id: " + selectedId);
      sourceContentsPresenter.setSelectedSource(selectedId);
      targetContentsPresenter.showEditors(selectedId);
      translatorService.transUnitSelected(selection);
   }

   public void goToPage(int pageNumber)
   {
      targetContentsPresenter.savePendingChangesIfApplicable();
      navigationController.gotoPage(pageNumber - 1, false);
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
         if (targetContentsPresenter.currentEditorContentHasChanged())
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
      if (targetContentsPresenter.getCurrentTransUnitIdOrNull() == null)
      {
         return;
      }
      targetContentsPresenter.saveCurrent(status);
      hideFilterConfirmationAndDoFiltering();
   }

   @Override
   public void discardChangesAndFilter()
   {
      targetContentsPresenter.onCancel(targetContentsPresenter.getCurrentTransUnitIdOrNull());
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
   public void refreshView(TransUnit updatedTransUnit, EditorClientId editorClientId, TransUnitUpdated.UpdateType updateType)
   {
      boolean setFocus = false;
      if (Objects.equal(selectedId, updatedTransUnit.getId()))
      {
         if (!Objects.equal(editorClientId, translatorService.getCurrentEditorClientId()))
         {
            //updatedTU is our active row but done by another user
            //TODO current edit has happened. What's the best way to show it to user? May need to put current editing value in some place
            Log.info("detect concurrent edit. Closing editor");
            // TODO localise
            eventBus.fireEvent(new NotificationEvent(Warning, "Concurrent edit detected. Reset value for current row"));
         }
         else if (updateType == TransUnitUpdated.UpdateType.WebEditorSaveFuzzy)
         {
            //same user and update type is save fuzzy
            setFocus = true;
         }
      }
      targetContentsPresenter.updateRow(updatedTransUnit);
      if (setFocus)
      {
         targetContentsPresenter.setFocus();
      }
   }

   @Override
   public void onRowSelected(int rowIndexOnPage)
   {
      if (navigationController.getCurrentRowIndexOnPage() != rowIndexOnPage)
      {
         Log.info("current row:" + navigationController.getCurrentRowIndexOnPage() + " rowSelected:" + rowIndexOnPage);
         targetContentsPresenter.savePendingChangesIfApplicable();
         navigationController.selectByRowIndex(rowIndexOnPage);
         display.applySelectedStyle(rowIndexOnPage);
      }
   }

   public void startEditing()
   {
      if (selectedId != null)
      {
         targetContentsPresenter.setFocus();
      }
      else
      {
         // select first row
         onRowSelected(0);
      }
   }

   @Override
   public void onTableRowSelected(TableRowSelectedEvent event)
   {
      TransUnitId selectedId = event.getSelectedId();
      int rowIndex = navigationController.findRowIndexById(selectedId);
      if (rowIndex != NavigationController.UNSELECTED)
      {
         onRowSelected(rowIndex);
      }
   }

   @Override
   public void onLoading(LoadingEvent event)
   {
      if (event == LoadingEvent.START_EVENT)
      {
         display.showLoading();
      }
      else if (event == LoadingEvent.FINISH_EVENT)
      {
         display.hideLoading();
      }
   }
}
