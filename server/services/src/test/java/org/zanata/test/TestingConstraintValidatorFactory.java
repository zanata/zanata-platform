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

import org.apache.deltaspike.beanvalidation.impl.CDIAwareConstraintValidatorFactory;
import org.apache.deltaspike.core.api.provider.BeanManagerProvider;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.Validation;

/**
 * The standard CDIAwareConstraintValidatorFactory configured in production
 * validation.xml requires DeltaSpike and CDI (and thus CDI-Unit). This
 * class makes use of CDI if it is active, otherwise falls back on the
 * default ConstraintValidatorFactory. See
 * https://issues.apache.org/jira/browse/DELTASPIKE-1197
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class TestingConstraintValidatorFactory implements ConstraintValidatorFactory {

    private final ConstraintValidatorFactory cdiFactory;
    private final ConstraintValidatorFactory defaultFactory;

    public TestingConstraintValidatorFactory() {
        cdiFactory = new CDIAwareConstraintValidatorFactory();
        defaultFactory = Validation.byDefaultProvider().configure()
                .getDefaultConstraintValidatorFactory();
    }

    private static boolean isCdiActive() {
        if (BeanManagerProvider.isActive()) {
            // workaround for https://issues.apache.org/jira/browse/DELTASPIKE-1198
            try {
                BeanManagerProvider.getInstance().getBeanManager().getBeans(ConstraintValidator.class);
                return true;
            } catch (IllegalStateException e) {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    public <T extends ConstraintValidator<?, ?>> T getInstance(
            Class<T> validatorClass) {
        return getDelegate().getInstance(validatorClass);
    }

    @Override
    public void releaseInstance(ConstraintValidator<?, ?> instance) {
        getDelegate().releaseInstance(instance);
    }

    private ConstraintValidatorFactory getDelegate() {
        if (isCdiActive()) {
            return cdiFactory;
        } else {
            return defaultFactory;
        }
    }
}
