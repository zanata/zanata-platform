package org.fedorahosted.flies.rest.client;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.fedorahosted.flies.rest.MediaTypes;
import org.fedorahosted.flies.rest.dto.deprecated.Documents;
import org.jboss.resteasy.client.ClientResponse;

public interface IDocumentsResource
{

   // @Path("/d/{documentId}")
   // public IDocumentResource getDocument(@PathParam("documentId") String
   // documentId);

   /**
    * Adds multiple Documents
    */
   @POST
   @Consumes( { MediaTypes.APPLICATION_FLIES_DOCUMENTS_XML, MediaTypes.APPLICATION_FLIES_DOCUMENTS_JSON })
   public ClientResponse post(Documents documents);

   /**
    * Replaces the existing set of documents (possibly should be POST to ., not
    * ./replace)
    * 
    * @param documents
    * @return
    */
   @PUT
   @Consumes( { MediaTypes.APPLICATION_FLIES_DOCUMENTS_XML, MediaTypes.APPLICATION_FLIES_DOCUMENTS_JSON })
   public ClientResponse put(Documents documents);

   @GET
   @Produces( { MediaTypes.APPLICATION_FLIES_DOCUMENTS_XML, MediaTypes.APPLICATION_FLIES_DOCUMENTS_JSON })
   public ClientResponse<Documents> getDocuments();
}
