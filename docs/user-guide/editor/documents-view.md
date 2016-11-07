![Documents view](/images/editor-doc-list.png)
<br/>

1. **Breadcrumbs** - Links to Project -> Version
2. **Statistic** - Statistic and remaining hours for the selected project version in selected language.
3. [**Views**](/user-guide/editor/overview#views-in-webtrans)
4. **Search** - Allow translators to by document name.
5. **Change statistic view** - Change statistic measurement in `Words`(total words in the all documents) or `Messages` (total translation entry in all documents).
6. **Settings** - From top to bottom: Notification, Chat room, Settings, Validations.
7. **Paging** - Page navigation for document list.

## Settings
<figure>
![Documents view settings](/images/editor-document-settings.png)
</figure>
<br/>

* **Editor buttons** - Show buttons in the Editor

* **Enter key saves immediately** - Saves any document changes when enter is pressed

* **Use syntax highlighting Editor** - Highlights syntax. Warning: no spell check, long lines may have wrapping issues.

* **Show Save as Approved warning** - Users are shown a warning to confirm that they wish to save the text as Approved.

* **Navigation key/button** - Change the navigation keyboard shortcut to whichever you selected in the options.

* **Translation memory options** - Select whether to show diff between source string and search string or just highlighting.

* **Page size** - Allow user to customise documents to show per page.

* **Layout** - Customise the view of document list with `Compact`, `Default` and `Loose`.

* **Show System Errors** - Shows detailed messages in new windows when errors occurs.


## Validations
<figure>
![Editor doc list validations](/images/editor-doc-list-validations.png)
</figure>
<br/>

* **Run validation** - Allows user to run validations against all documents.

------

1. This will run validation for the documents that are currently visible in the list (if the document list is multiple pages, validation must be run separately for each page).
2. When validation is complete, an icon is shown next to each document name on the page. A check-mark icon means the document did not have any validation warnings. A cross icon and red document name mean one or more translations in the document caused a warning.
3. Click a red document name to see all the translations that caused warnings.

**Note:** validation icons will not change until the next time validation is run on the document, even if the translations are updated. Make sure to re-run validation after making any changes so that the results are accurate.

See [validations](/user-guide/projects/validations) for more details.
