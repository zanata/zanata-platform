package org.zanata.webtrans.client.history;

import java.util.Map;

import com.allen_sauer.gwt.log.client.Log;
import com.google.common.base.Strings;

/**
 * @author Patrick Huang <a
 *         href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
class Token implements Map.Entry<String, String> {
    static final Token NULL_TOKEN = new Token("", "");
    private static final String DELIMITER_K_V = ":";

    private String key;
    private String value;

    Token(String key, String value) {
        this.key = key;
        this.value = value;
    }

    static Token fromString(String tokenString) {
        if (Strings.isNullOrEmpty(tokenString)) {
            return NULL_TOKEN;
        }

        String[] parts = tokenString.split(DELIMITER_K_V);
        return parts.length == 2 ? new Token(parts[0], decode(parts[1]))
                : NULL_TOKEN;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public String setValue(String value) {
        this.value = Strings.nullToEmpty(value);
        return this.value;
    }

    @Override
    public String toString() {
        if (this == NULL_TOKEN) {
            return "";
        } else {
            return key + DELIMITER_K_V + encode(value);
        }
    }

    /**
     *
     * @see org.zanata.webtrans.client.history.Token#decode(String)
     * @param toEncode
     * @return the given string with all token delimiters encoded
     */
    static String encode(String toEncode) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < toEncode.length(); i++) {
            char nextChar = toEncode.charAt(i);
            switch (nextChar) {
            case ':':
                sb.append("!c");
                break;
            case ';':
                sb.append("!s");
                break;
            case '!':
                sb.append('!');
            default:
                sb.append(nextChar);
            }
        }
        // Log.debug("Encoded: \"" + toEncode + "\" to \"" + sb + "\"");
        return sb.toString();
    }

    /**
     * @see #encode(String)
     * @param toDecode
     * @return the given string with any encoded token delimiters decoded
     */
    static String decode(String toDecode) {
        StringBuilder sb = new StringBuilder();
        boolean escaped = false;
        for (int i = 0; i < toDecode.length(); i++) {
            char nextChar = toDecode.charAt(i);
            if (escaped) {
                escaped = false;
                switch (nextChar) {
                case '!':
                    sb.append('!');
                    break;
                case 'c':
                    sb.append(':');
                    break;
                case 's':
                    sb.append(';');
                    break;
                default:
                    Log.warn("Unrecognised escaped character, appending: "
                            + nextChar);
                    sb.append(nextChar);
                }
            } else if (nextChar == '!') {
                escaped = true;
            } else {
                sb.append(nextChar);
            }
        }
        Log.debug("Decoded: \"" + toDecode + "\" to \"" + sb + "\"");
        return sb.toString();
    }
}
