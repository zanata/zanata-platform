package org.zanata.webhook.events;

import java.util.Date;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.zanata.events.WebhookEventType;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * Test Event WebHook
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@Getter
@Setter
@JsonPropertyOrder({"username", "project", "date"})
@EqualsAndHashCode
public class TestEvent extends WebhookEventType {

    public TestEvent(String username, String project) {
        this.username = username;
        this.project = project;
        this.date = new Date();
    }
    private String username;

    private String project;

    private Date date;

    @Override
    public String getType() {
        return "TEST_EVENT";
    }
}
