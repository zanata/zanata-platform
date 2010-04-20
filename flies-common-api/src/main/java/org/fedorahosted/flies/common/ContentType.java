package org.fedorahosted.flies.common;

import java.io.Serializable;

public final class ContentType implements Serializable{

	private static final long serialVersionUID = -7977805381672178179L;

	private final String contentType;
	// TODO split up

	public static final ContentType TextPlain = new ContentType("text/plain");
	
	// JaxB needs a no-arg constructor :(
	@SuppressWarnings("unused")
	private ContentType(){
		contentType = null;
	}
	
	public ContentType(String contentType) {
		if(contentType == null)
			throw new IllegalArgumentException("localeId");
		this.contentType = contentType.intern();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if (!(obj instanceof ContentType) ) return false;
		return this.contentType == ((ContentType)obj).contentType;
	}
	
	@Override
	public int hashCode() {
		return contentType.hashCode();
	}

	@Override
	public String toString() {
		return contentType;
	}
	
	public String getValue() {
		return contentType;
	}
	
}
