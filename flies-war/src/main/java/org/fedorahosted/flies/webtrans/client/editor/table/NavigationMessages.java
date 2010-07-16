package org.fedorahosted.flies.webtrans.client.editor.table;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;

@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
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
	
	@DefaultMessage("Ctrl+Home")
	String editCloneShortcut();
	
	@DefaultMessage("Clone & Save")
	String editCloneAndSave();
	
	@DefaultMessage("Ctrl+End")
	String editCloneAndSaveShortcut();
	
	@DefaultMessage("{0} ({1})")
	String actionToolTip(String actionName, String shortcut);
	
	@DefaultMessage("Fuzzy")
	String fuzzy();

	@DefaultMessage("Source comment: ")
	String sourceCommentLabel();
}
