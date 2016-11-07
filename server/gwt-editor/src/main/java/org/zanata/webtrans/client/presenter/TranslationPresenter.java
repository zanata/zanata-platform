/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
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
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.zanata.webtrans.client.events.DisplaySouthPanelEvent;
import org.zanata.webtrans.client.events.DisplaySouthPanelEventHandler;
import org.zanata.webtrans.client.events.KeyShortcutEvent;
import org.zanata.webtrans.client.events.KeyShortcutEventHandler;
import org.zanata.webtrans.client.events.NavTransUnitEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEvent;
import org.zanata.webtrans.client.events.WorkspaceContextUpdateEventHandler;
import org.zanata.webtrans.client.keys.KeyShortcut;
import org.zanata.webtrans.client.keys.KeyShortcut.KeyEvent;
import org.zanata.webtrans.client.keys.Keys;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.service.NavigationService;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.inject.Inject;

public class TranslationPresenter extends
        WidgetPresenter<TranslationPresenter.Display> implements
        WorkspaceContextUpdateEventHandler, DisplaySouthPanelEventHandler {
    public interface Display extends WidgetDisplay {
        void setSouthPanelExpanded(boolean expanded);

        void togglePanelDisplay(boolean showTMPanel, boolean showGlossaryPanel);
    }

    private final TranslationEditorPresenter translationEditorPresenter;
    private final TransMemoryPresenter transMemoryPresenter;
    private final GlossaryPresenter glossaryPresenter;
    private final TargetContentsPresenter targetContentsPresenter;
    private final KeyShortcutPresenter keyShortcutPresenter;

    private final UserWorkspaceContext userWorkspaceContext;
    private final NavigationService navigationService;
    private final UserConfigHolder configHolder;

    private final WebTransMessages messages;

    @Inject
    public TranslationPresenter(Display display, EventBus eventBus,
            TargetContentsPresenter targetContentsPresenter,
            TranslationEditorPresenter translationEditorPresenter,
            TransMemoryPresenter transMemoryPresenter,
            GlossaryPresenter glossaryPresenter, WebTransMessages messages,
            UserWorkspaceContext userWorkspaceContext,
            KeyShortcutPresenter keyShortcutPresenter,
            NavigationService navigationService, UserConfigHolder configHolder) {
        super(display, eventBus);
        this.messages = messages;
        this.translationEditorPresenter = translationEditorPresenter;
        this.transMemoryPresenter = transMemoryPresenter;
        this.glossaryPresenter = glossaryPresenter;
        this.targetContentsPresenter = targetContentsPresenter;
        this.keyShortcutPresenter = keyShortcutPresenter;
        this.userWorkspaceContext = userWorkspaceContext;
        this.navigationService = navigationService;
        this.configHolder = configHolder;
    }

    @Override
    public void onRevealDisplay() {
        if (targetContentsPresenter.hasSelectedRow()) {
            targetContentsPresenter.setFocus();
            targetContentsPresenter.revealDisplay();
        } else {
            targetContentsPresenter.concealDisplay();
        }
    }

    @Override
    protected void onBind() {
        bindSouthPanelPresenters();
        translationEditorPresenter.bind();

        registerHandler(eventBus.addHandler(
                WorkspaceContextUpdateEvent.getType(), this));
        registerHandler(eventBus.addHandler(DisplaySouthPanelEvent.TYPE, this));
        setSouthPanelReadOnly(userWorkspaceContext.hasReadOnlyAccess());

        // navigate to previous row shortcut
        KeyShortcutEventHandler gotoPreRowHandler =
                event -> {
                    targetContentsPresenter
                            .savePendingChangesIfApplicable();
                    eventBus.fireEvent(NavTransUnitEvent.PREV_ENTRY_EVENT);
                };
        KeyShortcut movePreviousShortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.ALT_KEY, KeyCodes.KEY_UP))
                        .addKey(new Keys(Keys.ALT_KEY, 'J'))
                        .setContext(ShortcutContext.Navigation)
                        .setDescription(messages.navigateToPreviousRow())
                        .setPreventDefault(true).setStopPropagation(true)
                        .setHandler(gotoPreRowHandler).build();
        keyShortcutPresenter.register(movePreviousShortcut);

        // navigate to next row shortcut
        KeyShortcutEventHandler gotoNextRowHandler =
                event -> {
                    targetContentsPresenter
                            .savePendingChangesIfApplicable();
                    eventBus.fireEvent(NavTransUnitEvent.NEXT_ENTRY_EVENT);
                };
        KeyShortcut moveNextShortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.ALT_KEY, KeyCodes.KEY_DOWN))
                        .addKey(new Keys(Keys.ALT_KEY, 'K'))
                        .setContext(ShortcutContext.Navigation)
                        .setDescription(messages.navigateToNextRow())
                        .setPreventDefault(true).setStopPropagation(true)
                        .setHandler(gotoNextRowHandler).build();
        keyShortcutPresenter.register(moveNextShortcut);

        // Register shortcut Enter to open editor in selected row - if no other
        // input field is in focus
        KeyShortcut startEditingShortcut =
                KeyShortcut.Builder.builder()
                        .addKey(new Keys(Keys.NO_MODIFIER, KeyCodes.KEY_ENTER))
                        .setContext(ShortcutContext.Navigation)
                        .setDescription(messages.openEditorInSelectedRow())
                        .setKeyEvent(KeyEvent.KEY_UP)
                        .setHandler(event -> {
                            if (!isOtherInputFieldFocused()
                                    && userWorkspaceContext
                                            .getWorkspaceRestrictions()
                                            .isHasEditTranslationAccess()) {
                                targetContentsPresenter.setFocus();
                                targetContentsPresenter.revealDisplay();
                            }
                        }).build();
        keyShortcutPresenter.register(startEditingShortcut);
    }

    private boolean isOtherInputFieldFocused() {
        return translationEditorPresenter.isTransFilterFocused()
                || transMemoryPresenter.isFocused()
                || glossaryPresenter.isFocused()
                || translationEditorPresenter.getDisplay().isPagerFocused();
    }

    @Override
    protected void onUnbind() {
        unbindSouthPanelPresenters();
        translationEditorPresenter.unbind();
    }

    public void saveEditorPendingChange() {
        targetContentsPresenter.savePendingChangesIfApplicable();
    }

    /**
     * Handle all changes required to completely hide and unbind the south panel
     * for read-only mode, or to undo said changes.
     *
     * @param readOnly
     *            read only
     */
    private void setSouthPanelReadOnly(boolean readOnly) {
        if (readOnly) {
            // includes unbinding
            setSouthPanelExpanded(false);
            translationEditorPresenter.setReadOnly(false);
        } else {
            setSouthPanelExpanded(true);
            translationEditorPresenter.setReadOnly(true);
        }
    }

    /**
     * Expand or collapse south panel, binding or unbinding presenters as
     * appropriate. Will have no effect if the panel is already in the state of
     * expansion or contraction that is specified.
     *
     * @param expanded
     *            expand
     */
    public void setSouthPanelExpanded(boolean expanded) {
        checkPanelDisplayOption();
        display.setSouthPanelExpanded(expanded);
        if (expanded && !userWorkspaceContext.hasReadOnlyAccess()) {
            bindSouthPanelPresenters();

            TransUnit tu = navigationService.getSelectedOrNull();
            if (tu != null) {
                if (configHolder.getState().isShowTMPanel()) {
                    transMemoryPresenter.createTMRequestForTransUnit(tu);
                }
                if (configHolder.getState().isShowGlossaryPanel()) {
                    glossaryPresenter.createGlossaryRequestForTransUnit(tu);
                }
            }
        } else {
            unbindSouthPanelPresenters();
        }
    }

    private void checkPanelDisplayOption() {
        if (configHolder.getState().isShowTMPanel()) {
            bindTransMemoryPresenter();
        } else {
            unbindTransMemoryPresenter();
        }
        if (configHolder.getState().isShowGlossaryPanel()) {
            bindGlossaryPresenter();
        } else {
            unbindGlossaryPresenter();
        }
        display.togglePanelDisplay(configHolder.getState().isShowTMPanel(),
                configHolder.getState().isShowGlossaryPanel());
    }

    private void bindSouthPanelPresenters() {
        bindTransMemoryPresenter();
        bindGlossaryPresenter();
    }

    private void bindGlossaryPresenter() {
        if (configHolder.getState().isShowGlossaryPanel()
                && !glossaryPresenter.isBound()) {
            glossaryPresenter.bind();
        }
    }

    private void bindTransMemoryPresenter() {
        if (configHolder.getState().isShowTMPanel()
                && !transMemoryPresenter.isBound()) {
            transMemoryPresenter.bind();
        }
    }

    private void unbindSouthPanelPresenters() {
        transMemoryPresenter.unbind();
        glossaryPresenter.unbind();
    }

    private void unbindGlossaryPresenter() {
        if (!configHolder.getState().isShowGlossaryPanel()
                && glossaryPresenter.isBound()) {
            glossaryPresenter.unbind();
        }
    }

    private void unbindTransMemoryPresenter() {
        if (!configHolder.getState().isShowTMPanel()
                && transMemoryPresenter.isBound()) {
            transMemoryPresenter.unbind();
        }
    }

    public void concealDisplay() {
        targetContentsPresenter.concealDisplay();
        keyShortcutPresenter
                .setContextActive(ShortcutContext.Navigation, false);
    }

    @Override
    public void onWorkspaceContextUpdated(WorkspaceContextUpdateEvent event) {
        userWorkspaceContext.setProjectActive(event.isProjectActive());
        userWorkspaceContext.getWorkspaceContext().getWorkspaceId()
                .getProjectIterationId().setProjectType(event.getProjectType());

        setSouthPanelReadOnly(userWorkspaceContext.hasReadOnlyAccess());
    }

    @Override
    public void onDisplaySouthPanel(DisplaySouthPanelEvent event) {
        setSouthPanelExpanded(event.isDisplay());
    }
}
