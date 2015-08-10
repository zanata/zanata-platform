package org.zanata.rest.service;

import java.lang.reflect.Type;
import java.net.URI;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import org.jboss.resteasy.util.GenericType;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Transactional;
import org.zanata.security.annotations.CheckLoggedIn;
import org.zanata.security.annotations.CheckPermission;
import org.zanata.security.annotations.CheckRole;
import org.zanata.common.Namespaces;
import org.zanata.rest.MediaTypes;
import org.zanata.rest.dto.Link;
import org.zanata.limits.RateLimitManager;
import org.zanata.security.annotations.ZanataSecured;
import org.zanata.util.Introspectable;
import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

/**
 * This API is experimental only and subject to change or even removal.
 *
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Name("introspectableObjectMonitorService")
@Path("/monitor")
@Produces({ "application/xml" })
@Consumes({ "application/xml" })
@Transactional
@ZanataSecured
@CheckRole("admin")
@Slf4j
@Beta
public class IntrospectableObjectMonitorService {
    // TODO check http://code.google.com/p/reflections/ and re-implement this
    private static List<Introspectable> introspectables = ImmutableList
            .<Introspectable> builder()
            .add(RateLimitManager.getInstance())
            .build();

    /** Type of media requested. */
    @HeaderParam("Accept")
    @DefaultValue(MediaType.APPLICATION_XML)
    @Context
    private MediaType accept;

    /**
     * Return all Introspectable objects link.
     *
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - all available introspectable objects with hypermedia link.<br>
     *         UNAUTHORIZED(401) - if not admin role.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Wrapped(element = "introspectable", namespace = Namespaces.ZANATA_API)
    public Response get() {
        List<LinkRoot> all =
                Lists.transform(introspectables,
                        new Function<Introspectable, LinkRoot>() {
                            @Override
                            public LinkRoot apply(Introspectable input) {
                                return new LinkRoot(
                                        URI.create("/"
                                                /*+ TokenBucketsHolder.HOLDER
                                                        .getId()*/), "self",
                                        MediaTypes.createFormatSpecificType(
                                                MediaType.APPLICATION_XML,
                                                accept));
                            }
                        });

        Type genericType = new GenericType<List<LinkRoot>>() {
        }.getGenericType();
        Object entity = new GenericEntity<List<LinkRoot>>(all, genericType);
        return Response.ok().entity(entity).build();
    }

    /**
     * Return a single introspectable fields as String.
     * @param id introspectable id
     * @return The following response status codes will be returned from this
     *         operation:<br>
     *         OK(200) - Response containing a string of all fields and values.<br>
     *         NOT_FOUND(404) - given id does not represent an introspectable.<br>
     *         INTERNAL SERVER ERROR(500) - If there is an unexpected error in
     *         the server while performing this operation.
     */
    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") final String id) {

        Optional<Introspectable> optional =
                Iterables.tryFind(introspectables,
                        new Predicate<Introspectable>() {
                            @Override
                            public boolean apply(Introspectable input) {
                                return input.getIntrospectableId().equals(id);
                            }
                        });
        if (!optional.isPresent()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        final Introspectable introspectable = optional.get();
        final String format = "%s:%s\n";
        Iterable<String> report =
                Iterables.transform(introspectable.getIntrospectableFieldNames(),
                        new Function<String, String>() {
                            @Override
                            public String apply(String fieldName) {
                                return String.format(format, fieldName,
                                        introspectable.getFieldValueAsString(
                                                fieldName));
                            }
                        });
        return Response
                .ok()
                .entity(introspectable.getIntrospectableId() + "{"
                        + Iterables.toString(report) + "}").build();
    }

    @XmlRootElement(name = "link")
    public static class LinkRoot extends Link {
        @SuppressWarnings("unused")
        public LinkRoot() {
            super();
        }

        public LinkRoot(URI href, String rel, String type) {
            super(href, rel, type);
       }
    }
}
