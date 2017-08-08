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

package org.zanata.ui;

import java.util.Collection;
import java.util.List;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.zanata.model.HDocument;
import org.zanata.model.HIterationGroup;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;
import org.zanata.model.HProject;
import org.zanata.model.HProjectIteration;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class FilterUtil {

    /**
     * Return filtered list of HPerson from personList which are NOT in
     * allPersonList
     *
     * @param allPersonList
     * @param personList
     * @return
     */
    public static List<HPerson> filterOutPersonList(
            final List<HPerson> allPersonList, List<HPerson> personList) {
        Collection<HPerson> filtered =
                Collections2.filter(personList, new Predicate<HPerson>() {
                    @Override
                    public boolean apply(HPerson input) {
                        return !allPersonList.contains(input);
                    }
                });

        return Lists.newArrayList(filtered);
    }

    /**
     * Return true if
     *
     * 1) Query is empty 2) hLocale is NOT in localeList and hLocale's display
     * name/localeId matches with query.
     *
     *
     *
     * @param localeList
     * @param hLocale
     * @param query
     * @return
     */
    public static boolean isIncludeLocale(Collection<HLocale> localeList,
            HLocale hLocale, String query) {
        return !localeList.contains(hLocale)
                && (StringUtils.startsWithIgnoreCase(hLocale.getLocaleId()
                        .getId(), query) || StringUtils.containsIgnoreCase(
                        hLocale.retrieveDisplayName(), query));
    }
}
