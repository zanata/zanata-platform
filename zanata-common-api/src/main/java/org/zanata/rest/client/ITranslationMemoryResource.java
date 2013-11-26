package org.zanata.rest.client;

import org.zanata.rest.service.TranslationMemoryResource;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(TranslationMemoryResource.SERVICE_PATH)
@Produces({ MediaType.APPLICATION_XML /* , "application/x-tmx" */})
@Consumes({ MediaType.APPLICATION_XML /* , "application/x-tmx" */})
public interface ITranslationMemoryResource extends TranslationMemoryResource {
}
