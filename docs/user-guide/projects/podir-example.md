This document presents an example of a Zanata project with type `podir`. It assumes that you have already created a project and a version (See [Project Creation](user-guide/projects/create-project) ).

Podir projects consist of multiple source files (.pot) in a common source directory, and several translation files (.po) located in their corresponding locale's directory. Below is a typical directory structure for a Podir project:

```bash
project-name
|-- zanata.xml
|-- pot
|   |-- file1.pot
|   |-- file2.pot
|   |-- file3.pot
|-- ja
|   |-- file1.po
|   |-- file2.po
|   |-- file3.po
|-- es
|   |-- file1.po
|   |-- file2.po
|   |-- file3.po
|-- it
|   |-- file1.po
|   |-- file2.po
|   |-- file3.po
...
```

Notice the presence of the `zanta.xml` file already there. This file will be generated for you by the [Zanata client init](http://zanata-client.readthedocs.org/en/latest/commands/init/) command, or you can download it from the Zanata server. This file will look something like this:

```xml
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<config xmlns="http://zanata.org/namespace/config/">
  <url>http://yourinstance.zanata.org/</url>
  <project>project-slug</project>
  <project-version>version-slug</project-version>
  <project-type>gettext</project-type>
  <locales>
    <locale>ja</locale>
    <locale>es</locale>
    <locale>it</locale>
    ...
  </locales>
</config>
```



See [Upload documents](user-guide/versions/upload-strings) and [Download translation](user-guide/versions/download-translations) for more information.

