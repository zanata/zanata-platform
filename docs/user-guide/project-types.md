The Zanata CLI clients require a project type to be specified. The project type is used in `push` and `pull` operations to help determine where to look for source and translation files, the type of files to look for, and how to deal with the files that are found.

Project type can be specified on the command line, in `zanata.xml`, in `pom.xml`, and can now be stored on the server.

## Which Project Type?
File types are _(selectively)_ supported via the Okapi Framework.
### Supported Types

#### No Selection
No Selection allows the upload of all file types other than `.properties` and `.xml` via the Website GUI, but will prevent the use of Zanata CLI clients.

#### File `.txt` `.odt` `.ods` `.odg` `.odp` `.idml` `.dtd` `.htm` `.html`
Previously _Raw_, File is an **experimental** project type that provides limited support for plain-text, LibreOffice, HTML, inDesign and DTD files. Source files must be under a separate directory to translation files. The behaviour of this project type is subject to change without notice while it is in experimental state.<br>
The parser recognises newlines for `.txt`, and paragraphs for `.html`.

#### Gettext `.pot`
Uses the gettext format with a single template (.pot) file. Translation files (.po) are named with the locale identifier.
#### Podir `.pot`
Uses gettext format with multiple template (.pot) files. Translation files use the same name as template files, but are placed in a directory named with the locale identifier. Use this type for publican/docbook projects.
#### Properties `.properties`
Handles normal java properties files using ISO-8859-1 encoding (Latin-1). Java properties files require non Latin-1 characters to be escaped with unicode escape characters (e.g. \uFEDC).<br>
**_Note: Currently, properties files can only be uploaded using the [Zanata CLI Client](http://zanata.org/help/cli/cli-push/)_**
#### Utf8Properties `.properties`
Handles non-standard java properties files that use UTF-8 encoding, and do not use unicode escape characters.<br>
**_Note: Currently, properties files can only be uploaded using the [Zanata CLI Client](http://zanata.org/help/cli/cli-push/)_**

### Partial / Limited Support
#### Xliff
Not actively supported. _(Details on file schema hopefully forthcoming...)_
#### Xml
Not actively supported. _(Details on file schema hopefully forthcoming...)_

