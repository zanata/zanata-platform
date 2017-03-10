package org.zanata.rest.service;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.resteasy.util.GenericType;
import org.zanata.common.Namespaces;
import org.zanata.rest.dto.Link;
import org.zanata.security.annotations.CheckRole;
import org.zanata.util.Introspectable;
import com.google.common.annotations.Beta;
import com.google.common.collect.Lists;

/**
 * This API is experimental only and subject to change or even removal.
 *
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
@Path("/monitor")
@Produces({ "application/json" })
@Consumes({ "application/xml" })
@Transactional(readOnly = true)
@CheckRole("admin")
@Beta
public class IntrospectableObjectMonitorService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory
            .getLogger(IntrospectableObjectMonitorService.class);

    @Inject
    private Instance<Introspectable> introspectables;

    /**
     * Return all Introspectable objects link.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - all available introspectable objects with hypermedia
     *         link.<br>
     *         UNAUTHORIZED(401) - if not admin role.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Wrapped(element = "introspectable", namespace = Namespaces.ZANATA_API)
    public Response get() {
        List<LinkRoot> all = Lists.newArrayList(introspectables.iterator())
                .stream()
                .map(introspectable -> new LinkRoot(
                        URI.create("/" + introspectable.getIntrospectableId()),
                        "self", MediaType.APPLICATION_JSON))
                .collect(Collectors.toList());
        Type genericType = new GenericType<List<LinkRoot>>() {

        }.getGenericType();
        Object entity = new GenericEntity<>(all, genericType);
        return Response.ok().entity(entity).build();
    }

    /**
     * Return a single introspectable fields as String.
     *
     * @param id
     *            introspectable id
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing a string of all fields and
     *         values.<br>
     *         NOT_FOUND(404) - given id does not represent an
     *         introspectable.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") final String id) {
        Optional<Introspectable> optional =
                Lists.newArrayList(introspectables.iterator()).stream()
                        .filter(input -> input.getIntrospectableId().equals(id))
                        .findFirst();
        if (!optional.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        final Introspectable introspectable = optional.get();
        String json = introspectable.getFieldValuesAsJSON();
        return Response.ok(json).build();
    }

    @XmlRootElement(name = "link")
    public static class LinkRoot extends Link {

        @SuppressWarnings("unused")
        public LinkRoot() {
        }

        public LinkRoot(URI href, String rel, String type) {
            super(href, rel, type);
        }
    }
}
