import React, { Component } from 'react'
import { Icon } from '../../../components'
import PropTypes from 'prop-types'
import Dropdown from '../../components/Dropdown'

/* eslint-disable max-len */

/**
 * A Local Editor Dropdown coponent that selects the Criteria
 * for a translation rejection
 */
class CriteriaDropdown extends Component {
  static propTypes = {
    criteriaList: PropTypes.arrayOf(PropTypes.string).isRequired,
    onCriteriaChange: PropTypes.func.isRequired,
    selectedCriteria: PropTypes.string.isRequired
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
    const { onCriteriaChange, criteriaList, selectedCriteria } = this.props
    const options = criteriaList.map((value, index) => {
      return (
        <li key={index}
          className="EditorDropdown-item"
          onClick={onCriteriaChange}>
          {value}
        </li>
      )
    })
    return (
      <Dropdown enabled isOpen={this.state.dropdownOpen}
        onToggle={this.toggleDropdown}
        className="dropdown-menu Criteria">
        <Dropdown.Button>
          <a className="EditorDropdown-item">
            {selectedCriteria}
            <Icon className="n1" name="chevron-down" />
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
