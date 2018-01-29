/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.model.validator;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 **/
public class ZanataEmailValidatorTest {
    private ZanataEmailValidator validator;
    @Mock
    private ConstraintValidatorContext context;

    @Before
    public void setUp() {
        validator = new ZanataEmailValidator();
    }

    @Test
    public void testEmail() {
        assertThat(validator.isValid(null, context)).isFalse();
        assertThat(validator.isValid(" ", context)).isFalse();
        assertThat(validator.isValid(".", context)).isFalse();
        assertThat(validator.isValid("test@zanata+", context)).isFalse();
        assertThat(validator.isValid("test@zanata.", context)).isFalse();
        assertThat(validator.isValid("test@zanata-", context)).isFalse();
        assertThat(validator.isValid("test", context)).isFalse();
        assertThat(validator.isValid("test@", context)).isFalse();
        assertThat(validator.isValid("test@0.0.0.0", context)).isFalse();
        assertThat(validator.isValid("@zanata", context)).isFalse();

        assertThat(validator.isValid("test@zanata", context)).isTrue();
        assertThat(validator.isValid("test@zanata.org", context)).isTrue();
    }
}
