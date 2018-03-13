// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { hideSettings } from '../actions'
import { updateSetting } from '../actions/settings-actions'
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
  isRTL
}) => {
  const directionClass = isRTL ? 'rtl' : 'ltr'
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
                active: validateHtmlXml
              },
              {
                id: JAVA_VARIABLES,
                label: 'Java variables',
                active: validateJavaVariables
              },
              {
                id: NEW_LINE,
                label: 'Leading/trailing newline (\\n)',
                active: validateNewLine
              },
              {
                id: PRINTF_XSI_EXTENSION,
                label: 'Positional printf (XSI extention)',
                active: validatePrintfXsi
              },
              {
                id: PRINTF_VARIABLES,
                label: 'Printf variables',
                active: validatePrintfVariables
              },
              {
                id: TAB,
                label: 'Tab characters (\\t)',
                active: validateTab
              },
              {
                id: XML_ENTITY,
                label: 'XML entity reference',
                active: validateXmlEntity
              }
            ]}
            updateSetting={updateSetting} />
        </div>
      </div>
    </div>
  )
}

SettingsPanel.propTypes = {
  enterSavesImmediately: PropTypes.bool.isRequired,
  syntaxHighligting: PropTypes.bool.isRequired,
  validateHtmlXml: PropTypes.bool.isRequired,
  validateNewLine: PropTypes.bool.isRequired,
  validateTab: PropTypes.bool.isRequired,
  validateJavaVariables: PropTypes.bool.isRequired,
  validateXmlEntity: PropTypes.bool.isRequired,
  validatePrintfVariables: PropTypes.bool.isRequired,
  validatePrintfXsi: PropTypes.bool.isRequired,
  hideSettings: PropTypes.func.isRequired,
  updateSetting: PropTypes.func.isRequired,
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
  { hideSettings, updateSetting })(SettingsPanel)
