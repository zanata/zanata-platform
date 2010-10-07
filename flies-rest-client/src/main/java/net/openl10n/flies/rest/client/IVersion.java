package net.openl10n.flies.rest.client;


import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import net.openl10n.flies.rest.MediaTypes;
import net.openl10n.flies.rest.dto.VersionInfo;


public interface IVersion
{
   @GET
   @Produces({ MediaTypes.APPLICATION_FLIES_VERSION_XML })
   public VersionInfo get();

}
