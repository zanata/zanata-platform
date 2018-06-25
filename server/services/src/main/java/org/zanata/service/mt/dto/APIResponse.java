package org.zanata.service.mt.dto;

import com.google.common.base.MoreObjects;

/**
 * @author Patrick Huang
 * <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
public class APIResponse {
    private int status;
    private String title;
    private String details;
    private String timestamp;

    public APIResponse(int status, String title, String details,
            String timestamp) {
        this.status = status;
        this.title = title;
        this.details = details;
        this.timestamp = timestamp;
    }

    public APIResponse() {
    }

    public int getStatus() {
        return status;
    }

    public String getTitle() {
        return title;
    }

    public String getDetails() {
        return details;
    }

    public String getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("status", status)
                .add("title", title)
                .add("details", details)
                .add("timestamp", timestamp)
                .toString();
    }
}
