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
package org.zanata.rest.editor.dto.suggestion;

import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single suggested translation.
 *
 * This could be from translation memory or other sources.
 *
 * This representation is designed for use with the pure JavaScript editor.
 */
@JsonPropertyOrder({ "relevanceScore", "similarityPercent", "sourceContents",
        "targetContents", "matchDetails" })
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Suggestion implements Serializable {
    private final double relevanceScore;
    private final double similarityPercent;
    private final List<String> sourceContents;
    private final List<String> targetContents;
    private final List<SuggestionDetail> matchDetails;

    public Suggestion(double relevanceScore, double similarityPercent,
            List<String> sourceContents, List<String> targetContents) {
        this.relevanceScore = relevanceScore;
        this.similarityPercent = similarityPercent;
        this.sourceContents = sourceContents;
        this.targetContents = targetContents;
        this.matchDetails = new ArrayList<>();
    }

    public double getRelevanceScore() {
        return this.relevanceScore;
    }

    public double getSimilarityPercent() {
        return this.similarityPercent;
    }

    public List<String> getSourceContents() {
        return this.sourceContents;
    }

    public List<String> getTargetContents() {
        return this.targetContents;
    }

    public List<SuggestionDetail> getMatchDetails() {
        return this.matchDetails;
    }
}
