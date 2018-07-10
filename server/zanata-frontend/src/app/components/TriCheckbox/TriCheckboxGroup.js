// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import RealTriCheckbox from '.'
import { ListGroup, ListGroupItem } from 'react-bootstrap'
import { find } from 'lodash'

class TriCheckboxGroup extends Component {
  static propTypes = {
    checked: PropTypes.bool.isRequired,
    indeterminate: PropTypes.bool.isRequired,
    options: PropTypes.arrayOf(PropTypes.shape({
      checked: PropTypes.bool,
      name: PropTypes.string
    })),
    onClick: PropTypes.func.isRequired
  }
  static defaultProps = {
    checked: false,
    indeterminate: false,
    options: [{checked: false, name: 'nothing'}],
    onClick: () => {
    }
  }

  constructor (props) {
    super(props)
    this.state = {
      checked: this.props.checked,
      indeterminate: this.props.indeterminate,
      options: this.props.options
    }
  }

  masterCheckBoxClick = (event) => {
    event.persist()
    if (event.target.checked) {
      this.setState((_prevState, props) => ({
        checked: true,
        indeterminate: false,
        options: props.options.map((value) => {
          value.checked = true
          return value
        })
      }))
    } else {
      this.setState((_prevState, props) => ({
        checked: false,
        indeterminate: false,
        options: props.options.map((value) => {
          value.checked = false
          return value
        })
      }))
    }
    this.props.onClick(event)
  }

  subCheckboxClick = (event) => {
    event.persist()
    this.setState((_prevState, props) => ({
      options: props.options.map((value) => {
        if (event.target.name === value.name) {
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

  render () {
    const optionGroup = this.props.options.map((value, index) => {
      return (
        <ListGroupItem key={index}>
          <RealTriCheckbox
            name={value.name}
            checked={this.state.options[index].checked}
            indeterminate={false}
            onChange={this.subCheckboxClick}
          />
          {value.name}
        </ListGroupItem>
      )
    })
    return (
      <React.Fragment>
        <h3>Checkbox group with tri-state control</h3>
        <ListGroup>
          <ListGroupItem>
            <RealTriCheckbox
              checked={this.state.checked}
              indeterminate={this.state.indeterminate}
              onChange={this.masterCheckBoxClick}
            />
            <label>Fruits</label>
          </ListGroupItem>
          {optionGroup}
        </ListGroup>
      </React.Fragment>
    )
  }
}

export default TriCheckboxGroup
