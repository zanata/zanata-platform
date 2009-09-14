package org.fedorahosted.flies.core.action;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.fedorahosted.flies.core.model.HFliesLocale;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.faces.FacesMessages;
import org.jboss.seam.framework.EntityQuery;

import com.ibm.icu.util.ULocale;

@Name("localeList")
public class LocaleList extends EntityQuery<HFliesLocale> {
	public LocaleList() {
		setEjbql("select locale from HFliesLocale locale");
	}

	
	public void importJavaLocales(){
		ULocale [] locales = ULocale.getAvailableLocales();
		List<String> addedLocales = new ArrayList<String>();
		for(ULocale locale : locales){
			HFliesLocale fliesLocale = getEntityManager().find(HFliesLocale.class, HFliesLocale.getFliesId(locale));
			if(fliesLocale == null){
				fliesLocale = new HFliesLocale(locale);
				getEntityManager().persist(fliesLocale);
				addedLocales.add(fliesLocale.getId());
			}
		}
		if(addedLocales.size() > 0)
			FacesMessages.instance().add("Added {0} locales: {1}", addedLocales.size(), StringUtils.join(addedLocales, ", "));
		else
			FacesMessages.instance().add("No locales added");
	}
}
