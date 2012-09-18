package org.zanata.webtrans.client.resources;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface TableEditorMessages extends Messages
{

   @DefaultMessage("Message has been copied to the target.")
   String notifyCopied();

   @DefaultMessage("Not logged in!")
   String notifyNotLoggedIn();

   @DefaultMessage("Failed to load data from server")
   String notifyLoadFailed();

   @DefaultMessage("Save FAILED: {0}")
   String notifyUpdateFailed(String errorMessage);

   @DefaultMessage("Row {0} (Id {1}) Saved")
   String notifyUpdateSaved(int rowIndex, String id);

   @DefaultMessage("Validation error - See validation message")
   String notifyValidationError();

   // @formatter:off
   @DefaultMessage("Validation Warnings: {0}")
   @AlternateMessage({
      "one", "Validation Warnings: 1",
      "=0", "Validation Warnings: none" })
   String validationWarningsHeading(@PluralCount int warningCount);
   // @formatter:on

   @DefaultMessage(" Run check")
   String runCheck();
   
   @DefaultMessage("Copy from translation memory match result no.{0}")
   String copyFromTM(int index);

   @DefaultMessage("Move to next row")
   String moveToNextRow();

   @DefaultMessage("Move to previous row")
   String moveToPreviousRow();
   
   @DefaultMessage("Move to next Fuzzy")
   String nextFuzzy();

   @DefaultMessage("Move to prev Fuzzy")
   String prevFuzzy();

   @DefaultMessage("Move to next Untranslated")
   String nextUntranslated();

   @DefaultMessage("Move to prev Untranslated")
   String prevUntranslated();

   @DefaultMessage("Move to next Fuzzy/Untranslated")
   String nextFuzzyOrUntranslated();

   @DefaultMessage("Move to prev Fuzzy/Untranslated")
   String prevFuzzyOrUntranslated();

   @DefaultMessage("Save as Approved (Ctrl+Enter)")
   String editSaveShortcut();

   @DefaultMessage("Save as Fuzzy (Ctrl+S)")
   String editSaveAsFuzzyShortcut();

   @DefaultMessage("Save as fuzzy")
   String saveAsFuzzy();

   @DefaultMessage("Save as approved")
   String saveAsApproved();

   @DefaultMessage("Close editor")
   String closeEditor();

   @DefaultMessage("Cancel")
   String editCancelShortcut();

   @DefaultMessage("History")
   String history();

   @DefaultMessage("Translation Unit Details: Row {0}; Id {1}; {2}")
   String transUnitDetailsHeadingWithInfo(int rowIndex, String transUnitId, String info);

   @DefaultMessage("Copy message from source language (Alt+G)")
   String copyFromSource();
}
