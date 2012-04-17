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
package org.zanata.webtrans.server.rpc;


import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.zanata.common.ContentState;
import org.zanata.common.LocaleId;
import org.zanata.webtrans.shared.model.TransUnit;
import org.zanata.webtrans.shared.model.TransUnitId;
import org.zanata.webtrans.shared.rpc.ReplaceText;
import org.zanata.webtrans.shared.rpc.UpdateTransUnit;
import com.google.common.collect.Lists;

import net.customware.gwt.dispatch.server.ExecutionContext;
import net.customware.gwt.dispatch.shared.ActionException;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

@Test(groups = "unit-test")
public class ReplaceTextHandlerTest
{
   public static final boolean CASE_INSENSITIVE = false;
   public static final boolean CASE_SENSITIVE = true;
   private ReplaceTextHandler handler;
   @Mock private UpdateTransUnitHandler mockUpdateTransUnitHandler;
   @Mock private ExecutionContext context;

   @BeforeClass
   public void setUp()
   {
      MockitoAnnotations.initMocks(this);
      handler = new ReplaceTextHandler();
      handler.updateTransUnitHandler = mockUpdateTransUnitHandler;
   }

   @Test
   public void canReplaceText() throws ActionException
   {
      TransUnit transUnit = new TransUnit(new TransUnitId(1L), "1", new LocaleId("en-US"), true, Lists.newArrayList("abc"), "comment", Lists.newArrayList("abc", "AbC", "ABC"), ContentState.New, null, null, null, 0);
      ReplaceText action = new ReplaceText(transUnit, "abc", "123", CASE_INSENSITIVE);

      handler.execute(action, context);

      ArgumentCaptor<UpdateTransUnit> captor = ArgumentCaptor.forClass(UpdateTransUnit.class);
      verify(mockUpdateTransUnitHandler).execute(captor.capture(), eq(context));
      MatcherAssert.assertThat(captor.getValue().getContents(), Matchers.equalTo(Lists.newArrayList("123", "123", "123")));
   }
}
