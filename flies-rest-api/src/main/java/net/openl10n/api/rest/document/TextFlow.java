package net.openl10n.api.rest.document;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.fedorahosted.flies.LocaleId;
import org.fedorahosted.flies.LocaleIdAdapter;
import org.fedorahosted.flies.Namespaces;


@XmlType(name="textFlowType", namespace=Namespaces.DOCUMENT, propOrder={"content", "extensions"})
public class TextFlow implements Resource{

	private String id;
	private LocaleId lang;
	private long version = 1;
	
	private String content;
	private List<Object> extensions;

	public TextFlow() {
	}
	
	public TextFlow(String id) {
		this.id = id;
	}
	
	public TextFlow(String id, LocaleId lang) {
		this(id);
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
	
	@XmlJavaTypeAdapter(type=LocaleId.class, value=LocaleIdAdapter.class)
	@XmlAttribute(name="lang", namespace=Namespaces.XML, required=false)
	public LocaleId getLang() {
		return lang;
	}
	
	public void setLang(LocaleId lang) {
		this.lang = lang;
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
	
	public TextFlowTarget getTarget(LocaleId localeId){
		for(Object obj : getExtensions()){
			if(obj instanceof TextFlowTargets){
				TextFlowTargets tft = (TextFlowTargets) obj;
				for(TextFlowTarget t : tft.getTargets()){
					if(localeId.equals(t.getLang()))
						return t;
				}
			}
		}
		return null;
	}
	
	public void addTarget(TextFlowTarget target) {
		TextFlowTargets targets = null;
		for(Object obj : getExtensions()){
			if(obj instanceof TextFlowTargets){
				targets = (TextFlowTargets) obj;
				break;
			}
		}
		if(targets == null){
			targets = new TextFlowTargets();
			getExtensions().add(targets);
		}
		targets.getTargets().add(target);

	}
	
}
