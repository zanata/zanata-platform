import React from 'react'
import { Component } from 'react'
import { Icon } from '../../../components'
import * as PropTypes from 'prop-types'
import Dropdown from '../../components/Dropdown'
import { MINOR, MAJOR, CRITICAL } from '../../utils/reject-trans-util'
import { UNSPECIFIED } from './index'

/**
 * A Local Editor Dropdown coponent that selects the Criteria
 * for a translation rejection
 */
class CriteriaDropdown extends Component {
  static propTypes = {
    criteriaList: PropTypes.arrayOf(PropTypes.shape({
      editable: PropTypes.bool.isRequired,
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
    this.props.onCriteriaChange(event)
    this.toggleDropdown()
  }
  onUnspecifiedCriteria = () => {
    this.props.onUnspecifiedCriteria()
    this.toggleDropdown()
  }
  render () {
    const { criteriaList, criteriaDescription } = this.props
    const options = criteriaList.map((value, index) => {
      return (
        <li key={index + 1}
          className='EditorDropdown-item'
          onClick={this.onCriteriaChange}>
          {value.description}
        </li>
      )
    })
    // FIXME: should not be modifying the options array
    options.unshift(
      <li key={0}
        className='EditorDropdown-item'
        onClick={this.onUnspecifiedCriteria}>
        {UNSPECIFIED}
      </li>
    )
    return (
      <Dropdown enabled isOpen={this.state.dropdownOpen}
        onToggle={this.toggleDropdown}
        className='dropdown-menu Criteria'>
        <Dropdown.Button>
          <a className='EditorDropdown-item'>
            {criteriaDescription}
            <Icon className='n1 u-pullRight' name='chevron-down' />
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
