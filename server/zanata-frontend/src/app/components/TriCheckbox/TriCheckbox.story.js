// @ts-nocheck
import React from 'react'
import { Component } from 'react'
import * as PropTypes from 'prop-types'
import { action } from '@storybook/addon-actions'
import { storiesOf } from '@storybook/react'
import RealTriCheckbox from '.'
import TriCheckboxGroup from './TriCheckboxGroup'
import { Table, Col, Well } from 'react-bootstrap'

class TriCheckbox extends Component {
  static propTypes = {
    className: PropTypes.string,
    checked: PropTypes.bool.isRequired,
    onChange: PropTypes.func.isRequired,
    // these 2 are .isRequired in the real component but have default values
    indeterminate: PropTypes.bool,
    useDefaultStyle: PropTypes.bool
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
      <React.Fragment>
        <RealTriCheckbox
          className={this.props.className}
          checked={this.state.checked}
          indeterminate={this.state.indeterminate}
          onChange={this.checkboxChanged}
          useDefaultStyle={this.state.useDefaultStyle}
        />
      </React.Fragment>
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
      <React.Fragment>
        <RealTriCheckbox
          checked={this.state.checked}
          indeterminate={this.state.indeterminate}
          onChange={this.checkboxChanged}
          useDefaultStyle={false}
        />
        <button onClick={this.makeIndeterminate}>Make intermediate</button>
      </React.Fragment>
    )
  }
}

storiesOf('TriCheckbox', module)
  .add('default', () => (
    <React.Fragment>
      <h2><img
          src="https://upload.wikimedia.org/wikipedia/commons/4/49/Zanata-Logo.svg"
          width="42px"/> TriCheckbox</h2>
      <Well bsSize="large">Checkbox with intermediate setting to indicate part of a list of checkbox items is selected.</Well>
      <h3>Setting Intermediate</h3>
      <WithButton
        checked={false}
        indeterminate={false}
        onChange={action('onChange')}
        />
    </React.Fragment>
  ))
  .add('truth table', () => (
    <React.Fragment>
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
    </React.Fragment>
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
    <React.Fragment>
      <h2>Passing custom styles through props</h2>
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
    </React.Fragment>
  ))
