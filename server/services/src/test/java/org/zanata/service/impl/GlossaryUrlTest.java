/*
 * Copyright 2016, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.service.impl;

import org.apache.deltaspike.core.spi.scope.window.WindowContext;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.GlossaryDAO;
import org.zanata.service.LocaleService;
import org.zanata.servlet.annotations.ContextPath;
import org.zanata.servlet.annotations.ServerPath;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.UrlUtil;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Alex Eng<a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
public class GlossaryUrlTest extends ZanataTest {

    @Produces @ServerPath String serverPath = "/";
    @Produces @ContextPath String contextPath = "";
    @Produces @Named("dswidQuery") String dswidQuery = "";
    @Produces @Named("dswidParam") String dswidParam = "";

    @Produces @Mock
    WindowContext windowContext;

    @Produces @Mock
    GlossaryDAO glossaryDAO;
    @Produces @Mock
    LocaleService localeServiceImpl;

    @Inject @Any
    private GlossarySearchServiceImpl impl;

    @Test
    @InRequestScope
    public void systemGlossaryUrlTest() {
        String qualifiedName = "global/glossary";
        String filter = "string with white space";
        LocaleId localeId = LocaleId.FR;

        String expectedUrl =
            contextPath + "/glossary?filter=" +
                UrlUtil.encodeString(filter) + "&locale=fr";

        String url = impl.glossaryUrl(qualifiedName, filter, localeId);
        assertThat(url).isEqualTo(expectedUrl);
    }

    @Test
    @InRequestScope
    public void projectGlossaryUrlTest() {
        String qualifiedName = "project/project1";
        String filter = "string with white space";
        LocaleId localeId = LocaleId.FR;

        String expectedUrl =
            contextPath + "/glossary/project/project1?filter=" +
                UrlUtil.encodeString(filter) + "&locale=fr";

        String url = impl.glossaryUrl(qualifiedName, filter, localeId);
        assertThat(url).isEqualTo(expectedUrl);
    }
}
