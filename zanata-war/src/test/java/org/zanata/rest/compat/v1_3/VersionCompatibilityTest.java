/*
 * Copyright 2010, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.rest.compat.v1_3;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

import org.testng.annotations.Test;
import org.zanata.ZanataCompatibilityTest;
import org.zanata.v1_3.rest.client.IVersion;
import org.zanata.v1_3.rest.dto.VersionInfo;

@Test(groups = {"compatibility-tests", "seam-tests"} )
public class VersionCompatibilityTest extends ZanataCompatibilityTest
{

   @Override
   protected void prepareDBUnitOperations()
   {
   }

   @Test
   public void getVersionXml()
   {
      IVersion versionClient = super.createProxy(IVersion.class, "/version");
      VersionInfo versionInfo = versionClient.get();
      
      assertThat( versionInfo.getVersionNo(), notNullValue() );
      assertThat( versionInfo.getBuildTimeStamp(), notNullValue() );
   }
}
