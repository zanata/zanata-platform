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
package org.zanata.page.groups;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.zanata.page.BasePage;
import com.google.common.base.Predicate;
import org.zanata.page.dashboard.DashboardGroupsTab;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class CreateVersionGroupPage extends BasePage {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CreateVersionGroupPage.class);
    public static final String LENGTH_ERROR =
            "value must be shorter than or equal to 100 characters";
    public static final String VALIDATION_ERROR =
            "must start and end with letter or number, and contain only letters, numbers, periods, underscores and hyphens.";
    private By groupIdField = By.id("group-form:slug:input:slug");
    public By groupNameField = By.id("group-form:name:input:name");
    private By groupDescriptionField =
            By.id("group-form:description:input:description");
    private By saveButton = By.id("group-form:group-create-new");
    private By createNewButton = By.id("group-form:group-create-new");

    public CreateVersionGroupPage(WebDriver driver) {
        super(driver);
    }

    public CreateVersionGroupPage inputGroupId(String groupId) {
        log.info("Enter Group ID {}", groupId);
        enterText(readyElement(groupIdField), groupId);
        return new CreateVersionGroupPage(getDriver());
    }

    public String getGroupIdValue() {
        log.info("Query Group ID");
        return readyElement(groupIdField).getAttribute("value");
    }

    public CreateVersionGroupPage inputGroupName(String groupName) {
        log.info("Enter Group name {}", groupName);
        enterText(readyElement(groupNameField), groupName);
        return new CreateVersionGroupPage(getDriver());
    }

    public CreateVersionGroupPage inputGroupDescription(String desc) {
        log.info("Enter Group description {}", desc);
        enterText(readyElement(groupDescriptionField), desc);
        return new CreateVersionGroupPage(getDriver());
    }

    public VersionGroupPage saveGroup() {
        log.info("Click Save");
        clickAndCheckErrors(readyElement(saveButton));
        return new VersionGroupPage(getDriver());
    }

    public CreateVersionGroupPage saveGroupFailure() {
        log.info("Click Save");
        clickElement(saveButton);
        return new CreateVersionGroupPage(getDriver());
    }

    public CreateVersionGroupPage clearFields() {
        readyElement(groupIdField).clear();
        readyElement(groupNameField).clear();
        readyElement(groupDescriptionField).clear();
        waitForAMoment().until(
                (Predicate<WebDriver>) webDriver -> getGroupIdValue().equals("")
                        && readyElement(groupNameField).getAttribute("value")
                                .equals(""));
        return new CreateVersionGroupPage(getDriver());
    }
}
