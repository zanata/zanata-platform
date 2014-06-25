package org.zanata.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.zanata.page.utility.HomePage;
import org.zanata.workflow.BasicWorkFlow;

/**
 * A test rule that will ensure tests have a clean browser session before and
 * after.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class EnsureLogoutRule implements TestRule {
    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                logoutIfLoggedIn();
                try {
                    base.evaluate();
                } finally {
                    logoutIfLoggedIn();
                }
            }
        };
    }

    private static HomePage logoutIfLoggedIn() {
        HomePage homePage = new BasicWorkFlow().goToHome();
        if (homePage.hasLoggedIn()) {
            homePage.logout();
        }
        return homePage;
    }
}
