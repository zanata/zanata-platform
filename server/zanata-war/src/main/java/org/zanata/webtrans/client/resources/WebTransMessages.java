package org.zanata.webtrans.client.resources;

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

   @DefaultMessage("(No Content)")
   String noContent();

   @DefaultMessage("{0}% ({1,number,#.#}hrs)")
   String statusBarPercentageHrs(int approved, double remainingHours);

   @DefaultMessage("{0,number,#.#}")
   String statusBarLabelHours(double remainingHours);

   @DefaultMessage("{0}%")
   String statusBarLabelPercentage(int approved);

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

   @DefaultMessage("Show Translation Details")
   String showTranslationDetailsPanel();

   @DefaultMessage("Find")
   String findButton();

   @DefaultMessage("Find Messages")
   String transUnitSearchesHeading();

   @DefaultMessage("Translation Unit Details")
   String transUnitDetailsHeading();

   @DefaultMessage("Validation Details")
   String validationDetailsHeading();

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

   @DefaultMessage("Translated words")
   String columnHeaderTranslated();

   @DefaultMessage("Untranslated words")
   String columnHeaderUntranslated();

   @DefaultMessage("Remaining hours")
   String columnHeaderRemaining();
}
