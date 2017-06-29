import React from 'react'
import PropTypes from 'prop-types'
import SettingOption from '../SettingOption'

class SettingsOptions extends React.Component {
  static propTypes = {
    settings: PropTypes.shape({
      id: PropTypes.any.isRequired, // I will update this to whatever I use when I wire it up
      label: PropTypes.string.isRequired,
      active: PropTypes.bool.isRequired
    }).isRequired,
    /* arguments: (any: settingId, bool: active) */
    updateSettingsOption: PropTypes.func.isRequired
  }

  onChange = (event) => {
    this.props.onChange(this.props.setting, event.target.checked)
  }

  render () {
  const { setting, checked } = this.props
  const checkboxes = (
    <li>
      <SettingOption
          setting={setting}
          checked={true}
          onChange={this.onChange} />
    </li>
  )

  return (
    <div className="settings-options">
      <ul>
        {checkboxes}
      </ul>
    </div>
  )
  }
}

export default SettingsOptions
