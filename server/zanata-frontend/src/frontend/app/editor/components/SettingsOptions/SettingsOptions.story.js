import React from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@kadira/storybook'
import RealSettingsOptions from '.'

class SettingsOptions extends React.Component {
  static propTypes = {
    settings: PropTypes.shape.isRequired,
    updateSettingsOption: PropTypes.func.isRequired
  }
  constructor (props) {
    super(props)
    this.state = props.state
  }
  updateSettingsOption = (settings, checked) => {
    // record the check state in the wrapper
    this.setState({ [settings]: checked })
    // call the real one that was passed in
    this.props.updateSettingsOption(settings, checked)
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

storiesOf('SettingsOptions', module)
    .add('default', () => (
        <SettingsOptions
            settings={{
              id: 'Ambulance',
              label: 'Krankenwagen',
              active: {true}
            }}
            updateSettingsOption={action(updateAction)} />
    ))

