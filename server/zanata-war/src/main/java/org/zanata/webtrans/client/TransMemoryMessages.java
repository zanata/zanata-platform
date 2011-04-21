package org.zanata.webtrans.client;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages.DefaultMessage;

@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface TransMemoryMessages extends Messages
{
   @DefaultMessage("Phrase")
   String tmPhraseButtonLabel();

   @DefaultMessage("Clear")
   String tmClearButtonLabel();

   @DefaultMessage("Search")
   String tmSearchButtonLabel();
}
