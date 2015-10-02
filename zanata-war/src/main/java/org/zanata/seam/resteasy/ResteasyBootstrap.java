// Implementation copied from Seam 2.3.1, commit f3077fe

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.zanata.seam.resteasy;

import static org.jboss.seam.annotations.Install.BUILT_IN;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ext.Providers;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.core.ThreadLocalResteasyProviderFactory;
import org.jboss.resteasy.plugins.providers.RegisterBuiltin;
import org.jboss.resteasy.plugins.server.resourcefactory.POJOResourceFactory;
import org.jboss.resteasy.spi.Registry;
import org.jboss.resteasy.spi.ResourceFactory;
import org.jboss.resteasy.spi.StringConverter;
import org.jboss.seam.Component;
import org.jboss.seam.Seam;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import org.jboss.seam.annotations.JndiName;
import org.jboss.seam.annotations.Logger;
import javax.inject.Named;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.contexts.Contexts;
import org.jboss.seam.core.Init;
import org.jboss.seam.deployment.AnnotationDeploymentHandler;
import org.jboss.seam.deployment.DeploymentStrategy;
import org.jboss.seam.log.Log;
import org.jboss.seam.util.EJB;
import org.jboss.seam.util.Reflections;

/**
 * Detects (through scanning and configuration) JAX-RS resources and providers,
 * then registers them with RESTEasy.
 * <p>
 * This class is a factory for <tt>org.jboss.seam.resteasy.dispatcher</tt> and
 * it has been designed for extension. Alternatively, you can ignore what this
 * class is doing and provide a different
 * <tt>org.jboss.seam.resteasy.dispatcher</tt> yourself without extending this
 * class.
 * </p>
 * <p>
 * The main methods of this class are <tt>registerProviders()</tt> and
 * <tt>registerResources()</tt>. These methods call out to the individual
 * fine-grained registration procedures, which you can override if a different
 * registration strategy for a particular type/component is desired.
 * </p>
 *
 * @author Christian Bauer
 */
@Named("org.jboss.seam.resteasy.bootstrap")
@javax.enterprise.context.ApplicationScoped
/* TODO [CDI] Remove @PostConstruct from startup method and make it accept (@Observes @Initialized ServletContext context) */

@Install(precedence = BUILT_IN,
        classDependencies = "org.jboss.resteasy.spi.ResteasyProviderFactory")
public class ResteasyBootstrap {

    @Logger
    Log log;

    @Inject
    protected Application application;

    // The job of this class is to initialize and configure the RESTEasy
    // Dispatcher instance
    protected Dispatcher dispatcher;

    @Produces(/* TODO [CDI] check this: migrated from @Factory */"org.jboss.seam.resteasy.dispatcher")
    public Dispatcher getDispatcher() {
        return dispatcher;
    }

    @PostConstruct
    public void init() {
        log.info("bootstrapping JAX-RS application");

        // Custom ResteasyProviderFactory that understands Seam component lookup
        // at runtime
        SeamResteasyProviderFactory providerFactory = createProviderFactory();
        dispatcher = createDispatcher(providerFactory);
        initDispatcher();

        // Always use the "deployment sensitive" factory - that means it is
        // handled through ThreadLocal, not static
        // TODO: How does that actually work? It's never used because the
        // dispatcher is created with the original one
        SeamResteasyProviderFactory
                .setInstance(new ThreadLocalResteasyProviderFactory(
                        providerFactory));

        // Put Providers, Registry and Dispatcher into RESTEasy context.
        dispatcher.getDefaultContextObjects().put(Providers.class,
                providerFactory);
        dispatcher.getDefaultContextObjects().put(Registry.class,
                dispatcher.getRegistry());
        dispatcher.getDefaultContextObjects().put(Dispatcher.class, dispatcher);
        Map contextDataMap = SeamResteasyProviderFactory.getContextDataMap();
        contextDataMap.putAll(dispatcher.getDefaultContextObjects());

        // Seam can scan the classes for us, we just have to list them in
        // META-INF/seam-deployment.properties
        DeploymentStrategy deployment =
                (DeploymentStrategy) Component
                        .getInstance("deploymentStrategy");
        AnnotationDeploymentHandler handler =
                (AnnotationDeploymentHandler) deployment
                        .getDeploymentHandlers().get(
                                AnnotationDeploymentHandler.NAME);

        Collection<Class<?>> providers = findProviders(handler);
        Collection<Class<?>> resources = findResources(handler);
        Collection<Component> seamComponents = findSeamComponents();

        registerProviders(seamComponents, providers);
        registerResources(seamComponents, resources);
    }

    protected SeamResteasyProviderFactory createProviderFactory() {
        return new SeamResteasyProviderFactory();
    }

    protected Dispatcher createDispatcher(
            SeamResteasyProviderFactory providerFactory) {
        return new SynchronousDispatcher(providerFactory);
    }

    protected void initDispatcher() {
        assert application.getLanguageMappings().isEmpty();
        assert application.getMediaTypeMappings().isEmpty();
        // not supported, but we don't use them
        // getDispatcher().setLanguageMappings(application.getLanguageMappings());
        // getDispatcher().setMediaTypeMappings(application.getMediaTypeMappings());
    }

    protected Collection<Class<?>> findProviders(
            AnnotationDeploymentHandler handler) {
        return findTypes(
                handler,
                application.isScanProviders(),
                javax.ws.rs.ext.Provider.class.getName(),
                application.getProviderClassNames());
    }

    protected Collection<Class<?>> findResources(
            AnnotationDeploymentHandler handler) {
        return findTypes(
                handler,
                application.isScanResources(),
                javax.ws.rs.Path.class.getName(),
                application.getResourceClassNames());
    }

    protected Collection<Class<?>> findTypes(
            AnnotationDeploymentHandler handler,
            boolean scanClasspathForAnnotations,
            String annotationFQName, Collection<String> includeTypeNames) {

        Collection<Class<?>> types = new HashSet();

        if (scanClasspathForAnnotations) {
            Collection<Class<?>> annotatedTypes =
                    handler.getClassMap().get(annotationFQName);
            if (annotatedTypes != null)
                types.addAll(annotatedTypes);
        }

        try {
            for (String s : new HashSet<String>(includeTypeNames)) {
                types.add(Reflections.classForName(s));
            }
        } catch (ClassNotFoundException ex) {
            log.error("error loading JAX-RS type: " + ex.getMessage(), ex);
        }

        return types;
    }

    protected Collection<Component> findSeamComponents() {
        // Iterate through all variables in the application context that end
        // with ".component"
        log.debug("discovering all Seam components");
        Collection<Component> seamComponents = new HashSet();
        String[] applicationContextNames =
                Contexts.getApplicationContext().getNames();
        for (String applicationContextName : applicationContextNames) {
            if (applicationContextName.endsWith(".component")) {
                Component seamComponent =
                        (Component) Component.getInstance(
                                applicationContextName, ScopeType.APPLICATION);
                seamComponents.add(seamComponent);
            }
        }
        return seamComponents;
    }

    protected void registerProviders(Collection<Component> seamComponents,
            Collection<Class<?>> providerClasses) {

        // RESTEasy built-in providers first
        if (application.isUseBuiltinProviders()) {
            log.info("registering built-in RESTEasy providers");
            RegisterBuiltin.register(getDispatcher().getProviderFactory());
        }

        Set<Class> handledProviders = new HashSet(); // Stuff we don't want to
        // examine twice

        /*
         * TODO: Retracted due to missing RESTEasy SPI for external provider
         * metadata, see https://jira.jboss.org/jira/browse/JBSEAM-4247 for
         * (Component seamComponent : seamComponents) { // The component can
         * have one (not many) @Provider annotated business interface Class
         * providerInterface =
         * getAnnotatedInterface(javax.ws.rs.ext.Provider.class, seamComponent);
         *
         * // How we register it depends on the component type switch
         * (seamComponent.getType()) {
         *
         * // TODO: We don't support EJB Seam components as providers
         *
         * case JAVA_BEAN:
         *
         * // We are only interested in components that have a @Provider
         * annotation on iface or bean if (providerInterface == null &&
         * !seamComponent
         * .getBeanClass().isAnnotationPresent(javax.ws.rs.ext.Provider.class))
         * { break; }
         *
         * // They also have to be in the right scope, otherwise we can't handle
         * their lifecylce (yet) switch (seamComponent.getScope()) { case
         * APPLICATION:
         *
         * // StringConverter is a special case if
         * (StringConverter.class.isAssignableFrom
         * (seamComponent.getBeanClass())) {
         * getDispatcher().getProviderFactory().addStringConverter(
         * (StringConverter) Component.getInstance(seamComponent.getName()) ); }
         * else { registerSeamComponentProvider(seamComponent); } break;
         *
         * default: throw new RuntimeException( "Provider Seam component '" +
         * seamComponent.getName() + "' must be scoped " + "APPLICATION" ); }
         * break; }
         *
         * // We simply add everything we have seen so far... it's not really
         * necessary but it doesn't hurt (does it?)
         * handledProviders.add(seamComponent.getBeanClass());
         * handledProviders.addAll(seamComponent.getBusinessInterfaces()); }
         */

        for (Class<?> providerClass : providerClasses) {

            // An @Provider annotated type may:

            // - have been already handled as a Seam component in the previous
            // routine
            if (handledProviders.contains(providerClass))
                continue;

            // - be a RESTEasy built-in provider
            if (providerClass.getName().startsWith(
                    "org.jboss.resteasy.plugins.providers"))
                continue;

            // - be an interface, which we don't care about if we don't have an
            // implementation
            if (providerClass.isInterface())
                continue;

            // - be just plain RESTEasy, no Seam component lookup or lifecycle
            if (StringConverter.class.isAssignableFrom(providerClass)) {
                log.debug(
                        "registering provider as RESTEasy StringConverter: {0}",
                        providerClass);
                // getDispatcher().getProviderFactory().addStringConverter((Class<?
                // extends StringConverter>) providerClass);
                throw new RuntimeException(
                        "StringConverter support not implemented");
            } else {
                log.debug("registering provider as plain JAX-RS type: {0}",
                        providerClass);
                getDispatcher().getProviderFactory().registerProvider(
                        providerClass);
            }
        }
    }

    protected void registerResources(Collection<Component> seamComponents,
            Collection<Class<?>> resourceClasses) {

        Set<Class> handledResources = new HashSet(); // Stuff we don't want to
        // examine twice
        // These classes themselves should not be registered at all
        // Configured ResourceHome and ResourceQuery components will be
        // registered later
        handledResources.add(ResourceHome.class);
        handledResources.add(ResourceQuery.class);

        for (Component seamComponent : seamComponents) {

            // A bean class of type (not subtypes) ResourceHome or ResourceQuery
            // annotated with @Path, then
            // it's a Seam component resource we need to register with getPath()
            // on the instance, it has been
            // configured in components.xml
            if (seamComponent.getBeanClass().equals(ResourceHome.class) ||
                    seamComponent.getBeanClass().equals(ResourceQuery.class)) {
                registerHomeQueryResources(seamComponent);
                continue;
            }

            // The component can have one (not many) @Path annotated business
            // interface
            Class resourceInterface =
                    getAnnotatedInterface(javax.ws.rs.Path.class,
                            seamComponent);

            // How we register it depends on the component type
            switch (seamComponent.getType()) {
            case STATELESS_SESSION_BEAN:
                // EJB seam component resources must be @Path annotated on one
                // of their business interfaces
                if (resourceInterface != null) {
                    // TODO: Do we have to consider the scope? It should be
                    // stateless, right?
                    registerInterfaceSeamComponentResource(seamComponent,
                            resourceInterface);
                }
                break;
            case STATEFUL_SESSION_BEAN:
                // EJB seam component resources must be @Path annotated on one
                // of their business interfaces
                if (resourceInterface != null) {
                    log.error(
                            "Not implemented: Stateful EJB Seam component resource: "
                                    + seamComponent);
                    // TODO:
                    // registerStatefulEJBSeamComponentResource(seamComponent);
                }
                break;
            case JAVA_BEAN:

                // We are only interested in components that have a @Path
                // annotation on iface or bean
                if (resourceInterface == null
                        &&
                        !seamComponent.getBeanClass().isAnnotationPresent(
                                javax.ws.rs.Path.class)) {
                    break;
                }

                // They also have to be in the right scope, otherwise we can't
                // handle their lifecylce (yet)
                switch (seamComponent.getScope()) {
                case EVENT:
                case APPLICATION:
                case STATELESS:
                case SESSION:
                    if (resourceInterface != null) {
                        registerInterfaceSeamComponentResource(
                                seamComponent,
                                resourceInterface);
                    } else if (seamComponent.getBeanClass()
                            .isAnnotationPresent(
                                    javax.ws.rs.Path.class)) {
                        registerSeamComponentResource(seamComponent);
                    }
                    break;
                default:
                    throw new RuntimeException(
                            "Resource Seam component '"
                                    + seamComponent.getName()
                                    + "' must be scoped either " +
                                    "EVENT, APPLICATION, STATELESS, or SESSION");
                }
                break;
            }

            // We simply add everything we have seen so far... it's not really
            // necessary but it doesn't hurt (does it?)
            handledResources.add(seamComponent.getBeanClass());
            handledResources.addAll(seamComponent.getBusinessInterfaces());

        }

        for (Class<?> resourceClass : resourceClasses) {
            // An @Path annotated type may:

            // - have been already handled as a Seam component in the previous
            // routine
            if (handledResources.contains(resourceClass))
                continue;

            // - be an interface, which we don't care about if we don't have an
            // implementation
            if (resourceClass.isInterface())
                continue;

            // - be a @Stateless EJB implementation class that was listed in
            // components.xml
            if (resourceClass.isAnnotationPresent(EJB.STATELESS)) {
                registerStatelessEJBResource(resourceClass);
            }  else if (resourceClass.isAnnotationPresent(EJB.STATEFUL)) {
                // - be a @Stateful EJB implementation class that was listed in
                // components.xml
                throw new RuntimeException(
                        "Only stateless EJBs can be JAX-RS resources, remove from configuration: "
                                + resourceClass.getName());
            } else {
                // - just be a regular JAX-RS lifecycle instance that can
                // created/destroyed by RESTEasy
                registerPlainResource(resourceClass);
            }
        }

    }

    protected void registerHomeQueryResources(Component seamComponent) {
        // We can always instantiate this safely here because it can't have
        // dependencies!
        AbstractResource instance =
                (AbstractResource) seamComponent.newInstance();
        String path = instance.getPath();
        if (path != null) {
            if (!path.startsWith("/")) {
                path = "/" + path;
            }

            log.debug(
                    "registering resource, configured ResourceHome/Query on path {1}, as Seam component: {0}",
                    seamComponent.getName(),
                    path
                    );

            ResourceFactory factory = new SeamResteasyResourceFactory(
                    seamComponent.getBeanClass(),
                    seamComponent,
                    getDispatcher().getProviderFactory()
                    );

            getDispatcher().getRegistry().addResourceFactory(factory, path);
        } else {
            log.error(
                    "Unable to register {0} resource on null path, check components.xml",
                    seamComponent.getName());
        }
    }

    protected void registerSeamComponentResource(Component seamComponent) {
        log.debug("registering resource as Seam component: {0}",
                seamComponent.getName());

        ResourceFactory factory = new SeamResteasyResourceFactory(
                seamComponent.getBeanClass(),
                seamComponent,
                getDispatcher().getProviderFactory()
                );

        getDispatcher().getRegistry().addResourceFactory(factory);
    }

    protected void registerInterfaceSeamComponentResource(
            Component seamComponent, Class resourceInterface) {
        log.debug(
                "registering resource, annotated interface {1}, as Seam component: {0}",
                seamComponent.getName(),
                resourceInterface.getName()
                );

        ResourceFactory factory = new SeamResteasyResourceFactory(
                resourceInterface,
                seamComponent,
                getDispatcher().getProviderFactory()
                );

        getDispatcher().getRegistry().addResourceFactory(factory);
    }

    protected void registerStatelessEJBResource(Class ejbImplementationClass) {
        String jndiName = getJndiName(ejbImplementationClass);

        log.debug(
                "registering resource, stateless EJB implementation {1}, as RESTEasy JNDI resource name: {0}",
                jndiName,
                ejbImplementationClass.getName()
                );
        getDispatcher().getRegistry().addJndiResource(jndiName);
    }

    protected void registerPlainResource(Class plainResourceClass) {
        log.debug("registering resource, event-scoped JAX-RS lifecycle: {0}",
                plainResourceClass.getName());
        getDispatcher().getRegistry().addResourceFactory(
                new POJOResourceFactory(plainResourceClass));
    }

    protected void registerSeamComponentProvider(Component seamComponent) {
        log.debug("registering provider as Seam component: {0}",
                seamComponent.getName());
        getDispatcher().getProviderFactory().registerProviderInstance(
                Component.getInstance(seamComponent.getName())
                );
    }

    protected Class getAnnotatedInterface(
            Class<? extends Annotation> annotation, Component seamComponent) {
        Class resourceInterface = null;
        for (Class anInterface : seamComponent.getBusinessInterfaces()) {
            if (anInterface.isAnnotationPresent(annotation)) {
                if (resourceInterface != null) {
                    throw new IllegalStateException(
                            "Only one business interface can be annotated "
                                    + annotation + ": " + seamComponent);
                }
                resourceInterface = anInterface;
            }
        }
        return resourceInterface;
    }

    protected String getJndiName(Class<?> beanClass) {
        if (beanClass.isAnnotationPresent(JndiName.class)) {
            return beanClass.getAnnotation(JndiName.class).value();
        } else {
            String jndiPattern = Init.instance().getJndiPattern();
            if (jndiPattern == null) {
                throw new IllegalArgumentException(
                        "You must specify org.jboss.seam.core.init.jndiPattern or use @JndiName: "
                                + beanClass.getName());
            }
            return jndiPattern
                    .replace("#{ejbName}", Seam.getEjbName(beanClass));
        }
    }

}
