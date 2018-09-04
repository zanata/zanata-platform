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
package org.zanata.feature.misc

import org.assertj.core.api.Assertions.assertThat

import java.util.Objects
import java.util.stream.Collectors

import javax.persistence.EntityManager
import javax.persistence.EntityManagerFactory
import javax.persistence.Persistence

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.zanata.util.PropertiesHolder

import com.google.common.base.MoreObjects
import org.junit.jupiter.api.Disabled
import org.zanata.feature.testharness.BasicAcceptanceTest

/**
 * If you use cargo wait and run this test in IDE, IDE may re-import the project
 * and wipe some of the maven properties out from setup.properties (normally
 * mysql.port). You can go to target/test-classes/setup.properties and add 13306
 * as port to the connection urls.
 *
 * @author Patrick Huang [pahuang@redhat.com](mailto:pahuang@redhat.com)
 */
@BasicAcceptanceTest
class DatabaseDDLTest {
    private lateinit var propForPersistenceUnit: MutableMap<String, String>

    @BeforeEach
    fun setUp() {
        val username = PropertiesHolder.getProperty("hibernate.connection.username")
        val password = PropertiesHolder.getProperty("hibernate.connection.password")
        propForPersistenceUnit = hashMapOf(
            username to "hibernate.connection.username",
            password to "hibernate.connection.password",
            "false" to "hibernate.cache.use_second_level_cache",
            // override below setting from zanata-war test db
            "" to "hibernate.connection.provider_class",
            "com.mysql.jdbc.Driver" to "hibernate.connection.driver_class",
            "org.hibernate.dialect.MySQL5InnoDBDialect" to "hibernate.dialect",
            "none" to "javax.persistence.validation.mode")
    }

    @Test
    @Disabled("TODO")
    fun hibernateValidate() {
        val dbName = PropertiesHolder.getProperty("ds.database")

        val emfFromLiquibase = entityManagerFactoryFromLiquibase()

        val em1 = emfFromLiquibase.createEntityManager()
        val fkFromLiquibase = getForeignKeysForDatabase(dbName, em1)
        log.debug("foreign keys from liquibase:{}", fkFromLiquibase)

        val emfFromHbm = entityManagerFactoryFromHibernateMapping()

        val em2 = emfFromHbm.createEntityManager()
        val fkFromHibernate = getForeignKeysForDatabase("test", em2)
        log.debug("foreign keys from hibernate:{}", fkFromHibernate)

        assertThat(fkFromLiquibase)
                .describedAs("Liquibase foreign keys should equate to hibernate mapping")
                .isEqualTo(fkFromHibernate)

        val ukFromLiquibase = getUniqueKeysForDatabase(dbName, em1)
        val ukFromHibernate = getUniqueKeysForDatabase("test", em2)

        assertThat(ukFromLiquibase)
                .describedAs("Liquibase unique keys should equate to hibernate mapping")
                .isEqualTo(ukFromHibernate)
    }

    private fun getUniqueKeysForDatabase(dbName: String,
                                         em: EntityManager): List<UniqueKey> {
        return em.createNativeQuery(UNIQUE_KEY_QUERY).setParameter(1, dbName)
                .resultList
                .stream()
                .map { it -> UniqueKey(arrayOf(it)[0], arrayOf(it)[1]) }
                .collect(Collectors.toList())
    }

    private fun entityManagerFactoryFromLiquibase(): EntityManagerFactory {
        val url = PropertiesHolder.getProperty("hibernate.connection.url")

        propForPersistenceUnit["hibernate.connection.url"] = url

        // hibernate validate only validates columns and sequences
        propForPersistenceUnit["hibernate.hbm2ddl.auto"] = "validate"
        return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME,
                propForPersistenceUnit)
    }

    private fun entityManagerFactoryFromHibernateMapping(): EntityManagerFactory {
        // here we use hbm2ddl create-drop to populate test database then query
        // foreign keys
        val urlForTestDB = PropertiesHolder.getProperty("test.db.connection.url")
        propForPersistenceUnit["hibernate.connection.url"] = urlForTestDB
        propForPersistenceUnit["hibernate.hbm2ddl.auto"] = "create-drop"
        return Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME,
                propForPersistenceUnit)
    }

    internal class ParentAndChild(parent: Any, child: Any, fkName: Any) {
        private val parent: String
        private val child: String
        // we ignore foreign key name in equal and hashcode method
        private val fkName: String

        init {
            this.parent = parent.toString()
            this.child = child.toString()
            this.fkName = fkName.toString()
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val that = o as ParentAndChild?
            return parent == that!!.parent && child == that.child
        }

        override fun hashCode(): Int {
            return Objects.hash(parent, child)
        }

        override fun toString(): String {
            return MoreObjects.toStringHelper(this).add("parent", parent)
                    .add("child", child)
                    // .add("fkName", fkName)
                    .toString()
        }
    }

    internal class UniqueKey(table: Any, keyName: Any) {
        private val table: String
        // we ignore key name in equal and hashcode method
        private val keyName: String

        init {
            this.table = table.toString()
            this.keyName = keyName.toString()
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) return true
            if (o == null || javaClass != o.javaClass) return false
            val uniqueKey = o as UniqueKey?
            return table == uniqueKey!!.table
        }

        override fun hashCode(): Int {
            return Objects.hash(table)
        }

        override fun toString(): String {
            return MoreObjects.toStringHelper(this)
                    .add("table", table)
                    .add("keyName", keyName)
                    .toString()
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(DatabaseDDLTest::class.java)

        private val FK_QUERY = (
                "SELECT ke.referenced_table_name parent, ke.table_name child, ke.constraint_name "
                        + "FROM information_schema.KEY_COLUMN_USAGE ke "
                        + "WHERE ke.referenced_table_name IS NOT NULL and ke.TABLE_SCHEMA = ?"
                        + "ORDER BY ke.referenced_table_name, ke.table_name")

        private val UNIQUE_KEY_QUERY = (
                "select table_name, CONSTRAINT_NAME from information_schema.TABLE_CONSTRAINTS "
                        + "where CONSTRAINT_SCHEMA = ? and constraint_type = 'UNIQUE' "
                        + "ORDER BY table_name")
        private val PERSISTENCE_UNIT_NAME = "zanataDatasourcePU"

        private fun getForeignKeysForDatabase(dbName: String,
                                              em: EntityManager): List<ParentAndChild> {
            return em.createNativeQuery(FK_QUERY)
                    .setParameter(1, dbName)
                    .resultList
                    .stream()
                    .map { e -> ParentAndChild(arrayOf(e)[0], arrayOf(e)[1], arrayOf(e)[2]) }
                    .collect(Collectors.toList())
        }
    }

}
