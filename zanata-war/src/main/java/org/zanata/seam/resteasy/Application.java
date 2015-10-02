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

import org.jboss.seam.Component;
import javax.inject.Named;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;

import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * RESTEasy and JAX-RS configuration, override in components.xml to customize
 * RESTful request processing and RESTEasy settings.
 *
 * @author Christian Bauer
 */
@Named("org.jboss.seam.resteasy.application")
@javax.enterprise.context.ApplicationScoped
@Install(precedence = Install.BUILT_IN)

public class Application extends javax.ws.rs.core.Application {

    final private Map<Class<?>, Set<Component>> providerClasses =
            new HashMap<Class<?>, Set<Component>>();
    final private Map<Class<?>, Set<Component>> resourceClasses =
            new HashMap<Class<?>, Set<Component>>();

    private List<String> providerClassNames = new ArrayList<String>();
    private List<String> resourceClassNames = new ArrayList<String>();

    private Map<String, String> mediaTypeMappings =
            new HashMap<String, String>();
    private Map<String, String> languageMappings =
            new HashMap<String, String>();

    private boolean scanProviders = true;
    private boolean scanResources = true;
    private boolean useBuiltinProviders = true;
    private boolean destroySessionAfterRequest = true;

    private String resourcePathPrefix = "/rest";
    private boolean stripSeamResourcePath = true;

    public Application() {
        super();
    }

    public Set<Class<?>> getProviderClasses() {
        return providerClasses.keySet();
    }

    @Override
    public Set<Class<?>> getClasses() {
        return resourceClasses.keySet();
    }

    public void addProviderClass(Class<?> clazz) {
        providerClasses.put(clazz, null);
    }

    public void addProviderClass(Class<?> clazz, Component component) {
        Set<Component> components = providerClasses.get(clazz);
        if (components == null) {
            components = new HashSet<Component>();
            providerClasses.put(clazz, components);
        }
        components.add(component);
    }

    public void removeProviderClass(Class<?> clazz) {
        providerClasses.remove(clazz);
    }

    public void addResourceClass(Class<?> clazz) {
        resourceClasses.put(clazz, null);
    }

    public void addResourceClass(Class<?> clazz, Set<Component> newComponents) {
        Set<Component> components = resourceClasses.get(clazz);
        if (components == null) {
            components = new HashSet<Component>();
            resourceClasses.put(clazz, components);
        }
        components.addAll(newComponents);
    }

    public void removeResourceClass(Class<?> clazz) {
        resourceClasses.remove(clazz);
    }

    public Set<Component> getProviderClassComponent(Class clazz) {
        return providerClasses.get(clazz) != null ? providerClasses.get(clazz)
                : null;
    }

    public Set<Component> getResourceClassComponent(Class clazz) {
        return resourceClasses.get(clazz) != null ? resourceClasses.get(clazz)
                : null;
    }

    public Map<String, MediaType> getMediaTypeMappings() {
        Map<String, MediaType> extMap = new HashMap<String, MediaType>();
        for (String ext : mediaTypeMappings.keySet()) {
            String value = mediaTypeMappings.get(ext);
            extMap.put(ext, MediaType.valueOf(value));
        }
        return extMap;
    }

    public void setMediaTypeMappings(Map<String, String> mediaTypeMappings) {
        this.mediaTypeMappings = mediaTypeMappings;
    }

    public Map<String, String> getLanguageMappings() {
        return languageMappings;
    }

    public void setLanguageMappings(Map<String, String> languageMappings) {
        this.languageMappings = languageMappings;
    }

    public List<String> getProviderClassNames() {
        return providerClassNames;
    }

    public void setProviderClassNames(List<String> providerClassNames) {
        this.providerClassNames = providerClassNames;
    }

    public List<String> getResourceClassNames() {
        return resourceClassNames;
    }

    public void setResourceClassNames(List<String> resourceClassNames) {
        this.resourceClassNames = resourceClassNames;
    }

    public boolean isScanProviders() {
        return scanProviders;
    }

    public void setScanProviders(boolean scanProviders) {
        this.scanProviders = scanProviders;
    }

    public boolean isScanResources() {
        return scanResources;
    }

    public void setScanResources(boolean scanResources) {
        this.scanResources = scanResources;
    }

    public boolean isUseBuiltinProviders() {
        return useBuiltinProviders;
    }

    public void setUseBuiltinProviders(boolean useBuiltinProviders) {
        this.useBuiltinProviders = useBuiltinProviders;
    }

    public boolean isDestroySessionAfterRequest() {
        return destroySessionAfterRequest;
    }

    public void
            setDestroySessionAfterRequest(boolean destroySessionAfterRequest) {
        this.destroySessionAfterRequest = destroySessionAfterRequest;
    }

    public String getResourcePathPrefix() {
        return resourcePathPrefix;
    }

    public void setResourcePathPrefix(String resourcePathPrefix) {
        this.resourcePathPrefix = resourcePathPrefix;
    }

    public boolean isStripSeamResourcePath() {
        return stripSeamResourcePath;
    }

    public void setStripSeamResourcePath(boolean stripSeamResourcePath) {
        this.stripSeamResourcePath = stripSeamResourcePath;
    }
}
