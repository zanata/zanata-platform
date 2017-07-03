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
const validations =
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
    .add('default -all checked', () => (
        <SettingsOptions
            settings={listAllChecked}
            updateSetting={action('updateSetting')} />
    ))
    .add('VALIDATION SETTINGS', () => (
    <div>
        <h2>Validation settings</h2>
        <SettingsOptions
            settings={validations}
            updateSetting={action('updateSetting')} />
    </div>
    ))

