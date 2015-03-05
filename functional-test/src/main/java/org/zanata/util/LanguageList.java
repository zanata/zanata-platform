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
package org.zanata.util;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.List;

/**
 * Handles the behaviour of the project language lists
 *
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class LanguageList {
    private LanguageList(){}

    private static By localeId = By.className("js-locale-id");
    private static By languageActions = By.className("button--group");

    public static List<String> getListedLocales(WebElement localeList) {
        return Lists.transform(getListElements(localeList),
                new Function<WebElement, String>() {
                    @Override
                    public String apply(WebElement li) {
                        return li.findElement(localeId).getText();
                    }
                });
    }

    public static List<WebElement> getListElements(WebElement localeList) {
        return localeList.findElements(By.className("reveal"));
    }

    public static String getLocaleID(WebElement localeListElement) {
        return localeListElement.findElement(localeId).getText();
    }

    public static void toggleEnabled(WebElement localeListElement) {
        localeListElement.findElement(languageActions)
                .findElements(By.tagName("button")).get(0).click();
    }

    public static boolean toggleLanguageInList(WebElement localeList, String localeId) {
        boolean clickedLocale = false;
        List<WebElement> listElements = LanguageList
                .getListElements(localeList);
        for (WebElement localeLi : listElements) {
            if (LanguageList.getLocaleID(localeLi).equals(localeId)) {
                LanguageList.toggleEnabled(localeLi);
                clickedLocale = true;
                break;
            }
        }
        return clickedLocale;
    }
}

