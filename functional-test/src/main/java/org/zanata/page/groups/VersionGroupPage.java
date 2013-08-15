package org.zanata.page.groups;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.page.projects.ProjectPage;
import org.zanata.page.projects.ProjectVersionPage;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

import com.google.common.base.Function;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class VersionGroupPage extends BasePage {

    private By versionsInGroupTableBy = By.id("projects-project_list");

    public VersionGroupPage(final WebDriver driver) {
        super(driver);
    }

    @FindBy(id = "settings-projects-form:newVersionField:newVersionInput")
    private WebElement projectSearchField;

    private final By newVersionListBy = By
            .id("settings-projects-form:newVersionField:newVersionItems");

    public List<WebElement> searchProject(final String projectName,
            final int expectedResultNum) {
        projectSearchField.sendKeys(projectName);

        return refreshPageUntil(this,
                new Function<WebDriver, List<WebElement>>() {
                    @Override
                    public List<WebElement> apply(WebDriver driver) {
                        // we want to wait until search result comes back. There
                        // is no way we can tell whether search result has come
                        // back and table refreshed.
                        // To avoid the
                        // org.openqa.selenium.StaleElementReferenceException
                        // (http://seleniumhq.org/exceptions/stale_element_reference.html),
                        // we have to set expected result num

                        List<WebElement> listItems =
                                WebElementUtil.getListItems(getDriver(),
                                        newVersionListBy);

                        if (listItems.size() != expectedResultNum) {
                            log.debug("waiting for search result refresh...");
                            return null;
                        }
                        return listItems;
                    }
                });
    }

    public VersionGroupPage addToGroup(int rowIndex) {

        List<WebElement> listItems =
                WebElementUtil.getListItems(getDriver(), newVersionListBy);

        listItems.get(rowIndex).click();

        WebElement addButton =
                getDriver()
                        .findElement(
                                By.id("settings-projects-form:group-add-new-project-button"));
        addButton.click();
        return this;
    }

    /**
     * Get the list of project versions attached to the group
     * @return a list of version group identifiers in the format
     *         "$projectID $version"
     */
    public List<String> getProjectVersionsInGroup() {

        List<WebElement> elements = WebElementUtil
                .getListItems(getDriver(), versionsInGroupTableBy);

        List<String> result = new ArrayList<String>();

        for (WebElement element : elements) {
            result.add(element
                    .findElement(By.className("list__title"))
                    .getText());
        }
        return result;
    }

    public ProjectPage clickOnProjectLinkOnRow(int row) {
        List<TableRow> tableRows =
                WebElementUtil
                        .getTableRows(getDriver(), versionsInGroupTableBy);
        WebElement projectLink =
                tableRows.get(row).getCells().get(0)
                        .findElement(By.tagName("a"));
        projectLink.click();
        return new ProjectPage(getDriver());
    }

    public ProjectVersionPage clickOnProjectVersionLinkOnRow(int row) {
        List<TableRow> tableRows =
                WebElementUtil
                        .getTableRows(getDriver(), versionsInGroupTableBy);
        WebElement versionLink =
                tableRows.get(row).getCells().get(1)
                        .findElement(By.tagName("a"));
        versionLink.click();
        return new ProjectVersionPage(getDriver());
    }

    public void clickOnTab(String tabId) {
        WebElement tab = getDriver().findElement(By.id(tabId));
        tab.click();
    }

    public VersionGroupPage clickProjectsTab() {
        getDriver().findElement(By.id("projects_tab")).click();
        return new VersionGroupPage(getDriver());
    }

    public VersionGroupPage clickAddProjectVersionsButton() {
        WebElement addProjectVersionButton =
                waitForTenSec().until(new Function<WebDriver, WebElement>() {
                    @Override
                    public WebElement apply(WebDriver driver) {
                        return driver.findElement(By
                                .xpath("//*[@href='#settings-projects']"));
                    }
                });
        addProjectVersionButton.click();
        return new VersionGroupPage(getDriver());
    }

    /**
     * Enter a project version identifier
     * @param projectVersion identifier in format "$projectID $version"
     * @return new VersionGroupPage
     */
    public VersionGroupPage enterProjectVersion(String projectVersion) {
        getDriver().findElement(By.id("settings-projects-form:newVersionField:"+
                "newVersionInput"))
                .sendKeys(projectVersion);
        return new VersionGroupPage(getDriver());
    }

    public VersionGroupPage clickAddProjectButton() {
        getDriver().findElement(By.id(
                "settings-projects-form:group-add-new-project-button")).click();
        return new VersionGroupPage(getDriver());
    }

}
