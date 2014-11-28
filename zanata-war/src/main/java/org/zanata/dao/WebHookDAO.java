package org.zanata.dao;

import org.hibernate.Session;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.model.WebHook;

@Name("webHookDAO")
@AutoCreate
@Scope(ScopeType.STATELESS)
public class WebHookDAO extends AbstractDAOImpl<WebHook, Integer> {

    public WebHookDAO() {
        super(WebHook.class);
    }

    public WebHookDAO(Session session) {
        super(WebHook.class, session);
    }
}
