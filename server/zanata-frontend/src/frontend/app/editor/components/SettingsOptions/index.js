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
  settings: PropTypes.arrayOf(PropTypes.shape({
    // FIXME update to appropriate type
    id: PropTypes.any.isRequired,
    label: PropTypes.string.isRequired,
    active: PropTypes.bool.isRequired
  })).isRequired,
  /* arguments: (any: settingId, bool: active) */
  updateSetting: PropTypes.func.isRequired
}

export default SettingsOptions
