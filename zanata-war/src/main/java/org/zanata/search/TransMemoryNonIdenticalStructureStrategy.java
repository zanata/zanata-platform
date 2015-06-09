/*
 * Copyright 2015, Red Hat, Inc. and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.zanata.search;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.parser.Tag;
import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
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
class TransMemoryNonIdenticalStructureStrategy implements TransMemoryReuseStrategy {

    private final Document upcomingSourceDoc;
    private boolean canFullyReuseTM;

    TransMemoryNonIdenticalStructureStrategy(Document upcomingSourceDoc,
            Element transMemorySourceRootElement, String tmTarget) {
        this.upcomingSourceDoc = upcomingSourceDoc;
        Element transMemoryTargetRootElement =
                Jsoup.parseBodyFragment(tmTarget).body();


        TransMemoryHTMLParser transMemoryParser =
                new TransMemoryHTMLParser(transMemorySourceRootElement,
                        transMemoryTargetRootElement);

        if (!transMemoryParser.perfectMatch && !transMemoryParser.heuristicMatch) {
            log.info(
                    "can not analyze TM - don't know how to match some translation tokens: {}",
                    transMemoryParser.leftOverTransTokens);
            canFullyReuseTM = false;
            return;
        }
        List<TextTokenKey> transMemoryTextTokens = transMemoryParser.textTokens;

        log.debug("=== about to parse upcoming source ===");
        List<Node> upcomingNodes = upcomingSourceDoc.body()
                .childNodes();
        LinkedList<TextTokenKey> upcomingSourceTokens = Lists.newLinkedList();
        depthFirstTraverseSourceNodes(upcomingNodes, new ParentNodes(),
                upcomingSourceTokens);

        Iterator<TextTokenKey> upcomingSourceTokensIt = upcomingSourceTokens
                .iterator();
        Iterator<TextTokenKey>
                transMemoryTokensIt = transMemoryTextTokens.iterator();
        TextTokenKey nextSourceToken = upcomingSourceTokensIt.next();
        TextTokenKey nextTransMemoryToken = transMemoryTokensIt.next();

        while (nextSourceToken != null) {
            if (upcomingSourceTokenMatchesTMSourceToken(
                    nextSourceToken,
                    nextTransMemoryToken)) {
                log.debug("found source token for [{}] in TM [{}]", nextSourceToken, nextTransMemoryToken);

                // we can't update text on the node here because it will affect matchText(nextSourceToken.optElementBefore, nextTransMemoryToken.optElementBefore) above
                nextSourceToken.targetText = nextTransMemoryToken.targetText;
                nextTransMemoryToken = nextOrNull(transMemoryTokensIt);
            } else {
                log.debug("can not find matching translation for [{}] in TM", nextSourceToken);
                nextSourceToken.targetText = "!UnKnoWn!";
            }
            nextSourceToken = nextOrNull(upcomingSourceTokensIt);
        }

        canFullyReuseTM = !upcomingSourceTokensIt.hasNext() &&
                !transMemoryTokensIt.hasNext();

        // apply matched tokens
        if (canFullyReuseTM) {
            StringBuilder generatedTargetTextOnly = new StringBuilder();
            for (TextTokenKey upcomingSourceToken : upcomingSourceTokens) {
                upcomingSourceToken.sourceNode.text(upcomingSourceToken.targetText);
                generatedTargetTextOnly.append(upcomingSourceToken.targetText);
            }
            String tmTargetTextOnly =
                    transMemoryParser.tmTargetTextOnly.toString();
            String translationBuildFromTM = upcomingSourceDoc.outputSettings(
                    TransMemoryReuseStrategy.OUTPUT_SETTINGS).body().html();
            log.debug("Translation build from given TM is:{}", translationBuildFromTM);
            // do a final comparison to see if the generated translation matches
            // TM translation (in case some tags has changed locations,
            // I don't have a way to fix it.
            log.debug(
                    "comparing TM target text only to generated target text:\nTM :{}\nGEN:{}",
                    tmTargetTextOnly, generatedTargetTextOnly);
            canFullyReuseTM = generatedTargetTextOnly.toString().equals(
                    tmTargetTextOnly);
        }
    }

    /**
     * We compare the token's before and after element text.
     *
     * @param upcomingSourceToken text token in upcoming source
     * @param transMemoryToken    text token in trans memory
     * @return true if two tokens' before element and after element matches in text
     */
    private static boolean upcomingSourceTokenMatchesTMSourceToken(
            TextTokenKey upcomingSourceToken,
            TextTokenKey transMemoryToken) {
        return transMemoryToken != null
                && upcomingSourceToken != null
                && matchText(upcomingSourceToken.optElementBefore,
                transMemoryToken.optElementBefore)
                && matchText(upcomingSourceToken.optElementAfter,
                transMemoryToken.optElementAfter);
    }

    private static boolean matchText(Optional<Element> optElement,
            Optional<Element> optOtherElement) {
        return getOptionalElementText(optElement).equals(getOptionalElementText(optOtherElement));

    }

    private static Optional<String> getOptionalElementText(Optional<Element> optElement) {
        return optElement.isPresent() ? Optional.of(optElement.get().text()) : Optional.<String>absent();
    }

    private static void depthFirstTraverseSourceNodes(List<Node> sourceNodes,
            ParentNodes parentNodes,
            List<TextTokenKey> sourceTextTokens) {
        for (Node node : sourceNodes) {
            if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                TextTokenKey textTokenKey =
                        new TextTokenKey(textNode, parentNodes,
                                getOptionalBeforeSiblingElement(textNode),
                                getOptionalAfterSiblingElement(textNode),
                                textNode.siblingIndex(),
                                textNode.getWholeText());
                sourceTextTokens.add(textTokenKey);
            } else {
                Element element = (Element) node;
                depthFirstTraverseSourceNodes(element.childNodes(), parentNodes.append(element),
                        sourceTextTokens);
            }
        }
    }

    private static Optional<Element> getOptionalAfterSiblingElement(
            TextNode textNode) {
        return Optional.fromNullable(
                (Element) textNode.nextSibling());
    }

    private static Optional<Element> getOptionalBeforeSiblingElement(
            TextNode textNode) {
        return Optional.fromNullable(
                (Element) textNode.previousSibling());
    }

    private static  <T> T nextOrNull(Iterator<T> iterator) {
        return (iterator.hasNext()) ? iterator.next() : null;
    }

    @Override
    public boolean canUse() {
        return canFullyReuseTM;
    }

    @Override
    public String translationFromTransMemory() {
        return upcomingSourceDoc.outputSettings(
                TransMemoryReuseStrategy.OUTPUT_SETTINGS).body().html();
    }

    private static class ParentNodes {
        private final List<Element> parentElements;
        private transient List<Tag> parentTags;

        ParentNodes() {
            this.parentElements = ImmutableList.of();
        }

        private ParentNodes(ParentNodes parentNodes, Element elementNode) {
            this.parentElements =
                    ImmutableList.<Element> builder().addAll(parentNodes.parentElements)
                            .add(elementNode).build();
        }

        ParentNodes append(Element currentElementNode) {
            return new ParentNodes(this, currentElementNode);
        }

        public int size() {
            return parentElements.size();
        }

        /**
         * Checking whether the parent elements are identical. When we try to
         * compare between source tokens, we need to make sure they share
         * exactly the same elements not just tags. When we do look up for
         * matching translation tokens, we use tags.
         *
         * @param o
         *            other ParentNodes object
         * @return true if parent nodes list are identical in identity
         */
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ParentNodes that = (ParentNodes) o;
            return Objects.equals(parentElements, that.parentElements);
        }

        @Override
        public int hashCode() {
            ensureParentTags();
            return Objects.hash(parentElements);
        }

        private void ensureParentTags() {
            if (parentTags == null) {
                parentTags = Lists
                        .transform(parentElements, new Function<Element, Tag>() {
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
            return parentTags().toString();
        }
    }

    @RequiredArgsConstructor
    @EqualsAndHashCode
    private static class TextTokenKey implements Comparable<TextTokenKey> {
        private final TextNode sourceNode;
        private final ParentNodes parentNodes;
        private final Optional<Element> optElementBefore;
        private final Optional<Element> optElementAfter;
        private final int siblingIndex;
        private final String sourceText;
        private TranslationToken matchedTranslationToken;
        private String targetText = "";
        public int appearanceOrder;

        @Override
        public String toString() {
            return "(" + parentNodes + ")#" + siblingIndex +":" + sourceText;
        }

        @Override
        public int compareTo(@Nonnull TextTokenKey o) {
            return appearanceOrder - o.appearanceOrder;
        }
    }

    @RequiredArgsConstructor
    private static class TranslationToken {
        private final List<Tag> parentTags;
        private final Optional<Element> optElementBefore;
        private final Optional<Element> optElementAfter;
        private final int siblingIndex;
        private final String targetText;

        @Override
        public String toString() {
            return "(" + parentTags + ")" + targetText;
        }
    }

    private static class TransMemoryHTMLParser {
        private boolean perfectMatch = true;
        private boolean heuristicMatch = true;
        private List<TextTokenKey> textTokens = Lists.newLinkedList();
        private StringBuilder tmTargetTextOnly = new StringBuilder();

        private transient Map<List<Tag>, List<TranslationToken>>
                parentTagsToTransTokens =
                Maps.newHashMap();
        private transient List<TextTokenKey> leftOverSourceTokens = Lists.newLinkedList();
        private transient List<TranslationToken> leftOverTransTokens = Collections
                .emptyList();

        private TransMemoryHTMLParser(
                Element sourceRootElement, Element targetRootElement) {
            parse(sourceRootElement, targetRootElement);
        }

        private void parse(Element sourceRootElement, Element targetElement) {
            List<Node> targetChildNodes = targetElement.childNodes();
            depthFirstTraverseTargetNodes(targetChildNodes, new ParentNodes());
            depthFirstTraverseSourceNodes(sourceRootElement.childNodes(),
                    new ParentNodes(), textTokens);
            matchSourceTokensToTargetTokens();
        }

        // TODO uses recursion. Potentially for deep HTML doc it may bloat up stack
        private void depthFirstTraverseTargetNodes(List<Node> targetChildNodes,
                ParentNodes parentNodes) {
            for (Node node : targetChildNodes) {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    tmTargetTextOnly.append(textNode.getWholeText());
                    TranslationToken translationToken = new TranslationToken(
                            parentNodes.parentTags(),
                            getOptionalBeforeSiblingElement(textNode),
                            getOptionalAfterSiblingElement(textNode),
                            textNode.siblingIndex(),
                            textNode.getWholeText());
                    addToParentTagsMap(parentNodes.parentTags(),
                            translationToken);
                } else {
                    Element element = (Element) node;
                    depthFirstTraverseTargetNodes(element.childNodes(),
                            parentNodes.append(element));
                }
            }
        }

        private void addToParentTagsMap(
                List<Tag> tags, TranslationToken translationToken) {
            if (parentTagsToTransTokens.containsKey(tags)) {
                parentTagsToTransTokens.get(tags).add(translationToken);
            } else {
                parentTagsToTransTokens.put(tags, Lists.newArrayList(translationToken));
            }
        }

        private void matchSourceTokensToTargetTokens() {

            for (TextTokenKey sourceToken : textTokens) {
                List<TranslationToken> transTokensUnderThisParentTags =
                        MoreObjects.firstNonNull(parentTagsToTransTokens
                                        .get(sourceToken.parentNodes
                                                .parentTags()),
                                Collections.<TranslationToken>emptyList());
                log.debug("translation tokens under this parent tags [{}]: {}",
                        sourceToken.parentNodes,
                        transTokensUnderThisParentTags);

                // we match translation token to source token by checking their
                // before and after element tag matches and they have same
                // parent tags
                Optional<TranslationToken> optMatchTransToken =
                        Iterables.tryFind(transTokensUnderThisParentTags,
                                new ExactTMSourceToTargetMatchingPredicate(
                                        sourceToken));
                if (optMatchTransToken.isPresent()) {
                    TranslationToken translationToken =
                            optMatchTransToken.get();
                    log.debug("found matching translation for [{}] -> [{}]", sourceToken, translationToken);
                    sourceToken.matchedTranslationToken = translationToken;
                    sourceToken.targetText = translationToken.targetText;
                    // remove matched translation
                    transTokensUnderThisParentTags.remove(translationToken);
                } else {
                    log.debug("can not find matching translation for [{}], assuming empty string for now.", sourceToken);
                    sourceToken.targetText = "";
                    leftOverSourceTokens.add(sourceToken);
                }
            }

            leftOverTransTokens =
                    Lists.newLinkedList(Iterables.concat(parentTagsToTransTokens.values()));
            if (leftOverTransTokens.size() > 0) {
                perfectMatch = false;
                log.info("target tokens left: {}", leftOverTransTokens);
                int leftOverTransSize = leftOverTransTokens.size();
                for (TranslationToken leftOverTransToken : leftOverTransTokens) {
                    // here we have to use some heuristic
                    Iterable<TextTokenKey> potentialMatchedTrans =
                            Iterables.filter(leftOverSourceTokens,
                                    new HeuristicTMSourceToTargetMatchingPredicate(
                                            leftOverTransToken));
                    if (Iterables.size(potentialMatchedTrans) > 0) {
                        TextTokenKey sourceToken =
                                potentialMatchedTrans.iterator().next();
                        log.info("<Heuristic match>: assuming source token [{}] -> target token [{}]", sourceToken,
                                leftOverTransToken);
                        sourceToken.matchedTranslationToken = leftOverTransToken;
                        sourceToken.targetText = leftOverTransToken.targetText;
                        leftOverSourceTokens.remove(sourceToken);
                        leftOverTransSize--;
                    }
                }

                // after apply heuristic, if there is no left over translation token
                heuristicMatch = leftOverTransSize == 0;
            }
        }

        /**
         * This will match text tokens by its parent tags, before element and
         * after element tag.
         */
        private static class ExactTMSourceToTargetMatchingPredicate
                implements Predicate<TranslationToken> {
            private final TextTokenKey sourceToken;

            public ExactTMSourceToTargetMatchingPredicate(
                    TextTokenKey sourceToken) {
                this.sourceToken = sourceToken;
            }

            @Override
            public boolean apply(@Nullable TranslationToken transToken) {
                return transToken != null
                        && sourceToken.parentNodes.parentTags().equals(
                        transToken.parentTags)
                        && matchTag(
                        sourceToken.optElementBefore,
                        transToken.optElementBefore)
                        && matchTag(
                        sourceToken.optElementAfter,
                        transToken.optElementAfter);
            }

            private static Optional<Tag> getTag(Optional<Element> elementOptional) {
                return elementOptional.isPresent() ? Optional.of(
                        elementOptional.get().tag()) : Optional.<Tag>absent();
            }

            private static boolean matchTag(Optional<Element> optSourceEle, Optional<Element> optTargetEle) {
                return (getTag(optSourceEle).equals(getTag(optTargetEle)));
            }
        }

        /**
         * This will try to match parent tags, but only match one of below in order:
         * <pre>
         * 1. Before element tag matches
         * 2. After element tag matches
         * 3. sibling index matches
         * </pre>
         */
        private static class HeuristicTMSourceToTargetMatchingPredicate
                implements Predicate<TextTokenKey> {
            private final TranslationToken targetToken;

            public HeuristicTMSourceToTargetMatchingPredicate(
                    TranslationToken targetToken) {
                this.targetToken = targetToken;
            }

            @Override
            public boolean apply(
                    @Nullable TextTokenKey input) {
                if (input == null) {
                    return false;
                }
                // if parent tag is different, it's definitely wrong
                if (!targetToken.parentTags
                        .equals(input.parentNodes
                                .parentTags())) {
                    return false;
                }
                // before element tag matches
                if (targetToken.optElementBefore
                        .isPresent()) {
                    Tag beforeTag =
                            targetToken.optElementBefore
                                    .get().tag();
                    return input.optElementBefore
                            .isPresent() &&
                            input.optElementBefore
                                    .get().tag()
                                    .equals(
                                            beforeTag);
                }
                // after element tag matches
                if (targetToken.optElementAfter
                        .isPresent()) {
                    Tag afterTag =
                            targetToken.optElementAfter
                                    .get().tag();
                    return input.optElementAfter
                            .isPresent() &&
                            input.optElementAfter
                                    .get().tag()
                                    .equals(afterTag);
                }
                // if nothing matches (tags may have swapped location in translation)
                return targetToken.siblingIndex ==
                        input.siblingIndex;
            }
        }
    }
}
