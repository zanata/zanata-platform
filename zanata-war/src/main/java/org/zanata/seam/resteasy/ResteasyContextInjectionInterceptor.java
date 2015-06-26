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

import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.PropertyInjector;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.intercept.AbstractInterceptor;
import org.jboss.seam.intercept.InvocationContext;

/**
 * Runs after Seam injection and provides JAX RS @Context handling, required for
 * field injection on the actual bean (not proxy) instance.
 *
 * @author Christian Bauer
 */
@Interceptor(stateless = true)
public class ResteasyContextInjectionInterceptor extends AbstractInterceptor {

    public static final String RE_HTTP_REQUEST_VAR =
            "org.jboss.resteasy.spi.HttpRequest";
    public static final String RE_HTTP_RESPONSE_VAR =
            "org.jboss.resteasy.spi.HttpResponse";

    private final PropertyInjector propertyInjector;

    public ResteasyContextInjectionInterceptor(
            PropertyInjector propertyInjector) {
        this.propertyInjector = propertyInjector;
    }

    public Object aroundInvoke(InvocationContext ic) throws Exception {
        HttpRequest request =
                (HttpRequest) Component.getInstance(RE_HTTP_REQUEST_VAR);
        HttpResponse response =
                (HttpResponse) Component.getInstance(RE_HTTP_RESPONSE_VAR);

        propertyInjector.inject(request, response, ic.getTarget());

        return ic.proceed();
    }

    public boolean isInterceptorEnabled() {
        return true;
    }
}
