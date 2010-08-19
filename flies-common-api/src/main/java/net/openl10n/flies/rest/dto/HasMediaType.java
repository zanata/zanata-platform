package net.openl10n.flies.rest.dto;

import javax.xml.bind.annotation.XmlTransient;

import net.openl10n.flies.rest.MediaTypes.Format;

import org.codehaus.jackson.annotate.JsonIgnore;

public interface HasMediaType
{

   @JsonIgnore
   @XmlTransient
   String getMediaType(Format format);
}
