/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.common.LocaleId;
import org.zanata.webtrans.client.events.InsertStringInEditorEvent;
import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.events.TransUnitSelectionEvent;
import org.zanata.webtrans.client.events.TransUnitSelectionHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.rpc.CachingDispatchAsync;
import org.zanata.webtrans.client.view.GlossaryDisplay;
import org.zanata.webtrans.shared.model.GlossaryResultItem;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;
import org.zanata.webtrans.shared.model.WorkspaceId;
import org.zanata.webtrans.shared.rpc.GetGlossary;
import org.zanata.webtrans.shared.rpc.GetGlossaryResult;
import org.zanata.webtrans.shared.rpc.HasSearchType.SearchType;

import com.allen_sauer.gwt.log.client.Log;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class GlossaryPresenter extends WidgetPresenter<GlossaryDisplay>
        implements GlossaryDisplay.Listener, TransUnitSelectionHandler {
    private final UserWorkspaceContext userWorkspaceContext;
    private final CachingDispatchAsync dispatcher;
    private final GlossaryDetailsPresenter glossaryDetailsPresenter;
    private final WebTransMessages messages;
    private GetGlossary submittedRequest = null;
    private GetGlossary lastRequest = null;
    private KeyShortcutPresenter keyShortcutPresenter;

    private boolean isFocused;

    @Inject
    public GlossaryPresenter(GlossaryDisplay display, EventBus eventBus,
            CachingDispatchAsync dispatcher, final WebTransMessages messages,
            GlossaryDetailsPresenter glossaryDetailsPresenter,
            UserWorkspaceContext userWorkspaceContext,
            KeyShortcutPresenter keyShortcutPresenter) {
        super(display, eventBus);
        this.dispatcher = dispatcher;
        this.userWorkspaceContext = userWorkspaceContext;
        this.glossaryDetailsPresenter = glossaryDetailsPresenter;
        this.keyShortcutPresenter = keyShortcutPresenter;
        this.messages = messages;
    }

    @Override
    protected void onBind() {
        glossaryDetailsPresenter.onBind();
        keyShortcutPresenter.register(KeyShortcut.Builder.builder()
                .addKey(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER))
                .setContext(ShortcutContext.Glossary)
                .setDescription(messages.searchGlossary())
                .setHandler(event -> fireSearchEvent()).build());

        registerHandler(eventBus.addHandler(TransUnitSelectionEvent.getType(),
                this));

        display.setListener(this);
        glossaryDetailsPresenter.setGlossaryListener(this);
    }

    @Override
    public void clearContent() {
        display.getGlossaryTextBox().setText("");
        display.clearTableContent();
    }

    @Override
    public void fireSearchEvent() {
        String query = display.getGlossaryTextBox().getText();
        createGlossaryRequest(query, display.getSearchType().getValue());
    }

    private void createGlossaryRequest(final String query,
            GetGlossary.SearchType searchType) {
        LocaleId srcLocale = LocaleId.EN_US;
        if (userWorkspaceContext.getSelectedDoc().getSourceLocale() != null) {
            srcLocale = userWorkspaceContext.getSelectedDoc().getSourceLocale();
        }
        WorkspaceId workspaceId =
                userWorkspaceContext.getWorkspaceContext().getWorkspaceId();

        final GetGlossary action =
                new GetGlossary(query,
                        workspaceId.getProjectIterationId(),
                        workspaceId.getLocaleId(),
                        srcLocale, searchType);
        scheduleGlossaryRequest(action);
    }

    public void createGlossaryRequestForTransUnit(TransUnit transUnit) {
        StringBuilder sources = new StringBuilder();
        for (String source : transUnit.getSources()) {
            sources.append(source);
            sources.append(" ");
        }
        SearchType searchType = GetGlossary.SearchType.FUZZY;
        createGlossaryRequest(sources.toString(), searchType);
    }

    private void scheduleGlossaryRequest(GetGlossary action) {
        lastRequest = action;
        if (submittedRequest == null) {
            submitGlossaryRequest(action);
        } else {
            Log.debug("blocking glossary request until outstanding request returns");
        }
    }

    private void submitGlossaryRequest(GetGlossary action) {
        display.startProcessing();
        Log.debug("submitting glossary request");
        dispatcher.execute(action, new AsyncCallback<GetGlossaryResult>() {
            @Override
            public void onFailure(Throwable caught) {
                Log.error(caught.getMessage(), caught);
                display.stopProcessing(false);
                submittedRequest = null;
            }

            @Override
            public void onSuccess(GetGlossaryResult result) {
                if (result.getRequest().equals(lastRequest)) {
                    Log.debug("received glossary result for query");
                    displayGlossaryResult(result);
                    lastRequest = null;
                } else {
                    Log.debug("ignoring old glossary result for query");
                    display.stopProcessing(false);
                }
                submittedRequest = null;
                if (lastRequest != null) {
                    // submit the waiting request
                    submitGlossaryRequest(lastRequest);
                }
            }
        });
        submittedRequest = action;
    }

    private void displayGlossaryResult(GetGlossaryResult result) {
        String query = submittedRequest.getQuery();
        display.getGlossaryTextBox().setText(query);
        display.getSearchType().setValue(submittedRequest.getSearchType());

        if (!result.getGlossaries().isEmpty()) {
            display.renderTable(result.getGlossaries());
            display.stopProcessing(true);
        } else {
            display.stopProcessing(false);
        }
    }

    @Override
    protected void onUnbind() {
    }

    @Override
    protected void onRevealDisplay() {
    }

    public boolean isFocused() {
        return isFocused;
    }

    @Override
    public void fireCopyEvent(GlossaryResultItem item) {
        eventBus.fireEvent(new InsertStringInEditorEvent(item.getSource(), item
                .getTarget()));
    }

    @Override
    public void showGlossaryDetail(GlossaryResultItem item) {
        glossaryDetailsPresenter.show(item);
    }

    @Override
    public void onTransUnitSelected(TransUnitSelectionEvent event) {
        createGlossaryRequestForTransUnit(event.getSelection());
    }

    @Override
    public void onFocus(boolean isFocused) {
        keyShortcutPresenter.setContextActive(ShortcutContext.Glossary,
                isFocused);
        keyShortcutPresenter.setContextActive(ShortcutContext.Navigation,
                !isFocused);
        keyShortcutPresenter.setContextActive(ShortcutContext.Edit, !isFocused);
        this.isFocused = isFocused;

    }
}
