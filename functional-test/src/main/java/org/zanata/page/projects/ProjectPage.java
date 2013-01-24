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
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectPage extends AbstractPage
{

   @FindBy(id = "main_content")
   private WebElement mainContent;

   @FindBy(id = "loggedIn_body")
   private WebElement loggedInBody;

   public ProjectPage(final WebDriver driver)
   {
      super(driver);
   }

   @SuppressWarnings("unused")
   public String getProjectId()
   {
      List<String> breadcrumbs = getBreadcrumbs();
      return breadcrumbs.get(breadcrumbs.size() - 1);
   }

   @SuppressWarnings("unused")
   public String getProjectName()
   {
      return getTitle().replaceAll("Zanata:", "");
   }

   public CreateVersionPage clickCreateVersionLink()
   {
      loggedInBody.findElement(By.id("addIterationLink")).click();
      return new CreateVersionPage(getDriver());
   }

   public ProjectVersionPage goToVersion(final String versionId)
   {
      List<WebElement> versionLinks = getDriver().findElements(By.className("version_link"));
      log.info("found {} active versions", versionLinks.size());

      Preconditions.checkState(!versionLinks.isEmpty(), "no version links available");
      Optional<WebElement> found = Iterables.tryFind(versionLinks, new Predicate<WebElement>()
      {
         @Override
         public boolean apply(WebElement input)
         {
            return input.getText().contains(versionId);
         }
      });
      Preconditions.checkState(found.isPresent(), versionId + " not found");
      found.get().click();
      return new ProjectVersionPage(getDriver());
   }

   public List<String> getVersions()
   {
      List<WebElement> versionLinks = getDriver().findElements(By.className("version_link"));
      if (versionLinks.isEmpty())
      {
         log.debug("no version exists for this project");
         return Collections.emptyList();
      }

      return WebElementUtil.elementsToText(versionLinks);
   }
}
