/*
 * Copyright 2018, Red Hat, Inc. and individual contributors as indicated by the
 *  @author tags. See the copyright.txt file in the distribution for a full
 *  listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */

package org.zanata.webtrans.client.ui;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Anchor;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.TranslationSourceType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class TransSourceIndicator extends Anchor {

    // Hard coded to Google temporarily
    public static final String MT_TITLE = "Translated by Google";

    public static final String MT_HTML_ATTR = "lang";

    public TransSourceIndicator(TranslationSourceType translationSourceType) {
        super();
        getElement().getStyle().setProperty("background", "#416988");
        getElement().getStyle().setProperty("padding", "0.15em");
        getElement().getStyle().setProperty("border-radius", "4px");
        getElement().getStyle().setProperty("color", "#fff");
        getElement().getStyle().setProperty("cursor", "default");
        getElement().getStyle().setProperty("font-size", "0.8em");

        if (TranslationSourceType.MACHINE_TRANS.equals(translationSourceType)) {
            setText("MT");
            setTitle(MT_TITLE);
            setVisible(true);
            addStyleName("txt-mini");
        } else {
            setVisible(false);
        }
    }

    /**
     * Generate MT attribute for HTML markup
     * e.g. fr-x-mtfrom-en
     * @param srcLocale
     * @param targetLocale
     * @return
     */
    public static String getMTAttribute(LocaleId srcLocale, LocaleId targetLocale) {
        return targetLocale.getId() + "-x-mtfrom-" + srcLocale.getId();
    }

    public HandlerRegistration addClickHandler(ClickHandler handler) {
        getElement().getStyle().setProperty("cursor", "pointer");
        return super.addClickHandler(handler);
    }
}
