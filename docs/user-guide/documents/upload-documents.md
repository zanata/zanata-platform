# Upload documents

Anyone with an account on Zanata can create a translation project for their documents.

You will need to [Create a project](/user-guide/projects/create-project) and [Create a version](/user-guide/versions/create-version) to upload strings.

------------

### From website

* Go to project version page

#### Uploading new source documents

1. Go to `Documents` tab, click on down arrow on top of Document panel.
![Upload source document](/images/upload-new-source-doc.png)
1. Select `Upload new source document`.
1. Browse or Drag your documents into the dialog and click `Upload Documents`.
1. You can access the same dialog in by go to `Settings` tab -> `Documents` section. Click `+` sign on top left panel.
![Upload source document dialog](/images/upload-source-doc-dialog.png)
<br/>

#### Updating existing source documents

1. Go to `Documents` tab, click on down arrow on left side of document and select `Update this document`.
1. Select your file and choose the source language for this file.
1. Click `Upload` to proceed.

![Upload source document](/images/upload-source-doc.png)

*Alternatively*

1. Go to `Settings` tab -> `Documents` section. Click `+` sign on top left panel.
1. Click on `Update this document` icon on right side of a document.
1. Select your file and choose the source language for this file.
1. Click `Upload` to proceed.

![Upload source document from settings](/images/upload-source-doc-from-settings.png)

 
#### Uploading translation documents

1. Click on `Languages` tab in version page, select the language of translation document you wish to upload.
1. On right panel, click on the down arrow(Translation option) on left side of document and select `Upload translation`.

![Upload translation document](/images/upload-translation-doc.png)
<br/>

![Upload translation document dialog](/images/upload-translation-doc-dialog.png)

1. On the dialog, select your translation file. 
    - `Merge` option - If unchecked, uploaded translations overrides current translation, otherwise, it will merge with current translation in system.
    - `My translations` option - Indicates if all uploaded translations were translated by you.

#### Translation document formats
Translation files, typically of the Gettext .po format, can be uploaded for a document in the selected language.
There is a limited set of document formats that support upload of a translated raw document. These are:
 * DTD (.dtd)
 * Qt Linguist (.ts)
 * Subtitle (.srt, .sbt, .sub, .vtt)

Other document formats uploaded as translation sources will have no, or potentially undesirable, results.

------------

### From client

See [Zanata-Client push command](/client/commands/push) for more information.
