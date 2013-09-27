/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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

package org.zanata.client.commands.push;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zanata.client.commands.gettext.PublicanUtil;
import org.zanata.client.config.LocaleMapping;

public class GettextDirStrategy extends AbstractGettextPushStrategy {
    private static final Logger log = LoggerFactory
            .getLogger(GettextDirStrategy.class);

    @Override
    List<LocaleMapping> findLocales() {
        List<LocaleMapping> locales;
        if (getOpts().getLocaleMapList() != null) {
            locales =
                    PublicanUtil.findLocales(getOpts().getTransDir(), getOpts()
                            .getLocaleMapList());
            if (locales.size() == 0) {
                log.warn("'pushType' is set to '" + getOpts().getPushType()
                        + "', but none of the configured locale "
                        + "directories was found (check zanata.xml)");
            }
        } else {
            locales = PublicanUtil.findLocales(getOpts().getTransDir());
            if (locales.size() == 0) {
                log.warn("'pushType' is set to '\" + getOpts().getPushType() + \"', but no locale directories were found");
            } else {
                log.info("'pushType' is set to '\" + getOpts().getPushType() + \"', but no locales specified in configuration: "
                        + "importing " + locales.size() + " directories");
            }
        }
        return locales;
    }

    @Override
    File getTransFile(LocaleMapping locale, String docName) {
        File localeDir =
                new File(getOpts().getTransDir(), locale.getLocalLocale());
        File transFile = new File(localeDir, docName + ".po");
        return transFile;
    }

}
