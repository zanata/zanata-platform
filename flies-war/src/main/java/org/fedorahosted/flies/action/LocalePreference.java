package org.fedorahosted.flies.action;

import java.io.Serializable;

import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fedorahosted.flies.model.HFliesLocale;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;

@Scope(ScopeType.SESSION)
@Name("localePreference")
public class LocalePreference implements Serializable{

	
	private static final long serialVersionUID = 1611775611052433060L;

	@In
	EntityManager entityManager;

	public HFliesLocale getPreference() {
		Cookie cookie = getCookie();
		return entityManager.find(HFliesLocale.class, cookie.getValue());
	}

	public void setPreference(HFliesLocale locale) {
		Cookie cookie = new Cookie("selectedLocale", locale.getId());
		HttpServletResponse response = (HttpServletResponse) FacesContext
				.getCurrentInstance().getExternalContext().getResponse();
		response.addCookie(cookie);
	}

	private Cookie getCookie() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		Cookie cookies[] = ((HttpServletRequest) facesContext
				.getExternalContext().getRequest()).getCookies();
		if (cookies == null)
			return null;

		for (Cookie cookie : cookies) {
			if ("selectedLocale".equals(cookie.getName())) {
				return cookie;
			}
		}

		return null;
	}

	public boolean hasPreference() {
		return getCookie() != null;
	}

}
