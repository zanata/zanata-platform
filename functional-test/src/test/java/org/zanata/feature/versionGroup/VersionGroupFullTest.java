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

import java.util.*;

import org.hamcrest.Matchers;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.*;
import org.junit.experimental.categories.Category;
import org.zanata.feature.BasicAcceptanceTest;
import org.zanata.feature.DetailedTest;
import org.zanata.page.HomePage;
import org.zanata.page.groups.VersionGroupPage;
import org.zanata.page.groups.VersionGroupsPage;
import org.zanata.page.groups.CreateVersionGroupPage;
import org.zanata.util.ResetDatabaseRule;
import org.zanata.workflow.LoginWorkFlow;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
@Category(DetailedTest.class)
public class VersionGroupFullTest
{
   @ClassRule
   public static ResetDatabaseRule resetDatabaseRule = new ResetDatabaseRule();
   private HomePage homePage;

   @Before
   public void before()
   {
      homePage = new LoginWorkFlow().signIn("admin", "admin");
   }

   @Test
   @Category(BasicAcceptanceTest.class)
   public void createABasicGroup()
   {
      String groupID = "basic-group";
      String groupName = "A Basic Group";

      CreateVersionGroupPage createVersionGroupPage = homePage.goToGroups().createNewGroup();
      createVersionGroupPage.inputGroupId(groupID);
      createVersionGroupPage.inputGroupName(groupName);
      createVersionGroupPage.inputGroupDescription("A basic group can be saved");
      createVersionGroupPage.selectStatus("ACTIVE");
      VersionGroupsPage versionGroupsPage = createVersionGroupPage.saveGroup();
      assertThat("Group was created", versionGroupsPage.getGroupNames().contains(groupName));
      VersionGroupPage groupView = versionGroupsPage.goToGroup(groupName);
      assertThat("The group is displayed", groupView.getTitle(), Matchers.equalTo("Zanata: Groups:".concat(groupName)));
   }

   @Test
   public void inputValidationForID()
   {
      String errorMsg = "must start and end with letter or number, and contain only letters, numbers, underscores and hyphens.";
      for (Map.Entry<String, String> entry : inputValidationForIDData().entrySet())
      {
         VersionGroupFullTest.log.info("Test " + entry.getKey() + ":" + entry.getValue());
         VersionGroupsPage versionGroupsPage = homePage.goToGroups();
         CreateVersionGroupPage groupPage = versionGroupsPage.createNewGroup();
         groupPage.inputGroupId(entry.getValue()).inputGroupName(entry.getValue());
         groupPage.saveGroupFailure();
         assertThat("Validation error is displayed for " + entry.getKey(), groupPage.getErrors().contains(errorMsg));
      }
   }

   @Test
   public void requiredFields()
   {
      String errorMsg = "value is required";
      String groupID = "verifyRequiredFieldsGroupID";
      String groupName = "verifyRequiredFieldsGroupName";

      CreateVersionGroupPage groupPage = homePage.goToGroups().createNewGroup().saveGroupFailure();
      assertThat("The two errors are value is required", groupPage.getErrors(),Matchers.contains(errorMsg, errorMsg));

      groupPage.clearFields();
      groupPage.inputGroupName(groupName);
      groupPage.saveGroupFailure();
      assertThat("The value required error shown", groupPage.getErrors(), Matchers.contains(errorMsg));

      groupPage = groupPage.clearFields().inputGroupId(groupID).saveGroupFailure();
      assertThat("The value required error shown", groupPage.getErrors(), Matchers.contains(errorMsg));
   }

   @Test
   public void groupIDFieldSize()
   {
      String errorMsg = "size must be between 1 and 40";
      String groupID = "abcdefghijklmnopqrstuvwxyzabcdefghijklmno";
      String groupName = "verifyIDFieldSizeName";

      CreateVersionGroupPage groupPage = homePage.goToGroups().createNewGroup();
      groupPage.inputGroupId(groupID).inputGroupName(groupName).saveGroupFailure();
      assertThat("Invalid length error is shown", groupPage.getErrors(), Matchers.contains(errorMsg));

      groupPage.clearFields();
      groupID = groupID.substring(0, 40);
      assertThat("GroupID is now 40 characters long", groupID.length(), Matchers.equalTo(40));
      groupPage.inputGroupId(groupID).inputGroupName(groupName);
      VersionGroupsPage versionGroupsPage = groupPage.saveGroup();
      assertThat("A group ID of 40 chars is valid", versionGroupsPage.getGroupNames(), Matchers.hasItem(groupName));
   }

   @Test
   public void groupDescriptionFieldSize()
   {
      String errorMsg = "size must be between 0 and 100";
      String groupID = "verifyDescriptionFieldSizeID";
      String groupName = "verifyDescriptionFieldSizeName";
      String groupDescription =
         "This text is to test that the description field takes no more than exactly 100 characters - actually.";

      assertThat("Description length is greater than 100 characters", groupDescription.length(), Matchers.equalTo(101));
      CreateVersionGroupPage groupPage = homePage.goToGroups().createNewGroup();
      groupPage.inputGroupId(groupID).inputGroupName(groupName).inputGroupDescription(groupDescription);
      groupPage.saveGroupFailure();
      assertThat("Invalid length error is shown", groupPage.getErrors(), Matchers.contains(errorMsg));

      groupPage.clearFields();
      groupDescription = groupDescription.substring(0, 100);
      assertThat("Description length is now 100 characters", groupDescription.length(), Matchers.equalTo(100));
      groupPage.inputGroupId("verifyDescriptionFieldSizeID").inputGroupName(groupName);
      VersionGroupsPage verGroupsPage = groupPage.inputGroupDescription(groupDescription).saveGroup();
      assertThat("A group description of 100 chars is valid", verGroupsPage.getGroupNames(), Matchers.hasItem(groupName));

   }

   private LinkedHashMap<String, String> inputValidationForIDData()
   {
      LinkedHashMap<String, String> inputData = new LinkedHashMap<String, String>(100);
      inputData.put("Invalid char |", "Group|ID");
      inputData.put("Invalid char /", "Group/ID");
      inputData.put("Invalid char ", "Group\\ID");
      inputData.put("Invalid char +", "Group+ID");
      inputData.put("Invalid char *", "Group*ID");
      inputData.put("Invalid char |", "Group|ID");
      inputData.put("Invalid char (", "Group(ID");
      inputData.put("Invalid char )", "Group)ID");
      inputData.put("Invalid char $", "Group$ID");
      inputData.put("Invalid char [", "Group[ID");
      inputData.put("Invalid char ]", "Group]ID");
      inputData.put("Invalid char :", "Group:ID");
      inputData.put("Invalid char ;", "Group;ID");
      inputData.put("Invalid char '", "Group'ID");
      inputData.put("Invalid char ,", "Group,ID");
      inputData.put("Invalid char ?", "Group?ID");
      inputData.put("Invalid char !", "Group!ID");
      inputData.put("Invalid char @", "Group@ID");
      inputData.put("Invalid char #", "Group#ID");
      inputData.put("Invalid char %", "Group%ID");
      inputData.put("Invalid char ^", "Group^ID");
      inputData.put("Invalid char =", "Group=ID");
      inputData.put("Must start with alphanumeric", "-GroupID");
      inputData.put("Must end with alphanumeric", "GroupID-");

      /* BUG id=973509 - remove/uncomment depending on outcome
      inputData.put("Invalid char .", "Group.ID");
      inputData.put("Invalid char {", "Group{ID");
      inputData.put("Invalid char }", "Group}ID");
      */

      return inputData;
   }

}
