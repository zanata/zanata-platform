package org.fedorahosted.flies.webtrans.client.ui;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface UiMessages extends Messages
{

   @DefaultMessage("")
   String typeToEnter();
}
