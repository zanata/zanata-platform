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
package org.zanata.webtrans.server.rpc;

import java.util.Map.Entry;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;

import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.seam.security.ZanataJpaIdentityStore;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.AccountOptionDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountOption;
import org.zanata.security.annotations.Authenticated;
import org.zanata.webtrans.server.ActionHandlerFor;
import org.zanata.webtrans.shared.model.UserOptions;
import org.zanata.webtrans.shared.rpc.SaveOptionsAction;
import org.zanata.webtrans.shared.rpc.SaveOptionsResult;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Named("webtrans.gwt.SaveOptionsHandler")
@javax.enterprise.context.Dependent
@ActionHandlerFor(SaveOptionsAction.class)
public class SaveOptionsHandler extends
        AbstractActionHandler<SaveOptionsAction, SaveOptionsResult> {
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;

    @Inject
    private AccountDAO accountDAO;

    @Inject
    private AccountOptionDAO accountOptionDAO;

    @Override
    public SaveOptionsResult execute(SaveOptionsAction action,
            ExecutionContext context) throws ActionException {
        HAccount account =
                accountDAO.findById(authenticatedAccount.getId(), true);

        for (Entry<UserOptions, String> entry : action.getConfigurationMap()
                .entrySet()) {
            HAccountOption option = updateOrCreateOption(account, entry.getKey(),
                entry.getValue());
            accountOptionDAO.makePersistent(option);
        }

        accountOptionDAO.flush();

        SaveOptionsResult result = new SaveOptionsResult();
        result.setSuccess(true);

        return result;
    }

    @Override
    public void rollback(SaveOptionsAction action, SaveOptionsResult result,
            ExecutionContext context) throws ActionException {
    }

    private HAccountOption updateOrCreateOption(HAccount account, UserOptions name,
            String newVal) {
        HAccountOption option =
                account.getEditorOptions().get(name.getPersistentName());

        if (option == null) {
            option = new HAccountOption(name.getPersistentName(), newVal);
            option.setAccount(account);
            account.getEditorOptions().put(name.getPersistentName(), option);
        } else {
            option.setValue(newVal);
        }
        return option;
    }
}
