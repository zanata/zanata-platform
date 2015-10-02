package org.zanata.rest;

import java.util.Collection;

import lombok.extern.slf4j.Slf4j;

import org.jboss.resteasy.core.Dispatcher;
import org.jboss.seam.Component;
import org.apache.deltaspike.core.api.exclude.Exclude;
import org.apache.deltaspike.core.api.projectstage.ProjectStage;
import javax.inject.Named;
import org.jboss.seam.annotations.Observer;
import org.jboss.seam.annotations.Startup;
import org.jboss.seam.deployment.AnnotationDeploymentHandler;
import org.jboss.seam.deployment.HotDeploymentStrategy;
import org.zanata.seam.resteasy.ResteasyBootstrap;
import org.zanata.seam.resteasy.SeamResteasyProviderFactory;

@Named("org.jboss.seam.resteasy.bootstrap")
@javax.enterprise.context.ApplicationScoped

/* TODO [CDI] Ensure that RESTEasy 3 uses RestLimitingSynchronousDispatcher */

//@Install(classDependencies = "org.jboss.resteasy.spi.ResteasyProviderFactory",
//        precedence = Install.DEPLOYMENT)
@Slf4j
public class ZanataResteasyBootstrap extends ResteasyBootstrap {

    @Override
    protected Dispatcher createDispatcher(
            SeamResteasyProviderFactory providerFactory) {
        return new RestLimitingSynchronousDispatcher(providerFactory);
    }

}
