package org.zanata.rest.dto;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "streamUploadResponse")
@XmlType(name = "streamUploadResponseType")
public class StreamUploadResponse {
    private Long uploadId;
    private String successMessage;
    private String errorMessage;

    public StreamUploadResponse() {
    }

    /**
     * Create a response indicating that something went wrong before starting an
     * upload.
     *
     * @param errorMessage
     *            detailing what went wrong
     */
    public StreamUploadResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public StreamUploadResponse(long uploadId, String successMessage) {
        this.uploadId = uploadId;
        this.successMessage = successMessage;
    }

    @XmlElement(required = false)
    public Long getUploadId() {
        return uploadId;
    }

    public void setUploadId(Long uploadId) {
        this.uploadId = uploadId;
    }

    @XmlElement(required = false)
    public String getSuccessMessage() {
        return successMessage;
    }

    public void setSuccessMessage(String message) {
        successMessage = message;
    }

    @XmlElement(required = false)
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return "StreamUploadResponse: uploadId=" + uploadId
                + " successMessage=\"" + successMessage + "\" errorMessage=\""
                + errorMessage + "\".";
    }
}
