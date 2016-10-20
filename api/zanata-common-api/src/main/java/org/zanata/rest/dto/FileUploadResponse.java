package org.zanata.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "fileUploadResponse")
@XmlType(name = "fileUploadResponseType")
// FIXME see also AsynchronousProcessResource.startSourceDocCreation() and ProcessStatus
public class FileUploadResponse {
    // FIXME eliminate this?
    private Long uploadId;
    private String successMessage;
    private String errorMessage;

    public FileUploadResponse() {
    }

    /**
     * Create a response indicating that something went wrong before starting an
     * upload.
     *
     * @param errorMessage
     *            detailing what went wrong
     */
    public FileUploadResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public FileUploadResponse(long uploadId, String successMessage) {
        this.uploadId = uploadId;
        this.successMessage = successMessage;
    }

    @XmlElement
    public Long getUploadId() {
        return uploadId;
    }

    public void setUploadId(Long uploadId) {
        this.uploadId = uploadId;
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
        return "FileUploadResponse: uploadId=" + uploadId
                + " successMessage=\"" + successMessage + "\" errorMessage=\""
                + errorMessage + "\".";
    }
}
