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
package org.zanata.test;

import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.UseParametersRunnerFactory;

import javax.annotation.Nullable;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.AnnotatedField;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class ParamTestCdiExtension implements Extension {

    public ParamTestCdiExtension() {
    }

    // FIXME yes, the static field and method are a nasty hack
    private static @Nullable Object[] currentTestParameters;

    // TODO modify CdiRunner to expose Extension instance, perhaps use a
    // ThreadLocal to support multi-threaded testing
    public static void setCurrentTestParameters(@Nullable Object[] currentTestParameters) {
        ParamTestCdiExtension.currentTestParameters = currentTestParameters;
    }

    public <T> void initializeParameterLoading(final @Observes ProcessInjectionTarget<T> pit) {
        AnnotatedType<T> at = pit.getAnnotatedType();
        if (!at.isAnnotationPresent(UseParametersRunnerFactory.class)) {
            return;
        }
        UseParametersRunnerFactory factory = at.getAnnotation(UseParametersRunnerFactory.class);
        if (!factory.value().isAssignableFrom(CdiUnitRunnerWithParameters.Factory.class)) {
            return;
        }
        if (currentTestParameters == null) {
            throw new InjectionException("you must call setCurrentTestParameters before starting Weld");
        }
        Map<Field, Object> fieldValues = assignParamsToFields(at.getFields());

        InjectionTarget<T> it = pit.getInjectionTarget();
        InjectionTarget<T> wrapped = new InjectionTarget<T>() {
            @Override
            public void inject(T instance, CreationalContext<T> ctx) {
                it.inject(instance, ctx);
                for (Map.Entry<Field, Object> entry: fieldValues.entrySet()) {
                    try {
                        Field field = entry.getKey();
                        field.setAccessible(true);
                        field.set(instance, entry.getValue());
                    } catch (Exception e) {
                        pit.addDefinitionError(new InjectionException(e));
                    }
                }
            }

            @Override
            public void postConstruct(T instance) {
                it.postConstruct(instance);
            }

            @Override
            public void preDestroy(T instance) {
                it.preDestroy(instance);
            }

            @Override
            public void dispose(T instance) {
                it.dispose(instance);
            }

            @Override
            public Set<InjectionPoint> getInjectionPoints() {
                return it.getInjectionPoints();
            }

            @Override
            public T produce(CreationalContext<T> ctx) {
                return it.produce(ctx);
            }
        };
        pit.setInjectionTarget(wrapped);
    }

    private <T> Map<Field, Object> assignParamsToFields(Set<AnnotatedField<? super T>> fields) {
        Map<Field, Object> fieldValues = new HashMap<>();
        fields.stream()
                .filter(field -> field.isAnnotationPresent(Parameter.class))
                .forEach(field -> {
                    Parameter parameter = field.getAnnotation(Parameter.class);
                    Object value = currentTestParameters[parameter.value()];
                    Field memberField = field.getJavaMember();
                    fieldValues.put(memberField, value);
                });
        return fieldValues;
    }

}
