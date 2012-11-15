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
package org.zanata.rest.service;

import org.dbunit.operation.DatabaseOperation;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.seam.security.AuthorizationException;
import org.jboss.seam.security.Credentials;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.Test;
import org.zanata.ZanataRestTest;
import org.zanata.rest.dto.CopyTransStatus;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;
import org.zanata.service.impl.CopyTransServiceImpl;
import org.zanata.service.impl.LocaleServiceImpl;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class CopyTransRestTest extends ZanataRestTest
{
   @Mock
   ZanataIdentity mockIdentity;
   SeamAutowire seam = SeamAutowire.instance();

   @Override
   protected void prepareResources()
   {
      MockitoAnnotations.initMocks(this);
      when(mockIdentity.hasPermission(anyString(), anyString(), anyVararg())).thenReturn(true);
      when(mockIdentity.hasPermission(anyString(), anyString())).thenReturn(true);
      Credentials credentials = new Credentials();
      credentials.setUsername("testuser");
      when(mockIdentity.getCredentials()).thenReturn(credentials);

      seam.reset();
      seam.ignoreNonResolvable()
            .use("session", getSession())
            .use("identity", mockIdentity)
            .useImpl(CopyTransServiceImpl.class)
            .useImpl(LocaleServiceImpl.class);

      CopyTransResource copyTransResource = seam.autowire(CopyTransResourceService.class);

      resources.add(copyTransResource);
   }

   @Override
   protected void prepareDBUnitOperations()
   {
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/LocalesData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/ProjectsData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/AccountData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));
      beforeTestOperations.add(new DataSetOperation("org/zanata/test/model/TextFlowTestData.dbunit.xml", DatabaseOperation.CLEAN_INSERT));

      afterTestOperations.add(new DataSetOperation("org/zanata/test/model/ClearAllTables.dbunit.xml", DatabaseOperation.DELETE_ALL));
   }

   @Test
   public void startCopyTrans()
   {
      CopyTransResource copyTransResource = getClientRequestFactory().createProxy(CopyTransResource.class);

      copyTransResource.startCopyTrans("sample-project", "1.0", "/my/path/document.txt");
      verify(mockIdentity).getCredentials();
   }

   @Test
   public void startCopyTransAndCheckStatus()
   {
      CopyTransResource copyTransResource = getClientRequestFactory().createProxy(CopyTransResource.class);

      copyTransResource.startCopyTrans("sample-project", "1.0", "/my/path/document.txt");
      verify(mockIdentity).getCredentials();

      CopyTransStatus status = copyTransResource.getCopyTransStatus("sample-project", "1.0", "/my/path/document.txt");
      assertThat(status, notNullValue());
      verify(mockIdentity, atLeast(1)).checkPermission(eq("copy-trans"), anyVararg());
   }

   @Test
   public void copyTransForUnknownDocument()
   {
      CopyTransResource copyTransResource = getClientRequestFactory().createProxy(CopyTransResource.class);

      try
      {
         copyTransResource.startCopyTrans("sample-project", "1.0", "/an/inexisting/document.txt");
         assertThat("startCopyTrans should have returned 404 in the form of an exception.", false);
      }
      catch (ClientResponseFailure failure)
      {
         assertThat(failure.getResponse().getStatus(), is(404));
      }
   }

   @Test
   public void unauthorizedStartCopyTrans()
   {
      CopyTransResource copyTransResource = getClientRequestFactory().createProxy(CopyTransResource.class);
      doThrow(new AuthorizationException("Expected Exception")).when(mockIdentity).checkPermission(eq("copy-trans"), anyVararg());

      try
      {
         copyTransResource.startCopyTrans("sample-project", "1.0", "/my/path/document.txt");
         assertThat("startCopyTrans should have returned 401 in the form of an exception.", false);
      }
      catch (ClientResponseFailure failure)
      {
         assertThat(failure.getResponse().getStatus(), is(401));
      }
   }

   @Test
   public void unauthorizedCopyTransStatus()
   {
      CopyTransResource copyTransResource = getClientRequestFactory().createProxy(CopyTransResource.class);
      doThrow(new AuthorizationException("Expected Exception")).when(mockIdentity).checkPermission(eq("copy-trans"), anyVararg());

      try
      {
         copyTransResource.getCopyTransStatus("sample-project", "1.0", "/my/path/document.txt");
         assertThat("getCopyTransStatus should have returned 401 in the form of an exception.", false);
      }
      catch (ClientResponseFailure failure)
      {
         assertThat(failure.getResponse().getStatus(), is(401));
      }
   }
   
}
