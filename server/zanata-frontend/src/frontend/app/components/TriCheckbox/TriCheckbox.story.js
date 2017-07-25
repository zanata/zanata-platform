import React, {Component} from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@kadira/storybook'
import RealTriCheckbox from '.'
import { Table, ListGroup, ListGroupItem } from 'react-bootstrap'
import { find } from 'lodash'
import './index.css'

class TriCheckbox extends Component {
  static propTypes = {
    checked: PropTypes.bool.isRequired,
    indeterminate: PropTypes.bool.isRequired
  }
  constructor (props) {
    super(props)
    this.state = {
      checked: this.props.checked,
      indeterminate: this.props.indeterminate
    }
  }

  checkboxChanged = (event) => {
    this.setState({checked: event.target.checked, indeterminate: false})
  }

  render () {
    return (
      <div>
        <RealTriCheckbox
          checked={this.state.checked}
          indeterminate={this.state.indeterminate}
          onChange={this.checkboxChanged}
        />
      </div>
    )
  }
}

class WithButton extends Component {
  static propTypes = {
    checked: PropTypes.bool.isRequired,
    indeterminate: PropTypes.bool.isRequired
  }
  constructor (props) {
    super(props)
    this.state = {
      checked: this.props.checked,
      indeterminate: this.props.indeterminate
    }
  }

  makeIndeterminate = (event) => {
    this.setState({checked: false, indeterminate: true})
  }

  checkboxChanged = (event) => {
    this.setState({checked: event.target.checked, indeterminate: false})
  }

  render () {
    return (
      <div>
        <RealTriCheckbox
          checked={this.state.checked}
          indeterminate={this.state.indeterminate}
          onChange={this.checkboxChanged}
        />
        <button onClick={this.makeIndeterminate}>Make intermediate</button>
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
    <div>
      <h2>Setting Intermediate</h2>
      <WithButton checked={false} indeterminate={false} />
    </div>
  ))
  .add('truth table', () => (
    <div>
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
            <TriCheckbox
              checked={false}
              indeterminate={false}
            />
          </td>
        </tr>
        <tr>
          <td>false</td>
          <td>true</td>
          <td>indeterminate</td>
          <td>
            <TriCheckbox
              checked={false}
              indeterminate
            />
          </td>
        </tr>
        <tr>
          <td>true</td>
          <td>false</td>
          <td>checked</td>
          <td>
            <TriCheckbox
              checked
              indeterminate={false}
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
            />
          </td>
        </tr>
      </tbody></Table>
    </div>
  ))
  .add('checkbox group', () => (
    <TriCheckboxGroup
      options={[
        {checked: false, name: 'リンゴ'},
        {checked: false, name: 'apple'},
        {checked: false, name: '梨'},
        {checked: false, name: 'pear'}
      ]}
      onClick={action('onClick')} />
  ))
  .add('with custom styles', () => (
    <div>
      <h2> Passing custom styles through props </h2>
      <TriCheckbox
        checked
        indeterminate={false}
        onChange={action('onChange')}
        className={'tri-checkbox-story'}
      />
      <TriCheckbox
        checked={false}
        indeterminate
        onChange={action('onChange')}
        className={'tri-checkbox-story'}
      />
      <TriCheckbox
        checked={false}
        indeterminate={false}
        onChange={action('onChange')}
        className={'tri-checkbox-story'}
      />
    </div>
  ))
