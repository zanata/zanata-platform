package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;



@XmlType(name="dataHookType", namespace=Namespaces.FLIES, propOrder={"extensions"})
@XmlRootElement(name="data-hook", namespace=Namespaces.FLIES)
public class DataHook extends AbstractBaseResource implements Resource {

	private String id;
	private Integer revision = 1;
	
	private List<Object> extensions;
	
	public DataHook() {
	}
	
	public DataHook(String id) {
		this.id = id;
	}

	@XmlAttribute(name="id", required=true)
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}

	@XmlAttribute(name="revision", required=true)
	@Override
	public Integer getRevision() {
		return revision;
	}
	
	public void setRevision(Integer revision) {
		this.revision = revision;
	}

	@XmlAnyElement(lax=true)
	public List<Object> getExtensions() {
		if(extensions == null)
			extensions = new ArrayList<Object>();
		return extensions;
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

}
