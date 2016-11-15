package org.zanata.rest.dto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "jobStatus")
@XmlType(name = "jobStatusType")
// FIXME see also AsynchronousProcessResource.startSourceDocCreation() and ProcessStatus
public class JobStatus {
    private Long jobId;
    private String successMessage;
    private String errorMessage;
    private String username;
    // ISO8601 dates:
    private String startTime;
    private String statusTime;
    private String estimatedCompletionTime;
    private int percentCompleted;
    private long currentItem;
    private long totalItems;
    private boolean totalItemsIsEstimated;
    private List<JobStatusMessage> messages = new ArrayList<>();

    public JobStatus() {
    }

    /**
     * Create a response indicating that something went wrong before starting
     * the job.
     *
     * @param errorMessage
     *            detailing what went wrong
     */
    public JobStatus(@Nonnull String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public JobStatus(long jobId, @Nonnull String successMessage) {
        this.jobId = jobId;
        this.successMessage = successMessage;
    }

    public @Nullable Long getJobId() {
        return jobId;
    }

    @XmlElement
    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String message) {
        successMessage = message;
    }

    @XmlElement
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "JobStatus{" +
                "jobId=" + jobId +
                ", successMessage='" + successMessage + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
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

    public String getStatusTime() {
        return statusTime;
    }

    public void setStatusTime(String statusTime) {
        this.statusTime = statusTime;
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

    @XmlType(name = "jobStatusMessage")
    public static class JobStatusMessage {
        private String time;
        private String level;
        private String text;

        public JobStatusMessage() {
        }
        public JobStatusMessage(String time, String level, String text) {
            this.time = time;
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
