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
import org.zanata.workflow.LoginWorkFlow;
import org.zanata.page.HomePage;

import static org.hamcrest.MatcherAssert.assertThat;

public class LoginTest
{
   @Test
   public void canLogIn() {
      LoginWorkFlow loginAction = new LoginWorkFlow();
      HomePage homePage = loginAction.signIn("admin", "admin");

      assertThat(homePage.getTitle(), Matchers.equalTo("Zanata:Home"));
      assertThat(homePage.hasLoggedIn(), Matchers.is(true));
      assertThat(homePage.loggedInAs(), Matchers.equalTo("admin"));

      //try to log in again won't cause any problem
      loginAction.signIn("admin", "admin");
      assertThat(homePage.loggedInAs(), Matchers.equalTo("admin"));
   }

}
