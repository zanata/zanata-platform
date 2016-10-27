package org.zanata.dao;

import org.hibernate.Session;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;
import org.zanata.model.WebHook;

@Named("webHookDAO")
@RequestScoped
public class WebHookDAO extends AbstractDAOImpl<WebHook, Long> {

    public WebHookDAO() {
        super(WebHook.class);
    }

    public WebHookDAO(Session session) {
        super(WebHook.class, session);
    }
}
