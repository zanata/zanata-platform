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
import org.fedorahosted.flies.rest.service.MockResourceFactory;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.resteasy.client.core.executors.InMemoryClientExecutor;
import org.jboss.resteasy.core.Dispatcher;
import org.jboss.resteasy.mock.MockDispatcherFactory;
import org.jboss.resteasy.spi.ResourceFactory;
import org.testng.annotations.BeforeMethod;

public abstract class FliesRestTest extends FliesDbunitJpaTest {
	
	private ClientRequestFactory clientRequestFactory;
	
	@BeforeMethod
	public void prepareRestEasyClientFramework() {
		
		Dispatcher dispatcher = MockDispatcherFactory.createDispatcher();
		
		for(Object obj : getResources() ) {
			ResourceFactory factory = new MockResourceFactory(obj); 
			dispatcher.getRegistry().addResourceFactory(factory);
		}
		
		// register Exception Mappers
		for(Class<? extends ExceptionMapper<? extends Throwable>> mapper : getExceptionMappers()) {
			dispatcher.getProviderFactory().addExceptionMapper(mapper);
		}
		
		clientRequestFactory = 
			new ClientRequestFactory(
					new InMemoryClientExecutor(dispatcher), URI.create("/"));
		
	}

	protected Set<Object> getResources() {
		return new HashSet<Object>();
	}

	protected Set<Class<? extends ExceptionMapper<? extends Throwable>>> getExceptionMappers(){
		Set<Class<? extends ExceptionMapper<? extends Throwable>>> mappers = new HashSet<Class<? extends ExceptionMapper<? extends Throwable>>>();
		mappers.add(AuthorizationExceptionMapper.class);
		mappers.add(HibernateExceptionMapper.class);
		mappers.add(InvalidStateExceptionMapper.class);
		mappers.add(NoSuchEntityExceptionMapper.class);
		mappers.add(NotLoggedInExceptionMapper.class);
		return mappers;
	}
	
	protected ClientRequestFactory getClientRequestFactory() {
		return clientRequestFactory;
	}
}
