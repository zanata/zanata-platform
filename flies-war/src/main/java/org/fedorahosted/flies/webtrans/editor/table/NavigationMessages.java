package org.fedorahosted.flies.webtrans.editor.table;

import com.google.gwt.i18n.client.Messages;

public interface NavigationMessages extends Messages {

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
	
	@DefaultMessage("Ctrl+Shift+PageDown")
	String nextFuzzyShortcut();

	@DefaultMessage("Prev Fuzzy")
	String prevFuzzy();
	
	@DefaultMessage("Ctrl+Shift+PageUp")
	String prevFuzzyShortcut();
	
	@DefaultMessage("Next Untranslated")
	String nextUntranslated();
	
	@DefaultMessage("Alt+PageDown")
	String nextUntranslatedShortcut();
	
	@DefaultMessage("Prev Untranslated")
	String prevUntranslated();

	@DefaultMessage("Alt+PageUp")
	String prevUntranslatedShortcut();
	
	@DefaultMessage("Save")
	String editSave();
	
	@DefaultMessage("Ctrl+Enter")
	String editSaveShortcut();
	
	@DefaultMessage("Cancel")
	String editCancel();
	
	@DefaultMessage("Esc")
	String editCancelShortcut();
	
	@DefaultMessage("Clone")
	String editClone();
	
	@DefaultMessage("NOT IMPLE YET")
	String editCloneShortcut();
	
	@DefaultMessage("{0} ({1})")
	String actionToolTip(String actionName, String shortcut);
}
