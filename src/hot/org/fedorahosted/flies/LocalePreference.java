package org.fedorahosted.flies;

import java.util.Map;

import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.fedorahosted.flies.entity.FliesLocale;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.util.Conversions.SetConverter;

@Scope(ScopeType.SESSION)
@Name("localePreference")
public class LocalePreference {

	@In
	EntityManager entityManager;
	
	public FliesLocale getPreference(){
		Cookie cookie = getCookie();
		return entityManager.find(FliesLocale.class, cookie.getValue());
	}

	public void setPreference(FliesLocale locale){
		Cookie cookie = new Cookie("selectedLocale", locale.getId());
		HttpServletResponse response = (HttpServletResponse)FacesContext.getCurrentInstance()
			.getExternalContext().getResponse(); 
		response.addCookie(cookie);
	}
	
	private Cookie getCookie(){
		FacesContext facesContext =  FacesContext.getCurrentInstance();
		Cookie cookies[] = ((HttpServletRequest)facesContext.getExternalContext().
				getRequest()).getCookies();
		if(cookies == null) return null;
		
		for(Cookie cookie: cookies){
			if("selectedLocale".equals(cookie.getName())){
				return cookie;
			}
		}
		
		return null;
	}
	
	public boolean hasPreference(){
		return getCookie() != null;
	}
	
}
