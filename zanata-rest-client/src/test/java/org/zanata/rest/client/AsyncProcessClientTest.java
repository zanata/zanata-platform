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

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.rest.dto.ProcessStatus;
import org.zanata.rest.dto.resource.Resource;
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.MockServerRule;

import com.google.common.collect.Sets;

import static org.junit.Assert.*;

public class AsyncProcessClientTest {
    @ClassRule
    public static MockServerRule mockServerRule = new MockServerRule();
    private AsyncProcessClient client;

    @Before
    public void setUp() throws Exception {
        client = new AsyncProcessClient(MockServerTestUtil
                .createClientFactory(mockServerRule.getServerBaseUri()));
    }

    @Test
    public void testStartSourceDocCreationOrUpdate() throws Exception {
        ProcessStatus processStatus =
                client.startSourceDocCreationOrUpdate("message",
                        "about-fedora",
                        "master",
                        new Resource("message"), Sets.newHashSet("gettext"),
                        false);

        assertThat(processStatus.getStatusCode(), Matchers.equalTo(
                ProcessStatus.ProcessStatusCode.Running));
    }

    @Test
    public void testStartTranslatedDocCreationOrUpdate() throws Exception {
        ProcessStatus processStatus =
                client.startTranslatedDocCreationOrUpdate("message",
                        "about-fedora",
                        "master", LocaleId.DE,
                        new TranslationsResource(), Sets.newHashSet("gettext"),
                        "auto");

        assertThat(processStatus.getStatusCode(), Matchers.equalTo(
                ProcessStatus.ProcessStatusCode.Running));
    }

    @Test
    public void testGetProcessStatus() throws Exception {
        ProcessStatus processStatus = client.getProcessStatus("a");

        assertThat(processStatus.getStatusCode(), Matchers.equalTo(
                ProcessStatus.ProcessStatusCode.Finished));
    }
}

