// Implementation copied from Seam 2.3.1, commit f3077fe

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.zanata.seam.resteasy;

import org.jboss.seam.framework.EntityHome;
import org.jboss.seam.framework.HibernateEntityHome;
import org.jboss.seam.framework.Home;

/**
 * This class provides an unified interface for accessing EntityHome and
 * HibernateEntityHome instances.
 *
 * @author Jozef Hartinger
 * @see org.jboss.seam.framework.EntityHome
 * @see org.jboss.seam.framework.HibernateEntityHome
 */
public class EntityHomeWrapper<T> {
    private Home<?, T> home = null;
    private boolean hibernate;

    /**
     * EntityHome or HibernateEntityHome instance is expected.
     */
    public EntityHomeWrapper(Home<?, T> entityHome) {
        if (entityHome instanceof EntityHome) {
            hibernate = false;
        } else if (entityHome instanceof HibernateEntityHome) {
            hibernate = true;
        } else {
            throw new IllegalArgumentException(
                    "You must use either EntityHome or HibernateEntityHome instance.");
        }
        this.home = entityHome;
    }

    public Object getId() {
        return home.getId();
    }

    public void setId(Object id) {
        home.setId(id);
    }

    public T getInstance() {
        return home.getInstance();
    }

    public void setInstance(T instance) {
        home.setInstance(instance);
    }

    public T find() {
        if (hibernate) {
            return getHibernateEntityHome().find();
        } else {
            return getEntityHome().find();
        }
    }

    public void persist() {
        if (hibernate) {
            getHibernateEntityHome().persist();
        } else {
            getEntityHome().persist();
        }
    }

    public void remove() {
        if (hibernate) {
            getHibernateEntityHome().remove();
        } else {
            getEntityHome().remove();
        }
    }

    /**
     * Merge the state of the given entity with the current persistence context.
     */
    public void merge(T object) {
        if (hibernate) {
            getHibernateEntityHome().getSession().merge(object);
            getHibernateEntityHome().update();
        } else {
            getEntityHome().getEntityManager().merge(object);
            getEntityHome().update();
        }
    }

    public Class<T> getEntityClass() {
        return home.getEntityClass();
    }

    private EntityHome<T> getEntityHome() {
        return (EntityHome<T>) home;
    }

    private HibernateEntityHome<T> getHibernateEntityHome() {
        return (HibernateEntityHome<T>) home;
    }

    /**
     * Return the underlying EntityHome or HibernateEntityHome instance.
     */
    public Home<?, T> unwrap() {
        if (hibernate) {
            return getHibernateEntityHome();
        } else {
            return getEntityHome();
        }
    }

}
