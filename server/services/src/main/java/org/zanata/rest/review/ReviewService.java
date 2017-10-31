/*
 * Copyright 2017, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
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
package org.zanata.rest.review;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.exception.ZanataServiceException;
import org.zanata.model.ReviewCriteria;
import org.zanata.webtrans.shared.rest.dto.TransReviewCriteria;
import org.zanata.security.annotations.CheckRole;
import com.google.common.annotations.VisibleForTesting;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
@Path("review")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ReviewService {
    @Inject
    private EntityManager entityManager;

    @Context
    UriInfo uriInfo;

    public ReviewService() {
    }

    @VisibleForTesting
    protected ReviewService(EntityManager entityManager, UriInfo uriInfo) {
        this.entityManager = entityManager;
        this.uriInfo = uriInfo;
    }

    public static TransReviewCriteria fromModel(ReviewCriteria criteria) {
        return new TransReviewCriteria(criteria.getId(), criteria.getPriority(),
                criteria.getDescription(), criteria.isEditable());
    }

    @POST
    @CheckRole("admin")
    @Path("criteria")
    @Transactional
    public Response addCriteria(TransReviewCriteria criteria) {
        ReviewCriteria reviewCriteria =
                new ReviewCriteria(criteria.getPriority(),
                        criteria.isEditable(), criteria.getDescription());
        entityManager.persist(reviewCriteria);
        try {
            return Response.created(new URI(uriInfo.getRequestUri() + "/" + reviewCriteria.getId()))
                    .entity(fromModel(reviewCriteria))
                    .build();
        } catch (URISyntaxException e) {
            throw new ZanataServiceException(e);
        }
    }

    @PUT
    @Path("criteria/{id}")
    @CheckRole("admin")
    @Transactional
    public Response editCriteria(@PathParam("id") Long id, TransReviewCriteria criteria) {
        ReviewCriteria reviewCriteria =
                entityManager.find(ReviewCriteria.class, id);
        if (reviewCriteria == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        reviewCriteria.setDescription(criteria.getDescription());
        reviewCriteria.setEditable(criteria.isEditable());
        reviewCriteria.setPriority(criteria.getPriority());
        return Response.ok(fromModel(reviewCriteria)).build();
    }

    @DELETE
    @Path("criteria/{id}")
    @CheckRole("admin")
    @Transactional
    public Response deleteCriteria(@PathParam("id") Long id) {
        ReviewCriteria reviewCriteria =
                entityManager.find(ReviewCriteria.class, id);
        if (reviewCriteria == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        TransReviewCriteria dto =
                fromModel(reviewCriteria);
        entityManager.remove(reviewCriteria);
        return Response.ok(dto).build();
    }

    @GET
    @Transactional(readOnly = true)
    public Response getAllCriteria() {
        List<ReviewCriteria> resultList = entityManager
                .createQuery("from ReviewCriteria", ReviewCriteria.class)
                .getResultList();
        List<TransReviewCriteria> entity =
                resultList.stream().map(ReviewService::fromModel).collect(
                        Collectors.toList());
        return Response.ok(entity).build();
    }
}
