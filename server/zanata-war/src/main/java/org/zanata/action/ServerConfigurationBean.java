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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.hibernate.validator.constraints.Email;
import org.slf4j.Logger;
import org.zanata.ApplicationConfiguration;
import org.zanata.action.validator.DomainList;
import org.zanata.action.validator.EmailList;
import org.zanata.dao.ApplicationConfigurationDAO;
import org.zanata.events.HomeContentChangedEvent;
import org.zanata.model.HApplicationConfiguration;
import org.zanata.model.validator.Url;
import org.zanata.rest.service.ServerConfigurationService;
import org.zanata.security.annotations.CheckRole;
import org.zanata.ui.faces.FacesMessages;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Model;
import javax.faces.bean.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.Pattern;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import static org.zanata.model.HApplicationConfiguration.KEY_ADMIN_EMAIL;
import static org.zanata.model.HApplicationConfiguration.KEY_ALLOW_ANONYMOUS_USER;
import static org.zanata.model.HApplicationConfiguration.KEY_DISPLAY_USER_EMAIL;
import static org.zanata.model.HApplicationConfiguration.KEY_DOMAIN;
import static org.zanata.model.HApplicationConfiguration.KEY_EMAIL_FROM_ADDRESS;
import static org.zanata.model.HApplicationConfiguration.KEY_EMAIL_LOG_EVENTS;
import static org.zanata.model.HApplicationConfiguration.KEY_EMAIL_LOG_LEVEL;
import static org.zanata.model.HApplicationConfiguration.KEY_HELP_URL;
import static org.zanata.model.HApplicationConfiguration.KEY_HOME_CONTENT;
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

@Named("serverConfigurationBean")
@ViewScoped
@Model
@Transactional
@CheckRole("admin")
public class ServerConfigurationBean implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final Logger log =
            org.slf4j.LoggerFactory.getLogger(ServerConfigurationBean.class);

    @Inject
    private FacesMessages facesMessages;

    public static final String DEFAULT_HELP_URL = "http://zanata.org/help";

    public static final String DEFAULT_TERM_OF_USE_URL = "http://zanata.org/terms";

    @Inject
    private ApplicationConfigurationDAO applicationConfigurationDAO;

    @Inject
    private ApplicationConfiguration applicationConfiguration;

    @Inject
    private Event<HomeContentChangedEvent> homeContentChangedEventEvent;

    @Url(canEndInSlash = true)
    private String registerUrl;

    @Url(canEndInSlash = false)
    private String serverUrl;

    private String emailDomain;

    @EmailList
    private String adminEmail;

    @Email
    private String fromEmailAddr;
    private PropertyWithKey<String> fromEmailAddrProperty = new PropertyWithKey<String>("fromEmailAddr", KEY_EMAIL_FROM_ADDRESS);

    private String homeContent = "";
    private PropertyWithKey<String> homeContentProperty = new PropertyWithKey<String>("homeContent", KEY_HOME_CONTENT);

    private boolean enableLogEmail;
    private PropertyWithKey<Boolean> enableLogEmailProperty = new PropertyWithKey<Boolean>("enableLogEmail", KEY_EMAIL_LOG_EVENTS);

    private boolean displayUserEmail;
    private PropertyWithKey<Boolean> displayUserEmailProperty = new PropertyWithKey<Boolean>("displayUserEmail", KEY_DISPLAY_USER_EMAIL);

    private boolean allowAnonymousUser = true;
    private PropertyWithKey<Boolean> allowAnonymousUserProperty = new PropertyWithKey<Boolean>("allowAnonymousUser", KEY_ALLOW_ANONYMOUS_USER);

    @EmailList
    private String logDestinationEmails;

    private String logEmailLevel;

    @Url(canEndInSlash = true)
    private String piwikUrl;

    private String piwikIdSite;

    @Url(canEndInSlash = true)
    private String termsOfUseUrl;

    @Url(canEndInSlash = true)
    private String helpUrl;

    @Pattern(regexp = "\\d{0,5}",
            message = "value must be an integer number between 0 to 99999")
    private String maxConcurrentRequestsPerApiKey;

    @Pattern(regexp = "\\d{0,5}",
            message = "value must be an integer number between 0 to 99999")
    private String maxActiveRequestsPerApiKey;

    @Pattern(regexp = "\\d{0,5}",
            message = "value must be an integer number between 0 to 99999")
    private String maxFilesPerUpload;

    @DomainList
    private String permittedUserEmailDomains;

    private List<PropertyWithKey<String>> commonStringProperties = Arrays.asList(
            new PropertyWithKey<String>("registerUrl", KEY_REGISTER),
            new PropertyWithKey<String>("serverUrl", KEY_HOST),
            new PropertyWithKey<String>("emailDomain", KEY_DOMAIN),
            new PropertyWithKey<String>("adminEmail", KEY_ADMIN_EMAIL),
            new PropertyWithKey<String>("logDestinationEmails", KEY_LOG_DESTINATION_EMAIL),
            new PropertyWithKey<String>("logEmailLevel", KEY_EMAIL_LOG_LEVEL),
            new PropertyWithKey<String>("piwikUrl", KEY_PIWIK_URL),
            new PropertyWithKey<String>("piwikIdSite", KEY_PIWIK_IDSITE),
            new PropertyWithKey<String>("termsOfUseUrl", KEY_TERMS_CONDITIONS_URL),
            new PropertyWithKey<String>("helpUrl", KEY_HELP_URL),
            new PropertyWithKey<String>("maxConcurrentRequestsPerApiKey", KEY_MAX_CONCURRENT_REQ_PER_API_KEY),
            new PropertyWithKey<String>("maxActiveRequestsPerApiKey", KEY_MAX_ACTIVE_REQ_PER_API_KEY),
            new PropertyWithKey<String>("maxFilesPerUpload", KEY_MAX_FILES_PER_UPLOAD),
            new PropertyWithKey<String>("displayUserEmail", KEY_DISPLAY_USER_EMAIL),
            new PropertyWithKey<String>("permittedUserEmailDomains",
                    KEY_PERMITTED_USER_EMAIL_DOMAIN),
            homeContentProperty
    );

    public String updateHomeContent() {
        persistPropertyToDatabase(homeContentProperty);
        applicationConfigurationDAO.flush();

        facesMessages.addGlobal("Home content was successfully updated.");
        homeContentChangedEventEvent.fire(new HomeContentChangedEvent());
        return "/public/home.xhtml";
    }

    @PostConstruct
    public void onCreate() {
        setPropertiesFromConfigIfNotNull(commonStringProperties);
        setBooleanPropertyFromConfigIfNotNull(enableLogEmailProperty);
        setBooleanPropertyFromConfigIfNotNull(allowAnonymousUserProperty);
        this.fromEmailAddr = applicationConfiguration.getFromEmailAddr();
    }

    private void setPropertiesFromConfigIfNotNull(List<PropertyWithKey<String>> properties) {
        for (PropertyWithKey<String> property : properties) {
            setPropertyFromConfigIfNotNull(property);
        }
    }

    private void setPropertyFromConfigIfNotNull(PropertyWithKey<String> property) {
        HApplicationConfiguration valueHolder =
                applicationConfigurationDAO
                        .findByKey(property.getKey());
        if (valueHolder != null) {
            try {
                property.set(valueHolder.getValue());
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error("error setting property value:" + property.getKey() + " -> " + valueHolder.getValue(), e);
            }
        }
    }

    private void setBooleanPropertyFromConfigIfNotNull(PropertyWithKey<Boolean> property) {
        HApplicationConfiguration valueHolder = applicationConfigurationDAO.findByKey(property.getKey());
        if (valueHolder != null) {
            try {
                property.set(Boolean.parseBoolean(valueHolder.getValue()));
            } catch (InvocationTargetException | IllegalAccessException e) {
                log.error("error setting property value:" + property.getKey() + " -> " + valueHolder.getValue(), e);
            }
        }
    }

    @Transactional
    public String update() {
        persistPropertiesToDatabase(commonStringProperties);
        persistPropertyToDatabase(fromEmailAddrProperty);

        HApplicationConfiguration emailLogEventsValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_EMAIL_LOG_EVENTS);
        if (emailLogEventsValue == null) {
            emailLogEventsValue =
                    new HApplicationConfiguration(
                            HApplicationConfiguration.KEY_EMAIL_LOG_EVENTS,
                            Boolean.toString(enableLogEmail));
        } else {
            emailLogEventsValue.setValue(Boolean.toString(enableLogEmail));
        }
        applicationConfigurationDAO.makePersistent(emailLogEventsValue);
        HApplicationConfiguration allowAnonymousUserValue =
                applicationConfigurationDAO.findByKey(KEY_ALLOW_ANONYMOUS_USER);
        if (allowAnonymousUserValue == null) {
            allowAnonymousUserValue =
                    new HApplicationConfiguration(KEY_ALLOW_ANONYMOUS_USER,
                            Boolean.toString(allowAnonymousUser));
        } else {
            allowAnonymousUserValue.setValue(Boolean.toString(allowAnonymousUser));
        }
        applicationConfigurationDAO.makePersistent(allowAnonymousUserValue);

        applicationConfigurationDAO.flush();
        facesMessages.clear();
        facesMessages.addGlobal("Configuration was successfully updated.");
        return "success";
    }

    private void persistPropertiesToDatabase(List<PropertyWithKey<String>> properties) {
        for (PropertyWithKey<String> property : properties) {
            persistPropertyToDatabase(property);
        }
    }

    private void persistPropertyToDatabase(PropertyWithKey<String> property) {
        HApplicationConfiguration value = applicationConfigurationDAO
                        .findByKey(property.getKey());
        try {
            ServerConfigurationService.persistApplicationConfig(
                    property.getKey(), value, property.get(),
                    applicationConfigurationDAO);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            log.error("error persisting property value:" + property.getKey() + " -> " + value, e);
        }
    }

    public String getRegisterUrl() {
        return this.registerUrl;
    }

    public String getServerUrl() {
        return this.serverUrl;
    }

    public String getEmailDomain() {
        return this.emailDomain;
    }

    public String getAdminEmail() {
        return this.adminEmail;
    }

    public String getFromEmailAddr() {
        return this.fromEmailAddr;
    }

    public String getHomeContent() {
        return this.homeContent;
    }

    public boolean isEnableLogEmail() {
        return this.enableLogEmail;
    }

    public boolean isDisplayUserEmail() {
        return this.displayUserEmail;
    }

    public boolean isAllowAnonymousUser() {
        return this.allowAnonymousUser;
    }

    public String getLogDestinationEmails() {
        return this.logDestinationEmails;
    }

    public String getLogEmailLevel() {
        return this.logEmailLevel;
    }

    public String getPiwikUrl() {
        return this.piwikUrl;
    }

    public String getPiwikIdSite() {
        return this.piwikIdSite;
    }

    public String getTermsOfUseUrl() {
        return this.termsOfUseUrl;
    }

    public String getHelpUrl() {
        return this.helpUrl;
    }

    public String getMaxConcurrentRequestsPerApiKey() {
        return this.maxConcurrentRequestsPerApiKey;
    }

    public String getMaxActiveRequestsPerApiKey() {
        return this.maxActiveRequestsPerApiKey;
    }

    public String getMaxFilesPerUpload() {
        return this.maxFilesPerUpload;
    }

    public String getPermittedUserEmailDomains() {
        return this.permittedUserEmailDomains;
    }

    public void setRegisterUrl(String registerUrl) {
        this.registerUrl = registerUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void setEmailDomain(String emailDomain) {
        this.emailDomain = emailDomain;
    }

    public void setAdminEmail(String adminEmail) {
        this.adminEmail = adminEmail;
    }

    public void setFromEmailAddr(String fromEmailAddr) {
        this.fromEmailAddr = fromEmailAddr;
    }

    public void setHomeContent(String homeContent) {
        this.homeContent = homeContent;
    }

    public void setEnableLogEmail(boolean enableLogEmail) {
        this.enableLogEmail = enableLogEmail;
    }

    public void setDisplayUserEmail(boolean displayUserEmail) {
        this.displayUserEmail = displayUserEmail;
    }

    public void setAllowAnonymousUser(boolean allowAnonymousUser) {
        this.allowAnonymousUser = allowAnonymousUser;
    }

    public void setLogDestinationEmails(String logDestinationEmails) {
        this.logDestinationEmails = logDestinationEmails;
    }

    public void setLogEmailLevel(String logEmailLevel) {
        this.logEmailLevel = logEmailLevel;
    }

    public void setPiwikUrl(String piwikUrl) {
        this.piwikUrl = piwikUrl;
    }

    public void setPiwikIdSite(String piwikIdSite) {
        this.piwikIdSite = piwikIdSite;
    }

    public void setTermsOfUseUrl(String termsOfUseUrl) {
        this.termsOfUseUrl = termsOfUseUrl;
    }

    public void setHelpUrl(String helpUrl) {
        this.helpUrl = helpUrl;
    }

    public void setMaxConcurrentRequestsPerApiKey(
            String maxConcurrentRequestsPerApiKey) {
        this.maxConcurrentRequestsPerApiKey = maxConcurrentRequestsPerApiKey;
    }

    public void setMaxActiveRequestsPerApiKey(
            String maxActiveRequestsPerApiKey) {
        this.maxActiveRequestsPerApiKey = maxActiveRequestsPerApiKey;
    }

    public void setMaxFilesPerUpload(String maxFilesPerUpload) {
        this.maxFilesPerUpload = maxFilesPerUpload;
    }

    public void setPermittedUserEmailDomains(String permittedUserEmailDomains) {
        this.permittedUserEmailDomains = permittedUserEmailDomains;
    }

    /**
     * Associates a field of type T with a HApplicationConfiguration key,
     * allowing abstraction around setting fields only if keys are bound.
     */
    private class PropertyWithKey<T> {
        private final String propertyName;
        private final String key;

        @java.beans.ConstructorProperties({ "propertyName", "key" })
        public PropertyWithKey(String propertyName, String key) {
            this.propertyName = propertyName;
            this.key = key;
        }

        public void set(T value) throws InvocationTargetException, IllegalAccessException {
            BeanUtils.setProperty(ServerConfigurationBean.this, propertyName, value);
        }
        public T get() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
            return (T) BeanUtils.getProperty(ServerConfigurationBean.this, propertyName);
        }

        public String getPropertyName() {
            return this.propertyName;
        }

        public String getKey() {
            return this.key;
        }

        public boolean equals(Object o) {
            if (o == this) return true;
            if (!(o instanceof PropertyWithKey))
                return false;
            final PropertyWithKey other =
                    (PropertyWithKey) o;
            if (!other.canEqual((Object) this)) return false;
            final Object this$propertyName = this.getPropertyName();
            final Object other$propertyName = other.getPropertyName();
            if (this$propertyName == null ? other$propertyName != null :
                    !this$propertyName.equals(other$propertyName)) return false;
            final Object this$key = this.getKey();
            final Object other$key = other.getKey();
            if (this$key == null ? other$key != null :
                    !this$key.equals(other$key))
                return false;
            return true;
        }

        public int hashCode() {
            final int PRIME = 59;
            int result = 1;
            final Object $propertyName = this.getPropertyName();
            result = result * PRIME +
                    ($propertyName == null ? 43 : $propertyName.hashCode());
            final Object $key = this.getKey();
            result = result * PRIME + ($key == null ? 43 : $key.hashCode());
            return result;
        }

        protected boolean canEqual(Object other) {
            return other instanceof PropertyWithKey;
        }

        public String toString() {
            return "org.zanata.action.ServerConfigurationBean.PropertyWithKey(propertyName=" +
                    this.getPropertyName() + ", key=" + this.getKey() + ")";
        }
    }

    public String getDefaultTermOfUseUrl() {
        return DEFAULT_TERM_OF_USE_URL;
    }

    public String getDefaultHelpUrl() {
        return DEFAULT_HELP_URL;
    }
}
