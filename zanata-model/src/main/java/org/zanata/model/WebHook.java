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
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.zanata.model.type.WebhookType;
import org.zanata.model.validator.Url;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@Getter
@Setter(AccessLevel.PRIVATE)
@NoArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"url", "projectId"}))
public class WebHook implements Serializable {

    private Long id;

    private HProject project;

    @Url
    @Size(max = 255)
    private String url;

    private Set<WebhookType> types = Sets.newHashSet();

    /**
     * Secret key used to generate webhook header in hmac-sha1 encryption.
     */
    @Size(max = 255)
    @Column(nullable = true)
    private String secret;

    public WebHook(HProject project, String url, Set<WebhookType> types,
        String secret) {
        this.project = project;
        this.url = url;
        this.types = types;
        this.secret = secret;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST,
            CascadeType.REFRESH })
    @JoinColumn(name = "projectId", nullable = false)
    public HProject getProject() {
        return project;
    }

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @JoinTable(name = "WebHook_WebHookType",
        joinColumns = @JoinColumn(name = "webhookId"))
    @Column(name = "type", nullable = false)
    public Set<WebhookType> getTypes() {
        return types;
    }

    /**
     * This will replace all properties with given ones.
     *
     * @param url - new url
     * @param newTypes - new types
     * @param secret - new secret key
     */
    @Transient
    public void update(String url, Set<WebhookType> newTypes, String secret) {
        this.url = url;
        this.secret = secret;

        /**
         * Copy all newTypes into currentTypes and remove those that are not
         * in the newTypes
         */
        this.types.addAll(newTypes);
        Set<WebhookType> currentTypes = Sets.newHashSet(this.types);
        for (WebhookType type: currentTypes) {
            if (!newTypes.contains(type)) {
                this.types.remove(type);
            }
        }
    }
}
