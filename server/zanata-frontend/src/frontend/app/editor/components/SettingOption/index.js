import React from 'react'
import PropTypes from 'prop-types'
import { Checkbox } from 'react-bootstrap'

const setting =

class SettingOption extends React.Component {
  setting: PropTypes.shape({
  id: PropTypes.any.isRequired, // I will update this to whatever I use when I wire it up
  label: PropTypes.string.isRequired,
  active: PropTypes.bool.isRequired
  }).isRequired
  /* arguments: (any: settingId, bool: active) */
  updateSetting: PropTypes.func.isRequired
  }

  onChange = (event) => {
    this.props.onChange(this.props.setting, event.target.checked)
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
