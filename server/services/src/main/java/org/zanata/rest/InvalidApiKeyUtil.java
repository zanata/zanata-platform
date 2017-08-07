package org.zanata.rest;

/**
 * Utility for invalid API key exception.
 *
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class InvalidApiKeyUtil {
    public static final String message = "Invalid API key";

    public static String getMessage(String username, String apiKey,
            String additionalMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append(getMessage(username, apiKey))
                .append(" ").append(additionalMessage);
        return sb.toString();
    }

    public static String getMessage(String username, String apiKey) {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(" for user: [").append(username).append("]")
                .append(" apiKey: [").append(apiKey).append("].");
        return sb.toString();
    }

    public static String getMessage(String additionalMessage) {
        StringBuilder sb = new StringBuilder();
        sb.append(message).append(". ").append(additionalMessage);
        return sb.toString();
    }
}
