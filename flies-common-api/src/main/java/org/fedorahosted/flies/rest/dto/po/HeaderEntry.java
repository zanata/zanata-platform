package org.fedorahosted.flies.rest.dto.po;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.StringUtils;

@XmlType(name="headerEntryType", namespace=PoHeader.NAMESPACE)
public class HeaderEntry {
	private String key;
	private String value;
	
	public HeaderEntry() {
	}
	
	public HeaderEntry(String key, String value) {
		this.key = key;
		this.value = value;
	}
	
	
	@XmlAttribute(name="key", required=true)
	public String getKey() {
		if(key == null)
			key = "";
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	@XmlAttribute(name="value", required=true)
	public String getValue() {
		if(value == null)
			value = "";
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj)
			return true;
		if(obj == null)
			return false;
		if( ! (obj instanceof HeaderEntry) ) 
			return false;

		HeaderEntry other = (HeaderEntry) obj;
		return StringUtils.equals(this.key, other.key) && StringUtils.equals(this.value, other.value); 
	}
}
