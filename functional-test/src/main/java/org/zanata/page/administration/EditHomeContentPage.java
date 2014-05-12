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
package org.zanata.page.administration;

import com.google.common.base.Function;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;
import org.zanata.page.utility.HomePage;

/**
 * @author Damian Jansen <a
 *         href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class EditHomeContentPage extends BasePage {

    @FindBy(id = "homeContentForm:update")
    private WebElement updateButton;

    @FindBy(id = "homeContentForm:cancel")
    private WebElement cancelButton;

    public EditHomeContentPage(final WebDriver driver) {
        super(driver);
    }

    public EditHomeContentPage enterText(String text) {
        // Switch to the CKEditor frame
        getDriver().switchTo().frame(waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver driver) {
                return getDriver().findElement(
                        By.id("cke_contents_homeContentForm:homeContent:inp"))
                        .findElement(By.tagName("iframe"));
            }
        }));

        WebElement textEdit = waitForTenSec().until(new Function<WebDriver, WebElement>() {
            @Override
            public WebElement apply(WebDriver driver) {
                System.out.println(getDriver().findElements(By.tagName("body")).size());
                return getDriver().findElement(By.tagName("body"));
            }
        });
        textEdit.sendKeys(text);
        // Switch back!
        getDriver().switchTo().defaultContent();
        return new EditHomeContentPage(getDriver());
    }

    public HomePage update() {
        updateButton.click();
        return new HomePage(getDriver());
    }

    public HomePage cancelUpdate() {
        updateButton.click();
        return new HomePage(getDriver());
    }
}
