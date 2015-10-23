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

import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Default;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Service Locator for CDI beans, intended for obtaining short-lived
 * components to use inside methods of long-lived components, such as DAOs
 * inside application scope singletons.
 * <p>
 * NOTE: BeanProvider will log a warning (via JUL) if getInstance returns a
 * dependent bean (you should use getDependent instead to for correct
 * lifecycle handling).
 * <p>
 * It's still an anti-pattern, but at least this way callers don't use
 * Component.getInstance() directly, and ServiceLocator can be subclassed
 * to return mock objects for testing.
 * <p>
 * Note that the deprecated methods which accept a bean name will output a
 * debug message if enabled, and go to the trouble of creating a stack trace to
 * find the caller's class name. This is expensive, so it would be best not to
 * enable debug if these methods are used in production.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Default
public class ServiceLocator implements IServiceLocator {
    private static Logger log = LoggerFactory.getLogger(ServiceLocator.class);

    protected static final ServiceLocator INSTANCE = new ServiceLocator();

    public static IServiceLocator instance() {
        return INSTANCE;
    }

    private ServiceLocator() {
    }

    /**
     * @deprecated Use class and/or qualifiers, not name
     */
    @Override
    @Deprecated
    public <T> BeanHolder<T> getDependent(String name, Class<T> clazz) {
        if (log.isDebugEnabled()) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            log.debug("{} is still using name in getDependent({}, {})", caller,
                    name, clazz);
        }
        return new BeanHolder<T>(BeanProvider.<T>getDependent(name));
    }

    @Override
    public <T> BeanHolder<T> getDependent(Class<T> clazz,
            Annotation... qualifiers) {
        return new BeanHolder<T>(BeanProvider.getDependent(clazz, qualifiers));
    }

    /**
     * @deprecated Use class and/or qualifiers, not name
     */
    @Override
    @Deprecated
    public <T> T getInstance(String name, Class<T> clazz) {
        if (log.isDebugEnabled()) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            log.debug("{} is still using name in getInstance({}, {})", caller,
                    name, clazz);
        }
        return BeanProvider.getContextualReference(name, false, clazz);
    }

    @Override
    public <T> T getInstance(Class<T> clazz, Annotation... qualifiers) {
        return BeanProvider.getContextualReference(clazz, qualifiers);
    }

    /**
     * @deprecated Use class and/or qualifiers, not name
     */
    @Override
    @Deprecated
    public <T> T getInstance(String name, Object scope, Class<T> clazz) {
        if (log.isDebugEnabled()) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            log.debug(
                    "{} is still using name in getInstance({}, {}, {}) - scope ignored",
                    caller, name, scope, clazz);
        }
        return BeanProvider.getContextualReference(name, false, clazz);
    }

    /**
     * @deprecated Use class and/or qualifiers, not name
     */
    @Deprecated
    public <T> Optional<T> getOptionalInstance(String name, Class<T> clazz) {
        if (log.isDebugEnabled()) {
            String caller = new Exception().getStackTrace()[1].getClassName();
            log.debug("{} is still using name in getOptionalInstance({}, {})",
                    caller, name, clazz);
        }
        return Optional.ofNullable(BeanProvider.getContextualReference(name, true, clazz));
    }

    public <T> Optional<T> getOptionalInstance(Class<T> clazz, Annotation... qualifiers) {
        return Optional.ofNullable(BeanProvider.getContextualReference(clazz, true, qualifiers));
    }

    @Override
    public EntityManager getEntityManager() {
        return getInstance(EntityManager.class);
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory() {
        return getInstance(EntityManagerFactory.class);
    }

    @Override
    public <T> T getJndiComponent(String jndiName, Class<T> clazz)
            throws NamingException {
        Context ctx = new InitialContext();
        return (T) ctx.lookup(jndiName);
    }

}
