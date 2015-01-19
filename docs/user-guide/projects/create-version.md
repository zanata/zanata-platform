Documents for a project are grouped into versions, rather than being added directly to the project.

For simple projects, it is typical to create a single version named 'master'. Other projects use workflows in which there is a version under active development, and one or more versions that are being maintained in a stable state with only some minor changes. Grouping these related versions under the same project allows for easier reuse of translations between versions.


 1. [Create a project](user-guide/projects/create-project).
 1. **Create a version under the project.**
 1. Upload documents to the version:
   - Using the website
   - Using the [command-line client push]({{ site.url }}/help/cli/cli-push) command


## Version creation through the website

To add a version to your project, navigate to the project using the menu or user dashboard and click the `Create Version` button. If your project has no versions yet, there will be a link under the project on the user dashboard to jump straight to version creation.

![Version creation button on a project homepage](images/create-version.png)

Version creation button on a project homepage.

![Shortcut version creation link on user dashboard](images/user-dashboard-create-version.png)

Shortcut version creation link on user dashboard.


The following screenshot shows the version creation page. The settings in this screenshot will create a version under the 'zanata-server' project with ID 'master', that does not require a review phase for translations and will use the locales and validations from the project. See below for details on each option.


![Example version creation form with completed name and other settings](images/create-version-master.png)


### Version ID

This is the identifier used to refer to the version on the Zanata website and when using one of the Zanata clients. Versions do not use a separate display name.

### Project Type

Project Type defines the type of files that your project uses to store source and translation strings. For more information, see `Project Type` under [Create a project][], and [Project Types wiki page](https://github.com/zanata/zanata/wiki/Project-Types).

### Require translation review

Translation review is an optional stage in the translation process in which an experienced translator can check that translations are of sufficient quality.

Without translation review, translators will save translations in the 'translated' state, making them immediately available to download and use for your project.

If you check the `Require translation review` option, translations in the 'translated' state are not considered ready for download. Instead, a reviewer must look at the translations and change them to 'approved' or 'rejected' state. Only translations in 'approved' state are considered ready for download.

Translation review adds extra time and effort to the translation process, so is not recommended for all projects. Translation review can be activated at a later time if it becomes necessary.

### Status

Status is used to set a version to read-only, which prevents translations being entered. Status should usually be left as active for a new version. Status can be set to read-only later, such as when work is finished on an old version and all translation effort should be put into the newer versions.

### Customized locales

By default, your project will be available for translation to a set of locales defined for your project, or on the Zanata server if your project does not have customized locales. If your version requires a different set of locales from your project, check the 'Would you like to add a customized list of locales?' checkbox to see and modify a list of locales available for your version.

### Customized list of validations

If your version requires a different set of validations than the parent project, they can be selected here. If customized validations are not specified, the validations specified for your project will be used. An advantage of inheriting validations from the poject is that new validations can be added to the project without having to add them to each different version.

For more information, see `Customized list of validations` under [Create a project][].


## Version creation through the command line

Versions can also be created using the command-line client. See the help output of the client for a list of available options.
