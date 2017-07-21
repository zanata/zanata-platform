import React, {Component} from 'react'
import PropTypes from 'prop-types'
import { storiesOf, action } from '@kadira/storybook'
import RealTriCheckbox from '.'
import { Table } from 'react-bootstrap'

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

/*
 * TODO add stories showing the range of states
 *      for TriCheckbox
 */
storiesOf('TriCheckbox', module)
  .add('default', () => (
    <TriCheckbox onClick={action('onClick')} />
  ))
