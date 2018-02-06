import React from 'react'
import * as PropTypes from 'prop-types'
import { Checkbox } from 'react-bootstrap'

class SettingOption extends React.Component {
  static propTypes = {
    id: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    active: PropTypes.bool.isRequired,
    /* arguments: (string: settingId, bool: active) */
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
