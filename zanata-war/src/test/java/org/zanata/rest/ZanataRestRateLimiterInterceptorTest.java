package org.zanata.rest;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.collections.map.MultiValueMap;
import org.jboss.resteasy.core.ResourceMethod;
import org.jboss.resteasy.spi.HttpRequest;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.zanata.ApplicationConfiguration;
import com.google.common.base.Throwables;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.zanata.seam.SeamAutowire.getComponentName;
import static org.zanata.seam.SeamAutowire.instance;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Test(groups = "unit-tests")
public class ZanataRestRateLimiterInterceptorTest {
    private ZanataRestRateLimiterInterceptor interceptor;
    @Mock
    private ApplicationConfiguration applicationConfiguration;
    @Mock
    private HttpRequest request;
    @Mock
    private ResourceMethod method;
    private RateLimiterHolder rateLimiterHolder;

    @BeforeMethod
    public void beforeMethod() {
        MockitoAnnotations.initMocks(this);

        rateLimiterHolder = spy(new RateLimiterHolder());
        interceptor =
                instance()
                        .reset()
                        .use(getComponentName(ApplicationConfiguration.class),
                                applicationConfiguration)
                        // to ensure it's application scoped a.k.a singleton
                        .use(getComponentName(RateLimiterHolder.class),
                                rateLimiterHolder)
                        .autowire(ZanataRestRateLimiterInterceptor.class);
     // @formatter:off
        HttpHeaders header = mock(HttpHeaders.class);
        when(request.getHttpHeaders()).thenReturn(header);
        MultivaluedMap<String, String> headers = mock(MultivaluedMap.class);
        when(header.getRequestHeaders()).thenReturn(headers);
        when(headers.getFirst(HeaderHelper.X_AUTH_TOKEN_HEADER)).thenReturn("apiKey");
        // @formatter:on
    }

    @Test
    public void willSkipIfNoApiKey() {
        when(
                request.getHttpHeaders().getRequestHeaders()
                        .getFirst(HeaderHelper.X_AUTH_TOKEN_HEADER))
                .thenReturn(null);

        interceptor.preProcess(request, method);

        verifyZeroInteractions(rateLimiterHolder);
    }

    @Test
    public void willSkipIfRateLimitSwitchIsOff() throws ExecutionException {

        when(applicationConfiguration.getRateLimitSwitch()).thenReturn(false);

        interceptor.preProcess(request, method);

        verifyZeroInteractions(rateLimiterHolder);
    }

    @Test(expectedExceptions = WebApplicationException.class)
    public void willFirstTryAcquire() throws InterruptedException {
        when(request.getUri()).thenReturn(mock(UriInfo.class));

        when(rateLimiterHolder.getLimitConfig()).thenReturn(
                new RestRateLimiter.RateLimitConfig(1, 1, 100.0));
        when(applicationConfiguration.getRateLimitSwitch()).thenReturn(true);

        Callable<Void> callable = new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                interceptor.preProcess(request, method);
                return null;
            }
        };
        int threads = 2;
        List<Callable<Void>> callables = Collections.nCopies(threads, callable);
        ExecutorService executorService = Executors.newFixedThreadPool(threads);
        List<Future<Void>> futures = executorService.invokeAll(callables);
        for (Future<Void> future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                throw Throwables.propagate(e.getCause());
            }
        }
    }
}
