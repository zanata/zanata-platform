package org.zanata.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Event for publish webhook after transaction.
 * See {@link org.zanata.service.impl.WebhookServiceImpl#onPublishWebhook}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public final class WebhookEvent {

    @Nonnull
    private final String url;

    @Nullable
    private final String secret;

    @Nonnull
    private final WebhookEventType type;

    @java.beans.ConstructorProperties({ "url", "secret", "type" })
    public WebhookEvent(String url, String secret, WebhookEventType type) {
        this.url = url;
        this.secret = secret;
        this.type = type;
    }

    @Nonnull
    public String getUrl() {
        return this.url;
    }

    @Nullable
    public String getSecret() {
        return this.secret;
    }

    @Nonnull
    public WebhookEventType getType() {
        return this.type;
    }
}
