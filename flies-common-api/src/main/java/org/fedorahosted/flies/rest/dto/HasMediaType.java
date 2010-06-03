package org.fedorahosted.flies.rest.dto;

import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.fedorahosted.flies.rest.MediaTypes.Format;

public interface HasMediaType {

	@JsonIgnore
	@XmlTransient
	String getMediaType(Format format);
}
