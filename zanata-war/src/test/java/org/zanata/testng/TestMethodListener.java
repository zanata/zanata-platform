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
package org.zanata.testng;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;
import org.zanata.testng.annotations.Disabled;

public class TestMethodListener implements IInvokedMethodListener
{

   @Override
   public void beforeInvocation(IInvokedMethod method, ITestResult testResult)
   {
      if( method.getTestMethod().getMethod().isAnnotationPresent(Disabled.class) )
      {
         Reporter.setCurrentTestResult(testResult);
         Reporter.log( "Skip reason: " + method.getTestMethod().getMethod().getAnnotation(Disabled.class).reason(), true );
         throw new SkipException("Test method skipped. See report output for reason.");
      }
   }
   
   @Override
   public void afterInvocation(IInvokedMethod method, ITestResult testResult)
   {
      // TODO Auto-generated method stub
   }
   
}
