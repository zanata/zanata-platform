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
package org.zanata.page.glossary;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;

/**
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@Slf4j
public class GlossaryPage extends BasePage {

    private By glossaryTable = By.id("glossary_form:data_table");

    public GlossaryPage(WebDriver driver) {
        super(driver);
    }

    public List<String> getAvailableGlossaryLanguages() {
        log.info("Query available glossary languages");
        return WebElementUtil.getColumnContents(getDriver(), glossaryTable, 0);
    }

    public int getGlossaryEntryCount(String lang) {
        log.info("Query number of glossary entries for {}", lang);
        List<String> langs = getAvailableGlossaryLanguages();
        int row = langs.indexOf(lang);
        if (row >= 0) {
            return Integer
                    .parseInt(WebElementUtil.getColumnContents(getDriver(),
                            glossaryTable, 2).get(row));
        }
        return -1;
    }
}
