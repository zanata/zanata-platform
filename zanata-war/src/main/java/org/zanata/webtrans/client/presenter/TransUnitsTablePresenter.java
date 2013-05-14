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

import java.util.Collections;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.common.ContentState;
import org.zanata.webtrans.client.events.CheckStateHasChangedEvent;
import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FilterViewEventHandler;
import org.zanata.webtrans.client.events.LoadingEvent;
import org.zanata.webtrans.client.events.LoadingEventHandler;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.RefreshPageEvent;
import org.zanata.webtrans.client.events.RefreshPageEventHandler;
import org.zanata.webtrans.client.events.RequestPageValidationEvent;
import org.zanata.webtrans.client.events.RequestPageValidationHandler;
import org.zanata.webtrans.client.events.ReviewModeChangeEvent;
import org.zanata.webtrans.client.events.ReviewModeChangeEventHandler;
import org.zanata.webtrans.client.events.RunValidationEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEventHandler;
import org.zanata.webtrans.client.events.TransUnitSaveEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.service.NavigationService;
import org.zanata.webtrans.client.service.TransUnitSaveService;
import org.zanata.webtrans.client.service.TranslatorInteractionService;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.ui.FilterViewConfirmationDisplay;
import org.zanata.webtrans.client.ui.GoToRowLink;
import org.zanata.webtrans.client.ui.InlineLink;
import org.zanata.webtrans.client.view.ReviewContentsDisplay;
import org.zanata.webtrans.client.view.SourceContentsDisplay;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.client.view.TransUnitsTableDisplay;
import org.zanata.webtrans.shared.auth.EditorClientId;
import org.zanata.webtrans.shared.model.TransHistoryItem;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.TransUnitUpdated;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;

import static org.zanata.webtrans.client.events.NotificationEvent.Severity.*;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
// @formatter:off
public class TransUnitsTablePresenter extends WidgetPresenter<TransUnitsTableDisplay> implements
      TransUnitSelectionHandler,
      FilterViewEventHandler,
      FilterViewConfirmationDisplay.Listener,
      NavigationService.PageDataChangeListener,
      TransUnitsTableDisplay.Listener,
      TableRowSelectedEventHandler,
      LoadingEventHandler,
      RefreshPageEventHandler, UserConfigChangeHandler,
      RequestPageValidationHandler,
      ReviewModeChangeEventHandler
// @formatter:on
{

   private final TransUnitsTableDisplay display;
   private ReviewPresenter reviewPresenter;
   private final TranslationHistoryPresenter translationHistoryPresenter;
   private final Provider<GoToRowLink> goToRowLinkProvider;
   private final WebTransMessages messages;
   private final EventBus eventBus;
   private final NavigationService navigationService;
   private final SourceContentsPresenter sourceContentsPresenter;
   private final TargetContentsPresenter targetContentsPresenter;
   private final TranslatorInteractionService translatorService;

   private final UserOptionsService userOptionsService;

   // state we need to keep track of
   private FilterViewEvent filterOptions = FilterViewEvent.DEFAULT;
   private boolean isInReviewMode = false;
   // In case of cancelling a filter
   private FilterViewEvent previousFilterOptions = FilterViewEvent.DEFAULT;
   private TransUnitId selectedId;
   private String findMessage;


   @Inject
   // @formatter:off
   public TransUnitsTablePresenter(TransUnitsTableDisplay display, EventBus eventBus, NavigationService navigationService,
                                   SourceContentsPresenter sourceContentsPresenter,
                                   TargetContentsPresenter targetContentsPresenter,
                                   TranslatorInteractionService translatorService,
                                   TranslationHistoryPresenter translationHistoryPresenter,
                                   Provider<GoToRowLink> goToRowLinkProvider,
                                   WebTransMessages messages, UserOptionsService userOptionsService)
   // @formatter:on
   {
      super(display, eventBus);
      this.display = display;
      this.translationHistoryPresenter = translationHistoryPresenter;
      this.goToRowLinkProvider = goToRowLinkProvider;
      this.messages = messages;
      this.display.setRowSelectionListener(this);

      this.display.addFilterConfirmationHandler(this);
      this.eventBus = eventBus;
      this.navigationService = navigationService;
      navigationService.addPageDataChangeListener(this);
      this.sourceContentsPresenter = sourceContentsPresenter;
      this.targetContentsPresenter = targetContentsPresenter;
      this.translatorService = translatorService;
      this.userOptionsService = userOptionsService;
   }

   @Override
   protected void onBind()
   {
      registerHandler(eventBus.addHandler(FilterViewEvent.getType(), this));
      registerHandler(eventBus.addHandler(TransUnitSelectionEvent.getType(), this));
      registerHandler(eventBus.addHandler(TableRowSelectedEvent.TYPE, this));
      registerHandler(eventBus.addHandler(LoadingEvent.TYPE, this));
      registerHandler(eventBus.addHandler(RefreshPageEvent.TYPE, this));
      registerHandler(eventBus.addHandler(UserConfigChangeEvent.TYPE, this));
      registerHandler(eventBus.addHandler(RequestPageValidationEvent.TYPE, this));
      registerHandler(eventBus.addHandler(ReviewModeChangeEvent.TYPE, this));

      display.setThemes(userOptionsService.getConfigHolder().getState().getDisplayTheme().name());
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
      reviewPresenter.setSelected(selectedId);
      targetContentsPresenter.setSelected(selectedId);
      display.ensureVisible(targetContentsPresenter.getCurrentDisplay());
      translatorService.transUnitSelected(selection);
   }

   public void goToPage(int pageNumber)
   {
      targetContentsPresenter.savePendingChangesIfApplicable();
      navigationService.gotoPage(pageNumber - 1);
   }

   @Override
   public void onFilterView(FilterViewEvent event)
   {
      previousFilterOptions = filterOptions;
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
      navigationService.execute(filterOptions);
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
      eventBus.fireEvent(new FilterViewEvent(previousFilterOptions.isFilterTranslated(), previousFilterOptions.isFilterNeedReview(), previousFilterOptions.isFilterUntranslated(), previousFilterOptions.isFilterHasError(), true, previousFilterOptions.getEnabledValidationIds()));
      display.hideFilterConfirmation();
   }

   @Override
   public void showDataForCurrentPage(List<TransUnit> transUnits)
   {
      sourceContentsPresenter.showData(transUnits);
      reviewPresenter.showData(transUnits);
      targetContentsPresenter.showData(transUnits);
      if (isInReviewMode)
      {
         display.buildTable(sourceContentsPresenter.getDisplays(), reviewPresenter.getDisplays());
      }
      else
      {
         display.buildTable(sourceContentsPresenter.getDisplays(), targetContentsPresenter.getDisplays());
      }
   }

   @Override
   public void onRefreshPage(RefreshPageEvent event)
   {
      if (event == RefreshPageEvent.REFRESH_CODEMIRROR_EVENT)
      {
         display.delayRefresh();
      }
      else if (event == RefreshPageEvent.REDRAW_PAGE_EVENT)
      {
         buildTableForEditor();
      }
   }

   private void buildTableForEditor()
   {
      targetContentsPresenter.savePendingChangesIfApplicable();
      List<TransUnit> currentPageValues = navigationService.getCurrentPageValues();
      sourceContentsPresenter.showData(currentPageValues);
      targetContentsPresenter.showData(currentPageValues);
      reviewPresenter.showData(currentPageValues);
      TransUnitId currentSelected = sourceContentsPresenter.getCurrentTransUnitIdOrNull();
      if (currentSelected != null)
      {
         sourceContentsPresenter.setSelectedSource(currentSelected);
         targetContentsPresenter.setSelected(currentSelected);
         reviewPresenter.setSelected(currentSelected);
      }
      if (isInReviewMode)
      {
         display.buildTable(sourceContentsPresenter.getDisplays(), reviewPresenter.getDisplays());
      }
      else
      {
         display.buildTable(sourceContentsPresenter.getDisplays(), targetContentsPresenter.getDisplays());
      }
   }

   @Override
   public void onReviewModeChange(ReviewModeChangeEvent event)
   {
      isInReviewMode = (event == ReviewModeChangeEvent.CHANGE_TO_REVIEW_MODE);
      buildTableForEditor();
   }

   @Override
   public void refreshRow(TransUnit updatedTransUnit, EditorClientId editorClientId, TransUnitUpdated.UpdateType updateType)
   {
      // TODO rhbz953734 - need to also refresh in review presenter
      if (isInReviewMode)
      {
         GoToRowLink goToRowLink = goToRowLinkProvider.get();

//         goToRowLink.prepare("");
//         eventBus.fireEvent(new NotificationEvent(Warning, "Translation has changed", ));
      }
      if (updateFromCurrentUsersEditorSave(editorClientId, updateType))
      {
         // the TransUnitUpdatedEvent is from current user's save action.
         // Ignored.
         return;
      }
      if (Objects.equal(selectedId, updatedTransUnit.getId()) && !Objects.equal(editorClientId, translatorService.getCurrentEditorClientId()))
      {
         // updatedTU is our active row but done by another user
         eventBus.fireEvent(new NotificationEvent(Error, messages.concurrentEdit()));
         if (targetContentsPresenter.currentEditorContentHasChanged())
         {
            translationHistoryPresenter.popupAndShowLoading(messages.concurrentEditTitle());
            TransHistoryItem latest = new TransHistoryItem(updatedTransUnit.getVerNum().toString(), updatedTransUnit.getTargets(), updatedTransUnit.getStatus(), updatedTransUnit.getLastModifiedBy(), updatedTransUnit.getLastModifiedTime());
            translationHistoryPresenter.displayEntries(latest, Collections.<TransHistoryItem> emptyList());
         }
      }
      targetContentsPresenter.updateRow(updatedTransUnit);
   }

   // update type is web editor save or web editor save fuzzy and coming from
   // current user
   private boolean updateFromCurrentUsersEditorSave(EditorClientId editorClientId, TransUnitUpdated.UpdateType updateType)
   {
      return Objects.equal(editorClientId, translatorService.getCurrentEditorClientId()) && (updateType == TransUnitUpdated.UpdateType.WebEditorSave || updateType == TransUnitUpdated.UpdateType.WebEditorSaveFuzzy);
   }

   @Override
   public void highlightSearch(String findMessage)
   {
      this.findMessage = findMessage;
      sourceContentsPresenter.highlightSearch(findMessage);
      targetContentsPresenter.highlightSearch(findMessage);
   }

   @Override
   public void refreshView()
   {
      List<TargetContentsDisplay> targetContentsDisplays = targetContentsPresenter.getDisplays();
      List<SourceContentsDisplay> sourceContentsDisplays = sourceContentsPresenter.getDisplays();
      List<ReviewContentsDisplay> reviewContentsDisplays = reviewPresenter.getDisplays();
      for (int i = 0; i < targetContentsDisplays.size(); i++)
      {
         TargetContentsDisplay targetDisplay = targetContentsDisplays.get(i);
         SourceContentsDisplay sourceDisplay = sourceContentsDisplays.get(i);
         ReviewContentsDisplay reviewDisplay = reviewContentsDisplays.get(i);
         targetDisplay.refresh();
         sourceDisplay.refresh();
         reviewDisplay.refresh();
         if (!Strings.isNullOrEmpty(findMessage))
         {
            targetDisplay.highlightSearch(findMessage);
            sourceDisplay.highlightSearch(findMessage);
         }
      }
   }

   @Override
   public void onRowSelected(int rowIndexOnPage)
   {
      onRowSelected(rowIndexOnPage, false);
   }

   @Override
   public void onTableRowSelected(TableRowSelectedEvent event)
   {
      TransUnitId selectedId = event.getSelectedId();
      int rowIndex = navigationService.findRowIndexById(selectedId);
      if (rowIndex != NavigationService.UNDEFINED)
      {
         onRowSelected(rowIndex, event.isSuppressSavePending());
      }
   }

   private void onRowSelected(int rowIndexOnPage, boolean suppressSavePending)
   {
      if (navigationService.getCurrentRowIndexOnPage() != rowIndexOnPage)
      {
         Log.info("current row:" + navigationService.getCurrentRowIndexOnPage() + " rowSelected:" + rowIndexOnPage);
         if (!suppressSavePending)
         {
            targetContentsPresenter.savePendingChangesIfApplicable();
         }
         navigationService.selectByRowIndex(rowIndexOnPage);
         display.applySelectedStyle(rowIndexOnPage);
      }
   }

   @Override
   public void onLoading(LoadingEvent event)
   {
      if (event == LoadingEvent.START_EVENT)
      {
         display.showLoading(true);
      }
      else if (event == LoadingEvent.FINISH_EVENT)
      {
         display.showLoading(false);
      }
   }

   /**
    * For testing only. Will not work in GWT compiled mode.
    * 
    * @param selectedId current selected id
    */
   protected void setStateForTesting(TransUnitId selectedId)
   {
      if (!GWT.isClient())
      {
         this.selectedId = selectedId;
      }
   }

   @Override
   public void onUserConfigChanged(UserConfigChangeEvent event)
   {
      display.setThemes(userOptionsService.getConfigHolder().getState().getDisplayTheme().name());
   }

   @Override
   public void onRequestPageValidation(RequestPageValidationEvent event)
   {
      List<SourceContentsDisplay> sourceDisplays = sourceContentsPresenter.getDisplays();
      List<TargetContentsDisplay> targetDisplays = targetContentsPresenter.getDisplays();
      List<ReviewContentsDisplay> reviewContentsDisplays = reviewPresenter.getDisplays();

      for (int i = 0; i < sourceContentsPresenter.getDisplays().size(); i++)
      {
         SourceContentsDisplay sourceDisplay = sourceDisplays.get(i);
         TargetContentsDisplay targetDisplay = targetDisplays.get(i);
         ReviewContentsDisplay reviewDisplay = reviewContentsDisplays.get(i);

         String source = sourceDisplay.getSourcePanelList().get(0).getSource();
         String target = targetDisplay.getEditors().get(0).getText();

         RunValidationEvent runValidationEvent = new RunValidationEvent(source, target, false);
         if (isInReviewMode)
         {
            runValidationEvent.addWidget(reviewDisplay);
         }
         else
         {
            runValidationEvent.addWidget(targetDisplay);
         }

         eventBus.fireEvent(runValidationEvent);
      }
   }

   public void setReviewPresenter(ReviewPresenter reviewPresenter)
   {
      this.reviewPresenter = reviewPresenter;
   }
}
