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
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Logger;
import org.jboss.seam.log.Logging;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
   private static final org.slf4j.Logger log = LoggerFactory.getLogger(SeamAutowire.class);

   private static SeamAutowire instance;

   private Map<String, Object> namedComponents = new HashMap<String, Object>();

   private Map<Class<?>, Class<?>> componentImpls = new HashMap<Class<?>, Class<?>>();

   private boolean ignoreNonResolvable;


   protected SeamAutowire()
   {
   }

   /**
    * Initializes and returns the SeamAutowire instance.
    *
    * @return The Singleton instance of the SeamAutowire class.
    */
   public static final SeamAutowire instance()
   {
      if(instance == null)
      {
         instance = new SeamAutowire();
         instance.rewireSeamComponentClass();
         instance.rewireSeamTransactionClass();
      }
      return instance;
   }

   /**
    * Clears out any components and returns to it's initial value.
    */
   public SeamAutowire reset()
   {
      this.ignoreNonResolvable = false;
      this.namedComponents.clear();
      return this;
   }

   /**
    * Indicates a specific instance of a component to use.
    *
    * @param name The name of the component. When another component injects using <code>@In(value = "name")</code> or
    *             <code>@In varName</code>, the provided component will be used.
    * @param component The component instance to use under the provided name.
    */
   public SeamAutowire use(String name, Object component)
   {
      this.addComponentInstance(name, component);
      return this;
   }

   /**
    * Registers an implementation to use for components. This method is provided just in case some components are
    * inject via interfaces.
    *
    * @param cls The class to register.
    */
   public SeamAutowire useImpl(Class<?> cls)
   {
      if( cls.isInterface() )
      {
         throw new RuntimeException("Class " + cls.getName() + " is an interface.");
      }
      this.registerInterfaces(cls);

      return this;
   }

   /**
    * Indicates that a warning should be logged if for some reason a component cannot be resolved. Otherwise, an
    * exception will be thrown.
    */
   public SeamAutowire ignoreNonResolvable()
   {
      this.ignoreNonResolvable = true;
      return this;
   }

   /**
    * Returns a component by name.
    *
    * @param name Component's name.
    * @return The component registered under the provided name, or null if such a component has not been auto wired
    * or cannot be resolved otherwise.
    */
   public Object getComponent(String name)
   {
      if( this.namedComponents.containsKey(name) )
      {
         return this.namedComponents.get(name);
      }
      return null;
   }

   /**
    * Autowires and returns the component instance for the provided class.
    *
    * @param componentClass The component class to autowire.
    * @return The autowired component.
    */
   public <T> T autowire(Class<T> componentClass)
   {
      // Create a new component instance (must have a no-arg constructor per Seam spec)
      T component;

      // If the component type is an interface, try to find a declared implementation
      if( componentClass.isInterface() && this.componentImpls.containsKey(componentClass) )
      {
         componentClass = (Class<T>)this.componentImpls.get(componentClass);
      }

      try
      {
         Constructor<T> constructor = componentClass.getDeclaredConstructor(); // No-arg constructor
         constructor.setAccessible(true);
         component = constructor.newInstance();
      }
      catch (InstantiationException e)
      {
         throw new RuntimeException(
               "Could not auto-wire component of type " + componentClass.getName(), e);
      }
      catch (IllegalAccessException e)
      {
         throw new RuntimeException("" +
               "Could not auto-wire component of type " + componentClass.getName(), e);
      }
      catch (NoSuchMethodException e)
      {
         throw new RuntimeException("" +
               "Could not auto-wire component of type " + componentClass.getName() + ". No empty constructor.", e);
      }
      catch (InvocationTargetException e)
      {
         throw new RuntimeException("" +
               "Could not auto-wire component of type " + componentClass.getName() + ". Exception thrown from constructor.", e);
      }

      return this.autowire(component);
   }

   /**
    * Autowires a component instance. The provided instance of the component will be autowired instead of creating a
    * new one.
    *
    * @param component The component instance to autowire.
    * @param <T>
    * @return Returns component.
    */
   public <T> T autowire(T component)
   {
      Class<T> componentClass = (Class<T>)component.getClass();

      // Register all interfaces for this class
      this.registerInterfaces(componentClass);

      // Resolve injected Components
      for( ComponentAccessor accessor : getAllComponentAccessors(component) )
      {
         // Another annotated component
         if( accessor.getAnnotation(In.class) != null )
         {
            Object fieldVal = null;
            String compName = accessor.getComponentName();
            Class<?> compType = accessor.getComponentType();

            // autowire the component if not done yet
            if( !namedComponents.containsKey(compName) )
            {
               try
               {
                  Object newComponent = autowire(compType);
                  this.addComponentInstance(compName, newComponent);
               }
               catch (RuntimeException e)
               {
                  if( ignoreNonResolvable )
                  {
                     log.warn("Could not resolve component of type: " + compType + ". Cause: " + e.getMessage());
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
               accessor.setValue(component, fieldVal);
            }
            catch (RuntimeException e)
            {
               if( ignoreNonResolvable )
               {
                  log.warn("Could not set autowire field " + accessor.getComponentName() +
                        " on component of type " + component.getClass().getName() +
                        " to value of type " + fieldVal.getClass().getName());
               }
               else
               {
                  throw e;
               }
            }
         }
         // Logs
         else if( accessor.getAnnotation(Logger.class) != null )
         {
            accessor.setValue( component, Logging.getLog(accessor.getComponentType()) );
         }
      }

      // call post constructor
      invokePostConstructMethod(component);

      return component;
   }

   private void addComponentInstance(String name, Object compInst)
   {
      this.namedComponents.put(name, compInst);
   }

   private void rewireSeamComponentClass()
   {
      try
      {
         ClassPool pool = ClassPool.getDefault();
         CtClass componentCls = pool.get("org.jboss.seam.Component");

         // Commonly used CtClasses
         final CtClass stringCls = pool.get("java.lang.String");
         final CtClass booleanCls = pool.get("boolean");
         final CtClass objectCls = pool.get("java.lang.Object");
         final CtClass scopeTypeCls = pool.get("org.jboss.seam.ScopeType");
         final CtClass classCls = pool.get("java.lang.Class");

         // Replace Component's method bodies with the ones in AutowireComponent
         replaceGetInstance(pool, componentCls, stringCls, booleanCls, booleanCls);
         replaceGetInstance(pool, componentCls, stringCls, scopeTypeCls, booleanCls, booleanCls);
         replaceGetInstance(pool, componentCls, classCls);
         replaceGetInstance(pool, componentCls, classCls, scopeTypeCls);
         try
         {
            // Seam 2.2.2
            replaceGetInstance(pool, componentCls, stringCls, booleanCls, booleanCls, objectCls, scopeTypeCls);
         }
         catch (NotFoundException e)
         {
            // Seam 2.2.0
            replaceGetInstance(pool, componentCls, stringCls, booleanCls, booleanCls, objectCls);
         }

         componentCls.toClass();
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

   /**
    * Replaces Component.getInstance(params) method body with that of
    * AutowireComponent.getInstance(params).
    * @param pool Class pool to get class instances.
    * @param componentCls Class that represents the jboss Component class.
    * @param params Parameters for the getComponent method that will be replaced
    * @throws NotFoundException
    * @throws CannotCompileException
    */
   private void replaceGetInstance(ClassPool pool, CtClass componentCls, CtClass... params) throws NotFoundException, CannotCompileException
   {
      CtMethod methodToReplace = componentCls.getDeclaredMethod("getInstance", params);
      methodToReplace.setBody(
            pool.get(AutowireComponent.class.getName()).getDeclaredMethod("getInstance", params),
            null);
   }

   private void rewireSeamTransactionClass()
   {
      try
      {
         ClassPool pool = ClassPool.getDefault();
         CtClass cls = pool.get("org.jboss.seam.transaction.Transaction");

         // Replace Component's method bodies with the ones in AutowireComponent
         CtMethod methodToReplace = cls.getDeclaredMethod("instance", new CtClass[]{});
         methodToReplace.setBody("{ return org.zanata.seam.AutowireTransaction.instance(); }");

         cls.toClass();
      }
      catch (NotFoundException e)
      {
         throw new RuntimeException("Problem rewiring Seam's Transaction class", e);
      }
      catch (CannotCompileException e)
      {
         throw new RuntimeException("Problem rewiring Seam's Transaction class", e);
      }
   }

   private static ComponentAccessor[] getAllComponentAccessors( Object component )
   {
      Collection<ComponentAccessor> props = new ArrayList<ComponentAccessor>();

      for( Field f : getAllComponentFields(component) )
      {
         if( f.getAnnotation(In.class) != null || f.getAnnotation(Logger.class) != null )
         {
            props.add( ComponentAccessor.newInstance(f) );
         }
      }
      for( Method m : getAllComponentMethods(component) )
      {
         if( m.getAnnotation(In.class) != null || m.getAnnotation(Logger.class) != null )
         {
            props.add( ComponentAccessor.newInstance(m) );
         }
      }

      return props.toArray( new ComponentAccessor[ props.size() ] );
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

   private void registerInterfaces(Class<?> cls)
   {
      if( !cls.isInterface() )
      {
         // register all interfaces registered by this component
         for (Class<?> iface : getAllInterfaces(cls))
         {
            this.componentImpls.put(iface, cls);
         }
      }
   }

   private static Set<Class<?>> getAllInterfaces(Class<?> cls)
   {
      Set<Class<?>> interfaces = new HashSet<Class<?>>();

      for( Class<?> superClass : cls.getInterfaces() )
      {
         interfaces.add(superClass);
         interfaces.addAll( getAllInterfaces(superClass) );
      }

      return interfaces;
   }

   /**
    * Invokes a single method (the first found) annotated with {@link javax.annotation.PostConstruct},
    * {@link org.jboss.seam.annotations.intercept.PostConstruct}, or {@link org.jboss.seam.annotations.Create} annotations.
    */
   private static void invokePostConstructMethod(Object component)
   {
      Class<?> compClass = component.getClass();
      boolean postConstructAlreadyFound = false;

      for( Method m : compClass.getDeclaredMethods() )
      {
         // Call the first Post Constructor found. Per the spec, there should be only one
         if( m.getAnnotation(javax.annotation.PostConstruct.class) != null
             || m.getAnnotation(org.jboss.seam.annotations.intercept.PostConstruct.class) != null
             || m.getAnnotation(org.jboss.seam.annotations.Create.class) != null)
         {
            if(postConstructAlreadyFound)
            {
               log.warn("More than one PostConstruct method found for class " + compClass.getName()
                     + ", only one will be invoked");
               break;
            }

            try
            {
               m.invoke(component); // there should be no params
               postConstructAlreadyFound = true;
            }
            catch (IllegalAccessException e)
            {
               throw new RuntimeException("Error invoking Post construct method in component of class: " + compClass.getName(), e);
            }
            catch (InvocationTargetException e)
            {
               throw new RuntimeException("Error invoking Post construct method in component of class: " + compClass.getName(), e);
            }
         }
      }
   }

}
