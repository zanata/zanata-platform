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
package org.zanata.page.projectversion;

import com.google.common.base.Predicate;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class VersionDocumentsPage extends VersionBasePage {

    public VersionDocumentsPage(WebDriver driver) {
        super(driver);
    }

    public VersionDocumentsPage waitForSourceDocsContains(final String document) {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return sourceDocumentsContains(document);
            }
        });
        return new VersionDocumentsPage(getDriver());
    }

    public boolean sourceDocumentsContains(String document) {
        List<WebElement> documentList = getDocumentsTabDocumentList();
        for (final WebElement tableRow : documentList) {
            if (tableRow.findElement(By.className("list__title")).getText()
                    .contains(document)) {
                return true;
            }
        }
        return false;
    }

    public List<String> getSourceDocumentNames() {
        List<String> filenames = new ArrayList<String>();
        for (WebElement element : getDocumentsTabDocumentList()) {
            filenames.add(element.findElement(By.className("list__title"))
                    .getText());
        }
        return filenames;
    }

    private List<WebElement> getDocumentsTabDocumentList() {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getDriver()
                        .findElement(By.id("documents-document_list"))
                        .isDisplayed();
            }
        });

        List<WebElement> rows =
                getDriver().findElement(By.id("documents-document_list"))
                        .findElements(By.xpath("./li"));
        return rows;
    }

}
