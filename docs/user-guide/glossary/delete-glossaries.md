---
title:  "Deleting Glossaries"
last-updated: 2014-10-30
redirect_from: "/help/glossary/glossary-delete/"
---

### Prerequisite
Requires **Glossary-admin** role. See [glossary roles and permission]({{ site.url }}/help/glossary/glossary-roles-permissions) for permission setup.

### Delete via Web UI
1. Login into Zanata
1. Click `Glossary` on menu.
1. Click on `Glossary options` on the glossary language you wish to delete and click `Delete glossary`.
<figure>
    <img alt="Glossary options" src="{{ site.url }}/images/351-glossary-options.png" />
</figure>
1. Click `OK` to confirm delete all glossary entries in selected locale.


### Delete via Zanata Maven client
The following instructions assume that you have installed and configured the Zanata Maven Plugin. Instructions for installation and configuration are available at [Installing the Maven Plugin]({{ site.url }}/help/maven-plugin/maven-plugin-install) and [Configuring the Maven Plugin]({{ site.url }}/help/maven-plugin/maven-plugin-config)

**Delete glossary entries by locale**

```
mvn zanata:glossary-delete -Dzanata.lang={locale of glossary to delete}
```

**Delete all glossary entries in Zanata**

```
mvn zanata:glossary-delete -Dzanata.allGlossary=true
```
