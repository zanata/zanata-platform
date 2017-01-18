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

package org.zanata.servlet;

import org.zanata.servlet.annotations.HttpParam;

import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.context.FacesContext;
import javax.servlet.ServletRequest;

/**
 * Producer for @HttpParam. Implementation taken from an example in the Weld manual.
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
class HttpParams {

    @Produces
    @HttpParam("")
    String getParamValue(InjectionPoint ip) {
        ServletRequest request = (ServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();
        String name = ip.getAnnotated().getAnnotation(HttpParam.class).value();
        return request.getParameter(name);
    }
}
