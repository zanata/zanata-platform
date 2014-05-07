/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.zanata.webtrans.client.presenter.UserConfigHolder.ConfigurationState;
import org.zanata.webtrans.client.ui.EditorSearchFieldListener;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public interface TransFilterDisplay extends WidgetDisplay, EditorSearchFieldListener {
    boolean isFocused();

    void setListener(Listener listener);

    void setSearchTerm(String searchTerm);

    void setTranslatedFilter(boolean filterByTranslated);

    void setNeedReviewFilter(boolean filterByNeedReview);

    void setUntranslatedFilter(boolean filterByUntranslated);

    void setApprovedFilter(boolean filterByApproved);

    void setRejectedFilter(boolean filterByRejected);

    void setHasErrorFilter(boolean filterByHasError);

    void selectPartialText(String text);

    interface Listener {
        void searchTerm(String searchTerm);

        void messageFilterOptionChanged(Boolean translatedChkValue,
                Boolean fuzzyChkValue, Boolean untranslatedChkValue,
                Boolean approvedChkValue, Boolean rejectedChkValue,
                Boolean hasErrorChkValue);

        void onSearchFieldFocused(boolean focused);
    }

    void setOptionsState(ConfigurationState state);
}
