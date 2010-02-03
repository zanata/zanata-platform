package org.fedorahosted.flies.errai;

import java.util.Map;

import org.fedorahosted.flies.webtrans.BusFactory;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.server.annotations.ExtensionComponent;
import org.jboss.errai.bus.server.ext.ErraiConfigExtension;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;

import com.google.inject.Inject;
import com.google.inject.Provider;

@ExtensionComponent
public class AppConfigurator implements ErraiConfigExtension {
	private static final Log log = Logging.getLog(AppConfigurator.class);

    private MessageBus bus;

    @Inject
    public AppConfigurator(MessageBus bus) {
        this.bus = bus;
    }

    @SuppressWarnings("unchecked")
	public void configure(Map<Class, Provider> bindings, Map<String, Provider> resourceProviders) {
        // provide extension points here
		BusFactory.initMessageBus(bus);
		log.info("ErraiBus added to AppContext");
    }
}