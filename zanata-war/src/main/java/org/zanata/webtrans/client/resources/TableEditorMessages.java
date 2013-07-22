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

   @DefaultMessage("Copy from translation memory match result no.{0}")
   String copyFromTM(int index);

   @DefaultMessage("Move to next row")
   String moveToNextRow();

   @DefaultMessage("Move to previous row")
   String moveToPreviousRow();
   
   @DefaultMessage("Move to next Fuzzy or Rejected")
   String nextDraft();

   @DefaultMessage("Move to prev Fuzzy or Rejected")
   String prevDraft();

   @DefaultMessage("Move to next Untranslated")
   String nextUntranslated();

   @DefaultMessage("Move to prev Untranslated")
   String prevUntranslated();

   @DefaultMessage("Move to next Fuzzy/Rejected/Untranslated")
   String nextIncomplete();

   @DefaultMessage("Move to prev Fuzzy/Rejected/Untranslated")
   String prevIncomplete();

   @DefaultMessage("Save as Translated (Ctrl+Enter)")
   String editSaveShortcut();

   @DefaultMessage("Save as Fuzzy (Ctrl+S)")
   String editSaveAsFuzzyShortcut();

   @DefaultMessage("Save as Fuzzy")
   String saveAsFuzzy();

   @DefaultMessage("Save as Translated")
   String saveAsTranslated();

   @DefaultMessage("Cancel")
   String editCancelShortcut();

   @DefaultMessage("History")
   String history();

   @DefaultMessage("#{0}; {1}")
   String transUnitDetailsHeadingWithInfo(int rowIndex, String info);

   @DefaultMessage("Copy message from source language.")
   String copyFromSource();

   @DefaultMessage("Saving...")
   String saving();

   @DefaultMessage("Cancel changes")
   String cancelChanges();

   @DefaultMessage("Cancel")
   String cancel();

   @DefaultMessage("Don''t show this warning again.")
   String saveAsApprovedDialogRememberDecision();

   @DefaultMessage("Warning! Saving a ''Fuzzy'' translation as ''Translated'' without changes.")
   String saveAsTranslatedDialogWarning1();

   @DefaultMessage("For navigation only, please use:")
   String saveAsApprovedDialogInfo1();

   @DefaultMessage("ALT+Up or ALT+J:  Move to previous row")
   String saveAsApprovedDialogInfo2();

   @DefaultMessage("ALT+Down or ALT+K:  Move to next row")
   String saveAsApprovedDialogInfo3();

   @DefaultMessage("Accept translation")
   String reviewAccept();

   @DefaultMessage("Reject translation")
   String reviewReject();

   @DefaultMessage("Comment")
   String comment();
   
   @DefaultMessage("Discard Changes")
   String discardChanges();
      
   @DefaultMessage("Cancel filter")
   String cancelFilter();
      
   @DefaultMessage("Save changes before filtering view?")
   String saveChangesConfirmationMessage();
}
