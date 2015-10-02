/*
 * Copyright 2011, Red Hat, Inc. and individual contributors
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
package org.zanata.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import javax.validation.constraints.NotNull;

import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;

/**
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 *
 **/
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Setter
@NoArgsConstructor
@ToString(of = "comment")
public class HTermComment implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;

    private String comment;

    private Integer pos;

    private HGlossaryTerm glossaryTerm;

    public HTermComment(String comment) {
        this.comment = comment;
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    @NotNull
    @javax.persistence.Lob
    public String getComment() {
        return comment;
    }

    @Column(insertable = false, updatable = false, nullable = false)
    public Integer getPos() {
        return pos;
    }

    @ManyToOne
    @JoinColumn(name = "glossaryTermId", insertable = false, updatable = false,
            nullable = false)
    // TODO PERF @NaturalId(mutable=false) for better criteria caching
            public
            HGlossaryTerm getGlossaryTerm() {
        return glossaryTerm;
    }

}
