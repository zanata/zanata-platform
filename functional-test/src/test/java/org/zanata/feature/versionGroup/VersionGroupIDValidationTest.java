/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.feature.versionGroup;

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.Matchers;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.experimental.categories.Category;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.zanata.feature.DetailedTest;
import org.zanata.page.groups.CreateVersionGroupPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.LoginWorkFlow;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@RunWith(Theories.class)
@Category(DetailedTest.class)
public class VersionGroupIDValidationTest {

    @ClassRule
    public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();

    @DataPoint
    public static String INVALID_CHARACTER_PIPE = "Group|ID";
    @DataPoint
    public static String INVALID_CHARACTER_SLASH = "Group/ID";
    @DataPoint
    public static String INVALID_CHARACTER_BACKSLASH = "Group\\ID";
    @DataPoint
    public static String INVALID_CHARACTER_PLUS = "Group+ID";
    @DataPoint
    public static String INVALID_CHARACTER_ASTERISK = "Group*ID";
    @DataPoint
    public static String INVALID_CHARACTER_LEFT_PARENTHESES = "Group(ID";
    @DataPoint
    public static String INVALID_CHARACTER_RIGHT_PARENTHESES = "Group)ID";
    @DataPoint
    public static String INVALID_CHARACTER_DOLLAR = "Group$ID";
    @DataPoint
    public static String INVALID_CHARACTER_LEFT_BRACKET = "Group[ID";
    @DataPoint
    public static String INVALID_CHARACTER_RIGHT_BRACKET = "Group]ID";
    @DataPoint
    public static String INVALID_CHARACTER_COLON = "Group:ID";
    @DataPoint
    public static String INVALID_CHARACTER_SEMICOLON = "Group;ID";
    @DataPoint
    public static String INVALID_CHARACTER_APOSTROPHE = "Group'ID";
    @DataPoint
    public static String INVALID_CHARACTER_COMMA = "Group,ID";
    @DataPoint
    public static String INVALID_CHARACTER_QUESTION = "Group?ID";
    @DataPoint
    public static String INVALID_CHARACTER_EXCLAMATION = "Group!ID";
    @DataPoint
    public static String INVALID_CHARACTER_AMPERSAT = "Group@ID";
    @DataPoint
    public static String INVALID_CHARACTER_HASH = "Group#ID";
    @DataPoint
    public static String INVALID_CHARACTER_PERCENT = "Group%ID";
    @DataPoint
    public static String INVALID_CHARACTER_CARAT = "Group^ID";
    @DataPoint
    public static String INVALID_CHARACTER_EQUALS = "Group=ID";
    @DataPoint
    public static String MUST_START_ALPHANUMERIC = "-GroupID";
    @DataPoint
    public static String MUST_END_ALPHANUMERIC = "GroupID-";
    private static CreateVersionGroupPage groupPage;

    @BeforeClass
    public static void beforeClass() {
        groupPage =
                new LoginWorkFlow().signIn("admin", "admin").goToGroups()
                        .createNewGroup();
    }

    @Theory
    public void inputValidationForID(String inputText) {
        String errorMsg =
                "must start and end with letter or number, and contain only letters, numbers, underscores and hyphens.";
        // Yes reassign groupPage is necessary since JSF re-renders itself after
        // each field input and selenium is not happy with it
        groupPage =
                groupPage.clearFields().inputGroupId(inputText)
                        .inputGroupName(inputText).selectStatus("OBSOLETE")
                        .selectStatus("ACTIVE") // this is to avoid
                                                // ConcurrentModificationException
                                                // thanks to JSF!!
                        .saveGroupFailure();
        assertThat("Validation error is displayed for input:" + inputText,
                groupPage.getErrors(1), Matchers.contains(errorMsg));
    }
}
