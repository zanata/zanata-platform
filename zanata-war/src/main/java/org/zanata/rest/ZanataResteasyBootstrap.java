package org.zanata.rest;

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.Install;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.deployment.AnnotationDeploymentHandler;
import org.jboss.seam.deployment.HotDeploymentStrategy;
import org.jboss.seam.resteasy.ResteasyBootstrap;
import org.jboss.seam.resteasy.SeamResteasyProviderFactory;

@Name("org.jboss.seam.resteasy.bootstrap")
@Scope(ScopeType.APPLICATION)
@Startup
@AutoCreate
@Install(classDependencies = "org.jboss.resteasy.spi.ResteasyProviderFactory",
        precedence = Install.DEPLOYMENT)
@Slf4j
public class ZanataResteasyBootstrap extends ResteasyBootstrap {

    @Observer("org.jboss.seam.postReInitialization")
    public void registerHotDeployedClasses() {

        Collection<Component> seamComponents = findSeamComponents();

        // Also scan for hot deployed components
        HotDeploymentStrategy hotDeployment = HotDeploymentStrategy.instance();
        if (hotDeployment != null && hotDeployment.available()) {
            log.info("scanning for hot deployable JAX-RS components");
            AnnotationDeploymentHandler hotDeploymentHandler =
                    (AnnotationDeploymentHandler) hotDeployment
                            .getDeploymentHandlers().get(
                                    AnnotationDeploymentHandler.NAME);
            registerProviders(seamComponents,
                    findProviders(hotDeploymentHandler));
            registerResources(seamComponents,
                    findResources(hotDeploymentHandler));
        }
    }

    @Override
    protected void initDispatcher() {
        super.initDispatcher();
                getDispatcher().getProviderFactory()
                .getServerPreProcessInterceptorRegistry()
                .register(ZanataRestSecurityInterceptor.class);
        getDispatcher().getProviderFactory()
                .getServerPreProcessInterceptorRegistry()
                .register(ZanataRestVersionInterceptor.class);
    }

    @Override
    protected Dispatcher createDispatcher(
            SeamResteasyProviderFactory providerFactory) {
        return new RestLimitingSynchronousDispatcher(providerFactory);
    }

}
