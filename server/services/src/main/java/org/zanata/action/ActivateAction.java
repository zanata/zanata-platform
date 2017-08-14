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
import java.util.Date;
import javax.enterprise.inject.Model;
import javax.faces.application.FacesMessage;
import org.apache.commons.lang.time.DateUtils;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.core.api.scope.GroupedConversation;
import org.apache.deltaspike.core.api.scope.GroupedConversationScoped;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.AccountActivationKeyDAO;
import org.zanata.exception.KeyNotFoundException;
import org.zanata.exception.ActivationLinkExpiredException;
import org.zanata.model.HAccountActivationKey;
import org.zanata.seam.security.AbstractRunAsOperation;
import org.zanata.seam.security.IdentityManager;
import org.zanata.ui.faces.FacesMessages;
import org.zanata.util.UrlUtil;
import com.google.common.base.Strings;

@Named("activate")
@GroupedConversationScoped
@Model
@Transactional
public class ActivateAction implements Serializable {

    private static final long serialVersionUID = -8079131168179421345L;
    private static final int LINK_ACTIVE_DAYS = 1;
    @Inject
    private GroupedConversation conversation;
    @Inject
    private AccountActivationKeyDAO accountActivationKeyDAO;
    @Inject
    private IdentityManager identityManager;
    @Inject
    private UrlUtil urlUtil;
    @Inject
    private FacesMessages facesMessages;
    private String activationKey;
    private HAccountActivationKey key;
    private String resetPasswordKey;
    // @Begin(join = true)

    public void validateActivationKey() {
        if (getActivationKey() == null) {
            throw new KeyNotFoundException("null activation key");
        }
        key = accountActivationKeyDAO.findById(getActivationKey(), false);
        if (key == null) {
            throw new KeyNotFoundException(
                    "activation key: " + getActivationKey());
        }
        if (isExpired(key.getCreationDate(), LINK_ACTIVE_DAYS)) {
            throw new ActivationLinkExpiredException(
                    "Activation link expired:" + getActivationKey());
        }
    }

    private boolean isExpired(Date creationDate, int activeDays) {
        Date expiryDate = DateUtils.addDays(creationDate, activeDays);
        return expiryDate.before(new Date());
    }

    @Transactional
    public void activate() {
        new AbstractRunAsOperation() {

            public void execute() {
                identityManager.enableUser(key.getAccount().getUsername());
                identityManager.grantRole(key.getAccount().getUsername(),
                        "user");
            }
        }.addRole("admin").run();
        accountActivationKeyDAO.makeTransient(key);
        if (Strings.isNullOrEmpty(resetPasswordKey)) {
            facesMessages.addGlobal(FacesMessage.SEVERITY_INFO,
                    "Your account was successfully activated. You can now sign in.");
            urlUtil.redirectToInternal(urlUtil.signInPage());
        } else {
            urlUtil.redirectToInternal(
                    urlUtil.resetPasswordPage(resetPasswordKey));
        }
        conversation.close();
    }

    public void setResetPasswordKey(String resetPasswordKey) {
        this.resetPasswordKey = resetPasswordKey;
    }

    public String getResetPasswordKey() {
        return resetPasswordKey;
    }

    public String getActivationKey() {
        return this.activationKey;
    }

    public void setActivationKey(final String activationKey) {
        this.activationKey = activationKey;
    }
}
