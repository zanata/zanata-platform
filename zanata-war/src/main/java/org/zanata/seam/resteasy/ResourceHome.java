// Implementation copied from Seam 2.3.1, commit f3077fe

/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.seam.resteasy;

import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.annotation.Annotation;
import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;

import org.jboss.resteasy.core.StringParameterInjector;
import org.jboss.seam.Entity;
import javax.annotation.PostConstruct;
import org.jboss.seam.framework.Home;

import static javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * This component exposes EntityHome and HibernateEntityHome components as a
 * REST resource.
 *
 * @param <T>
 *            Entity class
 * @param <T2>
 *            Entity id class
 * @author Jozef Hartinger
 */
// Empty @Path because it's ignored by second-stage bootstrap if not subclassed
// or in components.xml
// but we need it as a marker so we'll find components.xml declarations during
// first stage of bootstrap.
@Path("")
public class ResourceHome<T, T2> extends AbstractResource<T> {
    private EntityHomeWrapper<T> entityHome = null;

    @Context
    private UriInfo uriInfo;
    @Context
    private HttpHeaders headers;
    @HeaderParam("Content-Type")
    private MediaType requestContentType;

    private Class entityIdClass = null;
    private boolean readonly;

    private static final PathParamAnnotation pathParamAnnotation =
            new PathParamAnnotation();

    /**
     * Called at component instantiation. EntityHome component must be set in
     * order for component to be created.
     */
    @PostConstruct
    public void create() {
        setEntityHome(getEntityHome());
        if (entityHome == null) {
            throw new IllegalStateException("entityHome is not set");
        }
    }

    /**
     * Called by RESTEasy when HTTP GET request is received. String form of
     * entity identifier is passed as a parameter. Returns a response containing
     * database entity.
     *
     * @param rawId
     *            String form of entity identifier
     * @return response
     * @see #getEntity
     */
    @Path("/{id}")
    @GET
    public Response getResource(@PathParam("id") String rawId) {
        MediaType selectedMediaType = selectResponseMediaType();
        if (selectedMediaType == null) {
            return Response.status(UNSUPPORTED_MEDIA_TYPE).build();
        }

        T2 id = unmarshallId(rawId);
        T entity = getEntity(id);

        return Response.ok(new GenericEntity(entity, getEntityClass()) {
        }, selectedMediaType).build();
    }

    /**
     * Retrieve an entity identified by id parameter.
     *
     * @param id
     *            entity identifier
     * @return entity database entity
     */
    public T getEntity(T2 id) {
        entityHome.setId(id);
        return entityHome.find();

    }

    /**
     * Called by RESTEasy when HTTP POST request is received. Persists received
     * entity and returns 201 HTTP status code with location header set to new
     * URI if operation succeeds.
     *
     * @param messageBody
     *            HTTP request body
     * @return response
     * @see #createEntity
     */
    @POST
    public Response createResource(InputStream messageBody) {
        if (readonly) {
            return Response.status(405).build();
        }

        // check if we accept this content type
        if (!isMediaTypeCompatible(requestContentType)) {
            return Response.status(UNSUPPORTED_MEDIA_TYPE).build();
        }

        T entity = unmarshallEntity(messageBody);

        T2 id = createEntity(entity);

        URI uri = uriInfo.getAbsolutePathBuilder().path(id.toString()).build();
        return Response.created(uri).build();
    }

    /**
     * Store entity passed as a parameter in the database.
     *
     * @param entity
     *            Object to be persisted
     * @return id identifier assigned to the entity
     */
    public T2 createEntity(T entity) {
        entityHome.setInstance(entity);
        entityHome.persist();
        return (T2) entityHome.getId();
    }

    /**
     * Called by RESTEasy when HTTP PUT request is received. Merges the state of
     * the database entity with the received representation.
     *
     * @param rawId
     *            String form of entity identifier
     * @param messageBody
     *            HTTP request body
     * @return response
     * @see #updateEntity
     */
    @Path("/{id}")
    @PUT
    public Response updateResource(@PathParam("id") String rawId,
            InputStream messageBody) {
        if (readonly) {
            return Response.status(405).build();
        }

        // check if we accept this content type
        if (!isMediaTypeCompatible(requestContentType)) {
            return Response.status(UNSUPPORTED_MEDIA_TYPE).build();
        }

        T entity = unmarshallEntity(messageBody);
        T2 id = unmarshallId(rawId);

        // check representation id - we don't allow renaming
        Object storedId = Entity.forBean(entity).getIdentifier(entity);
        if (!id.equals(storedId)) {
            return Response.status(BAD_REQUEST).build();
        }

        updateEntity(entity, id);
        return Response.noContent().build();
    }

    /**
     * Merge the state of the database entity with the entity passed as a
     * parameter. Override to customize the update strategy - for instance to
     * update specific fields only instead of a full merge.
     *
     * @param entity
     */
    public void updateEntity(T entity, T2 id) {
        entityHome.merge(entity);
    }

    /**
     * Called by RESTEasy when HTTP DELETE request is received. Deletes a
     * database entity.
     *
     * @param rawId
     *            String form of entity identifier
     * @return response
     * @see #deleteEntity
     */
    @Path("/{id}")
    @DELETE
    public Response deleteResource(@PathParam("id") String rawId) {
        if (readonly) {
            return Response.status(405).build();
        }

        T2 id = unmarshallId(rawId);
        deleteEntity(id);
        return Response.noContent().build();
    }

    /**
     * Delete database entity.
     *
     * @param id
     *            entity identifier
     */
    public void deleteEntity(T2 id) {
        getEntity(id);
        entityHome.remove();
    }

    /**
     * Convert HTTP request body into entity class instance.
     *
     * @param is
     *            HTTP request body
     * @return entity
     */
    private T unmarshallEntity(InputStream is) {
        Class<T> entityClass = getEntityClass();
        MessageBodyReader<T> reader =
                SeamResteasyProviderFactory.getInstance().getMessageBodyReader(
                        entityClass, entityClass, entityClass.getAnnotations(),
                        requestContentType);
        if (reader == null) {
            throw new RuntimeException(
                    "Unable to find MessageBodyReader for content type "
                            + requestContentType);
        }
        T entity;
        try {
            entity =
                    reader.readFrom(entityClass, entityClass,
                            entityClass.getAnnotations(), requestContentType,
                            headers.getRequestHeaders(), is);
        } catch (Exception e) {
            throw new RuntimeException("Unable to unmarshall request body");
        }
        return entity;
    }

    /**
     * Converts String form of entity identifier to it's natural type.
     *
     * @param id
     *            String form of entity identifier
     * @return entity identifier
     */
    private T2 unmarshallId(String id) {
        StringParameterInjector injector =
                new StringParameterInjector(getEntityIdClass(),
                        getEntityIdClass(), "id", PathParam.class, null, null,
                        new Annotation[] { pathParamAnnotation },
                        SeamResteasyProviderFactory.getInstance());
        return (T2) injector.extractValue(id);
    }

    /**
     * EntityHome component getter. Override this method to set the EntityHome
     * this resource will operate on. You can use either EntityHome or
     * HibernateEntityHome instance.
     *
     * @return entity home
     */
    public Home<?, T> getEntityHome() {
        return (entityHome == null) ? null : entityHome.unwrap();
    }

    /**
     * EntityHome component setter
     *
     * @param entityHome
     */
    public void setEntityHome(Home<?, T> entityHome) {
        this.entityHome = new EntityHomeWrapper<T>(entityHome);
    }

    @Override
    public Class<T> getEntityClass() {
        return entityHome.getEntityClass();
    }

    public boolean isReadonly() {
        return readonly;
    }

    /**
     * If set to read-only mode, this resource will only response to GET
     * requests. HTTP 415 status code (method not allowed) will returned in all
     * other cases.
     *
     * @param readonly
     */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    /**
     * Retrieve entity identifier's class. If not set, type parameters of a
     * superclass are examined.
     *
     * @return class of entity identifier
     */
    public Class getEntityIdClass() {
        if (entityIdClass == null) {
            Type superclass = this.getClass().getGenericSuperclass();
            if (superclass instanceof ParameterizedType) {
                ParameterizedType parameterizedSuperclass =
                        (ParameterizedType) superclass;
                if (parameterizedSuperclass.getActualTypeArguments().length == 2) {
                    return (Class) parameterizedSuperclass
                            .getActualTypeArguments()[1];
                }
            }
            throw new RuntimeException("Unable to determine entity id class.");
        } else {
            return entityIdClass;
        }
    }

    public void setEntityIdClass(Class entityIdClass) {
        this.entityIdClass = entityIdClass;
    }

    /**
     * Annotation implementation (@PathParam("id")) for providing RESTEasy with
     * metadata.
     */
    static class PathParamAnnotation implements PathParam {

        public String value() {
            return "id";
        }

        public Class<? extends Annotation> annotationType() {
            return PathParam.class;
        }
    }
}
