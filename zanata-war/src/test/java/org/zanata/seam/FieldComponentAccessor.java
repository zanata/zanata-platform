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

import javax.inject.Inject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

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
    public void setValue(Object instance, Object value) {
        field.setAccessible(true);
        try {
            field.set(instance, value);
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
        String compName = null;
        In inAnnot = field.getAnnotation(In.class);

        if (inAnnot != null) {
            if (!inAnnot.value().trim().isEmpty()) {
                compName = inAnnot.value();
            }
        }

        // by default component name is the field name
        if (compName == null) {
            compName = field.getName();
        }

        return compName;
    }

    @Override
    public Class getComponentType() {
        return field.getType();
    }
}
