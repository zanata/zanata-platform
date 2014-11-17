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

package org.zanata.rest.client;

import java.lang.reflect.Method;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import com.google.common.base.Throwables;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class ClientUtil {


    static <T> Method getMethod(Class<T> resourceClass,
            String methodName, Class... paramTypes) {
        try {
            return resourceClass.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException e) {
            throw Throwables.propagate(e);
        }
    }

    static Class<?>[] arrayOf(int number, Class<?> type) {
        Class<?>[] result = new Class<?>[number];
        for (int i = 0; i < number; i++) {
           result[i] = type;
        }
        return result;
    }

    static MultivaluedMap<String, String> asMultivaluedMap(
            String paramKey, Iterable<String> values) {
        MultivaluedMapImpl map = new MultivaluedMapImpl();
        if (values == null) {
            return map;
        }
        for (String extension : values) {
            map.add(paramKey, extension);
        }
        return map;
    }
}
