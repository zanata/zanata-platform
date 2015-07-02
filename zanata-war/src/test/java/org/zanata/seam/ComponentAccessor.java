/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.seam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Represents a component accessor in a class.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
abstract class ComponentAccessor {
    public static final ComponentAccessor newInstance(Field f) {
        return new FieldComponentAccessor(f);
    }

    public static final ComponentAccessor newInstance(Method m) {
        return new MethodComponentAccessor(m);
    }

    public abstract Object getValue(Object instance);

    public abstract void setValue(Object instance, Object value);

    public abstract <T extends Annotation> T getAnnotation(Class<T> annotation);

    public abstract String getComponentName();

    public abstract Class<?> getComponentType();

    public abstract Set<Annotation> getQualifiers();
}
