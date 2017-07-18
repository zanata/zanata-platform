/*
 * Copyright 2012, Red Hat, Inc. and individual contributors
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

package org.zanata.webtrans.shared.model;

import java.util.ArrayList;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * @author Sean Flanigan <a
 *         href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 *
 */
public class GlossaryResultItem extends SearchResultItem implements
        IsSerializable {
    private String qualifiedName;
    private String source;
    private String target;
    private ArrayList<Long> sourceIdList = new ArrayList<Long>();

    // for GWT
    @SuppressWarnings("unused")
    private GlossaryResultItem() {
    }

    /**
     * @param source
     * @param target
     * @param relevanceScore
     * @param similarityPercent
     */
    public GlossaryResultItem(String qualifiedName, String source,
            String target, double relevanceScore, double similarityPercent) {
        super(relevanceScore, similarityPercent);
        this.qualifiedName = qualifiedName;
        this.source = source;
        this.target = target;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public ArrayList<Long> getSourceIdList() {
        return sourceIdList;
    }

    public void addSourceId(Long sourceId) {
        this.sourceIdList.add(sourceId);
    }

    public String getQualifiedName() {
        return qualifiedName;
    }

    public void setQualifiedName(String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }
}
