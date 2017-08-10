import PropTypes from 'prop-types'

export const TMMergeOptionsValuePropType = {
  differentDocId: PropTypes.bool.isRequired,
  differentContext: PropTypes.bool.isRequired,
  fromImportedTM: PropTypes.bool.isRequired,
  ignoreDifferentDocId: PropTypes.bool.isRequired,
  ignoreDifferentContext: PropTypes.bool.isRequired,
  importedTMCopyAsTranslated: PropTypes.bool.isRequired
}

export const TMMergeOptionsCallbackPropType = {
  onDocIdCheckboxChange: PropTypes.func.isRequired,
  onContextCheckboxChange: PropTypes.func.isRequired,
  onImportedCheckboxChange: PropTypes.func.isRequired,
  onIgnoreDifferentDocIdChange: PropTypes.func.isRequired,
  onIgnoreDifferentContextChange: PropTypes.func.isRequired,
  onImportedTMCopyRuleChange: PropTypes.func.isRequired
}
