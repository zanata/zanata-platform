/*
 * Copyright 2018, Red Hat, Inc. and individual contributors
 *  as indicated by the @author tags. See the copyright.txt file in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it
 *  under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This software is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this software; if not, write to the Free
 *  Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.dao;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Before;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;
import org.zanata.model.HAccount;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountDAOTest extends ZanataDbunitJpaTest {

    private AccountDAO accountDAO;

    @Before
    public void setup() {
        accountDAO = new AccountDAO(getSession());
    }

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
    }

    @Test
    public void createAPITest() {
        HAccount account = accountDAO.getByUsername("demo");
        String apiKey = account.getApiKey();
        accountDAO.createApiKey(account);
        assertThat(account.getApiKey()).isNotEqualTo(apiKey);
    }

    @Test
    public void createSaltedApiKeyTest() {
        String apiKey = AccountDAO.createSaltedApiKey();
        assertThat(apiKey).isNotBlank().hasSize(32);
    }
}
