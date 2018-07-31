# Merge translations from Machine translation

MT Merge allows users with `MT-bulk` role to prefill translations from [configured Machine Translations service](/user-guide/system-admin/configuration/installation/#machine-translations-magpie).

## Restrictions

- This feature is only available to user with `MT-bulk` role.
- Only [Gettext](/user-guide/projects/project-types) and [Properties](/user-guide/projects/project-types) project type are supported.
- The operation is limited to one language at a time.
- Only `fuzzy` and `untranslated` strings will be changed.
- User can select to save the translations as `fuzzy` or `translated`.
- MT Merge will save translations with validation errors as fuzzy.

     
## Run MT Merge for version

1. Login to Zanata.
1. Select a project version you wish to copy translations to.
1. Expand `More Action` menu on top right corner and click on `Merge Machine Translations`. This action is only available if for user with `MT-bulk` role.
<figure>
![More action menu in project version page](/images/version-more-action-menu.png)
</figure>
1. In displayed dialog, select the language you want to copy.
<figure>
![MT Merge language dialog](/images/version-mt-merge-language.png)
</figure>
1. Select the state you wish to save the translation as. `Fuzzy` or `Translated`
<figure>
![MT Merge dialog](/images/version-mt-dialog.png)
</figure>
1. Click `Run Merge` button to start the process.
1. The progress of the merge process is shown by a progress bar on the dialog. You can cancel the process before it finishes.

## Cancel MT Merge

User can click on `Cancel Operation` during the process to cancel the operation.
**_Note: This will only stop additional translations being copied. Any translations that have already been copied will remain._**
<figure>
![MT Merge process](/images/mt-merge-process.png)
</figure>
