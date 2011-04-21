package org.zanata.rest.dto.extensions.gettext;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.zanata.rest.dto.ExtensionValue;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "object-type")
@JsonSubTypes({ @Type(value = PoHeader.class, name = "po-header") })
@JsonTypeName("AbstractResourceMetaExtension")
@XmlSeeAlso({ PoHeader.class })
@XmlTransient
public interface AbstractResourceMetaExtension extends ExtensionValue
{

}
