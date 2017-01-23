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
package org.zanata.page.projects;

import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.projectversion.CreateVersionPage;
import org.zanata.page.projectversion.VersionLanguagesPage;
import org.zanata.util.WebElementUtil;
import com.google.common.base.Predicate;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class ProjectVersionsPage extends ProjectBasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(ProjectVersionsPage.class);
    private By versionTabMoreAction = By.id("versions-more-actions");
    private By createNewVersion = By.id("new-version-link");
    private By versionCount = By.id("versionSearch:versionSearch-page-info");
    private By versions = By.id("versions");
    private By searchIcon = By.className("panel__search__button");
    private By versionSearchInput = By.id("versionSearch__input");

    public ProjectVersionsPage(WebDriver driver) {
        super(driver);
    }

    public CreateVersionPage clickCreateVersionLink() {
        log.info("Click Create Version");
        gotoVersionsTab();
        clickLinkAfterAnimation(versionTabMoreAction);
        clickLinkAfterAnimation(createNewVersion);
        return new CreateVersionPage(getDriver());
    }

    public VersionLanguagesPage gotoVersion(final String versionId) {
        log.info("Click Version {}", versionId);
        waitForAMoment().until((Predicate<WebDriver>) webDriver -> {
            getDriver().findElement(By.id("versions_tab")).click();
            List<WebElement> versionLinks =
                    getDriver().findElement(By.id("versions_form"))
                            .findElement(By.className("list--stats"))
                            .findElements(By.tagName("li"));
            boolean clicked = false;
            for (WebElement links : versionLinks) {
                // The Translate Options menu can get picked up here
                for (WebElement link : links.findElements(By.tagName("a"))) {
                    if (link.getText().contains(versionId)) {
                        link.click();
                        clicked = true;
                        break;
                    }
                }
                if (clicked)
                    break;
            }
            return clicked;
        });
        return new VersionLanguagesPage(getDriver());
    }

    public List<String> getVersions() {
        log.info("Query Versions list");
        return WebElementUtil.elementsToText(getDriver(),
                By.xpath("//h3[@class=\'list__title\']"));
    }

    public int getNumberOfDisplayedVersions() {
        log.info("Query number of displayed versions");
        return Integer.parseInt(readyElement(versionCount).getText());
    }

    public ProjectVersionsPage expectDisplayedVersions(final int expected) {
        log.info("Wait for number of displayed versions to be {}", expected);
        waitForPageSilence();
        waitForAMoment().withMessage("Waiting for versions").until(
                (Predicate<WebDriver>) webDriver -> getNumberOfDisplayedVersions() == expected);
        assertThat(getNumberOfDisplayedVersions()).isEqualTo(expected);
        assertThat(getVersions()).hasSize(expected);
        return new ProjectVersionsPage(getDriver());
    }

    public ProjectVersionsPage clickSearchIcon() {
        log.info("Click Search icon");
        clickElement(readyElement(existingElement(versions), searchIcon));
        return new ProjectVersionsPage(getDriver());
    }

    public ProjectVersionsPage clearVersionSearch() {
        log.info("Clear version search field");
        int maxKeys = 500;
        while (!readyElement(versionSearchInput).getAttribute("value").isEmpty()
                && maxKeys > 0) {
            readyElement(versionSearchInput).sendKeys(Keys.BACK_SPACE);
            maxKeys = maxKeys - 1;
        }
        if (maxKeys == 0) {
            log.warn("Exceeded max keypresses for clearing search bar");
        }
        return new ProjectVersionsPage(getDriver());
    }

    public ProjectVersionsPage enterVersionSearch(String searchTerm) {
        log.info("Enter version search {}", searchTerm);
        enterText(readyElement(versionSearchInput), searchTerm);
        return new ProjectVersionsPage(getDriver());
    }
}
