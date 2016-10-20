package org.zanata.events;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Event for publish webhook after transaction.
 * See {@link org.zanata.service.impl.WebhookServiceImpl#onPublishWebhook}
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
@AllArgsConstructor
public final class WebhookEvent {

    @Getter
    @Nonnull
    private final String url;

    @Getter
    @Nullable
    private final String secret;

    @Getter
    @Nonnull
    private final WebhookEventType type;
}
