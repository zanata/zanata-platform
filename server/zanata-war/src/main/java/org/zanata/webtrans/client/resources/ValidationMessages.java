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

   @DefaultMessage("Newline lead/trail")
   String newlineValidatorName();

   @DefaultMessage("Newline lead/trail validation")
   String newlineValidatorDescription();

   @DefaultMessage("Leading newline missing from target")
   String leadingNewlineMissing();

   @DefaultMessage("Leading newline added to target")
   String leadingNewlineAdded();

   @DefaultMessage("Trailing newline missing from target")
   String trailingNewlineMissing();

   @DefaultMessage("Trailing newline added to target")
   String trailingNewlineAdded();


   // Variables validator

   @DefaultMessage("Variables check")
   String variablesValidatorName();

   @DefaultMessage("Variables check validation")
   String variablesValidatorDescription();

   @Description("Lists the variables that are in the original string but have not been included in the target")
   @DefaultMessage("Variables [ {0,list,string} ] missing in target")
   @AlternateMessage({ "one", "Variable [ {0,list,string} ] missing in target" })
   String varsMissing(@PluralCount
   List<String> vars);

   @Description("Lists the variables that are in the target but are not in the original string")
   @DefaultMessage("Variables [ {0,list,string} ] added in target")
   @AlternateMessage({ "one", "Variable [ {0,list,string} ] added in target" })
   String varsAdded(@PluralCount
   List<String> vars);


   // XHM/HTML tag validator

   @DefaultMessage("HTML/XML tag")
   String xmlHtmlValidatorName();

   @DefaultMessage("Matching HTML/XML tag validation")
   String xmlHtmlValidatorDescription();

   @Description("Lists the xml or html tags that are in the target but are not in the original string")
   @DefaultMessage("Tags [ {0,list,string} ] added in target")
   @AlternateMessage({ "one", "Tag [ {0,list,string} ] added in target" })
   String tagsAdded(@PluralCount List<String> tags);

   @Description("Lists the xml or html tags that are in the original string but have not been included in the target")
   @DefaultMessage("Tags [ {0,list,string} ] missing in target")
   @AlternateMessage({ "one", "Tag [ {0,list,string} ] missing in target" })
   String tagsMissing(@PluralCount List<String> tags);

   @DefaultMessage("Tags [ {0,list,string} ] are in a different order in target and source")
   @AlternateMessage({ "one", "Tag [ {0,list,string} ] is in a different location in target and source" })
   String tagsWrongOrder(@PluralCount List<String> tags);
}
