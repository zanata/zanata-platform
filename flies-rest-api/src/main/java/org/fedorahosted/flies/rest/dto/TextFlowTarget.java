package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fedorahosted.flies.LocaleId;


@XmlType(name="textFlowTargetType", namespace=TextFlowTargets.NAMESPACE, propOrder={"content", "extensions"})
public class TextFlowTarget implements IExtensible{
	
	private LocaleId lang;
	private List<Object> extensions;
	
	@XmlEnum(String.class)
	public static enum ContentState{
		@XmlEnumValue("new") New,
		@XmlEnumValue("leveraged") Leveraged,
		@XmlEnumValue("for-review") ForReview,
		@XmlEnumValue("approved") Final
	}
	
	private String id;
	private long version = 1;
	private ContentState state = ContentState.New;
	private String content;
	
	public TextFlowTarget() {
		// TODO Auto-generated constructor stub
	}
	
	public TextFlowTarget(Resource resource) {
		this.id = resource.getId();
		this.version = resource.getVersion();
	}

	public TextFlowTarget(Resource resource, LocaleId lang) {
		this(resource);
		this.lang = lang;
	}

	@XmlJavaTypeAdapter(type=LocaleId.class, value=LocaleIdAdapter.class)
	@XmlAttribute(name="lang", namespace=Namespaces.XML, required=true)
	public LocaleId getLang() {
		return lang;
	}
	
	public void setLang(LocaleId lang) {
		this.lang = lang;
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
	
	@XmlAttribute(name="state", required=true)
	public ContentState getState() {
		return state;
	}
	
	public void setState(ContentState state) {
		this.state = state;
	}
	
	@XmlElement(name="content",namespace=Namespaces.DOCUMENT, required=true)
	public String getContent() {
		if(content == null)
			return "";
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
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
