package org.zanata.page.administration;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;
import org.zanata.util.TableRow;
import org.zanata.util.WebElementUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class RoleAssignmentsPage extends BasePage {

    private By newRuleButton = By.linkText("New Rule");
    private By roleTable = By.tagName("table");

    public RoleAssignmentsPage(WebDriver driver) {
        super(driver);
    }

    public EditRoleAssignmentPage clickCreateNew() {
        log.info("Click Create New");
        waitForWebElement(newRuleButton).click();
        return new EditRoleAssignmentPage(getDriver());
    }

    public List<String> getRulesByPattern() {
        log.info("Query role rules");
        List<String> names = new ArrayList<String>();
        List<TableRow> tableRows = WebElementUtil.getTableRows(getDriver(),
                roleTable);
        for (TableRow tableRow : tableRows) {
            names.add(tableRow.getCells().get(1).getText());
        }
        return names;
    }
}
