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

import javax.inject.Provider;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.RequestValidationEvent;
import org.zanata.webtrans.client.events.TableRowSelectedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEvent;
import org.zanata.webtrans.client.events.TransUnitUpdatedEventHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.ui.HasSelectableSource;
import org.zanata.webtrans.client.view.SourceContentsDisplay;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.util.Finds;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.RefreshPageEvent;
import org.zanata.webtrans.client.events.RefreshPageEventHandler;
import org.zanata.webtrans.client.events.ReferenceVisibleEvent;
import org.zanata.webtrans.client.events.ReferenceVisibleEventHandler;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.shared.model.Locale;
import org.zanata.webtrans.shared.rpc.GetTargetForLocale;
import org.zanata.webtrans.shared.rpc.GetTargetForLocaleResult;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public class SourceContentsPresenter implements ClickHandler,
        UserConfigChangeHandler, TransUnitUpdatedEventHandler,
        ReferenceVisibleEventHandler, RefreshPageEventHandler {
    private final EventBus eventBus;
    private final Provider<SourceContentsDisplay> displayProvider;
    private final UserConfigHolder configHolder;
    private final CachingDispatchAsync dispatcher;

    // states
    private List<SourceContentsDisplay> displayList = Collections.emptyList();
    private TransUnitId currentTransUnitId;
    private HasSelectableSource selectedSource;
    private Boolean isReferenceShowing = false;
    private Locale selectedReferenceLocale;

    @Inject
    public SourceContentsPresenter(EventBus eventBus,
            Provider<SourceContentsDisplay> displayProvider,
            CachingDispatchAsync dispatcher,
            UserConfigHolder configHolder) {
        this.eventBus = eventBus;
        this.displayProvider = displayProvider;
        this.configHolder = configHolder;
        this.dispatcher = dispatcher;
        eventBus.addHandler(UserConfigChangeEvent.TYPE, this);
        eventBus.addHandler(TransUnitUpdatedEvent.getType(), this);
        eventBus.addHandler(ReferenceVisibleEvent.getType(), this);
        eventBus.addHandler(RefreshPageEvent.TYPE, this);
    }

    /**
     * Select first source in the list when row is selected or reselect previous
     * selected one
     *
     */
    public void setSelectedSource(TransUnitId id) {
        currentTransUnitId = id;
        Log.debug("source content selected id:" + id);

        Optional<SourceContentsDisplay> sourceContentsView =
                Finds.findDisplayById(displayList, id);
        if (sourceContentsView.isPresent()) {
            List<HasSelectableSource> sourcePanelList =
                    sourceContentsView.get().getSourcePanelList();
            Optional<HasSelectableSource> selectedSource =
                    tryFindSelectedSourcePanel(sourcePanelList);
            if (selectedSource.isPresent()) {
                selectedSource.get().clickSelf();
            } else {
                // by default select the first one
                sourcePanelList.get(0).clickSelf();
            }
        }
    }

    private Optional<HasSelectableSource> tryFindSelectedSourcePanel(
            List<HasSelectableSource> sourcePanelList) {
        return Iterables.tryFind(sourcePanelList,
                input -> input == selectedSource);
    }

    public String getSelectedSource() {
        return selectedSource == null ? null : selectedSource.getSource();
    }

    public void showData(List<TransUnit> transUnits) {
        selectedSource = null; // clear cache
        ImmutableList.Builder<SourceContentsDisplay> builder =
                ImmutableList.builder();
        for (TransUnit transUnit : transUnits) {
            SourceContentsDisplay display = displayProvider.get();
            display.setValue(transUnit);
            display.setSourceSelectionHandler(this);
            builder.add(display);
        }
        displayList = builder.build();
        if (isReferenceShowing) {
            showReference();
        }
    }

    public List<SourceContentsDisplay> getDisplays() {
        return displayList;
    }

    public void highlightSearch(String message) {
        for (SourceContentsDisplay sourceContentsDisplay : displayList) {
            sourceContentsDisplay.highlightSearch(message);
        }
    }

    @Override
    public void onClick(ClickEvent event) {
        if (event.getSource() instanceof HasSelectableSource) {
            HasSelectableSource previousSource = selectedSource;

            selectedSource = (HasSelectableSource) event.getSource();
            ensureRowSelection(selectedSource.getId());

            if (previousSource != null) {
                previousSource.setSelected(false);
            }

            selectedSource.setSelected(true);

            Log.debug("Selected source: " + selectedSource.getSource());
            // TODO this is firing every time we click.
            eventBus.fireEvent(RequestValidationEvent.EVENT);
        }
    }

    private void ensureRowSelection(TransUnitId id) {
        if (!Objects.equal(id, currentTransUnitId)) {
            eventBus.fireEvent(new TableRowSelectedEvent(id));
        }
    }

    public TransUnitId getCurrentTransUnitIdOrNull() {
        return currentTransUnitId;
    }

    @Override
    public void onUserConfigChanged(UserConfigChangeEvent event) {
        for (SourceContentsDisplay sourceContentsDisplay : displayList) {
            sourceContentsDisplay.toggleTransUnitDetails(configHolder
                    .getState().isShowOptionalTransUnitDetails());
        }
    }

    @Override
    public void onTransUnitUpdated(TransUnitUpdatedEvent event) {
        Optional<SourceContentsDisplay> sourceContentsView =
                Finds.findDisplayById(displayList, event.getUpdateInfo()
                        .getTransUnit().getId());
        if (sourceContentsView.isPresent()) {
            sourceContentsView.get().updateTransUnitDetails(
                    event.getUpdateInfo().getTransUnit());
            sourceContentsView.get().refresh();
        }
    }


    @Override
    public void onShowHideReference(ReferenceVisibleEvent event) {
        if (event.isVisible()) {
            isReferenceShowing = true;
            selectedReferenceLocale = event.getSelectedLocale();
            showReference();
        } else {
            hideReference();
        }
    }

    private void showReference() {
        for (final SourceContentsDisplay display : displayList) {
            GetTargetForLocale action = new GetTargetForLocale(display.getId(),
                    selectedReferenceLocale);
            dispatcher.execute(action,
                    new AsyncCallback<GetTargetForLocaleResult>() {
                @Override
                public void onFailure(Throwable caught) {
                    eventBus.fireEvent(new NotificationEvent(NotificationEvent
                            .Severity.Error, "Failed to fetch target"));
                }

                @Override
                public void onSuccess(GetTargetForLocaleResult result) {
                    display.showReference(result.getTarget());
                }
            });
        }
    }

    public void hideReference() {
        for (SourceContentsDisplay display : displayList) {
            display.hideReference();
        }
        isReferenceShowing = false;
    }

    @Override
    public void onRefreshPage(RefreshPageEvent event) {
        if (isReferenceShowing) {
            showReference();
        }
    }

    /**
     * Get the source string for a trans unit on the current page. This will be
     * the currently selected plural form if any is selected.
     *
     * @param id
     *            for the trans unit to check
     * @return currently selected plural, or the first plural if none is
     *         selected. The value will be absent if the trans unit is not on
     *         the current page.
     */
    public Optional<String> getSourceContent(TransUnitId id) {
        Optional<SourceContentsDisplay> view =
                Finds.findDisplayById(displayList, id);
        if (view.isPresent()) {
            List<HasSelectableSource> sourcePanelList =
                    view.get().getSourcePanelList();
            Optional<HasSelectableSource> selectedSourceOptional =
                    tryFindSelectedSourcePanel(sourcePanelList);
            if (selectedSourceOptional.isPresent()) {
                return Optional.of(selectedSourceOptional.get().getSource());
            } else {
                // by default return the first one
                return Optional.of(sourcePanelList.get(0).getSource());
            }
        } else {
            return Optional.absent();
        }
    }
}
