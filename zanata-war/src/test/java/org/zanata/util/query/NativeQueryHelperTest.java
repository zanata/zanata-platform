/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.util.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.zanata.util.query.NativeQueryHelperFactory.DB_TYPE_H2;

import org.hibernate.Session;
import org.hibernate.jdbc.ReturningWork;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.zanata.ZanataDbunitJpaTest;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class NativeQueryHelperTest extends ZanataDbunitJpaTest {

    @Mock
    Session mysqlSessionMock;

    @Mock
    Session postgresqlSessionMock;

    private NativeQueryHelperFactory nativeQueryHelperFactory =
            new NativeQueryHelperFactory();

    @Before
    public void prepareMocks() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(mysqlSessionMock.doReturningWork(any(ReturningWork.class)))
                .thenReturn(NativeQueryHelperFactory.DB_TYPE_MYSQL);
        Mockito.when(
                postgresqlSessionMock.doReturningWork(any(ReturningWork.class)))
                .thenReturn(NativeQueryHelperFactory.DB_TYPE_POSTGRESQL);
    }

    @Override
    protected void prepareDBUnitOperations() {
        // Nothing to do
    }

    @Test
    public void getDatabaseType() throws Exception {
        String databaseType = NativeQueryHelperFactory.getDatabaseType(getEm());
        assertThat(databaseType).isEqualTo(DB_TYPE_H2);
    }

    @Test
    public void getH2NativeHelper() throws Exception {
        NativeQueryHelper helper =
                nativeQueryHelperFactory.getNativeQueryHelper(getEm());
        assertThat(helper).isInstanceOf(H2NativeQueryHelper.class);
    }

    @Test
    public void getMysqlNativeHelper() throws Exception {
        NativeQueryHelper helper =
                nativeQueryHelperFactory.getNativeQueryHelper(mysqlSessionMock);
        assertThat(helper).isInstanceOf(MysqlNativeQueryHelper.class);
    }

    @Test
    public void getPostgresqlNativeHelper() throws Exception {
        NativeQueryHelper helper =
                nativeQueryHelperFactory
                        .getNativeQueryHelper(postgresqlSessionMock);
        assertThat(helper).isInstanceOf(PostgresqlNativeQueryHelper.class);
    }

    @Test
    public void getNullifFunction() throws Exception {
        NativeQueryHelper helper =
                nativeQueryHelperFactory.getNativeQueryHelper(getEm());
        // Make sure this is the right invocation for the H2 database
        assertThat(helper.ifnull("column", "aDate")).contains("ifnull");
    }
}
