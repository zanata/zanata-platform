package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 */
public enum ValidationId implements IsSerializable {

   // @formatter:off
   HTML_XML("HTML/XML tags"),
   NEW_LINE("Leading/trailing newline (\\n)"),
   TAB("Tab characters (\\t)"),
   JAVA_VARIABLES("Java variables"),
   XML_ENTITY("XML entity reference"),
   PRINTF_VARIABLES("Printf variables"),
   PRINTF_XSI_EXTENSION("Positional printf (XSI extension)");

   // @formatter:on
    private String displayName;

    ValidationId(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
