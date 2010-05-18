package org.fedorahosted.flies.hibernate.search;

import org.apache.lucene.search.Filter;
import org.fedorahosted.flies.common.LocaleId;
import org.hibernate.search.annotations.Factory;
import org.hibernate.search.annotations.Key;
import org.hibernate.search.filter.FilterKey;
import org.hibernate.search.filter.StandardFilterKey;
import org.jboss.seam.Seam;

public class TranslatedFilterFactory {
	@Factory
	public static Filter getFilter() {
		return (TranslatedFilter) Seam.componentForName("translatedFilter").newInstance();
	}

	private LocaleId locale;
	
	public LocaleId getLocale() {
		return locale;
	}
	
	public void setLocale(LocaleId locale) {
		this.locale = locale;
	}
	
	@Key
	public FilterKey getKey() {
		StandardFilterKey key = new StandardFilterKey();
		key.addParameter(locale);
		return key;
	}

}
