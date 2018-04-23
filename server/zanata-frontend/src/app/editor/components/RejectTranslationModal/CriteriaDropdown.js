import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import {
  MINOR, MAJOR, CRITICAL, UNSPECIFIED
} from '../../utils/reject-trans-util'
import Select from 'antd/lib/select'
import 'antd/lib/select/style/index.less'

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

  render () {
    const {
      criteriaList, criteriaDescription, onUnspecifiedCriteria, onCriteriaChange
    } = this.props
    const options = criteriaList.map((value, index) => {
      return (
        <Select.Option key={index}>
          {value.description}
        </Select.Option>
      )
    })
    const handleChange = (value) => {
      if (criteriaList[value].description === UNSPECIFIED.description) {
        onUnspecifiedCriteria()
      } else {
        onCriteriaChange(criteriaList[value])
      }
    }
    return (
      <Select
        defaultValue={criteriaDescription}
        style={{ width: '100%' }}
        onChange={handleChange}>
        {options}
      </Select>
    )
  }
}
export default CriteriaDropdown
