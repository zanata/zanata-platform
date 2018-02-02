// @ts-nocheck
import React from 'react'
import * as PropTypes from 'prop-types'
import { storiesOf, action } from '@storybook/react'
import RealSettingOption from '.'

class SettingOption extends React.Component {
  static propTypes = {
    id: PropTypes.any.isRequired,
    label: PropTypes.string.isRequired,
    active: PropTypes.bool.isRequired,
    updateSetting: PropTypes.func.isRequired
  }
  constructor (props) {
    super(props)
    this.state = {active: props.active}
  }
  updateSetting = (id, active) => {
    // record the check state in the wrapper
    this.setState({ active: active })
    // call the real one that was passed in
    this.props.updateSetting(id, active)
  }
  render () {
    return (
      <RealSettingOption
        updateSetting={this.updateSetting}
        id={this.props.id}
        label={this.props.label}
        active={this.state.active} />
    )
  }
}

storiesOf('SettingOption', module)
  .add('default', () => (
    <SettingOption
      id='list-item-1'
      label='List item 1'
      active
      updateSetting={action('updateSetting')} />
  ))
