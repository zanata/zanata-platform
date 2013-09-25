/**
 * <p>
 * The interfaces in this package exist purely for the purposes of
 * documenting the REST API with Enunciate.
 * </p>
 * <p>
 * Enunciate needs the class annotation @Path to determine API end-points, but
 * putting @Path on the resource interface prevents seam-resteasy from
 * injecting @Context variables into the service class.  Also, putting
 * @Path on the client interface makes it impossible for the client to
 * build request URLs for resources whose @Path includes a @PathParam.
 * </p>
 */
package org.zanata.rest.enunciate;
