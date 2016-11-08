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
package org.zanata.model.validator;

import org.junit.Before;
import org.junit.Test;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.Assertions.assertThat;

public class UrlValidatorTest {

    private UrlValidator validator;

    @Url(canEndInSlash = true)
    public String canEndInSlash;

    @Url(canEndInSlash = false)
    public String canNotEndInSlash;
    private Url canEndInSlashUrl;
    private Url canNotEndInSlashUrl;
    private ConstraintValidatorContext context = null;

    @Before
    public void setUp() throws Exception {
        validator = new UrlValidator();
        canEndInSlashUrl =
                UrlValidatorTest.class.getField("canEndInSlash")
                        .getAnnotation(Url.class);
        canNotEndInSlashUrl =
                UrlValidatorTest.class.getField("canNotEndInSlash")
                        .getAnnotation(Url.class);
    }

    @Test
    public void emptyOrNullUrlIsValid() {
        assertThat(validator.isValid(null, context)).isTrue();
        assertThat(validator.isValid("", context)).isTrue();
    }

    @Test
    public void testUrlWithCanEndInSlashSetting() throws Exception {
        validator.initialize(canEndInSlashUrl);

        assertThat(validator.isValid("http://localhost/", context)).isTrue();
        assertThat(validator.isValid("http://localhost", context)).isTrue();
    }

    @Test
    public void testUrlWithCanNotEndInSlashSetting() {
        validator.initialize(canNotEndInSlashUrl);

        assertThat(validator.isValid("http://localhost/", null)).isFalse();
        assertThat(validator.isValid("http://localhost", null)).isTrue();
    }

    @Test
    public void malformedUrlIsInvalid() {
        validator.initialize(canEndInSlashUrl);

        assertThat(validator.isValid("localhost/", null)).isFalse();
    }
}
