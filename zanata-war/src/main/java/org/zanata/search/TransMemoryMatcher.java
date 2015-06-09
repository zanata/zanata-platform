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

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.zanata.model.HLocale;
import org.zanata.model.HTextFlow;

/**
 * @author Patrick Huang
 *         <a href="mailto:pahuang@redhat.com">pahuang@redhat.com</a>
 */
@Slf4j
public class TransMemoryMatcher {
    private final Element transMemorySourceRootElement;
    private final String upcomingSourceTextOnly;
    private TransMemoryReuseStrategy strategy;

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
        Document upcomingSourceDoc =
                Jsoup.parseBodyFragment(upcomingSourceContent);
        upcomingSourceTextOnly = upcomingSourceDoc.body().text();

        transMemorySourceRootElement = Jsoup.parseBodyFragment(tmSource).body();

        // by default we try to use strategy that can handle identical xml
        // structure
        strategy =
                new TransMemoryIdenticalStructureStrategy(
                        upcomingSourceDoc.body(), transMemorySourceRootElement,
                        tmTarget);

        if (!strategy.canUse()) {
            // fallback to the other strategy
            strategy =
                    new TransMemoryNonIdenticalStructureStrategy(
                            upcomingSourceDoc, transMemorySourceRootElement,
                            tmTarget);
        }
    }



    private static void logContext(HTextFlow upcomingSource, HTextFlow transMemory,
            HLocale targetLocale) {
        if (log.isDebugEnabled()) {
            log.debug("about to match \nupcoming source: \n * {} to \nTM: \n * {} \n-> {}",
                    upcomingSource.getContents(), transMemory.getContents(),
                    transMemory.getTargets().get(targetLocale.getId())
                            .getContents());
        }
    }



    public double calculateSimilarityPercent() {
        double similarity =
                LevenshteinTokenUtil.getSimilarity(
                        upcomingSourceTextOnly,
                        transMemorySourceRootElement.text());
        double similarityPercent = similarity * 100;
        if (similarityPercent < 99.99) {
            // TODO pahuang here we could still try to put in reasonable effort (i.e. try to match as much text token as possible)
            return similarityPercent;
        } else if (strategy.canUse()) {
            // only return 100 if we can fully reuse TM.
            return 100;
        } else {
            // text only matches 100% but we can not fully reuse TM translation
            return 99;
        }

    }


    public String translationFromTransMemory() {
        Preconditions.checkState(strategy.canUse(), "do not know how to apply translation memory to this source! Similarity must be 100%.");
        return strategy.translationFromTransMemory();
    }



}
