import React from 'react'
import PropTypes from 'prop-types'
import { Checkbox } from 'react-bootstrap'
import SettingOption from '../SettingOption'

const settings =
/* add props here */


const SettingsOptions = ({states, updateSettingsOption}) => {
  const checkboxes = settings.map((setting, index) => (
    <li key={index}>
      <SettingOption
          setting={setting}
          checked={states[setting]}
          onChange={updateSettingsOption} />
    </li>
  ))

  return (
    <div className="settings-options">
      <ul>
        {checkboxes}
      </ul>
    </div>
  )
}

SettingsOptions.propTypes = {
  settings: PropTypes.arrayOf(shape(
      id: PropTypes.any.isRequired, // I will update this to whatever I use when I wire it up
      label: PropTypes.string.isRequired,
      active: PropTypes.bool.isRequired
  )).isRequired
  /* arguments: (any: settingId, bool: active) */
  updateSetting: PropTypes.func.isRequired
}

export default SettingsOptions
