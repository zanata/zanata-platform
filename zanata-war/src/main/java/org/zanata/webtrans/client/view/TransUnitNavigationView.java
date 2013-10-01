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

import org.zanata.webtrans.client.resources.NavigationMessages;
import org.zanata.webtrans.shared.rpc.NavOption;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class TransUnitNavigationView extends Composite implements
        TransUnitNavigationDisplay {

    private static TransUnitNavigationViewUiBinder uiBinder = GWT
            .create(TransUnitNavigationViewUiBinder.class);
    private Listener listener;

    interface Styles extends CssResource {
        String fuzzyUntranslated();

        String fuzzy();

        String untranslated();

        String navButton();
    }

    @UiField
    InlineLabel prevState, nextState, firstEntry, lastEntry;

    @UiField
    Styles style;

    private final NavigationMessages messages;

    @Inject
    public TransUnitNavigationView(final NavigationMessages messages) {
        this.messages = messages;

        initWidget(uiBinder.createAndBindUi(this));

        firstEntry.setTitle(messages.firstEntry());
        lastEntry.setTitle(messages.lastEntry());
        setFuzzyUntranslatedModeTooltip();
    }

    private void setFuzzyModeTooltip() {
        prevState.setStylePrimaryName(style.fuzzy());
        prevState.setTitle(messages.actionToolTip(messages.prevFuzzy(),
                messages.prevFuzzyOrUntranslatedShortcut()));

        nextState.setStylePrimaryName(style.fuzzy());
        nextState.setTitle(messages.actionToolTip(messages.nextFuzzy(),
                messages.nextFuzzyOrUntranslatedShortcut()));
    }

    private void setUntranslatedModeTooltip() {
        prevState.setStylePrimaryName(style.untranslated());
        prevState.setTitle(messages.actionToolTip(messages.prevUntranslated(),
                messages.prevFuzzyOrUntranslatedShortcut()));

        nextState.setStylePrimaryName(style.untranslated());
        nextState.setTitle(messages.actionToolTip(messages.nextUntranslated(),
                messages.nextFuzzyOrUntranslatedShortcut()));
    }

    private void setFuzzyUntranslatedModeTooltip() {
        prevState.setStylePrimaryName(style.fuzzyUntranslated());
        prevState.setTitle(messages.actionToolTip(
                messages.prevFuzzyOrUntranslated(),
                messages.prevFuzzyOrUntranslatedShortcut()));

        nextState.setStylePrimaryName(style.fuzzyUntranslated());
        nextState.setTitle(messages.actionToolTip(
                messages.nextFuzzyOrUntranslated(),
                messages.nextFuzzyOrUntranslatedShortcut()));
    }

    @UiHandler("firstEntry")
    public void onFirstEntryClicked(ClickEvent event) {
        listener.goToFirstEntry();
    }

    @UiHandler("lastEntry")
    public void onLastEntryClicked(ClickEvent event) {
        listener.goToLastEntry();
    }

    @UiHandler("prevState")
    public void onPrevStateClicked(ClickEvent event) {
        listener.goToPreviousState();
    }

    @UiHandler("nextState")
    public void onNextStateClicked(ClickEvent event) {
        listener.goToNextState();
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setNavModeTooltip(NavOption navOption) {
        switch (navOption) {
        case FUZZY_UNTRANSLATED:
            setFuzzyUntranslatedModeTooltip();
            break;
        case FUZZY:
            setFuzzyModeTooltip();
            break;
        case UNTRANSLATED:
            setUntranslatedModeTooltip();
            break;
        }
    }

    @Override
    public void setListener(Listener listener) {
        this.listener = listener;
    }

    interface TransUnitNavigationViewUiBinder extends
            UiBinder<Widget, TransUnitNavigationView> {
    }
}
