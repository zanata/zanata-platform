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
package org.zanata.ui.autocomplete;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jglue.cdiunit.deltaspike.SupportDeltaspikeCore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.zanata.ZanataTest;
import org.zanata.common.LocaleId;
import org.zanata.dao.LocaleDAO;
import org.zanata.i18n.Messages;
import org.zanata.model.HLocale;

import com.google.common.collect.Lists;
import org.zanata.service.impl.LocaleServiceImpl;
import org.zanata.test.CdiUnitRunner;

import javax.enterprise.inject.Produces;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@RunWith(CdiUnitRunner.class)
@SupportDeltaspikeCore
public class AutocompleteTest extends ZanataTest {

    private static final List<HLocale> supportedLocales = Lists.newArrayList(
            new HLocale(LocaleId.DE), new HLocale(LocaleId.EN), new HLocale(
                    LocaleId.EN_US), new HLocale(LocaleId.ES), new HLocale(
                    LocaleId.FR));

    @Produces @Mock
    LocaleServiceImpl localeServiceImpl;
    @Produces @Mock
    LocaleDAO localeDAO;
    @Produces @Mock
    Messages messages;

    @Test
    public void suggestLocales() {
        when(localeServiceImpl.getSupportedLocales())
                .thenReturn(supportedLocales);
        when(localeDAO.findAllActive())
                .thenReturn(supportedLocales);

        LocaleAutocomplete autocomplete = new LocaleAutocomplete() {
            private static final long serialVersionUID = 1L;

            @Override
            protected Collection<HLocale> getLocales() {
                return Collections.emptyList();
            }

            @Override
            public void onSelectItemAction() {
            }
        };

        autocomplete.setQuery("en");
        assertThat(autocomplete.suggest()).hasSize(3);
        // ENglish, ENglish-us and frENch
        assertThat(autocomplete.suggest()).contains(new HLocale(LocaleId.EN),
                new HLocale(LocaleId.EN_US), new HLocale(LocaleId.FR));

        autocomplete.setQuery("e");
        assertThat(autocomplete.suggest()).hasSize(5);
        // Everything
        assertThat(autocomplete.suggest()).contains(new HLocale(LocaleId.DE),
                new HLocale(LocaleId.EN), new HLocale(LocaleId.EN_US),
                new HLocale(LocaleId.ES), new HLocale(LocaleId.FR));

        autocomplete.setQuery("no-results-expected");
        assertThat(autocomplete.suggest()).hasSize(0);
        // Nothing
    }
}
