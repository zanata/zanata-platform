/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.action;

import java.io.Serializable;
import java.util.Date;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Model;
import javax.security.auth.login.LoginException;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.PersonDAO;
import org.zanata.exception.KeyNotFoundException;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.model.HPersonEmailValidationKey;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.service.impl.EmailChangeService;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.UrlUtil;
import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

@Named("validateEmail")
@RequestScoped
@Model
@Transactional
public class ValidateEmailAction implements Serializable {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ValidateEmailAction.class);

    private static final long serialVersionUID = 1L;
    private static int LINK_ACTIVE_DAYS = 1;
    @Inject
    private PersonDAO personDAO;
    @Inject
    private ZanataIdentity identity;
    @Inject
    private FacesMessages facesMessages;
    @Inject
    EmailChangeService emailChangeService;
    @Inject
    private UrlUtil urlUtil;
    private String activationKey;

    @PostConstruct
    public void onCreate() {
        identity.checkLoggedIn();
    }

    @Transactional
    @CheckLoggedIn
    public void validate() throws LoginException {
        if (activationKey != null && !activationKey.isEmpty()) {
            HPersonEmailValidationKey entry =
                    emailChangeService.getActivationKey(activationKey);
            if (entry == null) {
                throw new KeyNotFoundException(
                        "activation key: " + activationKey);
            }
            if (isExpiredDate(entry.getCreationDate())) {
                urlUtil.redirectToInternal(urlUtil.dashboardUrl());
            }
            HPerson person = entry.getPerson();
            HAccount account = person.getAccount();
            if (!account.getUsername()
                    .equals(identity.getCredentials().getUsername())) {
                throw new LoginException();
            }
            person.setEmail(entry.getEmail());
            account.setEnabled(true);
            personDAO.makePersistent(person);
            personDAO.flush();
            emailChangeService.removeEntry(entry);
            facesMessages.addGlobal(
                    "You have successfully changed your email account.");
            log.info("update email address to {} successfully",
                    entry.getEmail());
        }
        urlUtil.redirectToInternal(urlUtil.home());
    }

    private boolean isExpiredDate(Date createdDate) {
        if (emailChangeService.isExpired(createdDate, LINK_ACTIVE_DAYS)) {
            log.info("Creation date expired:" + createdDate);
            facesMessages.addGlobal(SEVERITY_ERROR,
                    "Link expired. Please update your email again.");
            return true;
        }
        return false;
    }

    public String getActivationKey() {
        return this.activationKey;
    }

    public void setActivationKey(final String activationKey) {
        this.activationKey = activationKey;
    }
}
