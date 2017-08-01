import React, {Component} from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@kadira/storybook'
import RealTriCheckbox from '.'
import TriCheckboxGroup from './TriCheckboxGroup'
import { Table, Col } from 'react-bootstrap'

class TriCheckbox extends Component {
  static propTypes = {
    className: PropTypes.string,
    checked: PropTypes.bool.isRequired,
    indeterminate: PropTypes.bool.isRequired,
    onChange: PropTypes.func.isRequired,
    useDefaultStyle: PropTypes.bool.isRequired
  }
  constructor (props) {
    super(props)
    this.state = {
      checked: this.props.checked,
      indeterminate: this.props.indeterminate,
      useDefaultStyle: this.props.useDefaultStyle
    }
  }

  checkboxChanged = (event) => {
    this.setState({checked: event.target.checked, indeterminate: false})
    this.props.onChange(event)
  }

  render () {
    return (
      <div>
        <RealTriCheckbox
          className={this.props.className}
          checked={this.state.checked}
          indeterminate={this.state.indeterminate}
          onChange={this.checkboxChanged}
          useDefaultStyle={this.state.useDefaultStyle}
        />
      </div>
    )
  }
}

class WithButton extends Component {
  static propTypes = {
    checked: PropTypes.bool.isRequired,
    indeterminate: PropTypes.bool.isRequired,
    onChange: PropTypes.func.isRequired
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
    this.props.onChange(event)
  }

  render () {
    return (
      <div>
        <RealTriCheckbox
          checked={this.state.checked}
          indeterminate={this.state.indeterminate}
          onChange={this.checkboxChanged}
          useDefaultStyle={false}
        />
        <button onClick={this.makeIndeterminate}>Make intermediate</button>
      </div>
    )
  }
}

storiesOf('TriCheckbox', module)
  .add('default', () => (
    <div>
      <h2>Setting Intermediate</h2>
      <WithButton
        checked={false}
        indeterminate={false}
        onChange={action('onChange')}
        />
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
              onChange={action('onChange')}
              useDefaultStyle={false}
            />
          </td>
        </tr>
        <tr>
          <td>false</td>
          <td>true</td>
          <td>intermediate</td>
          <td>
            <TriCheckbox
              checked={false}
              indeterminate
              onChange={action('onChange')}
              useDefaultStyle={false}
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
              onChange={action('onChange')}
              useDefaultStyle={false}
            />
          </td>
        </tr>
        <tr>
          <td>true</td>
          <td>true</td>
          <td>intermediate</td>
          <td>
            <TriCheckbox
              checked
              indeterminate
              onChange={action('onChange')}
              useDefaultStyle={false}
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
      <Col xs={4}>
        <h3>unstyled</h3>
        <TriCheckbox
          checked
          indeterminate={false}
          onChange={action('onChange')}
          useDefaultStyle={false}
        />
        <TriCheckbox
          checked={false}
          indeterminate
          onChange={action('onChange')}
          useDefaultStyle={false}
        />
        <TriCheckbox
          checked={false}
          indeterminate={false}
          onChange={action('onChange')}
          useDefaultStyle={false}
        />
      </Col>
      <Col xs={4}>
        <h3>default style</h3>
        <TriCheckbox
          checked
          indeterminate={false}
          onChange={action('onChange')}
        />
        <TriCheckbox
          checked={false}
          indeterminate
          onChange={action('onChange')}
        />
        <TriCheckbox
          checked={false}
          indeterminate={false}
          onChange={action('onChange')}
        />
      </Col>
      <Col xs={4}>
        <h3>s1 class</h3>
        <TriCheckbox
          className={'s1'}
          checked
          indeterminate={false}
          onChange={action('onChange')}
        />
        <TriCheckbox
          className={'s1'}
          checked={false}
          indeterminate
          onChange={action('onChange')}
        />
        <TriCheckbox
          className={'s1'}
          checked={false}
          indeterminate={false}
          onChange={action('onChange')}
        />
      </Col>
    </div>
  ))
