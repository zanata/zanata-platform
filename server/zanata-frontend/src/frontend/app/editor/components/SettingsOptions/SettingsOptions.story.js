import React from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@kadira/storybook'
import RealSettingsOptions from '.'

class SettingsOptions extends React.Component {
  static propTypes = {
    settings: PropTypes.shape({
      id: PropTypes.any.isRequired,
      label: PropTypes.string.isRequired,
      active: PropTypes.bool.isRequired
    }).isRequired,
    updateSetting: PropTypes.func.isRequired
  }
  constructor (props) {
    super(props)
    this.state = { settings: props.settings }
  }
  updateSetting = (id, active) => {
    // record the check state in the wrapper
    this.setState(newState => ({
      settings: newState.settings.map(setting => {
        if (setting.id === id) {
          return {
            ...setting,
            active
          }
        } else {
          return setting
        }
      })
    }))
    // call the real one that was passed in
    this.props.updateSetting(id, active)
  }
  render () {
    return (
      <RealSettingsOptions
        settings={ this.state.settings }
        updateSetting={ this.updateSetting } />
    )
  }
}

const updateSetting = action('updateSetting')
const settings =
    [
      {
        id: 'html-xml-tags',
        label: 'HTML/XML tags',
        active: false
      },
      {
        id: 'java-variables',
        label: 'Java variables',
        active: true
      },
      {
        id: 'leading-trailing-newline',
        label: 'Leading/trailing newline (n)',
        active: false
      },
      {
        id: 'positional-printf',
        label: 'Positional printf (XSI extension)',
        active: true
      },
      {
        id: 'printf-variables',
        label: 'Printf variables',
        active: false
      },
      {
        id: 'tab-characters',
        label: 'Tab characters (t)',
        active: false
      },
      {
        id: 'xml-entity-reference',
        label: 'XML entity reference',
        active: true
      }
    ]

storiesOf('SettingsOptions', module)
    .add('default', () => (
        <SettingsOptions
            settings={settings}
            updateSetting={action('updateSetting')} />
    ))

