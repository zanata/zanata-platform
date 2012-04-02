package org.zanata.webtrans.client.resources;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface TableEditorMessages extends Messages
{

   @DefaultMessage("Source comment: ")
   String sourceCommentLabel();

   @DefaultMessage("Warning: This Translation Unit is being edited by someone else.")
   String notifyInEdit();

   @DefaultMessage("Message has been copied to the target.")
   String notifyCopied();

   @DefaultMessage("Please open the target in the editor first.")
   String notifyUnopened();

   @DefaultMessage("Not logged in!")
   String notifyNotLoggedIn();

   @DefaultMessage("Failed to load data from server")
   String notifyLoadFailed();

   @DefaultMessage("Saving...")
   String notifySaving();

   @DefaultMessage("Save FAILED: {0}")
   String notifyUpdateFailed(String errorMessage);

   @DefaultMessage("Saved")
   String notifyUpdateSaved();

   @DefaultMessage("Validation error - See validation message")
   String notifyValidationError();

   @DefaultMessage("Failed to Stop Editing TransUnit")
   String notifyStopFailed();

   @DefaultMessage("Failed to Lock TransUnit")
   String notifyLockFailed();

   @DefaultMessage("Workspace is set to read only")
   String notifyReadOnlyWorkspace();

   @DefaultMessage("Workspace is set to edit mode")
   String notifyEditableWorkspace();

   @DefaultMessage("Validation Warnings: {0}")
   @AlternateMessage({ "=0", "Validation Warnings: none" })
   String validationWarningsHeading(@PluralCount
   int warningCount);
}
