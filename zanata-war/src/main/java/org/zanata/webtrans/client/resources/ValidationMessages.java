package org.zanata.webtrans.client.resources;

import java.util.Collection;
import java.util.List;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

/**
 * @author David Mason, damason@redhat.com
 * 
 */
@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface ValidationMessages extends Messages
{
   // Newline validator

   @DefaultMessage("Leading/trailing newline (\\n)")
   String newlineValidatorName();

   @DefaultMessage("Check for consistent leading and trailing newline (\\n)")
   String newlineValidatorDescription();

   @DefaultMessage("Leading newline (\\n) is missing")
   String leadingNewlineMissing();

   @DefaultMessage("Unexpected leading newline (\\n)")
   String leadingNewlineAdded();

   @DefaultMessage("Trailing newline (\\n) is missing")
   String trailingNewlineMissing();

   @DefaultMessage("Unexpected trailing newline (\\n)")
   String trailingNewlineAdded();

   // Tab validator
   @DefaultMessage("Tab characters (\t)")
   String tabValidatorName();
   @DefaultMessage("Check whether only source or only target have tabs")
   String tabValidatorDescription();

   @DefaultMessage("Source has one or more tabs but target has no tabs (t)")
   String targetHasNoTabs();
   @DefaultMessage("Target has one or more tabs but source has no tabs (t)")
   String targetHasExtraTabs();

   // Printf variables validator

   @DefaultMessage("Printf variables")
   String printfVariablesValidatorName();

   @DefaultMessage("Check that printf style (%x) variables are consistent")
   String printfVariablesValidatorDescription();

   // Printf variables validator with XSI extension (positional variables)
   @DefaultMessage("Positional printf (XSI extension)")
   String positionalPrintfVariablesValidatorName();

   @DefaultMessage("Variable {0} position is out of range")
   String varPositionOutOfRange(String var);

   @DefaultMessage("Numbered arguments cannot mix with unnumbered arguments")
   String mixVarFormats();

   @DefaultMessage("Variables have same position: {0,collection,string}")
   String varPositionDuplicated(Collection<String> vars);

   @DefaultMessage("Check that positional printf style (%n$x) variables are consistent")
   String positionalPrintfVariablesValidatorDescription();

   // Java variables validator

   @DefaultMessage("Java variables")
   String javaVariablesValidatorName();

   @DefaultMessage("Check that java style ('{x}') variables are consistent")
   String javaVariablesValidatorDescription();

   @Description("Lists variables that appear a different number of times between source and target strings")
   @DefaultMessage("Inconsistent count for variables: {0,list,string}")
   @AlternateMessage({ "one", "Inconsistent count for variable: {0,list,string}" })
   String differentVarCount(@PluralCount List<String> vars);

   @DefaultMessage("Number of apostrophes ('') in source does not match number in translation. This may lead to other warnings.")
   String differentApostropheCount();

   @DefaultMessage("Quoted characters found in translation but not in source text. " +
   "Apostrophe character ('') must be doubled ('''') to prevent quoting " +
   "when it is used in Java MessageFormat strings.")
   String quotedCharsAdded();


   // Shared variables validator messages

   @Description("Lists the variables that are in the original string but have not been included in the target")
   @DefaultMessage("Missing variables: {0,list,string}")
   @AlternateMessage({ "one", "Missing variable: {0,list,string}" })
   String varsMissing(@PluralCount List<String> vars);

   @Description("Lists the variables that are in the original string and are present but quoted in the target")
   @DefaultMessage("Unexpected quoting of variables: {0,list,string}")
   @AlternateMessage({ "one", "Unexpected quoting of variable: {0,list,string}" })
   String varsMissingQuoted(@PluralCount List<String> vars);

   @Description("Lists the variables that are in the target but are not in the original string")
   @DefaultMessage("Unexpected variables: {0,list,string}")
   @AlternateMessage({ "one", "Unexpected variable: {0,list,string}" })
   String varsAdded(@PluralCount List<String> vars);

   @Description("Lists the variables that are in the target and are present but quoted in the original string")
   @DefaultMessage("Variables not quoted: {0,list,string}")
   @AlternateMessage({ "one", "Variable not quoted: {0,list,string}" })
   String varsAddedQuoted(@PluralCount List<String> vars);



   // XHM/HTML tag validator

   @DefaultMessage("XML/HTML tags")
   String xmlHtmlValidatorName();

   @DefaultMessage("Check that XML/HTML tags are consistent")
   String xmlHtmlValidatorDescription();

   @Description("Lists the xml or html tags that are in the target but are not in the original string")
   @DefaultMessage("Unexpected tags: {0,list,string}")
   @AlternateMessage({ "one", "Unexpected tag: {0,list,string}" })
   String tagsAdded(@PluralCount List<String> tags);

   @Description("Lists the xml or html tags that are in the original string but have not been included in the target")
   @DefaultMessage("Missing tags: {0,list,string}")
   @AlternateMessage({ "one", "Missing tag: {0,list,string}" })
   String tagsMissing(@PluralCount List<String> tags);

   @DefaultMessage("Tags in unexpected position: {0,list,string}")
   @AlternateMessage({ "one", "Tag in unexpected position: {0,list,string}" })
   String tagsWrongOrder(@PluralCount List<String> tags);


   //XML Entity validator

   @DefaultMessage("XML entity reference")
   String xmlEntityValidatorName();

   @DefaultMessage("Check that XML entity are complete")
   String xmlEntityValidatorDescription();

   @DefaultMessage("Invalid XML entity [ {0} ]")
   String invalidXMLEntity(String entity);
   
   @Description("List of XML entity in original string have not been included in the target")
   @DefaultMessage("Missing entity: {0,list,string}")
   String entityMissing(List<String> entities);

   @DefaultMessage("Possible XML entity [ {0} ] does not match with pre-defined [ {1} ]")
   String invalidPredefinedEnity(String word, String preDefinedEntity);


}
