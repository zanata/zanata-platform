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
package org.zanata.webtrans.server.rpc;


import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.service.SecurityService;
import org.zanata.webtrans.shared.model.TransUnit;
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
   private TransUnit.Builder transUnitBuilder;
   @Mock private SecurityService mockSecurityService;

   @BeforeMethod
   public void beforeMethod()
   {
      MockitoAnnotations.initMocks(this);
      handler = new ReplaceTextHandler();
      handler.updateTransUnitHandler = mockUpdateTransUnitHandler;
      handler.securityServiceImpl = mockSecurityService;
      transUnitBuilder = TransUnit.Builder.newTransUnitBuilder()
            .setId(1).setResId("").setLocaleId("en-US").setVerNum(1).addSource("abc").setVerNum(0).setRowIndex(0);
   }

   @Test
   public void canReplaceTextCaseInsensitively() throws ActionException
   {
      TransUnit transUnit = transUnitBuilder.addTargets("abc", "AbC", "ABC").build();
      ReplaceText action = new ReplaceText(transUnit, "abc", "123", CASE_INSENSITIVE);

      handler.execute(action, context);

      ArgumentCaptor<UpdateTransUnit> captor = ArgumentCaptor.forClass(UpdateTransUnit.class);
      verify(mockUpdateTransUnitHandler).execute(captor.capture(), eq(context));
      List<String> expectedList = Lists.newArrayList("123", "123", "123");
      MatcherAssert.assertThat(captor.getValue().getUpdateRequests().get(0).getNewContents(), Matchers.equalTo(expectedList));
   }

   @Test
   public void canReplaceTextCaseSensitively() throws ActionException
   {
      TransUnit transUnit = transUnitBuilder.addTargets("abc", "AbC", "ABC").build();
      ReplaceText action = new ReplaceText(transUnit, "abc", "123", CASE_SENSITIVE);

      handler.execute(action, context);

      ArgumentCaptor<UpdateTransUnit> captor = ArgumentCaptor.forClass(UpdateTransUnit.class);
      verify(mockUpdateTransUnitHandler).execute(captor.capture(), eq(context));
      List<String> expectedList = Lists.newArrayList("123", "AbC", "ABC");
      MatcherAssert.assertThat(captor.getValue().getUpdateRequests().get(0).getNewContents(), Matchers.equalTo(expectedList));
   }

   @Test(expectedExceptions = {ActionException.class})
   public void willThrowExceptionIfSearchTextIsEmpty() throws ActionException
   {
      TransUnit transUnit = transUnitBuilder.build();
      ReplaceText action = new ReplaceText(transUnit, "", "123", CASE_SENSITIVE);
      handler.execute(action, context);
   }

   @Test(expectedExceptions = {ActionException.class})
   public void willThrowExceptionIfReplaceTextIsEmpty() throws ActionException
   {
      TransUnit transUnit = transUnitBuilder.build();
      ReplaceText action = new ReplaceText(transUnit, "abc", null, CASE_SENSITIVE);
      handler.execute(action, context);
   }
}
