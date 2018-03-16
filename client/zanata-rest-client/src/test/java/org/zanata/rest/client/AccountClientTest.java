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

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.zanata.rest.dto.Account;
import org.zanata.rest.service.StubbingServerRule;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountClientTest {
    @ClassRule
    public static StubbingServerRule
            stubbingServerRule = new StubbingServerRule();
    private AccountClient client;

    @Before
    public void setUp() throws Exception {
        client = new AccountClient(MockServerTestUtil
                .createClientFactory(stubbingServerRule.getServerBaseUri()));

    }

    @Test
    public void testGet() throws Exception {
        Account account = client.get("admin");

        assertThat(account.getEmail()).isEqualTo("admin@zanata.org");
    }

    @Test
    public void testPut() throws Exception {
        Response response =
                client.put("admin", new Account("a@b.c", "d", "e", "f"));
        assertThat(response.getStatus()).isEqualTo(201)
                .as("server returns successful status code");
    }
}


