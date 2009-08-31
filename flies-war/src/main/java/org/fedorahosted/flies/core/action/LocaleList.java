package org.fedorahosted.flies.core.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.core.model.FliesLocale;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;

import com.ibm.icu.util.ULocale;

@Name("localeList")
public class LocaleList extends EntityQuery<FliesLocale> {
	public LocaleList() {
		setEjbql("select locale from FliesLocale locale");
	}

	
	public void importJavaLocales(){
		ULocale [] locales = ULocale.getAvailableLocales();
		List<String> addedLocales = new ArrayList<String>();
		for(ULocale locale : locales){
			FliesLocale fliesLocale = getEntityManager().find(FliesLocale.class, FliesLocale.getFliesId(locale));
			if(fliesLocale == null){
				fliesLocale = new FliesLocale(locale);
				getEntityManager().persist(fliesLocale);
				addedLocales.add(fliesLocale.getId());
			}
		}
		if(addedLocales.size() > 0)
			getFacesMessages().add("Added {0} locales: {1}", addedLocales.size(), StringUtils.join(addedLocales, ", "));
		else
			getFacesMessages().add("No locales added");
	}
}
