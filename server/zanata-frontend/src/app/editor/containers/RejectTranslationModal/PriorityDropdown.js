import React, { Component } from 'react'
import { Icon } from '../../../components'
import PropTypes from 'prop-types'
import Dropdown from '../../components/Dropdown'

// TODO: Move these to a shared config util file
export const MINOR = 'Minor'
export const MAJOR = 'Major'
export const CRITICAL = 'Critical'

/**
 * A Local Editor Dropdown coponent that selects the Priority of a
 * translation rejection message
 */
class ProirityDropdown extends Component {
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
  render () {
    const { textState, priority, priorityChange } = this.props
    return (
      <span className="PriorityDropdown">
        <Icon name="warning" className="s2"
          parentClassName="u-textWarning" />
        <span id="PriorityTitle">Priority</span>
        <Dropdown enabled isOpen={this.state.dropdownOpen}
          onToggle={this.toggleDropdown}
          className="dropdown-menu priority">
          <Dropdown.Button>
            <a className="EditorDropdown-item">
              <span className={textState}>{priority}</span>
              <Icon className="n1" name="chevron-down" />
            </a>
          </Dropdown.Button>
          <Dropdown.Content>
            <ul>
              <li className="EditorDropdown-item" onClick={priorityChange}>
                <span>Minor</span></li>
              <li className="EditorDropdown-item" onClick={priorityChange}>
                <span className="u-textWarning">Major</span></li>
              <li className="EditorDropdown-item" onClick={priorityChange}>
                <span className="u-textDanger">Critical</span></li>
            </ul>
          </Dropdown.Content>
        </Dropdown>
      </span>
    )
  }
}
export default ProirityDropdown
