import React from 'react'
import PropTypes from 'prop-types'
import SettingOption from '../SettingOption'

const SettingsOptions = ({states, updateSettingOption}) => {
  const settings =
      ['HTML/XML tags',
        'Java variables',
        'Leading/trailing newline (n)',
        'Positional printf (XSI extension)',
        'Printf variables',
        'Tab characters (t)',
        'XML entity reference']
  const checkboxes = settings.map((settings, index) => (
      <li key={index}>
        <SettingOption
            setting={settings}
            updateSetting={updateSettingOption}/>
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
  updateSettingOption: PropTypes.func.isRequired
}

export default SettingsOptions
