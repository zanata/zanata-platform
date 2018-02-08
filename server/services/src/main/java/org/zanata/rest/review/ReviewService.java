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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
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
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.jpa.api.transaction.Transactional;
import org.zanata.dao.ReviewCriteriaDAO;
import org.zanata.model.ReviewCriteria;
import org.zanata.util.UrlUtil;
import org.zanata.webtrans.shared.rest.dto.TransReviewCriteria;
import org.zanata.security.annotations.CheckRole;
import com.google.common.annotations.VisibleForTesting;

import static org.zanata.model.ReviewCriteria.DESCRIPTION_MAX_LENGTH;

/**
 * @author Patrick Huang <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@RequestScoped
@Path("review")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ReviewService {

    @Inject
    private ReviewCriteriaDAO reviewCriteriaDAO;

    @Context
    UriInfo uriInfo;

    @Inject
    private UrlUtil urlUtil;

    public ReviewService() {
    }

    @VisibleForTesting
    protected ReviewService(ReviewCriteriaDAO reviewCriteriaDAO, UriInfo uriInfo, UrlUtil urlUtil) {
        this.reviewCriteriaDAO = reviewCriteriaDAO;
        this.uriInfo = uriInfo;
        this.urlUtil = urlUtil;
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
        Optional<Response> response = validateReviewCriteria(criteria);
        if (response.isPresent()) {
            return response.get();
        }
        ReviewCriteria reviewCriteria =
                new ReviewCriteria(criteria.getPriority(),
                        criteria.isEditable(), criteria.getDescription());
        reviewCriteriaDAO.makePersistent(reviewCriteria);
        return Response.created(UriBuilder.fromUri(urlUtil.restPath(
                uriInfo.getPath())).path(reviewCriteria.getId().toString()).build())
                .entity(fromModel(reviewCriteria))
                .build();
    }

    @PUT
    @Path("criteria/{id}")
    @CheckRole("admin")
    @Transactional
    public Response editCriteria(@PathParam("id") Long id, TransReviewCriteria criteria) {
        Optional<Response> response = validateReviewCriteria(criteria);
        if (response.isPresent()) {
            return response.get();
        }
        ReviewCriteria reviewCriteria =
                reviewCriteriaDAO.findById(id);
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
                reviewCriteriaDAO.findById(id);
        if (reviewCriteria == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        TransReviewCriteria dto =
                fromModel(reviewCriteria);
        reviewCriteriaDAO.remove(reviewCriteria);
        return Response.ok(dto).build();
    }

    @GET
    @Transactional(readOnly = true)
    public Response getAllCriteria() {
        List<ReviewCriteria> resultList = reviewCriteriaDAO.findAll();

        List<TransReviewCriteria> entity =
                resultList.stream().map(ReviewService::fromModel).collect(
                        Collectors.toList());
        return Response.ok(entity).build();
    }

    /**
     * Validate DTO in service due to missing hibernate validator in gwt-shared
     * DTO.
     *
     * TODO: move validation to DTO
     *
     * @param criteria
     */
    private Optional<Response> validateReviewCriteria(TransReviewCriteria criteria) {
        String description = criteria.getDescription();
        if (StringUtils.isBlank(description) ||
                StringUtils.length(description) > DESCRIPTION_MAX_LENGTH) {
            return Optional
                    .of(Response.status(Response.Status.BAD_REQUEST).build());
        }
        return Optional.empty();
    }
}
