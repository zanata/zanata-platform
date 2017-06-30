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

  updateSetting = (event) => {
    this.props.updateSetting(this.props.id, event.target.checked)
  }

  render () {
    const { label, active } = this.props
    return (
      <Checkbox checked={active}
        onChange={this.updateSetting}>
        &nbsp;{label}
      </Checkbox>
    )
  }
}

export default SettingOption
