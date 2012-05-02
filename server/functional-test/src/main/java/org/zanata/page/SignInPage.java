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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
      return PageFactory.initElements(getDriver(), pageClass);
   }
}
