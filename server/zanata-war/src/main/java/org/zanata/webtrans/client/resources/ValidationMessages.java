package org.zanata.webtrans.client.resources;

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

   @DefaultMessage("Leading/trailing newline (¶)")
   String newlineValidatorName();

   @DefaultMessage("Check for consistent leading and trailing newline (¶)")
   String newlineValidatorDescription();

   @DefaultMessage("Leading newline (¶) is missing")
   String leadingNewlineMissing();

   @DefaultMessage("Unexpected leading newline (¶)")
   String leadingNewlineAdded();

   @DefaultMessage("Trailing newline (¶) is missing")
   String trailingNewlineMissing();

   @DefaultMessage("Unexpected trailing newline (¶)")
   String trailingNewlineAdded();


   // Printf variables validator

   @DefaultMessage("%x variables")
   String printfVariablesValidatorName();

   @DefaultMessage("Check that printf style (%x) variables are consistent")
   String printfVariablesValidatorDescription();


   // Java variables validator

   @DefaultMessage("{x} variables")
   String javaVariablesValidatorName();

   @DefaultMessage("Check that java style ({x}) variables are consistent")
   String javaVariablesValidatorDescription();

   @Description("Lists variables that appear a different number of times between source and target strings")
   @DefaultMessage("Inconsistent count for variables: {0,list,string}")
   String differentVarCount(@PluralCount List<String> vars);


   // Shared variables validator messages

   @Description("Lists the variables that are in the original string but have not been included in the target")
   @DefaultMessage("Missing variables: {0,list,string}")
   @AlternateMessage({ "one", "Missing variable: {0,list,string}" })
   String varsMissing(@PluralCount List<String> vars);

   @Description("Lists the variables that are in the target but are not in the original string")
   @DefaultMessage("Unexpected variables: {0,list,string}")
   @AlternateMessage({ "one", "Unexpected variable: {0,list,string}" })
   String varsAdded(@PluralCount List<String> vars);



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
}
