/* tslint:disable:max-line-length */

/**
 * Default en-US locale Validation messages for translation
 */
const en = {
  ab: "string",
  notifyValidationError: "Validation error - See validation message",
  newLineValidatorDesc: "Check for consistent leading and trailing newline (/n).",
  leadingNewlineMissing: "Leading newline (/n) is missing",
  leadingNewlineAdded: "Unexpected leading newline (/n)",
  trailingNewlineMissing: "Trailing newline (/n) is missing",
  trailingNewlineAdded: "Unexpected trailing newline (/n)",
  tabValidatorDesc: "Check whether source and target have the same number of tabs.",
  targetHasFewerTabs: "Target has fewer tabs (/t) than source (source: {sourceTabs}, target: {targetTabs})",
  targetHasMoreTabs: "Target has more tabs (/t) than source (source: {sourceTabs}, target: {targetTabs})",
  printfVariablesValidatorDesc: "Check that printf style (%x) variables are consistent.",
  varPositionOutOfRange: "Variable {x} position is out of range",
  mixVarFormats: "Numbered arguments cannot mix with unnumbered arguments",
  varPositionDuplicated: "Variables have same position: ",
  javaVariablesValidatorDesc: "Check that java style ('\\{x\\}') variables are consistent.",
  differentVarCount: "Inconsistent count for variables: ",
  differentApostropheCount: "Number of apostrophes ('') in source does not match number in translation. This may lead to other warnings.",
  quotedCharsAdded: "Quoted characters found in translation but not in source text. Apostrophe character ('') must be doubled ('''') to prevent quoting when it is used in Java MessageFormat strings.",
  printfXSIExtensionValidationDesc: "Check that positional printf style (%n/$x) variables are consistent.",
  varsMissing: "Missing variables: {missing}",
  varsMissingQuoted: "Unexpected quoting of variables: ",
  varsAdded: "Unexpected variables: {added}",
  varsAddedQuoted: "Variables not quoted: ",
  xmlHtmlValidatorDesc: "Check that XML/HTML tags are consistent.",
  tagsAdded: "Unexpected tags: {added}",
  tagsMissing: "Missing tags: {missing}",
  tagsWrongOrder: "Tags in unexpected position: ",
  xmlEntityValidatorDesc: "Check that XML entity are complete.",
  invalidXMLEntity: "Invalid XML entity: {entity}"
  // invalidXMLEntity: "Invalid XML {COUNT, plural, =0 {entity: } =1 {entity: } other {entities: } } {ENTITY}",
}

export default en
