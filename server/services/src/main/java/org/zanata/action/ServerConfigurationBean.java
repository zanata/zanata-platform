/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.action;

import java.beans.ConstructorProperties;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.commons.beanutils.BeanUtils;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.security.annotations.CheckRole;
import org.zanata.dao.ApplicationConfigurationDAO;
import org.zanata.events.HomeContentChangedEvent;
import org.zanata.model.HApplicationConfiguration;
import org.zanata.rest.service.ServerConfigurationService;
import org.zanata.ui.faces.FacesMessages;

import static org.zanata.model.HApplicationConfiguration.KEY_ALLOW_ANONYMOUS_USER;
import static org.zanata.model.HApplicationConfiguration.KEY_HOME_CONTENT;

@Named("serverConfigurationBean")
@ViewScoped
@Model
@Transactional
@CheckRole("admin")
public class ServerConfigurationBean implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ServerConfigurationBean.class);

    private static final long serialVersionUID = 1L;
    @Inject
    private FacesMessages facesMessages;
    public static final String DEFAULT_HELP_URL =
        "http://docs.zanata.org/en/release/";
    public static final String DEFAULT_TERM_OF_USE_URL =
        "http://zanata.org/terms";
    @Inject
    private ApplicationConfigurationDAO applicationConfigurationDAO;
    @SuppressFBWarnings("SE_BAD_FIELD")
    @Inject
    private Event<HomeContentChangedEvent> homeContentChangedEventEvent;
    private String homeContent = "";
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private PropertyWithDBKey<String> homeContentProperty =
            new PropertyWithDBKey<>("homeContent", KEY_HOME_CONTENT);
    private boolean allowAnonymousUser = true;
    @SuppressFBWarnings(value = "SE_BAD_FIELD")
    private PropertyWithDBKey<Boolean> allowAnonymousUserProperty =
            new PropertyWithDBKey<>("allowAnonymousUser",
                    KEY_ALLOW_ANONYMOUS_USER);

    public String updateHomeContent() {
        persistPropertyToDatabase(homeContentProperty);
        applicationConfigurationDAO.flush();
        facesMessages.addGlobal("Home content was successfully updated.");
        homeContentChangedEventEvent.fire(new HomeContentChangedEvent());
        return "/public/home.xhtml";
    }

    @PostConstruct
    public void onCreate() {
        setPropertyFromConfigIfNotNull(homeContentProperty);
        setBooleanPropertyFromConfigIfNotNull(allowAnonymousUserProperty);
    }

    private void
            setPropertyFromConfigIfNotNull(PropertyWithDBKey<String> property) {
        HApplicationConfiguration valueHolder =
                applicationConfigurationDAO.findByKey(property.getKey());
        if (valueHolder != null) {
            try {
                property.set(valueHolder.getValue());
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error("error setting property: {} -> {}",
                        property.getKey(), valueHolder.getValue(), e);
            }
        }
    }

    private void setBooleanPropertyFromConfigIfNotNull(
            PropertyWithDBKey<Boolean> property) {
        HApplicationConfiguration valueHolder =
                applicationConfigurationDAO.findByKey(property.getKey());
        if (valueHolder != null) {
            try {
                property.set(Boolean.parseBoolean(valueHolder.getValue()));
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error("error setting boolean property: {} -> {}",
                        property.getKey(), valueHolder.getValue(), e);
            }
        }
    }

    private void persistPropertyToDatabase(PropertyWithDBKey<String> property) {
        HApplicationConfiguration configItem =
                applicationConfigurationDAO.findByKey(property.getKey());
        try {
            ServerConfigurationService.persistApplicationConfig(
                    property.getKey(), configItem, property.getAsString(),
                    applicationConfigurationDAO);
        } catch (IllegalAccessException | NoSuchMethodException
                | InvocationTargetException e) {
            log.error("error persisting property: {} -> {}",
                    property.getKey(), configItem, e);
        }
    }

    /**
     * Associates a field of type T with a HApplicationConfiguration key,
     * allowing abstraction around setting fields only if keys are bound.
     */
    private final class PropertyWithDBKey<T> {
        private final String propertyName;
        private final String key;

        public void set(T value)
                throws InvocationTargetException, IllegalAccessException {
            BeanUtils.setProperty(ServerConfigurationBean.this, propertyName,
                    value);
        }

        public String getAsString() throws IllegalAccessException, NoSuchMethodException,
                InvocationTargetException {
            return BeanUtils.getProperty(ServerConfigurationBean.this,
                    propertyName);
        }

        @ConstructorProperties({ "propertyName", "key" })
        public PropertyWithDBKey(final String propertyName, final String key) {
            this.propertyName = propertyName;
            this.key = key;
        }

        public String getPropertyName() {
            return this.propertyName;
        }

        public String getKey() {
            return this.key;
        }

        @Override
        public boolean equals(final Object o) {
            if (o == this)
                return true;
            if (!(o instanceof PropertyWithDBKey))
                return false;
            final PropertyWithDBKey<?> other = (PropertyWithDBKey<?>) o;
            if (!other.canEqual((Object) this))
                return false;
            final Object this$propertyName = this.getPropertyName();
            final Object other$propertyName = other.getPropertyName();
            if (this$propertyName == null ? other$propertyName != null
                    : !this$propertyName.equals(other$propertyName))
                return false;
            final Object this$key = this.getKey();
            final Object other$key = other.getKey();
            if (this$key == null ? other$key != null
                    : !this$key.equals(other$key))
                return false;
            return true;
        }

        protected boolean canEqual(final Object other) {
            return other instanceof PropertyWithDBKey;
        }

        @Override
        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $propertyName = this.getPropertyName();
            result = result * PRIME
                    + ($propertyName == null ? 43 : $propertyName.hashCode());
            final Object $key = this.getKey();
            result = result * PRIME + ($key == null ? 43 : $key.hashCode());
            return result;
        }

        @Override
        public String toString() {
            return "ServerConfigurationBean.PropertyWithKey(propertyName="
                    + this.getPropertyName() + ", key=" + this.getKey() + ")";
        }
    }

    public String getHomeContent() {
        return this.homeContent;
    }

    public void setHomeContent(final String homeContent) {
        this.homeContent = homeContent;
    }

    public boolean isAllowAnonymousUser() {
        return this.allowAnonymousUser;
    }

    public void setAllowAnonymousUser(final boolean allowAnonymousUser) {
        this.allowAnonymousUser = allowAnonymousUser;
    }
}
