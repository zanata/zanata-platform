import React from 'react'
import { Component } from 'react'
import { Icon } from '../../../components'
import * as PropTypes from 'prop-types'
import Dropdown from '../../components/Dropdown'
import {
  MINOR, MAJOR, CRITICAL, UNSPECIFIED
} from '../../utils/reject-trans-util'

/**
 * A Local Editor Dropdown coponent that selects the Criteria
 * for a translation rejection
 */
class CriteriaDropdown extends Component {
  static propTypes = {
    criteriaList: PropTypes.arrayOf(PropTypes.shape({
      commentRequired: PropTypes.bool.isRequired,
      description: PropTypes.string.isRequired,
      priority: PropTypes.oneOf([MINOR, MAJOR, CRITICAL]).isRequired
    })).isRequired,
    onCriteriaChange: PropTypes.func.isRequired,
    onUnspecifiedCriteria: PropTypes.func.isRequired,
    criteriaDescription: PropTypes.string.isRequired
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
  onCriteriaChange = (event) => {
    if (event.target.innerText === UNSPECIFIED.description) {
      this.props.onUnspecifiedCriteria()
    } else {
      this.props.onCriteriaChange(event)
    }
    this.toggleDropdown()
  }
  render () {
    const { criteriaList, criteriaDescription } = this.props
    const options = criteriaList.map((value, index) => {
      return (
        <li key={index}
          className='EditorDropdown-item'
          onClick={this.onCriteriaChange}>
          {value.description}
        </li>
      )
    })
    return (
      <Dropdown enabled isOpen={this.state.dropdownOpen}
        onToggle={this.toggleDropdown}
        className='dropdown-menu Criteria'>
        <Dropdown.Button>
          <a className='EditorDropdown-item ellipsis'>
            {criteriaDescription}
            <span className='arrow'>
              <Icon className='n1' name='chevron-down' />
            </span>
          </a>
        </Dropdown.Button>
        <Dropdown.Content>
          <ul>
            {options}
          </ul>
        </Dropdown.Content>
      </Dropdown>
    )
  }
}
export default CriteriaDropdown
