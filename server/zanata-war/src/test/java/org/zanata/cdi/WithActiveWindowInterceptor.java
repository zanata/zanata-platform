/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.cdi;

import java.io.Serializable;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;

@Interceptor
@WithActiveWindow
public class WithActiveWindowInterceptor implements Serializable {
    private static final long serialVersionUID = 1L;

    public WithActiveWindowInterceptor() {
    }

    @Inject
    private WindowContext windowContext;

    @AroundInvoke
    public Object around(InvocationContext ctx) throws Exception {
        String windowId = getWindowId(ctx);
        windowContext.activateWindow(windowId);
        try {
            return ctx.proceed();
        } finally {
            windowContext.closeWindow(windowId);
        }
    }

    private String getWindowId(InvocationContext context) {
        WithActiveWindow methodAnnot =
                context.getMethod().getAnnotation(WithActiveWindow.class);
        if (methodAnnot != null) return methodAnnot.value();
        WithActiveWindow classAnnot =
                context.getMethod().getDeclaringClass().getAnnotation(WithActiveWindow.class);
        if (classAnnot != null) return classAnnot.value();
        return "1";
    }
}
