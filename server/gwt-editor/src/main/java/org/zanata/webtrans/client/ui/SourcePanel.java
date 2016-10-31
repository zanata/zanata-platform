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
package org.zanata.webtrans.client.ui;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.shared.model.TransUnitId;

public class SourcePanel extends Composite implements HasSelectableSource {
    private static SourcePanelUiBinder uiBinder = GWT
            .create(SourcePanelUiBinder.class);

    interface SourcePanelUiBinder extends UiBinder<Widget, SourcePanel> {
    }

    interface Styles extends CssResource {
        String selectedRow();
    }

    @UiField
    HTMLPanel container;

    @UiField
    RadioButton selectButton;

    @UiField
    NavigationMessages messages;

    @UiField
    Styles style;
    @UiField
    SimplePanel sourceContentContainer;
    private SourceContentWrapper sourceContent;

    private String source = "";
    private final TransUnitId transUnitId;

    public SourcePanel(TransUnitId transUnitId, boolean useCodeMirrorEditor) {
        this.transUnitId = transUnitId;
        initWidget(uiBinder.createAndBindUi(this));
        if (useCodeMirrorEditor) {
            sourceContent = new CodeMirrorReadOnlyWidget();
        } else {
            sourceContent = new HighlightingLabel();
        }
        sourceContentContainer.setWidget(sourceContent);
        sinkEvents(Event.ONCLICK);
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        sourceContent.ensureDebugId(baseID + "-container");
    }

    public void setValue(String source, String sourceComment, boolean isPlural) {
        this.source = source;

        sourceContent.setText(source);
        sourceContent.setTitle(messages.sourceCommentLabel(sourceComment));

        if (!isPlural) {
            selectButton.setVisible(false);
        }
    }

    @Override
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addHandler(handler, ClickEvent.getType());
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public TransUnitId getId() {
        return transUnitId;
    }

    @Override
    public void setSelected(boolean selected) {
        if (selected && selectButton.isVisible()) {
            container.addStyleName(style.selectedRow());
            selectButton.setValue(true, true);
        } else {
            container.removeStyleName(style.selectedRow());
        }
    }

    @Override
    public void refresh() {
        sourceContent.refresh();
    }

    @Override
    public void clickSelf() {
        ClickEvent.fireNativeEvent(
                Document.get().createClickEvent(0, 0, 0, 0, 0, false, false,
                        false, false), this);
    }

    public void highlightSearch(String searchTerm) {
        sourceContent.highlight(searchTerm);
    }
}
