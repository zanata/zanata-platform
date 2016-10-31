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
package org.zanata.webtrans.client.resources;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface NavigationMessages extends Messages {
    @DefaultMessage("Alt+Down")
    String nextEntryShortcut();

    @DefaultMessage("Prev Entry")
    String prevEntry();

    @DefaultMessage("First Entry")
    String firstEntry();

    @DefaultMessage("Last Entry")
    String lastEntry();

    @DefaultMessage("Next Fuzzy or Rejected")
    String nextFuzzy();

    @DefaultMessage("Prev Fuzzy or Rejected")
    String prevFuzzy();

    @DefaultMessage("Next Untranslated")
    String nextUntranslated();

    @DefaultMessage("Prev Untranslated")
    String prevUntranslated();

    @DefaultMessage("Next Fuzzy/Rejected/Untranslated")
    String nextFuzzyOrUntranslated();

    @DefaultMessage("Alt+PageDown")
    String nextFuzzyOrUntranslatedShortcut();

    @DefaultMessage("Prev Fuzzy/Rejected/Untranslated")
    String prevFuzzyOrUntranslated();

    @DefaultMessage("Alt+PageUp")
    String prevFuzzyOrUntranslatedShortcut();

    @DefaultMessage("{0} ({1})")
    String actionToolTip(String actionName, String shortcut);

    @DefaultMessage("Source comment: {0}")
    String sourceCommentLabel(String comment);

}
