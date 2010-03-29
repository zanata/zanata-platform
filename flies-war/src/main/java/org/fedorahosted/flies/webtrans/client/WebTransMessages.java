package org.fedorahosted.flies.webtrans.client;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages.DefaultMessage;

@DefaultLocale("en_US")
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface WebTransMessages extends Messages {
	
	@DefaultMessage("{0} users online")
	@PluralText({"one", "One user online"})
	@Description("Title of the minimized users panel")
	String nUsersOnline(@PluralCount int numUsers);

	
	@DefaultMessage("{0}%")
	String statusBarLabelPercentage(int approved, @Optional int needReview, @Optional int untranslated);
	
	@DefaultMessage("{0}/{1}/{2}")
	String statusBarLabelUnits(int approved, int needReview, int untranslated);
	
	@DefaultMessage("http://code.google.com/docreader/#p=flies&s=flies&t=TranslatorGuide")
	String hrefHelpLink();


	@DefaultMessage("{0} to {1} - Flies Web Translation")
	String windowTitle(String workspaceName, String localeName);
	
	@DefaultMessage("first Page")
	String firstPage();

	@DefaultMessage("Home")
	String firstPageShortcut();

	@DefaultMessage("previous Page")	
	String prevPage();
	
	@DefaultMessage("PageUp")
	String prevPageShortcut();
	
	@DefaultMessage("next Page")
	String nextPage();
	
	@DefaultMessage("PageDown")			
	String nextPageShortcut();
	
	@DefaultMessage("last Page")
	String lastPage();
	
	@DefaultMessage("End")
	String lastPageShortcut();


	@DefaultMessage("Show Translation Suggestions")
	String showTranslationMemoryPanel();


	@DefaultMessage("Exact")
	String tmPhraseButtonLabel();

	@DefaultMessage("Clear")
	String tmClearButtonLabel();

	@DefaultMessage("Search")
	String tmSearchButtonLabel();
	
}
