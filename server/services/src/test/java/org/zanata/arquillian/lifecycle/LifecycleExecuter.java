/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.zanata.arquillian.lifecycle;

import java.lang.reflect.Method;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jboss.arquillian.container.spi.event.container.AfterDeploy;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.AfterUnDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeDeploy;
import org.jboss.arquillian.container.spi.event.container.BeforeSetup;
import org.jboss.arquillian.container.spi.event.container.BeforeStart;
import org.jboss.arquillian.container.spi.event.container.BeforeUnDeploy;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.test.spi.TestClass;

/**
 * LifecycleExecuter uses Arquillian lifecycle event observers to provide
 * various observable deployment events (see the annotations in the package
 * org.zanata.arquillian.lifecycle.api)
 * @see org.zanata.arquillian.lifecycle.api
 * @see LifecycleArquillian
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @author <a href="mailto:sflaniga@redhat.com">Sean Flanigan</a>
 */
class LifecycleExecuter
{
    // Yes, this is nasty.
    // See https://developer.jboss.org/thread/273201 if you want to try 27
    // classes and interfaces instead.
    private static @Nullable TestClass currentTestClass;

    public static void withTestClass(TestClass testClass, Runnable runnable) {
        currentTestClass = new TestClass(testClass.getJavaClass());
        try {
            runnable.run();
        } finally {
            currentTestClass = null;
        }
    }

    private static @Nonnull TestClass getCurrentTestClass() {
        if (currentTestClass == null) {
            throw new RuntimeException(
                    "currentTestClass has not been set. See withTestClass.");
        }
        return currentTestClass;
    }

    public void executeBeforeSetup(@Observes BeforeSetup event)
    {
        execute(
                getCurrentTestClass().getMethods(
                        org.zanata.arquillian.lifecycle.api.BeforeSetup.class));
    }

    public void executeBeforeStart(@Observes BeforeStart event)
    {
        execute(
                getCurrentTestClass().getMethods(
                        org.zanata.arquillian.lifecycle.api.BeforeStart.class));
    }

    public void executeAfterStart(@Observes AfterStart event)
    {
        execute(
                getCurrentTestClass().getMethods(
                        org.zanata.arquillian.lifecycle.api.AfterStart.class));
    }

    public void executeBeforeDeploy(@Observes BeforeDeploy event, TestClass testClass)
   {
      execute(
            testClass.getMethods(
                  org.zanata.arquillian.lifecycle.api.BeforeDeploy.class));
   }

   public void executeAfterDeploy(@Observes AfterDeploy event, TestClass testClass)
   {
      execute(
            testClass.getMethods(
                  org.zanata.arquillian.lifecycle.api.AfterDeploy.class));
   }

   public void executeBeforeUnDeploy(@Observes BeforeUnDeploy event, TestClass testClass)
   {
      execute(
            testClass.getMethods(
                  org.zanata.arquillian.lifecycle.api.BeforeUnDeploy.class));
   }

   public void executeAfterUnDeploy(@Observes AfterUnDeploy event, TestClass testClass)
   {
      execute(
            testClass.getMethods(
                  org.zanata.arquillian.lifecycle.api.AfterUnDeploy.class));
   }

   private void execute(Method[] methods)
   {
       if (methods == null) {
           return;
       }
       for (Method method : methods) {
           try {
               method.invoke(null);
           } catch (Exception e) {
               throw new RuntimeException(
                       "Could not execute lifecycle method: " + method, e);
           }
       }
   }
}
