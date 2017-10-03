/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

import org.apache.commons.lang3.StringUtils;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Model;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import javax.servlet.http.HttpServletRequest;

import org.zanata.ApplicationConfiguration;
import org.zanata.util.UrlUtil;

/**
 * This class serves as a UI proxy for the application configuration bean.
 * It offers methods that help figuring out how to present certain options
 * depending on the application configuration.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("applicationConfigurationAction")
@Dependent
@Model
@Transactional
public class ApplicationConfigurationAction {
    @Inject
    private ApplicationConfiguration applicationConfiguration;

    @Inject
    private UrlUtil urlUtil;

    public boolean isLoginHandledByInternalPage() {
        return applicationConfiguration.isInternalAuth()
                || applicationConfiguration.isJaasAuth()
                || (applicationConfiguration.isOpenIdAuth() && !applicationConfiguration
                        .isSingleOpenIdProvider());
    }

    public boolean isRegistrationLinkNeeded() {
        return !applicationConfiguration.isInternalAuth()
                && applicationConfiguration.getRegisterPath() != null
                && !applicationConfiguration.getRegisterPath().isEmpty();
    }

    public boolean isSingleOpenId() {
        return applicationConfiguration.isOpenIdAuth()
                && applicationConfiguration.isSingleOpenIdProvider();
    }

    public String getHelpUrl() {
        if(!StringUtils.isEmpty(applicationConfiguration.getHelpUrl())) {
            return applicationConfiguration.getHelpUrl();
        }
        return ServerConfigurationBean.DEFAULT_HELP_URL;
    }

    public String getTermOfUseUrl() {
        if(!StringUtils.isEmpty(applicationConfiguration.getTermsOfUseUrl())) {
            return applicationConfiguration.getTermsOfUseUrl();
        }
        return ServerConfigurationBean.DEFAULT_TERM_OF_USE_URL;
    }

    public String getLoginUrl(HttpServletRequest request) {
        if(applicationConfiguration.isKerberosAuth()) {
            return "/account/klogin?continue=" + urlUtil.getEncodedLocalUrl(request);
        }
        if(isSingleOpenId()) {
            return "/account/singleopenidlogin";
        }
        return "/account/sign_in?continue=" + urlUtil.getEncodedLocalUrl(request);
    }

    public String getRegisterUrl() {
        if(isRegistrationLinkNeeded()) {
            return applicationConfiguration.getRegisterPath();
        }
        return "/account/register";
    }
}
