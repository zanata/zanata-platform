/*
 * Copyright 2018, Red Hat, Inc. and individual contributors as indicated by the
 *  @author tags. See the copyright.txt file in the distribution for a full
 *  listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */

package org.zanata.service;

import javax.enterprise.inject.Produces;
import javax.persistence.EntityManager;

import org.hibernate.Session;
import org.jglue.cdiunit.InRequestScope;
import org.junit.Before;
import org.junit.Test;
import org.zanata.ZanataJpaTest;
import org.zanata.dao.AccountDAO;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sean Flanigan [sflaniga@redhat.com](mailto:sflaniga@redhat.com)
 */
public class GraphQLServiceTest extends ZanataJpaTest {
    private GraphQLService graphQLService;

    @Before
    public void setUp() throws Exception {
        AccountDAO accountDAO = new AccountDAO(getSession());
        graphQLService = new GraphQLService(accountDAO);
        graphQLService.postConstruct();
    }

    @Override
    @Produces
    protected Session getSession() {
        return super.getSession();
    }

    @Override
    @Produces
    protected EntityManager getEm() {
        return super.getEm();
    }

    @Test
    @InRequestScope
    public void schema() {
        String schema = graphQLService.getSchema();
        assertThat(schema).contains("type Account {");
        // NB this will break if we create new entities starting with H
        assertThat(schema).doesNotContainPattern("^type H");
        // TODO
        System.out.println(schema);
    }

}
