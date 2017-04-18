/*
 * Copyright 2016, Red Hat, Inc. and individual contributors
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
package org.zanata;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.util.PropertiesHolder;

import com.google.common.base.MoreObjects;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class DatabaseDDLTest {
    private static final Logger log =
            LoggerFactory.getLogger(DatabaseDDLTest.class);

    private static final String QUERY_FOR_FK =
            "SELECT ke.referenced_table_name parent, ke.table_name child, ke.constraint_name "
                    + "FROM information_schema.KEY_COLUMN_USAGE ke "
                    +
                    "WHERE ke.referenced_table_name IS NOT NULL and ke.TABLE_SCHEMA = ?"
                    + "ORDER BY ke.referenced_table_name, ke.table_name";


    @Test
    public void hibernateValidate() {
        String url = PropertiesHolder.getProperty("hibernate.connection.url");
        String username =
                PropertiesHolder.getProperty("hibernate.connection.username");
        String password =
                PropertiesHolder.getProperty("hibernate.connection.password");
        String dbName = PropertiesHolder.getProperty("ds.database");

        Map<String, String> propForLiquibaseEM = new HashMap<>();
        propForLiquibaseEM.put("hibernate.connection.url", url);
        propForLiquibaseEM.put("hibernate.connection.username", username);
        propForLiquibaseEM.put("hibernate.connection.password", password);
        // hibernate validate only validates columns and sequences
        propForLiquibaseEM.put("hibernate.hbm2ddl.auto", "validate");
        EntityManagerFactory emfFromLiquibase =
                Persistence.createEntityManagerFactory("zanataDatasourcePU",
                        propForLiquibaseEM);

        EntityManager em2 = emfFromLiquibase.createEntityManager();
        List<Object[]> resultFromLiquibase = em2.createNativeQuery(QUERY_FOR_FK)
                .setParameter(1, dbName).getResultList();
        List<ParentAndChild> fkFromLiquibase = resultFromLiquibase.stream()
                .map(e -> new ParentAndChild(e[0], e[1], e[2]))
                .collect(Collectors.toList());
        em2.close();
        log.debug("{}", fkFromLiquibase);

        // here we use hbm2ddl create-drop to populate test database then query foreign keys
        String urlForTestDB =
                PropertiesHolder.getProperty("test.db.connection.url");
        Map<String, String> propForHbm = new HashMap<>();
        propForHbm.put("hibernate.connection.url", urlForTestDB);
        propForHbm.put("hibernate.connection.username", username);
        propForHbm.put("hibernate.connection.password", password);
        propForHbm.put("hibernate.hbm2ddl.auto", "create-drop");
        EntityManagerFactory emfFromHbm = Persistence
                .createEntityManagerFactory("zanataDatasourcePU", propForHbm);

        EntityManager em1 = emfFromHbm.createEntityManager();
        List<Object[]> resultFromHibernate =
                em1.createNativeQuery(QUERY_FOR_FK)
                        .setParameter(1, "test").getResultList();

        List<ParentAndChild> fkFromHibernate = resultFromHibernate.stream()
                .map(e -> new ParentAndChild(e[0], e[1], e[2]))
                .collect(Collectors.toList());
        em1.close();
        log.debug("{}", fkFromHibernate);

        Assertions.assertThat(fkFromLiquibase).isEqualTo(fkFromHibernate);
    }


    static class ParentAndChild {
        private final String parent;
        private final String child;
        private final String fkName;

        ParentAndChild(Object parent, Object child, Object fkName) {
            this.parent = parent.toString();
            this.child = child.toString();
            this.fkName = fkName.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParentAndChild that = (ParentAndChild) o;
            return Objects.equals(parent, that.parent)
                    && Objects.equals(child, that.child);
        }

        @Override
        public int hashCode() {
            return Objects.hash(parent, child);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("parent", parent)
                    .add("child", child)
//                    .add("fkName", fkName)
                    .toString();
        }
    }

}
