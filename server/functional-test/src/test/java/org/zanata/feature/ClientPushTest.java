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

import java.io.IOException;

import org.hamcrest.Matchers;
import org.testng.annotations.Test;
import org.zanata.workflow.ClientPushWorkFlow;

import static org.hamcrest.MatcherAssert.*;

@Test(dependsOnGroups = {"web-setup"})
public class ClientPushTest
{
   @Test
   public void canPush() throws IOException
   {
      ClientPushWorkFlow clientPushWorkFlow = new ClientPushWorkFlow();
      int exitCode = clientPushWorkFlow.mvnPush("plural");

      assertThat(exitCode, Matchers.equalTo(0));
   }
}
