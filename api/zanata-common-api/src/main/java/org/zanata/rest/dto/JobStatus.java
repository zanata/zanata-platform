package org.zanata.rest.dto;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "jobStatus")
@XmlType(name = "jobStatusType")
// FIXME see also AsynchronousProcessResource.startSourceDocCreation() and ProcessStatus
public class JobStatus {
    private @Nullable Long jobId;
    private @Nullable String successMessage;
    private @Nullable String errorMessage;

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
        return "Job: uploadId=" + jobId
                + " successMessage=\"" + successMessage + "\" errorMessage=\""
                + errorMessage + "\".";
    }
}
