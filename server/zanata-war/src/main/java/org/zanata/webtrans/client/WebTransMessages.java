package org.zanata.webtrans.client;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface WebTransMessages extends Messages
{

   @DefaultMessage("{0} participants")
   @AlternateMessage({ "one", "One participant" })
   @Description("Title of the minimized users panel")
   String nUsersOnline(@PluralCount int numUsers);

   @DefaultMessage("{0}% (words)")
   String statusBarLabelPercentage(int approved, @Optional int needReview, @Optional int untranslated);
   
   @DefaultMessage("{0,number,#.#} hours")
   String statusBarLabelHours(double remainingHours);

   @DefaultMessage("{0}/{1}/{2} (wds)")
   String statusBarLabelWords(int approved, int needReview, int untranslated);

   @DefaultMessage("{0}/{1}/{2} (msgs)")
   String statusBarLabelUnits(int approved, int needReview, int untranslated);

   @DefaultMessage("{0}%")
   String statusGraphLabelPercentage(int approved, @Optional int needReview, @Optional int untranslated);

   @DefaultMessage("{0,number,#.#} hrs")
   String statusGraphLabelHours(double remainingHours);

   @DefaultMessage("{0} wds")
   String statusGraphLabelWords(int stat);

   @DefaultMessage("http://zanata.org/")
   String hrefHelpLink();

   @DefaultMessage("{0} to {1} - Zanata Web Translation")
   String windowTitle(String workspaceName, String localeName);

   @DefaultMessage("First Page")
   String firstPage();

   @DefaultMessage("Home")
   String firstPageShortcut();

   @DefaultMessage("Previous Page")
   String prevPage();

   @DefaultMessage("PageUp")
   String prevPageShortcut();

   @DefaultMessage("Next Page")
   String nextPage();

   @DefaultMessage("PageDown")
   String nextPageShortcut();

   @DefaultMessage("Last Page")
   String lastPage();

   @DefaultMessage("End")
   String lastPageShortcut();

   @DefaultMessage("Show Translation Suggestions")
   String showTranslationMemoryPanel();

   @DefaultMessage("Find")
   String findButton();

   @DefaultMessage("Find Messages")
   String transUnitSearchesHeading();

   @DefaultMessage("Translation Unit Details")
   String transUnitDetailsHeading();

   @DefaultMessage("Source or Target content")
   String findSourceOrTargetString();

   @DefaultMessage("{0} (Shortcut: {1})")
   String tooltipsWithShortcut(String text, String shortcut);

   @DefaultMessage("Failed to load document from Server")
   String loadDocFailed();

   @DefaultMessage("Directory")
   String columnHeaderDirectory();

   @DefaultMessage("Document")
   String columnHeaderDocument();

   @DefaultMessage("Statistic")
   String columnHeaderStatistic();

   @DefaultMessage("Translated")
   String columnHeaderTranslated();

   @DefaultMessage("Untranslated")
   String columnHeaderUntranslated();

   @DefaultMessage("Remaining")
   String columnHeaderRemaining();
}
