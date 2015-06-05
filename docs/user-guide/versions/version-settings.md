Once a version has been created, the maintainer can add further details and version behaviour via the Settings tab.
See the [Version Creation Help](/user-guide/versions/create-version) for details on creating version.
<figure>
![Version General Settings tab](/images/version-settings-button.png)
<figcaption>Versions Settings tab link.</figcaption>
</figure>

------------

## General Settings
<figure>
![Version General Settings tab](/images/version-general-settings.png)
<figcaption>Version General Settings tab</figcaption>
</figure>
<br>

### Project Type

Project Type settings by default inherits from project's settings but maintainers are able to select a different project type for the version. See [Project Types](/user-guide/projects/project-types) and [Project Settings](/user-guide/projects/project-settings/#project-type) for more information.

### Make this version read only

This button is used to set a version to read-only, which prevents translations being entered. This may be useful in some cases, but should be used sparingly so that translators are able to work on your project.

This can be toggled using the same button, as desired.

------------

## Documents Settings
<figure>
![Version Documents Settings tab](/images/version-documents-settings.png)
<figcaption>Version Documents Settings tab</figcaption>
</figure>

------------

### Adding source document

Click `+` sign on top left in Documents tab under Settings. Browse or Drag your documents into the dialog and click `Upload Documents`.

------------

## Languages Settings
<figure>
![Version Languages Settings tab](/images/version-languages-settings.png)
<figcaption>Version Languages Settings tab</figcaption>
</figure>

------------

### Customized locales

By default, your project will be available for translation to a set of locales defined for your project, or on the Zanata server if your project does not have customized locales. If your version requires a different set of locales from your project, click `Enable` or `Disable` button on right side of the locale.

------------

## Translation Settings
<figure>
![Version Translation Settings tab](/images/version-translation-settings.png)
<figcaption>Version Translation Settings tab</figcaption>
</figure>

------------
### Require translation review

See [Review overview](/user-guide/review/overview) for more information.

### Customized list of validations

If your version requires a different set of validations than the parent project, they can be selected here. If customized validations are not specified, the validations specified for your project will be used. An advantage of inheriting validations from the poject is that new validations can be added to the project without having to add them to each different version.

For more information, see [Project settings](/user-guide/projects/project-settings#validations).
