package org.fedorahosted.flies.webtrans.client.rpc;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages.DefaultMessage;

@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface RpcMessages extends Messages
{

   @DefaultMessage("Dispatcher not set up to delegate WorkspaceContext and Identity.")
   String dispatcherSetupFailed();

}
