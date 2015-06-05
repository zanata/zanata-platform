### Prerequisite
Requires **Glossary-admin** role. See [glossary roles and permission](/user-guide/glossary/glossary-roles-permissions) for permission setup.

### Delete via Web UI
1. Login into Zanata
1. Click `Glossary` on menu.
1. Click on `Glossary options` on the glossary language you wish to delete and click `Delete glossary`.
<figure>
![Glossary options](/images/glossary-options.png)
</figure>
1. Click `OK` to confirm delete all glossary entries in selected locale.


### Delete via Zanata Maven client
The following instructions assume that you have installed and configured the Zanata Maven Plugin. 
Instructions for installation and configuration are available at [Installing Zanata client](http://zanata-client.readthedocs.org/en/latest/installation/).

**Delete glossary entries by locale**

```
mvn zanata:glossary-delete -Dzanata.lang={locale of glossary to delete}
```

**Delete all glossary entries in Zanata**

```
mvn zanata:glossary-delete -Dzanata.allGlossary=true
```
