package org.zanata.seam.interceptor;

import java.lang.reflect.Method;
import javax.rmi.CORBA.Stub;

import org.jboss.seam.intercept.InvocationContext;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ApplicationConfiguration;
import org.zanata.annotation.RateLimiting;
import org.zanata.seam.SeamAutowire;
import org.zanata.security.ZanataIdentity;

import static org.mockito.Mockito.*;
import static org.zanata.seam.SeamAutowire.getComponentName;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class RateLimitingInterceptorTest {
    private RateLimitingInterceptor interceptor;
    @Mock
    private InvocationContext ic;
    @Mock
    private ApplicationConfiguration applicationConfiguration;
    @Mock
    private ZanataIdentity zanataIdentity;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);

        // @formatter:off
        interceptor = SeamAutowire.instance().reset()
                .use(getComponentName(ApplicationConfiguration.class),
                        applicationConfiguration)
                .use(getComponentName(ZanataIdentity.class), zanataIdentity)
                .autowire(RateLimitingInterceptor.class);
        // @formatter:on
    }

    @Test
    public void willNotLimitMethodWithoutAnnotation() throws Exception {
        when(ic.getMethod()).thenReturn(StubClass.getMethodWithAnnotation());

        interceptor.aroundInvoke(ic);

        verify(ic).proceed();
        verifyZeroInteractions(applicationConfiguration, zanataIdentity);
    }

    @Test
    public void willNotLimitRequestWithoutAPIKey() throws Exception {
        when(ic.getMethod()).thenReturn(StubClass.getMethodWithoutAnnotation());

        interceptor.aroundInvoke(ic);
        verify(ic).proceed();
    }

    private static class StubClass {
        static final StubClass stub = new StubClass();

        private static Method getMethodWithoutAnnotation() throws Exception {
            return StubClass.class.getDeclaredMethod("methodWithoutLimit");
        }

        private static Method getMethodWithAnnotation() throws Exception {
            return StubClass.class.getDeclaredMethod("methodWithLimit");
        }

        private void methodWithoutLimit() {
        }

        @RateLimiting
        private void methodWithLimit() {
        }
    }
}
