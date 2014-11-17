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
import org.zanata.rest.dto.resource.TranslationsResource;
import org.zanata.rest.service.MockServerRule;

import com.google.common.collect.Sets;

import static org.junit.Assert.*;

public class TransDocResourceClientTest {
    @ClassRule
    public static MockServerRule mockServerRule = new MockServerRule();

    private TransDocResourceClient client;

    @Before
    public void setUp() {
        client = new TransDocResourceClient(
                MockServerTestUtil.createClientFactory(mockServerRule.getServerBaseUri()), "about-fedora",
                "master");
    }

    @Test
    public void testGetTranslations() {
        TranslationsResource translations =
                client.getTranslations("test", LocaleId.DE,
                        Sets.newHashSet("gettext", "comment"), true, "abc");

        assertThat(translations.getTextFlowTargets(), Matchers.hasSize(1));
    }

}