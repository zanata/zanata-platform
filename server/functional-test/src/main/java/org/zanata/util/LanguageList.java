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

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the behaviour of the project language lists
 *
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class LanguageList {
    private LanguageList(){}

    private static By localeId = By.className("js-locale-id");
    private static By localeAlias = By.className("js-locale-alias");
    private static By languageActions = By.className("button--group");
    private static By dropdown = By.className("i--arrow-down");
    private static By aliasInput = By.className("form--inline__input");
    private static By setAlias = By.className("form--inline__addon");
    private static By addAliasLink = By.linkText("Add alias");
    private static By deleteAliasLink = By.className("i--trash");
    private static By editAliasLink = By.linkText("Edit alias");

    public static List<String> getListedLocales(WebElement localeList) {
        return getListElements(localeList)
                .stream()
                .filter(WebElement::isDisplayed)
                .map(li -> li.findElement(localeId).getText())
                .collect(Collectors.toList());
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

    public static boolean toggleLanguageInList(WebElement localeList,
            String locale) {
        WebElement localeItem = getLocaleEntry(localeList, locale);
        try {
            LanguageList.toggleEnabled(localeItem);
        } catch (NoSuchElementException nsee) {
            return false;
        }
        return true;
    }

    public static void clickActionsDropdown(WebElement list, String locale) {
        getLocaleEntry(list, locale).findElement(dropdown).click();
    }

    public static void clickAddAlias(WebElement list, String locale) {
        getLocaleEntry(list, locale).findElement(addAliasLink).click();
    }

    public static void clickEditAlias(WebElement list, String locale) {
        getLocaleEntry(list, locale).findElement(editAliasLink).click();
    }

    public static void enterAlias(WebElement localeList, String locale,
            String alias) {
        WebElement field = getLocaleEntry(localeList, locale)
                .findElement(aliasInput);
        field.clear();
        field.sendKeys(alias);
    }

    public static void setAlias(WebElement localeList, String locale) {
        getLocaleEntry(localeList, locale).findElement(setAlias).click();
    }

    public static void unsetAlias(WebElement localeList, String locale) {
        getLocaleEntry(localeList, locale).findElement(deleteAliasLink).click();
    }

    public static String getAliasForLocale(WebElement list, String locale) {
        if (!hasAlias(list, locale)) {
            return "";
        }

        return getLocaleEntry(list, locale).findElement(localeAlias)
                .getText();
    }

    private static boolean hasAlias(WebElement localeList, String locale) {
        return getLocaleEntry(localeList, locale).findElements(localeAlias)
                .size() > 0;
    }

    private static WebElement getLocaleEntry(WebElement list, String locale) {
        List<WebElement> listElements = LanguageList
                .getListElements(list);
        for (WebElement localeLi : listElements) {
            if (LanguageList.getLocaleID(localeLi).equals(locale)) {
                return localeLi;
            }
        }
        throw new NoSuchElementException("Unable to find locale " + locale);

    }

}

