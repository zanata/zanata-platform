An _experimental_ new feature was added to enable uploading and downloading additional types of documents.

This feature provides the ability to upload and download documents using the website, the REST interface, or Zanata client.

## Supported Formats

Supported formats include:

* plain-text (*.txt)
* Document Type Definition (*.dtd)
* Open Document Format (LibreOffice):
    * Open Document Text (*.odt)
    * Open Document Presentation (*.odp)
    * Open Document Spreadsheet (*.ods)
    * Open Document Graphics (*.odg)
* InDesign Markup Language (*.idml)

_Open Document Formula (*.odf) and Open Document Database (*.odb) are not supported._

## Known Issues

This is an experimental feature, and several issues exist that should be considered when using this feature:

### Offline Translation in the Original Document Format Only Partially Supported

Offline translation in the original document format must have exactly the same layout of the source document. These offline translations will work properly if paragraphs are not rearranged at all. If any paragraphs are added, split or removed, text flows after the affected paragraph will be associated with the wrong source string.

_**Workaround:** Do not change the number of paragraphs or rearrange any paragraphs when doing offline translation. A more reliable solution is to use the Zanata web editor instead of translating offline._

### Source Strings in Translated Documents Are Uploaded as Translations

If you upload a document that is only partially translated, any source strings in the document are treated as approved translations.

_**Workaround:** Only upload translation documents that are completely translated. Alternatively, enter translations in the Zanata web editor to avoid the issue entirely._


## Tips for Translating 'Raw' Documents
### Inline Tags
Some parts of raw documents are not intended for direct translation. These are converted to xml-style inline tags such as "&lt;g1>&lt;g2>&lt;/g2>&lt;/g1>" in the place of the image in the example document. It is recommended that these tags be included in translations with no modifications. The [XML/HTML tags validator](user-guide/projects/validations#htmlxml-tags) can help detect accidental changes to inline tags. 

If unsure, you can also download a preview document to ensure that there are no errors or layout problems associated with treatment of tags - see [Downloading Translations through Web Interface](user-guide/documents/download-translated-documents/#from-website)