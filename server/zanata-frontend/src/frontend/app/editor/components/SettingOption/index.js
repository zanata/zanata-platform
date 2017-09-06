import React from 'react'
import PropTypes from 'prop-types'
import { Checkbox } from 'react-bootstrap'

class SettingOption extends React.Component {
  propTypes = {
    setting: PropTypes.shape({
      // FIXME update type
      id: PropTypes.any.isRequired,
      label: PropTypes.string.isRequired,
      active: PropTypes.bool.isRequired
    }).isRequired,
    /* arguments: (any: settingId, bool: active) */
    updateSetting: PropTypes.func.isRequired
  }

  updateSetting = (event) => {
    this.props.updateSetting(this.props.setting.id, event.target.checked)
  }

  render () {
    const { label, active } = this.props.setting
    return (
      <Checkbox checked={active}
        onChange={this.updateSetting}>
        &nbsp;{label}
      </Checkbox>
    )
  }
}

export default SettingOption
