##### New Editor (Alpha)

[1088137](https://bugzilla.redhat.com/show_bug.cgi?id=1088137) - Translation Editor: Alpha 1 Prototype

The editor prototype can be accessed via the **(Try the new alpha editor)** button at the top of the regular editor.  It showcases the look and feel, workflow and intended direction of Zanata.

As it is a _prototype_, there are bound to be some bugs and sub-optimal behaviours - any suggestions or reports can be forwarded to our [bug tracker](https://bugzilla.redhat.com/enter_bug.cgi?product=Zanata).
* [1150373](https://bugzilla.redhat.com/show_bug.cgi?id=1150373) - Keyboard shortcuts
* [1172437](https://bugzilla.redhat.com/show_bug.cgi?id=1172437) - Add plurals to the new editor
* [1174071](https://bugzilla.redhat.com/show_bug.cgi?id=1174071) - [SPA editor] Save on Invalid entry should not cause NullPointerException

##### Infrastructure Changes

Zanata now requires JMS to be configured in standalone.xml in order to queue up some messages going out of the system. For instructions on how to do this, please [See Here](configuration/jms-messaging)

##### Drupal Plugin
* [1078009](https://bugzilla.redhat.com/show_bug.cgi?id=1078009) -  PROTOTYPE: Drupal plugin to push and pull Zanata translations
* [1148233](https://bugzilla.redhat.com/show_bug.cgi?id=1148233) -  RFE: Drupal plugin should split content into meaningful chunks
* [1150336](https://bugzilla.redhat.com/show_bug.cgi?id=1150336) -  RFE: Document Drupal plugin manual installation method

##### New Features
* [1044261](https://bugzilla.redhat.com/show_bug.cgi?id=1044261) - Drupal integration with Zanata
* [1066780](https://bugzilla.redhat.com/show_bug.cgi?id=1066780) - RFE: Improve the project list page [proto]
* [1127066](https://bugzilla.redhat.com/show_bug.cgi?id=1127066) - Copy Version button on project version listing
* [1162383](https://bugzilla.redhat.com/show_bug.cgi?id=1162383) - Updated pages in Administration section
* [1120457](https://bugzilla.redhat.com/show_bug.cgi?id=1120457) - Email notify the user when the language team permissions change
* [1139950](https://bugzilla.redhat.com/show_bug.cgi?id=1139950) - Flexible Translation file naming
* [1092193](https://bugzilla.redhat.com/show_bug.cgi?id=1092193) - Individual Translator Statistics
* [1127056](https://bugzilla.redhat.com/show_bug.cgi?id=1127056) - Migration Guide for community users
* [1122776](https://bugzilla.redhat.com/show_bug.cgi?id=1122776) - WebHooks callback API
* [1186951](https://bugzilla.redhat.com/show_bug.cgi?id=1186951) - Zanata Overlay module


##### Bugfixes
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