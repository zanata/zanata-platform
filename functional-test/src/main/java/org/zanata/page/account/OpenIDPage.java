package org.zanata.page.account;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.BasePage;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
public class OpenIDPage extends BasePage
{

   @FindBy(id="Email")
   private WebElement emailField;

   @FindBy(id="signIn")
   private WebElement signInButton;

   public OpenIDPage(WebDriver driver)
   {
      super(driver);
   }

   public OpenIDPage enterEmail(String email)
   {
      emailField.sendKeys(email);
      return new OpenIDPage(getDriver());
   }

   public GoogleAccountPage clickSignIn()
   {
      signInButton.click();
      return new GoogleAccountPage(getDriver());
   }

}
