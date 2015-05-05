This document presents an example of a Zanata project with type `gettext`. It assumes that you have already created a project and a version (See [Project Creation](user-guide/projects/create-project) ).

Gettext projects consist of a single source file (.pot), and several translation files (.po) named after their corresponding locale. Below is a typical directory structure for a Gettext project:

```bash
project-name
|-- zanata.xml
|-- singlesourcefile.pot
|-- es_ES.po
|-- fr.po
|-- zh_TW.po
...
```

Notice the presence of the `zanata.xml` file already there. This file will be generated for you by the Zanata client `init` command, or you can download it from the Zanata server. For more information see [Configuring the Client](http://zanata-client.readthedocs.org/en/latest/configuration/). This file will look something like this:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<config xmlns="http://zanata.org/namespace/config/">
  <url>http://yourinstance.zanata.org/</url>
  <project>project-slug</project>
  <project-version>version-slug</project-version>
  <project-type>gettext</project-type>
  <locales>
    <locale map-from="es_ES">es-ES</locale>
    <locale>fr</locale>
    <locale map-from="zh_TW">zh-Hant</locale>
    ...
  </locales>
</config>
```

This example is also using the locale mapping feature. The `map-from` attributes on the `locale` elements are telling the client that although it will find the files using locales `es_ES` and `zh_TW`, those translated documents should be stored in the server under locales `es-ES` and `zh-Hant`.

See [Upload documents](user-guide/versions/upload-strings) and [Download translation](user-guide/versions/download-translations) for more information.
