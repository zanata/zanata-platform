package org.zanata.rest.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.webcohesion.enunciate.metadata.Label;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.zanata.common.Namespaces;

/**
 * Information about a specific Glossary.
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@XmlRootElement(name = "glossaryInfo")
@XmlType(name = "glossaryInfoType", propOrder = { "srcLocale", "transLocale"})
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({ "srcLocale", "transLocale"})
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
@Label("Glossary Info")
public class GlossaryInfo implements Serializable {
    private static final long serialVersionUID = -5688873815049369490L;
    private GlossaryLocaleInfo srcLocale;
    private List<GlossaryLocaleInfo> transLocale;

    public GlossaryInfo() {
        this(null, new ArrayList<GlossaryLocaleInfo>());
    }

    public GlossaryInfo(GlossaryLocaleInfo srcLocale,
        List<GlossaryLocaleInfo> transLocale) {
        this.srcLocale = srcLocale;
        this.transLocale = transLocale;
    }

    /**
     * The glossary's source locale
     */
    @XmlElement(name = "srcLocale", required = false,
        namespace = Namespaces.ZANATA_API)
    public GlossaryLocaleInfo getSrcLocale() {
        return srcLocale;
    }

    public void setSrcLocale(GlossaryLocaleInfo srcLocale) {
        this.srcLocale = srcLocale;
    }

    /**
     * The list of translated locale's available for the glossary
     */
    @XmlElement(name = "transLocale", required = false,
        namespace = Namespaces.ZANATA_API)
    public List<GlossaryLocaleInfo> getTransLocale() {
        return transLocale;
    }

    public void setTransLocale(List<GlossaryLocaleInfo> transLocale) {
        this.transLocale = transLocale;
    }
}

