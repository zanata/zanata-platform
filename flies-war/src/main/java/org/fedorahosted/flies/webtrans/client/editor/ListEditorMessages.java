package org.fedorahosted.flies.webtrans.client.editor;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface ListEditorMessages extends Messages
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

   @DefaultMessage("Failed to load data from Server")
   String notifyLoadFailed();

   @DefaultMessage("An unknown error occurred")
   String notifyUnknownError();

   @DefaultMessage("Failed to update Translation Unit")
   String notifyUpdateFailed();

   @DefaultMessage("Saved change to Translation Unit")
   String notifyUpdateSaved();

   @DefaultMessage("Failed to Stop Editing TransUnit")
   String notifyStopFailed();

   @DefaultMessage("Failed to Lock TransUnit")
   String notifyLockFailed();
}
