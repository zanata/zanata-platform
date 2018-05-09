/* tslint:disable:max-line-length*/
export default interface ValidationMessages {
    ab(a: number, b: number): string
//    @DefaultMessage("Validation error - See validation message")
    notifyValidationError(): string

// Newline validator
//    @DefaultMessage("Check for consistent leading and trailing newline (\\n).")
    newLineValidatorDesc(): string

//    @DefaultMessage("Leading newline (\\n) is missing")
    leadingNewlineMissing(): string

//    @DefaultMessage("Unexpected leading newline (\\n)")
    leadingNewlineAdded(): string

//    @DefaultMessage("Trailing newline (\\n) is missing")
    trailingNewlineMissing(): string

//    @DefaultMessage("Unexpected trailing newline (\\n)")
    trailingNewlineAdded(): string

// Tab validator
//    @DefaultMessage("Check whether source and target have the same number of tabs.")
    tabValidatorDesc(): string

//    @DefaultMessage("Target has fewer tabs (\\t) than source (source: {0}, target: {1})")
    targetHasFewerTabs(sourceTabs: number, targetTabs: number): string

//    @DefaultMessage("Target has more tabs (\\t) than source (source: {0}, target: {1})")
    targetHasMoreTabs(sourceTabs: number, targetTabs: number): string

// Printf variables validator
//    @DefaultMessage("Check that printf style (%x) variables are consistent.")
    printfVariablesValidatorDesc(): string

//    @DefaultMessage("Variable {0} position is out of range")
    varPositionOutOfRange(varAdded: string): string

//    @DefaultMessage("Numbered arguments cannot mix with unnumbered arguments")
    mixVarFormats(): string

//    @DefaultMessage("Variables have same position: {0,collection,string}")
    varPositionDuplicated(vars: string[]): string

// Java variables validator
//    @DefaultMessage("Check that java style ('{x}') variables are consistent.")
    javaVariablesValidatorDesc(): string

//    @Description("Lists variables that appear a different number of times between source and target strings")
//    @DefaultMessage("Inconsistent count for variables: {0,list,string}")
//    @AlternateMessage("one", "Inconsistent count for variable: {0,list,string}")
    differentVarCount(/*@PluralCount*/ vars: string[]): string

//    @DefaultMessage("Number of apostrophes ('') in source does not match number in translation. This may lead to other warnings.")
    differentApostropheCount(): string

//    @DefaultMessage("Quoted characters found in translation but not in source text. "
//            + "Apostrophe character ('') must be doubled ('''') to prevent quoting "
//            + "when it is used in Java MessageFormat strings.")
    quotedCharsAdded(): string

// Shared variables validator messages
//    @DefaultMessage("Check that positional printf style (%n\$x) variables are consistent.")
    printfXSIExtensionValidationDesc(): string

//    @Description("Lists the variables that are in the original string but have not been included in the target")
//    @DefaultMessage("Missing variables: {0,list,string}")
//    @AlternateMessage("one", "Missing variable: {0,list,string}")
    varsMissing(/*@PluralCount*/ vars: string[]): string

//    @Description("Lists the variables that are in the original string and are present but quoted in the target")
//    @DefaultMessage("Unexpected quoting of variables: {0,list,string}")
//    @AlternateMessage("one", "Unexpected quoting of variable: {0,list,string}")
    varsMissingQuoted(/*@PluralCount*/ vars: string[]): string

//    @Description("Lists the variables that are in the target but are not in the original string")
//    @DefaultMessage("Unexpected variables: {0,list,string}")
//    @AlternateMessage("one", "Unexpected variable: {0,list,string}")
    varsAdded(/*@PluralCount*/ vars: string[]): string

//    @Description("Lists the variables that are in the target and are present but quoted in the original string")
//    @DefaultMessage("Variables not quoted: {0,list,string}")
//    @AlternateMessage("one", "Variable not quoted: {0,list,string}")
    varsAddedQuoted(/*@PluralCount*/ vars: string[]): string

    // XHM/HTML tag validator
//    @DefaultMessage("Check that XML/HTML tags are consistent.")
    xmlHtmlValidatorDesc(): string

//    @Description("Lists the xml or html tags that are in the target but are not in the original string")
//    @DefaultMessage("Unexpected tags: {0,list,string}")
//    @AlternateMessage("one", "Unexpected tag: {0,list,string}")
    tagsAdded(/*@PluralCount*/ tags: string[]): string

    //    @Description("Lists the xml or html tags that are in the original string but have not been included in the target")
    //    @DefaultMessage("Missing tags: {0,list,string}")
    //    @AlternateMessage("one", "Missing tag: {0,list,string}")
    tagsMissing(/*@PluralCount*/ tags: string[]): string

    //    @DefaultMessage("Tags in unexpected position: {0,list,string}")
    //    @AlternateMessage("one", "Tag in unexpected position: {0,list,string}")
    tagsWrongOrder(/*@PluralCount*/ tags: string[]): string

    // XML Entity validator
//    @DefaultMessage("Check that XML entity are complete.")
    xmlEntityValidatorDesc(): string

    // @DefaultMessage("Invalid XML entity [ {0} ]")
    invalidXMLEntity(entity: string): string
}
