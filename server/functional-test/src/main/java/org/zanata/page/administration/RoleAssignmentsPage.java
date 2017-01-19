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
package org.zanata.page.administration;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;
import org.zanata.util.WebElementUtil;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Damian Jansen
 *         <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class RoleAssignmentsPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(RoleAssignmentsPage.class);
    private By moreActions = By.id("roleassign-more-actions");
    private By newRuleButton = By.linkText("New Rule");
    private By roleTable = By.className("list--stats");

    public RoleAssignmentsPage(WebDriver driver) {
        super(driver);
    }

    public RoleAssignmentsPage clickMoreActions() {
        log.info("Click More Actions dropdown");
        clickElement(moreActions);
        return new RoleAssignmentsPage(getDriver());
    }

    public EditRoleAssignmentPage clickCreateNew() {
        log.info("Click Create New");
        clickElement(newRuleButton);
        return new EditRoleAssignmentPage(getDriver());
    }

    public List<String> getRulesByPattern() {
        log.info("Query role rules");
        List<String> ret = new ArrayList<>();
        List<String> names =
                WebElementUtil.elementsToText(getDriver(), roleTable);
        for (String name : names) {
            ret.add(name.substring(name.lastIndexOf(':') + 1).trim());
        }
        return ret;
    }
}
