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

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.ApplicationConfigurationDAO;
import org.zanata.model.HApplicationConfiguration;
import org.zanata.rest.service.ServerConfigurationService;
import org.zanata.security.annotations.CheckRole;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Arrays.asList;
import static org.zanata.action.ServerConfigurationBean.DEFAULT_HELP_URL;
import static org.zanata.action.ServerConfigurationBean.DEFAULT_TERM_OF_USE_URL;
import static org.zanata.model.HApplicationConfiguration.KEY_ADMIN_EMAIL;
import static org.zanata.model.HApplicationConfiguration.KEY_ALLOW_ANONYMOUS_USER;
import static org.zanata.model.HApplicationConfiguration.KEY_AUTO_ACCEPT_TRANSLATOR;
import static org.zanata.model.HApplicationConfiguration.KEY_DISPLAY_USER_EMAIL;
import static org.zanata.model.HApplicationConfiguration.KEY_DOMAIN;
import static org.zanata.model.HApplicationConfiguration.KEY_EMAIL_FROM_ADDRESS;
import static org.zanata.model.HApplicationConfiguration.KEY_EMAIL_LOG_EVENTS;
import static org.zanata.model.HApplicationConfiguration.KEY_EMAIL_LOG_LEVEL;
import static org.zanata.model.HApplicationConfiguration.KEY_GRAVATAR_RATING;
import static org.zanata.model.HApplicationConfiguration.KEY_HELP_URL;
import static org.zanata.model.HApplicationConfiguration.KEY_HOST;
import static org.zanata.model.HApplicationConfiguration.KEY_LOG_DESTINATION_EMAIL;
import static org.zanata.model.HApplicationConfiguration.KEY_MAX_ACTIVE_REQ_PER_API_KEY;
import static org.zanata.model.HApplicationConfiguration.KEY_MAX_CONCURRENT_REQ_PER_API_KEY;
import static org.zanata.model.HApplicationConfiguration.KEY_MAX_FILES_PER_UPLOAD;
import static org.zanata.model.HApplicationConfiguration.KEY_PERMITTED_USER_EMAIL_DOMAIN;
import static org.zanata.model.HApplicationConfiguration.KEY_PIWIK_IDSITE;
import static org.zanata.model.HApplicationConfiguration.KEY_PIWIK_URL;
import static org.zanata.model.HApplicationConfiguration.KEY_REGISTER;
import static org.zanata.model.HApplicationConfiguration.KEY_TERMS_CONDITIONS_URL;
import static org.zanata.model.HApplicationConfiguration.KEY_TM_FUZZY_BANDS;

@RequestScoped
@Path("/admin/server-settings")
@CheckRole("admin")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Transactional(readOnly = true)
public class ServerSettingsService {
    public static final int DEFAULT_MAX_FILE_UPLOAD = 100;
    public static final int DEFAULT_ACTIVE_REQUEST = 2;
    public static final int DEFAULT_CONCURRENT_REQUEST = 2;

    protected List<PropertyWithDBKey> allProperties = asList(
            // Please keep these sorted by DB key (to make it easy to
            // compare with the keys in HApplicationConfiguration):
            new PropertyWithDBKey<>(KEY_ADMIN_EMAIL, ""),
            new PropertyWithDBKey<>(KEY_ALLOW_ANONYMOUS_USER, false),
            new PropertyWithDBKey<>(KEY_AUTO_ACCEPT_TRANSLATOR, false),
            new PropertyWithDBKey<>(KEY_DISPLAY_USER_EMAIL, false),
            new PropertyWithDBKey<>(KEY_DOMAIN, ""),
            new PropertyWithDBKey<>(KEY_EMAIL_FROM_ADDRESS, ""),
            new PropertyWithDBKey<>(KEY_EMAIL_LOG_EVENTS, false),
            new PropertyWithDBKey<>(KEY_EMAIL_LOG_LEVEL, ""),
            new PropertyWithDBKey<>(KEY_GRAVATAR_RATING, ""),
            new PropertyWithDBKey<>(KEY_HELP_URL, DEFAULT_HELP_URL),
            new PropertyWithDBKey<>(KEY_HOST, ""),
            new PropertyWithDBKey<>(KEY_LOG_DESTINATION_EMAIL, ""),
            new PropertyWithDBKey<>(KEY_MAX_ACTIVE_REQ_PER_API_KEY, DEFAULT_ACTIVE_REQUEST),
            new PropertyWithDBKey<>(KEY_MAX_CONCURRENT_REQ_PER_API_KEY, DEFAULT_CONCURRENT_REQUEST),
            new PropertyWithDBKey<>(KEY_MAX_FILES_PER_UPLOAD, DEFAULT_MAX_FILE_UPLOAD),
            new PropertyWithDBKey<>(KEY_PERMITTED_USER_EMAIL_DOMAIN, ""),
            new PropertyWithDBKey<>(KEY_PIWIK_URL, ""),
            new PropertyWithDBKey<>(KEY_PIWIK_IDSITE, ""),
            new PropertyWithDBKey<>(KEY_REGISTER, ""),
            new PropertyWithDBKey<>(KEY_TERMS_CONDITIONS_URL, DEFAULT_TERM_OF_USE_URL),
            new PropertyWithDBKey<>(KEY_TM_FUZZY_BANDS, ""));

    private ApplicationConfigurationDAO applicationConfigurationDAO;

    @SuppressWarnings("unused")
    public ServerSettingsService() {
    }

    @Inject
    public ServerSettingsService(ApplicationConfigurationDAO applicationConfigurationDAO) {
        this.applicationConfigurationDAO = applicationConfigurationDAO;
        setPropertiesFromConfig();
    }

    @GET
    public Response getSettings() {
        return Response.ok(allProperties).build();
    }

    @POST
    @Transactional(readOnly = false)
    public Response saveSettings(Map<String, String> updatedSettings) {
        for (Map.Entry<String, String> prop : updatedSettings.entrySet()) {
            PropertyWithDBKey property = allProperties.stream()
                    .filter(p -> p.getKey().equals(prop.getKey()))
                    .findFirst().get();

            HApplicationConfiguration configItem =
                applicationConfigurationDAO.findByKey(property.getKey());

            String value = prop.getValue();
            if (property.getDefaultValue().getClass() == Boolean.class) {
                value = Boolean.toString(Boolean.parseBoolean(value));
            } else if (property.getDefaultValue().getClass() == Integer.class) {
                value = Integer.toString(Integer.parseInt((value)));
            }
            ServerConfigurationService.persistApplicationConfig(
                property.getKey(), configItem, value,
                applicationConfigurationDAO);
        }
        applicationConfigurationDAO.flush();
        setPropertiesFromConfig();
        return getSettings();
    }

    private void setPropertiesFromConfig() {
        for (PropertyWithDBKey property: allProperties) {
            setPropertyValue(property);
        }
    }

    @SuppressWarnings("rawtypes")
    private void setPropertyValue(PropertyWithDBKey property) {
        HApplicationConfiguration valueHolder =
                applicationConfigurationDAO.findByKey(property.getKey());

        if (valueHolder != null) {
            if (property.getDefaultValue().getClass() == Boolean.class) {
                property.setValue(Boolean.parseBoolean(valueHolder.getValue()));
            } else if (property.getDefaultValue().getClass() == Integer.class) {
                property.setValue(Integer.parseInt(valueHolder.getValue()));
            } else {
                property.setValue(valueHolder.getValue());
            }
        }
    }

    /**
     * associates a field of type t with a happlicationconfiguration key,
     * allowing abstraction around setting fields only if keys are bound.
     */
    protected final static class PropertyWithDBKey<T> {
        private final String key;
        private T value;
        private T defaultValue;

        public void setValue(T value) {
            this.value = value;
        }

        public PropertyWithDBKey(final String key,
            @NotNull final T defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }


        public String getKey() {
            return this.key;
        }

        @NotNull
        public T getDefaultValue() {
            return defaultValue;
        }

        public T getValue() {
            return this.value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PropertyWithDBKey<?> that = (PropertyWithDBKey<?>) o;
            return Objects.equals(key, that.key) &&
                Objects.equals(value, that.value) &&
                Objects.equals(defaultValue, that.defaultValue);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value, defaultValue);
        }
    }
}
