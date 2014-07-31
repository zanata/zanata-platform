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
package org.zanata.webtrans.client.view;

import org.zanata.webtrans.client.resources.WebTransMessages;
import org.zanata.webtrans.client.ui.HasPager;
import org.zanata.webtrans.client.ui.Pager;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasVisibility;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TranslationEditorView extends Composite implements
        TranslationEditorDisplay {
    private static TranslationEditorViewUiBinder uiBinder = GWT
            .create(TranslationEditorViewUiBinder.class);

    interface TranslationEditorViewUiBinder extends
            UiBinder<Widget, TranslationEditorView> {
    }

    @UiField(provided = true)
    Widget transUnitNavigationView;

    @UiField(provided = true)
    Widget transUnitsTableView;

    @UiField(provided = true)
    Pager pager;

    @UiField(provided = true)
    Widget transFilterView;

    @UiField
    Anchor refreshCurrentPage, resize;

    private Listener listener;

    private final WebTransMessages messages;

    private final static String STYLE_HIDE_SOUTHPANEL = "icon-down-circle";
    private final static String STYLE_RESTORE_SOUTHPANEL = "icon-up-circle";

    @Inject
    public TranslationEditorView(final WebTransMessages messages,
            TransFilterDisplay transFilterView,
            TransUnitNavigationDisplay transUnitNavigationView,
            TransUnitsTableDisplay transUnitsTableView) {
        this.pager = new Pager(messages);
        this.messages = messages;
        this.transFilterView = transFilterView.asWidget();
        this.transUnitNavigationView = transUnitNavigationView.asWidget();
        this.transUnitsTableView = transUnitsTableView.asWidget();

        initWidget(uiBinder.createAndBindUi(this));

        refreshCurrentPage.setTitle(messages.refreshCurrentPage());
        resize.setTitle(messages.hideSouthPanel());
        resize.addStyleName(STYLE_HIDE_SOUTHPANEL);
    }

    /**
     * return false if to be maximise, true for minimise
     *
     */
    @Override
    public boolean getAndToggleResizeButton() {
        if (resize.getStyleName().contains(STYLE_HIDE_SOUTHPANEL)) {
            resize.removeStyleName(STYLE_HIDE_SOUTHPANEL);
            resize.addStyleName(STYLE_RESTORE_SOUTHPANEL);
            resize.setTitle(messages.restoreSouthPanel());
            return false;
        } else {
            resize.removeStyleName(STYLE_RESTORE_SOUTHPANEL);
            resize.addStyleName(STYLE_HIDE_SOUTHPANEL);
            resize.setTitle(messages.hideSouthPanel());
            return true;
        }
    }

    @Override
    public HasVisibility getResizeButton() {
        return resize;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public HasPager getPageNavigation() {
        return pager;
    }

    @Override
    public boolean isPagerFocused() {
        return pager.isFocused();
    }

    @UiHandler("refreshCurrentPage")
    public void onRedrawCurrentPageClicked(ClickEvent event) {
        listener.refreshCurrentPage();
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    @UiHandler("pager")
    public void onPagerFocused(FocusEvent event) {
        listener.onPagerFocused();
    }

    @UiHandler("pager")
    public void onPagerBlurred(BlurEvent event) {
        listener.onPagerBlurred();
    }

    @UiHandler("pager")
    public void onPagerValueChanged(ValueChangeEvent<Integer> event) {
        listener.onPagerValueChanged(event.getValue());
    }

    @UiHandler("resize")
    public void onResizeIconClick(ClickEvent event) {
        listener.onResizeClicked();
    }

}
