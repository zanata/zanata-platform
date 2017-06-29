import React from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@kadira/storybook'
import RealSettingsOptions from '.'

class SettingsOptions extends React.Component {
  static propTypes = {
    settings: PropTypes.shape({
      id: PropTypes.any.isRequired, // I will update this to whatever I use when I wire it up
      label: PropTypes.string.isRequired,
      active: PropTypes.bool.isRequired
    }).isRequired,
    /* arguments: (any: settingId, bool: active) */
    updateSetting: PropTypes.func.isRequired
  }
  constructor (props) {
    super(props)
    this.state = props.state
  }
  updateSettingOption = (settings, checked) => {
    // record the check state in the wrapper
    this.setState({ [settings]: checked })
    // call the real one that was passed in
    this.props.updateSettingOption(settings, checked)
  }
  render () {
    return (
        <RealSettingsOptions
            updateSettingOption={this.updateSettingOption}
            states={this.state} />
    )
  }
}

const updateAction = action('updateSettingOption')
const settings =
    ['HTML/XML tags',
      'Java variables',
      'Leading/trailing newline (n)',
      'Positional printf (XSI extension)',
      'Printf variables',
      'Tab characters (t)',
      'XML entity reference']

storiesOf('SettingsOptions', module)
    .add('default', () => (
        <SettingsOptions
            settings={settings}
            updateSettingOption={action(updateAction)} />
    ))

