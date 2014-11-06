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

package org.zanata.rest.client;

import static org.junit.Assert.assertThat;

import java.net.URISyntaxException;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.ResourceMeta;
import org.zanata.rest.service.MockServerRule;
import com.google.common.collect.Sets;

public class SourceDocResourceClientTest {
    @ClassRule
    public static MockServerRule mockServerRule = new MockServerRule();

    private SourceDocResourceClient client;

    @Before
    public void setUp() throws URISyntaxException {
        client =
                new SourceDocResourceClient(
                        MockServerTestUtil.createClientFactory(mockServerRule.getServerBaseUri()), "about-fedora",
                        "master");
    }

    @Test
    public void testGetResourceMeta() {
        List<ResourceMeta> resourceMeta = client.getResourceMeta(null);

        assertThat(resourceMeta, Matchers.hasSize(2));
    }

    @Test
    public void testGetResource() {
        Resource resource = client.getResource("test",
                Sets.newHashSet("gettext", "comment"));

        assertThat(resource.getName(), Matchers.equalTo("test"));
    }

    @Test
    public void testPutResource() {
        String result = client.putResource("test", new Resource("newName"),
                Sets.newHashSet("gettext"), true);
        assertThat(result, Matchers.equalTo("newName"));
    }

    @Test
    public void testDeleteResource() {
        String result = client.deleteResource("test");
        assertThat(result, Matchers.isEmptyOrNullString());
    }
}