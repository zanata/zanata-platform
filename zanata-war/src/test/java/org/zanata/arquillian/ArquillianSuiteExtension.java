package org.zanata.arquillian;

import java.util.concurrent.Callable;

import org.jboss.arquillian.config.descriptor.api.ArquillianDescriptor;
import org.jboss.arquillian.container.spi.Container;
import org.jboss.arquillian.container.spi.ContainerRegistry;
import org.jboss.arquillian.container.spi.client.container.DeployableContainer;
import org.jboss.arquillian.container.spi.client.deployment.Deployment;
import org.jboss.arquillian.container.spi.client.deployment.DeploymentScenario;
import org.jboss.arquillian.container.spi.client.protocol.metadata.ProtocolMetaData;
import org.jboss.arquillian.container.spi.event.DeployDeployment;
import org.jboss.arquillian.container.spi.event.DeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.DeploymentEvent;
import org.jboss.arquillian.container.spi.event.UnDeployDeployment;
import org.jboss.arquillian.container.spi.event.UnDeployManagedDeployments;
import org.jboss.arquillian.container.spi.event.container.AfterStart;
import org.jboss.arquillian.container.spi.event.container.BeforeStop;
import org.jboss.arquillian.container.test.impl.client.deployment.event.GenerateDeployment;
import org.jboss.arquillian.core.api.Event;
import org.jboss.arquillian.core.api.Instance;
import org.jboss.arquillian.core.api.InstanceProducer;
import org.jboss.arquillian.core.api.annotation.Inject;
import org.jboss.arquillian.core.api.annotation.Observes;
import org.jboss.arquillian.core.api.event.ManagerStarted;
import org.jboss.arquillian.core.spi.EventContext;
import org.jboss.arquillian.core.spi.LoadableExtension;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.arquillian.test.spi.annotation.ClassScoped;
import org.jboss.arquillian.test.spi.annotation.TestScoped;
import org.jboss.arquillian.test.spi.context.ClassContext;
import org.jboss.arquillian.test.spi.event.suite.Before;
import org.jboss.arquillian.test.spi.event.suite.BeforeClass;

public class ArquillianSuiteExtension implements LoadableExtension {

    public void register(ExtensionBuilder builder) {
        builder.observer(SuiteDeployer.class);
    }

    public static class SuiteDeployer {

        private Class<?> deploymentClass;

        private DeploymentScenario suiteDeploymentScenario;

        @Inject
        @ClassScoped
        private InstanceProducer<DeploymentScenario> classDeploymentScenario;

        @Inject
        private Event<DeploymentEvent> deploymentEvent;

        @Inject
        private Event<GenerateDeployment> generateDeploymentEvent;

        @Inject
        // Active some form of ClassContext around our deployments due to
        // assumption bug in AS7 extension.
        private Instance<ClassContext> classContext;

        private ProtocolMetaData cachedProtocolMetaData;

        @TestScoped
        @Inject
        private InstanceProducer<ProtocolMetaData> testScopedProtocolMetaData;

        public void startup(@Observes(precedence = -100) ManagerStarted event,
                ArquillianDescriptor descriptor) {
            deploymentClass = getDeploymentClass(descriptor);

            executeInClassScope(new Callable<Void>() {
                public Void call() throws Exception {
                    generateDeploymentEvent.fire(new GenerateDeployment(
                            new TestClass(deploymentClass)));
                    suiteDeploymentScenario = classDeploymentScenario.get();
                    return null;
                }
            });
        }

        public void deploy(@Observes final AfterStart event,
                final ContainerRegistry registry) {
            executeInClassScope(new Callable<Void>() {
                public Void call() throws Exception {
                    for (Deployment d : suiteDeploymentScenario.deployments()) {
                        deploymentEvent.fire(new DeployDeployment(
                            findContainer(registry,
                                event.getDeployableContainer()), d));
                    }
                    return null;
                }
            });
        }

        public void undeploy(@Observes final BeforeStop event,
                final ContainerRegistry registry) {
            executeInClassScope(new Callable<Void>() {
                public Void call() throws Exception {
                    for (Deployment d : suiteDeploymentScenario.deployments()) {
                        deploymentEvent.fire(new UnDeployDeployment(
                                findContainer(registry,
                                        event.getDeployableContainer()), d));
                    }
                    return null;
                }
            });
        }

        public void blockDeployManagedDeployments(
                @Observes EventContext<DeployManagedDeployments> ignored) {
            // We need to block DeployManagedDeployments event
        }

        public void storeProtocolMetaData(
                @Observes ProtocolMetaData protocolMetaData) {
            cachedProtocolMetaData = protocolMetaData;
        }

        public void resotreProtocolMetaData(
                @Observes EventContext<Before> eventContext) {
            testScopedProtocolMetaData.set(cachedProtocolMetaData);
            eventContext.proceed();
        }

        public void restoreDeploymentScenario(
                @Observes EventContext<BeforeClass> event) {
            // Setup the Suite level scenario as if it came from the TestClass
            event.proceed();
            classDeploymentScenario.set(suiteDeploymentScenario);
        }

        public void blockUnDeployManagedDeployments(
                @Observes EventContext<UnDeployManagedDeployments> ignored) {
            // We need to block UnDeployManagedDeployments event
        }

        private void executeInClassScope(Callable<Void> call) {
            try {
                classContext.get().activate(deploymentClass);
                call.call();
            } catch (Exception e) {
                throw new RuntimeException("Could not invoke operation", e);
            } finally {
                classContext.get().deactivate();
            }
        }

        private Container findContainer(ContainerRegistry registry,
                DeployableContainer<?> deployable) {
            for (Container container : registry.getContainers()) {
                if (container.getDeployableContainer() == deployable) {
                    return container;
                }
            }
            return null;
        }

        private Class<?> getDeploymentClass(ArquillianDescriptor descriptor) {
            if (descriptor == null) {
                throw new IllegalArgumentException(
                        "Descriptor must be specified");
            }
            String className =
                    descriptor.extension("suite").getExtensionProperties()
                            .get("deploymentClass");
            if (className == null) {
                throw new IllegalArgumentException(
                        "A extension element with property deploymentClass must be specified in arquillian.xml");
            }
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(
                        "Could not load defined deploymentClass: " + className, e);
            }
        }
    }
}
