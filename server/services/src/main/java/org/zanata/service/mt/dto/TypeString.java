package org.zanata.service.mt.dto;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class TypeString {
    private String value;
    private String type = "text/xml";
    private String metadata = "";

    public TypeString(String value) {
        this.value = value;
    }

    public TypeString() {
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getMetadata() {
        return metadata;
    }
}
