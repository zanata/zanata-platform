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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.validation.ConstraintValidatorContext;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 *
 * Slugs must start with a number or letter, and only contain letters,
 * numbers, periods, underscores and hyphens
 */
@RunWith(Parameterized.class)
public class SlugValidatorTest {

    @Parameterized.Parameter(0)
    public String slug;

    @Parameterized.Parameter(1)
    public boolean isAcceptable;

    private SlugValidator slugValidator = new SlugValidator();
    private ConstraintValidatorContext context = null;

    @Parameterized.Parameters(name = "{index}: slug({0}) is valid: {1}")
    public static Iterable<Object[]> slugs() {
        Object[][] slugs = {
                { "slug.test", true },
                { "slug_test", true },
                { "slug-test", true },
                { "slug001", true },
                { "001slug", true },
                { "1slug1", true },
                { "s0000g", true },
                { "s.....0", true },
                { "s.l.u.g", true },
                { "s-l-u-g", true },
                { "s_l_u_g", true },
                { "slug|test", false },
                { "slug/test", false },
                { "slug\\test", false },
                { "slug+test", false },
                { "slug*test", false },
                { "slug(test", false },
                { "slug)test", false },
                { "slug$test", false },
                { "slug[test", false },
                { "slug]test", false },
                { "slug:test", false },
                { "slug;test", false },
                { "slug'test", false },
                { "slug,test", false },
                { "slug?test", false },
                { "slug!test", false },
                { "slug@test", false },
                { "slug#test", false },
                { "slug%test", false },
                { "slug^test", false },
                { "slug=test", false },
                { "-slugtest", false },
                { "slugtest-", false }
                /*
                RHBZ1170009 - braces are accepted
                { "slug{test", false },
                { "slug}test", false }
                */
        };
        return Arrays.asList(slugs);
    }

    @Test
    public void validateSlug() throws Exception {
        assertThat(slugValidator.isValid(slug, context))
                .isEqualTo(isAcceptable)
                .as("Slug is validated correctly");
    }
}
