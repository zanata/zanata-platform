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
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.time.DateUtils;
import org.hibernate.annotations.NaturalId;
import org.zanata.common.ActivityType;
import org.zanata.model.type.EntityType;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Entity
@EntityListeners({ Activity.EntityListener.class })
@Access(AccessType.FIELD)
@Table(uniqueConstraints = @UniqueConstraint(name = "UKactivity",
        columnNames = { "actor_id", "approxTime", "activityType", "contextType",
                "context_id" }))
public class Activity extends ModelEntityBase implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotNull
    @JoinColumn(name = "actor_id", nullable = false)
    @ManyToOne
    @NaturalId
    private HPerson actor;

    @Temporal(TemporalType.TIMESTAMP)
    @NotNull
    @NaturalId
    private Date approxTime;
    /**
     * maximum offset we can store equates to about 24.8 days, since this is a
     * signed int, but we only need one hour
     * see org.zanata.model.Activity.EntityListener.onPrePersist().
     * see org.zanata.service.impl.ActivityServiceImpl#logActivityAlreadyLocked(long, org.zanata.model.IsEntityWithType, org.zanata.model.IsEntityWithType, org.zanata.common.ActivityType, int)
     */
    @NotNull
    private int startOffsetMillis;
    @NotNull
    private int endOffsetMillis;
    @NotNull
    @Enumerated(EnumType.STRING)
    @NaturalId
    private EntityType contextType;

    @NotNull
    @Column(name = "context_id")
    @NaturalId
    private long contextId;

    @Enumerated(EnumType.STRING)
    private EntityType lastTargetType;

    @NotNull
    @Column(name = "last_target_id")
    private long lastTargetId;

    @Enumerated(EnumType.STRING)
    @NaturalId
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
        this.endOffsetMillis =
                (int) (currentTime.getTime() - approxTime.getTime());
        this.wordCount += wordCount;
        this.eventCount++;
        this.lastTargetType = target.getEntityType();
        this.lastTargetId = target.getId();
    }

    @Transient
    public Date getEndDate() {
        return DateUtils.addMilliseconds(approxTime, endOffsetMillis);
    }

    public static class EntityListener {

        @PrePersist
        private void onPrePersist(Activity activity) {
            activity.approxTime = DateUtils.truncate(activity.getCreationDate(),
                    Calendar.HOUR);
            activity.startOffsetMillis = (int) (activity.getCreationDate().getTime()
                                - activity.approxTime.getTime());
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

    /**
     * Business equality comparison
     *
     * @param other Activity
     * @return other is equal
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        if (!super.equals(other)) return false;

        Activity activity = (Activity) other;

        return (actor.equals(activity.actor)) &&
                (contextId == activity.contextId) &&
                (contextType == activity.contextType) &&
                (activityType == activity.activityType) &&
                (approxTime == activity.approxTime);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + actor.hashCode();
        result = 31 * result + Long.valueOf(contextId).hashCode();
        result = 31 * result + (contextType != null ? contextType.hashCode() : 0);
        result = 31 * result + (activityType != null ? activityType.hashCode() : 0);
        result = 31 * result + (approxTime != null ? approxTime.hashCode() : 0);
        return result;
    }
}
