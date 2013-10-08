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
package org.zanata.webtrans.client.ui;

import java.util.List;
import java.util.Map;

import net.customware.gwt.presenter.client.EventBus;

import org.zanata.webtrans.client.events.CopyDataToEditorEvent;
import org.zanata.webtrans.client.keys.ShortcutContext;
import org.zanata.webtrans.client.keys.TimedAction;
import org.zanata.webtrans.client.keys.Timer;
import org.zanata.webtrans.client.keys.TimerFactory;
import org.zanata.webtrans.client.presenter.KeyShortcutPresenter;
import org.zanata.webtrans.client.resources.TableEditorMessages;
import org.zanata.webtrans.client.service.NavigationService;
import org.zanata.webtrans.client.view.TargetContentsDisplay;
import org.zanata.webtrans.shared.model.DocumentInfo;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.ValidationAction;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.inject.Inject;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
public class ValidationWarningPanel extends ShortcutContextAwareDialogBox
        implements ValidationWarningDisplay {
    private static ValidationWarningPanelUiBinder uiBinder = GWT
            .create(ValidationWarningPanelUiBinder.class);

    interface ValidationWarningPanelUiBinder extends
            UiBinder<HTMLPanel, ValidationWarningPanel> {
    }

    private TransUnitId transUnitId;

    private DocumentInfo documentInfo;

    private List<String> targets;

    private TargetContentsDisplay.Listener listener;

    private NavigationService navigationService;

    @UiField
    UnorderedListWidget translations;

    @UiField
    UnorderedListWidget errorList;

    @UiField(provided = true)
    Button returnToEditor;

    @UiField(provided = true)
    Button saveAsFuzzy;

    private Timer timer;

    private final EventBus eventBus;

    private static int CHECK_EDITOR_SELECTED_DURATION = 500;

    @Inject
    public ValidationWarningPanel(TableEditorMessages messages,
            KeyShortcutPresenter keyShortcutPresenter,
            final NavigationService navigationService,
            final TimerFactory timer, final EventBus eventBus) {
        super(false, true, ShortcutContext.ValidationWarningPopup,
                keyShortcutPresenter);

        setStyleName("new-zanata");

        returnToEditor = new Button(messages.returnToEditor());
        saveAsFuzzy = new Button(messages.saveAsFuzzy());

        HTMLPanel container = uiBinder.createAndBindUi(this);

        this.navigationService = navigationService;
        this.eventBus = eventBus;

        this.timer = timer.create(new TimedAction() {
            @Override
            public void run() {
                copyTranslationToEditor();
            }
        });

        setGlassEnabled(true);
        setWidget(container);
        hide();
    }

    /**
     * Check if the editor is ready and selected. See gotoRow in
     * TargetContentsPresenter
     */
    private void copyTranslationToEditor() {
        TransUnit selectedTransUnit = navigationService.getSelectedOrNull();

        if (selectedTransUnit != null
                && selectedTransUnit.getId().equals(transUnitId)) {
            timer.cancel();
            eventBus.fireEvent(new CopyDataToEditorEvent(targets));
            hide();
        } else {
            timer.schedule(CHECK_EDITOR_SELECTED_DURATION);
        }
    }

    public void setListener(TargetContentsDisplay.Listener listener) {
        this.listener = listener;
        addListenerToButtons();
    }

    private void addListenerToButtons() {
        saveAsFuzzy.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hide();
                listener.saveAsFuzzy(transUnitId);
            }
        });
        returnToEditor.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                // clear data in navigation service to ensure first run of
                // copyTranslationToEditor() = false
                navigationService.clearData();
                listener.gotoRow(documentInfo, transUnitId);
                timer.schedule(CHECK_EDITOR_SELECTED_DURATION);
            }
        });
    }

    @Override
    public void center(TransUnitId transUnitId, DocumentInfo documentInfo,
            List<String> targets,
            Map<ValidationAction, List<String>> errorMessages) {
        this.transUnitId = transUnitId;
        this.documentInfo = documentInfo;
        this.targets = targets;
        refreshView(errorMessages);
        center();
    }

    private void refreshView(Map<ValidationAction, List<String>> errorMessages) {
        translations.clear();
        errorList.clear();

        for (String target : targets) {
            SafeHtmlBuilder builder = new SafeHtmlBuilder();
            builder.append(TextContentsDisplay.asSyntaxHighlight(
                    Lists.newArrayList(target)).toSafeHtml());
            translations.add(new HTMLPanel("li", builder.toSafeHtml()
                    .asString()));
        }

        for (List<String> messages : errorMessages.values()) {
            for (String message : messages) {
                SafeHtmlBuilder builder = new SafeHtmlBuilder();
                builder.appendEscaped(message);
                errorList.add(new HTMLPanel("li", builder.toSafeHtml()
                        .asString()));
            }
        }
    }
}
