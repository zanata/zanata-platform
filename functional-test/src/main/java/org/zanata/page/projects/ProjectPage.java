/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.page.projects;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.AbstractPage;
import org.zanata.util.WebElementUtil;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectPage extends AbstractPage
{

   public static final String ACTIVE_VERSIONS_TABLE_ID = "main_content:activeIterations";
   @FindBy(id = "main_content")
   private WebElement mainContent;
   private final List<WebElement> h1;

   @FindBy(linkText = "Create Version")
   private WebElement createVersionLink;

   public ProjectPage(final WebDriver driver)
   {
      super(driver);
      //TODO this is ugly and may change in the future
      h1 = mainContent.findElements(By.tagName("h1"));
      Preconditions.checkState(h1.size() >= 2, "should have at least 2 <h1> under main content");
   }

   public String getProjectId()
   {
      return h1.get(0).getText();
   }

   public String getProjectName()
   {
      return h1.get(1).getText();
   }

   public CreateVersionPage clickCreateVersionLink()
   {
      createVersionLink.click();
      return new CreateVersionPage(getDriver());
   }

   public ProjectVersionPage goToActiveVersion(final String versionId)
   {
      WebElement activeVersions = getDriver().findElement(By.id(ACTIVE_VERSIONS_TABLE_ID));
      List<WebElement> versionLinks = activeVersions.findElements(By.tagName("a"));
      log.info("found {} active versions", versionLinks.size());

      Preconditions.checkState(!versionLinks.isEmpty(), "no version links available");
      Collection<WebElement> found = Collections2.filter(versionLinks, new Predicate<WebElement>()
      {
         @Override
         public boolean apply(WebElement input)
         {
            // the link text has line break in it
            String linkText = input.getText().replaceAll("\\n", " ");
            return linkText.matches(versionId + "\\s+Documents:.+");
         }
      });
      Preconditions.checkState(found.size() == 1, versionId + " not found");
      found.iterator().next().click();
      return new ProjectVersionPage(getDriver());
   }

   public List<String> getVersions()
   {
      List<WebElement> tables = getDriver().findElements(By.id(ACTIVE_VERSIONS_TABLE_ID));
      if (tables.isEmpty())
      {
         log.debug("no version exists for this project");
         return Collections.emptyList();
      }

      List<WebElement> versionLinks = tables.get(0).findElements(By.tagName("a"));

      List<String> versions = WebElementUtil.elementsToText(versionLinks);
      return Lists.transform(versions, new Function<String, String>()
      {
         @Override
         public String apply(String from)
         {
            String replaceLineBreak = from.replaceAll("\\n", " ");
            log.debug("version text: {}", replaceLineBreak);
            return replaceLineBreak.split("\\s")[0];
         }
      });
   }
}
