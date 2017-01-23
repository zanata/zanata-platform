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
package org.zanata.page.projects.projectsettings;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projects.ProjectBasePage;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ProjectTranslationTab extends ProjectBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ProjectTranslationTab.class);
    private Map validationNames = getValidationMapping();
    private By validationsList =
            By.id("settings-translation-form:validation-list");

    public ProjectTranslationTab(WebDriver driver) {
        super(driver);
    }

    public boolean isValidationLevel(String optionName, String level) {
        log.info("Query is {} validation level {}", optionName, level);
        final String optionElementID =
                validationNames.get(optionName).toString().concat(level);
        WebElement option = existingElement(existingElement(validationsList),
                By.id(optionElementID));
        return option.getAttribute("checked").equals("true");
    }

    public ProjectTranslationTab setValidationLevel(String optionName,
            String level) {
        log.info("Click {} validation level {}", optionName, level);
        final String optionElementID =
                validationNames.get(optionName).toString().concat(level);
        WebElement option = existingElement(existingElement(validationsList),
                By.id(optionElementID));
        getExecutor().executeScript("arguments[0].click();", option);
        slightPause();
        return new ProjectTranslationTab(getDriver());
    }

    private Map<String, String> getValidationMapping() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("HTML/XML tags", "HTML_XML-");
        map.put("Java variables", "JAVA_VARIABLES-");
        map.put("Leading/trailing newline (\\n)", "NEW_LINE-");
        map.put("Positional printf (XSI extension)", "PRINTF_XSI_EXTENSION-");
        map.put("Printf variables", "PRINTF_VARIABLES-");
        map.put("Tab characters (\\t)", "TAB-");
        map.put("XML entity reference", "XML_ENTITY-");
        return map;
    }
}
