package org.fedorahosted.flies.rest.dto;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fedorahosted.flies.LocaleId;


@XmlType(name="textFlowTargetType", namespace=Namespaces.FLIES, propOrder={"content", "extensions"})
@XmlRootElement(name="text-flow-target", namespace=Namespaces.FLIES)
@XmlSeeAlso({
	SimpleComment.class
})
public class TextFlowTarget implements IExtensible{
	
	private LocaleId lang;
	private List<Object> extensions;
	
	@XmlEnum(String.class)
	@XmlType(name="contentStateType", namespace=Namespaces.FLIES)
	public static enum ContentState{
		@XmlEnumValue("new") New,
		@XmlEnumValue("leveraged") Leveraged,
		@XmlEnumValue("for-review") ForReview,
		@XmlEnumValue("approved") Final
	}
	
	private String id;
	private Integer resourceRevision;
	private Integer revision = 1;
	private ContentState state = ContentState.New;
	private String content;
	
	public TextFlowTarget() {
		// TODO Auto-generated constructor stub
	}
	
	public TextFlowTarget(DocumentResource resource) {
		this.id = resource.getId();
		this.resourceRevision = resource.getRevision();
	}

	public TextFlowTarget(DocumentResource resource, LocaleId lang) {
		this(resource);
		this.lang = lang;
	}

	public boolean hasComment() {
		return getExtension(SimpleComment.class) != null;
	}
	
	public SimpleComment getComment() {
		return getExtension(SimpleComment.class);
	}
	
	public SimpleComment getOrAddComment(){
		return getOrAddExtension(SimpleComment.class);
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
	
	@XmlAttribute(name="resourceRevision", required=false)
	public Integer getResourceRevision() {
		return resourceRevision;
	}
	
	public void setResourceRevision(Integer resourceRevision) {
		this.resourceRevision = resourceRevision;
	}
	
	@XmlAttribute(name="revision", required=false)
	public Integer getRevision() {
		return revision;
	}
	
	public void setRevision(Integer revision) {
		this.revision = revision;
	}
	
	@XmlAttribute(name="state", required=true)
	public ContentState getState() {
		return state;
	}
	
	public void setState(ContentState state) {
		this.state = state;
	}
	
	@XmlElement(name="content",namespace=Namespaces.FLIES, required=true)
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
				getExtensions().add(ext);
			} catch (Throwable e) {
				throw new RuntimeException("unable to create instance", e);
			}
		}
		return ext;
	}

	@Override
	public String toString() {
		return Utility.toXML(this);
	}

}
