### Prerequisite
Requires **Glossary-admin** role. See [glossary roles and permission](/user-guide/glossary/glossary-roles-permissions) for permission setup.

### Delete via Web UI
1. Login into Zanata
1. Click `Glossary` on menu.
1. To delete a glossary entry, go to the entry you wish to delete and click `Delete`.
<figure>
![Glossary edit](/images/glossary-edit.png)
</figure>


### Delete via Zanata Maven client
The following instructions assume that you have installed and configured the Zanata Maven Plugin.
Instructions for installation and configuration are available at [Installing Zanata client](http://docs.zanata.org/projects/zanata-client/en/latest/#installation).

**Delete all glossary entries in Zanata**

```
mvn zanata:glossary-delete -Dzanata.allGlossary=true
```
