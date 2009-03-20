package org.fedorahosted.flies.core.action;

import org.jboss.seam.annotations.Name;
import org.jboss.seam.framework.EntityQuery;
import org.fedorahosted.flies.core.model.FliesLocale;

@Name("localeList")
public class LocaleList extends EntityQuery<FliesLocale> {
	public LocaleList() {
		setEjbql("select locale from FliesLocale locale");
	}

}
