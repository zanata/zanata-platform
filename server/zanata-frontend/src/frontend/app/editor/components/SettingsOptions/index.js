import React from 'react'
import PropTypes from 'prop-types'
import SettingOption from '../SettingOption'

const settings =
    ['HTML/XML tags',
      'Java variables',
      'Leading/trailing newline (n)',
      'Positional printf (XSI extension)',
      'Printf variables',
      'Tab characters (t)',
      'XML entity reference']

const SettingsOptions = ({states, updateSettingOption}) => {
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

export default SettingsOptions
