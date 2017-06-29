import React from 'react'
import PropTypes from 'prop-types'
import { Checkbox } from 'react-bootstrap'

class SettingOption extends React.Component {
  propTypes = {
    setting: PropTypes.shape({
      id: PropTypes.any.isRequired, // I will update this to whatever I use when I wire it up
      label: PropTypes.string.isRequired,
      active: PropTypes.bool.isRequired
    }).isRequired,
    /* arguments: (any: settingId, bool: active) */
    updateSetting: PropTypes.func.isRequired
  }

  onChange = () => {
    this.props.updateSetting(this.props.setting, event.target.checked)
  }

  render () {
    const { setting, checked } = this.props
    return (
        <Checkbox checked={checked}
           onChange={this.onChange}>
          {setting}
        </Checkbox>
    )
  }
}

export default SettingOption
