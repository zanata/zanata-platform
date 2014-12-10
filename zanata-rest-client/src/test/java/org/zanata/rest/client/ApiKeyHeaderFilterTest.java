/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.MultivaluedHashMap;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.rest.RestConstant;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class ApiKeyHeaderFilterTest {
    @Mock
    private ClientRequest mockRequest;
    @Mock
    private ClientResponse response;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testHeaders() throws Exception {
        String username = "username";
        String apiKey = "apiKey";
        String ver = "ver";
        ApiKeyHeaderFilter filter =
                new ApiKeyHeaderFilter(username, apiKey, ver) {
                    @Override
                    protected ClientResponse handleNext(
                            ClientRequest cr) {
                        return response;
                    }
                };

        MultivaluedHashMap<String, Object> headerMap =
                new MultivaluedHashMap<>();
        when(mockRequest.getHeaders()).thenReturn(headerMap);
        //

        filter.handle(mockRequest);

        assertThat(headerMap.getFirst(RestConstant.HEADER_USERNAME).toString(),
                Matchers.equalTo(username));
        assertThat(headerMap.getFirst(RestConstant.HEADER_API_KEY).toString(),
                Matchers.equalTo(apiKey));
        assertThat(headerMap.getFirst(RestConstant.HEADER_VERSION_NO)
                .toString(), Matchers.equalTo(ver));
    }
}

