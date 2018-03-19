// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { hideSettings } from '../actions'
import {
  updateSetting,
  updateValidationSetting
} from '../actions/settings-actions'
import { Button } from 'react-bootstrap'
import Icon from '../../components/Icon'
import SettingsOptions from '../components/SettingsOptions'
import {
  getEnterSavesImmediately,
  getSyntaxHighlighting,
  getValidateHtmlXml,
  getValidateNewLine,
  getValidateTab,
  getValidateJavaVariables,
  getValidateXmlEntity,
  getValidatePrintfVariables,
  getValidatePrintfXsi
 } from '../reducers'
import {
  ENTER_SAVES_IMMEDIATELY,
  SYNTAX_HIGHLIGTING,
  // Validation options
  HTML_XML,
  NEW_LINE,
  TAB,
  JAVA_VARIABLES,
  XML_ENTITY,
  PRINTF_VARIABLES,
  PRINTF_XSI_EXTENSION
} from '../reducers/settings-reducer'

// Validator error types
// TODO: convert to tuple
export const ERROR = 'Error'
export const WARNING = 'Warning'
export const OFF = 'Off'

export const SettingsPanel = ({
  enterSavesImmediately,
  syntaxHighligting,
  validateHtmlXml,
  validateNewLine,
  validateTab,
  validateJavaVariables,
  validateXmlEntity,
  validatePrintfVariables,
  validatePrintfXsi,
  hideSettings,
  updateSetting,
  updateValidationSetting,
  isRTL
}) => {
  const directionClass = isRTL ? 'rtl' : 'ltr'
  const validatorChecked = (validator) => {
    return (validator === ERROR || validator === WARNING)
  }
  return (
    <div>
      <h1 className="SidebarEditor-heading">
        <Icon name="settings" className="s1" /> Settings
        <span className="s1 u-pullRight">
          <Button bsStyle="link" onClick={hideSettings}>
            <Icon name="cross" />
          </Button>
        </span>
      </h1>
      <div className={directionClass + ' SidebarEditor-wrapper'}>
        <div>
          <h2 className='SettingsHeading'>Editor options</h2>
          <SettingsOptions
            settings={[
              {
                id: ENTER_SAVES_IMMEDIATELY,
                label: 'Enter key saves immediately',
                active: enterSavesImmediately
              },
              {
                id: SYNTAX_HIGHLIGTING,
                label: 'Syntax Highlighting',
                active: syntaxHighligting
              }
            ]}
            updateSetting={updateSetting} />
          <h2 className='SettingsHeading'>Validation options</h2>
          <SettingsOptions
            settings={[
              {
                id: HTML_XML,
                label: 'HTML/XML tags',
                active: validatorChecked(validateHtmlXml),
                disabled: validateHtmlXml === ERROR
              },
              {
                id: JAVA_VARIABLES,
                label: 'Java variables',
                active: validatorChecked(validateJavaVariables),
                disabled: validateJavaVariables === ERROR
              },
              {
                id: NEW_LINE,
                label: 'Leading/trailing newline (\\n)',
                active: validatorChecked(validateNewLine),
                disabled: validateNewLine === ERROR
              },
              {
                id: PRINTF_XSI_EXTENSION,
                label: 'Positional printf (XSI extention)',
                active: validatorChecked(validatePrintfXsi)
              },
              {
                id: PRINTF_VARIABLES,
                label: 'Printf variables',
                active: validatorChecked(validatePrintfVariables)
              },
              {
                id: TAB,
                label: 'Tab characters (\\t)',
                active: validatorChecked(validateTab)
              },
              {
                id: XML_ENTITY,
                label: 'XML entity reference',
                active: validatorChecked(validateXmlEntity)
              }
            ]}
            updateSetting={updateValidationSetting} />
        </div>
      </div>
    </div>
  )
}

SettingsPanel.propTypes = {
  enterSavesImmediately: PropTypes.bool.isRequired,
  syntaxHighligting: PropTypes.bool.isRequired,
  validateHtmlXml: PropTypes.string.isRequired,
  validateNewLine: PropTypes.string.isRequired,
  validateTab: PropTypes.string.isRequired,
  validateJavaVariables: PropTypes.string.isRequired,
  validateXmlEntity: PropTypes.string.isRequired,
  validatePrintfVariables: PropTypes.string.isRequired,
  validatePrintfXsi: PropTypes.string.isRequired,
  hideSettings: PropTypes.func.isRequired,
  updateSetting: PropTypes.func.isRequired,
  updateValidationSetting: PropTypes.func.isRequired,
  isRTL: PropTypes.bool.isRequired
}

const mapStateToProps = (state) => {
  const {context, ui} = state
  const targetLocaleDetails = ui.uiLocales[context.lang]

  return {
    enterSavesImmediately: getEnterSavesImmediately(state),
    syntaxHighligting: getSyntaxHighlighting(state),
    validateHtmlXml: getValidateHtmlXml(state),
    validateNewLine: getValidateNewLine(state),
    validateTab: getValidateTab(state),
    validateJavaVariables: getValidateJavaVariables(state),
    validateXmlEntity: getValidateXmlEntity(state),
    validatePrintfVariables: getValidatePrintfVariables(state),
    validatePrintfXsi: getValidatePrintfXsi(state),
    isRTL: targetLocaleDetails ? targetLocaleDetails.isRTL || false
        : false
  }
}

export default connect(mapStateToProps,
  { hideSettings, updateSetting, updateValidationSetting })(SettingsPanel)
