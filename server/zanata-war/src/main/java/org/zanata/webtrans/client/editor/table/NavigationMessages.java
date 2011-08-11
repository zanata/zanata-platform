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
package org.zanata.webtrans.client.editor.table;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface NavigationMessages extends Messages
{

   @DefaultMessage("Next Entry")
   String nextEntry();

   @DefaultMessage("Alt+Down")
   String nextEntryShortcut();

   @DefaultMessage("Prev Entry")
   String prevEntry();

   @DefaultMessage("Alt+Up")
   String prevEntryShortcut();

   @DefaultMessage("Next Fuzzy")
   String nextFuzzy();

   @DefaultMessage("Next Fuzzy or Untranslated")
   String nextFuzzyOrUntranslated();

   @DefaultMessage("Alt+PageDown")
   String nextFuzzyOrUntranslatedShortcut();

   @DefaultMessage("Prev Fuzzy or Untranslated")
   String prevFuzzyOrUntranslated();

   @DefaultMessage("Alt+PageUp")
   String prevFuzzyOrUntranslatedShortcut();

   @DefaultMessage("Save as Approved (Ctrl+Enter)")
   String editSaveShortcut();

   @DefaultMessage("Cancel")
   String editCancelShortcut();

   // @DefaultMessage("Copy")
   // String editClone();

   // @DefaultMessage("Ctrl+Home")
   // @DefaultMessage("Clone")
   // String editCloneShortcut();

   // @DefaultMessage("Clone & Save")
   // String editCloneAndSave();

   // @DefaultMessage("Ctrl+End")
   // @DefaultMessage("Clone & Save")
   // String editCloneAndSaveShortcut();

   @DefaultMessage("Copy message from source language")
   String copySourcetoTarget();

   @DefaultMessage("{0} ({1})")
   String actionToolTip(String actionName, String shortcut);

   @DefaultMessage("Save as Fuzzy")
   String fuzzy();

   @DefaultMessage("Source comment: ")
   String sourceCommentLabel();

   @DefaultMessage("Undo")
   String undoLabel();

   @DefaultMessage("Redo")
   String redoLabel();

   @DefaultMessage("Click here to start translating")
   String clickHere();
}