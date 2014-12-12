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

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;

import javax.validation.constraints.Pattern;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.beanutils.BeanUtils;
import org.hibernate.validator.constraints.Email;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.annotations.security.Restrict;
import org.jboss.seam.faces.FacesMessages;
import org.zanata.ApplicationConfiguration;
import org.zanata.action.validator.EmailList;
import org.zanata.dao.ApplicationConfigurationDAO;
import org.zanata.model.HApplicationConfiguration;
import org.zanata.model.validator.Url;
import org.zanata.rest.service.ServerConfigurationService;

import static org.zanata.model.HApplicationConfiguration.*;

@Name("serverConfigurationBean")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasRole('admin')}")
@Slf4j
public class ServerConfigurationBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    private ApplicationConfigurationDAO applicationConfigurationDAO;

    @In
    private ApplicationConfiguration applicationConfiguration;

    @In
    private ServerConfigurationService serverConfigurationResource;

    @Url(canEndInSlash = true)
    @Getter
    @Setter
    private String registerUrl;

    @Url(canEndInSlash = false)
    @Getter
    @Setter
    private String serverUrl;

    @Getter
    @Setter
    private String emailDomain;

    @EmailList
    @Getter
    @Setter
    private String adminEmail;

    @Email
    @Getter
    @Setter
    private String fromEmailAddr;
    private PropertyWithKey<String> fromEmailAddrProperty = new PropertyWithKey<String>("fromEmailAddr", KEY_EMAIL_FROM_ADDRESS);

    @Getter
    @Setter
    private String homeContent = "";
    private PropertyWithKey<String> homeContentProperty = new PropertyWithKey<String>("homeContent", KEY_HOME_CONTENT);

    @Getter
    @Setter
    private String helpContent = "";
    private PropertyWithKey<String> helpContentProperty = new PropertyWithKey<String>("helpContent", KEY_HELP_CONTENT);

    @Getter
    @Setter
    private boolean enableLogEmail;
    private PropertyWithKey<Boolean> enableLogEmailProperty = new PropertyWithKey<Boolean>("enableLogEmail", KEY_EMAIL_LOG_EVENTS);

    @Getter
    @Setter
    private String logDestinationEmails;

    @Getter
    @Setter
    private String logEmailLevel;

    @Url(canEndInSlash = true)
    @Getter
    @Setter
    private String piwikUrl;

    @Getter
    @Setter
    private String piwikIdSite;

    @Url(canEndInSlash = true)
    @Getter
    @Setter
    private String termsOfUseUrl;

    @Pattern(regexp = "\\d{0,5}")
    @Getter
    @Setter
    private String maxConcurrentRequestsPerApiKey;

    @Pattern(regexp = "\\d{0,5}")
    @Getter
    @Setter
    private String maxActiveRequestsPerApiKey;

    @Pattern(regexp = "\\d{0,5}")
    @Getter
    @Setter
    private String maxFilesPerUpload;

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
            new PropertyWithKey<String>("maxConcurrentRequestsPerApiKey", KEY_MAX_CONCURRENT_REQ_PER_API_KEY),
            new PropertyWithKey<String>("maxActiveRequestsPerApiKey", KEY_MAX_ACTIVE_REQ_PER_API_KEY),
            new PropertyWithKey<String>("maxFilesPerUpload", KEY_MAX_FILES_PER_UPLOAD),
            homeContentProperty,
            helpContentProperty
    );

    public String updateHomeContent() {
        persistPropertyToDatabase(homeContentProperty);
        applicationConfigurationDAO.flush();

        FacesMessages.instance().add("Home content was successfully updated.");
        return "/home.xhtml";
    }

    public String updateHelpContent() {
        persistPropertyToDatabase(helpContentProperty);
        applicationConfigurationDAO.flush();

        FacesMessages.instance().add(
                "Help page content was successfully updated.");
        return "/help/view.xhtml";
    }

    @Create
    public void onCreate() {
        setPropertiesFromConfigIfNotNull(commonStringProperties);
        setBooleanPropertyFromConfigIfNotNull(enableLogEmailProperty);
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
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void setBooleanPropertyFromConfigIfNotNull(PropertyWithKey<Boolean> property) {
        HApplicationConfiguration valueHolder = applicationConfigurationDAO.findByKey(property.getKey());
        if (valueHolder != null) {
            try {
                property.set(Boolean.parseBoolean(valueHolder.getValue()));
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
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

        applicationConfigurationDAO.flush();
        FacesMessages facesMessages = FacesMessages.instance();
        facesMessages.clearGlobalMessages();
        facesMessages.add("Configuration was successfully updated.");
        return "success";
    }

    private void persistPropertiesToDatabase(List<PropertyWithKey<String>> properties) {
        for (PropertyWithKey<String> property : properties) {
            persistPropertyToDatabase(property);
        }
    }

    private void persistPropertyToDatabase(PropertyWithKey<String> property) {
        HApplicationConfiguration registerUrlValue =
                applicationConfigurationDAO
                        .findByKey(property.getKey());
        try {
            serverConfigurationResource.persistApplicationConfig(
                    property.getKey(),
                    registerUrlValue, property.get(), applicationConfigurationDAO);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public String cancel() {
        return "cancel";
    }

    /**
     * Associates a field of type T with a HApplicationConfiguration key,
     * allowing abstraction around setting fields only if keys are bound.
     */
    @Data
    private class PropertyWithKey<T> {
        private final String propertyName;
        private final String key;

        public void set(T value) throws InvocationTargetException, IllegalAccessException {
            BeanUtils.setProperty(ServerConfigurationBean.this, propertyName, value);
        }
        public T get() throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
            return (T) BeanUtils.getProperty(ServerConfigurationBean.this, propertyName);
        }
    }
}
