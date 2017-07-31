import React, { Component } from 'react'
import PropTypes from 'prop-types'
import TriCheckbox from '.'
import { SplitButton, MenuItem } from 'react-bootstrap'
import { find } from 'lodash'

class TriCheckboxDropdown extends Component {
  static propTypes = {
    title: PropTypes.string.isRequired,
    checked: PropTypes.bool.isRequired,
    indeterminate: PropTypes.bool.isRequired,
    open: PropTypes.bool.isRequired,
    options: PropTypes.arrayOf(PropTypes.shape({
      checked: PropTypes.bool,
      name: PropTypes.string
    })),
    onClick: PropTypes.func.isRequired
  }
  static defaultProps = {
    checked: false,
    indeterminate: false,
    open: false
  }

  constructor (props) {
    super(props)
    this.state = {
      checked: this.props.checked,
      indeterminate: this.props.indeterminate,
      options: this.props.options,
      open: this.props.open
    }
  }

  masterCheckBoxClick = (event) => {
    event.persist()
    const checked = !this.state.checked
    this.setState((prevState, props) => ({
      checked: checked,
      indeterminate: false,
      options: props.options.map((value) => {
        value.checked = checked
        return value
      })
    }))
    this.props.onClick(event)
  }

  subCheckboxClick = (event) => {
    event.persist()
    const name = event.target.name
    this.setState((prevState, props) => ({
      options: props.options.map((value) => {
        if (name === value.name) {
          value.checked = !value.checked
        }
        return value
      }),
      checked: props.options.every((option) => {
        return option.checked
      }),
      indeterminate: !(props.options.every((option) => {
        return option.checked
      })) && (find(props.options, (option) => {
        return option.checked
      }) !== undefined)
    }))
    this.props.onClick(event)
  }

  // Don't close the dropdown on menu select events
  preventClose = (open, s1, s2) => {
    if (s2 && s2.source !== 'select') {
      this.setState((prevState) => ({
        open: !prevState.open
      }))
    }
  }
  // Prevent default checkbox behavior
  preventDefault = (e) => {
    e.persist()
    e.stopPropagation()
    this.subCheckboxClick(e)
  }
  render () {
    const optionGroup = this.props.options.map((value, index) => {
      return (
        <MenuItem key={index} eventKey={index} onClick={this.subCheckboxClick}
          name={value.name}>
          <TriCheckbox
            name={value.name}
            checked={this.state.options[index].checked}
            indeterminate={false}
            onClick={this.preventDefault}
          /> {value.name}
        </MenuItem>
      )
    })
    return (
      <SplitButton bsStyle={'default'} open={this.state.open}
        onToggle={this.preventClose} value={this.state.checked}
        onClick={this.masterCheckBoxClick} id={'TriCheckbox'} title={
          <div>
            <TriCheckbox
              checked={this.state.checked}
              indeterminate={this.state.indeterminate}
              onChange={this.masterCheckBoxClick}
            /> {this.props.title}
          </div>
        }>
        {optionGroup}
      </SplitButton>
    )
  }
}

export default TriCheckboxDropdown
