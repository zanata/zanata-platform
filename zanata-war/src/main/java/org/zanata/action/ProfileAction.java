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
import lombok.extern.slf4j.Slf4j;
import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.model.HPerson;
import org.zanata.service.impl.EmailChangeService;

/**
 * This action only handles edit profile (existing user).
 * For new users,
 * @see org.zanata.action.NewProfileAction
 *
 */
@Named("profileAction")
@javax.faces.bean.ViewScoped
@Slf4j
public class ProfileAction extends AbstractProfileAction implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    EmailChangeService emailChangeService;

    @PostConstruct
    public void onCreate() {
        username = identity.getCredentials().getUsername();
        HPerson person =
                personDAO.findById(
                        authenticatedAccount.getPerson().getId(), false);
        name = person.getName();
        email = person.getEmail();
        authenticatedAccount.getPerson().setName(this.name);
        authenticatedAccount.getPerson().setEmail(this.email);
    }
}
