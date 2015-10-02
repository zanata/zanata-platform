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

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.annotations.providers.jaxb.Wrapped;
import javax.annotation.PostConstruct;
import org.jboss.seam.framework.EntityQuery;
import org.jboss.seam.framework.Query;

import static javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;

/**
 * This component exposes EntityQuery component as a REST resource responding to
 * HTTP GET request.
 *
 * @param <T>
 *            entity type
 * @author Jozef Hartinger
 */
// Empty @Path because it's ignored by second-stage bootstrap if not subclassed
// or in components.xml
// but we need it as a marker so we'll find components.xml declarations during
// first stage of bootstrap.
@Path("")
public class ResourceQuery<T> extends AbstractResource<T> {

    private Query<?, T> entityQuery = null;

    /**
     * Called at component instantiation.
     */
    @PostConstruct
    public void create() {
        this.entityQuery = getEntityQuery();
        if (entityQuery == null) {
            this.entityQuery = createEntityQuery();
        }
    }

    /**
     * Called by RESTEasy to respond for an HTTP GET request. Retrieves a list
     * of entities matching criteria set by query parameters from database and
     * returns it wrapped in Response instance.
     *
     * @param start
     *            first entity in the list
     * @param show
     *            maximum size of the list
     * @return representation of a list of database entries
     * @see #getEntityList
     */
    @GET
    @Wrapped
    public Response getResourceList(
            @QueryParam("start") @DefaultValue("0") int start,
            @QueryParam("show") @DefaultValue("25") int show) {
        MediaType selectedMediaType = selectResponseMediaType();
        if (selectedMediaType == null) {
            return Response.status(UNSUPPORTED_MEDIA_TYPE).build();
        }

        if ((start < 0) || (show < 0)) {
            return Response.status(BAD_REQUEST).build();
        }

        final List<T> result = getEntityList(start, show);
        // create a proper response type
        Type responseType = new ParameterizedType() {

            public Type getRawType() {
                return result.getClass();
            }

            public Type getOwnerType() {
                return null;
            }

            public Type[] getActualTypeArguments() {
                return new Type[] { getEntityClass() };
            }
        };
        return Response.ok(new GenericEntity(result, responseType) {
        }, selectedMediaType).build();
    }

    /**
     * Retrieve a list of database entities.
     *
     * @param start
     *            first entity in the list
     * @param show
     *            maximum size of the list, 0 for unlimited
     * @return list of database entries
     */
    public List<T> getEntityList(int start, int show) {
        entityQuery.setFirstResult(start);
        // set 0 for unlimited
        if (show > 0) {
            entityQuery.setMaxResults(show);
        }
        return entityQuery.getResultList();
    }

    /**
     * EntityQuery getter
     *
     * @return EntityQuery instance
     */
    public Query<?, T> getEntityQuery() {
        return entityQuery;
    }

    public void setEntityQuery(Query<?, T> query) {
        this.entityQuery = query;
    }

    public Query<?, T> createEntityQuery() {
        Query<?, T> entityQuery = new EntityQuery<T>();
        entityQuery.setEjbql("select entity from " + getEntityClass().getName()
                + " entity");
        return entityQuery;
    }
}
