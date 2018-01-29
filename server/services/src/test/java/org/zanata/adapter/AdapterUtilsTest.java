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
package org.zanata.adapter;

import org.junit.Assert;
import org.junit.Test;
import org.zanata.exception.FileFormatAdapterException;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class AdapterUtilsTest {

    @Test
    public void testNull() {
        try {
            AdapterUtils.readStream(URI.create(""));
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException iae) {
            assertThat(iae.getMessage()).contains("Could not open the URI. " +
                    "The URI must be absolute: URL is null");
        }
    }

    @Test
    public void testMalformed() {
        try {
            AdapterUtils.readStream(URI
                    .create("http://2aff:eaff:eaff:7aff:8aff:5aff:faff:eaff:8080/foo"));
            Assert.fail("Expected FileFormatAdapterException");
        } catch (FileFormatAdapterException ffae) {
            assertThat(ffae.getMessage()).contains("Could not open the URI. " +
                    "The URI may be malformed: URL is null");
        }
    }

    @Test
    public void testAccess() {
        try {
            AdapterUtils.readStream(URI.create("http://tmp"));
            Assert.fail("Expected FileFormatAdapterException");
        } catch (FileFormatAdapterException ffae) {
            assertThat(ffae.getMessage()).contains("Could not open the URL. " +
                    "The URL is OK but the input stream could not be opened.\ntmp");
        }
    }
}
