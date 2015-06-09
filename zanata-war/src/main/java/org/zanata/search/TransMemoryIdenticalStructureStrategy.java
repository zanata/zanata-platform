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

import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
class TransMemoryIdenticalStructureStrategy implements TransMemoryReuseStrategy {

    private final List<List<Element>> upcomingSourceElements;
    private final List<List<TextNode>> upcomingSourceTextNodes;
    private final List<List<Element>> transMemorySourceElements;
    private final List<List<TextNode>> transMemoryTextNodes;
    private final String transMemoryTarget;

    TransMemoryIdenticalStructureStrategy(
            Element upcomingSourceRootElement,
            Element transMemorySourceRootElement, String tmTarget) {
        transMemoryTarget = tmTarget;

        upcomingSourceElements = Lists.newLinkedList();
        upcomingSourceTextNodes = Lists.newLinkedList();
        breadthFirstTraverse(upcomingSourceElements, upcomingSourceTextNodes,
                upcomingSourceRootElement);

        transMemorySourceElements = Lists.newLinkedList();
        transMemoryTextNodes = Lists.newLinkedList();
        breadthFirstTraverse(transMemorySourceElements, transMemoryTextNodes, transMemorySourceRootElement);

    }

    private static void breadthFirstTraverse(List<List<Element>> elementQueue,
            List<List<TextNode>> textNodes, Element rootElement) {
        Elements elements = rootElement.children();
        textNodes.add(rootElement.textNodes());
        elementQueue.add(elements);
        for (Element element : elements) {
            breadthFirstTraverse(elementQueue, textNodes, element);
        }
    }


    @Override
    public boolean canUse() {
        if (upcomingSourceElements.size() != transMemorySourceElements.size()) {
            return false;
        }
        if (upcomingSourceTextNodes.size() != transMemoryTextNodes.size()) {
            return false;
        }
        for (int i = 0, upcomingSourceElementsSize =
                upcomingSourceElements.size(); i < upcomingSourceElementsSize;
                i++) {
            List<Element> upcomingSourceElement = upcomingSourceElements.get(i);
            List<Element> transMemoryElement =
                    this.transMemorySourceElements.get(i);
            if (upcomingSourceElement.size() != transMemoryElement.size()) {
                return false;
            }
        }
        for (int i = 0, upcomingSourceTextNodesSize =
                upcomingSourceTextNodes.size(); i < upcomingSourceTextNodesSize;
                i++) {
            List<TextNode> upcomingSourceTextNode =
                    upcomingSourceTextNodes.get(i);
            List<TextNode> transMemoryTextNode = transMemoryTextNodes.get(i);

            if (!upcomingSourceTextNode.equals(transMemoryTextNode)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public String translationFromTransMemory() {
        Document transMemoryTargetDoc = Jsoup.parseBodyFragment(transMemoryTarget);
        transferElements(transMemoryTargetDoc.body().children(), 0);
        String translationBuildFromTM = transMemoryTargetDoc.outputSettings(
                OUTPUT_SETTINGS).body().html();
        log.debug("Translation build from given TM is:{}", translationBuildFromTM);
        return translationBuildFromTM;
    }

    private void transferElements(Elements targetElements, int depth) {
        for (final Element targetElement : targetElements) {
            int matchedSourceIndex =
                    Iterables.indexOf(transMemorySourceElements.get(depth),
                            new Predicate<Element>() {
                                @Override
                                public boolean apply(Element input) {
                                    return targetElement.tag()
                                            .equals(input.tag());
                                }
                            });
            Element upcomingSourceElement =
                    upcomingSourceElements.get(depth).get(matchedSourceIndex);
            targetElement.tagName(upcomingSourceElement.tagName());
            // N.B. by copying the attributes over, the attribute value will all
            // become double quotes
            for (Attribute attribute : upcomingSourceElement.attributes()) {
                targetElement.attr(attribute.getKey(), attribute.getValue());
            }
            // N.B. here we didn't migrate all data sets in HTML5

            transferElements(targetElement.children(), depth + 1);
        }

    }
}
