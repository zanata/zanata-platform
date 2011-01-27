package net.openl10n.flies.rest.dto.extensions.gettext;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;

import net.openl10n.flies.rest.dto.ExtensionValue;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "object-type")
@JsonSubTypes({ @Type(value = PoTargetHeader.class, name = "po-target-header") })
@JsonTypeName("TranslationResourceExtension")
@XmlSeeAlso({ PoTargetHeader.class })
@XmlTransient
public interface TranslationsResourceExtension extends ExtensionValue
{

}
