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

import org.apache.commons.lang.time.DateUtils;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.Begin;
import org.jboss.seam.annotations.End;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.security.RunAsOperation;
import org.jboss.seam.security.management.IdentityManager;
import org.zanata.dao.AccountActivationKeyDAO;
import org.zanata.exception.KeyNotFoundException;
import org.zanata.exception.ActivationLinkExpiredException;
import org.zanata.model.HAccountActivationKey;
import org.zanata.ui.faces.FacesMessages;

@Name("activate")
@Scope(ScopeType.CONVERSATION)
public class ActivateAction implements Serializable {

    private static final long serialVersionUID = -8079131168179421345L;

    @In("jsfMessages")
    private FacesMessages facesMessages;

    @In
    private AccountActivationKeyDAO accountActivationKeyDAO;

    @In
    private IdentityManager identityManager;

    private String activationKey;

    public String getActivationKey() {
        return activationKey;
    }

    private HAccountActivationKey key;

    private static int LINK_ACTIVE_DAYS = 1;

    @Begin(join = true)
    public void validateActivationKey() {

        if (getActivationKey() == null) {
            throw new KeyNotFoundException("null activation key");
        }

        key = accountActivationKeyDAO.findById(getActivationKey(), false);

        if (key == null) {
            throw new KeyNotFoundException("activation key: "
                    + getActivationKey());
        }

        if (isExpired(key.getCreationDate(), LINK_ACTIVE_DAYS)) {
            throw new ActivationLinkExpiredException("Activation link expired:"
                    + getActivationKey());
        }
    }

    private boolean isExpired(Date creationDate, int activeDays) {
        Date expiryDate = DateUtils.addDays(creationDate, activeDays);
        return expiryDate.before(new Date());
    }

    public void setActivationKey(String activationKey) {
        this.activationKey = activationKey;
    }

    @End
    public String activate() {

        new RunAsOperation() {
            public void execute() {
                identityManager.enableUser(key.getAccount().getUsername());
                identityManager.grantRole(key.getAccount().getUsername(),
                        "user");
            }
        }.addRole("admin").run();

        accountActivationKeyDAO.makeTransient(key);

        facesMessages
                .addGlobal(
                "Your account was successfully activated. You can now sign in.");

        return "/account/login.xhtml";
    }

}
