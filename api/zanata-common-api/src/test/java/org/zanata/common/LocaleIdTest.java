/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
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
package org.zanata.common;

import org.junit.Test;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class LocaleIdTest {

    private LocaleId localeId = new LocaleId("en-GB");

    @Test
    public void validLocaleId() {
        assertThat(localeId.isValid("en"), is(true));
        assertThat(localeId.isValid("en-US"), is(true));
        assertThat(localeId.isValid("en-US.UTF-8"), is(true));
        assertThat(localeId.isValid("en-US.UTF-8@Alpha"), is(true));
    }

    @Test
    public void invalidLocaleId() {
        assertThat(localeId.isValid(""), is(false));
        assertThat(localeId.isValid("@en"), is(false));
        assertThat(localeId.isValid("en_US"), is(false));
        assertThat(localeId.isValid("*en"), is(false));
    }

    @Test
    public void localeIdFromJavaName() {
        assertThat(LocaleId.fromJavaName("en_GB"), is(localeId));
    }

    @Test
    public void localeIdToJavaName() {
        assertThat(localeId.toJavaName(), is("en_GB"));
    }

    @Test
    public void localeIdToString() {
        assertThat(localeId.toString(), is("en-GB"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void handleInvalidLocaleId() {
        @SuppressWarnings("unused")
        LocaleId brokenLocaleId = new LocaleId("@en-GB");

    }

    @Test
    public void testEquals() {
        assertThat(localeId.equals(localeId), is(true));
        assertThat(localeId.equals(new LocaleId("en-GB")), is(true));
    }
}
