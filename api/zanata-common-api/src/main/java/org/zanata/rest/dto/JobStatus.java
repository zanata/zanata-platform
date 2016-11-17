package org.zanata.rest.dto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "jobStatus")
@XmlType(name = "jobStatusType")
// FIXME see also AsynchronousProcessResource.startSourceDocCreation() and ProcessStatus
public class JobStatus {
    private String jobId;
//    private String url;
    private String username;
    // ISO8601 timestamps:
    private String startTime;
    private String statusTime;
    private String estimatedCompletionTime;
    private int percentCompleted;
    private long currentItem;
    private long totalItems;
    private boolean totalItemsIsEstimated;
    private List<JobStatusMessage> messages = new ArrayList<>();
    private JobStatusCode statusCode;

    public JobStatus() {
    }

    public JobStatus(String jobId) {
        this.jobId = jobId;
    }

    public @Nullable String getJobId() {
        return jobId;
    }

    @Override
    public String toString() {
        return "JobStatus{" +
                "jobId=" + jobId +
                ", username='" + username + '\'' +
                ", startTime='" + startTime + '\'' +
                ", statusTime='" + statusTime + '\'' +
                ", estimatedCompletionTime='" + estimatedCompletionTime + '\'' +
                ", percentCompleted=" + percentCompleted +
                ", currentItem=" + currentItem +
                ", totalItems=" + totalItems +
                ", totalItemsIsEstimated=" + totalItemsIsEstimated +
                ", messages=" + messages +
                '}';
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    @XmlTransient
    public void setStartTime(Instant startTime) {
        this.startTime = startTime.toString();
    }

    public String getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(String statusTime) {
        this.statusTime = statusTime;
    }

    @XmlTransient
    public void setStatusTime(Instant statusTime) {
        this.statusTime = statusTime.toString();
    }

    public String getEstimatedCompletionTime() {
        return estimatedCompletionTime;
    }

    public void setEstimatedCompletionTime(String estimatedCompletionTime) {
        this.estimatedCompletionTime = estimatedCompletionTime;
    }

    public int getPercentCompleted() {
        return percentCompleted;
    }

    public void setPercentCompleted(int percentCompleted) {
        this.percentCompleted = percentCompleted;
    }

    public long getCurrentItem() {
        return currentItem;
    }

    public void setCurrentItem(long currentItem) {
        this.currentItem = currentItem;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public boolean isTotalItemsIsEstimated() {
        return totalItemsIsEstimated;
    }

    public void setTotalItemsIsEstimated(boolean totalItemsIsEstimated) {
        this.totalItemsIsEstimated = totalItemsIsEstimated;
    }

    public List<JobStatusMessage> getMessages() {
        return messages;
    }

    public void setMessages(
            List<JobStatusMessage> messages) {
        this.messages = messages;
    }

    @XmlElement
    public JobStatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(JobStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    @XmlEnum
    public enum JobStatusCode {
        @XmlEnumValue("Waiting")
        Waiting,

        @XmlEnumValue("Running")
        Running,

        @XmlEnumValue("Finished")
        Finished,

        @XmlEnumValue("Failed")
        Failed,

//        @XmlEnumValue("NotAccepted")
//        NotAccepted,
    }

    @XmlType(name = "jobStatusMessage")
    public static class JobStatusMessage {
        // ISO8601 timestamp:
        private String time;
        private String level;
        private String text;

        public JobStatusMessage() {
        }

        public JobStatusMessage(Instant instant, String level, String text) {
            this.time = instant.toString();
            this.level = level;
            this.text = text;
        }

        public String getTime() {
            return time;
        }

        public String getLevel() {
            return level;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            return "JobStatusMessage{" +
                    "time='" + time + '\'' +
                    ", level='" + level + '\'' +
                    ", text='" + text + '\'' +
                    '}';
        }
    }
}
