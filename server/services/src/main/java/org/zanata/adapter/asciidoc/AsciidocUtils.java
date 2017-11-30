package org.zanata.adapter.asciidoc;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.StringUtils;
import org.ocpsoft.common.util.Strings;
import org.zanata.util.HashUtil;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class AsciidocUtils {
    private final static String SEPARATOR = "\u0000";
    private final static String ATTR_ROLE = "role";
    private final static String ATTR_ID = "id";
    private final static String ATTR_TITLE = "title";

    private final static ImmutableList<String> admonitionStyle =
            ImmutableList.of("TIP", "NOTE", "WARNING", "IMPORTANT", "CAUTION");

    public final static ImmutableList<String> TABLE_ATTR =
            ImmutableList.of("width", "options", "style", "cols");

    public final static String NEWLINE = "\n";

    public final static ImmutableList<String> ATTR_WHITELIST =
            ImmutableList.of("description", "backend", "doctitle", "library", "stylesheet");

    public final static String getSectionTitleSkeleton(int level) {
        String ske = "";
        for (int i = 1; i <= level + 1; i++) {
            ske += "=";
        }
        return ske;
    }

    public final static String getDocAttribute(@NotNull String attr,
            @Nullable Object value) {
        StringBuilder sb = new StringBuilder();
        sb.append(":").append(attr).append(":");

        if (value != null) {
            sb.append(" ").append(value);
        } else {
            sb.append("");
        }
        return sb.toString();
    }

    //[[id]]
    public final static Optional<String> getId(
            @Nullable Map<String, Object> attrs) {
        if (attrs == null || attrs.isEmpty()) {
            return Optional.empty();
        }
        String role = (String)attrs.get(ATTR_ID);
        return StringUtils.isBlank(role) ? Optional.empty() :
                Optional.of("[[" + role + "]]");
    }

    //[role='lead']
    public final static Optional<String> getRole(
            @Nullable Map<String, Object> attrs) {
        if (attrs == null || attrs.isEmpty()) {
            return Optional.empty();
        }
        String role = (String)attrs.get(ATTR_ROLE);
        return StringUtils.isBlank(role) ? Optional.empty() :
                Optional.of("[role='" + role + "']");
    }

    //.Block_Title
    public final static Optional<String> getBlockTitle(
            @Nullable Map<String, Object> attrs) {
        if (attrs == null || attrs.isEmpty()) {
            return Optional.empty();
        }
        String role = (String)attrs.get(ATTR_TITLE);
        return StringUtils.isBlank(role) ? Optional.empty() :
                Optional.of(role);
    }

    public final static String getBlockTitleSkeleton() {
        return ".";
    }


    public final static String getResId(@Nullable String val) {
        return HashUtil.generateHash(val == null ? "" : val);
    }

    public final static String getResId(@Nullable List<String> vals) {
        return HashUtil.generateHash(
                vals == null ? "" : Strings.join(vals, SEPARATOR));
    }

    public final static String getAdmonitionSkeleton(
            Map<String, Object> attributes) {
        String style = attributes.get("style").toString();
        return style + ": ";
    }

    public final static String getTableSkeleton() {
        return "|===";
    }

    public final static String getCellStartSkeleton() {
        return "|";
    }

    public final static String getCellEndSkeleton() {
        return " ";
    }

    public final static String getBlockSkeleton() {
        return "****";
    }

    public final static boolean isAdmonition(
            Map<String, Object> attributes) {
        if (attributes.containsKey("style")) {
            String style = attributes.get("style").toString();
            return !StringUtils.isBlank(style) &&
                    admonitionStyle.contains(style);
        }
        return false;
    }

}
