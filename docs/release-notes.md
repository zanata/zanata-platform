## 3.6.1

<h5>Bugfixes</h5>
* [1194543](https://bugzilla.redhat.com/show_bug.cgi?id=1194543) - Manual document re-upload makes previous translations fuzzy
* [1183412](https://bugzilla.redhat.com/show_bug.cgi?id=1183412) - Emails to administrators are sent in the current interface language
* [875965](https://bugzilla.redhat.com/show_bug.cgi?id=875965) - Enable visible white space in source
* [1205465](https://bugzilla.redhat.com/show_bug.cgi?id=1205465) - User emails are visible to non admin users in Language page
* [1205468](https://bugzilla.redhat.com/show_bug.cgi?id=1205468) - Sorting mechanism broken on Languages page
* [1205046](https://bugzilla.redhat.com/show_bug.cgi?id=1205046) - Key shortcuts are not all visible on a small window
* [1000273](https://bugzilla.redhat.com/show_bug.cgi?id=1000273) - Font in TM and font in Editor Not matching
* [1013928](https://bugzilla.redhat.com/show_bug.cgi?id=1013928) - Editor options panel cannot scroll on small screens

-----------------------

<h5>New Features</h5>
*

----

## 3.6

<h5>New Editor (Alpha)</h5>

[1088137](https://bugzilla.redhat.com/show_bug.cgi?id=1088137) - Translation Editor: Alpha 1 Prototype

The editor prototype can be accessed via the **(Try the new alpha editor)** button at the top of the regular editor.  It showcases the look and feel, workflow and intended direction of Zanata.

As it is a _prototype_, there are bound to be some bugs and sub-optimal behaviours - any suggestions or reports can be forwarded to our [bug tracker](https://bugzilla.redhat.com/enter_bug.cgi?product=Zanata).
* [1150373](https://bugzilla.redhat.com/show_bug.cgi?id=1150373) - Keyboard shortcuts
* [1172437](https://bugzilla.redhat.com/show_bug.cgi?id=1172437) - Add plurals to the new editor
* [1174071](https://bugzilla.redhat.com/show_bug.cgi?id=1174071) - [SPA editor] Save on Invalid entry should not cause NullPointerException

<h5>Infrastructure Changes</h5>

Zanata now requires JMS to be configured in standalone.xml in order to queue up some messages going out of the system. For instructions on how to do this, please [See Here](configuration/jms-messaging)

<h5>Drupal Plugin</h5>
* [1078009](https://bugzilla.redhat.com/show_bug.cgi?id=1078009) -  PROTOTYPE: Drupal plugin to push and pull Zanata translations
* [1148233](https://bugzilla.redhat.com/show_bug.cgi?id=1148233) -  RFE: Drupal plugin should split content into meaningful chunks
* [1150336](https://bugzilla.redhat.com/show_bug.cgi?id=1150336) -  RFE: Document Drupal plugin manual installation method

<h5>New Features</h5>
* [1044261](https://bugzilla.redhat.com/show_bug.cgi?id=1044261) - Drupal integration with Zanata

* [1127066](https://bugzilla.redhat.com/show_bug.cgi?id=1127066) - Copy Version button on project version listing
* [1162383](https://bugzilla.redhat.com/show_bug.cgi?id=1162383) - Updated pages in Administration section
* [1120457](https://bugzilla.redhat.com/show_bug.cgi?id=1120457) - Email notify the user when the language team permissions change
* [1139950](https://bugzilla.redhat.com/show_bug.cgi?id=1139950) - Flexible Translation file naming
* [1092193](https://bugzilla.redhat.com/show_bug.cgi?id=1092193) - Individual Translator Statistics
* [1127056](https://bugzilla.redhat.com/show_bug.cgi?id=1127056) - Migration Guide for community users
* [1122776](https://bugzilla.redhat.com/show_bug.cgi?id=1122776) - WebHooks callback API
* [1186951](https://bugzilla.redhat.com/show_bug.cgi?id=1186951) - Zanata Overlay module
* [1183994](https://bugzilla.redhat.com/show_bug.cgi?id=1183994) - RFE: Gather and display metrics detailing the number of words translated by a specific translator, for a specific project


<h5>Bugfixes</h5>
* [1132271](https://bugzilla.redhat.com/show_bug.cgi?id=1132271) - Access contact admin url without logging in will trigger an exception
* [1082448](https://bugzilla.redhat.com/show_bug.cgi?id=1082448) - Dashboard shows incorrect number of maintained projects
* [1069951](https://bugzilla.redhat.com/show_bug.cgi?id=1069951) - Empty string in adding a language causes a broken language to be added
* [1149968](https://bugzilla.redhat.com/show_bug.cgi?id=1149968) - Translation history shows last modifier as "Someone offline"
* [1154461](https://bugzilla.redhat.com/show_bug.cgi?id=1154461) - Admin user management list datascroller is broken
* [1160651](https://bugzilla.redhat.com/show_bug.cgi?id=1160651) - Regression: Admin server config save no longer shows success message
* [1166451](https://bugzilla.redhat.com/show_bug.cgi?id=1166451) - Normal user can access copy to new version action for non-maintained project
* [1172392](https://bugzilla.redhat.com/show_bug.cgi?id=1172392) - Project tab on dashboard does not show for users with no projects.
* [1174516](https://bugzilla.redhat.com/show_bug.cgi?id=1174516) - File mapping rules failed to be referred for project type podir push
* [1180988](https://bugzilla.redhat.com/show_bug.cgi?id=1180988) - Unable to add arbitrary language to Zanata in new admin page
* [1185134](https://bugzilla.redhat.com/show_bug.cgi?id=1185134) - Placeholder text in server config ToU field valid, but rejected
* [1185170](https://bugzilla.redhat.com/show_bug.cgi?id=1185170) - Create version in a project is always created as read only
* [1186084](https://bugzilla.redhat.com/show_bug.cgi?id=1186084) - WebUI is very slow if users cannot access Google
* [1186997](https://bugzilla.redhat.com/show_bug.cgi?id=1186997) - Introduction of hornetq-ra breaks the overlay installer
* [1192271](https://bugzilla.redhat.com/show_bug.cgi?id=1192271) - For gettext plural project, project-version statistics was inconsistent between language and document, sometime more than 100%
* [1193699](https://bugzilla.redhat.com/show_bug.cgi?id=1193699) - Bookmarked url (selected language or selected doc) in version page, bookmarked url selected language, selected project in version-group page not working

-----------------------

## 3.5

<h5>Infrastructure changes</h5>
* Now requires (i.e. is tested on) OpenJDK 7

<h5>New Features</h5>
* [1066694](https://bugzilla.redhat.com/show_bug.cgi?id=1066694) - As a project maintainer I would like to upload multiple source files simultaneously
* [1062835](https://bugzilla.redhat.com/show_bug.cgi?id=1062835) - SubRip Text (.srt) subtitle format support
* [1110048](https://bugzilla.redhat.com/show_bug.cgi?id=1110048) - Redesign account merge page
* [1110959](https://bugzilla.redhat.com/show_bug.cgi?id=1110959) - Add in more sorting options in version page
* [1110175](https://bugzilla.redhat.com/show_bug.cgi?id=1110175) - Add a JBoss SSO Login module
* [1110627](https://bugzilla.redhat.com/show_bug.cgi?id=1110627) - As a command line user I would like to be guided in setting up a project
* [1104015](https://bugzilla.redhat.com/show_bug.cgi?id=1104015) - Fork/copy from previous version with source and translation
* [1122363](https://bugzilla.redhat.com/show_bug.cgi?id=1122363) - Update glossary page view
* [1131300](https://bugzilla.redhat.com/show_bug.cgi?id=1131300) - Update on editor UI

<h5>Bug fixes</h5>
* [971652](https://bugzilla.redhat.com/show_bug.cgi?id=971652) - \[Document List\] Clicking column header "Complete" mistakenly sort by other column you sort
* [1060629](https://bugzilla.redhat.com/show_bug.cgi?id=1060629) - Manage Languages breadcrumb takes user to the wrong page
* [1094094](https://bugzilla.redhat.com/show_bug.cgi?id=1094094) - Copy Translations does not update the shown stats, if the language list is already loaded
* [1097470](https://bugzilla.redhat.com/show_bug.cgi?id=1097470) - When adding/removing maintainers in group, maintainer list doesn't update
* [1098394](https://bugzilla.redhat.com/show_bug.cgi?id=1098394) - No url validation on project homepage field
* [1098404](https://bugzilla.redhat.com/show_bug.cgi?id=1098404) - Project search resizes in the middle of clicking a result, preventing the click
* [1098407](https://bugzilla.redhat.com/show_bug.cgi?id=1098407) - Copy Translations box does not close if process halted via Process Manager
* [1099278](https://bugzilla.redhat.com/show_bug.cgi?id=1099278) - Changing email address produces invalid email
* [1099736](https://bugzilla.redhat.com/show_bug.cgi?id=1099736) - Increase cache retention for statistics
* [1102455](https://bugzilla.redhat.com/show_bug.cgi?id=1102455) - \[Search Field\] Failed to search the project by whole project name that contains spaces ' ' and hyphen '-'
* [1097552](https://bugzilla.redhat.com/show_bug.cgi?id=1097552) - Obsolete groups sometimes not visible to maintainer
* [1102488](https://bugzilla.redhat.com/show_bug.cgi?id=1102488) - \[zanata:stat\] Failed to return proper error message when getting stat for non-exists projects and versions
* [1101803](https://bugzilla.redhat.com/show_bug.cgi?id=1101803) - TMX clear function doesn't work from UI
* [1103547](https://bugzilla.redhat.com/show_bug.cgi?id=1103547) - Empty document statistic should show "No content" in version tabs
* [978618](https://bugzilla.redhat.com/show_bug.cgi?id=978618) - Accidental broken feature - admin can change usernames
* [1067288](https://bugzilla.redhat.com/show_bug.cgi?id=1067288) - Reduce size of zanata.war; exclude unused dependencies
* [1110599](https://bugzilla.redhat.com/show_bug.cgi?id=1110599) - Remove unused page in Zanata
* [1103940](https://bugzilla.redhat.com/show_bug.cgi?id=1103940) - Remove info level notification popup from the editor
* [1011310](https://bugzilla.redhat.com/show_bug.cgi?id=1011310) - Unhandled exception: Mail service is down
* [995904](https://bugzilla.redhat.com/show_bug.cgi?id=995904) - Unnecessary ellipsis on short TM source name in editor
* [994293](https://bugzilla.redhat.com/show_bug.cgi?id=994293) - Cancelling an upload causes a database lock exception
* [973509](https://bugzilla.redhat.com/show_bug.cgi?id=973509) - User not aware they can use other characters in Group ID
* [1112041](https://bugzilla.redhat.com/show_bug.cgi?id=1112041) - Upload feature should handle files that are deleted before the process begins nicely
* [993445](https://bugzilla.redhat.com/show_bug.cgi?id=993445) - User can successfully upload a txt file that doesn't exist
* [1130797](https://bugzilla.redhat.com/show_bug.cgi?id=1130797) - Cache document statistic and overflow to disk
* [1128954](https://bugzilla.redhat.com/show_bug.cgi?id=1128954) - Convoluted way of opening docs from groups
* [1120034](https://bugzilla.redhat.com/show_bug.cgi?id=1120034) - Pushing translations is too slow

-----------------------

## 3.4

<h5>New Features</h5>
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

<h5>Bug fixes</h5>
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

-----------------------

## 3.3.2

<h5>Infrastructure changes</h5>

* Now requires (i.e. is tested on) [JBoss EAP](http://www.jboss.org/products/eap) 6.2.0 instead of 6.1.1

<h5>New Features</h5>

* [978072](https://bugzilla.redhat.com/show_bug.cgi?id=978072) - RFE: support roff as an input/output format
    * This feature is implemented on the client side only with [1038449 - command hook](https://bugzilla.redhat.com/show_bug.cgi?id=1038449). Users who wish to push .roff file can use a command hook to invoke external tool (po4a) before push to convert .roff into .pot. Invoke po4a again after pull to convert translated .po into .roff.

* [1036435](https://bugzilla.redhat.com/show_bug.cgi?id=1036435) - RFE: Upgrade to Liquibase 3.x
* [980670](https://bugzilla.redhat.com/show_bug.cgi?id=980670) - [RFE] Add HTML as an input method to be translated
    * .html and .htm files are now supported in Zanata for translation.

* [1067253](https://bugzilla.redhat.com/show_bug.cgi?id=1067253) - RFE:/Tech Debt - Propagate translation done by upload and copyTrans to editor
    * Prior to this implementation, editor will not receive translation updates done by CopyTrans or REST, i.e. upload translation file though web UI or push from client. Now translation done by any of the above will be broadcast to any open editors. This includes latest translation and statistics.

* [1002378](https://bugzilla.redhat.com/show_bug.cgi?id=1002378) - RFE: Introduce a modular translation structure, and gwt generate the *Messages.properties files
    * Now Zanata editor is ready to be translated. See [[Localize Zanata]] for detail.

<h5>Bug fixes</h5>
* [981071](https://bugzilla.redhat.com/show_bug.cgi?id=981071) - Register account still available when logged in
* [995324](https://bugzilla.redhat.com/show_bug.cgi?id=995324) - "Agree to the Terms of Use" should be displayed looks relevant to users that sign up with OpenId
* [1023227](https://bugzilla.redhat.com/show_bug.cgi?id=1023227) - Add language member request email contains string jsf.email.joinrequest.RoleRequested
* [1035057](https://bugzilla.redhat.com/show_bug.cgi?id=1035057) - Group "Add Language" field should be limited to something sensible
* [1037925](https://bugzilla.redhat.com/show_bug.cgi?id=1037925) - Search Projects field not character limited
* [1039776](https://bugzilla.redhat.com/show_bug.cgi?id=1039776) - Email template link to zanata log broken
* [1039810](https://bugzilla.redhat.com/show_bug.cgi?id=1039810) - Cancel contact email causes exception
* [1049643](https://bugzilla.redhat.com/show_bug.cgi?id=1049643) - Using the project search field breaks the drop down main menu
* [1060627](https://bugzilla.redhat.com/show_bug.cgi?id=1060627) - [Regression] Drop down navmenu does not work in the editor
* [1060959](https://bugzilla.redhat.com/show_bug.cgi?id=1060959) - Use of "alternately" instead of "alternatively" in confirmation emails
* [1060970](https://bugzilla.redhat.com/show_bug.cgi?id=1060970) - Project-version information gathered for group join request not delivered
* [1060973](https://bugzilla.redhat.com/show_bug.cgi?id=1060973) - Deselecting the project version from a group add request results a non-error notification
* [1060987](https://bugzilla.redhat.com/show_bug.cgi?id=1060987) - No success/failure response for requesting add project to group from the group page
* [1062011](https://bugzilla.redhat.com/show_bug.cgi?id=1062011) - Overall Statistics show incorrect number of translators.
* [1063118](https://bugzilla.redhat.com/show_bug.cgi?id=1063118) - "Sort" drop down in group page is not correct
* [1064628](https://bugzilla.redhat.com/show_bug.cgi?id=1064628) - In Editor's Document list View, statistics are not updated immediately
* [1064737](https://bugzilla.redhat.com/show_bug.cgi?id=1064737) - Statistics on locale documents page are incorrect (inconsistent with project version and editor)
* [1065120](https://bugzilla.redhat.com/show_bug.cgi?id=1065120) - [Project Version JSF Document List View] Estimated work hours should stay the same by toggling between "By Message" and "By Words"
* [1067266](https://bugzilla.redhat.com/show_bug.cgi?id=1067266) - [Regression] CopyTrans via web UI causes an exception
* [1054524](https://bugzilla.redhat.com/show_bug.cgi?id=1054524) - Users api key is accessible by anyone
* [1056849](https://bugzilla.redhat.com/show_bug.cgi?id=1056849) - Incorrect group l10n statistics due to caching missing out update(s)
* [1059483](https://bugzilla.redhat.com/show_bug.cgi?id=1059483) - Cannot log into kerberos
* [1060598](https://bugzilla.redhat.com/show_bug.cgi?id=1060598) - [Regression] Obsolete projects are searchable, but not accessible (exception occurs)
* [1064106](https://bugzilla.redhat.com/show_bug.cgi?id=1064106) - Copy Trans times out with large enough documents
* [1065806](https://bugzilla.redhat.com/show_bug.cgi?id=1065806) - [Project Version JSF Language List View] After toggle the unit of status, spinner failed to be removed after statistics are updated
* [1060628](https://bugzilla.redhat.com/show_bug.cgi?id=1060628) - Admin manage search page has an empty "Current Progress" section
* [1013419](https://bugzilla.redhat.com/show_bug.cgi?id=1013419) - FAQ missing on translate.zanata.org
* [1056866](https://bugzilla.redhat.com/show_bug.cgi?id=1056866) - Error message should be shown when uploading unsupported Open Document Format (e.g. fodt)
* [1057432](https://bugzilla.redhat.com/show_bug.cgi?id=1057432) - No indication of where HTML fits in the project types
* [968619](https://bugzilla.redhat.com/show_bug.cgi?id=968619) - Editor: Difficulty in placing cursor at desired point and selecting exact part of text
* [1002792](https://bugzilla.redhat.com/show_bug.cgi?id=1002792) - Unhandled exception: Uploading an invalid .pot will result in WebApplicationException
* [1012502](https://bugzilla.redhat.com/show_bug.cgi?id=1012502) - server should never store fuzzy flag against a source document's textflows
* [1037932](https://bugzilla.redhat.com/show_bug.cgi?id=1037932) - Unhandled exception: Add language field allows more character than the database does (255)
* [1037933](https://bugzilla.redhat.com/show_bug.cgi?id=1037933) - Unhandled exception: Add language with a string too large can cause a lock exception on save
* [1043330](https://bugzilla.redhat.com/show_bug.cgi?id=1043330) - Existing OpenId user cannot save any setting from the Edit profile view
* [1055790](https://bugzilla.redhat.com/show_bug.cgi?id=1055790) - Unhelpful error code returned for incorrect html type
* [1056308](https://bugzilla.redhat.com/show_bug.cgi?id=1056308) - User edit profile page missing field validation for empty email address
* [1060621](https://bugzilla.redhat.com/show_bug.cgi?id=1060621) - [Regression] Validation warnings panel not displayed
* [1044768](https://bugzilla.redhat.com/show_bug.cgi?id=1044768) - Zanata does not pull the latest changes in translation due to unchanged ETags
* [1063112](https://bugzilla.redhat.com/show_bug.cgi?id=1063112) - Client push in dryRun mode should not invoke CopyTrans
* [1069428](https://bugzilla.redhat.com/show_bug.cgi?id=1069428) - Various concurrency problems due to unsafe Seam injections
