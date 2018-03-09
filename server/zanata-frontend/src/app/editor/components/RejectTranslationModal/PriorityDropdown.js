import React from 'react'
import { Component } from 'react'
import { Icon } from '../../../components'
import * as PropTypes from 'prop-types'
import Dropdown from '../../components/Dropdown'
import { MINOR, MAJOR, CRITICAL } from '../../utils/reject-trans-util'

/**
 * A Local Editor Dropdown coponent that selects the Priority of a
 * translation rejection message
 */
class PriorityDropdownrityDropdown extends Component {
  static propTypes = {
    textState: PropTypes.string.isRequired,
    priority: PropTypes.oneOf(
      [
        MINOR,
        MAJOR,
        CRITICAL
      ]
    ).isRequired,
    priorityChange: PropTypes.func.isRequired
  }
  constructor (props) {
    super(props)
    this.state = {
      dropdownOpen: false
    }
  }
  toggleDropdown = () => {
    this.setState(prevState => ({
      dropdownOpen: !prevState.dropdownOpen
    }))
  }
  onPriorityChange = (event) => {
    this.props.priorityChange(event)
    this.toggleDropdown()
  }
  render () {
    const { textState, priority } = this.props
    return (
      <span className='PriorityDropdown'>
        <Icon name='warning' className='s2'
          parentClassName='u-textWarning' />
        <span id='PriorityTitle'>Priority</span>
        <Dropdown enabled isOpen={this.state.dropdownOpen}
          onToggle={this.toggleDropdown}
          className='dropdown-menu priority'>
          <Dropdown.Button>
            <a className='EditorDropdown-item'>
              <span className={textState}>{priority}</span>
              <span className='arrow'>
                <Icon className='n1' name='chevron-down' />
              </span>
            </a>
          </Dropdown.Button>
          <Dropdown.Content>
            <ul>
              <li className='EditorDropdown-item'
                onClick={this.onPriorityChange}>
                <span>Minor</span></li>
              <li className='EditorDropdown-item'
                onClick={this.onPriorityChange}>
                <span className='u-textWarning'>Major</span></li>
              <li className='EditorDropdown-item'
                onClick={this.onPriorityChange}>
                <span className='u-textDanger'>Critical</span></li>
            </ul>
          </Dropdown.Content>
        </Dropdown>
      </span>
    )
  }
}
export default PriorityDropdown
