package org.zanata.page.account;


import java.util.Map;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.zanata.page.AbstractPage;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Damian Jansen <a href="mailto:djansen@redhat.com">djansen@redhat.com</a>
 */
@Slf4j
public class RegisterPage extends AbstractPage
{

   @FindBy(id = "registerForm:nameField:name")
   private WebElement nameField;

   @FindBy(id = "registerForm:emailField:email")
   private WebElement emailField;

   @FindBy(id = "registerForm:usernameField:username")
   private WebElement usernameField;

   @FindBy(id = "registerForm:passwordField:password")
   private WebElement passwordField;

   @FindBy(id = "registerForm:passwordConfirmField:passwordConfirm")
   private WebElement confirmPasswordField;

   @FindBy(id = "registerForm:captcha:verifyCaptcha")
   private WebElement captchaField;

   @FindBy(id = "registerForm:agreedToTerms:agreedToTerms")
   private WebElement termsCheckbox;

   @FindBy(id = "registerForm:registerButton")
   private WebElement registerButton;

   public RegisterPage(WebDriver driver)
   {
      super(driver);
   }

   public RegisterPage enterName(String name)
   {
      nameField.sendKeys(name);
      return new RegisterPage(getDriver());
   }

   public RegisterPage enterUserName(String userName)
   {
      usernameField.sendKeys(userName);
      return new RegisterPage(getDriver());
   }

   public RegisterPage enterEmail(String email)
   {
      emailField.sendKeys(email);
      return new RegisterPage(getDriver());
   }

   public RegisterPage enterPassword(String password)
   {
      passwordField.sendKeys(password);
      return new RegisterPage(getDriver());
   }

   public RegisterPage enterConfirmPassword(String confirmPassword)
   {
      confirmPasswordField.sendKeys(confirmPassword);
      return new RegisterPage(getDriver());
   }

   public RegisterPage enterCaptcha(String captcha)
   {
      captchaField.sendKeys(captcha);
      return new RegisterPage(getDriver());
   }

   public RegisterPage clickTerms()
   {
      termsCheckbox.click();
      return new RegisterPage(getDriver());
   }

   public AbstractPage register()
   {
      registerButton.click();
      return new AbstractPage(getDriver());
   }

   public RegisterPage registerFailure()
   {
      registerButton.click();
      return new RegisterPage(getDriver());
   }

   public RegisterPage clearFields()
   {
      nameField.clear();
      emailField.clear();
      usernameField.clear();
      passwordField.clear();
      confirmPasswordField.clear();
      captchaField.clear();
      return new RegisterPage(getDriver());
   }

   /*
      Pass in a map of strings, to be entered into the registration fields.
      Fields: name, email, username, password, confirmpassword, captcha
    */
   public RegisterPage setFields(Map<String, String> fields)
   {
      clearFields();
      enterName(fields.get("name"));
      enterEmail(fields.get("email"));
      enterUserName(fields.get("username"));
      enterPassword(fields.get("password"));
      enterConfirmPassword(fields.get("confirmpassword"));
      enterCaptcha(fields.get("captcha"));
      return new RegisterPage(getDriver());
   }
}
