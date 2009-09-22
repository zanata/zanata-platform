package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;



@XmlType(name="referenceType", namespace=Namespaces.FLIES, propOrder={"extensions"})
@XmlRootElement(name="reference", namespace=Namespaces.FLIES)
public class Reference extends AbstractBaseResource implements Resource{
	
	private String id;
	private long version = 1;
	
	private List<Object> extensions;
	
	public Reference() {
	}
	
	public Reference(String id) {
		this.id = id;
	}

	public Reference(String id, String relationshipId) {
		this(id);
		this.relationshipId = relationshipId;
	}

	@XmlAttribute(name="id", required=true)
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute(name="version", required=true)
	public long getVersion() {
		return version;
	}
	
	public void setVersion(long version) {
		this.version = version;
	}

	@XmlAnyElement(lax=true)
	public List<Object> getExtensions() {
		return extensions;
	}
	
	@Override
	public List<Object> getExtensions(boolean create) {
		if(extensions == null && create)
			extensions = new ArrayList<Object>();
		return extensions;
	}
	
	@Override
	public boolean hasExtensions() {
		return extensions != null;
	}
	
	@Override
	public <T> T getExtension(Class<T> clz){
		if(extensions == null)
			return null;
		for(Object o : extensions){
			if(clz.isInstance(o))
				return clz.cast(o);
		}
		return null;
	}
	
	@Override
	public <T> T getOrAddExtension(Class<T> clz) {
		T ext = getExtension(clz);
		if(ext == null){
			try {
				ext = clz.newInstance();
			} catch (Throwable e) {
				throw new RuntimeException("unable to create instance", e);
			}
		}
		return ext;
	}
	
	private String relationshipId;
	
	@XmlAttribute(name="rId", required=true)
	public String getRelationshipId() {
		return relationshipId;
	}
	
	public void setRelationshipId(String relationshipId) {
		this.relationshipId = relationshipId;
	}
	
}
