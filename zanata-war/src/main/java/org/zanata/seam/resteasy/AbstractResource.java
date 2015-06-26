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

import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

/**
 * Resource class used by ResourceHome and ResourceQuery components. Contains
 * information about path, media types and entity class the component operates
 * on.
 *
 * @param <T>
 *            entity class
 * @author Jozef Hartinger
 */
public abstract class AbstractResource<T> {

    @Context
    private HttpHeaders httpHeaders;

    private String path = null;
    private MediaType[] mediaTypes = null;
    private Class entityClass = null;

    public AbstractResource() {
        mediaTypes = new MediaType[] { MediaType.APPLICATION_XML_TYPE };
    }

    public String[] getMediaTypes() {
        String[] mediaTypes = new String[this.mediaTypes.length];
        for (int i = 0; i < mediaTypes.length; i++) {
            mediaTypes[i] = this.mediaTypes[i].toString();
        }
        return mediaTypes;
    }

    public void setMediaTypes(String[] mediaTypes) {
        this.mediaTypes = new MediaType[mediaTypes.length];
        for (int i = 0; i < mediaTypes.length; i++) {
            this.mediaTypes[i] = MediaType.valueOf(mediaTypes[i]);
        }
    }

    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Retrieve entity class. If not set, type parameters of a superclass are
     * examined.
     *
     * @return entity class
     */
    public Class<T> getEntityClass() {
        if (entityClass == null) {
            Type superclass = this.getClass().getGenericSuperclass();
            if (superclass instanceof ParameterizedType) {
                ParameterizedType parameterizedSuperclass =
                        (ParameterizedType) superclass;
                if (parameterizedSuperclass.getActualTypeArguments().length > 0) {
                    return (Class) parameterizedSuperclass
                            .getActualTypeArguments()[0];
                }
            }
            throw new RuntimeException("Unable to determine entity class.");
        } else {
            return entityClass;
        }
    }

    /**
     * Retrieve a suffix of this resource's URI. See
     * {@link #setPath(String path)}
     *
     * @return path
     * @see javax.ws.rs.Path
     */
    public String getPath() {
        return path;
    }

    /**
     * Set the path this resource will operate on. This method is intended to be
     * used only by Seam to create a resource configured in component
     * descriptor. To specify the path in other cases, use @Path annotation. See
     *
     * @param path
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * Select a media type that will be used for marshalling entity. Media type
     * is selected from the intersection of media types supported by this
     * resource and media types accepted by client.
     *
     * @return selected media type, returns null if no suitable media type is
     *         found
     */
    protected MediaType selectResponseMediaType() {
        for (MediaType acceptedMediaType : httpHeaders
                .getAcceptableMediaTypes()) {
            for (MediaType availableMediaType : mediaTypes) {
                if (acceptedMediaType.isCompatible(availableMediaType))
                    return availableMediaType;
            }
        }
        return null;
    }

    /**
     * Check if media type passed as parameter is supported by this resource.
     *
     * @param mediaType
     * @return true if and only if the media type is accepted by this resource
     */
    public boolean isMediaTypeCompatible(MediaType mediaType) {
        for (MediaType availableMediaType : mediaTypes) {
            if (availableMediaType.isCompatible(mediaType)) {
                return true;
            }
        }
        return false;
    }
}
