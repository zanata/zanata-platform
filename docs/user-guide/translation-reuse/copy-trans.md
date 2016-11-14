# Overview

Copy Trans is a service that finds valid translations for documents by finding matching strings in other documents that are already translated.

Copy Trans can save a lot of translation effort when adding a new version of a project that has been translated in Zanata. Because many of the strings will usually be the same between versions, many translations can be safely copied from the previous version.

Copy Trans usually runs automatically when a document is uploaded using the cli-client. The Copy Trans Options for the project are used in this case. You can also run Copy Trans manually against a project version.

# Rules

Copy Trans works by trying to find a good match for every string in a document in every language.

For each string that does not yet have a translation in either a "Translated" or "Approved" state, Copy Trans searches for strings with exactly the same content that have a translation, and checks every string in every project in Zanata that has a translation in either a "Translated" or "Approved" state. When Copy Trans finds a matching string, it checks a set of conditions to determine the next step. This is repeated for each language.

*Note:* Copy Trans only considers strings that have exactly the same source content. If you want to use strings that are not exactly the same, use the Translation Memory Merge feature in the editor.

If any "Translated" or "Approved" strings have the same source content and satisfy all the conditions, one of these strings will be used. If there is only one such string, it will be used. If there are multiple strings, the closest match is selected by:

 1. If any strings satisfy all conditions without matching a `Don't Copy` or `Continue as Fuzzy` condition, the latest translation is used.
 1. Otherwise, if any strings reach rule 5 without matching a `Don't Copy` rule, but match a `Continue as Fuzzy` rule, a "Fuzzy" string is used. If there is more than one such string, the strings that match later `Copy as Fuzzy` conditions take higher priority. The highest priority string is used, or if multiple strings share the highest priority, the string with the latest translation is used.
 1. If all strings match one or more `Don't Copy` rules, no translation is copied for the text flow.

This process is repeated for each text flow in the uploaded document, or, if Copy Trans is run against a project version, for each text flow in each document in the project version.

*Note:* If a "Fuzzy" translation already exists for a text flow, it may be overwritten by a "Translated" or "Approved" translation. Consequently, Copy Trans is best used before translation and review work is initiated on a project, because overwriting partly-completed translations and rejected strings could interfere with translator and reviewer work flows and cause confusion.

## Examples of Copy Trans Options

#### Permissive Options

If `Continue` is selected for all conditions, only the required "On content mismatch" rule is checked. This means that if a string has matching content it bypasses all other conditions and is reused in a "Translated" state (or "Approved" if your project requires review and the translation being copied is already in "Approved" state). When there are multiple matches, the latest translation is used.
<figure>
![Copy trans permissive options](/images/copy-trans-permissive.png)
</figure>
<br/>

#### Strict Options

If `Don't Copy` is selected for all conditions, a string must have matching content, a matching ID, be from the same project, and be from a document with the same name and path, otherwise it will not be reused. If all of the conditions are passed, the content is reused in a "Translated" state (or "Approved" if your project requires review and the translation being copied is already in "Approved" state).
<figure>
![Copy trans strict options](/images/copy-trans-strict.png)
</figure>
<br/>

#### Options for a New Version

For this example, consider that you have a new version of your project in Zanata. The previous version is completely translated and you want to reuse the translations, but the new version uses a different directory structure. This means that the document paths have all changed. The ID for most strings is the same, but some new strings have been added. You should set up the options as follows:

1. To ensure that Copy Trans uses strings from the other version in your project only, set "On project mismatch" to `Don't Copy`.
1. To ensure that the changed document paths do not affect the reuse process, set "On document mismatch" to `Continue`.
1. Because most string IDs have not changed, set "On Context mismatch" to `Continue as Fuzzy`.
<figure>
![Copy trans new version](/images/copy-trans-newversion.png)
</figure>
<br/>


## Running Copy Trans

You can run Copy Trans manually against a project version.

1. On the project version page, click on `More actions` menu on the top right.
<figure>
![Copy trans access](/images/copy-trans-access.png)
</figure>
<br/>
1. Select `Copy Translations`.
1. Select the appropriate action for each condition, and then click `Copy Translations` button.
<figure>
![Copy trans dialog](/images/copy-trans-dialog.png)
</figure>
<br/>

A progress bar on the version page displays the progress of the operation. Depending on the size of the project and the number of available translations, this process can take some time to complete.
