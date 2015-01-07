/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.administration;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.zanata.page.BasePage;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class TranslationMemoryPage extends BasePage {

    public static final String ID_UNAVAILABLE = "This Id is not available";
    public static final String UPLOAD_ERROR =
            "There was an error uploading the file";
    public static final String NO_MEMORIES =
            "No Translation Memories have been created.";

    private By listItemCount = By.className("badge");
    private By listItemDescription = By.className("list__item__meta");
    private By dropDownMenu = By.id("moreActions");
    private By createTmLink = By.id("createTmLink");
    private By tmList = By.id("tmList");
    private By filenameInput = By.name("uploadedFile");
    private By uploadButton = By.name("uploadBtn");
    private By listDropDownMenu = By.className("dropdown__toggle");
    private By listImportButton = By.linkText("Import");
    private By listExportButton = By.linkText("Export");
    private By listClearButton = By.linkText("Clear");
    private By listDeleteButton = By.linkText("Delete");

    public TranslationMemoryPage(WebDriver driver) {
        super(driver);
    }

    public TranslationMemoryEditPage clickCreateNew() {
        log.info("Click Create New");
        waitForWebElement(dropDownMenu).click();
        clickLinkAfterAnimation(createTmLink);
        return new TranslationMemoryEditPage(getDriver());
    }

    public TranslationMemoryPage clickOptions(String tmName) {
        log.info("Click Options dropdown for {}", tmName);
        waitForWebElement(findRowByTMName(tmName), listDropDownMenu).click();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickImport(String tmName) {
        log.info("Click Import");
        waitForWebElement(findRowByTMName(tmName), listImportButton).click();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage enterImportFileName(String importFileName) {
        log.info("Enter import TM filename {}", importFileName);
        waitForWebElement(filenameInput).sendKeys(importFileName);
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickUploadButtonAndAcknowledge() {
        log.info("Click and accept Upload button");
        waitForWebElement(uploadButton).click();
        switchToAlert().accept();
        return new TranslationMemoryPage(getDriver());
    }

    public Alert expectFailedUpload() {
        log.info("Click Upload");
        waitForWebElement(uploadButton).click();
        return switchToAlert();
    }

    public TranslationMemoryPage clickClearTMAndAccept(String tmName) {
        log.info("Click and accept Clear {}", tmName);
        waitForWebElement(findRowByTMName(tmName), listClearButton).click();
        switchToAlert().accept();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickClearTMAndCancel(String tmName) {
        log.info("Click and Cancel Clear {}", tmName);
        waitForWebElement(findRowByTMName(tmName), listClearButton).click();
        switchToAlert().dismiss();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickDeleteTmAndAccept(String tmName) {
        log.info("Click and accept Delete {}", tmName);
        waitForWebElement(findRowByTMName(tmName), listDeleteButton).click();
        switchToAlert().accept();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage clickDeleteTmAndCancel(String tmName) {
        log.info("Click and cancel Delete {}", tmName);
        waitForWebElement(findRowByTMName(tmName), listDeleteButton).click();
        switchToAlert().dismiss();
        return new TranslationMemoryPage(getDriver());
    }

    public TranslationMemoryPage dismissError() {
        log.info("Dismiss error dialog");
        switchToAlert().accept();
        return new TranslationMemoryPage(getDriver());
    }

    public List<String> getListedTranslationMemorys() {
        log.info("Query translation memory names");
        List<String> names = new ArrayList<>();
        for (WebElement listElement : getTMList()) {
            names.add(getListEntryName(listElement));
        }
        return names;
    }

    public String getDescription(String tmName) {
        log.info("Query description {}", tmName);
        return getListEntryDescription(findRowByTMName(tmName));
    }

    public String getNumberOfEntries(String tmName) {
        log.info("Query number of entries {}", tmName);
        return getListEntryCount(findRowByTMName(tmName));
    }

    public String waitForExpectedNumberOfEntries(final String tmName,
            final String expected) {
        log.info("Waiting for {} entries in {}", expected, tmName);
        return waitForAMoment().until(new Function<WebDriver, String>() {
            @Override
            public String apply(WebDriver driver) {
                return expected.equals(getNumberOfEntries(tmName)) ? getNumberOfEntries(tmName)
                        : null;
            }
        });
    }

    public boolean canDelete(String tmName) {
        log.info("Query can delete {}", tmName);
        String disabled = waitForWebElement(
                findRowByTMName(tmName), listDeleteButton)
                .getAttribute("disabled");

        return null == disabled || disabled.equals("false");
    }

    /*
     * Check to see if the TM list is empty
     */
    private boolean noTmsCreated() {
        for (WebElement element : waitForWebElement(tmList)
                .findElements(By.tagName("p"))) {
            if (element.getText().equals(NO_MEMORIES)) {
                return true;
            }
        }
        return false;
    }
    /*
     * Get a row from the TM table that corresponds with tmName
     */
    private WebElement findRowByTMName(final String tmName) {
        for (WebElement listElement : getTMList()) {
            if (getListEntryName(listElement).equals(tmName)) {
                return listElement;
            }
        }
        return null;
    }

    private List<WebElement> getTMList() {
        if (noTmsCreated()) {
            log.info("TM list is empty");
            return new ArrayList<>();
        }
        return waitForWebElement(waitForWebElement(tmList),
                By.className("list--stats"))
                .findElements(By.className("list__item--actionable"));
    }

    private String getListEntryName(WebElement listElement) {
        String title = listElement.findElement(By.tagName("h3")).getText().trim();
        return title.substring(0, title.lastIndexOf(getListEntryCount(listElement))).trim();
    }

    private String getListEntryDescription(WebElement listElement) {
        return waitForWebElement(listElement, listItemDescription).getText();
    }

    private String getListEntryCount(WebElement listElement) {
        return listElement.findElement(By.tagName("h3"))
                .findElement(listItemCount).getText();
    }
}
