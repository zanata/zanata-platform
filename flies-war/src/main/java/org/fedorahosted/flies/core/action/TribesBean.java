package org.fedorahosted.flies.core.action;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.persistence.EntityManager;

import org.fedorahosted.flies.core.model.HFliesLocale;
import org.fedorahosted.flies.core.model.HTribe;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Name("tribesBean")
@Scope(ScopeType.STATELESS)
public class TribesBean {

	static class TribeComparator implements Comparator<HTribe> {
		public int compare(HTribe a, HTribe b) {
			HFliesLocale aFliesLocale = a.getLocale();
			HFliesLocale bFliesLocale = b.getLocale();
			String aDisplayName = aFliesLocale.getLocale().getDisplayName();
			String bDisplayName = bFliesLocale.getLocale().getDisplayName();
			int comparison = aDisplayName.compareTo(bDisplayName);
			if (comparison == 0) {
				String aNativeName = aFliesLocale.getNativeName();
				String bNativeName = bFliesLocale.getNativeName();
				comparison = aNativeName.compareTo(bNativeName);
				if (comparison == 0)
					// if all else fails, fall back on numerical ID sort
					return a.getId().compareTo(b.getId());
				return comparison;
			}
			return comparison;
		}
	}

	@In EntityManager entityManager;
	
	public List<HTribe> getLatestTribes(){
		// NB ULocale data isn't stored in the database, so we have 
		// to do a post-select sort.
		List<HTribe> tribes = entityManager.createQuery("select t from HTribe t").getResultList();

		// This Comparator isn't complete enough for general use, but it should work 
		Collections.sort(tribes, new TribeComparator());
		return tribes;
	}
	
	
}
