import React from 'react'
import PropTypes from 'prop-types'
import { connect } from 'react-redux'
import { hideSettings } from '../actions'
import { updateSetting } from '../actions/settings-actions'
import { Button } from 'react-bootstrap'
import Icon from '../../components/Icon'
import SettingsOptions from '../components/SettingsOptions'
import { getEnterSavesImmediately } from '../reducers'
import { ENTER_SAVES_IMMEDIATELY } from '../reducers/settings-reducer'

export const SettingsPanel = ({
  enterSavesImmediately,
  hideSettings,
  updateSetting
}) => {
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
      <div className="SidebarEditor-wrapper">
        <div>
          <h2 className='SettingsHeading'>Editor options</h2>
          <SettingsOptions
            settings={[
              {
                id: ENTER_SAVES_IMMEDIATELY,
                label: 'Enter key saves immediately',
                active: enterSavesImmediately
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
  hideSettings: PropTypes.func.isRequired,
  updateSetting: PropTypes.func.isRequired
}

const mapStateToProps = (state) => {
  return {
    enterSavesImmediately: getEnterSavesImmediately(state)
  }
}

export default connect(mapStateToProps,
  { hideSettings, updateSetting })(SettingsPanel)
