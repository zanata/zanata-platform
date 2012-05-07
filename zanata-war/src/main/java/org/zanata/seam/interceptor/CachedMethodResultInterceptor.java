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
package org.zanata.seam.interceptor;

import java.lang.reflect.Method;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.jboss.seam.annotations.intercept.AroundInvoke;
import org.jboss.seam.annotations.intercept.Interceptor;
import org.jboss.seam.contexts.Context;
import org.jboss.seam.intercept.InvocationContext;
import org.jboss.seam.intercept.JavaBeanInterceptor;
import org.jboss.seam.intercept.OptimizedInterceptor;
import org.zanata.annotation.CachedMethodResult;

/**
 * Interceptor class that caches method return values in a given context.
 * 
 * @author camunoz@redhat.com
 */
@Interceptor(stateless = true, around = JavaBeanInterceptor.class)
public class CachedMethodResultInterceptor implements OptimizedInterceptor
{
   private static final String REG_CTX_KEY = "__CACHED_METHOD_RESULT_REG__";
   
   @Override
   @AroundInvoke
   public Object aroundInvoke(InvocationContext ic) throws Exception
   {
      Method m = ic.getMethod();
      CachedMethodResult cmr = m.getAnnotation(CachedMethodResult.class);
      if (cmr != null)
      {
         // the method result is cached => get it from the cache (or cache it if
         // absent)
         Context c = cmr.value().getContext();
         
         // Create / Get an existing cached result registry
         MultiKeyMap cachedResultReg = (MultiKeyMap)c.get( REG_CTX_KEY );
         if( cachedResultReg == null )
         {
            cachedResultReg = new MultiKeyMap();
            c.set(REG_CTX_KEY, cachedResultReg);
         }
         
         Object[] keys = new Object[2 + ic.getParameters().length];
         int i=0;
         keys[i++] = ic.getTarget().getClass().getName();     // Key by class name
         keys[i++] = m.getName();                             // Key by method name
         for( Object param :  ic.getParameters() )            // Key by each parameter called
         {
            keys[i++] = param;
         }
         
         MultiKey key = new MultiKey( keys );
         
         // Note: the key can be whatever unique value composed by the
         // Interceptor and Method. The above key could be improved
         Object result = null;
         if( cachedResultReg.containsKey( key ) )
         {
            result = cachedResultReg.get(key);
         }
         else
         {
            // result not yet in cache => cache it
            result = ic.proceed();
            cachedResultReg.put(key, result);
         }
         return result;
      }
      else
      {
         // the method is not cached => delegate call to the InvocationContext
         return ic.proceed();
      }
   }

   @Override
   public boolean isInterceptorEnabled()
   {
      return true;
   }
}
