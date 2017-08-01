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
package org.zanata.webtrans.client.view;

import java.util.ArrayList;
import java.util.List;

import org.zanata.webtrans.client.Application;
import org.zanata.webtrans.client.history.History;
import org.zanata.webtrans.client.history.HistoryToken;
import org.zanata.webtrans.client.presenter.UserConfigHolder;
import org.zanata.webtrans.client.resources.UiMessages;
import org.zanata.webtrans.client.ui.HasSelectableSource;
import org.zanata.webtrans.client.ui.ReferencePanel;
import org.zanata.webtrans.client.ui.SourcePanel;
import org.zanata.webtrans.client.ui.TransUnitDetailsPanel;
import org.zanata.webtrans.shared.model.TextFlowTarget;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.model.UserWorkspaceContext;

import com.google.common.base.Preconditions;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.OpenEvent;
import com.google.gwt.event.logical.shared.OpenHandler;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DisclosurePanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class SourceContentsView extends Composite implements
        SourceContentsDisplay {
    private static Binder binder = GWT.create(Binder.class);

    @UiField
    HTMLPanel sourcePanelContainer;

    @UiField
    Styles style;

    @UiField(provided = true)
    ReferencePanel referencePanel;

    @UiField(provided = true)
    TransUnitDetailsPanel transUnitDetailsPanel;

    @UiField(provided = true)
    DisclosurePanel tuLink;

    private List<HasSelectableSource> sourcePanelList;
    private TransUnit transUnit;
    private final UserConfigHolder configHolder;
    private final History history;
    private final UserWorkspaceContext userWorkspaceContext;

    private UiMessages messages;

    private HTMLPanel rootPanel;

    private TextBox urlTextField;

    @Inject
    public SourceContentsView(
            Provider<TransUnitDetailsPanel> transUnitDetailsPanelProvider,
            UserConfigHolder configHolder, History history,
            final UiMessages messages,
            UserWorkspaceContext userWorkspaceContext) {
        this.configHolder = configHolder;
        this.history = history;
        this.messages = messages;
        this.userWorkspaceContext = userWorkspaceContext;

        sourcePanelList = new ArrayList<HasSelectableSource>();
        referencePanel = new ReferencePanel();
        referencePanel.setReferenceText(messages.noReferenceFoundText());
        // Reference is hidden by default
        referencePanel.setVisible(false);

        tuLink = getUrlWidget();
        transUnitDetailsPanel = transUnitDetailsPanelProvider.get();
        rootPanel = binder.createAndBindUi(this);
        tuLink.setStyleName(style.table() + " txt--meta l--push-top-quarter");
    }

    private DisclosurePanel getUrlWidget() {
        DisclosurePanel widget = new DisclosurePanel();
        widget.setAnimationEnabled(true);

        HTMLPanel header = new HTMLPanel("div", "");
        header.setStyleName("button button--small");
        header.add(new InlineHTML("<i class='i i--star l--push-right-quarter'/>"));
        header.add(new InlineLabel("Link"));
        widget.setHeader(header);

        urlTextField = new TextBox();
        urlTextField.setReadOnly(true);

        HTMLPanel content = new HTMLPanel("div", "");
        content.setStyleName("bg--pop-highest txt--meta");
        content.add(urlTextField);
        widget.setContent(content);

        widget.getHeader().getParent().setStyleName("");
        return widget;
    }

    @Override
    public List<HasSelectableSource> getSourcePanelList() {
        return sourcePanelList;
    }

    @Override
    public void setValue(TransUnit value) {
        setValue(value, false);
    }

    @Override
    public void setValue(TransUnit value, boolean fireEvents) {
        transUnit = value;
        transUnitDetailsPanel.setDetails(value);
        sourcePanelList.clear();

        int rowIndex = 0;
        sourcePanelContainer.clear();
        boolean useCodeMirrorEditor =
                configHolder.getState().isUseCodeMirrorEditor();
        for (String source : value.getSources()) {
            SourcePanel sourcePanel =
                    new SourcePanel(transUnit.getId(), useCodeMirrorEditor);
            sourcePanel.ensureDebugId(value.getRowIndex() + "-source-panel-"
                    + rowIndex);
            sourcePanel.setValue(source, value.getSourceComment(),
                    value.isPlural());
            sourcePanelContainer.add(sourcePanel);
            sourcePanelList.add(sourcePanel);
            rowIndex++;
        }

        final HistoryToken historyToken = history.getHistoryToken();
        historyToken.setTextFlowId(transUnit.getId().toString());
        urlTextField.setStyleName(
                "txt--meta form__input--copyable " + style.ellipsis());
        urlTextField.setText(Application.getEditorUrl(
                userWorkspaceContext.getWorkspaceContext().getWorkspaceId(),
                historyToken.toTokenString()));

        tuLink.addOpenHandler(new OpenHandler<DisclosurePanel>() {
            @Override
            public void onOpen(OpenEvent<DisclosurePanel> openEvent) {
                history.newItem(historyToken);
                urlTextField.selectAll();
            }
        });

        toggleTransUnitDetails(configHolder.getState()
                .isShowOptionalTransUnitDetails());
    }

    @Override
    public void highlightSearch(String search) {
        for (Widget sourceLabel : sourcePanelContainer) {
            ((SourcePanel) sourceLabel).highlightSearch(search);
        }
    }

    @Override
    public void setSourceSelectionHandler(ClickHandler clickHandler) {
        Preconditions
                .checkState(!sourcePanelList.isEmpty(),
                        "empty source panel list. Did you forget to call setValue() before this?");
        for (HasSelectableSource hasSelectableSource : sourcePanelList) {
            hasSelectableSource.addClickHandler(clickHandler);
        }
    }

    @Override
    public void refresh() {
        for (HasSelectableSource hasSelectableSource : sourcePanelList) {
            hasSelectableSource.refresh();
        }
    }

    @Override
    public void toggleTransUnitDetails(boolean showTransUnitDetails) {
        transUnitDetailsPanel.setVisible(showTransUnitDetails);
    }

    @Override
    public void updateTransUnitDetails(TransUnit transUnit) {
        transUnitDetailsPanel.setDetails(transUnit);
    }

    @Override
    public TransUnitId getId() {
        return transUnit.getId();
    }

    @Override
    public void showReference(TextFlowTarget reference) {
        if (reference == null) {
            referencePanel.setReferenceText(messages.noReferenceFoundText());
        } else {
            referencePanel.setReferenceText(messages.inLocale() + " "
                    + reference.getDisplayName() + ": "
                    + reference.getContent());
        }
        referencePanel.setVisible(true);
    }

    @Override
    public void hideReference() {
        referencePanel.setVisible(false);
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    interface Binder extends UiBinder<HTMLPanel, SourceContentsView> {
    }

    interface Styles extends CssResource {
        String table();
        String ellipsis();
    }

}
