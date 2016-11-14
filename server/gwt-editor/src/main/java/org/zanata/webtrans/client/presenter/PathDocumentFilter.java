package org.zanata.webtrans.client.presenter;

import java.util.Set;

import org.zanata.webtrans.shared.model.DocumentInfo;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;

/**
 * Filters documents by their full path + name, with substring and exact modes.
 *
 * If there is no pattern set, this filter will accept all documents.
 *
 * @author David Mason, damason@redhat.com
 *
 */
public final class PathDocumentFilter {
    private static final String DOCUMENT_FILTER_LIST_DELIMITER = ",";

    private Set<String> patterns = Sets.newHashSet();
    private Set<String> patternsInLowerCase = Sets.newHashSet();
    private boolean isFullText = false;
    private boolean caseSensitive = false;

    public boolean accept(DocumentInfo value) {
        if (patterns.isEmpty()) {
            return true;
        }
        String fullPath = value.getPath() + value.getName();

        Iterable<String> patternsToUse =
                caseSensitive ? patterns : patternsInLowerCase;
        fullPath = caseSensitive ? fullPath : fullPath.toLowerCase();

        Optional<String> matchedPattern =
                Iterables.tryFind(patternsToUse, new MatchPredicate(isFullText,
                        fullPath));
        return matchedPattern.isPresent();
    }

    public PathDocumentFilter setPattern(String pattern) {
        Splitter splitter =
                Splitter.on(DOCUMENT_FILTER_LIST_DELIMITER).trimResults()
                        .omitEmptyStrings();
        patterns = Sets.newHashSet(splitter.split(pattern));
        patternsInLowerCase =
                Sets.newHashSet(splitter.split(pattern.toLowerCase()));
        return this;
    }

    public PathDocumentFilter setFullText(boolean fullText) {
        isFullText = fullText;
        return this;
    }

    public PathDocumentFilter setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
        return this;
    }

    private static class MatchPredicate implements Predicate<String> {
        private final boolean isFullText;
        private final String fullPath;

        private MatchPredicate(boolean isFullText, String fullPath) {
            this.isFullText = isFullText;
            this.fullPath = fullPath;
        }

        @Override
        public boolean apply(String input) {
            return isFullText ? fullPath.equals(input) : fullPath
                    .contains(input);
        }
    }
}
