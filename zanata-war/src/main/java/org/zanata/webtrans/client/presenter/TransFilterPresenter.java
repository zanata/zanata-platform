/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.FilterViewEvent;
import org.zanata.webtrans.client.events.FilterViewEventHandler;
import org.zanata.webtrans.client.events.FindMessageEvent;
import org.zanata.webtrans.client.events.FindMessageHandler;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.UserConfigChangeHandler;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.view.TransFilterDisplay;

import com.google.inject.Inject;

public class TransFilterPresenter extends WidgetPresenter<TransFilterDisplay>
        implements TransFilterDisplay.Listener, FindMessageHandler,
        UserConfigChangeHandler, FilterViewEventHandler {
    private final History history;

    private final UserOptionsService userOptionsService;

    @Inject
    public TransFilterPresenter(final TransFilterDisplay display,
            final EventBus eventBus, final History history,
            UserOptionsService userOptionsService) {
        super(display, eventBus);
        display.setListener(this);
        this.history = history;
        this.userOptionsService = userOptionsService;
    }

    @Override
    protected void onBind() {
        registerHandler(eventBus.addHandler(FindMessageEvent.getType(), this));
        registerHandler(eventBus.addHandler(FilterViewEvent.getType(), this));
        registerHandler(eventBus.addHandler(UserConfigChangeEvent.TYPE, this));

        display.setOptionsState(userOptionsService.getConfigHolder().getState());
    }

    public boolean isFocused() {
        return display.isFocused();
    }

    @Override
    public void searchTerm(String searchTerm) {
        HistoryToken newToken = history.getHistoryToken();
        newToken.setSearchText(searchTerm);
        history.newItem(newToken);
    }

    @Override
    public void messageFilterOptionChanged(Boolean translatedChkValue,
            Boolean fuzzyChkValue, Boolean untranslatedChkValue,
            Boolean approvedChkValue, Boolean rejectedChkValue,
            Boolean hasErrorChkValue) {
        UserConfigHolder configHolder = userOptionsService.getConfigHolder();
        configHolder.setFilterByTranslated(translatedChkValue);
        configHolder.setFilterByFuzzy(fuzzyChkValue);
        configHolder.setFilterByUntranslated(untranslatedChkValue);
        configHolder.setFilterByApproved(approvedChkValue);
        configHolder.setFilterByRejected(rejectedChkValue);
        configHolder.setFilterByHasError(hasErrorChkValue);

        pushFilterHistory(translatedChkValue, fuzzyChkValue,
                untranslatedChkValue, approvedChkValue, rejectedChkValue,
                hasErrorChkValue);
    }

    @Override
    public void onFindMessage(FindMessageEvent event) {
        // this is fired from HistoryEventHandlerService
        display.setSearchTerm(event.getMessage());
    }

    @Override
    protected void onUnbind() {
    }

    @Override
    public void onRevealDisplay() {
    }

    @Override
    public void onUserConfigChanged(UserConfigChangeEvent event) {
        if (event.getView() == MainView.Editor) {
            UserConfigHolder.ConfigurationState configurationState =
                    userOptionsService.getConfigHolder().getState();

            updateFilterStates(configurationState.isFilterByTranslated(),
                    configurationState.isFilterByFuzzy(),
                    configurationState.isFilterByUntranslated(),
                    configurationState.isFilterByApproved(),
                    configurationState.isFilterByRejected(),
                    configurationState.isFilterByHasError());
            pushFilterHistory(configurationState.isFilterByTranslated(),
                    configurationState.isFilterByFuzzy(),
                    configurationState.isFilterByUntranslated(),
                    configurationState.isFilterByApproved(),
                    configurationState.isFilterByRejected(),
                    configurationState.isFilterByHasError());
        }

    }

    @Override
    public void onFilterView(FilterViewEvent event) {
        if (event.isCancelFilter()) {
            updateFilterStates(event.isFilterTranslated(),
                    event.isFilterFuzzy(), event.isFilterUntranslated(),
                    event.isFilterApproved(), event.isFilterRejected(),
                    event.isFilterHasError());
            pushFilterHistory(event.isFilterTranslated(),
                    event.isFilterFuzzy(), event.isFilterUntranslated(),
                    event.isFilterApproved(), event.isFilterRejected(),
                    event.isFilterHasError());

        }
    }

    public void updateFilterStates(boolean filterByTranslated,
            boolean filterByFuzzy, boolean filterByUntranslated,
            boolean filterByApproved, boolean filterByRejected,
            boolean filterByHasError) {
        display.setTranslatedFilter(filterByTranslated);
        display.setNeedReviewFilter(filterByFuzzy);
        display.setUntranslatedFilter(filterByUntranslated);
        display.setApprovedFilter(filterByApproved);
        display.setRejectedFilter(filterByRejected);
        display.setHasErrorFilter(filterByHasError);
    }

    private void pushFilterHistory(boolean filterByTranslated,
            boolean filterByFuzzy, boolean filterByUntranslated,
            boolean filterByApproved, boolean filterByRejected,
            boolean filterByHasError) {
        HistoryToken token = history.getHistoryToken();
        token.setFilterTranslated(filterByTranslated);
        token.setFilterFuzzy(filterByFuzzy);
        token.setFilterUntranslated(filterByUntranslated);
        token.setFilterApproved(filterByApproved);
        token.setFilterRejected(filterByRejected);
        token.setFilterHasError(filterByHasError);
        history.newItem(token);
    }
}
