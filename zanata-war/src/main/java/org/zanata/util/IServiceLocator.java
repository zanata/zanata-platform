/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
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

import java.lang.annotation.Annotation;

import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public interface IServiceLocator {
    @Deprecated
    <T> BeanHolder<T> getDependent(String name, Class<T> clazz);

    /**
     * Returns a BeanHolder for the relevant CDI bean, which must be
     * closed when no longer needed (eg end of the calling method).
     * This is necessary for correct lifecycle management of dependent
     * beans.  Destroying a BeanHolder for a non-dependent bean is
     * a no-op.
     * @param clazz
     * @param qualifiers
     * @param <T>
     * @return
     */
    <T> BeanHolder<T> getDependent(Class<T> clazz,
            Annotation... qualifiers);

    @Deprecated
    <T> T getInstance(String name, Class<T> clazz);

    <T> T getInstance(Class<T> clazz, Annotation... qualifiers);

    @Deprecated
    <T> T getInstance(String name, Object scope, Class<T> clazz);

    EntityManager getEntityManager();

    EntityManagerFactory getEntityManagerFactory();

    <T> T getJndiComponent(String jndiName, Class<T> clazz)
            throws NamingException;
}
