/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.util;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jboss.seam.Component;
import javax.inject.Named;
import org.jboss.seam.annotations.intercept.BypassInterceptors;

/**
 * Service Locator for Seam components, intended for obtaining short-lived
 * components to use inside methods of long-lived components, such as DAOs
 * inside application scope singletons.
 * <p>
 * It's still an anti-pattern, but at least this way callers don't use
 * Component.getInstance() directly, and ServiceLocator can be subclassed
 * to return mock objects for testing.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */

@BypassInterceptors
@Named("serviceLocator")
@javax.enterprise.context.Dependent
public class ServiceLocator {

    public static ServiceLocator instance() {
        return (ServiceLocator) Component.getInstance(ServiceLocator.class);
    }

    public <T> T getInstance(Class<T> clazz) {
        return (T) Component.getInstance(clazz);
    }

    public <T> T getInstance(String name, Class<T> clazz) {
        return (T) Component.getInstance(name);
    }

    public <T> T getInstance(String name, ScopeType scope, Class<T> clazz) {
        return (T) Component.getInstance(name, scope);
    }

    public EntityManager getEntityManager() {
        return (EntityManager) Component.getInstance("entityManager");
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return (EntityManagerFactory) Component.getInstance("entityManagerFactory");
    }

    public <T> T getJndiComponent(String jndiName, Class<T> clazz)
            throws NamingException {
        Context ctx = new InitialContext();
        return (T) ctx.lookup(jndiName);
    }

}
