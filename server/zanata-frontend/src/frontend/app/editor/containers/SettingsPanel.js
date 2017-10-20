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

// TODO kgough find out how to set isLtr to false (default). Uncomment
// The location detection will need to be set up correctly
// and then the default of isLtr = false can be removed.

// import { createAction } from 'redux-actions'
// import { LOCALE_SELECTED } from '../actions/header-action-types'

// export const localeDetails = createAction(LOCALE_SELECTED)

export const SettingsPanel = ({
  enterSavesImmediately,
  hideSettings,
  updateSetting
}) => {
  // const directionClass = localeDetails.isLtr ? 'ltr' : 'rtl'
  const directionClass = 'rtl'
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
  updateSetting: PropTypes.func.isRequired,
  directionClass: PropTypes.object.isRequired,
  isLtr: PropTypes.bool.isRequired
}

const mapStateToProps = (state) => {
  return {
    enterSavesImmediately: getEnterSavesImmediately(state)
  }
}

export default connect(mapStateToProps,
  { hideSettings, updateSetting })(SettingsPanel)
