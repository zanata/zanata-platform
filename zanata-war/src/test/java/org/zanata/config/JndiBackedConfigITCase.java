/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.config;

import java.util.Set;

import javax.inject.Inject;
import org.junit.Test;
import org.zanata.ArquillianTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;

/**
 * Integration test for the JNDI backed configuration store.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class JndiBackedConfigITCase extends ArquillianTest {
    @Inject
    private JndiBackedConfig jndiBackedConfig;

    @Override
    protected void prepareDBUnitOperations() {
    }

    @Test
    public void getAdminUsers() throws Exception {
        String result = jndiBackedConfig.getAdminUsersList();
        assertThat(result, equalTo("user1,user2,user3"));
    }

    @Test
    public void getEnabledAuthenticationPolicies() throws Exception {
        Set<String> results =
                jndiBackedConfig.getEnabledAuthenticationPolicies();
        assertThat(results.size(), is(1));
        assertThat(results.contains("internal"), is(true));
    }

    @Test
    public void reset() throws Exception {
        String original = jndiBackedConfig.getAdminUsersList();
        jndiBackedConfig.reset();
        String reloaded = jndiBackedConfig.getAdminUsersList();
        assertThat(reloaded, equalTo(original));
    }
}
