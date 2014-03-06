package org.zanata.rest;

import java.io.IOException;
import java.util.Collection;

import javax.ws.rs.core.Response.Status;

import lombok.extern.slf4j.Slf4j;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.core.SynchronousDispatcher;
import org.jboss.resteasy.core.interception.InterceptorRegistry;
import org.jboss.resteasy.spi.HttpRequest;
import org.jboss.resteasy.spi.HttpResponse;
import org.jboss.resteasy.spi.UnhandledException;
import org.jboss.resteasy.spi.interception.PostProcessInterceptor;
import org.jboss.resteasy.spi.interception.PreProcessInterceptor;
import org.jboss.seam.Component;
import org.jboss.seam.ScopeType;
import org.jboss.seam.annotations.AutoCreate;
import org.jboss.seam.annotations.In;
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
        InterceptorRegistry<PreProcessInterceptor>
                preRegistry =
                getDispatcher().getProviderFactory()
                        .getServerPreProcessInterceptorRegistry();
        preRegistry.register(ZanataRestSecurityInterceptor.class);

        ZanataRestRateLimiterInterceptor zanataRestRateLimiterInterceptor =
                new ZanataRestRateLimiterInterceptor();
        preRegistry.register(zanataRestRateLimiterInterceptor);
        InterceptorRegistry<PostProcessInterceptor>
                postRegistry =
                getDispatcher().getProviderFactory()
                        .getServerPostProcessInterceptorRegistry();
        postRegistry.register(zanataRestRateLimiterInterceptor);
    }

    @Override
    protected Dispatcher createDispatcher(
            SeamResteasyProviderFactory providerFactory) {
        return new SynchronousDispatcher(providerFactory) {
            @Override
            public void invoke(HttpRequest request, HttpResponse response) {
                try {
                    super.invoke(request, response);
                } catch (UnhandledException e) {
                    Throwable cause = e.getCause();
                    log.error("Failed to process REST request", cause);
                    try {
                        // see https://issues.jboss.org/browse/RESTEASY-411
                        if (cause instanceof IllegalArgumentException
                                && cause.getMessage().contains(
                                        "Failure parsing MediaType")) {
                            response.sendError(
                                    Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(),
                                    cause.getMessage());
                        } else {
                            response.sendError(
                                    Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                                    "Error processing Request");
                        }

                    } catch (IOException ioe) {
                        log.error(
                                "Failed to send error on failed REST request",
                                ioe);
                    }
                }
            }
        };
    }

}
