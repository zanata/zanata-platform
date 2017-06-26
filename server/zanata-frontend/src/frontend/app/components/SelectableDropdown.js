import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {MenuItem, DropdownButton} from 'react-bootstrap'

/**
 * Root component of a selectable Dropdown
 */
class SelectableDropdown extends Component {
  static propTypes = {
    id: PropTypes.string.isRequired,
    onSelectDropdownItem: PropTypes.func.isRequired,
    selectedValue: PropTypes.any,
    values: PropTypes.arrayOf(PropTypes.any).isRequired,
    // optinal function to convert value to display string
    valueToDisplay: PropTypes.func,
    title: PropTypes.string,
    bsStyle: PropTypes.string,
    bsSize: PropTypes.string
  }
  render () {
    const {
      id,
      onSelectDropdownItem,
      selectedValue,
      title,
      values,
      valueToDisplay,
      bsStyle,
      bsSize
    } = this.props
    const items = values.map((v, index) => {
      return (
        <DropdownMenuItem onSelect={onSelectDropdownItem}
          value={v} valueToDisplay={valueToDisplay}
          isSelected={selectedValue === v}
          key={index} eventKey={index + 1}
        />
      )
    })
    return (
      <DropdownButton id={id} bsStyle={bsStyle || 'default'}
        bsSize={bsSize || 'small'} title={title || selectedValue || ''}>
        {items}
      </DropdownButton>
    )
  }
}

/**
 * Sub-component of Dropdown menu item.
 * Handles behavior of menu items
 */
class DropdownMenuItem extends Component {
  static propTypes = {
    value: PropTypes.any.isRequired,
    onSelect: PropTypes.func.isRequired,
    isSelected: PropTypes.bool.isRequired,
    valueToDisplay: PropTypes.func
  }
  onClick = () => {
    this.props.onSelect(this.props.value)
  }
  render () {
    const {value, isSelected, valueToDisplay} = this.props
    const display = valueToDisplay ? valueToDisplay(value) : value
    return (
      <MenuItem onClick={this.onClick} active={isSelected}>
        {display}
      </MenuItem>
    )
  }
}

export default SelectableDropdown
