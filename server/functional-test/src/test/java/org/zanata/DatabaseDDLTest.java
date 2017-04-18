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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.junit.Before;
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

    private static final String FK_QUERY =
            "SELECT ke.referenced_table_name parent, ke.table_name child, ke.constraint_name "
                    + "FROM information_schema.KEY_COLUMN_USAGE ke "
                    + "WHERE ke.referenced_table_name IS NOT NULL and ke.TABLE_SCHEMA = ?"
                    + "ORDER BY ke.referenced_table_name, ke.table_name";

    private static final String UNIQUE_KEY_QUERY =
            "select CONSTRAINT_NAME, table_name from information_schema.TABLE_CONSTRAINTS "
                    + "where CONSTRAINT_SCHEMA = ? and constraint_type = 'UNIQUE'";
    private String username;
    private String password;

    @Before
    public void setUp() {
        username =
                PropertiesHolder.getProperty("hibernate.connection.username");
        password =
                PropertiesHolder.getProperty("hibernate.connection.password");
    }

    @Test
    public void hibernateValidate() {
        String dbName = PropertiesHolder.getProperty("ds.database");

        EntityManagerFactory emfFromLiquibase =
                entityManagerFactoryFromLiquibase();

        EntityManager em1 = emfFromLiquibase.createEntityManager();
        List<ParentAndChild> fkFromLiquibase =
                getForeignKeysForDatabase(dbName, em1);
        log.debug("foreign keys from liquibase:{}", fkFromLiquibase);

        EntityManagerFactory emfFromHbm =
                entityManagerFactoryFromHibernateMapping();

        EntityManager em2 = emfFromHbm.createEntityManager();
        List<ParentAndChild> fkFromHibernate =
                getForeignKeysForDatabase("test", em2);
        log.debug("foreign keys from hibernate:{}", fkFromHibernate);

        assertThat(fkFromLiquibase).isEqualTo(fkFromHibernate)
                .as("Liquibase foreign keys should equal to hibernate mapping");

        List<Object[]> ukFromLiquibase = getUniqueKeysForDatabase(dbName, em1);
        List<Object[]> ukFromHibernate = getUniqueKeysForDatabase(dbName, em2);
        assertThat(ukFromLiquibase).hasSameSizeAs(ukFromHibernate)
                .as("Liquibase should have same number of unique key as in hibernate mapping");
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> getUniqueKeysForDatabase(String dbName,
            EntityManager em) {
        return em.createNativeQuery(UNIQUE_KEY_QUERY).setParameter(1, dbName)
                .getResultList();
    }

    private EntityManagerFactory entityManagerFactoryFromLiquibase() {
        String url = PropertiesHolder.getProperty("hibernate.connection.url");

        Map<String, String> propForLiquibaseEM = new HashMap<>();
        propForLiquibaseEM.put("hibernate.connection.url", url);
        propForLiquibaseEM.put("hibernate.connection.username", username);
        propForLiquibaseEM.put("hibernate.connection.password", password);
        // hibernate validate only validates columns and sequences
        propForLiquibaseEM.put("hibernate.hbm2ddl.auto", "validate");
        return Persistence.createEntityManagerFactory("zanataDatasourcePU",
                propForLiquibaseEM);
    }

    private EntityManagerFactory entityManagerFactoryFromHibernateMapping() {
        // here we use hbm2ddl create-drop to populate test database then query
        // foreign keys
        String urlForTestDB =
                PropertiesHolder.getProperty("test.db.connection.url");
        Map<String, String> propForHbm = new HashMap<>();
        propForHbm.put("hibernate.connection.url", urlForTestDB);
        propForHbm.put("hibernate.connection.username", username);
        propForHbm.put("hibernate.connection.password", password);
        propForHbm.put("hibernate.hbm2ddl.auto", "create-drop");
        return Persistence.createEntityManagerFactory("zanataDatasourcePU",
                propForHbm);
    }

    private static List<ParentAndChild> getForeignKeysForDatabase(String dbName,
            EntityManager em) {
        @SuppressWarnings("unchecked")
        List<Object[]> resultFromLiquibase = em.createNativeQuery(FK_QUERY)
                .setParameter(1, dbName).getResultList();
        return resultFromLiquibase.stream()
                .map(e -> new ParentAndChild(e[0], e[1], e[2]))
                .collect(Collectors.toList());
    }

    static class ParentAndChild {
        private final String parent;
        private final String child;
        // we ignore foreign key name in equal and hashcode method
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
            return MoreObjects.toStringHelper(this).add("parent", parent)
                    .add("child", child)
                    // .add("fkName", fkName)
                    .toString();
        }
    }

}
