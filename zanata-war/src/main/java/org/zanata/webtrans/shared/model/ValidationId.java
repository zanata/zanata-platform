package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * 
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 * 
 */
public enum ValidationId implements IsSerializable
{
   // @formatter:off
   HTML_XML("HTML/XML tags", "jsf.validation.htmlXmlValidator"),
   NEW_LINE("Leading/trailing newline (\\n)", "jsf.validation.newlineValidator"),
   TAB("Tab characters (\\t)", "jsf.validation.tabValidator"),
   JAVA_VARIABLES("Java variables", "jsf.validation.javaVariablesValidator"),
   XML_ENTITY("XML entity reference", "jsf.validation.xmlEntityValidator"),
   PRINTF_VARIABLES("Printf variables", "jsf.validation.printfVariablesValidator"),
   PRINTF_XSI_EXTENSION("Positional printf (XSI extension)", "jsf.validation.printfXSIExtensionValidation");
  
   // @formatter:on
   private String displayName;
   private String message_prefix;

   ValidationId(String displayName, String message_prefix)
   {
      this.displayName = displayName;
      this.message_prefix = message_prefix;
   }

   public String getDisplayName()
   {
      return displayName;
   }
   
   public String getMessagePrefix()
   {
      return message_prefix;
   }
}
