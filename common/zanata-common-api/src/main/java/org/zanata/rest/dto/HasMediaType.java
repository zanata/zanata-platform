package org.zanata.rest.dto;

import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.zanata.rest.MediaTypes.Format;

public interface HasMediaType
{

   @JsonIgnore
   @XmlTransient
   String getMediaType(Format format);
}
