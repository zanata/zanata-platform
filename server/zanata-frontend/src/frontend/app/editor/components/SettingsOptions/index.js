import React from 'react'
import PropTypes from 'prop-types'
import SettingOption from '../SettingOption'

const SettingsOptions = ({settings, updateSetting}) => {

  const checkboxes = settings.map((setting, index) => (
    <li key={index}>
      <SettingOption
         updateSetting={updateSetting}
         {...setting} />
    </li>
  ))
  return (
    <div>
      <ul>
        {checkboxes}
      </ul>
    </div>
  )
}

SettingsOptions.propTypes = {
  settings: PropTypes.shape({
    id: PropTypes.any.isRequired, // I will update this to whatever I use when I wire it up
    label: PropTypes.string.isRequired,
    active: PropTypes.bool.isRequired
  }).isRequired,
  /* arguments: (any: settingId, bool: active) */
  updateSetting: PropTypes.func.isRequired
}

export default SettingsOptions
