package org.zanata.webtrans.client.resources;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface RpcMessages extends Messages
{

   @DefaultMessage("Dispatcher not set up to delegate WorkspaceContext and Identity.")
   String dispatcherSetupFailed();

   @DefaultMessage("No response from server. Please refresh your page and make sure server is still up.")
   String noResponseFromServer();
}
