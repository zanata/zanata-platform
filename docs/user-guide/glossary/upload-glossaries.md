### Prerequisite
See [glossary roles and permission](/user-guide/glossary/glossary-roles-permissions) for permission setup.

### Supported file format
#### PO

1. GNU Gettext-based PO file

#### CSV (comma-separated value), UTF-8 format
##### header row

1. locale columns: The locale header column must contain a valid locale code. At least one locale code is required for the glossary. First locale column will be used as source.
1. part-of-speech column (optional): The part-of-speech header column, when included, should be only one of: `pos`, `partofspeech` or `part of speech`.
1. description column (optional): The description header column, when included, should be only one of: `desc`, `description` or `definition` .


##### data rows

1. terminology columns: Terminology in each row should correspond to the locale in the header. At least one term is required for a row to be valid.
1. part-of-speech column: The part-of-speech is an informational field that indicates the sense in which the terms in the row should be used. Sample parts-of-speech include adjective, adverb, noun, and verb.
1. description column: The description should provide any notes for the translator, including the meaning of the terms in the row.
<figure>
![sample glossary csv file](/images/glossary-csv.png)
<figcaption>Valid glossary csv file</figcaption>
</figure>

### Upload via Web UI

1. Login into Zanata
1. To upload to the **system glossary**, click `Glossary` menu.
1. To upload to a **project glossary**, navigate to the project page, click on "Glossary" in the project page.
1. Click on `Import Glossary` on top right corner of the page.
<figure>
![More action in glossary page](/images/glossary-upload.png)
<figcaption>More actions in glossary table</figcaption>
</figure>

1. A window will popup, click on `choose file` to select your glossary file.
<figure>
![Glossary upload window](/images/glossary-upload-windows.png)
</figure>

1. For PO file format, you will need to select **Source and Target languages** of the selected po file.
<figure>
![Glossary upload window, PO file](/images/glossary-upload-windows-po.png)
</figure>

1. Click `Upload` button to start uploading selected glossary file.

### Upload via Zanata client

The following instructions assume that you have installed and configured the Zanata Client. Instructions for installation and configuration are available at [Zanata Client Installation](/client#installation).

**System glossary**

```
zanata-cli glossary-push --file {filename} --trans-lang {translation locale}
```

**Project glossary**

```
zanata-cli glossary-push --file {filename} --trans-lang {translation locale} --project {projectSlug}
```
