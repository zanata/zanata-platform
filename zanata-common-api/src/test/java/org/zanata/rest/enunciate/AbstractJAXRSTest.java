/*
 * Copyright 2013, Red Hat, Inc. and individual contributors
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
package org.zanata.rest.enunciate;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
public class AbstractJAXRSTest
{
   private static final String ENUNCIATE_PACKAGE =
       AccountResource.class.getPackage().getName();
   private static final Joiner JOINER = Joiner.on(", ");

   /**
    * if clazz has @Path, should match @Path in resource and/or enunciate;
    * for each method in clazz, if method has jaxrs annotations, they should
    * match resource method (and enunciate, if any - not implemented).
    */
   protected void checkAnnotations(Class clazz, boolean expectPath) throws ClassNotFoundException
   {
      checkClassAnnotations(clazz, expectPath);
      Class<?> resourceInterface = getResourceInterfaceFor(clazz);
      Map<String, List<List<Annotation>>> annotationsForResourceMethods = getAnnotationMapFor(resourceInterface);

      // Use getDeclaredMethods (not getMethods) to avoid methods
      // from resource interface.
      for (Method clientMethod : clazz.getDeclaredMethods())
      {
         if (!clientMethod.isAnnotationPresent(Deprecated.class))
         {
            // don't bother checking deprecated method overloads (which
            // probably don't exist in the resource interface)
            checkMethodAnnotations(clientMethod, annotationsForResourceMethods);
         }
      }
   }

   /** Checks class-level @Path annotation against other classes. */
   private void checkClassAnnotations(Class<?> clazz, boolean expectPathOnClazz) throws ClassNotFoundException
   {
      String name = clazz.getSimpleName();
      List<String> typePaths = Lists.newArrayList();

      Class<?> resourceInterface = getResourceInterfaceFor(clazz);
      addPathIfAny(typePaths, resourceInterface);
      Class<?> enunciateInterface = getEnunciateInterfaceFor(resourceInterface);
      assertThat("enunciate interface is needed for docs", enunciateInterface != null);
      addPathIfAny(typePaths, enunciateInterface);

      String path = addPathIfAny(typePaths, clazz);
      if (expectPathOnClazz)
      {
         assertThat(name+": @Path is required", path != null);
      }
      assertThat(name+": @Path must match, if present", allEqual(typePaths));

      assertThat(name+": resource and/or enunciate should have @Path (for Enunciate docs)", !typePaths.isEmpty());
      assertThat(name+": resource/enunciate @Path must match, if both are present", allEqual(typePaths));
   }

   /** Check client method against corresponding resource method. */
   private void checkMethodAnnotations(
       Method clientMethod,
       Map<String, List<List<Annotation>>> annotationsForResourceMethods)
   {
      List<List<Annotation>> clientMethodAnns = getJaxrsAnnotationsForMethodAndParams(clientMethod);
      if (!allEmptyLists(clientMethodAnns))
      {
         // if client method has no JAX-RS annotations, the resource
         // method's annotations should be used

         String methodName = getShortMethodName(clientMethod);
         String methodSig = getSignature(clientMethod);
         List<List<Annotation>> resourceMethodAnns = annotationsForResourceMethods.get(methodSig);

         assertThat(methodName+": client method must override resource method", resourceMethodAnns != null);
         assertThat(
             methodName+": client method's JAX-RS annotations (if any) must match resource method's",
             clientMethodAnns,
             equalTo(resourceMethodAnns));
      }
   }

   private String getShortMethodName(Method clientMethod)
   {
      return clientMethod.getDeclaringClass().getSimpleName()+"."+clientMethod.getName();
   }

   private Map<String, List<List<Annotation>>> getAnnotationMapFor(Class<?> resourceInterface)
   {
      // Make a map of all the resource methods' annotations.
      // Use getMethods (not getDeclaredMethods) just in case resource
      // interface extends another interface.
      Map<String, List<List<Annotation>>> annotationsForResourceMethods = Maps.newHashMap();
      for (Method method : resourceInterface.getMethods())
      {
         String methodSig = getSignature(method);
         List<List<Annotation>> jaxrsAnns = getJaxrsAnnotationsForMethodAndParams(method);
         annotationsForResourceMethods.put(methodSig, jaxrsAnns);
      }
      return annotationsForResourceMethods;
   }

   private Class<?> getEnunciateInterfaceFor(Class<?> resourceInterface) throws ClassNotFoundException
   {
      String enunciateName = ENUNCIATE_PACKAGE + "." + resourceInterface.getSimpleName();
      return Class.forName(enunciateName);
   }

   private Class<?> getResourceInterfaceFor(Class<?> aClass)
   {
      // This assumes each class implements the resource interface first
      return aClass.getInterfaces()[0];
   }

   private boolean allEmptyLists(List<List<Annotation>> clientMethodAnns)
   {
      for (List<Annotation> anns : clientMethodAnns)
      {
         if (!anns.isEmpty())
         {
            return false;
         }
      }
      return true;
   }

   // element 0 is list of jaxrs annotations for method itself
   // subsequent elements are list of jaxrs annotations for each param
   private List<List<Annotation>> getJaxrsAnnotationsForMethodAndParams(Method method)
   {
      List<List<Annotation>> jaxrsAnns = Lists.newArrayList();

      Annotation[] methodAnnotations = method.getDeclaredAnnotations();
      List<Annotation> jaxrsMethodAnns = getJaxrsAnnotations(methodAnnotations);
      jaxrsAnns.add(jaxrsMethodAnns);

      for (Annotation[] paramAnns : method.getParameterAnnotations())
      {
         List<Annotation> jaxParamAnns = getJaxrsAnnotations(paramAnns);
         jaxrsAnns.add(jaxParamAnns);
      }
      return jaxrsAnns;
   }

   private List<Annotation> getJaxrsAnnotations(Annotation[] annotations)
   {
      List<Annotation> jaxrsAnns = Lists.newArrayList();
      for (Annotation ann : annotations)
      {
         if (isJAXRSAnnotation(ann))
         {
            jaxrsAnns.add(ann);
         }
      }
      return jaxrsAnns;
   }

   private String getSignature(Method cMethod)
   {
      StringBuilder sb = new StringBuilder(cMethod.getName());
      sb.append("(");
      JOINER.appendTo(sb, cMethod.getParameterTypes());
      sb.append(")");
      return sb.toString();
   }

   private boolean isJAXRSAnnotation(Annotation ann)
   {
      return ann.annotationType().getPackage().getName().startsWith("javax.ws.rs");
   }

   private String addPathIfAny(List<String> paths, Class<?> clazz)
   {
      Path cPath = clazz.getAnnotation(Path.class);
      if (cPath != null)
      {
         paths.add(cPath.value());
         return cPath.value();
      }
      return null;
   }

   private boolean allEqual(List<String> paths)
   {
      if (paths.isEmpty())
      {
         return true;
      }
      String expected = paths.get(0);
      for (String path : paths)
      {
         if (!path.equals(expected))
         {
            return false;
         }
      }
      return true;
   }
}
