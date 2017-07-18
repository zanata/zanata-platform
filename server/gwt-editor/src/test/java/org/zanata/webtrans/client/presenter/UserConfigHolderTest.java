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
package org.zanata.webtrans.client.presenter;

import static org.hamcrest.MatcherAssert.assertThat;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Random;

import org.apache.commons.beanutils.BeanUtils;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.zanata.webtrans.shared.rpc.NavOption;
import org.zanata.webtrans.shared.ui.UserConfigHolder;

public class UserConfigHolderTest {

    private UserConfigHolder configHolder;

    @Before
    public void setUp() throws Exception {
        configHolder = new UserConfigHolder();
    }

    @Test
    public void testDefaultValues() throws Exception {

        Map<String, String> propertiesMap = getPropertiesMap();

        assertThat(propertiesMap,
                Matchers.hasEntry("enterSavesApproved", "false"));
        assertThat(propertiesMap, Matchers.hasEntry("displayButtons", "true"));
        assertThat(propertiesMap, Matchers.hasEntry("editorPageSize", "25"));
        assertThat(propertiesMap, Matchers.hasEntry("showError", "false"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getPropertiesMap()
            throws IllegalAccessException, InvocationTargetException,
            NoSuchMethodException {
        return BeanUtils.describe(configHolder.getState());
    }

    @Test
    public void randomSetterAndGetter() throws Exception {
        Random random = new Random(System.nanoTime());
        boolean value = random.nextBoolean();

        configHolder.setEnterSavesApproved(value);
        configHolder.setDisplayButtons(value);
        configHolder.setShowError(value);

        assertThat(configHolder.getState().isEnterSavesApproved(),
                Matchers.equalTo(value));
        assertThat(configHolder.getState().isDisplayButtons(),
                Matchers.equalTo(value));
        assertThat(configHolder.getState().isShowError(),
                Matchers.equalTo(value));
    }

    @Test
    public void canGetPredicateBasedOnNavOption() {
        configHolder.setNavOption(NavOption.FUZZY_UNTRANSLATED);
        assertThat(configHolder.getContentStatePredicate(),
                Matchers.is(UserConfigHolder.INCOMPLETE_PREDICATE));

        configHolder.setNavOption(NavOption.FUZZY);
        assertThat(configHolder.getContentStatePredicate(),
                Matchers.is(UserConfigHolder.DRAFT_PREDICATE));

        configHolder.setNavOption(NavOption.UNTRANSLATED);
        assertThat(configHolder.getContentStatePredicate(),
                Matchers.is(UserConfigHolder.NEW_PREDICATE));
    }
}
