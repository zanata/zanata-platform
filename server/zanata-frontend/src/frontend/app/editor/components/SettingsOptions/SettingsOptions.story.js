import React from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@storybook/react'
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
        settings={this.state.settings}
        updateSetting={this.updateSetting} />
    )
  }
}

const listUnchecked =
  [
    {
      id: 'list-item-1',
      label: 'List item 1',
      active: false
    },
    {
      id: 'list-item-2',
      label: 'List item 2',
      active: false
    },
    {
      id: 'list-item-3',
      label: 'List item 3',
      active: false
    },
    {
      id: 'list-item-4',
      label: 'List item 4',
      active: false
    }
  ]
const listHalfChecked =
  [
    {
      id: 'list-item-1',
      label: 'List item 1',
      active: false
    },
    {
      id: 'list-item-2',
      label: 'List item 2',
      active: true
    },
    {
      id: 'list-item-3',
      label: 'List item 3',
      active: false
    },
    {
      id: 'list-item-4',
      label: 'List item 4',
      active: true
    }
  ]

const listAllChecked =
  [
    {
      id: 'list-item-1',
      label: 'List item 1',
      active: true
    },
    {
      id: 'list-item-2',
      label: 'List item 2',
      active: true
    },
    {
      id: 'list-item-3',
      label: 'List item 3',
      active: true
    },
    {
      id: 'list-item-4',
      label: 'List item 4',
      active: true
    }
  ]

const settings =
  [
    {
      id: 'key-saves',
      label: 'Enter key saves immediately',
      active: true
    },
    {
      id: 'syntax-highlight',
      label: 'Syntax highlighting',
      active: false
    }
  ]

const defaults =
  [
    {
      id: 'suggestions-diff',
      label: 'Suggestions diff',
      active: false
    },
    {
      id: 'panel-layout',
      label: 'Panel layout',
      active: false
    }
  ]

const validations =
  [
    {
      id: 'html-xml-tags',
      label: 'HTML/XML tags',
      active: true
    },
    {
      id: 'java-variables',
      label: 'Java variables',
      active: true
    },
    {
      id: 'leading-trailing-newline',
      label: 'Leading/trailing newline',
      active: true
    },
    {
      id: 'positional-printf',
      label: 'Positional printf (XSI extension)',
      active: false
    },
    {
      id: 'printf-variables',
      label: 'Printf variables',
      active: false
    },
    {
      id: 'tab-characters',
      label: 'Tab characters',
      active: true
    },
    {
      id: 'xml-entity-reference',
      label: 'XML entity reference',
      active: false
    }
  ]

storiesOf('SettingsOptions', module)
  .add('default - unchecked', () => (
    <SettingsOptions
      settings={listUnchecked}
      updateSetting={action('updateSetting')} />
  ))
  .add('default - half-checked', () => (
    <SettingsOptions
      settings={listHalfChecked}
      updateSetting={action('updateSetting')} />
  ))
  .add('default - all checked', () => (
    <SettingsOptions
      settings={listAllChecked}
      updateSetting={action('updateSetting')} />
  ))
  .add('EDITOR OPTIONS', () => (
    <div>
      <h2 className='SettingsHeading'>Editor options</h2>
      <SettingsOptions
        settings={settings}
        updateSetting={action('updateSetting')} />
      <h3 className='SettingsHeading'>Set current layouts as default:</h3>
      <SettingsOptions
        settings={defaults}
        updateSetting={action('updateSetting')} />
    </div>
  ))
  .add('VALIDATION SETTINGS', () => (
    <div>
      <h3 className='SettingsHeading'>Validation settings</h3>
      <SettingsOptions
        settings={validations}
        updateSetting={action('updateSetting')} />
    </div>
  ))
