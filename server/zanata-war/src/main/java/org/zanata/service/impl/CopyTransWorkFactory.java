/*
 * Copyright 2014, Red Hat, Inc. and individual contributors
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
package org.zanata.service.impl;

import static org.zanata.common.ContentState.Approved;
import static org.zanata.common.ContentState.NeedReview;
import static org.zanata.common.ContentState.New;
import static org.zanata.common.ContentState.Translated;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.DOWNGRADE_TO_FUZZY;
import static org.zanata.model.HCopyTransOptions.ConditionRuleAction.REJECT;
import static org.zanata.transaction.TransactionUtilImpl.runInTransaction;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import org.zanata.common.ContentState;
import org.zanata.dao.TextFlowTargetDAO;
import org.zanata.events.DocStatsEvent;
import org.zanata.events.DocumentLocaleKey;
import org.zanata.model.HAccount;
import org.zanata.model.HCopyTransOptions;
import org.zanata.model.HDocument;
import org.zanata.model.HLocale;
import org.zanata.model.HProjectIteration;
import org.zanata.model.HSimpleComment;
import org.zanata.model.HTextFlow;
import org.zanata.model.HTextFlowTarget;
import org.zanata.model.type.TranslationSourceType;
import org.zanata.security.annotations.Authenticated;
import org.zanata.service.ValidationService;
import org.zanata.service.VersionStateCache;
import org.zanata.util.TranslationUtil;
import org.zanata.webtrans.shared.model.ValidationAction;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * @author Sean Flanigan
 *         <a href="mailto:sflaniga@redhat.com">sflaniga@redhat.com</a>
 */
@Named("copyTransWorkFactory")
@javax.enterprise.context.Dependent
public class CopyTransWorkFactory {
    private static final org.slf4j.Logger log =
            org.slf4j.LoggerFactory.getLogger(CopyTransWorkFactory.class);

    // Inject textFlowTargetDAO (@DatabaseSearch) for Hibernate-based query
    // Inject translationMemoryServiceImpl (no qualifier) for Hibernate Search
    // query
    @Inject
    private TextFlowTargetDAO translationFinder;
    @Inject
    private TextFlowTargetDAO textFlowTargetDAO;
    @Inject
    private ValidationService validationServiceImpl;
    @Inject
    private VersionStateCache versionStateCacheImpl;
    @Inject
    @Authenticated
    private HAccount authenticatedAccount;

    public Integer runCopyTransInNewTx(HLocale targetLocale,
            HCopyTransOptions options, HDocument document,
            boolean requireTranslationReview, List<HTextFlow> copyTargets)
            throws Exception {
        return runInTransaction(() -> runCopyTrans(targetLocale, options,
                document, requireTranslationReview, copyTargets));
    }

    public Integer runCopyTrans(HLocale targetLocale, HCopyTransOptions options,
            HDocument document, boolean requireTranslationReview,
            List<HTextFlow> copyTargets) {
        int numCopied = 0;
        boolean checkContext = false;
        boolean checkProject = false;
        boolean checkDocument = false;
        // Only outright reject copies if the options say so
        if (options
                .getDocIdMismatchAction() == HCopyTransOptions.ConditionRuleAction.REJECT) {
            checkDocument = true;
        }
        if (options
                .getProjectMismatchAction() == HCopyTransOptions.ConditionRuleAction.REJECT) {
            checkProject = true;
        }
        if (options
                .getContextMismatchAction() == HCopyTransOptions.ConditionRuleAction.REJECT) {
            checkContext = true;
        }
        Long actorId = authenticatedAccount.getPerson().getId();
        for (HTextFlow textFlow : copyTargets) {
            if (shouldFindMatch(textFlow, targetLocale,
                    requireTranslationReview)) {
                Optional<HTextFlowTarget> bestMatch =
                        translationFinder.searchBestMatchTransMemory(textFlow,
                                targetLocale.getLocaleId(),
                                document.getLocale().getLocaleId(),
                                checkContext, checkDocument, checkProject);
                if (bestMatch.isPresent()) {
                    numCopied++;
                    saveCopyTransMatch(actorId, bestMatch.get(), textFlow,
                            options, requireTranslationReview);
                }
            }
        }
        return numCopied;
    }

    /**
     * Determines the content state for a translation given a list of rules and
     * their evaluation result, and the initial state that it was copied as.
     *
     * @param pairs
     *            List of evaluated rules and the match result.
     * @param initialState
     *            The initial content state of the translation that the content
     *            was copied from.
     * @return The content state that the copied translation should have. 'New'
     *         indicates that the translation should not copied.
     */
    private static ContentState determineContentStateFromMatchRules(
            List<MatchRulePair> pairs, ContentState initialState) {
        if (pairs.isEmpty()) {
            return initialState;
        }
        MatchRulePair p = pairs.get(0);
        if (shouldReject(p.getMatchResult().get(), p.getRuleAction())) {
            return New;
        } else if (shouldDowngradeToFuzzy(p.getMatchResult().get(),
                p.getRuleAction())) {
            return determineContentStateFromMatchRules(
                    pairs.subList(1, pairs.size()), NeedReview);
        } else {
            return determineContentStateFromMatchRules(
                    pairs.subList(1, pairs.size()), initialState);
        }
    }

    /**
     * Determines the content state for a translation given a list of rules and
     * their evaluation result.
     *
     * @param pairs
     *            List of evaluated rules and their result.
     * @param requireTranslationReview
     *            Whether the project to copy the translation to requires
     *            translations to be reviewed.
     * @param matchingTargetState
     *            The initial state of the matching translation (the translation
     *            that will be copied over).
     * @return The content state that the copied translation should have. 'New'
     *         indicates that the translation should not copied.
     */
    static ContentState determineContentStateFromRuleList(
            List<MatchRulePair> pairs, boolean requireTranslationReview,
            ContentState matchingTargetState) {
        assert matchingTargetState == Translated
                || matchingTargetState == Approved;
        return determineContentStateFromMatchRules(pairs,
                requireTranslationReview ? matchingTargetState : Translated);
    }

    /**
     * Determines the content state that a copied translation should have.
     *
     * @param contextMatches
     *            Indicates if there is a context match between the match and
     *            copy-target text flows.
     * @param projectMatches
     *            Indicates if there is a project match between the match and
     *            copy-target text flows.
     * @param docIdMatches
     *            Indicates if there is a doc Id match between the match and
     *            copy-target text flows.
     * @param options
     *            The copy trans options that are effective.
     * @param requireTranslationReview
     *            Whether the project to copy the translation to requires
     *            translations to be reviewed.
     * @param matchingTargetState
     *            he initial state of the matching translation (the translation
     *            that will be copied over).
     * @return The content state that the copied translation should have. 'New'
     *         indicates that the translation should not copied.
     */
    static ContentState determineContentState(Supplier<Boolean> contextMatches,
            Supplier<Boolean> projectMatches, Supplier<Boolean> docIdMatches,
            HCopyTransOptions options, boolean requireTranslationReview,
            ContentState matchingTargetState) {
        List<MatchRulePair> rules = ImmutableList.of(
                new MatchRulePair(contextMatches,
                        options.getContextMismatchAction()),
                new MatchRulePair(projectMatches,
                        options.getProjectMismatchAction()),
                new MatchRulePair(docIdMatches,
                        options.getDocIdMismatchAction()));
        return determineContentStateFromRuleList(rules,
                requireTranslationReview, matchingTargetState);
    }

    private void saveCopyTransMatch(Long actorId,
            final HTextFlowTarget matchingTarget, final HTextFlow originalTf,
            final HCopyTransOptions options,
            final boolean requireTranslationReview) {
        final HProjectIteration matchingTargetProjectIteration = matchingTarget
                .getTextFlow().getDocument().getProjectIteration();
        // lazy evaluation of some conditions
        Supplier<Boolean> contextMatches = new Supplier<Boolean>() {

            @Override
            public Boolean get() {
                return originalTf.getResId()
                        .equals(matchingTarget.getTextFlow().getResId());
            }
        };
        Supplier<Boolean> projectMatches = new Supplier<Boolean>() {

            public Boolean get() {
                return originalTf.getDocument().getProjectIteration()
                        .getProject().getId()
                        .equals(matchingTargetProjectIteration.getProject()
                                .getId());
            }
        };
        Supplier<Boolean> docIdMatches = new Supplier<Boolean>() {

            @Override
            public Boolean get() {
                return originalTf.getDocument().getDocId().equals(
                        matchingTarget.getTextFlow().getDocument().getDocId());
            }
        };
        final ContentState copyState = determineContentState(contextMatches,
                projectMatches, docIdMatches, options, requireTranslationReview,
                matchingTarget.getState());
        boolean hasValidationError = validationTranslations(copyState,
                matchingTargetProjectIteration, originalTf.getContents(),
                matchingTarget.getContents());
        if (hasValidationError) {
            return;
        }
        HTextFlowTarget hTarget = textFlowTargetDAO
                .getOrCreateTarget(originalTf, matchingTarget.getLocale());
        ContentState prevState =
                hTarget.getId() == null ? ContentState.New : hTarget.getState();
        if (shouldOverwrite(hTarget, copyState)) {
            // NB we don't touch creationDate
            hTarget.setTextFlowRevision(originalTf.getRevision());
            hTarget.setLastChanged(matchingTarget.getLastChanged());
            hTarget.setLastModifiedBy(matchingTarget.getLastModifiedBy());
            hTarget.setTranslator(matchingTarget.getTranslator());
            // TODO rhbz953734 - will need a new copyTran option for
            // review state
            if (copyState == ContentState.Approved) {
                hTarget.setReviewer(matchingTarget.getReviewer());
            }
            hTarget.setContents(matchingTarget.getContents());
            hTarget.setState(copyState);
            if (matchingTarget.getComment() == null) {
                hTarget.setComment(null);
            } else {
                HSimpleComment hComment = hTarget.getComment();
                if (hComment == null) {
                    hComment = new HSimpleComment();
                    hTarget.setComment(hComment);
                }
                hComment.setComment(matchingTarget.getComment().getComment());
            }
            hTarget.setRevisionComment(
                    TranslationUtil.getCopyTransMessage(matchingTarget));
            hTarget.setSourceType(TranslationSourceType.COPY_TRANS);
            TranslationUtil.copyEntity(matchingTarget, hTarget);
            // TODO Maybe we should think about registering a Hibernate
            // integrator for these updates
            signalCopiedTranslation(hTarget, prevState,
                    originalTf.getWordCount());
        }
    }

    /**
     * Indicates if a copied translation should be downgraded to fuzzy/
     *
     * @param match
     *            The result of a match evaluating condition.
     * @param action
     *            The selected action to take based on the result of the
     *            condition evaluation.
     * @return True, if the translation should be downgraded to fuzzy based on
     *         the evaluated condition.
     */
    static boolean shouldDowngradeToFuzzy(boolean match,
            HCopyTransOptions.ConditionRuleAction action) {
        return !match && action == DOWNGRADE_TO_FUZZY;
    }

    /**
     * Indicates if a given text flow should have a match found for a given
     * target locale, or if it is already good enough.
     */
    private boolean shouldFindMatch(HTextFlow textFlow, HLocale locale,
            boolean requireTranslationReview) {
        // TODO getTargets will fill up Hibernate cache for large textflows
        // and locales. Check which one is more efficient
        HTextFlowTarget targetForLocale =
                textFlow.getTargets().get(locale.getId());

        // HTextFlowTarget targetForLocale =
        // textFlowTargetDAO.getTextFlowTarget(
        // textFlow, locale);
        if (targetForLocale == null
                || targetForLocale.getState() == ContentState.NeedReview) {
            return true;
        } else if (requireTranslationReview
                && targetForLocale.getState() != ContentState.Approved) {
            return true;
        } else if (!requireTranslationReview
                && targetForLocale.getState() != ContentState.Translated) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Indicates if a copied translation should be rejected.
     *
     * @param match
     *            The result of a match evaluating condition.
     * @param action
     *            The selected action to take based on the result of the
     *            condition evaluation.
     * @return True, if the translation should be outright rejected based on the
     *         evaluated condition.
     */
    static boolean shouldReject(boolean match,
            HCopyTransOptions.ConditionRuleAction action) {
        return !match && action == REJECT;
    }

    /**
     * Indicates if a Copy Trans found match should overwrite the currently
     * stored one based on their states.
     */
    private static boolean shouldOverwrite(HTextFlowTarget currentlyStored,
            ContentState matchState) {
        if (matchState == ContentState.New) {
            return false;
        } else if (currentlyStored != null) {
            if (currentlyStored.getState().isRejectedOrFuzzy()
                    && matchState.isTranslated()) {
                return true; // If it's fuzzy, replace only with approved ones
            } else if (currentlyStored.getState() == ContentState.Translated
                    && matchState.isApproved()) {
                return true; // If it's Translated and found an Approved one
            } else if (currentlyStored.getState() == ContentState.New) {
                return true; // If it's new, replace always
            } else {
                return false;
            }
        }
        return true;
    }

    private void signalCopiedTranslation(HTextFlowTarget target,
            ContentState previousState, Long wordCount) {
        /*
         * Using a direct method call instead of an event because it's easier to
         * read. Since these events are being called synchronously (as opposed
         * to an 'after Transaction' events), there is no big performance gain
         * and makes the code easier to read and navigate.
         */
        // TODO how was this not causing duplicate events? Is this bypassing
        // TranslationServiceImpl?
        // FIXME other observers may not be notified
        HDocument document = target.getTextFlow().getDocument();
        DocumentLocaleKey key =
                new DocumentLocaleKey(document.getId(), target.getLocaleId());
        Map<ContentState, Long> contentStates = Maps.newHashMap();
        DocStatsEvent.updateContentStateDeltas(contentStates, target.getState(),
                previousState, wordCount);
        DocStatsEvent docEvent =
                new DocStatsEvent(key, document.getProjectIteration().getId(),
                        contentStates, target.getId());
        versionStateCacheImpl.docStatsUpdated(docEvent);
    }

    /**
     * Run enforced validation check(Error) if translation is saving as
     * 'Translated'.
     *
     * @param newState
     * @param projectVersion
     * @param sources
     * @param translations
     * @return if translation has validation error
     */
    private boolean validationTranslations(ContentState newState,
            HProjectIteration projectVersion, List<String> sources,
            List<String> translations) {
        if (newState.isTranslated()) {
            List<String> validationMessages = validationServiceImpl
                    .validateWithServerRules(projectVersion, sources,
                            translations, ValidationAction.State.Error);
            if (!validationMessages.isEmpty()) {
                log.warn(validationMessages.toString());
                return true;
            }
        }
        return false;
    }

    /**
     * Holds the result of a match evaluation in the form of a boolean, and the
     * corresponding action to be taken for the result.
     */
    static final class MatchRulePair {
        private final Supplier<Boolean> matchResult;
        private final HCopyTransOptions.ConditionRuleAction ruleAction;

        @java.beans.ConstructorProperties({ "matchResult", "ruleAction" })
        public MatchRulePair(final Supplier<Boolean> matchResult,
                final HCopyTransOptions.ConditionRuleAction ruleAction) {
            this.matchResult = matchResult;
            this.ruleAction = ruleAction;
        }

        public Supplier<Boolean> getMatchResult() {
            return this.matchResult;
        }

        public HCopyTransOptions.ConditionRuleAction getRuleAction() {
            return this.ruleAction;
        }
    }
}
