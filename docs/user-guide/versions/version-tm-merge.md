# Merge translations from Translation Memory

TM Merge allows project maintainers to copy translations from TM based on certain criteria. (See below for matching criteria)

## Restrictions

- This feature is only available to project maintainers.
- Only untranslated strings will be changed
- TM Merge translation can only be run if there are no other TM merge operations in progress for the selected version and locale.

## Criteria of which translations will be copied
<table class='docutils'>
    <tr>
        <td>**Condition**</td><td>**User selection**</td><td>**Copy?**</td>
    </tr>
    <tr>
        <td>Different content</td><td>above selected threshold (80%, 90%)</td><td>Copy as fuzzy</td>
    </tr>
    <tr>
        <td>From different project</td><td>I don't mind at all</td><td>Saved as translated if other conditions are met</td>
    </tr>
    <tr>
        <td>From different project</td><td>I will need to review it</td><td>Saved as fuzzy</td>
    </tr>
    <tr>
        <td>Different document</td><td>I don't want it</td><td>No</td>
    </tr>
    <tr>
        <td>Different document</td><td>I will need to review it</td><td>Saved as fuzzy</td>
    </tr>
    <tr>
        <td>Different document</td><td>I don't mind at all</td><td>Saved as translated if other conditions are met</td>
    </tr>
    <tr>
        <td>Different msgctxt or res ID (message context)</td><td>I don't want it</td><td>No</td>
    </tr>
    <tr>
        <td>Different msgctxt or res ID (message context)</td><td>I will need to review it</td><td>Saved as fuzzy</td>
    </tr>
    <tr>
        <td>Different msgctxt or res ID (message context)</td><td>I don't mind at all</td><td>Saved as translated if other conditions are met</td>
    </tr>
    <tr>
        <td>From imported TM (TMX import)</td><td>I don't mind at all</td><td>Saved as translated if other conditions are met</td>
    </tr>
    <tr>
        <td>From imported TM (TMX import)</td><td>Copy as fuzzy</td><td>Saved as fuzzy</td>
    </tr>
    <tr>
        <td>TM origin</td><td>From any project version or from selected project versions</td><td>Yes if it's from selected versions</td>
    </tr>
</table>
     
## Run TM Merge for version

1. Login to Zanata.
1. Select a project version you wish to copy translations to.
1. Expand `More Action` menu on top right corner and click on `TM Merge Translations`. This action is only available if you are a maintainer of the project.
<figure>
![More action menu in project version page](/images/version-more-action-menu.png)
</figure>
1. In displayed dialog, select the language you want to copy and the percentage threshold for TM matches.
<figure>
![TM Merge translation dialog](/images/version-tm-merge-language-percent.png)
</figure>
1. Select TM sources. You can copy translation from either project TM or imported TMX or both. Based on your selection, you can also configure the copy criteria.
<figure>
![TM Merge translation source](/images/version-tm-merge-sources.png)
</figure>
1. If you just want to copy translations from certain project versions, ensure that `Select TM from some projects` is checked. Then use the search box to search for projects and add them to the `From Source` section. Adjust project/version order accordingly.
1. Click `Merge Translations` button to start the process.
1. The progress of the merge process is shown by a progress bar on the dialog. You can cancel the process before it finishes.

## Cancel Merge translation
**_Note: This will only stop additional translations being copied. Any translations that have already been copied will remain._**
