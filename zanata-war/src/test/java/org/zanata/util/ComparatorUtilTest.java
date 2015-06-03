/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
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

package org.zanata.util;

import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.common.ProjectType;
import org.zanata.model.*;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ComparatorUtilTest {

    @Test
    public void HLocaleNameCompare() throws Exception {
        HLocale hLocale1 = new HLocale();
        hLocale1.setLocaleId(new LocaleId("alpha"));
        HLocale hLocale2 = new HLocale();
        hLocale2.setLocaleId(new LocaleId("bravo"));

        assertThat(ComparatorUtil.LOCALE_COMPARATOR
                .compare(hLocale1, hLocale1)).isEqualTo(0);
        assertThat(ComparatorUtil.LOCALE_COMPARATOR
                .compare(hLocale2, hLocale1)).isGreaterThan(0);
        assertThat(ComparatorUtil.LOCALE_COMPARATOR
                .compare(hLocale1, hLocale2)).isLessThan(0);
    }

    @Test
    public void HPersonNameCompare() throws Exception {
        HPerson hPerson1 = new HPerson();
        hPerson1.setName("alpha");
        HPerson hPerson2 = new HPerson();
        hPerson2.setName("bravo");

        assertThat(ComparatorUtil.PERSON_NAME_COMPARATOR
                .compare(hPerson1, hPerson1)).isEqualTo(0);
        assertThat(ComparatorUtil.PERSON_NAME_COMPARATOR
                .compare(hPerson2, hPerson1)).isGreaterThan(0);
        assertThat(ComparatorUtil.PERSON_NAME_COMPARATOR
                .compare(hPerson1, hPerson2)).isLessThan(0);
    }

    @Test
    public void HProjectDateCompare() throws Exception {
        HProject hProject1 = new HProject();
        hProject1.setCreationDate(new Date(10));
        HProject hProject2 = new HProject();
        hProject2.setCreationDate(new Date(1));

        assertThat(ComparatorUtil.PROJECT_CREATION_DATE_COMPARATOR
                .compare(hProject2, hProject1)).isEqualTo(1);
        assertThat(ComparatorUtil.PROJECT_CREATION_DATE_COMPARATOR
                .compare(hProject1, hProject2)).isEqualTo(-1);
        assertThat(ComparatorUtil.PROJECT_CREATION_DATE_COMPARATOR
                .compare(hProject1, hProject1)).isEqualTo(0);
    }

    @Test
    public void HProjectIterationProjectNameCompare() throws Exception {
        HProject hProject1 = new HProject();
        hProject1.setName("alpha");
        HProject hProject2 = new HProject();
        hProject2.setName("bravo");
        HProject hProject3 = new HProject();
        hProject3.setName("BRAVO");

        HProjectIteration hProjectIteration1 = new HProjectIteration();
        hProjectIteration1.setProject(hProject1);
        HProjectIteration hProjectIteration2 = new HProjectIteration();
        hProjectIteration2.setProject(hProject2);
        HProjectIteration hProjectIteration3 = new HProjectIteration();
        hProjectIteration3.setProject(hProject3);

        assertThat(ComparatorUtil.PROJECT_NAME_COMPARATOR
                .compare(hProjectIteration1, hProjectIteration1)).isEqualTo(0);
        assertThat(ComparatorUtil.PROJECT_NAME_COMPARATOR
                .compare(hProjectIteration2, hProjectIteration3)).isEqualTo(0);
        assertThat(ComparatorUtil.PROJECT_NAME_COMPARATOR
                .compare(hProjectIteration1, hProjectIteration3)).isEqualTo(-1);
        assertThat(ComparatorUtil.PROJECT_NAME_COMPARATOR
                .compare(hProjectIteration3, hProjectIteration1)).isEqualTo(1);
    }

    @Test
    public void HProjectIterationDateCompare() throws Exception {
        HProjectIteration hProjectIteration1 = new HProjectIteration();
        hProjectIteration1.setCreationDate(new Date(10));
        HProjectIteration hProjectIteration2 = new HProjectIteration();
        hProjectIteration2.setCreationDate(new Date(1));

        assertThat(ComparatorUtil.VERSION_CREATION_DATE_COMPARATOR
                .compare(hProjectIteration1, hProjectIteration1)).isEqualTo(0);
        assertThat(ComparatorUtil.VERSION_CREATION_DATE_COMPARATOR
                .compare(hProjectIteration1, hProjectIteration2)).isEqualTo(-1);
        assertThat(ComparatorUtil.VERSION_CREATION_DATE_COMPARATOR
                .compare(hProjectIteration2, hProjectIteration1)).isEqualTo(1);
    }

    @Test
    public void HAccountRoleCompare() throws Exception {
        HAccountRole hAccountRole1 = new HAccountRole();
        hAccountRole1.setName("alpha");
        HAccountRole hAccountRole2 = new HAccountRole();
        hAccountRole2.setName("bravo");

        assertThat(ComparatorUtil.ACCOUNT_ROLE_COMPARATOR
                .compare(hAccountRole1, hAccountRole2)).isEqualTo(-1);
        assertThat(ComparatorUtil.ACCOUNT_ROLE_COMPARATOR
                .compare(hAccountRole2, hAccountRole1)).isEqualTo(1);
        assertThat(ComparatorUtil.ACCOUNT_ROLE_COMPARATOR
                .compare(hAccountRole1, hAccountRole1)).isEqualTo(0);
    }

    @Test
    public void ProjectTypeCompare() throws Exception {
        ProjectType projectType1 = ProjectType.File;
        ProjectType projectType2 = ProjectType.Gettext;
        ProjectType projectType3 = null;

        assertThat(ComparatorUtil.PROJECT_TYPE_COMPARATOR
                .compare(projectType1, projectType1)).isEqualTo(0);
        assertThat(ComparatorUtil.PROJECT_TYPE_COMPARATOR
                .compare(projectType1, projectType2)).isEqualTo(-1);
        assertThat(ComparatorUtil.PROJECT_TYPE_COMPARATOR
                .compare(projectType2, projectType1)).isEqualTo(1);
        assertThat(ComparatorUtil.PROJECT_TYPE_COMPARATOR
                .compare(projectType1, projectType3)).isEqualTo(1);
        assertThat(ComparatorUtil.PROJECT_TYPE_COMPARATOR
                .compare(projectType3, projectType1)).isEqualTo(-1);
    }
}
