package org.zanata.search;

import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;
import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class TransMemoryMatcher {
    private final SourceHTMLParser upcomingSourceParser;
    private final HTMLParser tmSourceParser;
    private final Map<TextTokenKey, String> tmTokensMap;
    private final Map<TextTokenKey, String> upcomingTokensMap;
    private final String upcomingSourceContent;

    public TransMemoryMatcher(HTextFlow upcomingSource,
            HTextFlow transMemory, HLocale targetLocale) {
        // TODO pahuang work on plural
        upcomingSourceContent = upcomingSource.getContents().get(0);
        String tmSource = transMemory.getContents().get(0);
        String tmTarget = transMemory.getTargets().get(targetLocale.getId()).getContents().get(
                0);
        logContext(upcomingSource, transMemory, targetLocale);

        Map<ParentNodes, Map.Entry<String, String>> tmMap = Maps.newHashMap();

        // the HTML parse will use can not handle <p> <br> very well.
        // see http://webdesign.about.com/od/htmltags/a/aabg092299a.htm
        // jsoup will append a </p> to the end if it sees a standalone <p>
        // jsoup will ignore <br>
        tmSourceParser = new SourceHTMLParser(tmMap, tmSource);
        HTMLParser tmTargetParser = new TargetHTMLParser(tmMap, tmTarget);

        tmSourceParser.parse();
        tmTargetParser.parse();

        Map<ParentNodes, Map.Entry<String, String>> upcomingSourceMap = Maps.newHashMap();

        upcomingSourceParser =
                new SourceHTMLParser(upcomingSourceMap, upcomingSourceContent);
        upcomingSourceParser.parse();

        tmTokensMap = toMatchableTextTokensMap(
                tmMap);
        upcomingTokensMap = toMatchableTextTokensMap(upcomingSourceMap);
    }

    private static void logContext(HTextFlow upcomingSource, HTextFlow transMemory,
            HLocale targetLocale) {
        if (log.isDebugEnabled()) {
            log.debug("about to match upcoming source: {} to TM: {} -> {}",
                    upcomingSource.getContents(), transMemory.getContents(),
                    transMemory.getTargets().get(targetLocale.getId())
                            .getContents());
        }
    }

    public double calculateSimilarityPercent() {
        double similarity =
                LevenshteinTokenUtil.getSimilarity(
                        tmSourceParser.strippedTagsTextContent(),
                        upcomingSourceParser.strippedTagsTextContent());
        double similarityPercent = similarity * 100;
        if (similarityPercent < 99.99) {
            // TODO pahuang here we could still try to put in reasonable effort (i.e. try to match as much text token as possible)
            return similarityPercent;
        }

        boolean canFullyReuseTM = canFullyReuseTM();

        if (canFullyReuseTM) {
            return 100;
        }
        return similarityPercent;

    }

    private boolean canFullyReuseTM() {
        for (Map.Entry<TextTokenKey, String> entry : upcomingTokensMap
                .entrySet()) {
            TextTokenKey key = entry.getKey();
            if (!tmTokensMap.containsKey(key)) {
                return false;
            }
        }
        return true;
    }


    private static Map<TextTokenKey, String> toMatchableTextTokensMap(
            Map<ParentNodes, Map.Entry<String, String>> parentNodesToSourceTargetMap) {
        ImmutableMap.Builder<TextTokenKey, String> tmMapBuilder = ImmutableMap.builder();
        for (Map.Entry<ParentNodes, Map.Entry<String, String>> entry : parentNodesToSourceTargetMap
                .entrySet()) {
            ParentNodes parentNodesForTextNode = entry.getKey();
            Map.Entry<String, String> sourceToTarget = entry.getValue();
            String textFlowSource = sourceToTarget
                    .getKey();
            String textFlowTarget = sourceToTarget.getValue() == null ? textFlowSource : sourceToTarget.getValue();
            tmMapBuilder.put(
                    new TextTokenKey(parentNodesForTextNode.size(),
                            textFlowSource), textFlowTarget);
        }
        return tmMapBuilder.build();
    }

    public String translationFromTransMemory() {
        HTMLParser upcomingSourceTargetParser =
                new HTMLParser(this.upcomingSourceParser.parentNodesToSourceTargetEntryMap,
                        upcomingSourceContent) {

                    @Override
                    protected void doWithTextNode(
                            ParentNodes parentNodes,
                            TextNode textNode) {
                        TextTokenKey textTokenKey =
                                new TextTokenKey(parentNodes.size(), textNode.getWholeText());
                        if (tmTokensMap.containsKey(textTokenKey) &&
                                parentNodesToSourceTargetEntryMap.containsKey(parentNodes)) {
                            parentNodesToSourceTargetEntryMap.get(parentNodes)
                                    .setValue(tmTokensMap.get(textTokenKey));
                        }
                    }
                };
        upcomingSourceTargetParser.parse();

        String translationBuildFromTM = upcomingSourceTargetParser.doc.body().html();
        log.debug("Translation build from given TM is {}", translationBuildFromTM);
        return translationBuildFromTM;
    }


    private static class ParentNodes {
        private final List<Element> parentNodes;
        private transient List<Tag> parentTags;

        ParentNodes() {
            this.parentNodes = ImmutableList.of();
        }

        private ParentNodes(ParentNodes parentNodes, Element elementNode) {
            this.parentNodes =
                    ImmutableList.<Element> builder().addAll(parentNodes.parentNodes)
                            .add(elementNode).build();
        }

        ParentNodes append(Element currentElementNode) {
            return new ParentNodes(this, currentElementNode);
        }

        public int size() {
            return parentNodes.size();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParentNodes that = (ParentNodes) o;
            ensureParentTags();
            return Objects.equals(parentTags, that.parentTags);
        }

        public void ensureParentTags() {
            if (parentTags == null) {
                parentTags = Lists
                        .transform(parentNodes, new Function<Element, Tag>() {
                            @Nullable
                            @Override
                            public Tag apply(Element input) {
                                return input.tag();
                            }
                        });
            }
        }

        @Override
        public int hashCode() {
            ensureParentTags();
            return Objects.hash(parentTags);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("parentTags", parentTags)
                    .toString();
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class TextTokenKey {
        private final int numOfParentNodes;
        private final String textToken;

        @Override
        public String toString() {
            return "(" + numOfParentNodes + "):" + textToken;
        }
    }

    private static class SourceTokensToTargetTokens {
        private final List<Map.Entry<String, String>> tokens;

        private SourceTokensToTargetTokens(
                List<Map.Entry<String, String>> tokens) {
            this.tokens = ImmutableList.copyOf(tokens);
        }

        private SourceTokensToTargetTokens(List<Map.Entry<String, String>> tokens, String sourceToken, String targetToken) {
            this.tokens = ImmutableList
                    .<Map.Entry<String, String>> builder()
                    .addAll(tokens)
                    .add(new AbstractMap.SimpleEntry<>(sourceToken,
                            targetToken)).build();
        }

        SourceTokensToTargetTokens add(String sourceToken, String targetToken) {
            return new SourceTokensToTargetTokens(tokens, sourceToken, targetToken);
        }
    }

    private abstract static class HTMLParser {
        private static final Document.OutputSettings OUTPUT_SETTINGS =
                new Document.OutputSettings()
                        .charset(Charsets.UTF_8).indentAmount(0)
                        .prettyPrint(false);
        protected final Map<ParentNodes, Map.Entry<String, String>>
                parentNodesToSourceTargetEntryMap;
        private final String htmlContent;
        private final StringBuilder textContent;
        private Document doc;

        private HTMLParser(
                Map<ParentNodes, Map.Entry<String, String>> parentNodesToSourceTargetEntryMap,
                String htmlContent) {
            this.parentNodesToSourceTargetEntryMap =
                    parentNodesToSourceTargetEntryMap;
            this.htmlContent = htmlContent;
            this.textContent = new StringBuilder();
        }

        public void parse() {
            doc = Jsoup.parseBodyFragment(htmlContent)
                    .outputSettings(OUTPUT_SETTINGS);
            parseTransMemory(doc.body().childNodes(), new ParentNodes());
        }

        private void parseTransMemory(List<Node> childNodes, ParentNodes parentNodes) {

            for (Node node : childNodes) {
                if (node instanceof Element) {
                    parseTransMemory(node.childNodes(), parentNodes.append(
                            (Element) node));
                } else if (node instanceof TextNode) {
                    String wholeText = ((TextNode) node).getWholeText();
                    // TODO this does not take into account stop words etc. It
                    // will affect not 100% identical text content TM result
                    // (e.g. not only tags are different but some stop words are
                    // different too)
                    doWithTextNode(parentNodes, ((TextNode) node));
                    textContent.append(wholeText);
                }

            }
        }

        protected abstract void doWithTextNode(ParentNodes parentNodes,
                TextNode textNode);

        public String strippedTagsTextContent() {
            return textContent.toString();
        }
    }

    private static class SourceHTMLParser extends HTMLParser {

        public SourceHTMLParser(
                Map<ParentNodes, Map.Entry<String, String>> tmMap,
                String tmSource) {
            super(tmMap, tmSource);
        }

        @Override
        protected void doWithTextNode(ParentNodes parentNodes,
                TextNode textNode) {
            AbstractMap.SimpleEntry<String, String>
                    sourceToTarget =
                    new AbstractMap.SimpleEntry<>(textNode.getWholeText(),
                            null);
            if (parentNodesToSourceTargetEntryMap.containsKey(parentNodes)) {
                Map.Entry<String, String> previousEntry =
                        parentNodesToSourceTargetEntryMap.get(parentNodes);

            }
            parentNodesToSourceTargetEntryMap
                    .put(parentNodes, sourceToTarget);
        }
    }

    private static class TargetHTMLParser extends HTMLParser {

        public TargetHTMLParser(
                Map<ParentNodes, Map.Entry<String, String>> tmMap,
                String tmTarget) {
            super(tmMap, tmTarget);
        }

        @Override
        protected void doWithTextNode(ParentNodes parentNodes,
                TextNode textNode) {
            Map.Entry<String, String> sourceToTarget =
                    parentNodesToSourceTargetEntryMap.get(parentNodes);
            String wholeText = textNode.getWholeText();
            if (sourceToTarget == null) {
                log.warn("Can not match translation text token [{}] in source using parent nodes:{}", wholeText,
                        parentNodes);

                throw new IllegalStateException(
                        "can not match translation text token [" +
                                wholeText
                                + "] in source using parent nodes:"
                                + parentNodes);
            }
            sourceToTarget.setValue(wholeText);
        }
    }
}
