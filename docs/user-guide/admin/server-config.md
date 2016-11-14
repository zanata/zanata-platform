## General

General configuration for Zanata server.
<figure>
![Admin settings general](/images/admin-settings-general.png)
</figure>
<br/>

1. Server URL - The base URL for Zanata server including the application context path.
2. Register URL - The user registration URL for the server.
3. Email Domain name - Domain name used for email address.
4. Contact Admin address - Email address for Zanata admin.
5. From Email Address - Email address of 'From' field in all email sent from Zanata.
6. Terms of Use URL - URL for terms of use statement.
7. Help URL - URL for help page.



------ 

## Email log

This enables or disables the sending of Zanata diagnostics log information via email.
<figure>
![Admin settings email log](/images/admin-settings-email-log.png)
</figure>
<br/>

1. To enable, click on `Enabled` checkbox.
1. Select log level to send email. `Error` will only send error messages, while `Warning` will send both warning and error messages.
1. Fill in email address (comma separate for multiple email address) in provided text field.
1. Click `Save` at the bottom of the page for save changes.


------ 

## Piwik

[Piwik](http://piwik.org/) is a web analytics tools application. It tracks online visits to one or more websites and displays reports on these visits for analysis.
<figure>
![Admin settings piwik](/images/admin-settings-piwik.png)
</figure>
<br/>

1. Fill in your hosted piwik url and id to enable Piwik.


------ 

## Client

Admin can control the limit of client communication towards server via Client or REST API.
<figure>
![Admin settings client](/images/admin-settings-client.png)
</figure>
<br/>

1. Max concurrent requests per API key - Once over the limit server will return status code 403. 0 means no limit. Default(blank) is 6.
1. Max active requests per API key - Request may block. 0 means no limit. Default(blank) is 2. If this is greater than max concurrent request limit, it will have no effect.
1. Max files per upload - Maximum number of files a user can queue for upload in the web upload dialog.