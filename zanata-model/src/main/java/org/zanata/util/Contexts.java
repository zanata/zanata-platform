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

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.apache.deltaspike.core.util.ContextUtils;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;

/**
 * Minimal CDI/DeltaSpike version of Seam's Contexts class, compatibility
 * with migrated Seam code.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Deprecated
public class Contexts {

    public static boolean isApplicationContextActive() {
        return BeanManagerProvider.isActive() && ContextUtils.isContextActive(ApplicationScoped.class);
    }

    public static boolean isSessionContextActive() {
        return BeanManagerProvider.isActive() && ContextUtils.isContextActive(SessionScoped.class);
    }

    public static boolean isRequestContextActive() {
        return BeanManagerProvider.isActive() && ContextUtils.isContextActive(RequestScoped.class);
    }

}
