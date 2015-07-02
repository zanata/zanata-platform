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
import java.lang.reflect.ParameterizedType;
import java.util.Set;
import javax.enterprise.inject.Instance;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;

import com.google.common.collect.Sets;

/**
 * Property Accessor that relies on a field.
 */
class FieldComponentAccessor extends ComponentAccessor {
    private Field field;

    FieldComponentAccessor(Field field) {
        this.field = field;
    }

    @Override
    public Object getValue(Object instance) {
        field.setAccessible(true);
        try {
            return field.get(instance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error accessing field "
                    + field.getName() + " on instance of type "
                    + instance.getClass().getName(), e);
        }
    }

    @Override
    public void setValue(Object instance, final Object value) {
        field.setAccessible(true);
        try {
            if (field.getType().equals(Instance.class)) {
                field.set(instance, new AutowireInstance(value));
            } else {
                field.set(instance, value);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Error accessing field "
                    + field.getName() + " on instance of type "
                    + instance.getClass().getName(), e);
        }
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotation) {
        return field.getAnnotation(annotation);
    }

    @Override
    public String getComponentName() {
        return field.getName();
    }

    @Override
    public Class<?> getComponentType() {

        Class<?> type = field.getType();
        if (type.equals(Instance.class)) {
            ParameterizedType genericType =
                    (ParameterizedType) field.getGenericType();
            // should only have one generic argument
            return (Class<?>) genericType.getActualTypeArguments()[0];

        }
        return type;
    }

    @Override
    public Set<Annotation> getQualifiers() {
        Set<Annotation> annotations =
                Sets.newHashSet(field.getAnnotations());
        annotations.remove(new AnnotationLiteral<Inject>() {});
        return annotations;
    }

}
