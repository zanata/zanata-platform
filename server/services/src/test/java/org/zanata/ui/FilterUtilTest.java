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
package org.zanata.ui;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.zanata.common.LocaleId;
import org.zanata.model.HLocale;
import org.zanata.model.HPerson;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class FilterUtilTest {

    private static final List<HLocale> ALL_LOCALES = Lists.newArrayList(
            new HLocale(
                    LocaleId.DE), new HLocale(LocaleId.EN), new HLocale(
                    LocaleId.FR));

    @Test
    public void filterOutEmptyPersonList() throws Exception {
        assertThat(
                FilterUtil.filterOutPersonList(Lists.<HPerson> newArrayList(),
                        Lists.<HPerson> newArrayList())).isEmpty();
    }

    @Test
    public void filterOutSamePersonList() throws Exception {
        HPerson person1 = new HPerson();
        HPerson person2 = new HPerson();
        HPerson person3 = new HPerson();
        List<HPerson> all = Lists.newArrayList(person1, person2, person3);
        List<HPerson> filter = Lists.newArrayList(person1, person2, person3);

        assertThat(
                FilterUtil.filterOutPersonList(all, filter)).isEmpty();
    }

    @Test
    public void localeNotIncluded() throws Exception {
        assertThat(
                FilterUtil.isIncludeLocale(ALL_LOCALES,
                        new HLocale(LocaleId.EN), null)).isFalse();
    }
}
