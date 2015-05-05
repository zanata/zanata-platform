'Role assignment rules' are Zanata's way of dynamically assigning security roles and even Project access to users. 
To configure Role Assignment Rules, admin users must go to the Administration section and click 'Role assignment rules' button.

<figure>
<img alt="Role Assignment Rules Location" src="images/role-assignment-access.png" />
</figure>
<br/>


### Overview

<figure>
<img alt="Role Assignment Rules overview" src="images/role-assignment-view.png" />
</figure>
<br/>

1. **Policy Name:** This is the type of authentication system that a user logs in with (Open Id, Internal Authentication, etc).

2. **Role To Assign:** This is the Role that will be assigned to the user upon login, if the Policy name and Identity pattern match.

3. **Identity Pattern:** This is a Regular expression that will be matched against the user's authenticated identity (most of the time this is the user name, but for Open Id, it's the Open Id itself).


### How it works

1. User Logs in.
2. Authentication Policy is checked. If the user logged in using the authentication policy declared in the rule, continue to the next step. Otherwise, this rule is not applied.
3. Identity Patter is checked. If the user's name (or Open Id) matches the pattern described in the rule, continue to the next step. Otherwise, this rule is not applied.
4. The user is assigned the role stated in the Rule. Process subsequent rules.

### Adding new rules

1. Navigate to 'Role assignment rules' page.
1. Click on `More Action` menu on the top right panel.
<figure>
<img alt="Role Assignment Rules new" src="images/role-assignment-new-access.png" />
</figure>
<br/>
1. Select `New Rule`.
<figure>
<img alt="Role Assignment Rules new form" src="images/role-assignment-new-form.png" />
</figure>
<br/>
1. Fill in the fields and click `Save`.

### Deleting rules

1. Navigate to 'Role assignment rules' page.
1. Locate the rule you wish to delete, click on `Options` menu in front of the row.
1. Click `Delete` to remove the rule.
<figure>
<img alt="Role Assignment delete" src="images/role-assignment-delete.png" />
</figure>
<br/>


### Restricting Project Access to User Roles

To complement the dynamic role assignment described above, Zanata can now restrict project access by roles. 
To restrict project access,

1. Go to project settings page.
1. Click on `Permission` tab.
1. Check `Restrict access to certain user roles` and select roles that you wish to allow access to your project.
<figure>
<img alt="Role Assignment project" src="images/role-assignment-project.png" />
</figure>
<br/>
1. Any role restrictions will now be seen on the project's page and only users belonging to that role will be able to work on the project.