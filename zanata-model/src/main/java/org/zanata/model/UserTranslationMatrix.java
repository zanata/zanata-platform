/*
 * Copyright 2015, Red Hat, Inc. and individual contributors as indicated by the
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

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;
import org.joda.time.DateTimeZone;
import org.zanata.common.ContentState;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;
import java.util.TimeZone;

/**
 * This is a hibernate entity that will store aggregate word counts for a
 * particular user in a given day. It mainly serves as a cache. The day is
 * stored with a timezone offset to UTC. This means if user change his timezone,
 * the entry in database will no longer be valid and needs to be deleted.
 *
 * @auther pahuang
 */
@Entity
@Access(AccessType.FIELD)
@Immutable
@Getter
public class UserTranslationMatrix implements Serializable {
    // we store timezone offset so that we can compare equivalent time zones. We
    // could use any instance to base on. Just need to be consistent.
    public static final int TIMEZONE_OFFSET_INSTANCE = 0;
    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    private Long id;

    @JoinColumn(name = "person_id", nullable = false, updatable = false)
    @ManyToOne(targetEntity = HPerson.class, cascade = CascadeType.DETACH,
            optional = false)
    private HPerson person;

    @JoinColumn(name = "project_iteration_id", nullable = false,
            updatable = false)
    @ManyToOne(targetEntity = HProjectIteration.class,
            cascade = CascadeType.DETACH, optional = false)
    private HProjectIteration projectIteration;

    @Column(nullable = false, updatable = false)
    private ContentState savedState;

    @Column(nullable = false, updatable = false)
    private Long wordCount;

    @Temporal(TemporalType.DATE)
    @Column(nullable = false, updatable = false)
    private Date savedDate;

    @Column(nullable = false, updatable = false)
    @Setter
    private long timeZoneOffset;

    @JoinColumn(name = "locale_id", nullable = false, updatable = false)
    @ManyToOne(targetEntity = HPerson.class, cascade = CascadeType.DETACH,
            optional = false)
    private HLocale locale;

    public UserTranslationMatrix(HPerson person,
            HProjectIteration projectIteration, HLocale locale,
            ContentState savedState, Long wordCount, Date savedDate) {
        this.person = person;
        this.projectIteration = projectIteration;
        this.locale = locale;
        this.savedState = savedState;
        this.wordCount = wordCount;
        this.savedDate = savedDate;
    }
}
