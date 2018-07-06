/*
 * Copyright 2018, Red Hat, Inc. and individual contributors as indicated by the
 *  @author tags. See the copyright.txt file in the distribution for a full
 *  listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */

package org.zanata.rest.admin;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.zanata.dao.ApplicationConfigurationDAO;
import org.zanata.model.HApplicationConfiguration;

import javax.ws.rs.core.Response;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;
import static org.zanata.model.HApplicationConfiguration.KEY_ADMIN_EMAIL;
import static org.zanata.model.HApplicationConfiguration.KEY_ALLOW_ANONYMOUS_USER;
import static org.zanata.model.HApplicationConfiguration.KEY_AUTO_ACCEPT_TRANSLATOR;
import static org.zanata.model.HApplicationConfiguration.KEY_DISPLAY_USER_EMAIL;
import static org.zanata.model.HApplicationConfiguration.KEY_DOMAIN;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ServerSettingsServiceTest {
    @Mock
    private ApplicationConfigurationDAO applicationConfigurationDAO;

    private ServerSettingsService service;

    @Before
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);
        when(applicationConfigurationDAO.findByKey(KEY_ADMIN_EMAIL))
                .thenReturn(new HApplicationConfiguration(KEY_ADMIN_EMAIL,
                        "admin@email.com"));

        when(applicationConfigurationDAO.findByKey(KEY_ALLOW_ANONYMOUS_USER))
                .thenReturn(
                        new HApplicationConfiguration(KEY_ALLOW_ANONYMOUS_USER,
                                Boolean.toString(true)));

        when(applicationConfigurationDAO.findByKey(KEY_AUTO_ACCEPT_TRANSLATOR))
                .thenReturn(new HApplicationConfiguration(
                        KEY_AUTO_ACCEPT_TRANSLATOR, Boolean.toString(true)));

        when(applicationConfigurationDAO.findByKey(KEY_DISPLAY_USER_EMAIL))
                .thenReturn(
                        new HApplicationConfiguration(KEY_DISPLAY_USER_EMAIL,
                                Boolean.toString(true)));

        when(applicationConfigurationDAO.findByKey(KEY_DOMAIN))
                .thenReturn(new HApplicationConfiguration(KEY_DOMAIN,
                        "redhat.com"));

        service = new ServerSettingsService(applicationConfigurationDAO);
    }

    @Test
    public void getSettingsTest() {
        Response response = service.getSettings();
        List<ServerSettingsService.PropertyWithDBKey> settings =
                (List<ServerSettingsService.PropertyWithDBKey>) response
                        .getEntity();

        assertThat(settings).hasSize(service.allProperties.size());

        Optional<ServerSettingsService.PropertyWithDBKey> adminEmail =
                getProperty(settings, KEY_ADMIN_EMAIL);
        assertThat(adminEmail.get().getValue()).isEqualTo("admin@email.com");

        Optional<ServerSettingsService.PropertyWithDBKey> allowAnonymous =
                getProperty(settings, KEY_ALLOW_ANONYMOUS_USER);
        assertThat(allowAnonymous.get().getValue()).isEqualTo(true);

        Optional<ServerSettingsService.PropertyWithDBKey> autoAccept =
                getProperty(settings, KEY_AUTO_ACCEPT_TRANSLATOR);
        assertThat(autoAccept.get().getValue()).isEqualTo(true);

        Optional<ServerSettingsService.PropertyWithDBKey> displayUserEmail =
                getProperty(settings, KEY_DISPLAY_USER_EMAIL);
        assertThat(displayUserEmail.get().getValue()).isEqualTo(true);

        Optional<ServerSettingsService.PropertyWithDBKey> domain =
                getProperty(settings, KEY_DOMAIN);
        assertThat(domain.get().getValue()).isEqualTo("redhat.com");
    }

    private Optional<ServerSettingsService.PropertyWithDBKey> getProperty(
            List<ServerSettingsService.PropertyWithDBKey> settings,
            String key) {
        return settings.stream().filter(setting -> setting.getKey().equals(key))
                .findFirst();
    }
}
