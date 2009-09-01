package org.fedorahosted.flies;

import java.io.Serializable;

public final class LocaleId implements Serializable{

	private static final long serialVersionUID = -7977805381672178179L;

	private final String id;
	// TODO split up to language code, country code, qualifier etc..

	public static final LocaleId EN = new LocaleId("en");
	public static final LocaleId EN_US = new LocaleId("en-US");
	
	// JaxB needs a no-arg constructor :(
	private LocaleId(){
		id = null;
	}
	
	public LocaleId(String localeId) {
		if(localeId == null)
			throw new IllegalArgumentException("localeId");
		this.id = localeId.intern();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == this) return true;
		if (!(obj instanceof LocaleId) ) return false;
		return this.id == ((LocaleId)obj).id;
	}
	
	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public String toString() {
		return id;
	}
	
    public static LocaleId fromJavaName(String localeName) {
        return new LocaleId(localeName.replace('_', '-'));
    }

    public String toJavaName() {
        return id.replace('-', '_');
    }
	
	
}
