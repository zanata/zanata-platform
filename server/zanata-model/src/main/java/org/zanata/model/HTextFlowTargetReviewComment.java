/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
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

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Immutable;
import org.hibernate.search.annotations.IndexedEmbedded;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Entity
@Immutable
@Cacheable
@BatchSize(size = 20)
@Access(AccessType.FIELD)
public class HTextFlowTargetReviewComment extends ModelEntityBase {
    private static final long serialVersionUID = 1413384329431214946L;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commenter_id", nullable = false)
    private HPerson commenter;
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id")
    @IndexedEmbedded
    private HTextFlowTarget textFlowTarget;
    @NotEmpty
    @javax.persistence.Lob
    private String comment;
    @NotNull
    private Integer targetVersion;
    private transient String commenterName;

    public HTextFlowTargetReviewComment(HTextFlowTarget target, String comment,
            HPerson commenter) {
        this.textFlowTarget = target;
        this.comment = comment;
        this.commenter = commenter;
        commenterName = commenter.getName();
        targetVersion = target.getVersionNum();
    }

    @Transient
    public String getCommenterName() {
        if (commenterName == null) {
            commenterName = getCommenter().getName();
        }
        return commenterName;
    }

    protected HTextFlowTargetReviewComment() {
    }

    public HPerson getCommenter() {
        return this.commenter;
    }

    public HTextFlowTarget getTextFlowTarget() {
        return this.textFlowTarget;
    }

    public String getComment() {
        return this.comment;
    }

    protected void setTargetVersion(final Integer targetVersion) {
        this.targetVersion = targetVersion;
    }

    public Integer getTargetVersion() {
        return this.targetVersion;
    }
}
