/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
package org.zanata.async;

import java.util.concurrent.ExecutionException;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.contexts.Contexts;
import org.junit.Test;
import org.zanata.ArquillianTest;


/**
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class AsyncTaskITCase extends ArquillianTest
{
   @In
   private TaskExecutor taskExecutor;

   @Override
   protected void prepareDBUnitOperations()
   {
   }

   @Test
   public void contextInheritance() throws Exception
   {
      // A given context component
      final String compName = "TEST";
      String component = "A test Component";
      Contexts.getEventContext().set(compName, component);

      // Start an asynchronous process
      AsyncTaskHandle<String> handle =
         taskExecutor.startTask(new SimpleAsyncTask<String>()
         {
            @Override
            public String call() throws Exception
            {
               return Component.getInstance(compName).toString();
            }
         });

      // Wait for it to finish and get the result
      String comp = handle.get();

      // Must be the same as the component that was inserted outside of the task
      MatcherAssert.assertThat(comp, Matchers.equalTo(component));
   }

   @Test(expected = ExecutionException.class)
   public void executionError() throws Exception
   {
      // Start an asynchronous process that throws an exception
      AsyncTaskHandle<String> handle =
         taskExecutor.startTask(new SimpleAsyncTask<String>()
         {
            @Override
            public String call() throws Exception
            {
               throw new RuntimeException("Expected Exception");
            }
         });

      // Wait for it to finish and get the result
      String result = handle.get();
   }


}
