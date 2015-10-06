package org.zanata.rest;

import lombok.extern.slf4j.Slf4j;

import org.jboss.resteasy.core.Dispatcher;
import javax.inject.Named;
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
