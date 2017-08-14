package org.zanata.webtrans.client.resources;

import com.google.gwt.i18n.client.LocalizableResource.DefaultLocale;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.Messages;

@DefaultLocale
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat")
public interface WebTransMessages extends Messages {
    @DefaultMessage("Save")
    String save();

    @DefaultMessage("OK")
    String ok();

    @DefaultMessage("Cancel")
    String cancel();

    @DefaultMessage("(No Content)")
    String noContent();

    @DefaultMessage("{0}% ({1}hrs) {2}")
    String statusBarPercentageHrs(String approved, String remainingHours,
            String by);

    @DefaultMessage("{0}")
    String statusBarLabelHours(String remainingHours);

    @DefaultMessage("{0} to {1} - Zanata Web Translation")
    String windowTitle(String workspaceName, String localeName);

    @DefaultMessage("{0} to {1} - {2}")
    String windowTitle2(String workspaceName, String localeName, String title);

    @DefaultMessage("First Page")
    String firstPage();

    @DefaultMessage("Previous Page")
    String prevPage();

    @DefaultMessage("Next Page")
    String nextPage();

    @DefaultMessage("Last Page")
    String lastPage();

    @DefaultMessage("Show Translation Suggestions")
    String showTranslationMemoryPanel();

    @DefaultMessage("Show Glossary Suggestions")
    String showGlossaryPanel();

    @DefaultMessage("Chat room")
    String chatRoom();

    @DefaultMessage("Hide Translation Memory and Glossary")
    String hideSouthPanel();

    @DefaultMessage("Restore Translation Memory and Glossary")
    String restoreSouthPanel();

    @DefaultMessage("Load")
    String load();

    @DefaultMessage("Restore Defaults")
    String restoreDefaults();

    @DefaultMessage("Loading")
    String loading();

    @DefaultMessage("Path")
    String columnHeaderPath();

    @DefaultMessage("Document")
    String columnHeaderDocument();

    @DefaultMessage("Statistic")
    String columnHeaderStatistic();

    @DefaultMessage("Complete")
    String columnHeaderComplete();

    @DefaultMessage("Incomplete")
    String columnHeaderIncomplete();

    @DefaultMessage("Remaining hours")
    String columnHeaderRemaining();

    @DefaultMessage("Last Upload")
    String columnHeaderLastUpload();

    @DefaultMessage("Last Translated")
    String columnHeaderLastTranslated();

    @DefaultMessage("Actions")
    String columnHeaderAction();

    @DefaultMessage("No document selected")
    String noDocumentSelected();

    @DefaultMessage("Project-wide Search & Replace")
    String projectWideSearchAndReplace();

    @DefaultMessage("Document List")
    String documentListTitle();

    @DefaultMessage("{0} has quit workspace")
    String hasQuitWorkspace(String user);

    @DefaultMessage("{0} has joined workspace")
    String hasJoinedWorkspace(String user);

    @DefaultMessage("Searching")
    String searching();

    @DefaultMessage("Showing results for search \"{0}\" ({1} text flows in {2} documents)")
    // @formatter:off
    @AlternateMessage({
            "one|one", "Showing results for search \"{0}\" (1 text flow in 1 document)",
            "other|one", "Showing results for search \"{0}\" ({1} text flows in 1 document)"
    })
    // @formatter:on
            String showingResultsForProjectWideSearch(String searchString,
                    @PluralCount int textFlows, @PluralCount int documents);

    @DefaultMessage("Search \"{0}\" returned no results")
    String searchForPhraseReturnedNoResults(String searchString);

    @DefaultMessage("There are no search results to display")
    String noSearchResults();

    @DefaultMessage("Search failed")
    String searchFailed();

    @DefaultMessage("Preview")
    String fetchPreview();

    @DefaultMessage("Hide preview")
    String hidePreview();

    @DefaultMessage("Previewing")
    String fetchingPreview();

    @DefaultMessage("Fetched preview")
    String fetchedPreview();

    @DefaultMessage("Failed to fetch preview")
    String previewFailed();

    @DefaultMessage("Select all found occurrences of searched term (all pages)")
    String selectAllDescription();

    @DefaultMessage("Replace")
    String replace();

    @DefaultMessage("Replace text in all selected text flows.")
    String replaceSelectedDescription();

    @DefaultMessage("Select text flows and view preview to enable replace.")
    String replaceSelectedDisabledDescription();

    @DefaultMessage("Preview is required before replacing text")
    String previewRequiredBeforeReplace();

    @DefaultMessage("Replacing")
    String replacing();

    @DefaultMessage("Replaced")
    String replaced();

    @DefaultMessage("Replaced \"{0}\" with \"{1}\" in {2} row {3} (\"{4}...\")")
            String replacedTextInOneTextFlow(String searchText,
                    String replacement, String docName, int oneBasedRowIndex,
                    String truncatedText);

    @DefaultMessage("Replaced \"{0}\" with \"{1}\" in {2} text flows")
    @AlternateMessage({ "one", "Replaced \"{0}\" with \"{1}\" in 1 text flow" })
            String replacedTextInMultipleTextFlows(String searchText,
                    String replacement, @PluralCount int numFlows);

    @DefaultMessage("Replace text failed in text flow {0}, error message: {1}")
    String replaceTextFailureWithMessage(String id, String errorMessage);

    @DefaultMessage("Replace text failed")
    String replaceTextFailure();

    @DefaultMessage("No replacement text has been entered")
    String noReplacementPhraseEntered();

    @DefaultMessage("No replacements to make")
    String noReplacementsToMake();

    @DefaultMessage("You have no access to modify translations")
    String youAreNotAllowedToModifyTranslations();

    @DefaultMessage("View in editor")
    String viewDocInEditor();

    @DefaultMessage("Show this document in editor view")
    String viewDocInEditorDetailed();

    @DefaultMessage("Search document in editor")
    String searchDocInEditor();

    @DefaultMessage("Show this document in the editor with the current search active")
            String searchDocInEditorDetailed();

    @DefaultMessage("Index")
    String rowIndex();

    @DefaultMessage("Source")
    String source();

    @DefaultMessage("Target")
    String target();

    @DefaultMessage("Actions")
    String actions();

    @DefaultMessage("Undo")
    String undo();

    @DefaultMessage("Undoing")
    String undoInProgress();

    @DefaultMessage("Undo replacement failed")
    String undoReplacementFailure();

    @DefaultMessage("Undo successful")
    String undoSuccess();

    @Description("Message for unsuccessful undo")
    @DefaultMessage("{0} items can not be undone. {1} items are reverted")
    @AlternateMessage({
        // @formatter:off
        "one|=0", "Item can not be undone",
        "other|=0", "Items can not be undone"
        // @formatter:on
            })
    String undoUnsuccessful(@PluralCount int unsuccessfulCount,
        @PluralCount int successfulCount);

    @DefaultMessage("Undo failed")
    String undoFailure();

    @DefaultMessage("Undo not possible in read-only workspace")
    String cannotUndoInReadOnlyMode();

    @DefaultMessage("No text flows selected")
    String noTextFlowsSelected();

    @DefaultMessage("Notification")
    String notification();

    @DefaultMessage("Available Keyboard Shortcuts")
    String availableKeyShortcutsTitle();

    @DefaultMessage("Select text flows in all documents")
    String selectAllTextFlowsKeyShortcut();

    @DefaultMessage("Focus search phrase")
    String focusSearchPhraseKeyShortcut();

    @DefaultMessage("Focus replacement phrase")
    String focusReplacementPhraseKeyShortcut();

    @DefaultMessage("Show available shortcuts")
    String showAvailableKeyShortcuts();

    @DefaultMessage("Application")
    String applicationScope();

    @DefaultMessage("Editing text")
    String editScope();

    @DefaultMessage("Editor navigation")
    String navigationScope();

    @DefaultMessage("Translation memory")
    String tmScope();

    @DefaultMessage("Glossary")
    String glossaryScope();

    @DefaultMessage("Toggle individual row action buttons (changes for next search)")
            String toggleRowActionButtons();

    @DefaultMessage("Replace text in selected rows")
    String replaceSelectedKeyShortcut();

    @DefaultMessage("Show document list")
    String showDocumentListKeyShortcut();

    @DefaultMessage("Show editor view")
    String showEditorKeyShortcut();

    @DefaultMessage("Show project-wide search view")
    String showProjectWideSearch();

    @DefaultMessage("This is a public channel")
    String thisIsAPublicChannel();

    @DefaultMessage("Only show documents that contain the search text with matching case")
            String docListFilterCaseSensitiveDescription();

    @DefaultMessage("Only show documents with full path and name in the search text")
            String docListFilterExactMatchDescription();

    @DefaultMessage("Enter text to filter the document list. Use commas (,) to separate multiple searches")
            String docListFilterDescription();

    @DefaultMessage("Disable 'Replace' button until previews have been generated for all selected text flows")
            String requirePreviewDescription();

    @DefaultMessage("Navigate to next row")
    String navigateToNextRow();

    @DefaultMessage("Navigate to previous row")
    String navigateToPreviousRow();

    @DefaultMessage("Open editor in selected row")
    String openEditorInSelectedRow();

    @DefaultMessage("Close keyboard shortcuts list")
    String closeShortcutView();

    @DefaultMessage("Search translation memory")
    String searchTM();

    @DefaultMessage("Search glossary")
    String searchGlossary();

    @DefaultMessage("Chat")
    String chatScope();

    @DefaultMessage("Publish chat content")
    String publishChatContent();

    @DefaultMessage("Translation History")
    String translationHistory();

    @Description("Tab text for translation history comparison")
    @DefaultMessage("Compare ver. {0} and {1}")
    String translationHistoryComparison(String versionOne, String versionTwo);

    @DefaultMessage("Select 2 entries to compare")
    String translationHistoryComparisonTitle();

    @Description("latest version in translation history")
    @DefaultMessage("Latest")
    String latest();

    @Description("current unsaved value in editor for translation history display")
    @DefaultMessage("Unsaved")
            String unsaved();

    @DefaultMessage("Flip entries")
    String flipComparingEntries();

    @DefaultMessage("Workspace is set to read only")
    String notifyReadOnlyWorkspace();

    @DefaultMessage("Workspace is set to edit mode")
    String notifyEditableWorkspace();

    @DefaultMessage("Close")
    String close();

    @DefaultMessage("Options")
    String options();

    @DefaultMessage("Editor options")
    String editorOptions();

    @DefaultMessage("Validation options")
    String validationOptions();

    @DefaultMessage("Run validation")
    String runValidation();

    @DefaultMessage("Navigation key/button")
    String navOption();

    @DefaultMessage("Page size")
    String pageSize();

    @DefaultMessage("This project-version is readonly. It cannot be edited.")
    String readOnlyTooltip();

    @DefaultMessage("This project-version is archived. It cannot be edited.")
    String obsoleteTooltip();

    @DefaultMessage("Advanced user configuration")
    String otherConfiguration();

    @DefaultMessage("When unexpected error happens, a popup window will display and show it")
            String showErrorsTooltip();

    @DefaultMessage("Show System Errors")
    String showSystemErrors();

    @DefaultMessage("Show warning when 'Save as Approved' triggered via keyboard shortcut")
            String showSaveApprovedWarningTooltip();

    @DefaultMessage("Go to this entry")
    String goToThisEntry();

    @DefaultMessage("Style")
    String style();

    @DefaultMessage("Description")
    String description();

    @DefaultMessage("Search only")
    String searchOnly();

    /**
     * Used for color legend in search and replace view
     * The label represent the color for the searched text
     */
    @DefaultMessage("Blue color")
    String searchColor();

    @DefaultMessage("Non-matching text")
    String noMatch();

    @DefaultMessage("Matching")
    String matching();

    /**
     * Used for color legend in search and replace view
     * The label represent the color for no changes text
     */
    @DefaultMessage("Yellow color")
    String highlightColor();

    @DefaultMessage("TM only")
    String tmOnly();

    /**
     * Used for color legend in search and replace view
     * The label represent the color for text to be replaced
     */
    @DefaultMessage("Red color")
    String tmColor();

    @DefaultMessage("Text contained in search term but not in TM")
    String tmInsertTagDesc();

    @DefaultMessage("Text contained in TM but not in search term")
    String tmDelTagDesc();

    @DefaultMessage("Text contained in both search term and TM")
    String tmHighlightTextDesc();

    @DefaultMessage("New replacement text")
    String searchReplaceInsertTagDesc();

    @DefaultMessage("Old text to be replaced")
    String searchReplaceDelTagDesc();

    @DefaultMessage("No changes")
    String searchReplacePlainTextDesc();

    @DefaultMessage("Color legend")
    String colorLegend();

    @DefaultMessage("Concurrent edit detected. Reset value for current row.")
    String concurrentEdit();

    @DefaultMessage("Other user has saved a newer version (Latest) while you are editing (Unsaved). Please resolve conflicts.")
            String concurrentEditTitle();

    @DefaultMessage("Editor")
    String editor();

    @DefaultMessage("Words")
    String byWords();

    @DefaultMessage("Messages")
    String byMessage();

    @DefaultMessage("Refresh current page")
    String refreshCurrentPage();

    @DefaultMessage("Translation Memory (TM) options")
    String transMemoryOption();

    @DefaultMessage("Display configuration")
    String displayConfiguration();

    @DefaultMessage("Configure how you want your editor to look like")
    String displayConfigurationTooltip();

    @DefaultMessage("Display optional Trans Unit Details")
    String showTransUnitDetails();

    @DefaultMessage("Only display Translation Unit Details when there is meta data otherwise hide it")
            String showTransUnitDetailsTooltip();

    @DefaultMessage("Download All Files")
    String downloadAllFiles();

    @DefaultMessage("Your download will be prepared and may take a few minutes to complete. Is this ok?")
            String prepareDownloadConfirmation();

    @DefaultMessage("Download files (zip)")
    String downloadAllAsZip();

    @DefaultMessage("Download all translated files.")
    String downloadAllAsZipDescription();

    @DefaultMessage("Download files (offline po zip)")
    String downloadAllAsOfflinePoZip();

    @DefaultMessage("Download all translated files in po format for offline translation.")
            String downloadAllAsOfflinePoZipDescription();

    @DefaultMessage("The project type for this iteration has not been set. Contact the project maintainer.")
            String projectTypeNotSet();

    @DefaultMessage("Run validation on documents in this page")
    String documentValidationTitle();

    @DefaultMessage("Has validation errors - {0}")
    String hasValidationErrors(String docName);

    @DefaultMessage("Last run: {0}")
    String lastValidationRun(String completeTime);

    @DefaultMessage("Download document with extension {0}")
    String downloadFileTitle(String key);

    @DefaultMessage("You")
    String you();

    @DefaultMessage("Compare")
    String compare();

    @DefaultMessage("Remove from comparison")
    String removeFromComparison();

    @DefaultMessage("Why is this translation rejected?")
    String rejectCommentTitle();

    @DefaultMessage("Confirm rejection (ctrl + enter)")
    String confirmRejection();

    @DefaultMessage("More details")
    String moreDetais();

    @DefaultMessage("Editor Buttons")
    String editorButtons();

    @DefaultMessage("'Enter' key saves immediately")
    String enterKeySaves();

    @DefaultMessage("Use syntax highlighting Editor (Warning: no spell check, long lines may have some wrapping issues)")
    String useSyntaxHighlight();

    @DefaultMessage("Show 'Save as Approved' warning")
    String showSaveApproveWarning();

    @DefaultMessage("Cancel: Esc")
    String attentionKeyCancelLabel();

    @DefaultMessage("Copy Source: G")
    String attentionKeyCopySourceLabel();

    @DefaultMessage("Toggle Syntax Highlighting: H")
    String attentionKeyToggleSyntaxHighlightingLabel();

    @DefaultMessage("Try the new alpha editor")
    String newEditorMessage();

    @DefaultMessage("Anonymous")
    String anonymousUser();

    @DefaultMessage("Project ID or version ID has changed. Please copy your unsaved work into clipboard. Editor will reload in 10 seconds.")
    String workspaceUrlHasChanged();

    @DefaultMessage("Loaded user options")
    String loadedUserOptions();

    @DefaultMessage("Restore to default options")
    String restoreToDefaultOptions();

    @DefaultMessage("Saved user options")
    String savedOptions();


}
