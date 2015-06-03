package org.zanata.search;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class TransMemoryMatcher {
    private static final Document.OutputSettings OUTPUT_SETTINGS =
            new Document.OutputSettings()
                    .charset(Charsets.UTF_8).indentAmount(0)
                    .prettyPrint(false);
    private boolean canFullyReuseTM = false;
    private final Element transMemorySourceRootElement;
    private final Document upcomingSourceDoc;
    private LinkedList<TextTokenKey> upcomingSourceTokens;

    public TransMemoryMatcher(HTextFlow upcomingSource,
            HTextFlow transMemory, HLocale targetLocale) {
        // TODO pahuang work on plural
        String upcomingSourceContent = upcomingSource.getContents().get(0);
        String tmSource = transMemory.getContents().get(0);

        String tmTarget = transMemory.getTargets().get(targetLocale.getId()).getContents().get(
                0);
        logContext(upcomingSource, transMemory, targetLocale);

        // the HTML parse will use can not handle <p> <br> very well.
        // see http://webdesign.about.com/od/htmltags/a/aabg092299a.htm
        // jsoup will append a </p> to the end if it sees a standalone <p>
        // jsoup will ignore <br>
        upcomingSourceDoc = Jsoup.parseBodyFragment(upcomingSourceContent);
        transMemorySourceRootElement = Jsoup.parseBodyFragment(tmSource).body();

        TransMemoryHTMLParser transMemoryParser =
                new TransMemoryHTMLParser(transMemorySourceRootElement, tmTarget);

        if (!transMemoryParser.canFullyMatch) {
            canFullyReuseTM = false;
            return;
        }
        log.debug("=== about to parse upcoming source ===");
        upcomingSourceTokens = Lists.newLinkedList();
        List<Node> upcomingNodes = upcomingSourceDoc.body()
                .childNodes();
        depthFirstTraverseSourceNodes(upcomingNodes, new ParentNodes(),
                upcomingSourceTokens);

        Iterator<TextTokenKey> upcomingSourceTokensIt = upcomingSourceTokens
                .iterator();
        Iterator<TextTokenKey>
                transMemoryTokensIt = transMemoryParser.sourceTextTokens.iterator();
        TextTokenKey nextSourceToken = upcomingSourceTokensIt.next();
        TextTokenKey nextTransMemoryToken = transMemoryTokensIt.next();

        while (nextSourceToken != null) {
            if (nextTransMemoryToken != null
                    && matchText(nextSourceToken.optElementBefore,
                    nextTransMemoryToken.optElementBefore)
                    && matchText(nextSourceToken.optElementAfter,
                    nextTransMemoryToken.optElementAfter)) {
                log.debug("found source token for [{}] in TM [{}]", nextSourceToken, nextTransMemoryToken);

                // we can't update text on the node here becuase it will affect matchText(nextSourceToken.optElementBefore, nextTransMemoryToken.optElementBefore) above
                nextSourceToken.targetText = nextTransMemoryToken.targetText;
                nextTransMemoryToken = nextOrNull(transMemoryTokensIt);
            } else {
                log.debug("can not find matching translation for [{}] in TM", nextSourceToken);
                nextSourceToken.targetText = "";
            }
            nextSourceToken = nextOrNull(upcomingSourceTokensIt);
        }

        canFullyReuseTM = !upcomingSourceTokensIt.hasNext() &&
                !transMemoryTokensIt.hasNext();
    }

    private static boolean matchText(Optional<Element> optElement,
            Optional<Element> optOtherElement) {
        return getOptionalElementText(optElement).equals(getOptionalElementText(optOtherElement));

    }

    private static Optional<String> getOptionalElementText(Optional<Element> optElement) {
        return optElement.isPresent() ? Optional.of(optElement.get().text()) : Optional.<String>absent();
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

    private static void depthFirstTraverseSourceNodes(List<Node> sourceNodes,
            ParentNodes parentNodes,
            List<TextTokenKey> sourceTextTokens) {
        for (Node node : sourceNodes) {
            if (node instanceof TextNode) {
                TextNode textNode = (TextNode) node;
                TextTokenKey textTokenKey =
                        new TextTokenKey(textNode, parentNodes.parentTags(),
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

    public double calculateSimilarityPercent() {
        double similarity =
                LevenshteinTokenUtil.getSimilarity(
                        upcomingSourceDoc.body().text(),
                        transMemorySourceRootElement.text());
        double similarityPercent = similarity * 100;
        if (similarityPercent < 99.99) {
            // TODO pahuang here we could still try to put in reasonable effort (i.e. try to match as much text token as possible)
            return similarityPercent;
        }

        if (canFullyReuseTM) {
            return 100;
        }
        return similarityPercent;

    }


    public String translationFromTransMemory() {
        for (TextTokenKey upcomingSourceToken : upcomingSourceTokens) {
            upcomingSourceToken.node.text(upcomingSourceToken.targetText);
        }
        String translationBuildFromTM = upcomingSourceDoc.outputSettings(OUTPUT_SETTINGS).body().html();
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
        private final TextNode node;
        private final List<Tag> parentTags;
        private final Optional<Element> optElementBefore;
        private final Optional<Element> optElementAfter;
        private final int siblingIndex;
        private final String sourceText;
        private String targetText = "";

        @Override
        public String toString() {
            return "(" + parentTags + ")#" + siblingIndex +":" + sourceText;
        }
    }

    @RequiredArgsConstructor
    private static class TranslationToken {
        private static final TranslationToken EMPTY = new TranslationToken(Optional.<Element>absent(), Optional.<Element>absent(), "");
        private final Optional<Element> optElementBefore;
        private final Optional<Element> optElementAfter;
        private final String targetText;

        @Override
        public String toString() {
            return targetText;
        }
    }

    private static class TransMemoryHTMLParser {
        private final Element sourceRootElement;
        private boolean canFullyMatch = true;
        private List<TextTokenKey> sourceTextTokens = Lists.newLinkedList();
        private List<TranslationToken> targetTextTokens = Lists.newLinkedList();
        private List<TranslationToken> leftOverTransTokens = Lists.newLinkedList();

        private TransMemoryHTMLParser(
                Element sourceRootElement, String targetContent) {
            this.sourceRootElement = sourceRootElement;
            Document targetDoc = Jsoup.parseBodyFragment(targetContent);
            parse(targetDoc);
        }

        private void parse(Document targetDoc) {
            List<Node> targetChildNodes = targetDoc.body().childNodes();
            depthFirstTraverseTargetNodes(targetChildNodes);
            depthFirstTraverseSourceNodes(sourceRootElement.childNodes(),
                    new ParentNodes(), sourceTextTokens);
            matchSourceTokensToTargetTokens();
        }

        private void depthFirstTraverseTargetNodes(List<Node> targetChildNodes) {
            for (Node node : targetChildNodes) {
                if (node instanceof TextNode) {
                    TextNode textNode = (TextNode) node;
                    TranslationToken translationToken = new TranslationToken(
                            getOptionalBeforeSiblingElement(textNode),
                            getOptionalAfterSiblingElement(textNode),
                            textNode.getWholeText());
                    targetTextTokens.add(translationToken);
                } else {
                    Element element = (Element) node;
                    depthFirstTraverseTargetNodes(element.childNodes());
                }
            }
        }

        private void matchSourceTokensToTargetTokens() {
            Iterator<TextTokenKey> sourceTokensIt = sourceTextTokens.iterator();
            Iterator<TranslationToken>
                    targetTokensIt = targetTextTokens.iterator();
            TextTokenKey nextSourceToken = sourceTokensIt.next();
            TranslationToken nextTargetToken = targetTokensIt.next();

            while (nextSourceToken != null) {
                if (nextTargetToken != null
                        && matchTag(nextSourceToken.optElementBefore,
                                nextTargetToken.optElementBefore)
                        && matchTag(nextSourceToken.optElementAfter,
                                nextTargetToken.optElementAfter)) {
                    log.debug("found matching translation for [{}] -> [{}]", nextSourceToken, nextTargetToken);
                    nextSourceToken.targetText = nextTargetToken.targetText;
                    nextTargetToken = nextOrNull(targetTokensIt);
                } else {
                    log.debug("can not find matching translation for [{}]", nextSourceToken);
                    nextSourceToken.targetText = "";
                }
                nextSourceToken = nextOrNull(sourceTokensIt);
            }
            if (targetTokensIt.hasNext()) {
                Iterators.addAll(leftOverTransTokens, targetTokensIt);
                log.info("target tokens left: {}", leftOverTransTokens);
                canFullyMatch = false;
            }
        }

        private static Optional<Tag> getTag(Optional<Element> elementOptional) {
            return elementOptional.isPresent() ? Optional.of(
                    elementOptional.get().tag()) : Optional.<Tag>absent();
        }

        private static boolean matchTag(Optional<Element> optSourceEle, Optional<Element> optTargetEle) {
            return (getTag(optSourceEle).equals(getTag(optTargetEle)));
        }

    }

}
