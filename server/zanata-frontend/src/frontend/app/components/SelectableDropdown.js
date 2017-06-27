import React, {Component} from 'react'
import PropTypes from 'prop-types'
import {MenuItem, DropdownButton} from 'react-bootstrap'
import {isEqual} from 'lodash'

/**
 * Root component of a selectable Dropdown
 */
const SelectableDropdown = (props) => {
  const {
    id,
    onSelectDropdownItem,
    selectedValue,
    title,
    values,
    valueToDisplay,
    bsStyle,
    bsSize
  } = props
  const items = values.map((v, index) => {
    return (
      <DropdownMenuItem onSelect={onSelectDropdownItem}
        value={v} valueToDisplay={valueToDisplay}
        isSelected={isEqual(selectedValue, v)}
        key={index}
      />
    )
  })
  const selection = selectedValue && valueToDisplay(selectedValue)
  const titleValue = title || selection || ''
  return (
    <DropdownButton id={id} bsStyle={bsStyle}
      bsSize={bsSize} title={titleValue}>
      {items}
    </DropdownButton>
  )
}
SelectableDropdown.propTypes = {
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
SelectableDropdown.defaultProps = {
  bsStyle: 'default',
  bsSize: 'small',
  valueToDisplay: v => v
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
  static defaultProps = {
    valueToDisplay: v => v
  }
  onClick = () => {
    this.props.onSelect(this.props.value)
  }
  render () {
    const {value, isSelected, valueToDisplay} = this.props
    const display = valueToDisplay(value)
    return (
      <MenuItem onClick={this.onClick} active={isSelected}>
        {display}
      </MenuItem>
    )
  }
}

export default SelectableDropdown
