import React from 'react'
import PropTypes from 'prop-types'
import { Checkbox } from 'react-bootstrap'

const settings =
    ['Enter key saves immediately',
      'Syntax highlighting']

const SettingsOptions = ({states, updateSettingsOption}) => {
  const checkboxes = settings.map((setting, index) => (
    <li key={index}>
      <SettingsCheckbox
          setting={setting}
          checked={states[setting]}
          onChange={updateSettingsOption} />
    </li>
  ))

  return (
    <div className="settings-options">
      <ul>
        {checkboxes}
      </ul>
    </div>
  )
}

SettingsOptions.propTypes = {
  states: PropTypes.shape({
    'Enter key saves immediately': PropTypes.bool.isRequired,
    'Syntax highlighting': PropTypes.bool.isRequired,
    'Suggestions diff': PropTypes.bool.isRequired,
    'Panel layout': PropTypes.bool.isRequired,
  }).isRequired,
  updateSettingsOption: PropTypes.func.isRequired
}

class SettingsCheckbox extends React.Component {
  static propTypes = {
    setting: PropTypes.string.isRequired,
    checked: PropTypes.bool.isRequired,
    /* Will be called with (validation, newValue) */
    onChange: PropTypes.func.isRequired
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

export default SettingsOptions
