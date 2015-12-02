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

import javax.security.auth.login.LoginException;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.StringUtils;
import org.jboss.seam.annotations.Create;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.jboss.seam.faces.Redirect;
import org.zanata.dao.PersonDAO;
import org.zanata.exception.KeyNotFoundException;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;
import org.zanata.model.HPersonEmailValidationKey;
import org.zanata.security.ZanataIdentity;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.ZanataSecured;
import org.zanata.service.impl.EmailChangeService;
import org.zanata.ui.faces.FacesMessages;

import static javax.faces.application.FacesMessage.SEVERITY_ERROR;

@Name("validateEmail")
@Slf4j
@ZanataSecured
public class ValidateEmailAction implements Serializable {
    private static final long serialVersionUID = 1L;

    private static int LINK_ACTIVE_DAYS = 1;

    @In
    private PersonDAO personDAO;

    @In
    private ZanataIdentity identity;

    @In
    private EmailChangeService emailChangeService;

    //TODO [CDI] change to urlUtil
    @In
    private Redirect redirect;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @Getter
    @Setter
    private String activationKey;

    @Create
    public void onCreate() {
        identity.checkLoggedIn();
    }

    @Transactional
    @CheckLoggedIn
    public void validate() throws LoginException {
        String returnUrl = "/home.xhtml";

        if (activationKey != null && !activationKey.isEmpty()) {
            HPersonEmailValidationKey entry =
                    emailChangeService.getActivationKey(activationKey);
            if (entry == null) {
                throw new KeyNotFoundException("activation key: "
                        + activationKey);
            }

            String checkResult = checkExpiryDate(entry.getCreationDate());

            if (StringUtils.isEmpty(checkResult)) {
                HPerson person = entry.getPerson();
                HAccount account = person.getAccount();
                if (!account.getUsername().equals(
                        identity.getCredentials().getUsername())) {
                    throw new LoginException();
                }

                person.setEmail(entry.getEmail());
                account.setEnabled(true);
                personDAO.makePersistent(person);
                personDAO.flush();
                emailChangeService.removeEntry(entry);
                facesMessages.addGlobal(
                        "You have successfully changed your email account.");
                log.info("update email address to {}  successfully",
                        entry.getEmail());
            } else {
                returnUrl = checkResult;
            }
        }
        redirect.setConversationPropagationEnabled(true);
        redirect.setViewId(returnUrl);
        redirect.execute();
    }

    private String checkExpiryDate(Date createdDate) {
        if (emailChangeService.isExpired(createdDate, LINK_ACTIVE_DAYS)) {
            log.info("Creation date expired:" + createdDate);
            facesMessages.addGlobal(SEVERITY_ERROR,
                    "Link expired. Please update your email again.");
            return "/profile/edit.xhtml";
        }
        return "";
    }
}
