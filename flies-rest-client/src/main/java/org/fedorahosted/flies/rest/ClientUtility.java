package org.fedorahosted.flies.rest;

import java.net.URL;
import java.util.Arrays;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.BaseClientResponse;

public class ClientUtility {
	public static void checkResult(ClientResponse response, URL url) {
		if (response.getStatus() >= 399) {
			String annots = "";
			String entity = "";
			if (response instanceof BaseClientResponse) {
				BaseClientResponse resp = (BaseClientResponse) response;
				annots = Arrays.asList(resp.getAnnotations()).toString();
				try {
					entity = ", entity: "+resp.getEntity(String.class);
				} catch (Exception e) {
					entity = "";
				}
			}
			throw new RuntimeException(
					"operation returned "+response.getStatus()+": "+
					Response.Status.fromStatusCode(response.getStatus())+
					entity+", url: "+url+", annotations: "+annots);
		}
	}
}
