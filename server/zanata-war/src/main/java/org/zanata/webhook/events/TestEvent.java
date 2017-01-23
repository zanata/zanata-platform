package org.zanata.webhook.events;

import java.util.Date;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.zanata.events.WebhookEventType;

/**
 * Test Event WebHook
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@JsonPropertyOrder({ "username", "project", "date" })
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

    public String getUsername() {
        return this.username;
    }

    public String getProject() {
        return this.project;
    }

    public Date getDate() {
        return this.date;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public void setProject(final String project) {
        this.project = project;
    }

    public void setDate(final Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (!(o instanceof TestEvent))
            return false;
        final TestEvent other = (TestEvent) o;
        if (!other.canEqual((Object) this))
            return false;
        final Object this$username = this.getUsername();
        final Object other$username = other.getUsername();
        if (this$username == null ? other$username != null
                : !this$username.equals(other$username))
            return false;
        final Object this$project = this.getProject();
        final Object other$project = other.getProject();
        if (this$project == null ? other$project != null
                : !this$project.equals(other$project))
            return false;
        final Object this$date = this.getDate();
        final Object other$date = other.getDate();
        if (this$date == null ? other$date != null
                : !this$date.equals(other$date))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof TestEvent;
    }

    @Override
    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $username = this.getUsername();
        result = result * PRIME
                + ($username == null ? 43 : $username.hashCode());
        final Object $project = this.getProject();
        result = result * PRIME + ($project == null ? 43 : $project.hashCode());
        final Object $date = this.getDate();
        result = result * PRIME + ($date == null ? 43 : $date.hashCode());
        return result;
    }
}
