package org.fedorahosted.flies.rest.dto.resource;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.codehaus.jackson.annotate.JsonTypeInfo.As;
import org.codehaus.jackson.annotate.JsonTypeInfo.Id;
import org.fedorahosted.flies.common.Namespaces;
import org.fedorahosted.flies.rest.dto.extensions.PoHeader;
import org.fedorahosted.flies.rest.dto.extensions.PoTargetHeaders;
import org.fedorahosted.flies.rest.dto.extensions.PotEntryHeader;
import org.fedorahosted.flies.rest.dto.extensions.SimpleComment;

//@XmlType(name="extensionType", namespace=Namespaces.FLIES)
@XmlSeeAlso({PoHeader.class, PotEntryHeader.class, SimpleComment.class, PoTargetHeaders.class})
@JsonTypeInfo(use=Id.NAME, include=As.WRAPPER_OBJECT)
@JsonSubTypes({
	@JsonSubTypes.Type(PoHeader.class),
	@JsonSubTypes.Type(PotEntryHeader.class),
	@JsonSubTypes.Type(PoTargetHeaders.class),
	@JsonSubTypes.Type(SimpleComment.class)
})
public abstract class Extension {

	private String id;
	private String version;

	public Extension() {
	}
	
	public Extension(String id, String version) {
		setId(id);
		setVersion(version);
	}
	
	@JsonIgnore // covered by wrapper id
	@XmlAttribute(name="id", required=true)
	public String getId(){
		return id;
	}
	
	@XmlAttribute(name="version", required=true)
	public String getVersion(){
		return version;
	}
	
	protected void setId(String id) {
		if(id == null) {
			throw new NullPointerException("id is null");
		}
		this.id = id;
	}
	
	protected void setVersion(String version) {
		if(version == null) {
			throw new NullPointerException("id is null");
		}
		this.version = version;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if(obj == null) return false;
		if( !(obj instanceof Extension) ) return false;
		Extension other = (Extension) obj;
		return other.id.equals(id) && other.version.equals(version);
	}
	
	@Override
	public int hashCode() {
	    int hash = 1;
	    hash += hash * 31 + id.hashCode();
	    hash += hash * 31 + version.hashCode();
	    return hash;
	}

}
