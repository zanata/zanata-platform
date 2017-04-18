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
package org.zanata.service.impl;


import org.jglue.cdiunit.InRequestScope;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ApplicationConfiguration;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.security.annotations.Authenticated;
import org.zanata.test.CdiUnitRunner;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@InRequestScope
@RunWith(CdiUnitRunner.class)
public class GravatarServiceImplTest {

    @Produces
    @Mock
    @Authenticated
    HAccount authenticatedAccount;

    @Produces
    @Mock
    private ApplicationConfiguration applicationConfiguration;

    @Inject
    private GravatarServiceImpl gravatarService;

    @Test
    public void getGravatarURLTest() {
        when(applicationConfiguration.getGravatarRating()).thenReturn("R");
        assertThat(gravatarService.getUserImageUrl(100, "aloy@redhat.com"))
            .isEqualTo("//www.gravatar.com/avatar/f6e0a5949fdb5df2c78551d1b6c769e6?d=mm&r=R&s=100");
    }

    @Test
    public void getGravatarURLNoEmailTest() {
        HPerson person = new HPerson();
        person.setEmail("aloy@redhat.com");
        when(authenticatedAccount.getPerson()).thenReturn(person);
        when(applicationConfiguration.getGravatarRating()).thenReturn("R");
        assertThat(gravatarService.getUserImageUrl(100))
                .isEqualTo("//www.gravatar.com/avatar/f6e0a5949fdb5df2c78551d1b6c769e6?d=mm&r=R&s=100");
    }
}
