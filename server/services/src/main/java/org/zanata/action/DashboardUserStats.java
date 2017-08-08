/*
 *
 *  * Copyright 2014, Red Hat, Inc. and individual contributors as indicated by the
 *  * @author tags. See the copyright.txt file in the distribution for a full
 *  * listing of individual contributors.
 *  *
 *  * This is free software; you can redistribute it and/or modify it under the
 *  * terms of the GNU Lesser General Public License as published by the Free
 *  * Software Foundation; either version 2.1 of the License, or (at your option)
 *  * any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful, but WITHOUT
 *  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  * details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public License
 *  * along with this software; if not, write to the Free Software Foundation,
 *  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  * site: http://www.fsf.org.
 */
package org.zanata.action;

import java.io.Serializable;

/**
 * @author Alex Eng <a href="mailto:aeng@redhat.com">aeng@redhat.com</a>
 */
public class DashboardUserStats implements Serializable {
    private int wordsTranslated;
    private int messagesTranslated;
    private int documentsTranslated;
    private int wordsReviewed;
    private int messagesReviewed;
    private int documentsReviewed;

    public int getWordsTranslated() {
        return this.wordsTranslated;
    }

    public int getMessagesTranslated() {
        return this.messagesTranslated;
    }

    public int getDocumentsTranslated() {
        return this.documentsTranslated;
    }

    public int getWordsReviewed() {
        return this.wordsReviewed;
    }

    public int getMessagesReviewed() {
        return this.messagesReviewed;
    }

    public int getDocumentsReviewed() {
        return this.documentsReviewed;
    }

    public void setWordsTranslated(final int wordsTranslated) {
        this.wordsTranslated = wordsTranslated;
    }

    public void setMessagesTranslated(final int messagesTranslated) {
        this.messagesTranslated = messagesTranslated;
    }

    public void setDocumentsTranslated(final int documentsTranslated) {
        this.documentsTranslated = documentsTranslated;
    }

    public void setWordsReviewed(final int wordsReviewed) {
        this.wordsReviewed = wordsReviewed;
    }

    public void setMessagesReviewed(final int messagesReviewed) {
        this.messagesReviewed = messagesReviewed;
    }

    public void setDocumentsReviewed(final int documentsReviewed) {
        this.documentsReviewed = documentsReviewed;
    }

    @java.beans.ConstructorProperties({ "wordsTranslated", "messagesTranslated",
            "documentsTranslated", "wordsReviewed", "messagesReviewed",
            "documentsReviewed" })
    public DashboardUserStats(final int wordsTranslated,
            final int messagesTranslated, final int documentsTranslated,
            final int wordsReviewed, final int messagesReviewed,
            final int documentsReviewed) {
        this.wordsTranslated = wordsTranslated;
        this.messagesTranslated = messagesTranslated;
        this.documentsTranslated = documentsTranslated;
        this.wordsReviewed = wordsReviewed;
        this.messagesReviewed = messagesReviewed;
        this.documentsReviewed = documentsReviewed;
    }

    public DashboardUserStats() {
    }
}
