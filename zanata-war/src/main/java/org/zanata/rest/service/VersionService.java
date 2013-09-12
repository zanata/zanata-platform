package org.zanata.rest.service;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.codehaus.enunciate.jaxrs.TypeHint;
import org.jboss.seam.annotations.Name;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.VersionInfo;
import org.zanata.seam.resteasy.IgnoreInterfacePath;
import org.zanata.util.VersionUtility;


@Name("versionService")
@Path(VersionService.SERVICE_PATH)
@IgnoreInterfacePath
public class VersionService implements VersionResource
{
   public static final String SERVICE_PATH = "/version";
   
   private final VersionInfo version;

   public VersionService()
   {
      this(VersionUtility.getAPIVersionInfo());
   }

   VersionService(VersionInfo ver)
   {
      this.version = ver;
   }
   
   /**
    * Retrieve Version information for the application.
    * 
    * @return The following response status codes will be returned from this operation:<br>
    * OK(200) - Response with the system's version information in the content.<br>
    * INTERNAL SERVER ERROR(500) - If there is an unexpected error in the server while performing this operation.
    */
   @Override
   @GET
   @Produces({ MediaTypes.APPLICATION_ZANATA_VERSION_JSON, MediaTypes.APPLICATION_ZANATA_VERSION_XML })
   @TypeHint(VersionInfo.class)
   public Response get()
   {
      return Response.ok(version).build();
   }
}
