## 4.1.0
### Breaking changes
New Zanata instances will now block anonymous users from accessing any resources. 
For existing instances, anonymous users can still access read-only resources
including web pages and REST resources. To change the behavior, admin user
can sign in and go to server configuration page to change the value there.

## 4.0.0
##### Infrastructure Changes
* Recommended platform: JBoss EAP 7 (7.0.1.GA or later).
* Alternative platform: WildFly version 10.x.

Zanata now requires EAP 7 or WildFly 10 with no extra modules.

If using EAP, you must upgrade to EAP 7 and apply the various Zanata
changes to `JBOSS_HOME/standalone/configuration/standalone.xml`.

If using WildFly, you must remove any previously installed JSF or Hibernate
modules from `WILDFLY_HOME/modules/{com,javax,org}`.

Also, Hibernate Search and Lucene have been upgraded - before starting
Zanata 4.0 you will need to clear your index directory (as configured
in the system property `hibernate.search.default.indexBase`) and then
trigger a complete re-index from the admin pages once Zanata has loaded.

Zanata's jboss-web.xml has been updated: if you maintain a custom version, you should remove the "valve" section.

##### Migration Guide
From version 4.0.0, Zanata platform depends on JBossEAP 7 or Wildfly 10, thus please refer
[JBoss EAP 7 Migration Guide](https://access.redhat.com/documentation/en/red-hat-jboss-enterprise-application-platform/7.0/single/migration-guide/)
or [Migrate my appliction from AS7 to WildFly](https://docs.jboss.org/author/display/WFLY10/How+do+I+migrate+my+application+from+AS7+to+WildFly) to migrate your configuration.

We use system properties instead of JNDI from 4.0.0,
thus, following configuration became obsolete and deployment will abort on presence of these obsolete JNDI entries
```xml
<subsystem xmlns="urn:jboss:domain:naming:1.3">
  <bindings>
    <simple name="java:global/zanata/security/auth-policy-names/kerberos" value="zanata.kerberos"/>
    <simple name="java:global/zanata/security/admin-users" value="admin"/>
    <simple name="java:global/zanata/files/document-storage-directory" value="/var/lib/zanata/files"/>
    <simple name="java:global/zanata/email/default-from-address" value="{{ zanata_noreply_address }}"/>
    <simple name="java:global/zanata/security/auth-policy-names/internal" value="zanata.internal"/>
    <simple name="java:global/zanata/security/auth-policy-names/jaas" value="zanata.jaas"/>
    <simple name="java:global/zanata/security/auth-policy-names/openid" value="zanata.openid"/>
    <simple name="java:global/zanata/smtp/port" value="${smtp.port,env.SMTP_PORT:2552}" />
    <lookup name="java:jboss/exported/zanata/files/document-storage-directory"
      lookup="java:global/zanata/files/document-storage-directory"/>
  </bindings>
  <remote-naming/>
</subsystem>
```
Settings have moved to `<system-properties>` section.

Zanata aborts deployment when *required* properties are absent or
*obsolete* properties are present in `standalone.xml`.

###### Required System Properties and Directory Paths
**All** of the following properties are required in `<system-properties>` section:

* `<property name="hibernate.search.default.indexBase" value="${jboss.server.data.dir}/zanata/indexes"/>`
* `<property name="javamelody.storage-directory" value="${jboss.server.data.dir}/zanata/stats"/>`
* `<property name="zanata.file.directory" value="${jboss.server.data.dir}/zanata/files"/>`

###### Required Auth Policy
**Exactly one** auth policy is required in `<system-properties>` section. The only exception is combining `internal` and `openid`.

 * `<property name="zanata.security.authpolicy.internal" value="zanata.internal"/>`
 * `<property name="zanata.security.authpolicy.jass" value="zanata.jaas"/>`
 * `<property name="zanata.security.authpolicy.kerberos" value="zanata.kerberos"/>`
 * `<property name="zanata.security.authpolicy.openid" value="zanata.openid"/>`

###### Obsolete Properties
**None** of the following properties should present, otherwise the deployment will abort.
 * `<property name="ehcache.disk.store.dir" value=.../>`

Directory of `ehcache.disk.store.dir` needs to be cleaned.

##### Changes
 * [ZNTA-1557](https://zanata.atlassian.net/browse/ZNTA-1557) - Zanata should abort deployment if required system properties missing
 * [ZNTA-1516](https://zanata.atlassian.net/browse/ZNTA-1516) - Update Zanata-cli documentation link in readthedoc release
 * [ZNTA-1494](https://zanata.atlassian.net/browse/ZNTA-1494) - Search in admin user management bottom page is wrong
 * [ZNTA-1480](https://zanata.atlassian.net/browse/ZNTA-1480) - Change url from /a/more to /info
 * [ZNTA-1478](https://zanata.atlassian.net/browse/ZNTA-1478) - Change path of frontend languages page to be non-hash path
 * [ZNTA-1463](https://zanata.atlassian.net/browse/ZNTA-1463) - Update shrinkwrap to make the deployment easier
 * [ZNTA-1462](https://zanata.atlassian.net/browse/ZNTA-1462) - Allow developers to use docker scripts with no prior docker knowledge.
 * [ZNTA-1459](https://zanata.atlassian.net/browse/ZNTA-1459) - Update readthedocs for Zanata-platform
 * [ZNTA-1452](https://zanata.atlassian.net/browse/ZNTA-1452) - Add option to skip recompiling frontend
 * [ZNTA-1449](https://zanata.atlassian.net/browse/ZNTA-1449) - Have gwt client code live in its own module
 * [ZNTA-1447](https://zanata.atlassian.net/browse/ZNTA-1447) - Update css for cancel and plus icon
 * [ZNTA-1445](https://zanata.atlassian.net/browse/ZNTA-1445) - Use jboss cli script for docker image generation
 * [ZNTA-1429](https://zanata.atlassian.net/browse/ZNTA-1429) - Update admin icons
 * [ZNTA-1413](https://zanata.atlassian.net/browse/ZNTA-1413) - Add Takari Maven extensions: Smart Builder, Lifecycle, OkHttp
 * [ZNTA-1394](https://zanata.atlassian.net/browse/ZNTA-1394) - Add draft profile to frontend build for faster builds
 * [ZNTA-1391](https://zanata.atlassian.net/browse/ZNTA-1391) - Merge zanata-parent,-api,-common,-client with zanata-server
 * [ZNTA-1352](https://zanata.atlassian.net/browse/ZNTA-1352) - Upgrade to Enunciate 2.x for REST API docs
 * [ZNTA-1337](https://zanata.atlassian.net/browse/ZNTA-1337) - Exclude node_modules in maven build in zanata-frontend
 * [ZNTA-1333](https://zanata.atlassian.net/browse/ZNTA-1333) - React-ify the Admin Languages Bootstrap page
 * [ZNTA-1279](https://zanata.atlassian.net/browse/ZNTA-1279) - Change frontend and editor path
 * [ZNTA-1271](https://zanata.atlassian.net/browse/ZNTA-1271) - Migrate Zanata client JAX-RS library back to resteasy
 * [ZNTA-1263](https://zanata.atlassian.net/browse/ZNTA-1263) - Replace icons in zanata-editor with zanata-ui
 * [ZNTA-1255](https://zanata.atlassian.net/browse/ZNTA-1255) - Enable DEBUG logging for Weld messages with string "Catching"
 * [ZNTA-1223](https://zanata.atlassian.net/browse/ZNTA-1223) - Move zanata-editor into frontend Maven module
 * [ZNTA-1222](https://zanata.atlassian.net/browse/ZNTA-1222) - Use Infinispan for Hibernate second level cache
 * [ZNTA-1220](https://zanata.atlassian.net/browse/ZNTA-1220) - Use the same dependency versions in frontend and zanata-editor
 * [ZNTA-1207](https://zanata.atlassian.net/browse/ZNTA-1207) - Support JBoss EAP 7 for Zanata Server
 * [ZNTA-1203](https://zanata.atlassian.net/browse/ZNTA-1203) - Remove Hibernate 4.2 module for WildFly
 * [ZNTA-1193](https://zanata.atlassian.net/browse/ZNTA-1193) - Merge Zanata-spa into Zanata-server
 * [ZNTA-1168](https://zanata.atlassian.net/browse/ZNTA-1168) - RFE: User should be able to select the glossary page length
 * [ZNTA-1167](https://zanata.atlassian.net/browse/ZNTA-1167) - Include lastModifiedBy information in glossary entry
 * [ZNTA-1166](https://zanata.atlassian.net/browse/ZNTA-1166) - Feature Request (Fedora Hub): Aditional webhooks
 * [ZNTA-1163](https://zanata.atlassian.net/browse/ZNTA-1163) - Convert user profile page to use redux
 * [ZNTA-1160](https://zanata.atlassian.net/browse/ZNTA-1160) - RFE: Test button for project webhooks
 * [ZNTA-1156](https://zanata.atlassian.net/browse/ZNTA-1156) - Use embedded Infinispan, not platform module
 * [ZNTA-1154](https://zanata.atlassian.net/browse/ZNTA-1154) - Infinispan Cache Statistics Management
 * [ZNTA-1131](https://zanata.atlassian.net/browse/ZNTA-1131) - Rename system property zanata.originWhitelist to zanata.origin.whitelist
 * [ZNTA-1130](https://zanata.atlassian.net/browse/ZNTA-1130) - Remove replyTo email in contact language team member email
 * [ZNTA-1101](https://zanata.atlassian.net/browse/ZNTA-1101) - Project-level glossaries
 * [ZNTA-1090](https://zanata.atlassian.net/browse/ZNTA-1090) - Allow REST authentication via OAuth
 * [ZNTA-1085](https://zanata.atlassian.net/browse/ZNTA-1085) - migrate opencsv library to apache-common-csv
 * [ZNTA-1059](https://zanata.atlassian.net/browse/ZNTA-1059) - Allow language team coordinator to contact all team members
 * [ZNTA-1045](https://zanata.atlassian.net/browse/ZNTA-1045) - Eliminate JNDI settings for document-storage-directory
 * [ZNTA-1042](https://zanata.atlassian.net/browse/ZNTA-1042) - Add option for cross-origin requests with credentials
 * [ZNTA-1023](https://zanata.atlassian.net/browse/ZNTA-1023) - Client should use file types from server
 * [ZNTA-958](https://zanata.atlassian.net/browse/ZNTA-958) - Integrate sidebar
 * [ZNTA-942](https://zanata.atlassian.net/browse/ZNTA-942) - Zanata Sync authentication prototype
 * [ZNTA-938](https://zanata.atlassian.net/browse/ZNTA-938) - Webhook event for translation updated
 * [ZNTA-858](https://zanata.atlassian.net/browse/ZNTA-858) - Add new side menu bar to the server
 * [ZNTA-841](https://zanata.atlassian.net/browse/ZNTA-841) - Remove Angular from the zanata-spa build
 * [ZNTA-840](https://zanata.atlassian.net/browse/ZNTA-840) - Replace Angular ajax calls with an equivalent framework-agnostic library
 * [ZNTA-839](https://zanata.atlassian.net/browse/ZNTA-839) - Replace all Angular services with async actions (redux-thunk-middleware)
 * [ZNTA-838](https://zanata.atlassian.net/browse/ZNTA-838) - Move service state into the redux state tree
 * [ZNTA-837](https://zanata.atlassian.net/browse/ZNTA-837) - Combine the 3 redux stores (header, content, suggestions) to a single store
 * [ZNTA-836](https://zanata.atlassian.net/browse/ZNTA-836) - Switch to redux to handle all state and events for document content panel
 * [ZNTA-835](https://zanata.atlassian.net/browse/ZNTA-835) - Create React component tree for all UI in document content panel
 * [ZNTA-834](https://zanata.atlassian.net/browse/ZNTA-834) - Code review and ensure full test coverage
 * [ZNTA-746](https://zanata.atlassian.net/browse/ZNTA-746) - Add a shortcut key for approving and rejecting translations
 * [ZNTA-689](https://zanata.atlassian.net/browse/ZNTA-689) - Support Qt TS files
 * [ZNTA-685](https://zanata.atlassian.net/browse/ZNTA-685) - Migrate document content (trans units) from AngularJS to ReactJS
 * [ZNTA-667](https://zanata.atlassian.net/browse/ZNTA-667) - JSON file support
 * [ZNTA-509](https://zanata.atlassian.net/browse/ZNTA-509) - RFE: The table search 'x' button is in a funny place, put it next to 'Cancel'?
 * [ZNTA-52](https://zanata.atlassian.net/browse/ZNTA-52) - [RFE] Allow admin to change native name of language

##### Bug Fixes
 * [ZNTA-1615](https://zanata.atlassian.net/browse/ZNTA-1615) - Unable to start Zanata if email log is enabled
 * [ZNTA-1594](https://zanata.atlassian.net/browse/ZNTA-1594) - Client nullpointer exception on delete all glossary
 * [ZNTA-1593](https://zanata.atlassian.net/browse/ZNTA-1593) - Add new language pop-up shows previously added language details.
 * [ZNTA-1580](https://zanata.atlassian.net/browse/ZNTA-1580) - Buttons on delete glossary pop-up are not aligned
 * [ZNTA-1564](https://zanata.atlassian.net/browse/ZNTA-1564) - There should be a proper error message shown for client request fields.
 * [ZNTA-1563](https://zanata.atlassian.net/browse/ZNTA-1563) - There is no email validation in server configuration->email log
 * [ZNTA-1561](https://zanata.atlassian.net/browse/ZNTA-1561) - Join a language team is not working from User Settings -> languages.
 * [ZNTA-1560](https://zanata.atlassian.net/browse/ZNTA-1560) - Repository url text is getting cut.
 * [ZNTA-1559](https://zanata.atlassian.net/browse/ZNTA-1559) - Dashboard and Setting tabs from left panel both are highlighted at a time.
 * [ZNTA-1558](https://zanata.atlassian.net/browse/ZNTA-1558) - Cancel button on po-ups of Find user to add, Contact coordinator,Contact team members, Add team member are not properly aligned
 * [ZNTA-1556](https://zanata.atlassian.net/browse/ZNTA-1556) - Show Transunit Details option not working
 * [ZNTA-1552](https://zanata.atlassian.net/browse/ZNTA-1552) - Obsolete JNDI setting should return Error and stop service
 * [ZNTA-1551](https://zanata.atlassian.net/browse/ZNTA-1551) - Groups should be clickable to go back to the group list from group page.
 * [ZNTA-1550](https://zanata.atlassian.net/browse/ZNTA-1550) - While adding projects to the group it shows project-id_Version instead of project-name_version   
 * [ZNTA-1546](https://zanata.atlassian.net/browse/ZNTA-1546) - Projects should have to be clickable for going back to the projects list from project version page..
 * [ZNTA-1545](https://zanata.atlassian.net/browse/ZNTA-1545) - Edit entry link for glossary from webtrans broken
 * [ZNTA-1544](https://zanata.atlassian.net/browse/ZNTA-1544) - Glossary Select Language list not always showing correct count
 * [ZNTA-1540](https://zanata.atlassian.net/browse/ZNTA-1540) - Created glossary entry shows "Invalid Date" on info
 * [ZNTA-1537](https://zanata.atlassian.net/browse/ZNTA-1537) - After successfull sign-up process page should redirect to the login page.
 * [ZNTA-1536](https://zanata.atlassian.net/browse/ZNTA-1536) - Download translation pop-up  shows mixes/scattered cancel and download buttons.
 * [ZNTA-1530](https://zanata.atlassian.net/browse/ZNTA-1530) - Project Glossary page shows project id instead of project name.
 * [ZNTA-1518](https://zanata.atlassian.net/browse/ZNTA-1518) - Notification message/pop-up shows in bulk or multiple times when we enable/disable multiple languages at a time.
 * [ZNTA-1517](https://zanata.atlassian.net/browse/ZNTA-1517) - Cancel and Ok/import button is not properly alligned on import,Export and Delete TM Pop-up
 * [ZNTA-1514](https://zanata.atlassian.net/browse/ZNTA-1514) - Export TM pop-up/dialog box is not proper.
 * [ZNTA-1513](https://zanata.atlassian.net/browse/ZNTA-1513) - 403 forbidden on glossary selection from project.
 * [ZNTA-1511](https://zanata.atlassian.net/browse/ZNTA-1511) - Japanese plural translations not showing up in React editor
 * [ZNTA-1507](https://zanata.atlassian.net/browse/ZNTA-1507) - Heap dump from javamelody triggers class not found exception
 * [ZNTA-1502](https://zanata.atlassian.net/browse/ZNTA-1502) - Dashboard and Profile menu on left manu panel are not focused when we click on it.
 * [ZNTA-1501](https://zanata.atlassian.net/browse/ZNTA-1501) - We are not able to make page content blank once text added/updated.
 * [ZNTA-1474](https://zanata.atlassian.net/browse/ZNTA-1474) - release-plugin does not do well with properties variable substitution
 * [ZNTA-1473](https://zanata.atlassian.net/browse/ZNTA-1473) - mvn -Dgwt-i18n process-test-resources: GWT Module org.zanata.webtrans.ApplicationI18n not found in project sources or resources
 * [ZNTA-1472](https://zanata.atlassian.net/browse/ZNTA-1472) - mvn site:site Exit code: 1 - javadoc: error - com.sun.tools.doclets.internal.toolkit.util.DocletAbortException
 * [ZNTA-1464](https://zanata.atlassian.net/browse/ZNTA-1464) - No error checking on push option
 * [ZNTA-1451](https://zanata.atlassian.net/browse/ZNTA-1451) - project type md file should reference documents md file for file type project
 * [ZNTA-1442](https://zanata.atlassian.net/browse/ZNTA-1442) - zanata client does not handle 404 properly when the code is designed to ignore it
 * [ZNTA-1437](https://zanata.atlassian.net/browse/ZNTA-1437) - Zanata docker image can not send emails
 * [ZNTA-1427](https://zanata.atlassian.net/browse/ZNTA-1427) - No way to override Zanata client cache file location 
 * [ZNTA-1406](https://zanata.atlassian.net/browse/ZNTA-1406) - Link to glossary from editor does not work
 * [ZNTA-1363](https://zanata.atlassian.net/browse/ZNTA-1363) - Loading cachestats page triggers IndexOutOfBoundsException
 * [ZNTA-1348](https://zanata.atlassian.net/browse/ZNTA-1348) - Bad redirect when coming from a translate action
 * [ZNTA-1340](https://zanata.atlassian.net/browse/ZNTA-1340) - Fedora platform - issue with dswid in URL
 * [ZNTA-1298](https://zanata.atlassian.net/browse/ZNTA-1298) - REST Api documentations is not update
 * [ZNTA-1278](https://zanata.atlassian.net/browse/ZNTA-1278) - Javascript alert failed functional test
 * [ZNTA-1253](https://zanata.atlassian.net/browse/ZNTA-1253) - escape lucene key words so that it can parse the string
 * [ZNTA-1248](https://zanata.atlassian.net/browse/ZNTA-1248) - Backwards compatibility broken for fileTypes parameter
 * [ZNTA-1230](https://zanata.atlassian.net/browse/ZNTA-1230) - Extract general components from frontend to zanata-ui
 * [ZNTA-1213](https://zanata.atlassian.net/browse/ZNTA-1213) - Profile, Glossary page not loading
 * [ZNTA-1199](https://zanata.atlassian.net/browse/ZNTA-1199) - Suggestion panel flicker when css transform
 * [ZNTA-1183](https://zanata.atlassian.net/browse/ZNTA-1183) - Loading page forever for empty glossary 
 * [ZNTA-1172](https://zanata.atlassian.net/browse/ZNTA-1172) - Admin: Failed to delete user
 * [ZNTA-1164](https://zanata.atlassian.net/browse/ZNTA-1164) - Nothing happened when clicking the menu item when deploy as ROOT.war
 * [ZNTA-1140](https://zanata.atlassian.net/browse/ZNTA-1140) - Editor UI not complete after sidebar update
 * [ZNTA-1138](https://zanata.atlassian.net/browse/ZNTA-1138) - Can attempt to delete the same glossary entry twice
 * [ZNTA-1136](https://zanata.atlassian.net/browse/ZNTA-1136) - Rapidly scrolling the Glossary page can render empty
 * [ZNTA-1132](https://zanata.atlassian.net/browse/ZNTA-1132) - Qt-ts failed to support context
 * [ZNTA-1129](https://zanata.atlassian.net/browse/ZNTA-1129) - Milestone webhook fires too many when uploading translations
 * [ZNTA-1097](https://zanata.atlassian.net/browse/ZNTA-1097) - "Contact Coordinators+" option in team page to be hidden if not logged in
 * [ZNTA-1073](https://zanata.atlassian.net/browse/ZNTA-1073) - NullPointerException when trying to access non-existing project
 * [ZNTA-1051](https://zanata.atlassian.net/browse/ZNTA-1051) - Redirection after login doesn't pick up hash location in url 
 * [ZNTA-906](https://zanata.atlassian.net/browse/ZNTA-906) - NullPointerException in getLocaleAliasesByIteration
 * [ZNTA-895](https://zanata.atlassian.net/browse/ZNTA-895) - Glossary entries endpoint not working for application/xml
 * [ZNTA-872](https://zanata.atlassian.net/browse/ZNTA-872) - Superfluous 'Cancel' button on "New User" page
 * [ZNTA-855](https://zanata.atlassian.net/browse/ZNTA-855) - fedora.zanata.org allows FAS users to change their username
 * [ZNTA-846](https://zanata.atlassian.net/browse/ZNTA-846) - Group "request add project version" needs field limit
 * [ZNTA-773](https://zanata.atlassian.net/browse/ZNTA-773) - Documents list loader visible over header
 * [ZNTA-527](https://zanata.atlassian.net/browse/ZNTA-527) - UI waits unnecessarily for stats before displaying other info
 * [ZNTA-504](https://zanata.atlassian.net/browse/ZNTA-504) - Create Project description field validates only after user has pressed save
 * [ZNTA-459](https://zanata.atlassian.net/browse/ZNTA-459) - Proto: New Project creation - where's the button?
 * [ZNTA-448](https://zanata.atlassian.net/browse/ZNTA-448) - RFE: As a translator, I would like to have a direct link to the referencing entry in Translation Memory detail, so I can see the context of the translation in TM and translate more accurately
 * [ZNTA-421](https://zanata.atlassian.net/browse/ZNTA-421) - Reverse the compare order for TM diffs, remove strikeout
 * [ZNTA-388](https://zanata.atlassian.net/browse/ZNTA-388) - Invalid url validation on admin->server config Register url field
 * [ZNTA-369](https://zanata.atlassian.net/browse/ZNTA-369) - RFE: Search results page should include Projects, People, Groups
 * [ZNTA-311](https://zanata.atlassian.net/browse/ZNTA-311) - RFE: Zanata to handle English glossary translations and ordering
 * [ZNTA-289](https://zanata.atlassian.net/browse/ZNTA-289) - RFE: Add an indicator to the sorting when it can be sorted in asc or desc order
 * [ZNTA-236](https://zanata.atlassian.net/browse/ZNTA-236) - Cancel button on a glossary upload doesn't cancel
 * [ZNTA-134](https://zanata.atlassian.net/browse/ZNTA-134) - Long document names are not dealt with, and stretch outside the bounds of panels
 * [ZNTA-132](https://zanata.atlassian.net/browse/ZNTA-132) - "Rejected" text state breaks the new editor
 * [ZNTA-58](https://zanata.atlassian.net/browse/ZNTA-58) - Show current workspace info as title of browser tab in React editor
 * [ZNTA-47](https://zanata.atlassian.net/browse/ZNTA-47) - Have a consistent format for page titles
 * [ZNTA-44](https://zanata.atlassian.net/browse/ZNTA-44) - Java client is missing glossary commands
 * [ZNTA-27](https://zanata.atlassian.net/browse/ZNTA-27) - RFE: Autofocus on fields in create pages
 * [ZNTA-18](https://zanata.atlassian.net/browse/ZNTA-18) - Glossary push doesn't work if transLang not specified
 * [ZNTA-16](https://zanata.atlassian.net/browse/ZNTA-16) - Poor usability of history/bookmark feature
 * [ZNTA-7](https://zanata.atlassian.net/browse/ZNTA-7) - No notification on project settings updated

-----------------------

## 3.9.6
##### Bug Fixes
 * [ZNTA-1404](https://zanata.atlassian.net/browse/ZNTA-1404) - Incorrect email body when becoming a reviewer
 * [ZNTA-1400](https://zanata.atlassian.net/browse/ZNTA-1400) - CopyTrans "On project mismatch Don't Copy" does not work as expected
 * [ZNTA-1392](https://zanata.atlassian.net/browse/ZNTA-1392) - Cannot load document with comma separated docId in alpha editor
 * [ZNTA-1386](https://zanata.atlassian.net/browse/ZNTA-1386) - Piwik insertion code is outdated, slows page loading
 * [ZNTA-1384](https://zanata.atlassian.net/browse/ZNTA-1384) - UI opens "compare version" instead of Translation History

-----------------------

## 3.9.5
##### Bug Fixes
 * [ZNTA-1358](https://zanata.atlassian.net/browse/ZNTA-1358) - Memory leak causing OOM error in idle server
 * [ZNTA-1293](https://zanata.atlassian.net/browse/ZNTA-1293) - Log files full of NonexistentConversationException

-----------------------

## 3.9.4
##### Changes
 * [ZNTA-1286](https://zanata.atlassian.net/browse/ZNTA-1286) - Language/Dashboard: migrate js alert and js confirm to modal
 * [ZNTA-1285](https://zanata.atlassian.net/browse/ZNTA-1285) - Version pages: migrate js alert and js confirm to modal
 * [ZNTA-1284](https://zanata.atlassian.net/browse/ZNTA-1284) - Project pages: migrate js alert and js confirm to modal
 * [ZNTA-1283](https://zanata.atlassian.net/browse/ZNTA-1283) - Admin pages: Migrate js alert and js confirm to modal

##### Bug Fixes
 * [ZNTA-1323](https://zanata.atlassian.net/browse/ZNTA-1323) - Register with non ASCII full name, the name is messed
 * [ZNTA-1318](https://zanata.atlassian.net/browse/ZNTA-1318) - Need better exception handling for transaction errors
 * [ZNTA-1317](https://zanata.atlassian.net/browse/ZNTA-1317) - Sorting Projects list leads to "Transaction was rolled back in a different thread!" and many other errors

-----------------------

## 3.9.3
##### Bug Fixes
 * [ZNTA-608](https://zanata.atlassian.net/browse/ZNTA-608) - Error (exception) on project page after removing an alias

-----------------------

## 3.9.2
##### Bug Fixes
 * [ZNTA-1275](https://zanata.atlassian.net/browse/ZNTA-1275) - Fedora: login failed if name contains non-ascii characters
 * [ZNTA-1112](https://zanata.atlassian.net/browse/ZNTA-1112) - Yahoo login indicates "Login failed" despite login success
 * [ZNTA-1178](https://zanata.atlassian.net/browse/ZNTA-1178) - File type project can not be pulled using zanata cli

-----------------------

## 3.9.1
##### Changes
 * [ZNTA-1192](https://zanata.atlassian.net/browse/ZNTA-1192) - RFE: Scripts to build the Docker development images

##### Bug Fixes
 * [ZNTA-1182](https://zanata.atlassian.net/browse/ZNTA-1182) - Cannot login via kerberos as a new user
 * [ZNTA-1175](https://zanata.atlassian.net/browse/ZNTA-1175) - "Login" text not showing in single openid setup
 * [ZNTA-1174](https://zanata.atlassian.net/browse/ZNTA-1174) - Regression: cannot connect Fedora / OpenID account (NPE), various changes not saved
 * [ZNTA-1145](https://zanata.atlassian.net/browse/ZNTA-1145) - PO-Revision-Date is empty if all translations in document is by copyTrans

-----------------------

## 3.9.0
##### Infrastructure Changes
* System admin can set system property `zanata.enforce.matchingusernames` to enforce matching username to be used for new user registration.
* Zanata has eliminated all JNDI-based configuration and replaced it with system properties. Please see the following sections for how certain values are now configured:
  * (/user-guide/system-admin/configuration/installation)
  * (/user-guide/system-admin/configuration/authentication)
  * (/user-guide/system-admin/configuration/document-storage-directory)

##### New feature
* [ZNTA-746](https://zanata.atlassian.net/browse/ZNTA-746) - Add shortcut key for approve and reject translation
* [ZNTA-938](https://zanata.atlassian.net/browse/ZNTA-938) - Webhook event for translation update by user.
* [ZNTA-746](https://zanata.atlassian.net/browse/ZNTA-746) - Add shorcut key for approve and reject translation
* [ZNTA-1059](https://zanata.atlassian.net/browse/ZNTA-1059) - Language coordinator can contact team members
* [ZNTA-858](https://zanata.atlassian.net/browse/ZNTA-858) - New side menu bar replace top and bottom panel
* [ZNTA-855](https://zanata.atlassian.net/browse/ZNTA-855) - Add system property to enforce username for registration

##### Changes
 * [ZNTA-1028](https://zanata.atlassian.net/browse/ZNTA-1028) - Refactor QualifiedSrcDocName and UnqualifiedSrcDocName

-----------------------

## 3.9.0
<h5>Infrastructure Changes</h5>
* Recommended platform: JBoss EAP 6 (6.4.6.GA or later).
* Alternative platform: WildFly version 10.x.
* [ZNTA-530](https://zanata.atlassian.net/browse/ZNTA-530) - Replace Seam 2 with CDI
  * In WildFly or EAP `standalone.xml`, please make sure the Weld
    extension is present in the `extensions` section like this:

        <extensions>
            ...
            <extension module="org.jboss.as.weld" />
            ...
        </extensions>


  * Secondly, please ensure the Weld subsystem is present in the
    `profiles` section, eg like this:

        <profiles>
            ...
            <subsystem xmlns="urn:jboss:domain:weld:1.0" />
            ...
        </profiles>

##### Changes
 * [ZNTA-1067](https://zanata.atlassian.net/browse/ZNTA-1067) - Kerberos ticket authentication leads to mostly blank page
 * [ZNTA-1017](https://zanata.atlassian.net/browse/ZNTA-1017) - Refactor translation update event to use batches
 * [ZNTA-957](https://zanata.atlassian.net/browse/ZNTA-957) - User profile
 * [ZNTA-956](https://zanata.atlassian.net/browse/ZNTA-956) - Migrate glossary to redux
 * [ZNTA-939](https://zanata.atlassian.net/browse/ZNTA-939) - Update to WildFly 10 (ready for EAP 7)
 * [ZNTA-905](https://zanata.atlassian.net/browse/ZNTA-905) - 0% matching translation listed in TM
 * [ZNTA-894](https://zanata.atlassian.net/browse/ZNTA-894) - dswid is not unique after opening link in new tab (in projects list)
 * [ZNTA-892](https://zanata.atlassian.net/browse/ZNTA-892) - Performance tests for REST in Jenkins
 * [ZNTA-887](https://zanata.atlassian.net/browse/ZNTA-887) - Conversation scope message not displaying
 * [ZNTA-879](https://zanata.atlassian.net/browse/ZNTA-879) - Display user email based on admin configuration
 * [ZNTA-833](https://zanata.atlassian.net/browse/ZNTA-833) - Reduce JS runtime warnings and errors
 * [ZNTA-797](https://zanata.atlassian.net/browse/ZNTA-797) - Replace SeamAutowire with real CDI in tests
 * [ZNTA-793](https://zanata.atlassian.net/browse/ZNTA-793) - TranslationMemoryAction.lastTaskResult appears to be useless
 * [ZNTA-744](https://zanata.atlassian.net/browse/ZNTA-744) - Add review data to the contribution statistics API
 * [ZNTA-742](https://zanata.atlassian.net/browse/ZNTA-742) - Get a list of contributors for a Project version via the API
 * [ZNTA-699](https://zanata.atlassian.net/browse/ZNTA-699) - Migrate page navigation flow from Seam pages.xml to faces-config.xml
 * [ZNTA-684](https://zanata.atlassian.net/browse/ZNTA-684) - Migrate suggestions panel from AngularJS to ReactJS

##### Bug Fixes
 * [ZNTA-1128](https://zanata.atlassian.net/browse/ZNTA-1128) - Create version - entering a name before unchecking 'copy' breaks the list
 * [ZNTA-1125](https://zanata.atlassian.net/browse/ZNTA-1125) - TransactionRequiredException on enabling project Invite Only
 * [ZNTA-1120](https://zanata.atlassian.net/browse/ZNTA-1120) - Rest endpoint broken for alpha editor
 * [ZNTA-1119](https://zanata.atlassian.net/browse/ZNTA-1119) - Version copy started notification states source as null
 * [ZNTA-1116](https://zanata.atlassian.net/browse/ZNTA-1116) - ResourceExceptions when saving a server config
 * [ZNTA-1086](https://zanata.atlassian.net/browse/ZNTA-1086) - NullPointerException when uploading from client (tested with cs-CZ)
 * [ZNTA-1084](https://zanata.atlassian.net/browse/ZNTA-1084) - NoSuchElementException when uploading empty data
 * [ZNTA-1081](https://zanata.atlassian.net/browse/ZNTA-1081) - Should use @Synchronized for @SessionScoped beans
 * [ZNTA-1075](https://zanata.atlassian.net/browse/ZNTA-1075) - Occasional NullPointerException in PasswordUtil.generateSaltedHash
 * [ZNTA-1074](https://zanata.atlassian.net/browse/ZNTA-1074) - Glossary blurts entire exception stacktrace to user dialog
 * [ZNTA-1072](https://zanata.atlassian.net/browse/ZNTA-1072) - [Regression] Cannot set multiple roles for a user
 * [ZNTA-1068](https://zanata.atlassian.net/browse/ZNTA-1068) - Adding multiple languages to a project does not update the page
 * [ZNTA-1066](https://zanata.atlassian.net/browse/ZNTA-1066) - Delete project still indicates "Obsolete" in the notification
 * [ZNTA-1065](https://zanata.atlassian.net/browse/ZNTA-1065) - Drop-down user menu does not obscure the Glossary buttons
 * [ZNTA-1060](https://zanata.atlassian.net/browse/ZNTA-1060) - Unable to upload translations on Wildfly 10
 * [ZNTA-1013](https://zanata.atlassian.net/browse/ZNTA-1013) - Merge translation triggers exception when creating new TextFlowTarget
 * [ZNTA-991](https://zanata.atlassian.net/browse/ZNTA-991) - Cannot change email addresses in app config (ClassNotFoundException)
 * [ZNTA-981](https://zanata.atlassian.net/browse/ZNTA-981) - Uploading a .pot file via Web UI retains the extension
 * [ZNTA-931](https://zanata.atlassian.net/browse/ZNTA-931) - Reset password feature fails on session closed
 * [ZNTA-928](https://zanata.atlassian.net/browse/ZNTA-928) - Readonly project doesn't have "lock" icon in UI
 * [ZNTA-871](https://zanata.atlassian.net/browse/ZNTA-871) - Changing user email address redirects to invalid page
 * [ZNTA-870](https://zanata.atlassian.net/browse/ZNTA-870) - Reset Password feature not inserting key into database
 * [ZNTA-850](https://zanata.atlassian.net/browse/ZNTA-850) - org.zanata.async.AsyncTaskManager:  Exception when executing an asynchronous task.
 * [ZNTA-804](https://zanata.atlassian.net/browse/ZNTA-804) - Coordinators' email addresses should be BCC in Contact Coordinator
 * [ZNTA-693](https://zanata.atlassian.net/browse/ZNTA-693) - ClientAbortException: java.net.SocketException: Connection reset
 * [ZNTA-668](https://zanata.atlassian.net/browse/ZNTA-668) - ServerConfiguration tests unstable
 * [ZNTA-537](https://zanata.atlassian.net/browse/ZNTA-537) - Expired sessions have poor usability
 * [ZNTA-470](https://zanata.atlassian.net/browse/ZNTA-470) - RFE: Prevent user sending large text in 'contact admin' emails
 * [ZNTA-412](https://zanata.atlassian.net/browse/ZNTA-412) - RFE: Link displayed usernames to profile page
 * [ZNTA-393](https://zanata.atlassian.net/browse/ZNTA-393) - Redirect to a "good" page if URL is not known to zanata
 * [ZNTA-358](https://zanata.atlassian.net/browse/ZNTA-358) - Unable to sign up with user specified OpenID
 * [ZNTA-54](https://zanata.atlassian.net/browse/ZNTA-54) - Can't remove languages
 * [ZNTA-12](https://zanata.atlassian.net/browse/ZNTA-12) - User cannot send two messages to admin in a row
<h5>New Features</h5>
* [ZNTA-689](https://zanata.atlassian.net/browse/ZNTA-689) - Support Qt TS files

<h5>Bug fixes</h5>
* [ZNTA-804](https://zanata.atlassian.net/browse/ZNTA-804) - Coordinators' email addresses should be BCC in Contact Coordinator
* [ZNTA-693](https://zanata.atlassian.net/browse/ZNTA-693) - Handle ClientAbortException exception and reduce severity.
* [ZNTA-742](https://zanata.atlassian.net/browse/ZNTA-742) - Get a list of contributors for a Project version via the API
* [ZNTA-744](https://zanata.atlassian.net/browse/ZNTA-744) - Add review data to the contribution statistics API
* [ZNTA-879](https://zanata.atlassian.net/browse/ZNTA-879) - Allow admin to configure the visibility of user email
* [ZNTA-412](https://zanata.atlassian.net/browse/ZNTA-412) - Profile link to project maintainers, language members, and version group maintainers
* [ZNTA-905](https://zanata.atlassian.net/browse/ZNTA-905) - Remove 0% matching translation memory entry
* [ZNTA-928](https://zanata.atlassian.net/browse/ZNTA-928) - Readonly project doesn't have "lock" icon in UI
* [ZNTA-54](https://zanata.atlassian.net/browse/ZNTA-54) - Allow delete language
* [ZNTA-1066](https://zanata.atlassian.net/browse/ZNTA-1066) - Delete project still indicates "Obsolete" in the notification
* [ZNTA-1068](https://zanata.atlassian.net/browse/ZNTA-1068) - Refresh page after adding language in project page
* [ZNTA-1074](https://zanata.atlassian.net/browse/ZNTA-1074) - Hide notification details section
* [ZNTA-1065](https://zanata.atlassian.net/browse/ZNTA-1065) - Drop-down user menu does not obscure the Glossary buttons
* [ZNTA-1086](https://zanata.atlassian.net/browse/ZNTA-1086) - NullPointerException when uploading from client (tested with cs-CZ)

* [ZNTA-981](https://zanata.atlassian.net/browse/ZNTA-981) - Remove file extension for gettext project file type

<h5>Infrastructure Changes</h5>
* Recommended platform: JBoss EAP 6 (6.4.6.GA or later).
* Alternative platform: WildFly version 10.x.

-----------------------

## 3.8.4
##### Changes
* [ZNTA-1000](https://zanata.atlassian.net/browse/ZNTA-1000) - Try catch finally on copy version action, logging exception
* [ZNTA-1001](https://zanata.atlassian.net/browse/ZNTA-1001) - Investigate and fix: copy version fails silently after triggering


##### Bug fixes
* [ZNTA-959](https://zanata.atlassian.net/browse/ZNTA-959) - Copy version fails silently and leaves version readonly
* [ZNTA-1013](https://zanata.atlassian.net/browse/ZNTA-1013) - Fix Merge translation exception
* [ZNTA-1011](https://zanata.atlassian.net/browse/ZNTA-1011) - Zanata client 3.8.3 can't push file to server running on WildFly
* [ZNTA-125](https://zanata.atlassian.net/browse/ZNTA-125) - zanata-cli on Fedora for xliff project has NullPointerException during push when --validate is not specified


## 3.8.3
<h5>Bug fixes</h5>
* [ZNTA-953](https://zanata.atlassian.net/browse/ZNTA-953) - Fix delete version

## 3.8.2
<h5>Bug fixes</h5>
* [ZNTA-854](https://zanata.atlassian.net/browse/ZNTA-854) - Fast scrolling(infinite scroll) in glossary table not loading data
* [ZNTA-519](https://zanata.atlassian.net/browse/ZNTA-519) - Unable to push the POT file beginning from the underscore (_) in the filename
* [ZNTA-843](https://zanata.atlassian.net/browse/ZNTA-843) - Improve/fix 0install bug
* [ZNTA-901](https://zanata.atlassian.net/browse/ZNTA-901) - txw2 should be included in zanata-cli 0install
* [ZNTA-930](https://zanata.atlassian.net/browse/ZNTA-930) - Zanata init command fails when specifying &quot;file&quot; project type
* [ZNTA-937](https://zanata.atlassian.net/browse/ZNTA-937) - zanata-client: need better error message for unexpected html response

## 3.8.1
##### Changes
* Add a new parameter includeAutomatedEntry to API getContributionStatics
* Downgrade dependency to keep enforcer happy
* 0install feed for Zanata CLI has been migrated to https://raw.githubusercontent.com/zanata/zanata.github.io/master/files/0install/zanata-cli.xml. Please see [installation](/#installation) for updated command.

<h5>Bug fixes</h5>
* [ZNTA-844](https://zanata.atlassian.net/browse/ZNTA-844) - Merge Translations dialog broken


## 3.8.0
##### Highlight
* Copy Trans will no longer run by default. Option `--copy-trans` is now required to invoke copy trans when pushing

##### Infrastructure Changes
* In wildfly or EAP standalone.xml, change all occurrences of "org.jboss.seam.security.jaas.SeamLoginModule" to "org.zanata.security.jaas.InternalLoginModule"
* Zanata now requires JBoss EAP 6.4.2.GA or later (recommended), or WildFly version 9.x.
* Zanata now requires a Java 1.8 virtual machine.

<h5>Bug fixes</h5>
* [1235070](https://bugzilla.redhat.com/show_bug.cgi?id=1235070) - Copied translations (using copy trans) should not be taken into account as contributions in the stats
* [1004620](https://bugzilla.redhat.com/show_bug.cgi?id=1004620) - Import TM box should disable the Import button when no file specified
* [1002386](https://bugzilla.redhat.com/show_bug.cgi?id=1002386) - Capitalised proper nouns in project type description
* [1242679](https://bugzilla.redhat.com/show_bug.cgi?id=1242679) - Version entry does not update after copy version is completed
* [1243672](https://bugzilla.redhat.com/show_bug.cgi?id=1243672) - Source refs does not shows up in editor
* [1062476](https://bugzilla.redhat.com/show_bug.cgi?id=1062476) - User can attempt to upload an unspecified glossary file
* [1243251](https://bugzilla.redhat.com/show_bug.cgi?id=1243251) - Project search does not sort correctly by date over multiple pages
* [1243682](https://bugzilla.redhat.com/show_bug.cgi?id=1243682) - When uploading POT file using web UI, source comments are being ignored
* [1243688](https://bugzilla.redhat.com/show_bug.cgi?id=1243688) - Groups filter by archived not working
* [ZNTA-615](https://zanata.atlassian.net/browse/ZNTA-615) - Fix glossary permission
* [ZNTA-643](https://zanata.atlassian.net/browse/ZNTA-643) - Fix concurrent map modification issue in language page
* [ZNTA-624](https://zanata.atlassian.net/browse/ZNTA-624) - Increase password length to 1024.
* [ZNTA-9](https://zanata.atlassian.net/browse/ZNTA-9) - Account activation message not displaying
* [ZNTA-696](https://zanata.atlassian.net/browse/ZNTA-696) - Fix user service for null authenticated user
* [ZNTA-634](https://zanata.atlassian.net/browse/ZNTA-634) - Users being logged out after a short period of inactivity
* [ZNTA-719](https://zanata.atlassian.net/browse/ZNTA-719) - Translation merge targets the wrong source version
* [ZNTA-718](https://zanata.atlassian.net/browse/ZNTA-718) - Allow admin and project maintainer to merge translation from all active projects.
* [ZNTA-639](https://zanata.atlassian.net/browse/ZNTA-639) - Expired sessions causing ViewExpiredExceptions
* [ZNTA-720](https://zanata.atlassian.net/browse/ZNTA-720) - Redirect to login page after account activation
* [ZNTA-767](https://zanata.atlassian.net/browse/ZNTA-767) - File names / paths with spaces cannot be navigated to via the Documents tab. **Note:** This will invalidate all existing bookmarked URL with selected document in the project version page.
* [ZNTA-815](https://zanata.atlassian.net/browse/ZNTA-815) - New user login redirected to login screen again instead of Dashboard
* [ZNTA-657](https://zanata.atlassian.net/browse/ZNTA-657) - Add new language uses the search text instead of the selected locale
* [ZNTA-817](https://zanata.atlassian.net/browse/ZNTA-817) - Fix default terms of use url
* [ZNTA-816](https://zanata.atlassian.net/browse/ZNTA-816) - No success/confirmation message after updating email via Settings->Profile
* [ZNTA-811](https://zanata.atlassian.net/browse/ZNTA-811) - Downloaded translated .pot file has no extension
* [ZNTA-726](https://zanata.atlassian.net/browse/ZNTA-726) - leading/trailing newline validation should not check the line numbers
* [ZNTA-818](https://zanata.atlassian.net/browse/ZNTA-818) - 'SHOW' does not work while changing password in the Settings twice
* [ZNTA-813](https://zanata.atlassian.net/browse/ZNTA-813) - Add tooltip for disabled account
* [ZNTA-814](https://zanata.atlassian.net/browse/ZNTA-814) - handle enter key in inactive account page
* [ZNTA-812](https://zanata.atlassian.net/browse/ZNTA-812) - Project text search and replace does bad html safe encoding on search text
* [ZNTA-354](https://zanata.atlassian.net/browse/ZNTA-354) - Improve Zanata client installation documentation and workflow
* [ZNTA-664](https://zanata.atlassian.net/browse/ZNTA-664) - Change client push command not to run copy trans by default

-----------------------

<h5>New Features</h5>
* [1224912](https://bugzilla.redhat.com/show_bug.cgi?id=1224912) - Filter "Last modified by translators other than &lt;user&gt;"
* [1213630](https://bugzilla.redhat.com/show_bug.cgi?id=1213630) - Webhook header needs to include cryptographic signature in header for identification
* [1214502](https://bugzilla.redhat.com/show_bug.cgi?id=1214502) -  RFE: Grant project creation permission to certain sets of users
* [ZNTA-555](https://zanata.atlassian.net/browse/ZNTA-555) - Internal "user request management" and tracking
* [1214502](https://bugzilla.redhat.com/show_bug.cgi?id=1214502) - RFE: Grant project creation permission to certain sets of users
* [1233524](https://bugzilla.redhat.com/show_bug.cgi?id=1233524) - Update project search page to include user
* [ZNTA-108](https://zanata.atlassian.net/browse/ZNTA-108) - Improved glossary management: add, edit and delete individual glossary entries

-----------------------
## 3.7.3

<h5>Bug fixes</h5>
* [ZNTA-686](https://zanata.atlassian.net/browse/ZNTA-686) - Editor filter by last modified date uses wrong date pattern to validate
* [ZNTA-707](https://zanata.atlassian.net/browse/ZNTA-707) - mysql on Mac throws error migrating DB with indexname 'Idx_lastChanged' vs 'Idx_LastChanged'
* [ZNTA-721](https://zanata.atlassian.net/browse/ZNTA-721) - Fuzzy/automatic TM search not finding good/exact matches

## 3.7.2

<h5>Improvements</h5>
* [ZNTA-653](https://zanata.atlassian.net/browse/ZNTA-653) - Include MDC values in log emails
* [ZNTA-665](https://zanata.atlassian.net/browse/ZNTA-665) - liquibase merge addColumn changes
  It helps to reduce the database migration time.

<h5>Bug fixes</h5>
* [ZNTA-594](https://zanata.atlassian.net/browse/ZNTA-594) - Project-wide search and replace only shows 15 results per document
* [ZNTA-615](https://zanata.atlassian.net/browse/ZNTA-615) - Glossarist/-admin not getting glossary-update permission
* [ZNTA-643](https://zanata.atlassian.net/browse/ZNTA-643) - javax.servlet.ServletException: java.util.ConcurrentModificationException , Caused by: javax.faces.el.EvaluationException: java.util.ConcurrentModificationException

-----------------------

## 3.7.1

<h5>Bug fixes</h5>

* [1235070](https://bugzilla.redhat.com/show_bug.cgi?id=1235070) - Copied translations (using copy trans) should not be taken into account as contributions in the stats

-----------------------

## 3.7.0

(Non public release. Database migrations executed directly by this release will not be compatible with future releases.)

<h5>Deployment</h5>

* Deployment for this release may require a longer timeout due to underlying database schema changes and data migration. This is dependent on database size and system performance, and the system administrator should consider increasing the JBoss timeout value in standalone.xml.  This example sets a timeout of two hours, which should be more than enough:

        <system-properties>
            ...
            <property name="jboss.as.management.blocking.timeout" value="7200"/>
            ...
        </system-properties>


* The Zanata administrator will also need to reindex HProject table via the Administration menu. See [Manage search](user-guide/admin/manage-search) for more information.


<h5>Infrastructure Changes</h5>

* Zanata now uses Infinispan as its cache provider, and the cache needs to be configured in Jboss' `standalone.xml` file. Please see the [Infinispan](user-guide/system-admin/configuration/infinispan) section for more information.

* [1207423](https://bugzilla.redhat.com/show_bug.cgi?id=1207423) - zanata-assets(javascipts and css style) now are packaged as jar and is part of zanata-server dependency.
[Release](http://repository-zanata.forge.cloudbees.com/release/org/zanata/zanata-assets/) and [snapshot](http://repository-zanata.forge.cloudbees.com/snapshot/org/zanata/zanata-assets/)

zanata-assets is set to **http://{zanata.url}/javax.faces.resource/jars/assets** by default. You override the value by setting the system property `zanata.assets.url` when running the server.

Example usage in html file: `<link rel="shortcut icon" href="#{assets['img/logo/logo.ico']}"/>`

* [PR 633](https://github.com/zanata/zanata-server/pull/633) - Use JNDI to obtain mail server from app server
    * Zanata now uses `java:jboss/mail/Default` mail session for SMTP configuration.  See "Email configuration" in [System admin guide](http://docs.zanata.org/en/latest/user-guide/system-admin/configuration/installation/index.html) for details.


<h5>Bug fixes</h5>
* [1203521](https://bugzilla.redhat.com/show_bug.cgi?id=1203521) - Alpha Editor doesn't escape reserved characters
* [1194543](https://bugzilla.redhat.com/show_bug.cgi?id=1194543) - Manual document re-upload makes previous translations fuzzy
* [1029734](https://bugzilla.redhat.com/show_bug.cgi?id=1029734) - po header contains invalid entry will cause upload/push failure
* [895881](https://bugzilla.redhat.com/show_bug.cgi?id=895881) - 'Restore Defaults' in editor options does not properly restore defaults
* [1207426](https://bugzilla.redhat.com/show_bug.cgi?id=1207426) - Update request to join group/language page
* [1165939](https://bugzilla.redhat.com/show_bug.cgi?id=1165939) - The Groups actions panel should not show for a normal user
* [1205512](https://bugzilla.redhat.com/show_bug.cgi?id=1205512) - Run validation in editor document list is disabled when it should not be
* [903964](https://bugzilla.redhat.com/show_bug.cgi?id=903964) - Error message not propagated to client when push fails
* [1218002](https://bugzilla.redhat.com/show_bug.cgi?id=1218002) - Disable Google Open Id option
* [1222710](https://bugzilla.redhat.com/show_bug.cgi?id=1222710) - Editor option save fails due to ClassCastException
* [1222358](https://bugzilla.redhat.com/show_bug.cgi?id=1222358) - User profile page dropdown will not work in firefox
* [1207980](https://bugzilla.redhat.com/show_bug.cgi?id=1207980) - Split up large Liquibase changesets to avoid partial updates
* [1165930](https://bugzilla.redhat.com/show_bug.cgi?id=1165930) - 'Copy from previous version' shows if an obsolete version exists
* [1098362](https://bugzilla.redhat.com/show_bug.cgi?id=1098362) - download link in editor doesn't encode properly and result in 404
* [1225689](https://bugzilla.redhat.com/show_bug.cgi?id=1225689) - [Project Version View] Failed to load entries when the doc id contains characters that should be URL encoded
* [1098878](https://bugzilla.redhat.com/show_bug.cgi?id=1098878) - OpenID login with FAS does not direct to user dashboard
* [981498](https://bugzilla.redhat.com/show_bug.cgi?id=981498) - No underscore sanity checking on creating usernames
* [1147304](https://bugzilla.redhat.com/show_bug.cgi?id=1147304) - Project search fails on special characters
* [1123186](https://bugzilla.redhat.com/show_bug.cgi?id=1123186) - Project search fails for multiple word project names
* [1112498](https://bugzilla.redhat.com/show_bug.cgi?id=1112498) - Unable to remove self as maintainer
* [1227575](https://bugzilla.redhat.com/show_bug.cgi?id=1227575) - Exception on emptying the search field when many users were reported
* [1224030](https://bugzilla.redhat.com/show_bug.cgi?id=1224030) - Search form does not trigger search if paste text
* [1198898](https://bugzilla.redhat.com/show_bug.cgi?id=1198898) - Exception on using the URL to view a language not yet added to Zanata
* [1116172](https://bugzilla.redhat.com/show_bug.cgi?id=1116172) - Captcha no longer used, dead code still exists
* [1230419](https://bugzilla.redhat.com/show_bug.cgi?id=1230419) - Only show "approved" figure in version page if "Review required" is enable or value more than 0
* [1229940](https://bugzilla.redhat.com/show_bug.cgi?id=1229940) - When deleting a version or project remove links and replace icon from the activity feed
* [1230424](https://bugzilla.redhat.com/show_bug.cgi?id=1230424) - Update message "Archived" to "Deleted" in activity table
* [1231054](https://bugzilla.redhat.com/show_bug.cgi?id=1231054) - Exception when clicking "more activity" when there is no valid "editor url"
* [1234687](https://bugzilla.redhat.com/show_bug.cgi?id=1234687) - [REGRESSION] can not upload pot file from web UI
* [1235495](https://bugzilla.redhat.com/show_bug.cgi?id=1235495) - [REGRESSION] can not upload po file from web UI

-----------------------

<h5>New Features</h5>
* [1133989](https://bugzilla.redhat.com/show_bug.cgi?id=1133989) - Copy translations from existing version.
* [1186972](https://bugzilla.redhat.com/show_bug.cgi?id=1186972) - Server-side file conversion and REST service.
    * File project type now supports XLIFF, PROPERTIES, PROPERTIES_UTF8, and GETTEXT
* [1204982](https://bugzilla.redhat.com/show_bug.cgi?id=1204982) - Documentation update for zanata.org/help + readthedocs
* [1209670](https://bugzilla.redhat.com/show_bug.cgi?id=1209670) - Improve review statistics - approved vs translated
* [1211134](https://bugzilla.redhat.com/show_bug.cgi?id=1211134) - Review should be enabled in editor by default
* [1198433](https://bugzilla.redhat.com/show_bug.cgi?id=1198433) - Replace Seam Text with CommonMark Markdown
    * User text on the home page and project "about" pages will now be rendered as CommonMark.
    * Existing Seam Text will be migrated to CommonMark where possible.
* [1204982](https://bugzilla.redhat.com/show_bug.cgi?id=1204982) - Documentation update for zanata.org/help + readthedocs
* [1211849](https://bugzilla.redhat.com/show_bug.cgi?id=1211849) - Project maintainer can change project/version slug
* [1082840](https://bugzilla.redhat.com/show_bug.cgi?id=1082840) - Project maintainer can delete a project or project version
* [1209669](https://bugzilla.redhat.com/show_bug.cgi?id=1209669) - New REST endpoint for editor suggestions.

-----------------------


## 3.6.3

<h5>Infrastructure Changes</h5>

Zanata now requires JBoss EAP 6.4.0.GA or later (recommended), or WildFly version 9.0.0.CR1 or later.

The WildFly modules for Hibernate and Mojarra have been updated, to 4.2.19.Final and 2.1.29-01 respectively.  The Zanata installer includes the updated modules.

The Zanata installer's configuration now enables "connection debugging" to prevent any potential JDBC connection leaks.

-----------------------

<h5>New Features</h5>
* Added Liquibase logging (as part of [1207575](https://bugzilla.redhat.com/show_bug.cgi?id=1207575))

-----------------------

<h5>Bugfixes</h5>
* [1207575](https://bugzilla.redhat.com/show_bug.cgi?id=1207575) - Zanata still creates MyISAM (not InnoDB) tables in some cases
* [1197955](https://bugzilla.redhat.com/show_bug.cgi?id=1197955) - [WildFly] IllegalStateException: UT000010: Session not found
* [[1197955]](https://bugzilla.redhat.com/show_bug.cgi?id=1223597) - Statistic on last page in document list view in Editor always loading

-----------------------

## 3.6.2

<h5>Bugfixes</h5>
* [1206018](https://bugzilla.redhat.com/show_bug.cgi?id=1206018) - RichFaces: Remote Command Execution via insufficient EL parameter sanitization

-----------------------

## 3.6.1

<h5>Bug fixes</h5>
* [1194543](https://bugzilla.redhat.com/show_bug.cgi?id=1194543) - Manual document re-upload makes previous translations fuzzy
* [1197902](https://bugzilla.redhat.com/show_bug.cgi?id=1197902) - Large translated document push times are inconsistent
* [1183412](https://bugzilla.redhat.com/show_bug.cgi?id=1183412) - Emails to administrators are sent in the current interface language
* [1202670](https://bugzilla.redhat.com/show_bug.cgi?id=1202670) - There should be visual clues to indicate active, readonly, and archived versions.
* [875965](https://bugzilla.redhat.com/show_bug.cgi?id=875965) - Enable visible white space in source
* [1205465](https://bugzilla.redhat.com/show_bug.cgi?id=1205465) - User emails are visible to non admin users in Language page
* [1205468](https://bugzilla.redhat.com/show_bug.cgi?id=1205468) - Sorting mechanism broken on Languages page
* [1205046](https://bugzilla.redhat.com/show_bug.cgi?id=1205046) - Key shortcuts are not all visible on a small window
* [1000273](https://bugzilla.redhat.com/show_bug.cgi?id=1000273) - Font in TM and font in Editor Not matching
* [1013928](https://bugzilla.redhat.com/show_bug.cgi?id=1013928) - Editor options panel cannot scroll on small screens


<h5>New Features</h5>
* [1172618](https://bugzilla.redhat.com/show_bug.cgi?id=1172618) - Allow anonymous pull from Zanata

----

## 3.6

<h5>New Editor (Alpha)</h5>

[1088137](https://bugzilla.redhat.com/show_bug.cgi?id=1088137) - Translation Editor: Alpha 1 Prototype

The editor prototype can be accessed via the **(Try the new alpha editor)** button at the top of the regular editor.  It showcases the look and feel, workflow and intended direction of Zanata.

As it is a _prototype_, there are bound to be some bugs and sub-optimal behaviours - any suggestions or reports can be forwarded to our [bug tracker](https://zanata.atlassian.net/).

* [1150373](https://bugzilla.redhat.com/show_bug.cgi?id=1150373) - Keyboard shortcuts
* [1172437](https://bugzilla.redhat.com/show_bug.cgi?id=1172437) - Add plurals to the new editor
* [1174071](https://bugzilla.redhat.com/show_bug.cgi?id=1174071) - [SPA editor] Save on Invalid entry should not cause NullPointerException

<h5>Infrastructure Changes</h5>

Zanata now requires JMS to be configured in standalone.xml in order to queue up some messages going out of the system. For instructions on how to do this, please [See Here](user-guide/system-admin/configuration/jms-messaging)

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


<h5>Bug fixes</h5>
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

-----------------------

## 3.0.0
<h5>New Features</h5>

* [980659](https://bugzilla.redhat.com/show_bug.cgi?id=980659) - TMX import/export
    * Imported TMX shown in translation memory search results
    * Allow users to export translations to TMX (from Project/Version pages)
    * Allow admins to export **all** project translations to TMX (from Projects page)
    * Allow admins to import and export TMX translation memories (from Admin pages)

* [953734](https://bugzilla.redhat.com/show_bug.cgi?id=953734) - Translation review/approval
    * Coordinators can assign reviewers for their languages
    * Project maintainers can require review for translations in their projects

* [979285](https://bugzilla.redhat.com/show_bug.cgi?id=979285) - Implement virus scanning using ClamAV (clamdscan)
* [978666](https://bugzilla.redhat.com/show_bug.cgi?id=978666) - Translators and reviewers can add comments to translations
* [844819](https://bugzilla.redhat.com/show_bug.cgi?id=844819) - New visual style for Zanata
* [1066756](https://bugzilla.redhat.com/show_bug.cgi?id=1066756) - Add user dashboard
* [981064](https://bugzilla.redhat.com/show_bug.cgi?id=981064) - Recent translation/review activity
* Upgrade platform to JBoss EAP 6.1
* Add descriptions on project type selectors
* Allow adapter parameters to be set on source document upload
* Editor improvements
* Add attention key shortcut: Alt+X
* Add attention shortcut to copy from source: Alt+X,G
* File upload
* Move raw document storage to file system

-----------------------

## 2.3.2

* [958350](https://bugzilla.redhat.com/show_bug.cgi?id=958350) - Concurrent save on different row causes incorrect validation warnings in current row
* [959060](https://bugzilla.redhat.com/show_bug.cgi?id=959060) - Generated Zanata config file contains invalid project type
* [961163](https://bugzilla.redhat.com/show_bug.cgi?id=961163) - shift-w and g erroneously triggers Copy from Source
* [959115](https://bugzilla.redhat.com/show_bug.cgi?id=959115) - Database connection pool leaks under load

-----------------------

## 2.3.1
<h5>Bug fixes</h5>

* [953195](https://bugzilla.redhat.com/show_bug.cgi?id=953195) - HQL query exception while trying to filter strings
* Prevent incorrect validation warnings with concurrent edits
* Search result back to editor causes multiple code mirror focus
* Support message bookmark

-----------------------

## 2.3.0

* [908548](https://bugzilla.redhat.com/show_bug.cgi?id=908548) - Long document names cause layout issues in Doc page
* [786630](https://bugzilla.redhat.com/show_bug.cgi?id=786630) - Shortcut Alt+G causes editor to lose focus
* [870876](https://bugzilla.redhat.com/show_bug.cgi?id=870876) - PO download for non-PO projects cannot be uploaded
* [846314](https://bugzilla.redhat.com/show_bug.cgi?id=846314) - Show validation state in doc list and link to error-filter view in editor
* [844553](https://bugzilla.redhat.com/show_bug.cgi?id=844553) - Notification of an entry should have a link that go to the entry
* [727826](https://bugzilla.redhat.com/show_bug.cgi?id=727826) - Order Projects alphabetically
* [917911](https://bugzilla.redhat.com/show_bug.cgi?id=917911) - Keep "Validation Warnings: n" displayed even when moving focus to different pages
* [910637](https://bugzilla.redhat.com/show_bug.cgi?id=910637) - Keep "Validation Warnings: n" displayed even when moving focus to different entry
* [767055](https://bugzilla.redhat.com/show_bug.cgi?id=767055) - Error when pull as XLIFF file format: Underlying stream encoding 'ASCII' and input parameter for writeStartDocument() method 'utf-8' do not match
* [953361](https://bugzilla.redhat.com/show_bug.cgi?id=953361) - Source document name search triggers delete confirmation
* [874335](https://bugzilla.redhat.com/show_bug.cgi?id=874335) - Allow admins to see the email addresses of project maintainers
* [950806](https://bugzilla.redhat.com/show_bug.cgi?id=950806) - Notification links disappear from list when detail is viewed
* [947832](https://bugzilla.redhat.com/show_bug.cgi?id=947832) - Empty translation page when pushing next
* [923461](https://bugzilla.redhat.com/show_bug.cgi?id=923461) - Update document list view and link to the violated entries after project wide validation
* [910183](https://bugzilla.redhat.com/show_bug.cgi?id=910183) - Search in Document List does not show when on page 2+ of Document List
* [854087](https://bugzilla.redhat.com/show_bug.cgi?id=854087) - report which locales have recent changes

-----------------------

## 2.2.2

* [917895](https://bugzilla.redhat.com/show_bug.cgi?id=917895) - Validation rules should be enabled by default
* [917897](https://bugzilla.redhat.com/show_bug.cgi?id=917897) - AlreadyClosedException when new document uploaded and translated
* [807100](https://bugzilla.redhat.com/show_bug.cgi?id=807100) - Removing admin role doesn't take effect for Kerberos authentication

-----------------------

## 2.2.1

* [915130](https://bugzilla.redhat.com/show_bug.cgi?id=915130) - Unexpected error when clicking "resend activation email" or "update email address"
* [916812](https://bugzilla.redhat.com/show_bug.cgi?id=916812) - Activation Key should update after user click "Resend activation email" and "Change email"

-----------------------

## 2.2.0

* [895280](https://bugzilla.redhat.com/show_bug.cgi?id=895280) - Persist project type on server
* [893811](https://bugzilla.redhat.com/show_bug.cgi?id=893811) -  Old registration activation link should expire after a given period
* [750104](https://bugzilla.redhat.com/show_bug.cgi?id=750104) - Old email validation links for email change should expire after a given period
* [913373](https://bugzilla.redhat.com/show_bug.cgi?id=913373) - Ctrl-Enter not moving to next trans unit if there are no changes
* [908563](https://bugzilla.redhat.com/show_bug.cgi?id=908563) - Html Xml tag validation will produce exception in certain case
* [912583](https://bugzilla.redhat.com/show_bug.cgi?id=912583) - Change project type 'raw' to be 'file'
* [910216](https://bugzilla.redhat.com/show_bug.cgi?id=910216) - Statistics API returns word level statistics when only message level statistics are requested
* [910212](https://bugzilla.redhat.com/show_bug.cgi?id=910212) - Ability to resume push/pull from a specified document
* [903470](https://bugzilla.redhat.com/show_bug.cgi?id=903470) - Allow java clients to send and receive source control URLs for projects
* [896356](https://bugzilla.redhat.com/show_bug.cgi?id=896356) - Need to specify the size of the stream when sending a file (or part thereof)
* [896299](https://bugzilla.redhat.com/show_bug.cgi?id=896299) - store and display source control URL
* [895295](https://bugzilla.redhat.com/show_bug.cgi?id=895295) - Validator to warn of inconsistent number of lines
* [913745](https://bugzilla.redhat.com/show_bug.cgi?id=913745) - Zip File download does not work
* [913331](https://bugzilla.redhat.com/show_bug.cgi?id=913331) - "Contact Team Coordinator" return unexpected error
* [913310](https://bugzilla.redhat.com/show_bug.cgi?id=913310) - Value in zanata.properties does not shows up in server configuration page
* [912590](https://bugzilla.redhat.com/show_bug.cgi?id=912590) - Project maintainer should be able to "edit page code"
* [909032](https://bugzilla.redhat.com/show_bug.cgi?id=909032) - Project version's project type should default to that of the project
* [909026](https://bugzilla.redhat.com/show_bug.cgi?id=909026) - Unexpected error when trying to download config file when project-type not set on version
* [903926](https://bugzilla.redhat.com/show_bug.cgi?id=903926) - Project maintainer should be able to define and save validations rules per project/document
* [903477](https://bugzilla.redhat.com/show_bug.cgi?id=903477) - Workspace document list view should have same features as JSF document list view
* [903026](https://bugzilla.redhat.com/show_bug.cgi?id=903026) - Display Last Translator and Last Modified column in the document list

-----------------------

## 2.1.3

* [896332](https://bugzilla.redhat.com/show_bug.cgi?id=896332) - CopyTrans should use the most recent matching translation

-----------------------

## 2.1.1

* [894909](https://bugzilla.redhat.com/show_bug.cgi?id=894909) - Kerberos user unable to log in properly
* [888090](https://bugzilla.redhat.com/show_bug.cgi?id=888090) - Implement REST ETag mechanism for certain GET operations

-----------------------

## 2.1.0

* [844550](https://bugzilla.redhat.com/show_bug.cgi?id=844550) - Provide sort by option on branch stats page
* [874367](https://bugzilla.redhat.com/show_bug.cgi?id=874367) - Editor should warn before saving a Fuzzy translation as Approved from a keyboard shortcut
* [877223](https://bugzilla.redhat.com/show_bug.cgi?id=877223) - Add "clear" button to search field in workspace
* [878275](https://bugzilla.redhat.com/show_bug.cgi?id=878275) - Breadcrumb navigation in workspace should separate project version and locale
* [880436](https://bugzilla.redhat.com/show_bug.cgi?id=880436) - Plain text area editor doesn't get autosize correctly with long string
* [882739](https://bugzilla.redhat.com/show_bug.cgi?id=882739) - Tooltips on paging buttons (editor) shows shortcut keys which doesn't apply
* [892816](https://bugzilla.redhat.com/show_bug.cgi?id=892816) - Recently removed project maintainer retains access to project maintainer actions
* [874374](https://bugzilla.redhat.com/show_bug.cgi?id=874374) - Make translation editor options persistent
* [880894](https://bugzilla.redhat.com/show_bug.cgi?id=880894) - Externalize Email Server configuration
* [881549](https://bugzilla.redhat.com/show_bug.cgi?id=881549) - Allow admins to change account user names
* [884335](https://bugzilla.redhat.com/show_bug.cgi?id=884335) - Add Translation Memory Cache for filter query
* [891485](https://bugzilla.redhat.com/show_bug.cgi?id=891485) - Removing a locale member causes a RecordNotFound error
* [864280](https://bugzilla.redhat.com/show_bug.cgi?id=864280) - upload/download raw file types with the Maven plugin
* [876012](https://bugzilla.redhat.com/show_bug.cgi?id=876012) - The Content-Type of Download as po link is application/octet-stream, but should be text/plain
* [881962](https://bugzilla.redhat.com/show_bug.cgi?id=881962) - Project-wide Search and replace starts by Enter key before ready
* [887052](https://bugzilla.redhat.com/show_bug.cgi?id=887052) - Source and Target search in editor fails when the search term includes an apostrophe (')
* [888150](https://bugzilla.redhat.com/show_bug.cgi?id=888150) - Case sensitive search should return case sensitive results
* [877228](https://bugzilla.redhat.com/show_bug.cgi?id=877228) - Clearing the search field in workspace should keep position at last selected message
* [880444](https://bugzilla.redhat.com/show_bug.cgi?id=880444) - enable spell check in code mirror editor for Firefox
* [880879](https://bugzilla.redhat.com/show_bug.cgi?id=880879) - Undo button causing repeated save failures and other weirdness
* [884402](https://bugzilla.redhat.com/show_bug.cgi?id=884402) - Entry should NOT move unless it is absolutely needed
* [884502](https://bugzilla.redhat.com/show_bug.cgi?id=884502) - navigation breaks in filter mode after saved status not included in filter view
* [887717](https://bugzilla.redhat.com/show_bug.cgi?id=887717) - enable 'Enter' key saves immediately will make pager input dysfunctional
* [887718](https://bugzilla.redhat.com/show_bug.cgi?id=887718) - Too slow to load the last pages of a big document with Firefox
* [888096](https://bugzilla.redhat.com/show_bug.cgi?id=888096) - project become read only with editor options panel open will still allow user to change editor options
* [888592](https://bugzilla.redhat.com/show_bug.cgi?id=888592) - Options to customize translation editor display
* [889411](https://bugzilla.redhat.com/show_bug.cgi?id=889411) - Red border indicating failed validation shows on strings without validation warning/error
* [891458](https://bugzilla.redhat.com/show_bug.cgi?id=891458) - Document List search returning incorrect results
* [885934](https://bugzilla.redhat.com/show_bug.cgi?id=885934) - option to avoid encoding tab as \t
* [803923](https://bugzilla.redhat.com/show_bug.cgi?id=803923) - email should be able to corrected during register validation
* [829565](https://bugzilla.redhat.com/show_bug.cgi?id=829565) - Kerberos activation link in email gets 404 page not found
* [872039](https://bugzilla.redhat.com/show_bug.cgi?id=872039) - Escaping with single-quote (a.k.a. Apostrophes ') character in MessageFormat strings can cause confusing validation warnings
* [886711](https://bugzilla.redhat.com/show_bug.cgi?id=886711) - Error when using pull for project type raw when the document name does not include a type extension
* [831056](https://bugzilla.redhat.com/show_bug.cgi?id=831056) - Option for highlight only the search terms
* [785046](https://bugzilla.redhat.com/show_bug.cgi?id=785046) - Limit source string length in properties file
* [846643](https://bugzilla.redhat.com/show_bug.cgi?id=846643) - Shorten the navigation sequence to open a document in the Editor
* [884386](https://bugzilla.redhat.com/show_bug.cgi?id=884386) - Email validation link should be invalid after user validate the email, or user request another validation

-----------------------

## 2.0.3
<h5>New Features</h5>

* Allow admin to add extra locales by typing in the BCP-47 locale code.
* TM Merge reports what it did
* Allow choice of editor page size
* Support txt, dtd and open document format (REST & web interface)
* Editor option to disable CodeMirror (to enable browser spell-check)
* Detect loss of connection to server
* Fix for problem creating users with Kerberos
* Allow Project Maintainers to Delete a Source Document

[<h5>Bug fixes | Bugzilla</h5>](https://bugzilla.redhat.com/buglist.cgi?j_top=OR&f1=cf_fixed_in&o1=substring&classification=Community&o2=substring&query_format=advanced&f2=cf_fixed_in&bug_status=VERIFIED&bug_status=RELEASE_PENDING&bug_status=POST&bug_status=CLOSED&v1=2.0.3&product=Zanata)

-----------------------

## 2.0.2
* Bug fixes for document search/navigation


-----------------------

## 2.0.1
* Update jboss-el to avoid bad artifact in repository

-----------------------

## 2.0.0
<h5>New Features</h5>

* UI redesign
* Performance: async push service to avoid timeouts when pushing source/target
* Performance: improve performance when loading large documents
* Allow user to save work when concurrent edit occurs
* Include last translator information in TM info box
* Web analytics (Piwik integration)
* Navigation breadcrumbs
* Bugzilla link in UI
* Get Stats about Translation Documents via REST
* Remove blinking notification in editor
* Configurable page size
* Advanced glossary features
* Open ID Authentication
* Admin role assignment configuration for authentication types
* Highlight tags in editor fields (CodeMirror for editor)
* Translation editor rewrite
* Project-level default Copy Trans options
* Red bars for translations with validation warnings should stay in red when moving to the next row
* Option to show word or message based statistics
* Visible whitespace in editor
* View history of translations for a text flow

[<h5>Bug fixes | Bugzilla</h5>](https://bugzilla.redhat.com/buglist.cgi?j_top=OR&f1=cf_fixed_in&o1=substring&classification=Community&o2=substring&query_format=advanced&f2=cf_fixed_in&bug_status=CLOSED&v1=1.8.0&v2=2.0.0&product=Zanata)

-----------------------

## 1.7.3
[<h5>Bug fixes | Bugzilla</h5>](https://bugzilla.redhat.com/buglist.cgi?f1=cf_fixed_in&o1=substring&classification=Community&query_format=advanced&bug_status=CLOSED&v1=1.7.3&product=Zanata)

-----------------------

## 1.7.2
[<h5>Bug fixes | Bugzilla</h5>](https://bugzilla.redhat.com/buglist.cgi?classification=Community&target_release=1.7.2&query_format=advanced&bug_status=CLOSED&product=Zanata)

-----------------------

## 1.7.1
[<h5>Bug fixes | Bugzilla</h5>](https://bugzilla.redhat.com/buglist.cgi?classification=Community&target_release=1.7.1&query_format=advanced&bug_status=CLOSED&product=Zanata)

-----------------------

## 1.7.0
<h5>New Features</h5>

* UI Improvements
* Don't enforce locales for source documents
* On-Demand copy trans
* Email log appender
* Centralise management of key shortcuts in Zanata
* Improvements to reindexing (processing in small batches, index classes separately)
* Editor validation for XML entities
* Undo button for saved translations
* Translation Memory merge in editor
* Add support for positional strings in printf validator
* Translation Memory now uses word-based indexing

[<h5>Bug fixes | Bugzilla</h5>](https://bugzilla.redhat.com/buglist.cgi?classification=Community&target_release=1.6.2&target_release=1.7&query_format=advanced&bug_status=CLOSED&product=Zanata)

-----------------------

## 1.6.1
* Allow Zanata to add locales for which plural form is not known

[<h5>Bug fixes | Bugzilla</h5>](https://bugzilla.redhat.com/buglist.cgi?classification=Community&target_release=1.6.1&query_format=advanced&bug_status=CLOSED&product=Zanata)

-----------------------

## 1.6.0
<h5>New Features</h5>

* UI Improvements
* Allow Project Maintainers to edit all language files
* Glossary suggestions
* Add the ability to specify custom locales that are not enabled by default
* Upgrade Liquibase to version 2.0
* Project grouping
* Support plural forms
* Offline translation feature via web UI
* Allow translators to push translations using Maven client
* Indicators for simultaneous edits
* Java style variable validations in translation editor
* "Create Project" for non-administrator users
* Display page context in window title
* Ability to monitor Zanata server statistics (JavaMelody)
* Overview for available keyboard shortcuts in web editor

[<h5>Bug fixes</h5>](https://bugzilla.redhat.com/buglist.cgi?classification=Community&target_release=1.6&target_release=1.6-alpha-1&target_release=1.6-beta-1&query_format=advanced&bug_status=CLOSED&product=Zanata)

-----------------------

## 1.5.0
<h5>New Features</h5>

* [757621](https://bugzilla.redhat.com/show_bug.cgi?id=757621) - Allow bookmarking of selected document, document list filter and current view
* [758587](https://bugzilla.redhat.com/show_bug.cgi?id=758587) - Add workspace query string parameters for generating a custom doclist with a custom title.
    * e.g. &title=Custom%20title&doc=full/path/of/first/doc&doc=full/path/of/second/doc

* [755759](https://bugzilla.redhat.com/show_bug.cgi?id=755759) - Allow readonly access to retired project/project iteration
* [773459](https://bugzilla.redhat.com/show_bug.cgi?id=773459) - Implement filter messages in the editor by translation status
* [768802](https://bugzilla.redhat.com/show_bug.cgi?id=768802) - Newline validation on leading and trailing string
* [769471](https://bugzilla.redhat.com/show_bug.cgi?id=769471) - Variables to be checked for consistency
* [756235](https://bugzilla.redhat.com/show_bug.cgi?id=756235) - XML and HTML tags to be checked for completeness
* Redesign of color scheme translation editor workspace layout
* Project/project iteration status changes: ACTIVE, READONLY, and OBSOLETE
* Project list filtering based on status
* Overall statistics page for Admin
* Add file download page with the option to download a single PO file, or a zip with a project iteration's files for one locale
* Translation memory results now have highlighted differences
* Activate entity caching
* Maven client option to create 'skeleton' PO files when no translations are present
* Maven client option to log detailed client-server message information
* Generate Zanata Rest API documentation
* Add automated compatibility tests with previous versions of the Zanata java client classes
* Redirect to previous page after sign in
* Several UI updates and changes

<h5>Bug fixes</h5>

* [785034](https://bugzilla.redhat.com/show_bug.cgi?id=785034) - Rapid key navigation causes backlog of TM queries
* [750956](https://bugzilla.redhat.com/show_bug.cgi?id=750956) - Long strings slow down the operation
* [756292](https://bugzilla.redhat.com/show_bug.cgi?id=756292) - "Participants" information is incorrect
* [759337](https://bugzilla.redhat.com/show_bug.cgi?id=759337) - Translation editor: Long word in source cell invades the editor cell
* [746899](https://bugzilla.redhat.com/show_bug.cgi?id=746899) - On push operations, copyTrans runs too slowly
* [719176](https://bugzilla.redhat.com/show_bug.cgi?id=719176) - Edit profile: "duplicate email" is shown even if user press save without changing email
* [690669](https://bugzilla.redhat.com/show_bug.cgi?id=690669) - Translation editor table shows changes which failed to save

-----------------------

## 1.4.5.2
 * Fix handling of fuzzy entries when saving Properties files

-----------------------

## 1.4.5.1
 * [795597](https://bugzilla.redhat.com/show_bug.cgi?id=795597) - Fix regression with Unicode encoding for ordinary (Latin-1) .properties files

-----------------------

## 1.4.5
 * [742872](https://bugzilla.redhat.com/show_bug.cgi?id=742872) - Add support for Maven modules:
 * [760431](https://bugzilla.redhat.com/show_bug.cgi?id=760431) - Fix bug: Moving to a new page does not refresh the translation textboxes (ghost translations)

-----------------------

## 1.4.4
* [747836](https://bugzilla.redhat.com/show_bug.cgi?id=747836) - Ensure final reindex batch is properly flushed
* [760390](https://bugzilla.redhat.com/show_bug.cgi?id=760390) - Support UTF-8 Properties files, handle empty properties
* [759994](https://bugzilla.redhat.com/show_bug.cgi?id=759994) - Fix bug: Editor table stops working after 'Source and Target' search returns no results
* Add dryRun option for Maven goals 'push' and 'pull'

-----------------------

## 1.4.3
<h5>New Features</h5>

* [750690](https://bugzilla.redhat.com/show_bug.cgi?id=750690) - Show message context in editor info panel
* [727716](https://bugzilla.redhat.com/show_bug.cgi?id=727716) - Add failsafe editor in case of Seam Text problems
* [730189](https://bugzilla.redhat.com/show_bug.cgi?id=730189) - Change string similarity algorithm so that only identical strings (not substrings) can get 100%
* [747836](https://bugzilla.redhat.com/show_bug.cgi?id=747836) - Show progress during re-index operations; avoid timeout for large databases
* Update gwteventservice to 1.2.0-RC1
* Modify email templates to include server URL

<h5>Bug fixes</h5>

* [754637](https://bugzilla.redhat.com/show_bug.cgi?id=754637) - 'J' and 'K' navigation keys trigger when entering text in the TM search box
* [756293](https://bugzilla.redhat.com/show_bug.cgi?id=756293) - Not able to work in parallel on the same workbench
* [751264](https://bugzilla.redhat.com/show_bug.cgi?id=751264) - Fix problems with editor table when searching or switching pages

-----------------------

## 1.4.2
* [742083](https://bugzilla.redhat.com/show_bug.cgi?id=742083) - Language team coordinator
* [742854](https://bugzilla.redhat.com/show_bug.cgi?id=742854) - Contact server admins
* [743783](https://bugzilla.redhat.com/show_bug.cgi?id=743783) - First/last entry button
* [744114](https://bugzilla.redhat.com/show_bug.cgi?id=744114) - Load project pages faster
* [744671](https://bugzilla.redhat.com/show_bug.cgi?id=744671) - Option for Enter to save translation
* [746859](https://bugzilla.redhat.com/show_bug.cgi?id=746859) - Sort projects by name, not ID
* [740122](https://bugzilla.redhat.com/show_bug.cgi?id=740122) - Make newlines visible to reduce newline mismatch errors in translations
* [740191](https://bugzilla.redhat.com/show_bug.cgi?id=740191) - Improve shortcut keys
* [746870](https://bugzilla.redhat.com/show_bug.cgi?id=746870) - Save as Fuzzy now leaves the cell editor open
* [743134](https://bugzilla.redhat.com/show_bug.cgi?id=743134) - Modal navigation: next fuzzy, untranslated, fuzzy or untranslated
* Rearrange various UI elements to be more logical (profile page, document stats, project search field)
* Users now have to ask before joining a language team
* Coordinator can add and remove team members
* Contact coordinators
* Fix tab order: editor cell -> Save as Approved -> Save as Fuzzy -> Cancel

-----------------------

## 1.4.1
* [741523](https://bugzilla.redhat.com/show_bug.cgi?id=741523) - Fixed: % completed should be calculated with words, not messages
* [724867](https://bugzilla.redhat.com/show_bug.cgi?id=724867) - Fixed: Selecting Administration submenu items does not always highlight the parent menu
* [742111](https://bugzilla.redhat.com/show_bug.cgi?id=742111) - Fixed: Change of tile to list view on Language page, make project list sortable
* [743179](https://bugzilla.redhat.com/show_bug.cgi?id=743179) - Performance fix for projects with 1000+ documents

-----------------------

## 1.4
* add project-type to zanata.xml for generic push/pull commands
* redirect to login from translation editor when required
* if domain is left blank by admin, don't populate email address for new users
* UI bug fixes

-----------------------

## 1.4-alpha-1
* create generic push/pull commands, with include/exclude filters
* add support for Java Properties and XLIFF projects
* bug fix: mark existing translations of modified XLIFF/Properties strings as fuzzy
* modify keyboard shortcuts in editor
* add new Zanata logo/favicon
* various UI improvements
* auto-size for translation text area
* add icons to buttons and remove text
* add option to hide editor buttons
* remove Clone and Save button; move Copy button to middle
* autosave when leaving a cell
* remove Fuzzy checkbox; add Save as Fuzzy
* better statistics graphs
* display resource IDs for translation units
* add ability to hide translation unit details
* show translation states with coloured side bars, and italics for Fuzzy
* recalculate missing word counts
* add liquibase script
* bug fix for search re-indexing by admin
* copy translations of identical strings when importing new documents
* bug fixes and improvements for UI
* bug fix for word counts (thread safety)
* remove email address from Language Team pages
* enable stats for anonymous users
* no need to enforce locales for source documents
* bug fix for push/merge when PO files are missing some msgids
* bug fixes

-----------------------

## 1.3
* bug fixes for authentication and for source comments

-----------------------

## 1.3-alpha-3
* finalise rebrand from flies->zanata: XML namespaces, media types, etc
* more logging for authentication errors
* bug fix for Kerberos authentication

-----------------------

## 1.3-alpha-2
* switch source control to git on github
* rebrand from flies->zanata (maven artifacts, java packages, mailing lists)
* Fedora authentication rhbz#692011
* generate zanata.xml config file (http://code.google.com/p/flies/issues/detail?id=282)
* merge translations on import (http://code.google.com/p/flies/issues/detail?id=28)
* preserve and generate PO header comments for translator credits (http://code.google.com/p/flies/issues/detail?id=269)
* bug fixes

-----------------------

## 1.3-alpha-1
* rebrand from flies->zanata (except URIs, maven artifacts and java packages)
* specify locales per project/version (http://code.google.com/p/flies/issues/detail?id=261)
* added tab for home page, removed project list, contents editable by admin (http://code.google.com/p/flies/issues/detail?id=279)
* added help page/tab, contents editable by admin (http://code.google.com/p/flies/issues/detail?id=280)
* removed name and description from project version (http://code.google.com/p/flies/issues/detail?id=281)
* stats for all languages (http://code.google.com/p/flies/issues/detail?id=275)
* workaround for form/login issue on Firefox 4.0 rhbz#691963
* bug fixes

-----------------------

## flies-1.2
* disabled bad key bindings (http://code.google.com/p/flies/issues/detail?id=262)
* fixed python client issue with PotEntryHeader.extractedComment (http://code.google.com/p/flies/issues/detail?id=256)
* web template redesign (new logo, CSS) (http://code.google.com/p/flies/issues/detail?id=238)
* fixed Seam integration tests (http://code.google.com/p/flies/issues/detail?id=231)

-----------------------

## flies-1.2-alpha-3
* improve notifications in editor (http://code.google.com/p/flies/issues/detail?id=191)
* highlight search terms in editor (http://code.google.com/p/flies/issues/detail?id=227)

-----------------------

## flies-1.2-alpha-2
* better messages
* bug fixes

-----------------------

## flies-1.2-alpha-1
* development change: re-arranged Maven modules into common, client and server

-----------------------

## flies-1.1.1
* use word counts in translation statistics (http://code.google.com/p/flies/issues/detail?id=203)
* bug fixes

-----------------------

## flies-1.1
* Kerberos/JAAS fixes
* require name & email address on first login for JAAS/Kerberos
* validate changes to email address
* use correct BCP-47 language tags (zh-CN-Hans is now zh-Hans-CN)

-----------------------

## flies-1.1-alpha-1
* JAAS authentication
* Kerberos authentication
* remove communities tab and my communities UI (http://code.google.com/p/flies/issues/detail?id=197)
* remove "Language Missing" button (http://code.google.com/p/flies/issues/detail?id=185)
* show member number for the language groups (http://code.google.com/p/flies/issues/detail?id=186)
* allow overriding POT directory in Maven client (http://code.google.com/p/flies/issues/detail?id=200)
* support `[servers]` in flies.ini for Maven client (http://code.google.com/p/flies/issues/detail?id=193)
* better info/error messages in Maven client

-----------------------

## flies-1.0.3
* fix TM caching issue (http://code.google.com/p/flies/issues/detail?id=190)
* add 'translator' role and security rules
* configurable URLs

-----------------------

## flies-1.0.2
* minor UI fixes (http://code.google.com/p/flies/issues/detail?id=173, http://code.google.com/p/flies/issues/detail?id=176)
* ergonomics for Maven client
* UI for assigning project maintainers (http://code.google.com/p/flies/issues/detail?id=180)
* better error checking in REST API (http://code.google.com/p/flies/issues/detail?id=175)
* security rule fix (http://code.google.com/p/flies/issues/detail?id=182)

-----------------------

## flies-1.0.1
* database schema fixes
* fixes for deployment issues

-----------------------

## flies-1.0
* initial release
