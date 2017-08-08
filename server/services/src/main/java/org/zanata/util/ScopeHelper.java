/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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

import org.apache.deltaspike.cdise.api.ContextControl;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ContextNotActiveException;
import javax.enterprise.context.RequestScoped;
import java.lang.annotation.Annotation;

/**
 * This class holds utility/helper functions relating to CDI scopes.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class ScopeHelper {
    private static final Logger log = LoggerFactory.getLogger(ScopeHelper.class);

    /**
     * Ensure that Request scope is active.
     * See http://stackoverflow.com/a/8720148/14379 and also
     * https://issues.jboss.org/browse/JBEAP-2526?focusedCommentId=13144080
     * @param runnable code to execute with Request scope active
     * @throws Exception if runnable throws an exception
     */
    public static void withRequestScope(RunnableEx runnable) throws Exception {
        boolean active =
                isScopeActive(RequestScoped.class);
        if (active) {
            log.debug("RequestScope already active");
            runnable.run();
        } else {
            log.debug("RequestScope not already active");
            ContextControl ctxCtrl =
                    ServiceLocator.instance().getInstance(ContextControl.class);
            ctxCtrl.startContext(RequestScoped.class);
            try {
                runnable.run();
            } finally {
                ctxCtrl.stopContext(RequestScoped.class);
            }
        }
    }

    /**
     * Check whether a specified CDI scope has been activated.
     * @param scopeClass scope to be checked, eg RequestScoped.class
     * @return true only if scope is active
     */
    public static boolean isScopeActive(Class<? extends Annotation> scopeClass) {
        try {
            return BeanManagerProvider.getInstance().getBeanManager().getContext(
                    scopeClass).isActive();
        } catch (ContextNotActiveException e) {
            return false;
        }
    }
}
