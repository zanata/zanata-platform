/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.zanata.util;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.zanata.ZanataTest;
import org.zanata.servlet.annotations.ContextPath;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.test.CdiUnitRunner;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(CdiUnitRunner.class)
@InRequestScope
public class UrlUtilTest extends ZanataTest {

    @Produces
    @ServerPath
    String serverPath = "/";
    @Produces
    @ContextPath
    String contextPath = "";
    @Produces
    @Named("dswidQuery")
    String dswidQuery = "";
    @Produces
    @Named("dswidParam")
    String dswidParam = "";
    @Produces @Mock
    WindowContext windowContext;

    @Inject
    private UrlUtil urlUtil;

    @Test
    public void getLocalUrlNoAttributeTest() {
        String queryString = "queryStringforUrl";
        String uri = "/testingurl";

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getAttribute(anyString())).thenReturn(null);
        when(request.getQueryString()).thenReturn(queryString);
        when(request.getRequestURI()).thenReturn(uri);
        String localUrl = urlUtil.getLocalUrl(request);
        assertThat(localUrl).contains(queryString).contains(uri);
    }

    @Test
    public void getLocalUrlWithAttributeTest() {
        String queryString = "queryStringforUrl";
        String contextPath = "/contextPath";

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        when(request.getAttribute("javax.servlet.forward.request_uri")).thenReturn("true");
        when(request.getAttribute("javax.servlet.forward.context_path")).thenReturn(contextPath);
        when(request.getAttribute("javax.servlet.forward.servlet_path")).thenReturn("servletPath");
        when(request.getAttribute("javax.servlet.forward.query_string")).thenReturn(queryString);

        String localUrl = urlUtil.getLocalUrl(request);
        assertThat(localUrl).contains(queryString).contains(contextPath);
    }
}
