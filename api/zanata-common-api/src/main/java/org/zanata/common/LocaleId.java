package org.zanata.common;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;
import javax.validation.constraints.Size;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public class LocaleId implements Serializable {

    private static final long serialVersionUID = 1L;

    @Size(max = 255)
    private @Nonnull
    String id;
    // TODO split up to language code, country code, qualifier etc..

    public static final LocaleId EN = new LocaleId("en");
    public static final LocaleId EN_US = new LocaleId("en-US");
    public static final LocaleId DE = new LocaleId("de");
    public static final LocaleId FR = new LocaleId("fr");
    public static final LocaleId ES = new LocaleId("es");

    // JaxB needs a no-arg constructor :(
    // TODO can we make this private?
    public LocaleId() {
        id = "";
    }

    @JsonCreator
    public LocaleId(@Nonnull String localeId) {
        if (!isValid(localeId))
            throw new IllegalArgumentException(
                    "expected lang[-country[-modifier]], got " + localeId);
        this.id = localeId.intern();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof LocaleId))
            return false;
        return this.id.equals(((LocaleId) obj).id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    @JsonValue
    public @Nonnull
    String toString() {
        return id;
    }

    public static LocaleId fromJavaName(String localeName) {
        return new LocaleId(localeName.replace('_', '-'));
    }

    public @Nonnull
    String toJavaName() {
        return id.replace('-', '_');
    }

    /**
     * BCP-47 language tag
     *
     * @return
     */
    public @Nonnull
    String getId() {
        return id;
    }


    /**
     * Check validity of given code against BCP-47
     * @param localeId code to validate
     * @return
     */
    public boolean isValid(String localeId) {
        Matcher matcher = Pattern.compile("[\\w\\d@.-]*").matcher(localeId);
        return (matcher.matches()
                && !localeId.isEmpty()
                && localeId.length() <= 255
                && (Character.isAlphabetic(localeId.charAt(0))
                    || Character.isDigit(localeId.charAt(0)))
                && localeId.indexOf('_') == -1);
    }
}
