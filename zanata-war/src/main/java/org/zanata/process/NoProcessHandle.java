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
package org.zanata.process;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jboss.seam.util.ProxyFactory;

import com.google.common.base.Defaults;

import javassist.util.proxy.MethodHandler;

/**
 * A process handle that does nothing with the updates provided. It is meant to be used
 * with services that require a process handle yet the execution context does not
 * necessarily require one to be present.
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
class NoProcessHandle extends ProcessHandle
{
   private NoProcessHandle()
   {
      super();
   }

   public static final <H extends ProcessHandle> H getNullProcessHandle(Class<H> handleType)
   {
      ProxyFactory factory = new ProxyFactory();
      factory.setSuperclass(handleType);
      factory.setHandler( new MethodHandler()
      {
         @Override
         public Object invoke(Object o, Method method, Method method1, Object[] objects) throws Throwable
         {
            // primitive types
            if( method.getReturnType().isPrimitive() && method.getReturnType() != Void.TYPE )
            {
               return Defaults.defaultValue(method.getReturnType());
            }
            else
            {
               return null;
            }
         }
      });

      try
      {
         // NB: Must provide a no-arg constructor
         return (H)factory.create(new Class[0], new Object[0]);
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }
}
