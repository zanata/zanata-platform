/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zanata.ui;

import java.beans.FeatureDescriptor;
import java.util.Iterator;
import java.util.Locale;

import javax.el.ELContext;
import javax.el.ELException;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.ValueReference;
import javax.el.VariableMapper;
import javax.faces.el.CompositeComponentExpressionHolder;

/**
 * N.B. This class is copied from seam's migration demo project at https://github.com/seam/migration.
 * <p>
 * Analyzes a {@link ValueExpression} and provides access to the base object and property name referenced by the expression.
 * </p>
 * <p/>
 * <p>
 * The getValueReference(ELContext) method returns a {@link ValueReference} object, which encapsulates the base object and
 * property name to which the expression maps. This process works by resolving the expression up until the last segment.
 * </p>
 * <p/>
 * <p>
 * Although access to the ValueReference was added in EL 2.2, the feature does not work correctly, which is why this custom
 * class is required.
 * </p>
 *
 * @author Dan Allen
 */
class ValueExpressionAnalyzer {
    private ValueExpression expression;

    public ValueExpressionAnalyzer(ValueExpression expression) {
        this.expression = expression;
    }

    public ValueReference getValueReference(ELContext elContext) {
        InterceptingResolver resolver = new InterceptingResolver(elContext.getELResolver());
        try {
            expression.setValue(decorateELContext(elContext, resolver), null);
        } catch (ELException ele) {
            return null;
        }
        ValueReference reference = resolver.getValueReference();
        if (reference != null) {
            Object base = reference.getBase();
            if (base instanceof CompositeComponentExpressionHolder) {
                ValueExpression ve = ((CompositeComponentExpressionHolder) base)
                        .getExpression((String) reference.getProperty());
                if (ve != null) {
                    this.expression = ve;
                    reference = getValueReference(elContext);
                }
            }
        }
        return reference;
    }

    private ELContext decorateELContext(final ELContext context, final ELResolver resolver) {
        return new ELContext() {
            // punch in our new ELResolver
            @Override
            public ELResolver getELResolver() {
                return resolver;
            }

            // The rest of the methods simply delegate to the existing context
            @Override
            public Object getContext(Class key) {
                return context.getContext(key);
            }

            @Override
            public Locale getLocale() {
                return context.getLocale();
            }

            @Override
            public boolean isPropertyResolved() {
                return context.isPropertyResolved();
            }

            @Override
            public void putContext(Class key, Object contextObject) {
                context.putContext(key, contextObject);
            }

            @Override
            public void setLocale(Locale locale) {
                context.setLocale(locale);
            }

            @Override
            public void setPropertyResolved(boolean resolved) {
                context.setPropertyResolved(resolved);
            }

            @Override
            public FunctionMapper getFunctionMapper() {
                return context.getFunctionMapper();
            }

            @Override
            public VariableMapper getVariableMapper() {
                return context.getVariableMapper();
            }
        };
    }

    private static class InterceptingResolver extends ELResolver {
        private ELResolver delegate;
        private ValueReference valueReference;

        public InterceptingResolver(ELResolver delegate) {
            this.delegate = delegate;
        }

        public ValueReference getValueReference() {
            return valueReference;
        }

        // Capture the base and property rather than write the value
        @Override
        public void setValue(ELContext context, Object base, Object property, Object value) {
            if (base != null && property != null) {
                context.setPropertyResolved(true);
                valueReference = new ValueReference(base, property.toString());
            }
        }

        // The rest of the methods simply delegate to the existing context
        @Override
        public Object getValue(ELContext context, Object base, Object property) {
            return delegate.getValue(context, base, property);
        }

        @Override
        public Class<?> getType(ELContext context, Object base, Object property) {
            return delegate.getType(context, base, property);
        }

        @Override
        public boolean isReadOnly(ELContext context, Object base, Object property) {
            return delegate.isReadOnly(context, base, property);
        }

        @Override
        public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
            return delegate.getFeatureDescriptors(context, base);
        }

        @Override
        public Class<?> getCommonPropertyType(ELContext context, Object base) {
            return delegate.getCommonPropertyType(context, base);
        }
    }
}
