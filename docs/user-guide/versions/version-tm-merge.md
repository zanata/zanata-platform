# Merge translations from TM

TM Merge allows project maintainers to copy translations from TM based on certain criteria. (See below for matching criteria)

## Restrictions

- This feature is only available to project maintainers.
- Only untranslated strings will be changed
- TM Merge translation can only be run if there are no other TM merge operations in progress for the selected version and locale.

## Criteria of which translations will be copied over
<table class='docutils'>
    <tr>
        <td>**Condition**</td><td>**User selection**</td><td>**Copy?**</td>
    </tr>
    <tr>
        <td>Different content</td><td>above selected threshold (80%, 90%)</td><td>Copy as fuzzy</td>
    </tr>
    <tr>
        <td>Different document</td><td>Don't copy</td><td>No</td>
    </tr>
    <tr>
        <td>Different document</td><td>Copy as fuzzy</td><td>Saved as fuzzy</td>
    </tr>
    <tr>
        <td>Different msgctxt or res ID (message context)</td><td>Don't copy</td><td>No</td>
    </tr>
    <tr>
        <td>Different msgctxt or res ID (message context)</td><td>Copy as fuzzy</td><td>Saved as fuzzy</td>
    </tr>
    <tr>
        <td>From imported TM (TMX import)</td><td>Don't copy</td><td>No</td>
    </tr>
    <tr>
        <td>From imported TM (TMX import)</td><td>Copy as fuzzy</td><td>Saved as fuzzy</td>
    </tr>
    <tr>
        <td>From imported TM (TMX import)</td><td>Copy as fuzzy</td><td>Saved as fuzzy</td>
    </tr>
    <tr>
        <td>TM origin</td><td>From any project version or from selected project versions</td><td></td>
    </tr>
</table>
     
## Run TM Merge for version

1. Login to Zanata.
1. Select a project version you wish to copy translations to.
1. Expand `More Action` menu on top right corner and click on `TM Merge Translations`. This action is only available if you are a maintainer of the project.
<figure>
![More action menu in project version page](/images/version-more-action-menu.png)
</figure>
1. In displayed dialog, select the language you want to copy.
<figure>
![TM Merge translation dialog](/images/version-tm-merge-dialog.png)
</figure>
1. Check or uncheck different TM selection criteria.
1. If you just want to copy translations from certain project versions, ensure that `Select TM from all projects` is unchecked. Then use the search box to search for projects and add them to the `From Source` section. Adjust project/version order accordingly.
1. Click `Merge Translations` button to start the process.
1. The progress of the merge process is shown by a progress bar on the dialog. You can cancel the process before it finishes.

## Cancel Merge translation
**_Note: This will only stop additional translations being copied. Any translations that have already been copied will remain._**
