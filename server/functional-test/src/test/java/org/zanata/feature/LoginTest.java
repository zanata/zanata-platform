/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.zanata.feature;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import org.zanata.action.LoginAction;
import org.zanata.page.HomePage;

import static org.hamcrest.MatcherAssert.assertThat;

@Test(enabled = false)
public class LoginTest
{
   public void canLogIn() {
      LoginAction loginAction = new LoginAction();
      HomePage homePage = loginAction.signIn("http://localhost:8080/zanata", "admin", "admin");

      assertThat(homePage, Matchers.notNullValue());
   }

}
