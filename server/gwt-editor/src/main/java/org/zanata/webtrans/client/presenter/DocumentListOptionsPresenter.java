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

import java.util.ArrayList;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.NotificationEvent;
import org.zanata.webtrans.client.events.UserConfigChangeEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.service.UserOptionsService;
import org.zanata.webtrans.client.view.DocumentListOptionsDisplay;
import org.zanata.webtrans.client.view.OptionsDisplay;
import org.zanata.webtrans.shared.model.UserOptions;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.rpc.LoadOptionsAction;
import org.zanata.webtrans.shared.rpc.LoadOptionsResult;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

public class DocumentListOptionsPresenter extends
        WidgetPresenter<DocumentListOptionsDisplay> implements
        DocumentListOptionsDisplay.Listener,
        OptionsDisplay.CommonOptionsListener,
        WorkspaceContextUpdateEventHandler {
    private final CachingDispatchAsync dispatcher;
    private final UserWorkspaceContext userWorkspaceContext;
    private final UserOptionsService userOptionsService;
    private final WebTransMessages messages;

    @Inject
    public DocumentListOptionsPresenter(WebTransMessages messages,
            DocumentListOptionsDisplay display,
            EventBus eventBus, UserWorkspaceContext userWorkspaceContext,
            CachingDispatchAsync dispatcher,
            UserOptionsService userOptionsService) {
        super(display, eventBus);
        this.userWorkspaceContext = userWorkspaceContext;
        this.dispatcher = dispatcher;
        this.userOptionsService = userOptionsService;
        this.messages = messages;
    }

    @Override
    protected void onBind() {
        registerHandler(eventBus.addHandler(
                WorkspaceContextUpdateEvent.getType(), this));
        display.setListener(this);

        if (userWorkspaceContext.hasReadOnlyAccess()) {
            setReadOnly(true);
        }

        // set options default values
        display.setOptionsState(userOptionsService.getConfigHolder().getState());
    }

    @Override
    public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event) {
        userWorkspaceContext.setProjectActive(event.isProjectActive());
        userWorkspaceContext.getWorkspaceContext().getWorkspaceId()
                .getProjectIterationId().setProjectType(event.getProjectType());
        setReadOnly(userWorkspaceContext.hasReadOnlyAccess());
    }

    private void setReadOnly(boolean readOnly) {
        if (readOnly) {
            loadDefaultOptions();
        }
    }

    @Override
    public void onPageSizeClick(int pageSize) {
        if (userOptionsService.getConfigHolder().getState()
                .getDocumentListPageSize() != pageSize) {
            userOptionsService.getConfigHolder().setDocumentListPageSize(
                    pageSize);
            eventBus.fireEvent(UserConfigChangeEvent.DOCUMENT_CONFIG_CHANGE_EVENT);
        }
    }

    @Override
    protected void onUnbind() {
    }

    @Override
    public void onRevealDisplay() {
    }

    @Override
    public void persistOptionChange() {
        userOptionsService.persistOptionChange(userOptionsService
            .getDocumentListOptions());
    }

    @Override
    public void loadOptions() {
        ArrayList<String> prefixes = new ArrayList<String>();
        prefixes.add(UserOptions.doc());
        prefixes.add(UserOptions.common());

        dispatcher.execute(new LoadOptionsAction(prefixes),
                new AsyncCallback<LoadOptionsResult>() {
                    @Override
                    public void onFailure(Throwable caught) {
                        eventBus.fireEvent(new NotificationEvent(
                                NotificationEvent.Severity.Warning,
                                "Unable to load user options"));
                    }

                    @Override
                    public void onSuccess(LoadOptionsResult result) {
                        userOptionsService.getConfigHolder().setState(
                                result.getConfiguration());
                        display.setOptionsState(userOptionsService
                                .getConfigHolder().getState());
                        eventBus.fireEvent(UserConfigChangeEvent.DOCUMENT_CONFIG_CHANGE_EVENT);
                        eventBus.fireEvent(new NotificationEvent(
                                NotificationEvent.Severity.Warning,
                                messages.loadedUserOptions()));
                    }
                });
    }

    @Override
    public void loadDefaultOptions() {
        userOptionsService.loadDocumentListDefaultOptions();
        display.setOptionsState(userOptionsService.getConfigHolder().getState());

        eventBus.fireEvent(UserConfigChangeEvent.DOCUMENT_CONFIG_CHANGE_EVENT);
        eventBus.fireEvent(new NotificationEvent(
                NotificationEvent.Severity.Warning,
                messages.restoreToDefaultOptions()));
    }
}
