Once a project has been created, the maintainer can add further details and project behaviour via the Settings tab.
See the [Project Creation Help](/user-guide/projects/create-project) for details on creating projects.
<figure>
![Project General Settings tab](/images/project-settings-button.png)
<figcaption>Project Settings tab link.</figcaption>
</figure>

The Settings tab contains fields that manage appearance and workflow of your project.  Some of these are already covered in the [Project Creation Help](/user-guide/projects/create-project).

------------

## General Settings
<figure>
![Project General Settings tab](/images/admin-project-settings.png)
<figcaption>Project General Settings tab</figcaption>
</figure>
<br>

### Project Type

Project Type defines the type of files that your project uses to store source and translation strings. This setting ensures that files for your project will be downloaded in the correct format.

There is a brief description for each project type next to each project type option. If the description is insufficient, more information on each project type is available at [Project Types](/user-guide/projects/project-types).

### Home Page

This is an optional field to provide a URL that translators can use to view the source files in their original format. This will be shown on your project's homepage as a clickable link. This will most commonly be a link to the source document in a version control system such as github. Providing this link can help professional translators provide high quality translations.

For example, the URL provided for the strings used in the Zanata user interface points to [Zanata Source Strings](https://github.com/zanata/zanata-server/blob/master/zanata-war/src/main/resources/messages.properties)

### Repository

This is an optional field to provide a machine-readable string that can be used to download the source files.

For example, a git checkout URL is provided for the Zanata server project that can be used with git command line to download a copy of the Zanata server source code as shown here:

```bash
$ git clone git@github.com:zanata/zanata-platform.git
```

### Private

This setting determines if the project will be visible to all Zanata users or just subset of users.
When it is activated, the project will only visible to assigned users and only translators in your [project team](/user-guide/projects/project-team/) are allowed to translate your project.

### Make this project read only

This button is used to set a project to read-only, which prevents translations being entered. This may be useful in some cases, but should be used sparingly so that translators are able to work on your project.

This can be toggled using the same button, as desired.

### Delete this project

This button is used to delete a project and remove it from the public projects list. You will not be able to access it anymore. This action cannot be undone, so use with caution.

------------

## Languages Settings
<figure>
![Project Languages Settings tab](/images/project-languages-settings.png)
<figcaption>Project Languages Settings tab</figcaption>
</figure>
<br>

------------
### Reset languages from global settings

By default, your project will be available for translation to all of the enabled locales defined on the Zanata server. If your project has added or removed any languages, this button will appear, allowing you to reset the project's languages to the default list.

------------
### Add a Language

To search for available languages, enter text into the field under "Add a language". Available languages matching the entered text will display in a dropdown.
To add add a language to your project, select the desired language from the dropdown.
<figure>
![Adding a project language](/images/project-languages-add.png)
<figcaption>Adding a project language</figcaption>
</figure>

------------
### Remove a Language

To remove a language from the list of available locales, first move the cursor over the language, then click the "X" that appears.
<figure>
![Removing a project language](/images/project-languages-remove.png)
<figcaption>Removing a project language</figcaption>
</figure>

------------

## Translation Settings
<figure>
![Project Translation Settings tab](/images/project-translation-settings.png)
<figcaption>Project Translation Settings tab</figcaption>
</figure>

------------

### Validations

Validations run in the translation editor and help translators to provide translations that are valid for your project. Validations set to `Warning` or `Error` in this list will be displayed in the translation editor when an invalid translation has been entered.
Validations not enabled here can be toggled by translators to suit their individual workflow.

* Off - turn off validation check by default.
* Warning - display warning to translator when validation failed. Translator can save the translation as `Translated`.
* Error - display error to translator when validation failed. Translator cannot save translation as `Translated` until error has been fixed.

See [validation](/user-guide/projects/validations) all available check.

### Copy Translations settings

Copy Translations attempts to reuse translations that have been entered in Zanata by matching them with untranslated strings in your project-version.  These settings change the way Copy Translations behaves when a new version is created.

Refer to [the Copy Translations reference](/user-guide/translation-reuse/copy-trans/) for more information.

------------

## Permissions Settings
<figure>
![Project Permissions Settings tab](/images/project-permissions-settings.png)
<figcaption>Project Permissions Settings tab</figcaption>
</figure>
<br>

Note: Maintainers can also be added and removed through the [project team](/user-guide/projects/project-team/).

### Add a Maintainer

To search for users, enter at least three characters of a username into the field under "Add a Maintainer". Available users matching the entered text will display in a dropdown.
To add a user as a maintainer for the project, select their username from the dropdown.

### Remove a Maintainer

To remove a maintainer from the maintainers list, first move the cursor over the maintainer, then click the "X" that appears.

### Restrict access to certain user roles

The access restriction feature is intended for use with special roles that can be defined by an administrator. For example, the role 'Fedora_CLA' is automatically assigned by users who sign in using Fedora OpenID, and can be used here to ensure that Fedora translation is only performed by users who have accepted the Fedora Contributor License Agreement.

------------

## Webhooks
<figure>
![Project Webhooks Settings tab](/images/project-webhooks-settings.png)
<figcaption>Project Webhooks Settings tab</figcaption>
</figure>

The Webhooks feature is HTTP callbacks which are triggered when a selected event happens.
When an event occurs, Zanata will make a HTTP POST to the provided payload URL in the project.
Types of events available:

#### Translation Milestone
Trigger when document has reached 100% Translated or Approved
```
{
  "type": "DocumentMilestoneEvent",
  "milestone": "100% Translated",
  "locale": "de",
  "docId": "zanata-war/src/main/resources/messages",
  "version": "master",
  "project": "zanata-server",
  "editorDocumentUrl": "https://translate.zanata.org/zanata/webtrans/Application.xhtml?project=zanata-server&iteration=master&localeId=de&locale=en#view:doc;doc:zanata-war/src/main/resources/messages"
}
```
  
#### Translation update
Trigger when translation is updated
```
{
    "username":"aeng",
    "project":"Zanata",
    "version":"master",
    "docId":"doc1id",
    "locale":"zh-CN",
    "wordDeltasByState":{"New":-16,"Translated":16},
    "type":"DocumentStatsEvent"
}
```

#### Project version
Trigger when a version is added to removed from a project
```
{"project":"zanata","version":"new-version","changeType":"CREATE","type":"VersionChangedEvent"}
```
  
#### Project maintainer
Trigger when a project maintainer is added or removed from project
```
{"project":"zanata","username":"aeng","changeType":"REMOVE","role":"Maintainer","type":"ProjectMaintainerChangedEvent"}
```
  
#### Document
Trigger when a source document is added or removed from project version
```
{"project":"zanata","version":"new-version"","changeType":"ADD","type":"SourceDocumentChangedEvent"}
```

#### Manual event
Trigger by user manually.

To trigger such an event, first the project maintainer will need to add and enable the webhook.
Then the translator can select the language they have access to from the languages tab on the version overview page. Then from the top right dropdown menu of the documents column, they should be able to fire the event to the registered webhook endpoint.
```json
{
  "project": "zanata",
  "version": "master",
  "username": "triggerer",
  "zanataServer": "translate.zanata.org",
  "locale": "zh",
  "type": "ManuallyTriggeredEvent"
}
```

If a secret key is provided for that payload URL, Zanata will sign the webhook request with HTTP header `X-Zanata-Webhook`. 
The header is a double hash of `HMAC-SHA1` in base64 digest. The double hash is generated from the full request body and the payload URL as provided.

Here is some sample pseudocode for checking the validity of a request:
```
boolean verifyRequest(request, secret, callbackURL) {
    Bytes content = request.body.getBytes(UTF8) + callbackURL.getBytes(UTF8);
    String expectedHash = base64(hmacSha1(secret, base64(hmacSha1(secret, content))));
    String headerHash = request.getHeader("X-Zanata-Webhook");
    return headerHash == expectedHash;
}
```

### Adding a new webhook
<figure>
![Project Webhooks New](/images/project-webhooks-new.png)
</figure>

1. Click on 'New webhook' button.
2. Enter a valid URL and secret key (optional) into the provided text input. 
3. Select webhook types for this URL.
4. Click on 'Add webhook' button to add the URL.

### Update a webhook
<figure>
![Project Webhooks Edit](/images/project-webhooks-edit.png)
</figure>
1. Click on 'Edit' button on the right of the webhook entry
2. Update any value in the form
3. Click on 'Update' to save the changes
4. To remove webhook, click on button 'Delete'

------------

## About
<figure>
![Project About Settings tab](/images/project-about-settings.png)
<figcaption>Project About Settings tab</figcaption>
</figure>
<br>

About is optional text that will be shown on your project's about
tab. This can be used to provide more detailed information to translators
to help them understand and translate your project.

The text format is parsed as CommonMark markdown. For help with text
formatting, click "CommonMark" under the editor.
