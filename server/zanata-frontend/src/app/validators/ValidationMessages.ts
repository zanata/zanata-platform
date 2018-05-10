/* tslint:disable:max-line-length*/
export default interface ValidationMessages {
    ab: string
    notifyValidationError: string

// Newline validator

    newLineValidatorDesc: string
    leadingNewlineMissing: string
    leadingNewlineAdded: string
    trailingNewlineMissing: string
    trailingNewlineAdded: string

// Tab validator

    tabValidatorDesc: string
    targetHasFewerTabs: string
    targetHasMoreTabs: string

// Printf variables validator

    printfVariablesValidatorDesc: string
    varPositionOutOfRange: string
    mixVarFormats: string
    varPositionDuplicated: string
// Java variables validator

    javaVariablesValidatorDesc: string
    // @Description("Lists variables that appear a different number of times between source and target strings")
    differentVarCount: string
    differentApostropheCount: string
    quotedCharsAdded: string

// Shared variables validator messages

    printfXSIExtensionValidationDesc: string
    // @Description("Lists the variables that are in the original string but have not been included in the target")
    varsMissing: string
    // @Description("Lists the variables that are in the original string and are present but quoted in the target")
    varsMissingQuoted: string
    // @Description("Lists the variables that are in the target but are not in the original string")
    varsAdded: string
    // @Description("Lists the variables that are in the target and are present but quoted in the original string")
    varsAddedQuoted: string

// XHM/HTML tag validator

    xmlHtmlValidatorDesc: string
    // @Description("Lists the xml or html tags that are in the target but are not in the original string")
    tagsAdded: string
    // @Description("Lists the xml or html tags that are in the original string but have not been included in the target")
    tagsMissing: string
    tagsWrongOrder: string

// XML Entity validator

    xmlEntityValidatorDesc: string
    invalidXMLEntity: string
}
