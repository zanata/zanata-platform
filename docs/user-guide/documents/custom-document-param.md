##### Editing Custom document parameters

Custom parameters allow adjustments to how an uploaded document is processed. The available adjustments vary depending on the document type. This page will list available parameters for some commonly used document types.

**IDML**

Default parameter string:
```
#v1
extractNotes.b=false
simplifyCodes.b=true
extractMasterSpreads.b=true
skipThreshold.i=1000
newTuOnBr.b=false
```

You can use this as a base and change any of the `true`, `false` or number values.
Zanata uses Okapi IDML Filter to process IDML documents, so the usage details of each parameter can be found on the [Okapi IDML Filter page](http://www.opentag.com/okapi/wiki/index.php?title=IDML_Filter)