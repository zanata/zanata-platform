package org.fedorahosted.flies.core.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang.StringUtils;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.BaseClientResponse;
import org.jboss.resteasy.core.Headers;
import org.jboss.seam.mock.AbstractSeamTest;
import org.jboss.seam.mock.EnhancedMockHttpServletRequest;
import org.jboss.seam.mock.EnhancedMockHttpServletResponse;
import org.jboss.seam.mock.ResourceRequestEnvironment;
import org.jboss.seam.mock.ResourceRequestEnvironment.Method;
import org.jboss.seam.mock.ResourceRequestEnvironment.ResourceRequest;

public class SeamMockClientExecutor implements ClientExecutor {

	private AbstractSeamTest seamTest;

	public SeamMockClientExecutor(AbstractSeamTest seamTest) {
		this.seamTest = seamTest;
	}

	@Override
	public ClientResponse execute(final ClientRequest clientRequest)
			throws Exception {
		RestEasyResourceRequest resourceRequest = new RestEasyResourceRequest(seamTest, clientRequest);
		return resourceRequest.execute();
	}
	
	private class RestEasyResourceRequest extends ResourceRequest {
		
		private BaseClientResponse clientResponse;
		private final ClientRequest clientRequest;
		
		public RestEasyResourceRequest(AbstractSeamTest seamTest, ClientRequest clientRequest) throws Exception{
			super(  new ResourceRequestEnvironment(seamTest), 
					Method.valueOf(clientRequest.getHttpMethod()), 
					clientRequest.getUri().toString());
			this.clientRequest = clientRequest;
		}
		
		@Override
		protected void prepareRequest(EnhancedMockHttpServletRequest request) {
			MultivaluedMap<String, String> headers = clientRequest.getHeaders();
			for (String hVal : headers.keySet()) {
				request.addHeader(hVal, StringUtils.join(headers.get(hVal),
						" "));
			}
	
		      if (clientRequest.getHeaders() != null)
		      {
		         for (Map.Entry<String, List<String>> header : clientRequest.getHeaders().entrySet())
		         {
		            List<String> values = header.getValue();
		            for (String value : values)
		            {
		               request.addHeader(header.getKey(), value);
		            }
		         }
		      }
		      if (clientRequest.getBody() != null && !clientRequest.getFormParameters().isEmpty())
		         throw new RuntimeException("You cannot send both form parameters and an entity body");

		      if (!clientRequest.getFormParameters().isEmpty())
		      {
		         for (Map.Entry<String, List<String>> formParam : clientRequest.getFormParameters().entrySet())
		         {
		            List<String> values = formParam.getValue();
		            for (String value : values)
		            {
		               request.addParameter(formParam.getKey(), value);
		            }
		         }
		      }
		      if (clientRequest.getBody() != null)
		      {
		         if (clientRequest.getHttpMethod().equals("GET")) 
		        	 throw new RuntimeException("A GET request cannot have a body.");
		      }
			
		}
		
		@Override
		protected void onResponse(final EnhancedMockHttpServletResponse response) {
			
			clientResponse.setStatus(response.getStatus());
			
			MultivaluedMap<String, String> headers = new Headers<String>();
			for (Object hObj : response.getHeaderNames()) {
				String h = (String) hObj;
				ArrayList<String> hVals = new ArrayList<String>();
				for(Object hVal : response.getHeaders(h)){
					hVals.add((String) hVal);
				}
				headers.put(h, hVals);
			}
			clientResponse.setHeaders(headers);
			
			clientResponse.setProviderFactory(clientRequest.getProviderFactory());
			clientResponse.setStreamFactory( new MockClientResponseStreamFactory(response) );
		}
	
		public ClientResponse execute() throws Exception{
			clientResponse = new MockClientResponse();
			this.run();
			return clientResponse;
		}
		
	}
	
    private static class MockClientResponse<T> extends BaseClientResponse<T> {
    	private static final BaseClientResponse.BaseClientResponseStreamFactory MOCK_FACTORY = new BaseClientResponseStreamFactory() {
			@Override
			public InputStream getInputStream() throws IOException { return null; }
			@Override
			public void performReleaseConnection() {}
		};
		
		public MockClientResponse() {
			super( MOCK_FACTORY );
		}
	}
	
	private class MockClientResponseStreamFactory implements BaseClientResponse.BaseClientResponseStreamFactory {
		private final EnhancedMockHttpServletResponse response;
		private final ByteArrayInputStream inputStream;
		
		public MockClientResponseStreamFactory(EnhancedMockHttpServletResponse response) {
			this.response = response;
			this.inputStream = new ByteArrayInputStream(response.getContentAsByteArray());
		}
		
		@Override
		public InputStream getInputStream() throws IOException {
			return inputStream;
		}

		@Override
		public void performReleaseConnection() {}
	}
}
