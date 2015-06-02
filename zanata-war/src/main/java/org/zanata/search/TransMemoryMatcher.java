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
    private static final String EMPTY_STRING = "";
    private final HTMLParser tmSourceParser;
    private final Map<TextTokenKey, String> tmTokensMap;
    private final Map<TextTokenKey, String> upcomingTokensMap;
    private final HTMLParser upcomingSourceParser;

    public TransMemoryMatcher(HTextFlow upcomingSource,
            HTextFlow transMemory, HLocale targetLocale) {
        // TODO pahuang work on plural
        String upcomingSourceContent = upcomingSource.getContents().get(0);
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

        log.debug("=== about to parse TM source ===");
        tmSourceParser.parse();
        log.debug("=== about to parse TM target ===");
        tmTargetParser.parse();

        Map<ParentNodes, Map.Entry<String, String>> upcomingSourceMap = Maps.newHashMap();

        log.debug("=== about to parse upcoming source ===");
        upcomingSourceParser =
                new HTMLParser(
                        upcomingSourceMap,
                        upcomingSourceContent) {

                    @Override
                    protected void doWithTextNode(
                            ParentNodes parentNodes,
                            TextNode textNode) {
                        String sourceTokenText = textNode.getWholeText();
                        String translation = EMPTY_STRING;
                        // text node text will get updated if it can find a
                        // match in TM tokens
                        textNode.text(translation);
                        if (parentNodesToSourceTargetEntryMap.containsKey(parentNodes)) {
                            Map.Entry<String, String> previousEntry =
                                    parentNodesToSourceTargetEntryMap.get(parentNodes);
                            // more than one text token share the same parent nodes, we combine the text together
                            String combinedText = previousEntry.getKey() +
                                    sourceTokenText;
                            Map.Entry<String, String> newEntry =
                                    makeMapEntry(combinedText, translation);
                            parentNodesToSourceTargetEntryMap.put(parentNodes, newEntry);
                            TextTokenKey key =
                                    new TextTokenKey(parentNodes.size(),
                                            combinedText);
                            if (tmTokensMap.containsKey(key)) {
                                translation = tmTokensMap.get(key);
                                textNode.text(translation);
                                newEntry.setValue(translation);
                            }

                        } else {
                            Map.Entry<String, String>
                                    sourceToTarget =
                                    makeMapEntry(sourceTokenText, translation);
                            parentNodesToSourceTargetEntryMap
                                    .put(parentNodes, sourceToTarget);
                            TextTokenKey key =
                                    new TextTokenKey(parentNodes.size(),
                                            sourceTokenText);
                            if (tmTokensMap.containsKey(key)) {
                                translation = tmTokensMap.get(key);
                                textNode.text(translation);
                                sourceToTarget.setValue(translation);
                            }

                        }
                    }
                };
        upcomingSourceParser.parse();

        tmTokensMap = toMatchableTextTokensMap(tmMap);
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
            String textFlowTarget = sourceToTarget.getValue() == null ?
                    EMPTY_STRING : sourceToTarget.getValue();
            tmMapBuilder.put(
                    new TextTokenKey(parentNodesForTextNode.size(),
                            textFlowSource), textFlowTarget);
        }
        return tmMapBuilder.build();
    }

    public String translationFromTransMemory() {
        String translationBuildFromTM = upcomingSourceParser.doc.body().html();
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

        /**
         * Checking whether the parent nodes are identical. When we try to join
         * together source tokens, we need to make sure they share exactly the
         * same nodes not just tags as parents. When we do look up for matching
         * translation tokens, we use different algorithm.
         *
         * @param o other ParentNodes object
         * @return true if parent nodes list are identical in identity
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParentNodes that = (ParentNodes) o;
            return Objects.equals(parentNodes, that.parentNodes);
        }

        @Override
        public int hashCode() {
            ensureParentTags();
            return Objects.hash(parentNodes);
        }

        private void ensureParentTags() {
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

        List<Tag> parentTags() {
            ensureParentTags();
            return parentTags;
        }

        @Override
        public String toString() {
            ensureParentTags();
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
            String sourceTokenText = textNode.getWholeText();

            if (parentNodesToSourceTargetEntryMap.containsKey(parentNodes)) {
                Map.Entry<String, String> previousEntry =
                        parentNodesToSourceTargetEntryMap.get(parentNodes);
                // more than one text token share the same parent nodes, we will
                // store individual token as well as combined tokens in case
                // text tokens get swapped around in translation
                Map.Entry<String, String>
                        newEntry = makeMapEntry(previousEntry.getKey() +
                        sourceTokenText, EMPTY_STRING);
                parentNodesToSourceTargetEntryMap.put(parentNodes, newEntry);
                log.debug("appending to parent nodes: {}, old token(s) [{}], new token [{}]",
                        parentNodes, previousEntry.getKey(), sourceTokenText);

            } else {
                Map.Entry<String, String>
                        sourceToTarget =
                        makeMapEntry(sourceTokenText, EMPTY_STRING);
                parentNodesToSourceTargetEntryMap
                        .put(parentNodes, sourceToTarget);
                log.debug("putting to parent nodes: {}, token [{}]",
                        parentNodes, sourceTokenText);
            }
        }
    }

    private static <K, V> Map.Entry<K, V> makeMapEntry(K key, V value) {
        return new AbstractMap.SimpleEntry<>(key, value);
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
            String targetTokenText = textNode.getWholeText();
            Map.Entry<String, String> matchingSourceToken =
                    findMatchingSourceToken(parentNodes, targetTokenText);
            // we may have other translation tokens under same parent nodes
            String previousTrans = matchingSourceToken.getValue();
            matchingSourceToken.setValue(previousTrans + targetTokenText);
            log.debug(
                    "putting to source token [{}] as translation: existing trans [{}], current trans [{}]",
                    matchingSourceToken.getKey(), previousTrans,
                    targetTokenText);
        }

        private Map.Entry<String, String> findMatchingSourceToken(
                ParentNodes parentNodes, String targetTokenText) {
            // because we build a new ParentNodes for target, the identity of
            // parent nodes will NOT be the same but the tag names will be. We
            // have to use tag names to look up matching source tokens
            for (Map.Entry<ParentNodes, Map.Entry<String, String>> entry : parentNodesToSourceTargetEntryMap
                    .entrySet()) {
                ParentNodes key = entry.getKey();
                if (key.parentTags().equals(parentNodes.parentTags())) {
                    return entry.getValue();
                }
            }
            log.warn("Can not match translation text token [{}] in source using parent nodes:{}", targetTokenText,
                    parentNodes);

            throw new IllegalStateException(
                    "can not match translation text token [" +
                            targetTokenText
                            + "] in source using parent nodes:"
                            + parentNodes);
        }
    }
}
