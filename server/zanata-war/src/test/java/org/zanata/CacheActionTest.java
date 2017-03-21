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
package org.zanata;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jglue.cdiunit.InRequestScope;
import org.jglue.cdiunit.InSessionScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.zanata.action.CacheAction;
import org.zanata.i18n.Messages;
import org.zanata.test.CdiUnitRunner;
import org.zanata.util.Zanata;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@InRequestScope
@InSessionScope
@RunWith(CdiUnitRunner.class)
public class CacheActionTest {

    @Produces
    @Zanata
    @Mock
    private EmbeddedCacheManager cacheManager;

    @Produces
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Messages msgs;

    @Inject
    private CacheAction cacheAction;

    @Test
    public void validateCacheDisplayName() {
        Mockito.when(msgs.get("jsf.cacheStats.emptyString")).thenReturn("empty string");
        assertThat(cacheAction.getDisplayName("")).isEqualTo("<em>(empty string)</em>");
        assertThat(cacheAction.getDisplayName("bread & butter")).isEqualTo("bread &amp; butter");
    }
}
