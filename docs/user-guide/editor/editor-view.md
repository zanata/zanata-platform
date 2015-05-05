<figure>
<img alt="Editor view" src="images/editor-overview.png" />
</figure>
<br/>

### 1. Breadcrumbs

Links to Project -> Version -> [Documents view](user-guide/editor/documents-view) -> Document name. Each link will redirect to the respective page.


### 2. Statistic

Statistic and remaining hours of the current document translation document.

### 3. Views

See [Views](user-guide/editor/overview#views-in-webtrans) for more information.

### 4. Search and filter 

Allow translators to search text, and filter by translation status. 
Translations can be in one of several states that indicate where they are in the translation workflow. The states are:

Empty: no translation has been entered.
Fuzzy: a translation has been entered, but is not considered ready for use.
Translated: a translation has been entered and the translator considers it ready for use.
and only in projects that have review turned on:

Approved: a translation has been entered and a reviewer considers it ready for use.
Rejected: a translation has been entered, but a reviewer does not consider it ready for use.
You can show or hide translations with any of these states using the filter settings above the editor table. Just check the 'Incomplete' or 'Complete' category, or check individual states within each category to show translations of interest (e.g. showing only Rejected translations to change them after review).

In addition to filtering by states, you can check the 'Invalid' checkbox to only show translations that match one of the selected states and have one or more validation warnings.

### 5. Source string

Source strings show the original text for which a translation is needed. The text itself is displayed within a text box, and cannot be changed. In addition to the source text, some other useful information is shown.

Line numbers are shown to the left of the source and translation text boxes. Long lines will be displayed over several lines in the text box, but will only have a single line number.

Whitespace characters are shown in source and translation text to allow accurate translation. Spaces are displayed with a faint grey underline, newlines are shown with a faint grey pilcrow character (¶) and tabs are shown with a faint grey right-arrow-to-bar character (⇥).

The string number within the document is shown below the source string for the currently selected line, along with the source comment if one exists. Clicking the string number causes some extra information about the string to be displayed.

To the left of the string number is a bookmark icon. Clicking the bookmark icon updates the URL so that you can create a link directly to this particular string.

### 6. Translation validation

As you type, validations will run on the text you have entered. If any validations fail, warnings will be displayed here. Warnings are cleared when the text no longer fails the validation.

### 7. Translation actions

From left to right: Save as translated, Save as fuzzy, Cancel changes, Translation history, undo translation.

### 8. Editor settings

From top to bottom: Notification, Chat room, Settings, Validation options.

### 9. Paging

Page navigation of the document.

### 10. Translation memory

The Translation Memory (TM) searches for translations of strings the same as or similar to the currently selected source string. The search looks across all projects in Zanata for the most similar strings that have a Translated or Approved translation in the correct language.

Matches can be copied to the translation text box and used as-is or modified before saving. To copy TM matches to the selected text box, click the `Copy` button next to the match, or use `Ctrl+Alt+1` to `Ctrl+Alt+4` keyboard shortcuts to copy the first to fourth match in the list.

You can also search the TM for other phrases by entering them in the TM text box and clicking `Search`.

### 11. Glossary

If a glossary has been uploaded for your language, each word in the currently selected row will be searched for a glossary entry.


## Readonly editor

If you're not part of that language team, the editor will open as readonly mode.
You will be restricted to only viewing source and translations in the editor.

<figure>
<img alt="Readonly editor" src="images/editor-readonly-indicator.png" />
</figure>
<br/>

## Settings

<figure>
<img alt="Editor settings" src="images/editor-settings.png" />
</figure>
<br/>

* **Editor Buttons** - Show/Hide editor buttons.
* **Enter key saves immediately** - Save translation when user hit `Enter` key.
* **Use syntax highlighting Editor** - Enable syntax highlighting in editor. (Warning: no spell check, long lines may have some wrapping issues)
* **Show `Save as Approved` warning** - Show warning when Save as Approved triggered via keyboard shortcut
* **Page size** - Allow user to customise documents to show per page.
* **Show Translation Suggestion** - Show/hide Translation memory panel
* **Show Glossary Suggestion** - Show/hide glossary panel
* **Layout** - Customise the view of document list with `Compact`, `Default` and `Loose`.
* **Show System Errors** - Shows detailed messages in new windows when errors occurs.


## Validations

<figure>
<img alt="Editor validation settings" src="images/editor-validations.png" />
</figure>
<br/>

Translators can enable or disable validation check on their translations except for those which is enforced by project maintainers.
See [Project settings](user-guide/project/project-settings#validations) for more information.

See [validations](user-guide/projects/validations) all available check.
    