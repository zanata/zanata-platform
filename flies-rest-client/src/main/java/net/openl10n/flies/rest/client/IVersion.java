package net.openl10n.flies.rest.client;


import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import net.openl10n.flies.rest.dto.VersionInfo;


@Produces({ MediaType.APPLICATION_XML })
public interface IVersion
{
   @GET
   public VersionInfo get();

}
