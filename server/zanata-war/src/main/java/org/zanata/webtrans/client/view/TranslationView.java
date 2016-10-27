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
package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.presenter.TranslationPresenter;
import org.zanata.webtrans.client.ui.SplitLayoutPanelHelper;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TranslationView extends Composite implements
        TranslationPresenter.Display {
    private static TranslationViewUiBinder uiBinder = GWT
            .create(TranslationViewUiBinder.class);
    private static final double MIN_SOUTH_PANEL_HEIGHT = 10;
    private static final double GLOSSARY_PANEL_WIDTH = 500;
    private static final int ANIMATE_DURATION = 200;
    private static double SOUTH_PANEL_HEIGHT = 150;

    private final TranslationMemoryDisplay transMemoryView;
    private final GlossaryDisplay glossaryView;
    private final HasVisibility resizeButton;

    @UiField(provided = true)
    Widget translationEditorView;

    @UiField(provided = true)
    SplitLayoutPanel mainSplitPanel;

    @UiField(provided = true)
    SplitLayoutPanel tmGlossaryPanel;

    @Inject
    public TranslationView(TranslationEditorDisplay translationEditorView,
            TranslationMemoryDisplay transMemoryView,
            GlossaryDisplay glossaryView) {
        this.transMemoryView = transMemoryView;
        this.glossaryView = glossaryView;
        resizeButton = translationEditorView.getResizeButton();

        mainSplitPanel = new SplitLayoutPanel(2);
        tmGlossaryPanel = new SplitLayoutPanel(2);
        this.translationEditorView = translationEditorView.asWidget();

        initWidget(uiBinder.createAndBindUi(this));
        mainSplitPanel.setWidgetMinSize(tmGlossaryPanel,
                (int) MIN_SOUTH_PANEL_HEIGHT);

        setGlossaryView();
        setTranslationMemoryView();
    }

    private void setTranslationMemoryView() {
        tmGlossaryPanel.remove(transMemoryView.asWidget());
        tmGlossaryPanel.add(transMemoryView.asWidget());
    }

    private void setGlossaryView() {
        tmGlossaryPanel.remove(glossaryView.asWidget());
        tmGlossaryPanel.addEast(glossaryView.asWidget(), GLOSSARY_PANEL_WIDTH);
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setSouthPanelExpanded(boolean expanded) {
        mainSplitPanel.forceLayout();
        Widget splitter =
                SplitLayoutPanelHelper.getAssociatedSplitter(mainSplitPanel,
                        tmGlossaryPanel);
        if (expanded) {
            mainSplitPanel.setWidgetSize(tmGlossaryPanel.asWidget(),
                    SOUTH_PANEL_HEIGHT);
        } else {
            mainSplitPanel.setWidgetSize(tmGlossaryPanel.asWidget(),
                    MIN_SOUTH_PANEL_HEIGHT);
            SOUTH_PANEL_HEIGHT =
                    mainSplitPanel.getWidgetContainerElement(tmGlossaryPanel)
                            .getOffsetHeight();
        }
        splitter.setVisible(expanded);
        mainSplitPanel.animate(ANIMATE_DURATION);

    }

    @Override
    public void togglePanelDisplay(boolean showTMPanel,
            boolean showGlossaryPanel) {
        tmGlossaryPanel.forceLayout();
        resizeButton.setVisible(true);
        if (showTMPanel && showGlossaryPanel) {
            // show both
            tmGlossaryPanel.setWidgetSize(glossaryView.asWidget(),
                    GLOSSARY_PANEL_WIDTH);
        } else if (showGlossaryPanel) {
            // only show glossary
            tmGlossaryPanel.setWidgetSize(glossaryView.asWidget(), glossaryView
                    .asWidget().getOffsetWidth()
                    + transMemoryView.asWidget().getOffsetWidth());
        } else if (showTMPanel) {
            // only show TM
            tmGlossaryPanel.setWidgetSize(glossaryView.asWidget(), 0);
        } else {
            // hide both
            resizeButton.setVisible(false);
        }
        tmGlossaryPanel.animate(ANIMATE_DURATION);
    }

    interface TranslationViewUiBinder extends
            UiBinder<LayoutPanel, TranslationView> {
    }
}
