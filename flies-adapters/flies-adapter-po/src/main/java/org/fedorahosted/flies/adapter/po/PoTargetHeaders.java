package org.fedorahosted.flies.adapter.po;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import net.openl10n.api.LocaleId;
/**
 * Represents a list of target-language translations for a single TextFlow
 * 
 * @author asgeirf
 *
 */
@XmlType(name="poHeadersType", namespace=PoHeader.NAMESPACE)
@XmlRootElement(name="po-target-headers", namespace=PoHeader.NAMESPACE)
public class PoTargetHeaders {
	
	private Set<PoTargetHeader> headers;
	
	@XmlElement(name="po-target-header", namespace=PoHeader.NAMESPACE)
	public Set<PoTargetHeader> getHeaders() {
		if(headers == null)
			headers = new HashSet<PoTargetHeader>();
		return headers;
	}

	public PoTargetHeader getByLocale(LocaleId locale){
		for(PoTargetHeader header : headers){
			if(locale.equals(header.getTargetLanguage()))
				return header;
		}
		
		return null;
	}
}