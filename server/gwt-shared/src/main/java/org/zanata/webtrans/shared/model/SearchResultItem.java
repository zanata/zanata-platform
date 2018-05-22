package org.zanata.webtrans.shared.model;

import com.google.gwt.user.client.rpc.IsSerializable;

public abstract class SearchResultItem implements IsSerializable {

    private double relevanceScore;
    private double similarityPercent;

    // for GWT
    protected SearchResultItem() {
    }

    protected SearchResultItem(double relevanceScore, double similarityPercent) {
        this.relevanceScore = relevanceScore;
        this.similarityPercent = similarityPercent;
    }

    /**
     * The query relevance score for the source language string corresponding to this match.
     */
    public double getRelevanceScore() {
        return relevanceScore;
    }

    /**
     * The similarity percentage for the source language string corresponding to this match.
     */
    public double getSimilarityPercent() {
        return similarityPercent;
    }

}
