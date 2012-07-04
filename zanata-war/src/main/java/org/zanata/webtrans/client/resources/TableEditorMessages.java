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

   @DefaultMessage("Workspace is set to read only")
   String notifyReadOnlyWorkspace();

   @DefaultMessage("Workspace is set to edit mode")
   String notifyEditableWorkspace();

   @DefaultMessage("Validation Warnings: {0}")
   @AlternateMessage({
      "one", "Validation Warnings: 1",
      "=0", "Validation Warnings: none" })
   String validationWarningsHeading(@PluralCount
   int warningCount);

   @DefaultMessage("Run Validation")
   String runValidation();
   
   @DefaultMessage("Copy from translation memory {0}")
   String copyFromTM(String index);

   @DefaultMessage("Move to next row")
   String moveToNextRow();

   @DefaultMessage("Move to previous row")
   String moveToPreviousRow();
   
   @DefaultMessage("Move to next state row")
   String moveToNextStateRow();
   
   @DefaultMessage("Move to previous state row")
   String moveToPreviousStateRow();

   @DefaultMessage("Save as fuzzy")
   String saveAsFuzzy();

   @DefaultMessage("Save as approved")
   String saveAsApproved();
   
   @DefaultMessage("Save as approved (if \"Enter key saves immediately\" enabled)")
   String saveAsApprovedEnter();

   @DefaultMessage("Close editor (if \"Esc key closes editor\" enabled)")
   String closeEditor();

   @DefaultMessage("User typing")
   String userTyping();
}
