package org.zanata.service;

import java.util.Hashtable;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.junit.rules.ExternalResource;
import org.mockito.Mockito;

/**
 * A JUnit test rule that allows you to use a mock context in test.
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class MockInitialContextRule extends ExternalResource {

    private final Context context;

    public MockInitialContextRule(Context context) {
        this.context = context;
    }

    /**
     * Create a rule with a mockito mocked context.
     */
    public MockInitialContextRule() {
        context = Mockito.mock(Context.class);
    }

    @Override
    protected void before() throws Throwable {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                MockInitialContextFactory.class.getName());
        MockInitialContextFactory.setCurrentContext(context);
    }

    @Override
    protected void after() {
        System.clearProperty(Context.INITIAL_CONTEXT_FACTORY);
        MockInitialContextFactory.clearCurrentContext();
    }

    public static class MockInitialContextFactory implements
            InitialContextFactory {

        private static final ThreadLocal<Context> currentContext =
                new ThreadLocal<>();

        @Override
        public Context getInitialContext(Hashtable<?, ?> environment) throws
                NamingException {
            return currentContext.get();
        }

        public static void setCurrentContext(Context context) {
            currentContext.set(context);
        }

        public static void clearCurrentContext() {
            currentContext.remove();
        }

    }
}
