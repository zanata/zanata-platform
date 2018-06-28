import React from 'react'
import * as PropTypes from 'prop-types'
import Checkbox from 'antd/lib/checkbox'
import 'antd/lib/checkbox/style/css'

class SettingOption extends React.Component {
  static propTypes = {
    id: PropTypes.string.isRequired,
    label: PropTypes.string.isRequired,
    active: PropTypes.bool.isRequired,
    /* arguments: (string: settingId, bool: active) */
    updateSetting: PropTypes.func.isRequired,
    disabled: PropTypes.bool
  }

  // @ts-ignore any
  updateSetting = (event) => {
    this.props.updateSetting(this.props.id, event.target.checked)
  }

  render () {
    const { label, active, disabled } = this.props
    return (
      <Checkbox checked={active}
        disabled={disabled}
        onChange={this.updateSetting}>
        &nbsp;{label}
      </Checkbox>
    )
  }
}

export default SettingOption
