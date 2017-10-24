This is only needed once on the page to add all the icons to the page.

- They are hidden and referenced by each `Icon` component
- Including it here so it is usable by the `Icons` component

    <Icons />
    
If you watch build is complaining
```
Module not found: Error: Can't resolve './Icons' in 'platform/server/zanata-frontend/src/frontend/app/components'

```
You need to run ```make draft``` first to generate the index.jsx file and then run the normal watch task.   
