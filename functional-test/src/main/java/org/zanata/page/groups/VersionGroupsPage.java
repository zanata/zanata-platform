package org.zanata.page.groups;

import com.google.common.base.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;

import java.util.List;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class VersionGroupsPage extends BasePage {
    public static final int GROUP_NAME_COLUMN = 0;
    public static final int GROUP_DESCRIPTION_COLUMN = 1;
    public static final int GROUP_TIMESTAMP_COLUMN = 2;
    public static final int GROUP_STATUS_COLUMN = 3;

    private By groupTable = By.id("groupForm:groupTable");
    private By infomsg = By.className("infomsg.icon-info-circle-2");
    private By createGroupButton = By.id("group-create");
    private By toggleObsolete = By.id("groupForm:showObsolete");
    private By obsoleteLink = By.className("obsolete_link");

    public VersionGroupsPage(WebDriver driver) {
        super(driver);
    }

    public List<String> getGroupNames() {
        return WebElementUtil.getColumnContents(getDriver(), groupTable,
                GROUP_NAME_COLUMN);
    }

    public CreateVersionGroupPage createNewGroup() {
        log.info("Click New Group button");
        waitForWebElement(createGroupButton).click();
        return new CreateVersionGroupPage(getDriver());
    }

    public VersionGroupPage goToGroup(String groupName) {
        log.info("Click group {}", groupName);
        waitForWebElement(groupTable).findElement(By.linkText(groupName)).click();
        return new VersionGroupPage(getDriver());
    }

    public VersionGroupsPage toggleObsolete(final boolean show) {
        WebElement showObsolete = waitForWebElement(toggleObsolete);
        if (show != showObsolete.isSelected()) {
            showObsolete.click();
        }
        waitForAMoment().until(new Predicate<WebDriver>() {
            @Override
            public boolean apply(WebDriver input) {
                return waitForWebElement(groupTable)
                        .findElements(obsoleteLink)
                        .isEmpty() == !show;
            }
        });
        return new VersionGroupsPage(getDriver());
    }

    public String getInfoMessage() {
        log.info("Test info msg");
        log.info(waitForWebElement(infomsg).getText());
        return waitForWebElement(infomsg).getText();
    }

}
