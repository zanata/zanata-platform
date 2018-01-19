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
    criteria: PropTypes.string.isRequired
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
    const { criteria } = this.props
    return (
      <Dropdown enabled isOpen={this.state.dropdownOpen}
        onToggle={this.toggleDropdown}
        className="dropdown-menu Criteria">
        <Dropdown.Button>
          <a className="EditorDropdown-item">
            {criteria}
            <Icon className="n1" name="chevron-down" />
          </a>
        </Dropdown.Button>
        <Dropdown.Content>
          <ul>
            <li className="EditorDropdown-item" onClick={this.toggleDropdown}>
            Translation Errors (terminology, mistranslated, addition, omission, un-localized, do not translate, etc)</li>
            <li className="EditorDropdown-item" onClick={this.toggleDropdown}>
              Language Quality (grammar, spelling, punctuation, typo, ambiguous wording, product name, sentence structuring,
              readability, word choice, not natural, too literal, style and tone, etc)
            </li>
            <li className="EditorDropdown-item" onClick={this.toggleDropdown}>
            Consistency (inconsistent style or vocabulary, brand inconsistency, etc.)</li>
            <li className="EditorDropdown-item" onClick={this.toggleDropdown}>Style Guide & Glossary Violations</li>
            <li className="EditorDropdown-item" onClick={this.toggleDropdown}>
            Format (mismatches, white-spaces, tag error or missing, special character, numeric format, truncated, etc.)</li>
            <li className="EditorDropdown-item" onClick={this.toggleDropdown}>
            Other (reason may be in comment section/history if necessary)</li>
          </ul>
        </Dropdown.Content>
      </Dropdown>
    )
  }
}
export default CriteriaDropdown
