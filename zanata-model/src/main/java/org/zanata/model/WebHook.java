/*
 * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */

package org.zanata.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Size;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.zanata.model.validator.Url;

import com.google.common.annotations.VisibleForTesting;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
public class WebHook implements Serializable {

    private Long id;

    private HProject project;

    @Url
    private String url;

    /**
     * Secret key used to generate webhook header in hmac-sha1 encryption.
     */
    @Size(max = 255)
    @Column(nullable = true)
    private String secret;

    public WebHook(HProject project, String url, String secret) {
        this.project = project;
        this.url = url;
        this.secret = secret;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    @ManyToOne
    @JoinColumn(name = "projectId", nullable = false)
    public HProject getProject() {
        return project;
    }
}
