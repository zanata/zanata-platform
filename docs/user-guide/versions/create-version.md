Documents for a project are grouped into versions, rather than being added directly to the project.

For simple projects, it is typical to create a single version named 'master'. Other projects use workflows in which there is a version under active development, and one or more versions that are being maintained in a stable state with only some minor changes. Grouping these related versions under the same project allows for easier reuse of translations between versions.


 1. [Create a project](user-guide/projects/create-project).
 1. **Create a version under the project.**
 1. Upload documents to the version:
   - Using the website
   - Using the [command-line client push command](http://zanata-client.readthedocs.org/en/latest/commands/push/)


## Version creation through the website

To add a version to your project, navigate to the project using the menu or user dashboard, go to `Version` tab, click on `More action` and select `New Version`. If your project has no versions yet, there will be a link under the project on the user dashboard to jump straight to version creation.

![Version creation button on a project homepage](images/version-create.png)

Version creation button on a project homepage.

![Shortcut version creation link on user dashboard](images/user-dashboard-create-version.png)

Shortcut version creation link on user dashboard.


The following screenshot shows the version creation page. The settings in this screenshot will create a version under the 'zanata-server' project with ID 'master', that does not require a review phase for translations and will use the locales and validations from the project. See below for details on each option.


![Example version creation form with completed name and other settings](images/version-create-new.png)


### Version ID

This is the identifier used to refer to the version on the Zanata website and when using one of the Zanata clients. Versions do not use a separate display name.

### Project Type

Project Type defines the type of files that your project uses to store source and translation strings. For more information see the [Project Types](user-guide/projects/project-types).

### Copy from previous version

If selected, this new version is will based off the selected version.
See [Copy version](user-guide/translation-reuse/copy-version) for more information.

## Version creation through the command line

Versions can also be created using the command-line client. See the help output of the client for a list of available options.
