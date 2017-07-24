import React, {Component} from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@kadira/storybook'
import RealTriCheckbox from '.'
import { Table, ListGroup, ListGroupItem } from 'react-bootstrap'
import { find } from 'lodash'

class TriCheckbox extends Component {
  static propTypes = {
    checked: PropTypes.bool.isRequired,
    indeterminate: PropTypes.bool.isRequired,
    onClick: PropTypes.func.isRequired
  }
  static defaultProps = {
    checked: false,
    indeterminate: false,
    onClick: () => {}
  }
  constructor (props) {
    super(props)
    this.state = {
      checked: this.props.checked,
      indeterminate: this.props.indeterminate
    }
  }

  onClick = (event) => {
    this.setState({checked: event.target.checked, indeterminate: false})
    this.props.onClick(event)
  }

  makeIndeterminate = (event) => {
    this.setState({checked: false, indeterminate: true})
    this.props.onClick(event)
  }
  render () {
    return (
      <div>
        <RealTriCheckbox
          checked={this.state.checked}
          indeterminate={this.state.indeterminate}
          onChange={this.onClick}
        />
        <button onClick={this.makeIndeterminate}>Make indeterminate</button>
        <h2>Truth Table</h2>
        <Table striped bordered condensed hover><tbody>
          <tr>
            <th>checked</th>
            <th>intermediate</th>
            <th>expected</th>
            <th>appearance</th>
          </tr>
          <tr>
            <td>false</td>
            <td>false</td>
            <td>unchecked</td>
            <td>
              <RealTriCheckbox
                checked={false}
                indeterminate={false}
                onChange={this.onClick}
              />
            </td>
          </tr>
          <tr>
            <td>false</td>
            <td>true</td>
            <td>indeterminate</td>
            <td>
              <RealTriCheckbox
                checked={false}
                indeterminate
                onChange={this.onClick}
              />
            </td>
          </tr>
          <tr>
            <td>true</td>
            <td>false</td>
            <td>checked</td>
            <td>
              <RealTriCheckbox
                checked
                indeterminate={false}
                onChange={this.onClick}
              />
            </td>
          </tr>
          <tr>
            <td>true</td>
            <td>true</td>
            <td>intermediate</td>
            <td>
              <RealTriCheckbox
                checked
                indeterminate
                onChange={this.onClick}
              />
            </td>
          </tr>
        </tbody></Table>
      </div>
    )
  }
}

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
      this.setState((prevState, props) => ({
        checked: true,
        indeterminate: false,
        options: props.options.map((value) => {
          value.checked = true
          return value
        })
      }))
    } else {
      this.setState((prevState, props) => ({
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
    this.setState((prevState, props) => ({
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
        <ListGroupItem>
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
      <div>
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
      </div>
    )
  }
}

storiesOf('TriCheckbox', module)
  .add('default', () => (
    <TriCheckbox onClick={action('onClick')} />
  ))
  .add('checkboxgroup', () => (
    <TriCheckboxGroup
      options={[
        {checked: false, name: 'リンゴ'},
        {checked: false, name: 'apple'},
        {checked: false, name: '梨'},
        {checked: false, name: 'pear'}
      ]}
      onClick={action('onClick')} />
  ))
