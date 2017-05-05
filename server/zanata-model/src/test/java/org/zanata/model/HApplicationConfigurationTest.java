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

import java.util.Date;

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
        assertThat(HApplicationConfiguration.getAvailableKeys())
                .contains(HApplicationConfiguration.KEY_HOST,
                        HApplicationConfiguration.KEY_EMAIL_LOG_EVENTS);
    }

    @Test
    public void testEqualsHashcode() {
        Date now = new Date();
        HApplicationConfiguration hac =
                new HApplicationConfiguration("host.url", "test");
        hac.setCreationDate(now);
        hac.setLastChanged(now);

        HApplicationConfiguration other = new HApplicationConfiguration("host.url", "test");
        other.setCreationDate(now);
        other.setLastChanged(now);
        assertThat(hac.equals(other)).isTrue();

        other = new HApplicationConfiguration("host.url", "test");
        other.setCreationDate(now);
        other.setLastChanged(now);
        assertThat(hac.hashCode()).isEqualTo(other.hashCode());

        other = new HApplicationConfiguration("register.url", "test");
        other.setCreationDate(now);
        other.setLastChanged(now);
        assertThat(hac.equals(other)).isFalse();

        other = new HApplicationConfiguration("register.url", "test");
        other.setCreationDate(now);
        other.setLastChanged(now);
        assertThat(hac.hashCode()).isNotEqualTo(other.hashCode());
    }
}
