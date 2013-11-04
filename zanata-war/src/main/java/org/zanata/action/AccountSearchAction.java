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
import java.util.ArrayList;
import java.util.List;

import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.zanata.dao.AccountDAO;
import org.zanata.dao.PersonDAO;
import org.zanata.model.HAccount;
import org.zanata.model.HPerson;

@Name("accountSearch")
@Scope(ScopeType.PAGE)
@AutoCreate
public class AccountSearchAction implements Serializable {
    private static final long serialVersionUID = 1L;
    private String maintainer;
    private HPerson person;
    private List<HAccount> searchResults = new ArrayList<HAccount>();

    @In
    private AccountDAO accountDAO;
    @In
    private PersonDAO personDAO;

    public void setMaintainer(String sr) {
        this.maintainer = sr;
    }

    public String getMaintainer() {
        return this.maintainer;
    }

    public List<HAccount> getSearchResults() {
        return this.searchResults;
    }

    public List<HAccount> search(String input) {
        this.person = null;
        return accountDAO.searchQuery(input);
    }

    public void update() {
        person = personDAO.findByUsername(this.maintainer);
    }

    public HPerson getPerson() {
        return this.person;
    }
}
