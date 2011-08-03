package org.zanata.rest.dto.extensions.gettext;

import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeName;
import org.zanata.rest.dto.ExtensionValue;
import org.zanata.rest.dto.extensions.comment.SimpleComment;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "object-type")
@JsonSubTypes({ @Type(value = SimpleComment.class, name = "comment") })
@JsonTypeName("TextFlowTargetExtension")
@XmlSeeAlso({ SimpleComment.class })
@XmlTransient
public interface TextFlowTargetExtension extends ExtensionValue
{

}
