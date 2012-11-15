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
package org.zanata.page;

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Predicate;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SignInPage extends AbstractPage
{
   private static final Logger LOGGER = LoggerFactory.getLogger(SignInPage.class);

   @FindBy(id = "login:usernameField:username")
   private WebElement usernameField;

   @FindBy(id = "login:passwordField:password")
   private WebElement passwordField;

   @FindBy(id = "login:Sign_in")
   private WebElement signInButton;

   public SignInPage(final WebDriver driver)
   {
      super(driver);
   }

   public <P extends AbstractPage> P signInAndGoToPage(String username, String password, Class<P> pageClass)
   {
      LOGGER.info("log in as username: {}", username);
      usernameField.sendKeys(username);
      passwordField.sendKeys(password);
      signInButton.click();
      try
      {
         waitForSeconds(getDriver(), 3).until(new Predicate<WebDriver>()
         {
            @Override
            public boolean apply(WebDriver driver)
            {
               List<WebElement> signOutLink = driver.findElements(By.id("Sign_out"));
               return signOutLink.size() == 1;
            }
         });
      }
      catch (TimeoutException e)
      {
         log.error("timeout on login. If you are running tests manually with cargo.wait, you probably forget to create the user admin/admin. See ManualRunHelper.");
         throw e;
      }
      return PageFactory.initElements(getDriver(), pageClass);
   }
}
