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
package org.zanata.rest;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.Provider;

import org.codehaus.jackson.jaxrs.Annotations;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.jboss.resteasy.annotations.providers.NoJackson;
import org.jboss.resteasy.util.FindAnnotation;

/**
 * ResteasyJacksonProvider will use JAXB annotation as well as Jackson. This is
 * different from RESTEasy 2 which only use Jackson annotations. We need to
 * override this to make our REST api backward compatible.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Provider
@Consumes({ "application/*+json", "text/json" })
@Produces({ "application/*+json", "text/json" })
public class ZanataJacksonJsonProvider extends JacksonJsonProvider {
    public ZanataJacksonJsonProvider() {
        super(Annotations.JACKSON);
    }

    @Override
    public boolean isReadable(Class<?> aClass, Type type,
            Annotation[] annotations, MediaType mediaType) {
        if (FindAnnotation
                .findAnnotation(aClass, annotations, NoJackson.class) != null)
            return false;
        return super.isReadable(aClass, type, annotations, mediaType);
    }

    @Override
    public boolean isWriteable(Class<?> aClass, Type type,
            Annotation[] annotations, MediaType mediaType) {
        if (FindAnnotation
                .findAnnotation(aClass, annotations, NoJackson.class) != null)
            return false;
        return super.isWriteable(aClass, type, annotations, mediaType);
    }
}
