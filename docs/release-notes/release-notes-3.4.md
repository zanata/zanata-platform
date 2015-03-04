##### Infrastructure change

##### New Features
* [882770](https://bugzilla.redhat.com/show_bug.cgi?id=882770) - RFE: Filter translation units by multiple fields in the editor
* [988202](https://bugzilla.redhat.com/show_bug.cgi?id=988202) - RFE: REST API rate limiting
* [1002378](https://bugzilla.redhat.com/show_bug.cgi?id=1002378) - RFE: Introduce a modular translation structure, and gwt generate the *Messages.properties files
* [1066701](https://bugzilla.redhat.com/show_bug.cgi?id=1066701) - RFE: As a Zanata user, I would like to be able to bookmark language and project selections in the groups page
  * Now is possible to bookmark a project version, language, or setting item for communication or later reference. 
* [1066756](https://bugzilla.redhat.com/show_bug.cgi?id=1066756) - RFE: Merge user settings pages into dashboard
* [1066796](https://bugzilla.redhat.com/show_bug.cgi?id=1066796) - RFE: Implement new project page
* [1077439](https://bugzilla.redhat.com/show_bug.cgi?id=1077439) - RFE: Use lucene indexes to do Copy Trans.
* [1094100](https://bugzilla.redhat.com/show_bug.cgi?id=1094100) - RFE: As a user, I would like to be able to bookmark language and document selections on version page
* [1094106](https://bugzilla.redhat.com/show_bug.cgi?id=1094106) - RFE: As project maintainer, I would like to select copyTrans option before running it

##### Bug fixes
* [831479](https://bugzilla.redhat.com/show_bug.cgi?id=831479) - Bug 831479 - 500 internal error when REST client specifies invalid extensions
* [981085](https://bugzilla.redhat.com/show_bug.cgi?id=981085) - User not aware they can use underscores in username
* [1025645](https://bugzilla.redhat.com/show_bug.cgi?id=1025645) - Both GPL and LGPL license files are required for LGPLv2+ project
* [1033375](https://bugzilla.redhat.com/show_bug.cgi?id=1033375) - Copy and Paste does not work when typing Italian in msgstr
* [1043720](https://bugzilla.redhat.com/show_bug.cgi?id=1043720) - The project search field failed to found existing project using the project name
* [1062508](https://bugzilla.redhat.com/show_bug.cgi?id=1062508) - Spell check changes are not saved after replacement
* [1065790](https://bugzilla.redhat.com/show_bug.cgi?id=1065790) - Admin manage search no longer shows time estimates
* [1080770](https://bugzilla.redhat.com/show_bug.cgi?id=1080770) - Empty group "Add Project" button on languages tab doesn't work
* [1086036](https://bugzilla.redhat.com/show_bug.cgi?id=1086036) - Project / version language listing and inheritance issue
* [1088651](https://bugzilla.redhat.com/show_bug.cgi?id=1088651) - New About tab does not handle existing project Seam text
* [1088737](https://bugzilla.redhat.com/show_bug.cgi?id=1088737) - Project type for a version is null after creation if the project type setting is not touched
* [1094071](https://bugzilla.redhat.com/show_bug.cgi?id=1094071) - Copy Translations information not correct 
* [1094090](https://bugzilla.redhat.com/show_bug.cgi?id=1094090) - TMX import/export blocked by api not providing user key
* [1096564](https://bugzilla.redhat.com/show_bug.cgi?id=1096564) - Entering garbage at the end of a projects url breaks navigation
* [1097940](https://bugzilla.redhat.com/show_bug.cgi?id=1097940) - New password field should have show/hide toggle
* [1098003](https://bugzilla.redhat.com/show_bug.cgi?id=1098003) - Missing string for group request email notification sent
* [1098371](https://bugzilla.redhat.com/show_bug.cgi?id=1098371) - Sort options in language and document lists on the version page do not take effect until a search is performed on the list
* [1098924](https://bugzilla.redhat.com/show_bug.cgi?id=1098924) - Copy Translations copies translations that should not be copied
* [1099400](https://bugzilla.redhat.com/show_bug.cgi?id=1099400) - Failed to upload translation via JSF
* [1100079](https://bugzilla.redhat.com/show_bug.cgi?id=1100079) - Activity containing tags causes a broken dashboard
* [1100092](https://bugzilla.redhat.com/show_bug.cgi?id=1100092) - Filter translation units by multiple fields in the editor should use ISO 8601 date format
* [1100131](https://bugzilla.redhat.com/show_bug.cgi?id=1100131) - \[webTran\] filter translation by last modified date returns wrong result
* [1102964](https://bugzilla.redhat.com/show_bug.cgi?id=1102964) - CopyTrans takes excessively long hours to complete copying translations
* [1103930](https://bugzilla.redhat.com/show_bug.cgi?id=1103930) - Noticeable delay on right column when selection are made on left column (ui design)
* [1103940](https://bugzilla.redhat.com/show_bug.cgi?id=1103940) - Remove info level notification popup from the editor
* [1103947](https://bugzilla.redhat.com/show_bug.cgi?id=1103947) - \[Translation Editor\] Dialog "Invalid translation" failed to obtain input focus.
* [1107882](https://bugzilla.redhat.com/show_bug.cgi?id=1107882) - translate.zanata.org admin manage users screen can not be loaded
* [1109611](https://bugzilla.redhat.com/show_bug.cgi?id=1109611) - Version drop down with quick actions not shown on Project page
* [1109653](https://bugzilla.redhat.com/show_bug.cgi?id=1109653) - \[Project Version\] Failed to load language list for source file name that contains space " "
* [1111449](https://bugzilla.redhat.com/show_bug.cgi?id=1111449) - Hold active tasks in a map, but cache finished tasks briefly