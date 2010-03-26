package org.fedorahosted.flies.selenium;

import com.thoughtworks.selenium.*;
import java.util.regex.Pattern;

public class ExampleTest extends SeleneseTestCase {
    public void setUp() throws Exception {
        setUp("http://www.google.com/", "*firefox");
    }
      public void testNew() throws Exception {
          selenium.open("/");
          selenium.type("q", "selenium rc");
          selenium.click("btnG");
          selenium.waitForPageToLoad("30000");
          assertTrue(selenium.isTextPresent("Results * for selenium rc"));
    }
}
