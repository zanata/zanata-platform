/*
 *
 *  * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */

package org.zanata.util;

import java.util.Comparator;

import org.zanata.common.ProjectType;
import org.zanata.model.HAccountRole;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HTextFlow;

import lombok.Getter;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class ComparatorUtil {

    public static final Comparator<HLocale> LOCALE_COMPARATOR =
            new Comparator<HLocale>() {
                @Override
                public int compare(HLocale hLocale, HLocale hLocale2) {
                    return hLocale.retrieveDisplayName().compareTo(
                            hLocale2.retrieveDisplayName());
                }
            };

    public static final Comparator<HPerson> PERSON_NAME_COMPARATOR =
            new Comparator<HPerson>() {
                @Override
                public int compare(HPerson hPerson, HPerson hPerson2) {
                    return hPerson.getName().compareTo(hPerson2.getName());
                }
            };

    public static final Comparator<HProjectIteration> PROJECT_NAME_COMPARATOR =
            new Comparator<HProjectIteration>() {
                @Override
                public int compare(HProjectIteration version1,
                        HProjectIteration version2) {
                    return version1
                            .getProject()
                            .getName()
                            .toLowerCase()
                            .compareTo(
                                    version2.getProject().getName()
                                            .toLowerCase());
                }
            };

    public static final Comparator<HProjectIteration> VERSION_CREATION_DATE_COMPARATOR =
            new Comparator<HProjectIteration>() {
                @Override
                public int compare(HProjectIteration o1, HProjectIteration o2) {
                    return o1.getCreationDate().compareTo(o2.getCreationDate());
                }
            };

    public static final Comparator<HProject> PROJECT_CREATION_DATE_COMPARATOR =
            new Comparator<HProject>() {
                @Override
                public int compare(HProject o1, HProject o2) {
                    return o2.getCreationDate().after(o1.getCreationDate()) ? 1
                            : -1;
                }
            };

    public static final Comparator<HAccountRole> ACCOUNT_ROLE_COMPARATOR =
            new Comparator<HAccountRole>() {
                @Override
                public int compare(HAccountRole o1, HAccountRole o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            };

    public static final Comparator<ProjectType> PROJECT_TYPE_COMPARATOR =
            new Comparator<ProjectType>() {
                @Override
                public int compare(ProjectType o1, ProjectType o2) {
                    if (o1 == null) {
                        return -1;
                    }
                    if (o2 == null) {
                        return 1;
                    }
                    return o1.toString().compareTo(o2.toString());
                }
            };
}
