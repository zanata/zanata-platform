package org.fedorahosted.flies.rest.service;

import javax.ws.rs.core.Response;

import org.fedorahosted.flies.rest.dto.Documents;

public interface DocumentsServiceAction {

    public Response get();

    public Response post(Documents docs);

    public Response put(Documents docs);

}