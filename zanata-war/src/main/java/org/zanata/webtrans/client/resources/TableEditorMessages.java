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

   @DefaultMessage("Please open the target in the editor first.")
   String notifyUnopened();

   @DefaultMessage("Not logged in!")
   String notifyNotLoggedIn();

   @DefaultMessage("Failed to load data from server")
   String notifyLoadFailed();

   @DefaultMessage("Save FAILED: {0}")
   String notifyUpdateFailed(String errorMessage);

   @DefaultMessage("Saved")
   String notifyUpdateSaved();

   @DefaultMessage("Validation error - See validation message")
   String notifyValidationError();

   @DefaultMessage("Validation Warnings: {0}")
   @AlternateMessage({
      "one", "Validation Warnings: 1",
      "=0", "Validation Warnings: none" })
   String validationWarningsHeading(@PluralCount
   int warningCount);

   @DefaultMessage("Run Validation")
   String runValidation();
   
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

   @DefaultMessage("Save as fuzzy")
   String saveAsFuzzy();

   @DefaultMessage("Save as approved")
   String saveAsApproved();

   @DefaultMessage("Close editor")
   String closeEditor();
}
