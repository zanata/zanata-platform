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
package org.zanata.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Status of a reindex operation.
 *
 * @author Carlos Munoz <a
 *         href="mailto:camunoz@redhat.com">camunoz@redhat.com</a>
 */
@XmlRootElement(name = "reindexStatus")
@XmlType(name = "reindexStatusType")
public class ReindexStatus {
    private boolean startedReindex;

    private long indexedElements;

    private long totalElements;

    private String currentElementType;

    private long timeElapsed;

    private long timeRemaining;

    public boolean isStartedReindex() {
        return startedReindex;
    }

    public void setStartedReindex(boolean startedReindex) {
        this.startedReindex = startedReindex;
    }

    @XmlElement
    public long getPercentageComplete() {
        return this.indexedElements * 100 / this.totalElements;
    }

    @XmlElement
    public long getIndexedElements() {
        return indexedElements;
    }

    public void setIndexedElements(long indexedElements) {
        this.indexedElements = indexedElements;
    }

    @XmlElement
    public long getTotalElements() {
        return totalElements;
    }

    public void setTotalElements(long totalElements) {
        this.totalElements = totalElements;
    }

    @XmlElement
    public String getCurrentElementType() {
        return currentElementType;
    }

    public void setCurrentElementType(String currentElementType) {
        this.currentElementType = currentElementType;
    }

    @XmlElement
    public long getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(long timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    @XmlElement
    public long getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }
}
