import React from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@kadira/storybook'
import RealSettingsOptions from '.'

/* Wrapper class for storybook.

 * The checkbox states will be stored and updated in the live
 * app. This wrapper stores the states so that we can see
 * them working in storybook too.
 */
class SettingsOptions extends React.Component {
  static propTypes = {
    states: PropTypes.object.isRequired,
    updateSettingsOption: PropTypes.func.isRequired
  }
  constructor (props) {
    super(props)
    this.state = props.states
  }
  updateValidationOption = (setting, checked) => {
    // record the check state in the wrapper
    this.setState({ [setting]: checked })
    // call the real one that was passed in
    this.props.updateSettingsOption(setting, checked)
  }
  render () {
    return (
      <RealSettingsOptions
        updateSettingsOption={this.updateSettingsOption}
        states={this.state} />
    )
  }
}

const updateAction = action('updateSettingsOption')
/*
 * See .storybook/README.md for info on the component storybook.
 */
storiesOf('SettingOptions', module)
  .add('default', () => (
    <SettingsOptions
      updateValidationOption={updateAction}
      states={{
        'Enter key saves immediately': false,
        'Syntax highlighting': false,
        'Suggestions diff': false,
        'Panel layout': false,
      }} />
  ))

  .add('half checked', () => (
      <SettingsOptions
          updateValidationOption={updateAction}
          states={{
            'Enter key saves immediately': true,
            'Syntax highlighting': false,
            'Suggestions diff': true,
            'Panel layout': false,
          }} />
  ))

  .add('all checked', () => (
      <SettingsOptions
          updateValidationOption={updateAction}
          states={{
            'Enter key saves immediately': true,
            'Syntax highlighting': true,
            'Suggestions diff': true,
            'Panel layout': true,
          }} />

  ))
