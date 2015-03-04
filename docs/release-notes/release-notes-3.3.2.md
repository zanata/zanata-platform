##### Infrastructure change
* Now requires (i.e. is tested on) [JBoss EAP](http://www.jboss.org/products/eap) 6.2.0 instead of 6.1.1

##### New Features
* [978072](https://bugzilla.redhat.com/show_bug.cgi?id=978072) - RFE: support roff as an input/output format
    * This feature is implemented on the client side only with [1038449 - command hook](https://bugzilla.redhat.com/show_bug.cgi?id=1038449). Users who wish to push .roff file can use a command hook to invoke external tool (po4a) before push to convert .roff into .pot. Invoke po4a again after pull to convert translated .po into .roff. 

* [1036435](https://bugzilla.redhat.com/show_bug.cgi?id=1036435) - RFE: Upgrade to Liquibase 3.x
* [980670](https://bugzilla.redhat.com/show_bug.cgi?id=980670) - [RFE] Add HTML as an input method to be translated
    * .html and .htm files are now supported in Zanata for translation.

* [1067253](https://bugzilla.redhat.com/show_bug.cgi?id=1067253) - RFE:/Tech Debt - Propagate translation done by upload and copyTrans to editor
    * Prior to this implementation, editor will not receive translation updates done by CopyTrans or REST, i.e. upload translation file though web UI or push from client. Now translation done by any of the above will be broadcast to any open editors. This includes latest translation and statistics.

* [1002378](https://bugzilla.redhat.com/show_bug.cgi?id=1002378) - RFE: Introduce a modular translation structure, and gwt generate the *Messages.properties files
    * Now Zanata editor is ready to be translated. See [[Localize Zanata]] for detail.

##### Bug fixes
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