package org.zanata.feature.administration;

import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.zanata.feature.testharness.TestPlan;
import org.zanata.feature.testharness.ZanataTestCase;
import org.zanata.page.utility.HomePage;
import org.zanata.util.AddUsersRule;
import org.zanata.workflow.LoginWorkFlow;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Damian Jansen
 * <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Category(TestPlan.DetailedTest.class)
public class HelpPageTest extends ZanataTestCase {

    @Rule
    public AddUsersRule addUsersRule = new AddUsersRule();

    @Test
    public void setHelpURLTest() {
        HomePage homePage = new LoginWorkFlow()
                .signIn("admin", "admin")
                .goToAdministration()
                .goToServerConfigPage()
                .inputHelpURL("http://www.test.com")
                .save()
                .goToHomePage();

        assertThat(homePage.getHelpURL())
                .isEqualTo("http://www.test.com/")
                .as("The help URL was set correctly");
    }
}
