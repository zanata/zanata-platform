/*
 * Copyright 2017, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.model;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class HApplicationConfigurationTest {

    @Test
    public void testKeyValuePair() {
        HApplicationConfiguration hac =
                new HApplicationConfiguration("host.url", "test");
        assertThat(hac.getKey()).isEqualTo("host.url");
        assertThat(hac.getValue()).isEqualTo("test");
        hac.setKey("register.url");
        hac.setValue("other");
        assertThat(hac.getKey()).isEqualTo("register.url");
        assertThat(hac.getValue()).isEqualTo("other");
    }

    @Test
    public void testListKeys() {
        assertThat(HApplicationConfiguration.getAvailableKeys().size())
                .isEqualTo(21);
        assertThat(HApplicationConfiguration.getAvailableKeys())
                .contains("host.url", "log.email.level");
    }

    @Test
    public void testEqualsHashcode() {
        HApplicationConfiguration hac =
                new HApplicationConfiguration("host.url", "test");

        assertThat(hac.equals(
                new HApplicationConfiguration("host.url", "test")))
                .isTrue();
        assertThat(hac.hashCode()).isEqualTo(
                new HApplicationConfiguration("host.url", "test").hashCode());
        assertThat(hac.equals(
                new HApplicationConfiguration("register.url", "test")))
                .isFalse();
        assertThat(hac.hashCode()).isNotEqualTo(
                new HApplicationConfiguration("register.url", "test").hashCode());
    }
}
