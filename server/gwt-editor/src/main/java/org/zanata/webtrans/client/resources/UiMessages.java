/*
 * Copyright 2010, Red Hat, Inc. and individual contributors as indicated by the
 * @author tags. See the copyright.txt file in the distribution for a full
 * listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.zanata.webtrans.client.resources;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface UiMessages extends Messages {
    @DefaultMessage("Clear")
    String clearButtonLabel();

    @DefaultMessage("Search")
    String searchButtonLabel();

    @DefaultMessage("Similarity")
    String similarityLabel();

    @DefaultMessage("Details")
    String detailsLabel();

    @DefaultMessage("Origin")
    String originLabel();

    @DefaultMessage("Number of times translation has been used")
    String matchCountHeaderTooltip();

    @DefaultMessage("This translation has been used {0} times")
    @AlternateMessage({ "one", "This translation has been used once" })
    String matchCountTooltip(@PluralCount int matchCount);

    @DefaultMessage("Source")
    String sourceLabel();

    @DefaultMessage("Target")
    String targetLabel();

    @DefaultMessage("Source Term")
    String srcTermLabel();

    @DefaultMessage("Translation")
    String targetTermLabel();

    @DefaultMessage("Project glossary")
    String glossaryProjectTypeTitle();

    @DefaultMessage("Glossary")
    String glossaryGlobalTypeTitle();

    @DefaultMessage("Translation Memory")
    String translationMemoryHeading();

    @DefaultMessage("Translation Memory Details")
    String translationMemoryDetails();

    @DefaultMessage("Glossary")
    String glossaryHeading();

    @DefaultMessage("Entry #{0}")
    String entriesLabel(int count);

    @DefaultMessage("Source Term [{0}]:")
    String glossarySourceTermLabel(String locale);

    @DefaultMessage("Translation [{0}]:")
    String glossaryTargetTermLabel(String locale);

    @DefaultMessage("-none-")
    String noGlossaryTranslationLabel();

    @DefaultMessage("Send")
    String sendLabel();

    @DefaultMessage("Processing")
    String processing();

    @DefaultMessage("TM merge")
    String mergeTMButtonLabel();

    @DefaultMessage("Merge translation from Translation Memory for untranslated text flows in this document")
            String mergeTMTooltip();

    @DefaultMessage("Select TM match percentage to pre-fill translations. All the conditions will be checked to determine final state. This will apply to the entire document")
            String mergeTMHeading();

    @DefaultMessage("Auto-fill Translation Memory")
    String mergeTMCaption();

    @DefaultMessage("No text can be TM merged")
    String noTranslationToMerge();

    @DefaultMessage("TM merge failed")
    String mergeTMFailed();

    @DefaultMessage("Cancel TM merge failed")
    String mergeTMCancelFailed();

    @DefaultMessage("TM merge success on {0} rows")
    String mergeTMSuccess(long count);

    @DefaultMessage("TM merge for document {0} triggered by {1} has finished {2} - {3}")
    String mergeTMFinished(String document, String triggerBy, String startTime, String endTime);

    @DefaultMessage("Proceed to auto-fill")
    String mergeTMConfirm();

    @DefaultMessage("Cancel")
    String mergeTMCancel();

    @DefaultMessage("TM merge started...")
    String mergeTMStarted();

    @DefaultMessage("Someone has started TM merge for document {0}. Please wait for it to finish before working on translation")
    String mergeTMStartedBySomeone(String document);

    @DefaultMessage("Someone has started TM merge for document: {0}")
    String mergeTMStartedBySomeoneForDoc(String document);

    @DefaultMessage("{0}% has been processed")
    String mergeProgressPercentage(String percentDisplay);


    @DefaultMessage("Match percentage threshold")
    String matchThreshold();

    @DefaultMessage("Different Content:")
    String differentContent();

    @DefaultMessage("Different Project:")
    String differentProjectSlug();

    @DefaultMessage("Copy as Fuzzy")
    String copyAsFuzzy();

    @DefaultMessage("Copy as Translated")
    String copyAsTranslated();

    @DefaultMessage("Different Document Id (Document name and path):")
    String differentDocument();

    @DefaultMessage("Different Context (resId, msgctxt):")
    String differentContext();

    @DefaultMessage("Match from Imported Translation Memory:")
    String importedMatch();

    @DefaultMessage("If none of the above conditions apply")
    String otherwise();


    @DefaultMessage("Only possible when TM is from same project but different version, has same content, context and within same document")
    String otherwiseTooltip();

    @DefaultMessage("Condition")
    String condition();

    @DefaultMessage("Action")
    String action();

    @DefaultMessage("Result")
    String result();

    @DefaultMessage("100% (Identical)")
    String identical();

    @DefaultMessage("Copy")
    String copy();

    @DefaultMessage("Copy text and paste into editor")
    String copyTooltip();

    @DefaultMessage("Glossary Details")
    String glossaryDetails();

    @DefaultMessage("Last modified on {0}")
    String lastModifiedOn(String date);

    @DefaultMessage("Last modified on {0} by {1}")
    String lastModified(String date, String by);

    @DefaultMessage("Searching...")
    String searching();

    @DefaultMessage("No glossary results found")
    String foundNoGlossaryResults();

    @DefaultMessage("No translation memory results found")
    String foundNoTMResults();

    @DefaultMessage("Glossary save failed")
    String saveGlossaryFailed();

    @DefaultMessage("#")
    String hash();

    @DefaultMessage("Color legend")
    String colorLegend();

    @DefaultMessage("TM Diff Highlighting")
    String tmDiffHighlighting();

    @DefaultMessage("TM Highlighting")
    String tmHighlighting();

    @DefaultMessage("Click to see available filter terms (separate by space)")
    String filterMesssagesByTerm();

    @DefaultMessage("Show as Diff")
    String diffModeAsDiff();

    @DefaultMessage("Highlight Diff")
    String diffModeAsHighlight();

    @DefaultMessage("Translation that contained validation warning or error.")
    String invalidTooltip();

    @DefaultMessage("Show reference translations from")
    String changeSourceLangDescription();

    @DefaultMessage("No reference found")
    String noReferenceFoundText();

    @DefaultMessage("In")
    String inLocale();

    @DefaultMessage("None")
    String chooseRefLang();

    @DefaultMessage("No content")
    String noContent();

    @DefaultMessage("Matching")
    String matching();

    @DefaultMessage("Not Matching")
    String notMatching();
}
