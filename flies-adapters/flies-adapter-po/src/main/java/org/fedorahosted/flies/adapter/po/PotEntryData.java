package org.fedorahosted.flies.adapter.po;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.rest.dto.SimpleComment;
import org.fedorahosted.tennera.jgettext.Message;

@XmlRootElement(name="po-entry", namespace=PoHeader.NAMESPACE)
@XmlType(name="poEntryType", namespace=PoHeader.NAMESPACE, propOrder={"context", "extractedComment", "references", "flags"})
public class PotEntryData {

	private String id;
	private String context;
	private SimpleComment extractedComment;
	private List<String> flags;
	private List<String> references;
	
	public PotEntryData() {
	}
	
	public PotEntryData(String id) {
		this.id = id;
	}
	
	public PotEntryData(String id, Message message) {
		this.id = id;
		if(message.getMsgctxt() != null){
			setContext(message.getMsgctxt());
		}
		getExtractedComment().setValue(StringUtils.join(message.getExtractedComments(),"\n"));
		getFlags().addAll(message.getFormats());
		getReferences().addAll(message.getSourceReferences());
	}
	
	
	@XmlAttribute(name="id", required=true)
	public String getId() {
		return id;
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	@XmlElement(name="context", namespace=PoHeader.NAMESPACE, required=false)
	public String getContext() {
		return context;
	}
	
	public void setContext(String context) {
		this.context = context;
	}
	
	@XmlElement(name="extracted-comment", namespace=PoHeader.NAMESPACE, required=false)
	public SimpleComment getExtractedComment() {
		if(extractedComment == null)
			extractedComment = new SimpleComment();
		return extractedComment;
	}
	
	public void setExtractedComment(SimpleComment extractedComment) {
		this.extractedComment = extractedComment;
	}
	
	@XmlElementWrapper(name="flags", namespace=PoHeader.NAMESPACE, required=true)
	@XmlElement(name="flag", namespace=PoHeader.NAMESPACE)
	public List<String> getFlags() {
		if(flags == null)
			flags = new ArrayList<String>();
		return flags;
	}
	
	@XmlElementWrapper(name="source-references", namespace=PoHeader.NAMESPACE, required=true)
	@XmlElement(name="sourcereference", namespace=PoHeader.NAMESPACE)
	public List<String> getReferences() {
		if(references == null)
			references = new ArrayList<String>();
		return references;
	}
	
}

