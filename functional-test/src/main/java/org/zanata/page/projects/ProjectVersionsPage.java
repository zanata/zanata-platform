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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.projectversion.CreateVersionPage;
import org.zanata.page.projectversion.VersionLanguagesPage;
import org.zanata.util.WebElementUtil;

import java.util.List;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class ProjectVersionsPage extends ProjectBasePage {

    @FindBy(id = "versions-more-actions")
    private WebElement versionTabMoreAction;

    @FindBy(id = "new-version-link")
    private WebElement createNewVersion;

    public ProjectVersionsPage(WebDriver driver) {
        super(driver);
    }

    public CreateVersionPage clickCreateVersionLink() {
        gotoVersionsTab();

        clickLinkAfterAnimation(versionTabMoreAction);
        clickLinkAfterAnimation(createNewVersion);
        return new CreateVersionPage(getDriver());
    }

    public VersionLanguagesPage gotoVersion(final String versionId) {
        return refreshPageUntil(this,
                new Function<WebDriver, VersionLanguagesPage>() {
                    @Override
                    public VersionLanguagesPage apply(WebDriver input) {
                        WebElement versionForm =
                                getDriver().findElement(By.id("versions_form"));

                        List<WebElement> versionLinks =
                                versionForm.findElement(
                                        By.className("list--stats"))
                                        .findElements(By.tagName("li"));

                        log.info("found {} active versions",
                                versionLinks.size());

                        Optional<WebElement> found =
                                Iterables.tryFind(versionLinks,
                                        new Predicate<WebElement>() {
                                            @Override
                                            public boolean apply(
                                                    WebElement input) {
                                                return input
                                                        .findElement(
                                                                By.className("list__title"))
                                                        .getText()
                                                        .contains(versionId);
                                            }
                                        });

                        Preconditions.checkState(found.isPresent(), versionId
                                + " not found");
                        found.get().findElement(By.tagName("a")).click();
                        return new VersionLanguagesPage(getDriver());
                    };
                });
    }

    public List<String> getVersions() {
        return WebElementUtil.elementsToText(getDriver(),
                By.xpath("//h3[@class='list__title']"));
    }

    public int getNumberOfDisplayedVersions() {
        return Integer.parseInt(getDriver()
                .findElement(By.id("versionSearch:versionSearch-page-info"))
                .getText());
    }

    public ProjectVersionsPage waitForDisplayedVersions(final int expected) {
        waitForTenSec().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return getNumberOfDisplayedVersions() == expected &&
                        getVersions().size() == expected;
            }
        });
        return new ProjectVersionsPage(getDriver());
    }

    public ProjectVersionsPage clickSearchIcon() {
        getDriver()
                .findElement(By.id("versions"))
                .findElement(By.className("panel__search__button"))
                .click();
        return new ProjectVersionsPage(getDriver());
    }

    public ProjectVersionsPage clearVersionSearch() {
        int maxKeys = 500;
        while (!getDriver().findElement(By.id("versionSearch__input"))
                .getAttribute("value").isEmpty() && maxKeys > 0) {
            getDriver().findElement(By.id("versionSearch__input"))
                .sendKeys(Keys.BACK_SPACE);
        }
        if (maxKeys == 0) {
            log.warn("Exceeded max keypresses for clearing search bar");
        }
        return new ProjectVersionsPage(getDriver());
    }

    public ProjectVersionsPage enterVersionSearch(String searchTerm) {
        getDriver().findElement(By.id("versionSearch__input"))
                .sendKeys(searchTerm);
        return new ProjectVersionsPage(getDriver());
    }
}
