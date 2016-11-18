package org.zanata.rest.dto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "jobStatus")
@XmlType(name = "jobStatusType")
// FIXME see also AsynchronousProcessResource.startSourceDocCreation() and ProcessStatus
public class JobStatus {
    private String jobId;
//    private String url;
    private JobStatusCode statusCode;
    // ISO8601 timestamp:
    // TODO should this be remaining time instead? should it be here at all?
    private String estimatedCompletionTime;
    private long currentItem;
    private long totalItems;
    private List<String> messages = new ArrayList<>();

    public JobStatus() {
    }

    public JobStatus(String jobId) {
        this.jobId = jobId;
    }

    public @Nullable String getJobId() {
        return jobId;
    }

    @XmlElement
    public JobStatusCode getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(JobStatusCode statusCode) {
        this.statusCode = statusCode;
    }

    public String getEstimatedCompletionTime() {
        return estimatedCompletionTime;
    }

    public void setEstimatedCompletionTime(String estimatedCompletionTime) {
        this.estimatedCompletionTime = estimatedCompletionTime;
    }

    @XmlTransient
    public void setEstimatedCompletionTime(Instant estimatedCompletionTime) {
        this.estimatedCompletionTime = estimatedCompletionTime.toString();
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

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(
            List<String> messages) {
        this.messages = messages;
    }

    @Override
    public String toString() {
        return "JobStatus{" +
                "jobId=" + jobId +
                ", statusCode='" + statusCode + '\'' +
                ", estimatedCompletionTime='" + estimatedCompletionTime + '\'' +
                ", currentItem=" + currentItem +
                ", totalItems=" + totalItems +
                ", messages=" + messages +
                '}';
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

        @XmlEnumValue("Aborted")
        Aborted,
    }

}
