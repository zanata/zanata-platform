### Prerequisite
See [glossary roles and permission](/user-guide/glossary/glossary-roles-permissions) for permission setup.

### Delete via Web UI
1. Login into Zanata
1. To delete an entry in **system glossary**, click `Glossary` menu. 
1. To delete an entry in **project glossary**, navigate to the project page, click on "Glossary" in the project page.
1. Go to the entry you wish to delete and click `Delete`.
<figure>
![Glossary edit](/images/glossary-edit.png)
</figure>


### Delete via Zanata client
The following instructions assume that you have installed and configured the Zanata client.
Instructions for installation and configuration are available at [Installing Zanata client](/client#installation).

**Delete all system glossary entries**
```
zanata-cli glossary-delete --all
```

**Delete all project glossary entries**
```
zanata-cli glossary-delete --all --project {projectSlug}
```
