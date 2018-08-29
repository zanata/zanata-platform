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
package org.zanata.page.administration

import org.openqa.selenium.By
import org.openqa.selenium.WebDriver
import org.zanata.page.BasePage
import org.zanata.util.WebElementUtil
import java.util.ArrayList

/**
 * @author Damian Jansen [djansen@redhat.com](mailto:djansen@redhat.com)
 */
class RoleAssignmentsPage(driver: WebDriver) : BasePage(driver) {

    private val moreActions = By.id("roleassign-more-actions")
    private val newRuleButton = By.linkText("New Rule")
    private val roleTable = By.className("list--stats")

    /**
     * Query the rules, returning a list of patterns
     * @return list of pattern strings
     */
    val rulesByPattern: List<String>
        get() {
            log.info("Query role rules")
            val ret = ArrayList<String>()
            for (name in WebElementUtil.elementsToText(driver, roleTable)) {
                ret.add(name.substring(name.lastIndexOf(':') + 1).trim { it <= ' ' })
            }
            return ret
        }

    /**
     * Press the More Actions dropdown
     * @return new RoleAssignmentsPage
     */
    fun clickMoreActions(): RoleAssignmentsPage {
        log.info("Click More Actions dropdown")
        clickElement(moreActions)
        return RoleAssignmentsPage(driver)
    }

    /**
     * Select the Create New Rule option from the drop down
     * @return new EditRoleAssignmentPage
     */
    fun clickCreateNew(): EditRoleAssignmentPage {
        log.info("Click Create New dropdown option")
        clickLinkAfterAnimation(newRuleButton)
        return EditRoleAssignmentPage(driver)
    }

    companion object {
        private val log = org.slf4j.LoggerFactory.getLogger(RoleAssignmentsPage::class.java)
    }
}
