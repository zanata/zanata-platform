/*
 * Copyright 2010-2015, Red Hat, Inc. and individual contributors as indicated by the
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


import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import javax.inject.Inject;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang.ArrayUtils;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.zanata.util.AutowireLocator;

import com.google.common.collect.Lists;

/**
 * Helps with Auto-wiring of beans for integrated tests without the
 * need for a full Seam environment. It's a singleton class that upon first use
 * will change the way the {@link org.zanata.util.ServiceLocator} class works by
 * returning its own auto-wired beans.
 * <p>
 * Note: If CDI-Unit is active, the modified ServiceLocator will attempt
 * to use real CDI beans first, otherwise falling back on Autowire
 * beans if available.
 * </p>
 * <p>
 * Supports beans injected using: {@link javax.inject.Inject},
 * {@link org.zanata.util.ServiceLocator#getInstance(String, Class)} and similar methods...
 * and which have no-arg constructors.
 * </p>
 * <p>
 * Limitations:
 * <ul>
 *     <li>Injection by name is only supported where use(String, Object)
 *     has been called beforehand. Otherwise, injection will be done by
 *     class/interface.</li>
 *     <li>Injection by class or interface is only supported where
 *     useImpl(Class) has been called beforehand.</li>
 *     <li>Injection by class or interface creates a new instance of the
 *     bean class at each injection point.</li>
 *     <li>There is only one, global, scope.  All scope annotations are
 *     ignored.</li>
 *     <li>Lifecycle methods are ignored.</li>
 *     <li>Injection of unnamed beans may not work (Seam components
 *     always have names).</li>
 *     <li>CDI qualifiers are ignored with a warning.</li>
 *     <li>Beans with the same name or interfaces will silently overwrite
 *     each other in the autowire scope.</li>
 * </ul>
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 * @author Sean Flanigan <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
@Exclude(ifProjectStage = ProjectStage.IntegrationTest.class)
public class SeamAutowire {

    private static final Object PLACEHOLDER = new Object();

    private static SeamAutowire instance;
    public static boolean useRealServiceLocator = false;

    private Map<String, Object> namedComponents = new HashMap<String, Object>();

    /**
     * key is an interface Class, values is the concrete bean Class
     */
    private Map<Class<?>, Class<?>> beanImpls =
            new HashMap<Class<?>, Class<?>>();

    private boolean ignoreNonResolvable;

    private boolean allowCycles;

    static {
        rewireServiceLocatorClass();
    }

    protected SeamAutowire() {
    }

    public SeamAutowire allowCycles() {
        allowCycles = true;
        return this;
    }

    /**
     * Initializes and returns the SeamAutowire instance.
     *
     * @return The Singleton instance of the SeamAutowire class.
     */
    public static SeamAutowire instance() {
        if (instance == null) {
            instance = new SeamAutowire();
        }
        return instance;
    }

    /**
     * Clears out any beans and returns to it's initial value.
     */
    public SeamAutowire reset() {
        // TODO create a new instance instead, to be sure of clearing all state
        ignoreNonResolvable = false;
        namedComponents.clear();
        beanImpls.clear();
        allowCycles = false;
        AutowireContexts.simulateSessionContext(false);
        useImpl(AutowireLocator.class);
        return this;
    }

    /**
     * Indicates if the presence of a session context will be simulated.
     * By default contexts are not simulated.
     */
    public SeamAutowire simulateSessionContext(boolean simulate) {
        AutowireContexts.simulateSessionContext(simulate);
        return this;
    }

    /**
     * Indicates if the presence of an event context will be simulated.
     * By default contexts are not simulated.
     */
    public SeamAutowire simulateEventContext(boolean simulate) {
        AutowireContexts.simulateEventContext(simulate);
        return this;
    }

    /**
     * Indicates a specific instance of a bean to use.
     *
     * @param name
     *            The name of the bean. When another bean injects
     *            using <code>@Inject(value = "name")</code> or
     *            <code>@Inject varName</code>, the provided bean will be used.
     * @param bean
     *            The bean instance to use under the provided name.
     */
    public SeamAutowire use(String name, Object bean) {
        if (namedComponents.containsKey(name)) {
            throw new AutowireException("The bean " + name
                    + " was already created.  You should register it before "
                    + "it is resolved.");
        }
        namedComponents.put(name, bean);
        // we could register the parent interfaces, but note that
        // getComponent(Class) currently constructs a new instance every time
//        this.registerInterfaces(bean.getClass());
        return this;
    }

    /**
     * Registers an implementation to use for beans. This method is
     * provided for beans which are injected by interface rather than name.
     *
     * @param cls
     *            The class to register.
     */
    public SeamAutowire useImpl(Class<?> cls) {
        if (Modifier.isAbstract(cls.getModifiers())) {
            throw new AutowireException("Class " + cls.getName()
                    + " is abstract.");
        }
        this.registerInterfaces(cls);

        return this;
    }

    /**
     * Indicates that a warning should be logged if for some reason a bean
     * cannot be resolved. Otherwise, an exception will be thrown.
     */
    public SeamAutowire ignoreNonResolvable() {
        this.ignoreNonResolvable = true;
        return this;
    }

    /**
     * Returns a bean by name.
     *
     * @param name
     *            The bean's name.
     * @return The bean registered under the provided name, or null if such
     *         a bean has not been auto wired or cannot be resolved
     *         otherwise.
     */
    public Object getComponent(String name) {
        return namedComponents.get(name);
    }

    public <T> T getComponent(Class<T> beanClass, Annotation... qualifiers) {
        if (qualifiers.length != 0) {
            log.warn(
                    "Qualifiers currently not supported by SeamAutowire.  Try CDI-Unit and CdiUnitRunner. Class:{}, Qualifiers:{}",
                    beanClass,
                    Lists.newArrayList(qualifiers));
        }
        return autowire(beanClass);
    }

    /**
     * Creates (but does not autowire) the bean instance for the provided
     * class.
     *
     * @param fieldClass
     *            The bean class to create - may be an interface if useImpl
     *            was called, otherwise must have a no-arg constructor per Seam
     *            spec.
     * @return The bean.
     */
    private <T> T create(Class<T> fieldClass, String beanPath) {
        // field might be an interface, but we need to find the
        // implementation class
        Class<T> beanClass = getImplClass(fieldClass);
        // The bean class might be an interface
        if (beanClass.isInterface()) {
            throw new AutowireException(""
                    + "Could not auto-wire bean with path "
                    + beanPath + " of type "
                    + beanClass.getName()
                    + ". The bean is defined as an interface, but no "
                    + "implementations have been defined for it.");
        }

        try {
            // No-arg constructor
            Constructor<T> constructor =
                    beanClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (NoSuchMethodException e) {
            throw new AutowireException(""
                    + "Could not auto-wire bean with path "
                    + beanPath + " of type "
                    + beanClass.getName()
                    + ". No no-args constructor.", e);
        } catch (InvocationTargetException e) {
            throw new AutowireException(""
                    + "Could not auto-wire bean with path "
                    + beanPath + " of type "
                    + beanClass.getName()
                    + ". Exception thrown from constructor.", e);
        } catch (Exception e) {
            throw new AutowireException(""
                    + "Could not auto-wire bean with path "
                            + beanPath + " of type "
                            + beanClass.getName(), e);
        }
    }

    private <T> Class<T> getImplClass(Class<T> fieldClass) {
        // If the bean type is an interface, try to find a declared
        // implementation
        // TODO field class might a concrete superclass
        // of the impl class
        if (Modifier.isAbstract(fieldClass.getModifiers())
                && this.beanImpls.containsKey(fieldClass)) {
            fieldClass = (Class<T>) this.beanImpls.get(fieldClass);
        }

        return (Class<T>) fieldClass;
    }

    /**
     * Autowires and returns the bean instance for the provided class.
     *
     * @param beanClass
     *            The bean class to create - may be an interface if useImpl
     *            was called, otherwise must have a no-arg constructor per Seam
     *            spec.
     * @return The autowired bean.
     */
    public <T> T autowire(Class<T> beanClass) {
        // We could use getComponentName(Class) to simulate Seam's lookup
        // (by the @Named annotation on the injection point's class), but
        // this would just move us further away from CDI semantics.
        // TODO don't create multiple instances of a class
        Predicate<Object> predicate = Predicates.instanceOf(beanClass);
        Optional<Object> namedOptional = Iterables.tryFind(
                namedComponents.values(), predicate);
        if (namedOptional.isPresent()) {
            return (T) namedOptional.get();
        }
        Optional<Class<?>> implOptional =
                Iterables.tryFind(beanImpls.values(), predicate);
        if (implOptional.isPresent()) {
            return (T) implOptional.get();
        }
        String beanPath = beanClass.getSimpleName();
        return autowire(create(beanClass, beanPath), beanPath);
    }

    /**
     * Autowires a bean instance. The provided instance of the bean
     * will be autowired instead of creating a new one.
     *
     * @param bean
     *            The bean instance to autowire.
     * @param <T>
     * @return Returns bean.
     */
    public <T> T autowire(T bean) {
        return autowire(bean, bean.getClass().getSimpleName());
    }

    private <T> T autowire(T bean, String beanPath) {
        Class<T> beanClass = (Class<T>) bean.getClass();

        // Register all interfaces for this class
        this.registerInterfaces(beanClass);
        // Resolve injected Components
        for (ComponentAccessor accessor : getAllComponentAccessors(bean)) {
            // Another annotated bean
            Inject inAnnotation = accessor.getAnnotation(Inject.class);
            if (inAnnotation != null) {
                Object fieldVal = null;
                String beanName = accessor.getComponentName();
                Class<?> beanType = accessor.getComponentType();
                Set<Annotation> qualifiers = accessor.getQualifiers();
                Class<?> implType = getImplClass(beanType);

                // TODO stateless beans should not / need not be cached
                // autowire the bean if not done yet
                if (!namedComponents.containsKey(beanName)) {
                    String newComponentPath = beanPath + "." + beanName;
                    Object newComponent = null;
                    try {
                        newComponent = create(beanType, newComponentPath);
                    } catch (AutowireException e) {
                        if (ignoreNonResolvable) {
                            log.warn("Could not build bean with name '" +
                                    beanName + "' of type: "
                                    + beanType + ".", e);
                        } else {
                            throw e;
                        }
                    }

                    if (allowCycles) {
                        namedComponents.put(beanName, newComponent);
                    } else {
                        // to detect mutual injection
                        namedComponents.put(beanName, PLACEHOLDER);
                    }

                    try {
                        if (newComponent != null) {
                            autowire(newComponent, newComponentPath);
                        }
                    } catch (AutowireException e) {
                        if (ignoreNonResolvable) {
                            log.warn("Could not autowire bean of type: "
                                    + beanType + ".", e);
                        } else {
                            throw e;
                        }
                    }

                    if (!allowCycles) {
                        // replace placeholder with the injected object
                        namedComponents.put(beanName, newComponent);
                    }
                }

                fieldVal = namedComponents.get(beanName);
                if (fieldVal == PLACEHOLDER) {
                    throw new AutowireException(
                            "Recursive dependency: unable to inject "
                                    + beanName + " into bean of type "
                                    + bean.getClass().getName());
                }
                try {
                    accessor.setValue(bean, fieldVal);
                } catch (AutowireException e) {
                    if (ignoreNonResolvable) {
                        log.warn("Could not set autowire field "
                                + accessor.getComponentName()
                                + " on bean of type "
                                + bean.getClass().getName()
                                + " to value of type "
                                + fieldVal.getClass().getName());
                    } else {
                        throw e;
                    }
                }
            }
        }

        // call post constructor
        invokePostConstructMethod(bean, beanPath);

        return bean;
    }

    // TODO why are we rewiring classes we control?
    private static void rewireServiceLocatorClass() {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass locatorCls = pool.get("org.zanata.util.ServiceLocator");

            // Commonly used CtClasses
            final CtClass stringCls = pool.get("java.lang.String");
            final CtClass objectCls = pool.get("java.lang.Object");
            final CtClass classCls = pool.get("java.lang.Class");

            // Replace ServiceLocator's method bodies with the ones in
            // AutowireComponent
            CtClass[] emptyArgs = {};
            CtMethod methodToReplace =
                    locatorCls.getDeclaredMethod("instance", emptyArgs);
            methodToReplace.setBody("{return org.zanata.util.AutowireLocator.instance(); }");

            locatorCls.toClass();
        } catch (NotFoundException | CannotCompileException e) {
            throw new AutowireException(
                    "Problem rewiring ServiceLocator class", e);
        }
    }

    // TODO delete this when Seam is entirely gone
    private static void rewireSeamContextsClass() {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass contextsCls = pool.get("org.jboss.seam.contexts.Contexts");

            // Replace Context's method bodies with the ones in
            // AutowireComponent
            contextsCls.getDeclaredMethod("isSessionContextActive")
                    .setBody(
                            "{ return org.zanata.seam.AutowireContexts.isSessionContextActive(); }");

            contextsCls.getDeclaredMethod("isEventContextActive")
                    .setBody("{ return org.zanata.seam.AutowireContexts.isEventContextActive(); }");

            contextsCls.getDeclaredMethod("getEventContext")
                    .setBody("{ return org.zanata.seam.AutowireContexts.getInstance().getEventContext(); }");

            contextsCls.getDeclaredMethod("getSessionContext")
                    .setBody("{ return org.zanata.seam.AutowireContexts.getInstance().getSessionContext(); }");

            contextsCls.toClass();
        } catch (NotFoundException | CannotCompileException e) {
            throw new AutowireException(
                    "Problem rewiring Seam's Contexts class", e);
        }

    }

    // TODO delete this when Seam is entirely gone
    private static void rewireSeamComponentClass() {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass componentCls = pool.get("org.jboss.seam.Component");

            // Commonly used CtClasses
            final CtClass stringCls = pool.get("java.lang.String");
            final CtClass objectCls = pool.get("java.lang.Object");
            final CtClass classCls = pool.get("java.lang.Class");

            // Replace Component's method bodies with the ones in
            // AutowireComponent
            replaceGetInstance(pool, componentCls, stringCls, classCls);
            replaceGetInstance(pool, componentCls, stringCls, objectCls,
                    classCls);

            componentCls.toClass();
        } catch (NotFoundException | CannotCompileException e) {
            throw new AutowireException(
                    "Problem rewiring Seam's Component class", e);
        }

    }

    /**
     * Replaces Component.getInstance(params) method body with that of
     * AutowireComponent.getInstance(params).
     *
     * @param pool
     *            Class pool to get class instances.
     * @param componentCls
     *            Class that represents the jboss Component class.
     * @param params
     *            Parameters for the getComponent method that will be replaced
     * @throws javassist.NotFoundException
     * @throws javassist.CannotCompileException
     */
    private static void replaceGetInstance(ClassPool pool,
            CtClass componentCls, CtClass... params) throws NotFoundException,
            CannotCompileException {
        CtMethod methodToReplace =
                componentCls.getDeclaredMethod("getInstance", params);
        methodToReplace.setBody(pool.get(AutowireLocator.class.getName())
                .getDeclaredMethod("getInstance", params), null);
    }

    // TODO why are we rewiring classes we control?
    private static void replaceGetDependent(ClassPool pool, CtClass locatorCls, CtClass... params) throws
            NotFoundException, CannotCompileException {
        CtMethod methodToReplace =
                locatorCls.getDeclaredMethod("getDependent", params);
        methodToReplace.setBody(pool.get(AutowireLocator.class.getName())
                .getDeclaredMethod("getDependent", params), null);
    }

    // TODO delete this when Seam is entirely gone
    private static void rewireSeamTransactionClass() {
        try {
            ClassPool pool = ClassPool.getDefault();
            CtClass cls = pool.get("org.jboss.seam.transaction.Transaction");

            // Replace Component's method bodies with the ones in
            // AutowireComponent
            CtMethod methodToReplace =
                    cls.getDeclaredMethod("instance", new CtClass[] {});
            methodToReplace
                    .setBody(
                            "{ return org.zanata.seam.AutowireTransaction.instance(); }");

            cls.toClass();
        } catch (NotFoundException | CannotCompileException e) {
            throw new AutowireException(
                    "Problem rewiring Seam's Transaction class", e);
        }
    }

    private static ComponentAccessor[]
            getAllComponentAccessors(Object bean) {
        Collection<ComponentAccessor> props =
                new ArrayList<ComponentAccessor>();

        for (Field f : getAllComponentFields(bean)) {
            if (f.getAnnotation(Inject.class) != null) {
                props.add(ComponentAccessor.newInstance(f));
            }
        }
        for (Method m : getAllComponentMethods(bean)) {
            if (m.getAnnotation(Inject.class) != null) {
                props.add(ComponentAccessor.newInstance(m));
            }
        }

        return props.toArray(new ComponentAccessor[props.size()]);
    }

    private static Field[] getAllComponentFields(Object bean) {
        Field[] fields = bean.getClass().getDeclaredFields();
        Class<?> superClass = bean.getClass().getSuperclass();

        while (superClass != null) {
            fields =
                    (Field[]) ArrayUtils.addAll(fields,
                            superClass.getDeclaredFields());
            superClass = superClass.getSuperclass();
        }

        return fields;
    }

    private static Method[] getAllComponentMethods(Object bean) {
        Method[] methods = bean.getClass().getDeclaredMethods();
        Class<?> superClass = bean.getClass().getSuperclass();

        while (superClass != null) {
            methods =
                    (Method[]) ArrayUtils.addAll(methods,
                            superClass.getDeclaredMethods());
            superClass = superClass.getSuperclass();
        }

        return methods;
    }

    private void registerInterfaces(Class<?> cls) {
        assert !Modifier.isAbstract(cls.getModifiers());
        // register all interfaces registered by this bean
        for (Class<?> iface : getAllInterfaces(cls)) {
            this.beanImpls.put(iface, cls);
        }
    }

    private static Set<Class<?>> getAllInterfaces(Class<?> cls) {
        Set<Class<?>> interfaces = new HashSet<Class<?>>();

        for (Class<?> superClass : cls.getInterfaces()) {
            interfaces.add(superClass);
            interfaces.addAll(getAllInterfaces(superClass));
        }

        return interfaces;
    }

    /**
     * Invokes a single method (the first found) annotated with
     * {@link javax.annotation.PostConstruct},
     */
    private static void invokePostConstructMethod(Object bean,
            String beanPath) {
        Class<?> compClass = bean.getClass();
        boolean postConstructAlreadyFound = false;

        for (Method m : compClass.getDeclaredMethods()) {
            // Per the spec, there should be only one PostConstruct method
            if (m.getAnnotation(javax.annotation.PostConstruct.class) != null) {
                if (postConstructAlreadyFound) {
                    throw new AutowireException("More than one PostConstruct method found for class "
                            + compClass.getName());
                }
                try {
                    m.setAccessible(true);
                    m.invoke(bean); // there should be no params
                    postConstructAlreadyFound = true;
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new AutowireException(
                            "Error invoking PostConstruct method in bean '" +
                                    beanPath + "' of class "
                                    + compClass.getName(), e);
                }
            }
        }
    }

//    public static String getComponentName(Class<?> clazz) {
//        Named named = clazz.getAnnotation(Named.class);
//        if (named == null) {
//            return null;
//        }
//        if (named.value().isEmpty()) {
//            return StringUtils.uncapitalize(clazz.getSimpleName());
//        }
//        return named.value();
//    }

}
