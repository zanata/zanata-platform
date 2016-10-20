# Merge translations from another project version

Merge translations allows project maintainers to copy translations across different project version based on matching context. (See below for context matching rules)

## Restrictions

- This feature is only available to project maintainers.
- Only translations that are in translated/approved state will be used.
- Merge translation can only be run if there are no other copy operations in progress for the selected version, such as copy-trans or copy version.

## Rule of which translations will be copied over
<table class='docutils'>
    <tr>
        <td>**From**</td><td>**To**</td><td>**Copy?**</td>
    </tr>
    <tr>
        <td>fuzzy/untranslated</td><td>any</td><td>No</td>
    </tr>
    <tr>
        <td>different source text/document id/<br />resId/msgctxt/locale</td><td>any</td><td>No</td>
    </tr>
    <tr>
        <td>translated/approved</td><td>untranslated/fuzzy</td><td>Yes</td>
    </tr>
     <tr>
        <td>translated/approved</td><td>translated/approved</td><td>copy if `From` is newer and <br />`Keep existing translated/approved translations` is unchecked</td>
    </tr>
</table>
     
## Run Merge translations

1. Login to Zanata.
1. Select a project version you wish to copy translations to.
1. Expand `More Action` menu on top right corner and click on `Merge Translations`. This action is only available if you are a maintainer of the project.
<figure>
![More action menu in project version page](/images/version-more-action-menu.png)
</figure>
1. In displayed dialog, select the project-version you wish to copy translations from.
<figure>
![Merge translation dialog](/images/version-merge-trans-dialog.png)
</figure>
1. If you do not want to overwrite translated/approved translations, ensure that `Keep existing translated/approved translations` is checked. If this option is not checked, translated/approved translations will be replaced with newer translated/approved translations if they are available.
1. Click `Merge Translations` button to start the process.
1. The progress of the merge process is shown by a progress bar on the version page.
<figure>
![Merge translation in progress](/images/version-merge-trans-progress.png)
</figure>

## Cancel Merge translation
**_Note: This will only stop additional translations being merged. Any translations that have already been merged will remain merged._**

1. Go to the progress bar section in project version page.
1. Click on the `Cancel` button on top right panel.
<figure>
![Cancel merge translation in progress](/images/version-merge-trans-cancel.png)
</figure>



