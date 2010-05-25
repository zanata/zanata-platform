package org.fedorahosted.flies;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ext.ExceptionMapper;

import org.fedorahosted.flies.rest.AuthorizationExceptionMapper;
import org.fedorahosted.flies.rest.HibernateExceptionMapper;
import org.fedorahosted.flies.rest.InvalidStateExceptionMapper;
import org.fedorahosted.flies.rest.NoSuchEntityExceptionMapper;
import org.fedorahosted.flies.rest.NotLoggedInExceptionMapper;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.core.executors.InMemoryClientExecutor;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.spi.ResourceFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class FliesRestTest extends FliesDbunitJpaTest {
	
	protected static final URI MOCK_BASE_URI = URI.create("http://mockhost");
	
	private ClientRequestFactory clientRequestFactory;
	protected final Set<Class<? extends ExceptionMapper<? extends Throwable>>> exceptionMappers = new HashSet<Class<? extends ExceptionMapper<? extends Throwable>>>();
	protected final Set<Object> resources = new HashSet<Object>();
	
	@BeforeMethod
	public final void prepareRestEasyFramework() {
		
		Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
		prepareResources();
		prepareExceptionMappers();
		
		for(Object obj : resources ) {
			ResourceFactory factory = new MockResourceFactory(obj); 
			dispatcher.getRegistry().addResourceFactory(factory);
		}
		
		// register Exception Mappers
		for(Class<? extends ExceptionMapper<? extends Throwable>> mapper : exceptionMappers ) {
			dispatcher.getProviderFactory().addExceptionMapper(mapper);
		}
		InMemoryClientExecutor executor = new InMemoryClientExecutor(dispatcher); 
		executor.setBaseUri(MOCK_BASE_URI);
		clientRequestFactory = 
			new ClientRequestFactory(
					executor, MOCK_BASE_URI);
		
	}
	
	@AfterMethod
	public final void cleanUpRestEasyFramework() {
		exceptionMappers.clear();
		resources.clear();
	}

	/**
	 * Clients should add instances of the tested server side Resource
	 * to the protected resources set within this method.
	 */
	protected abstract void prepareResources();

	/**
	 * Override this to add custom server-side ExceptionMappers for
	 * the test, by configuring the protected exceptionMappers set.
	 */
	protected void prepareExceptionMappers(){
		exceptionMappers.add(AuthorizationExceptionMapper.class);
		exceptionMappers.add(HibernateExceptionMapper.class);
		exceptionMappers.add(InvalidStateExceptionMapper.class);
		exceptionMappers.add(NoSuchEntityExceptionMapper.class);
		exceptionMappers.add(NotLoggedInExceptionMapper.class);
	}
	
	/**
	 * Retrieve the configured request factory
	 * 
	 * @return a ClientRequestFactory configured for your environment.
	 */
	protected final ClientRequestFactory getClientRequestFactory() {
		return clientRequestFactory;
	}
	
	/**
	 * Creates a URI suitable for passing to ClientRequestFactory for
	 * a given resource.  
	 * 
	 * @param resourcePath the class-level @Path structure for the 
	 * 	      resource being tested.
	 * @return a URI suitable for passing to ClientRequestFactory for
	 *         the resource being tested.
	 */
	protected final URI createBaseURI(String resourcePath) {
		return MOCK_BASE_URI.resolve(resourcePath);		
	}
}
