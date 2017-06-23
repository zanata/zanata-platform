import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {MenuItem, DropdownButton} from 'react-bootstrap'

/**
 * Root component of TM Match Percentage Dropdown
 */
class TMMatchPercentageDropdown extends Component {
  static propTypes = {
    /* params: percentage */
    selectPercentage: PropTypes.func.isRequired,
    matchPercentage: PropTypes.number.isRequired
  }
  render () {
    const {
      selectPercentage,
      matchPercentage
    } = this.props
    const percentageItems = [100, 90, 80].map(percentage => {
      return (
        <TMMatchPercentageItem
          selectPercentage={selectPercentage}
          percentage={percentage}
          matchPercentage={matchPercentage}
          key={percentage} />
      )
    })
    return (
      <DropdownButton bsStyle='default' bsSize='small'
        title={matchPercentage + '%'}
        id='dropdown-basic'
        className='vmerge-ddown'>
        {percentageItems}
      </DropdownButton>
    )
  }
}

/**
 * Sub-component of TM Match Percentage Dropdown
 * Handles behavior of percentage menu items
 */
class TMMatchPercentageItem extends Component {
  static propTypes = {
    percentage: PropTypes.number.isRequired,
    selectPercentage: PropTypes.func.isRequired,
    matchPercentage: PropTypes.number.isRequired
  }
  selectPercentage = () => {
    this.props.selectPercentage(this.props.percentage)
  }
  render () {
    const {
      percentage,
      matchPercentage
    } = this.props
    return (
      <MenuItem onClick={this.selectPercentage}
        eventKey={percentage} key={percentage}
        active={percentage === matchPercentage}>
        {percentage}%
      </MenuItem>
    )
  }
}

export default TMMatchPercentageDropdown
