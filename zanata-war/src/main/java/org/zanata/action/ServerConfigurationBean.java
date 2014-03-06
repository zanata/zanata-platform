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

import javax.validation.constraints.Digits;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

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

@Name("serverConfigurationBean")
@Scope(ScopeType.PAGE)
@Restrict("#{s:hasRole('admin')}")
@Getter
@Setter
public class ServerConfigurationBean implements Serializable {
    private static final long serialVersionUID = 1L;

    @In
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private ApplicationConfigurationDAO applicationConfigurationDAO;

    @In
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private ApplicationConfiguration applicationConfiguration;

    @Url(canEndInSlash = true)
    private String registerUrl;

    @Url(canEndInSlash = false)
    private String serverUrl;

    private String emailDomain;

    @EmailList
    private String adminEmail;

    @Email
    private String fromEmailAddr;

    private String homeContent;

    private String helpContent;

    private boolean enableLogEmail;

    private String logDestinationEmails;

    private String logEmailLevel;

    @Url(canEndInSlash = true)
    private String piwikUrl;

    private String piwikIdSite;

    @Url(canEndInSlash = true)
    private String termsOfUseUrl;

    @Digits(integer = 10, fraction = 0)
    // TODO this won't allow empty string
    private String rateLimitPerSecond;

    @Digits(integer = 5, fraction = 0)
    private String maxConcurrentRequestsPerApiKey;

    @Digits(integer = 5, fraction = 0)
    private String maxActiveRequestsPerApiKey;

    public String getHomeContent() {
        HApplicationConfiguration var =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_HOME_CONTENT);
        return var != null ? var.getValue() : "";
    }

    public String getHelpContent() {
        HApplicationConfiguration var =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_HELP_CONTENT);
        return var != null ? var.getValue() : "";
    }

    public String updateHomeContent() {
        HApplicationConfiguration var =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_HOME_CONTENT);
        ServerConfigurationService.persistApplicationConfig(
                HApplicationConfiguration.KEY_HOME_CONTENT,
                var, homeContent, applicationConfigurationDAO);
        applicationConfigurationDAO.flush();

        FacesMessages.instance().add("Home content was successfully updated.");
        return "/home.xhtml";
    }

    public String updateHelpContent() {
        HApplicationConfiguration var =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_HELP_CONTENT);
        ServerConfigurationService.persistApplicationConfig(
                HApplicationConfiguration.KEY_HELP_CONTENT,
                var, helpContent, applicationConfigurationDAO);
        applicationConfigurationDAO.flush();

        FacesMessages.instance().add(
                "Help page content was successfully updated.");
        return "/help/view.xhtml";
    }

    // TODO tech debt: all below code should really be cleaned up
    @Create
    public void onCreate() {
        HApplicationConfiguration registerUrlValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_REGISTER);
        if (registerUrlValue != null) {
            this.registerUrl = registerUrlValue.getValue();
        }
        HApplicationConfiguration serverUrlValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_HOST);
        if (serverUrlValue != null) {
            this.serverUrl = serverUrlValue.getValue();
        }
        HApplicationConfiguration emailDomainValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_DOMAIN);
        if (emailDomainValue != null) {
            this.emailDomain = emailDomainValue.getValue();
        }
        HApplicationConfiguration adminEmailValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_ADMIN_EMAIL);
        if (adminEmailValue != null) {
            this.adminEmail = adminEmailValue.getValue();
        }

        this.fromEmailAddr = applicationConfiguration.getFromEmailAddr();

        HApplicationConfiguration emailLogEventsValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_EMAIL_LOG_EVENTS);
        if (emailLogEventsValue != null) {
            this.enableLogEmail =
                    Boolean.parseBoolean(emailLogEventsValue.getValue());
        }
        HApplicationConfiguration logDestinationValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_LOG_DESTINATION_EMAIL);
        if (logDestinationValue != null) {
            this.logDestinationEmails = logDestinationValue.getValue();
        }
        HApplicationConfiguration logEmailLevelValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_EMAIL_LOG_LEVEL);
        if (logEmailLevelValue != null) {
            this.logEmailLevel = logEmailLevelValue.getValue();
        }
        HApplicationConfiguration piwikUrlValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_PIWIK_URL);
        if (piwikUrlValue != null) {
            this.piwikUrl = piwikUrlValue.getValue();
        }
        HApplicationConfiguration piwikIdSiteValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_PIWIK_IDSITE);
        if (piwikIdSiteValue != null) {
            this.piwikIdSite = piwikIdSiteValue.getValue();
        }

        HApplicationConfiguration termsOfUseUrlValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_TERMS_CONDITIONS_URL);
        if (termsOfUseUrlValue != null) {
            this.termsOfUseUrl = termsOfUseUrlValue.getValue();
        }

        HApplicationConfiguration rateLimitValue = applicationConfigurationDAO
                .findByKey(HApplicationConfiguration.KEY_RATE_LIMIT_PER_SECOND);
        if (rateLimitValue != null) {
            this.rateLimitPerSecond = rateLimitValue.getValue();
        }

        HApplicationConfiguration maxConcurrent = applicationConfigurationDAO.findByKey(
                HApplicationConfiguration.KEY_MAX_CONCURRENT_REQ_PER_API_KEY);
        if (maxConcurrent != null) {
            this.maxConcurrentRequestsPerApiKey = maxConcurrent.getValue();
        }

        HApplicationConfiguration maxActive = applicationConfigurationDAO.findByKey(
                HApplicationConfiguration.KEY_MAX_ACTIVE_REQ_PER_API_KEY);
        if (maxActive != null) {
            this.maxActiveRequestsPerApiKey = maxActive.getValue();
        }
    }

    @Transactional
    public void update() {
        HApplicationConfiguration registerUrlValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_REGISTER);
        ServerConfigurationService.persistApplicationConfig(
                HApplicationConfiguration.KEY_REGISTER,
                registerUrlValue, registerUrl, applicationConfigurationDAO);

        HApplicationConfiguration serverUrlValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_HOST);
        ServerConfigurationService
                .persistApplicationConfig(HApplicationConfiguration.KEY_HOST,
                        serverUrlValue, serverUrl, applicationConfigurationDAO);

        HApplicationConfiguration emailDomainValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_DOMAIN);
        ServerConfigurationService
                .persistApplicationConfig(HApplicationConfiguration.KEY_DOMAIN,
                        emailDomainValue, emailDomain,
                        applicationConfigurationDAO);

        HApplicationConfiguration adminEmailValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_ADMIN_EMAIL);
        ServerConfigurationService.persistApplicationConfig(
                HApplicationConfiguration.KEY_ADMIN_EMAIL,
                adminEmailValue, adminEmail, applicationConfigurationDAO);

        HApplicationConfiguration fromEmailAddrValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_EMAIL_FROM_ADDRESS);
        ServerConfigurationService.persistApplicationConfig(
                HApplicationConfiguration.KEY_EMAIL_FROM_ADDRESS,
                fromEmailAddrValue, fromEmailAddr, applicationConfigurationDAO);

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

        HApplicationConfiguration logDestEmailValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_LOG_DESTINATION_EMAIL);
        ServerConfigurationService.persistApplicationConfig(
                HApplicationConfiguration.KEY_LOG_DESTINATION_EMAIL,
                logDestEmailValue, logDestinationEmails,
                applicationConfigurationDAO);

        HApplicationConfiguration logEmailLevelValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_EMAIL_LOG_LEVEL);
        ServerConfigurationService.persistApplicationConfig(
                HApplicationConfiguration.KEY_EMAIL_LOG_LEVEL,
                logEmailLevelValue, logEmailLevel, applicationConfigurationDAO);

        HApplicationConfiguration piwikUrlValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_PIWIK_URL);
        ServerConfigurationService.persistApplicationConfig(
                HApplicationConfiguration.KEY_PIWIK_URL,
                piwikUrlValue, piwikUrl, applicationConfigurationDAO);

        HApplicationConfiguration piwikIdSiteValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_PIWIK_IDSITE);
        ServerConfigurationService.persistApplicationConfig(
                HApplicationConfiguration.KEY_PIWIK_IDSITE,
                piwikIdSiteValue, piwikIdSite, applicationConfigurationDAO);

        HApplicationConfiguration termsOfUseUrlValue =
                applicationConfigurationDAO
                        .findByKey(
                                HApplicationConfiguration.KEY_TERMS_CONDITIONS_URL);
        ServerConfigurationService.persistApplicationConfig(
                HApplicationConfiguration.KEY_TERMS_CONDITIONS_URL,
                termsOfUseUrlValue, termsOfUseUrl, applicationConfigurationDAO);

        HApplicationConfiguration rateLimitValue =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_RATE_LIMIT_PER_SECOND);
        ServerConfigurationService.persistApplicationConfig(
                HApplicationConfiguration.KEY_RATE_LIMIT_PER_SECOND,
                rateLimitValue, rateLimitPerSecond, applicationConfigurationDAO);

        HApplicationConfiguration maxConcurrent =
                applicationConfigurationDAO
                        .findByKey(
                                HApplicationConfiguration.KEY_MAX_CONCURRENT_REQ_PER_API_KEY);
        ServerConfigurationService.persistApplicationConfig(
                HApplicationConfiguration.KEY_MAX_CONCURRENT_REQ_PER_API_KEY,
                maxConcurrent, maxConcurrentRequestsPerApiKey,
                applicationConfigurationDAO);

        HApplicationConfiguration maxActive =
                applicationConfigurationDAO
                        .findByKey(HApplicationConfiguration.KEY_MAX_ACTIVE_REQ_PER_API_KEY);
        ServerConfigurationService.persistApplicationConfig(
                HApplicationConfiguration.KEY_MAX_ACTIVE_REQ_PER_API_KEY,
                maxActive, maxActiveRequestsPerApiKey,
                applicationConfigurationDAO);

        applicationConfigurationDAO.flush();
        FacesMessages.instance().add("Configuration was successfully updated.");
    }

    public String cancel() {
        return "cancel";
    }
}
