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
  getSyntaxHighlighting
 } from '../reducers'
import {
  ENTER_SAVES_IMMEDIATELY,
  SYNTAX_HIGHLIGTING
} from '../reducers/settings-reducer'

export const SettingsPanel = ({
  enterSavesImmediately,
  syntaxHighligting,
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
        </div>
      </div>
    </div>
  )
}

SettingsPanel.propTypes = {
  enterSavesImmediately: PropTypes.bool.isRequired,
  syntaxHighligting: PropTypes.bool.isRequired,
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
    isRTL: targetLocaleDetails ? targetLocaleDetails.isRTL || false
        : false
  }
}

export default connect(mapStateToProps,
  { hideSettings, updateSetting })(SettingsPanel)
