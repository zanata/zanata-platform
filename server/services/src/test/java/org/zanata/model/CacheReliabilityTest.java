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
package org.zanata.model;

import javax.persistence.EntityManager;

import org.dbunit.operation.DatabaseOperation;
import org.junit.Test;
import org.zanata.ZanataDbunitJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class CacheReliabilityTest extends ZanataDbunitJpaTest {

    @Override
    protected void prepareDBUnitOperations() {
        beforeTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/AccountData.dbunit.xml",
                DatabaseOperation.CLEAN_INSERT));
        afterTestOperations.add(new DataSetOperation(
                "org/zanata/test/model/ClearAllTables.dbunit.xml",
                DatabaseOperation.DELETE_ALL));
    }

    @Test
    public void secondLevelCacheAccessInSameTx() throws Exception {
        EntityManager em = super.newEntityManagerInstance();

        HPerson p = em.find(HPerson.class, 3L);
        assertThat(p.getName()).isEqualTo("Bob Translator");

        em.clear();

        p = em.find(HPerson.class, 3L);
        // Should still be bob translator
        assertThat(p.getName()).isEqualTo("Bob Translator");
    }

    @Test
    public void secondLevelCacheAccessAfterCommit() throws Exception {
        EntityManager em = super.newEntityManagerInstance();

        HPerson p = em.find(HPerson.class, 3L);
        assertThat(p.getName()).isEqualTo("Bob Translator");

        em.close();
        em = super.newEntityManagerInstance();

        p = em.find(HPerson.class, 3L);
        // Should still be bob translator
        assertThat(p.getName()).isEqualTo("Bob Translator");
    }

    @Test
    public void readWriteCacheTest() throws Exception {
        EntityManager em1 = super.newEntityManagerInstance(), em2 =
                super.newEntityManagerInstance();

        em1.getTransaction().begin();
        em2.getTransaction().begin();

        // EM 1
        HPerson bobT = em1.find(HPerson.class, 3L);
        assertThat(bobT.getName()).isEqualTo("Bob Translator");

        // EM 2
        HPerson bobTCopy = em2.find(HPerson.class, 3L);
        assertThat(bobTCopy.getName()).isEqualTo("Bob Translator");

        // EM 1
        bobT.setName("Bob Administrator");
        bobT = em1.merge(bobT);
        em1.flush();
        assertThat(bobT.getName()).isEqualTo("Bob Administrator");

        // EM2
        bobTCopy = em2.find(HPerson.class, 3L);
        // Still Bob Translator (even after flush)
        assertThat(bobTCopy.getName()).isEqualTo("Bob Translator");

        // EM 1
        em1.getTransaction().commit();
        em1.close();

        // EM 2
        em2.clear();
        bobTCopy = em2.find(HPerson.class, 3L);
        // Bob Administrator now
        assertThat(bobTCopy.getName()).isEqualTo("Bob Administrator");

        // EM 2
        em2.getTransaction().commit();
        em2.close();
    }

}
