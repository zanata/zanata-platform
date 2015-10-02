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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Objects;

@Entity
@org.hibernate.annotations.Entity(mutable = false)
public class HTextFlowHistory extends HTextContainer implements Serializable,
        ITextFlowHistory {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Integer revision;
    private HTextFlow textFlow;
    private List<String> contents;
    private boolean obsolete;

    private Integer pos;

    public HTextFlowHistory() {
    }

    public HTextFlowHistory(HTextFlow textFlow) {
        this.revision = textFlow.getRevision();
        this.textFlow = textFlow;
        this.setContents(textFlow.getContents());
    }

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    protected void setId(Long id) {
        this.id = id;
    }

    // TODO PERF @NaturalId(mutable=false) for better criteria caching
    @NaturalId
    @Override
    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    // TODO PERF @NaturalId(mutable=false) for better criteria caching
    @NaturalId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tf_id")
    public HTextFlow getTextFlow() {
        return textFlow;
    }

    public void setTextFlow(HTextFlow textFlow) {
        this.textFlow = textFlow;
    }

    @NotEmpty
    @javax.persistence.Lob
    @AccessType("field")
    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name = "HTextFlowContentHistory", joinColumns = @JoinColumn(
            name = "text_flow_history_id"))
    @IndexColumn(name = "pos", nullable = false)
    @Column(name = "content", nullable = false)
    @Override
    public List<String> getContents() {
        return contents;
    }

    public void setContents(List<String> contents) {
        this.contents = new ArrayList<String>(contents);
    }

    @Override
    public Integer getPos() {
        return pos;
    }

    public void setPos(Integer pos) {
        this.pos = pos;
    }

    @Override
    public boolean isObsolete() {
        return obsolete;
    }

    public void setObsolete(boolean obsolete) {
        this.obsolete = obsolete;
    }

    /**
     * Determines whether a Text Flow has changed when compared to this history
     * object. Currently, this method only checks for changes in the revision
     * number.
     *
     * @param current
     *            The current Text Flow state.
     * @return True, if the revision number in the Text Flow has changed. False,
     *         otherwise.
     */
    public boolean hasChanged(HTextFlow current) {
        return !Objects.equal(current.getRevision(), this.getRevision());
    }

}
