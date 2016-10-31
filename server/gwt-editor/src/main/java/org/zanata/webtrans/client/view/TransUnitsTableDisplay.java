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

import java.util.List;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.webtrans.client.ui.FilterViewConfirmationDisplay;

import com.google.inject.ImplementedBy;

@ImplementedBy(TransUnitsTableView.class)
public interface TransUnitsTableDisplay extends WidgetDisplay {
    void addFilterConfirmationHandler(
            FilterViewConfirmationDisplay.Listener listener);

    void showFilterConfirmation();

    void hideFilterConfirmation();

    void buildTable(List<? extends NeedsRefresh> sourceDisplays,
            List<? extends NeedsRefresh> targetDisplays);

    void setRowSelectionListener(Listener listener);

    void showLoading(boolean isLoading);

    void applySelectedStyle(int rowIndex);

    void delayRefresh();

    void ensureVisible(TargetContentsDisplay currentDisplay);

    void setThemes(String theme);

    interface Listener {
        void onRowSelected(int rowIndex);

        void refreshView();
    }

}
