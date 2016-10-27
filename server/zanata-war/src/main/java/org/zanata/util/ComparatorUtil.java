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
import java.util.Date;

import org.apache.commons.lang.StringUtils;
import org.zanata.common.ProjectType;
import org.zanata.model.HAccount;
import org.zanata.model.HAccountRole;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;

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
                    return compareStringIgnoreCase(hPerson.getName(),
                            hPerson2.getName());
        }
            };

    public static final Comparator<HProjectIteration> VERSION_PROJECT_NAME_COMPARATOR =
            new Comparator<HProjectIteration>() {
                @Override
                public int compare(HProjectIteration version1,
                        HProjectIteration version2) {
                    return compareStringIgnoreCase(
                            version1.getProject().getName(),
                            version2.getProject().getName());
                }
            };

    // Sort by creation date descending.
    public static final Comparator<HProjectIteration> VERSION_CREATION_DATE_COMPARATOR =
            new Comparator<HProjectIteration>() {
                @Override
                public int compare(HProjectIteration o1, HProjectIteration o2) {
                    return compareDate(o2.getCreationDate(),
                            o1.getCreationDate());
                }
            };

    public static final Comparator<HProject> PROJECT_CREATION_DATE_COMPARATOR =
            new Comparator<HProject>() {
                @Override
                public int compare(HProject o1, HProject o2) {
                    return compareDate(o2.getCreationDate(),
                            o1.getCreationDate());
                }
            };

    public static final Comparator<HProject> PROJECT_NAME_COMPARATOR =
        new Comparator<HProject>() {
            @Override
            public int compare(HProject o1, HProject o2) {
                return compareStringIgnoreCase(o1.getName(), o2.getName());
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

    public static final Comparator<HAccount> ACCOUNT_NAME_COMPARATOR =
            new Comparator<HAccount>() {
                @Override
                public int compare(HAccount o1, HAccount o2) {
                    return compareStringIgnoreCase(o1.getPerson().getName(), o2
                            .getPerson().getName());
                }
            };

    public static int compareDate(Date date1, Date date2) {
        if (date1 == null) {
            return -1;
        }
        if (date2 == null) {
            return 1;
        }
        return date1.compareTo(date2);
    }

    public static final int compareStringIgnoreCase(String str1, String str2) {
        if (StringUtils.isBlank(str1)) {
            return -1;
        }
        if (StringUtils.isBlank(str2)) {
            return 1;
        }
        return str1.toLowerCase().compareTo(str2.toLowerCase());
    }
}
