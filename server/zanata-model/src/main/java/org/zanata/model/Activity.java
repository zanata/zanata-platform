/*
 * Copyright 2013, Red Hat, Inc. and individual contributors as indicated by the
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
import java.util.Calendar;
import java.util.Date;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang.time.DateUtils;
import org.zanata.common.ActivityType;
import org.zanata.model.type.EntityType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@EntityListeners({ Activity.EntityListener.class })
@Access(AccessType.FIELD)
public class Activity extends ModelEntityBase implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotNull
    @JoinColumn(name = "actor_id", nullable = false)
    @ManyToOne
    private HPerson actor;
    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    private Date approxTime;
    @NotNull
    private long startOffsetMillis;
    @NotNull
    private long endOffsetMillis;
    @NotNull
    @Enumerated(EnumType.STRING)
    private EntityType contextType;
    @NotNull
    @Column(name = "context_id")
    private long contextId;
    @Enumerated(EnumType.STRING)
    private EntityType lastTargetType;
    @NotNull
    @Column(name = "last_target_id")
    private long lastTargetId;
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;
    // Event count starts with 1 because there is a single event when new
    // activity created
    private int eventCount = 1;
    private int wordCount;

    public Activity(HPerson actor, IsEntityWithType context,
            IsEntityWithType target, ActivityType activityType, int wordCount) {
        this.actor = actor;
        this.contextType = context.getEntityType();
        this.contextId = context.getId();
        this.lastTargetType = target.getEntityType();
        this.lastTargetId = target.getId();
        this.activityType = activityType;
        this.wordCount = wordCount;
    }

    public void updateActivity(Date currentTime, IsEntityWithType target,
            int wordCount) {
        this.endOffsetMillis = currentTime.getTime() - approxTime.getTime();
        this.wordCount += wordCount;
        this.eventCount++;
        this.lastTargetType = target.getEntityType();
        this.lastTargetId = target.getId();
    }

    @Transient
    public Date getEndDate() {
        return DateUtils.addMilliseconds(approxTime, (int) endOffsetMillis);
    }

    public static class EntityListener {

        @PrePersist
        private void onPrePersist(Activity activity) {
            activity.approxTime = DateUtils.truncate(activity.getCreationDate(),
                    Calendar.HOUR);
            activity.startOffsetMillis = activity.getCreationDate().getTime()
                    - activity.approxTime.getTime();
            activity.endOffsetMillis = activity.startOffsetMillis;
        }
    }

    public Activity() {
    }

    public HPerson getActor() {
        return this.actor;
    }

    public Date getApproxTime() {
        // Deep-copy to prevent malicious code vulnerability (EI_EXPOSE_REP)
        return new Date(this.approxTime.getTime());
    }

    public long getStartOffsetMillis() {
        return this.startOffsetMillis;
    }

    public long getEndOffsetMillis() {
        return this.endOffsetMillis;
    }

    public long getContextId() {
        return this.contextId;
    }

    public EntityType getLastTargetType() {
        return this.lastTargetType;
    }

    public long getLastTargetId() {
        return this.lastTargetId;
    }

    public ActivityType getActivityType() {
        return this.activityType;
    }

    public int getEventCount() {
        return this.eventCount;
    }

    public int getWordCount() {
        return this.wordCount;
    }

    public EntityType getContextType() {
        return this.contextType;
    }
}
