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
package org.zanata.seam;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import org.apache.commons.lang.ArrayUtils;
import org.jboss.seam.Component;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Logging;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Helps with Auto-wiring of Seam components for integrated tests without the need for a full Seam
 * environment.
 * It's a singleton class that upon first use will change the way Seam's {@link org.jboss.seam.Component} class
 * works by returning its own auto-wired components.
 *
 * Supports components injected using:
 * {@link In}
 * {@link Logger}
 * {@link org.jboss.seam.Component#getInstance(String)} and similar methods...
 * and that have no-arg constructors
 *
 * @author Carlos Munoz <a href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
public class SeamAutowire
{
   private static SeamAutowire instance;

   private Map<String, Object> namedComponents = new HashMap<String, Object>();

   //private Map<Class, Object> classComponents = new HashMap<Class, Object>();

   private boolean ignoreNonResolvable;


   protected SeamAutowire()
   {
   }

   public static final SeamAutowire instance()
   {
      if(instance == null)
      {
         instance = new SeamAutowire();
         instance.rewireSeamComponents();
      }
      return instance;
   }

   public SeamAutowire reset()
   {
      this.ignoreNonResolvable = false;
      this.namedComponents.clear();
      return this;
   }

   public SeamAutowire use(String name, Object component)
   {
      this.addComponentInstance(name, component);
      return this;
   }

   public SeamAutowire ignoreNonResolvable()
   {
      this.ignoreNonResolvable = true;
      return this;
   }

   public Object getComponent(String name)
   {
      if( this.namedComponents.containsKey(name) )
      {
         return this.namedComponents.get(name);
      }
      return null;
   }

   public <T> T autowire(Class<T> componentClass)
   {
      // Create a new component instance (must have a no-arg constructor per Seam spec)
      T component;
      try
      {
         component = componentClass.newInstance();
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException("Could not auto-wire component of type " + componentClass.getName(), e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException("Could not auto-wire component of type " + componentClass.getName(), e);
      }

      return this.autowire(component);
   }

   public <T> T autowire(T component)
   {
      Class<T> componentClass = (Class<T>)component.getClass();

      for( Field compField : getAllComponentFields(component) )
      {
         compField.setAccessible(true);

         // Other annotated component
         if( compField.getAnnotation(In.class) != null )
         {
            Object fieldVal = null;
            String compName = getComponentName(compField);

            // autowire the component if not done yet
            if( !namedComponents.containsKey(compName) )
            {
               try
               {
                  Object newComponent = autowire(compField.getType());
                  this.addComponentInstance(compName, newComponent);
               }
               catch (RuntimeException e)
               {
                  if( ignoreNonResolvable )
                  {
                     // TODO warn
                  }
                  else
                  {
                     throw e;
                  }
               }
            }

            fieldVal = namedComponents.get( compName );

            try
            {
               compField.set( component, fieldVal);
            }
            catch (IllegalAccessException e)
            {
               throw new RuntimeException("Could not auto-wire field " + compField.getName() +
                     " in component of type " + componentClass.getName(), e);
            }
         }
         // Logs
         else if( compField.getAnnotation(Logger.class) != null )
         {
            try
            {
               compField.set( component, Logging.getLog(compField.getType()));
            }
            catch (IllegalAccessException e)
            {
               throw new RuntimeException("Could not auto-wire field " + compField.getName() +
                     " in component of type " + componentClass.getName(), e);
            }
         }
      }

      // Resolve injected components using methods
      for( Method compMethod : getAllComponentMethods(component) )
      {
         compMethod.setAccessible(true);

         // Other annotated component
         if( compMethod.getAnnotation(In.class) != null )
         {
            Object fieldVal = null;
            String compName = getComponentName(compMethod);

            // autowire the component if not done yet
            if( !namedComponents.containsKey(compName) )
            {
               try
               {
                  Object newComponent = autowire(compMethod.getParameterTypes()[0]);
                  this.addComponentInstance(compName, newComponent);
               }
               catch (RuntimeException e)
               {
                  if( ignoreNonResolvable )
                  {
                     // TODO warn
                  }
                  else
                  {
                     throw e;
                  }
               }
            }

            fieldVal = namedComponents.get(compName);

            try
            {
               compMethod.invoke(component, fieldVal);
            }
            catch (InvocationTargetException e)
            {
               throw new RuntimeException("Could not auto-wire field " + compMethod.getName() +
                     " in component of type " + componentClass.getName(), e);
            }
            catch (IllegalAccessException e)
            {
               throw new RuntimeException("Could not auto-wire field " + compMethod.getName() +
                     " in component of type " + componentClass.getName(), e);
            }
         }
         // Logs
         else if( compMethod.getAnnotation(Logger.class) != null )
         {
            try
            {
               compMethod.invoke( component, Logging.getLog(compMethod.getParameterTypes()[0]));
            }
            catch(InvocationTargetException e)
            {
               throw new RuntimeException("Could not auto-wire field " + compMethod.getName() +
                     " in component of type " + componentClass.getName(), e);
            }
            catch (IllegalAccessException e)
            {
               throw new RuntimeException("Could not auto-wire field " + compMethod.getName() +
                     " in component of type " + componentClass.getName(), e);
            }
         }
      }

      return component;
   }

   private void addComponentInstance(String name, Object compInst)
   {
      this.namedComponents.put(name, compInst);
      //this.classComponents.put(compInst.getClass(), compInst);
   }

   private void rewireSeamComponents()
   {
      try
      {
         ClassPool pool = ClassPool.getDefault();
         CtClass cls = pool.get("org.jboss.seam.Component");

         // Commonly used CtClasses
         final CtClass stringCls = pool.get("java.lang.String");
         final CtClass booleanCls = pool.get("boolean");
         final CtClass objectCls = pool.get("java.lang.Object");
         final CtClass scopeTypeCls = pool.get("org.jboss.seam.ScopeType");

         // Replace Component's method bodies with the ones in AutowireComponent
         CtMethod methodToReplace = cls.getDeclaredMethod("getInstance", new CtClass[]{stringCls, booleanCls, booleanCls});
         methodToReplace.setBody(
               pool.get(AutowireComponent.class.getName()).getDeclaredMethod("getInstance", new CtClass[]{stringCls, booleanCls, booleanCls}),
               null);

         methodToReplace = cls.getDeclaredMethod("getInstance", new CtClass[]{stringCls, scopeTypeCls, booleanCls, booleanCls});
         methodToReplace.setBody(
               pool.get(AutowireComponent.class.getName()).getDeclaredMethod("getInstance", new CtClass[]{stringCls, scopeTypeCls, booleanCls, booleanCls}),
               null);

         methodToReplace = cls.getDeclaredMethod("getInstance", new CtClass[]{stringCls, booleanCls, booleanCls, objectCls});
         methodToReplace.setBody(
               pool.get(AutowireComponent.class.getName()).getDeclaredMethod("getInstance", new CtClass[]{stringCls, booleanCls, booleanCls, objectCls}),
               null);

         cls.toClass();
      }
      catch (NotFoundException e)
      {
         throw new RuntimeException("Problem rewiring Seam's Component class", e);
      }
      catch (CannotCompileException e)
      {
         throw new RuntimeException("Problem rewiring Seam's Component class", e);
      }

   }

   private static Field[] getAllComponentFields(Object component)
   {
      Field[] fields = component.getClass().getDeclaredFields();
      Class<?> superClass = component.getClass().getSuperclass();

      while(superClass != null)
      {
         fields = (Field[])ArrayUtils.addAll(fields, superClass.getDeclaredFields());
         superClass = superClass.getSuperclass();
      }

      return fields;
   }

   private static Method[] getAllComponentMethods(Object component)
   {
      Method[] methods = component.getClass().getDeclaredMethods();
      Class<?> superClass = component.getClass().getSuperclass();

      while(superClass != null)
      {
         methods = (Method[])ArrayUtils.addAll(methods, superClass.getDeclaredMethods());
         superClass = superClass.getSuperclass();
      }

      return methods;
   }

   private static String getComponentName( Field field )
   {
      In inAnnot = field.getAnnotation(In.class);
      if( inAnnot != null )
      {
         if( inAnnot.value().trim().isEmpty() )
         {
            return field.getName();
         }
         else
         {
            return inAnnot.value();
         }
      }
      return null;
   }

   private static String getComponentName( Method method )
   {
      In inAnnot = method.getAnnotation(In.class);
      if( inAnnot != null )
      {
         if( inAnnot.value().trim().isEmpty() )
         {
            // assume it's a setter
            String name = method.getName().substring(3);
            name = name.substring(0,1).toLowerCase() + name.substring(1);
            return name;
         }
         else
         {
            return inAnnot.value();
         }
      }
      return null;
   }
}
