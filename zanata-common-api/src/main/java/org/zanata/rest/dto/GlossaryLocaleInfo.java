package org.zanata.rest.dto;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.Namespaces;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@XmlRootElement(name = "glossaryLocaleInfo")
@XmlType(name = "glossaryLocaleInfoType", propOrder = { "locale", "numberOfTerms" })
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "locale", "numberOfTerms"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class GlossaryLocaleInfo implements Serializable {
    private LocaleDetails locale;
    private int numberOfTerms;

    public GlossaryLocaleInfo() {
        this(null, 0);
    }

    public GlossaryLocaleInfo(LocaleDetails locale, int numberOfTerms) {
        this.locale = locale;
        this.numberOfTerms = numberOfTerms;
    }

    @XmlElement(name = "locale", required = false,
        namespace = Namespaces.ZANATA_API)
    public LocaleDetails getLocale() {
        return locale;
    }

    public void setLocale(LocaleDetails locale) {
        this.locale = locale;
    }

    @XmlElement(name = "numberOfTerms", required = false,
        namespace = Namespaces.ZANATA_API)
    public int getNumberOfTerms() {
        return numberOfTerms;
    }

    public void setNumberOfTerms(int numberOfTerms) {
        this.numberOfTerms = numberOfTerms;
    }
}
