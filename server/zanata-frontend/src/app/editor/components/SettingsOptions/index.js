// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import SettingOption from '../SettingOption'

const SettingsOptions = ({settings, updateSetting, disabled}) => {
  const checkboxes = settings.map((setting, index) => (
    <li key={index}>
      <SettingOption
        updateSetting={updateSetting}
        disabled={disabled}
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
    id: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    active: PropTypes.bool.isRequired
  })).isRequired,
  /* arguments: (string: settingId, bool: active) */
  updateSetting: PropTypes.func.isRequired,
  disabled: PropTypes.bool
}

export default SettingsOptions
