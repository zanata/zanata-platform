import React from 'react'
import {storiesOf} from '@storybook/react'
import {Alert, Table, Well} from 'react-bootstrap'

storiesOf('Alert', module)
    .add('success', () => (
        <span>
          <h2>Alert</h2>
          <Well>Use this for the default alert overlay. In the case where feedback is needed from the user before dismissing the alert, use a <strong>Notification</strong>.</Well>
        <Alert bsStyle='success'>
          <strong>Holy guacamole!</strong> Best check yo self
        </Alert>
        <h3>Props</h3>
          <Table striped bordered condensed hover>
          <thead>
          <tr>
            <th>Name</th>
            <th>Type</th>
            <th>Default</th>
            <th>Description</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td>bsClass</td>
            <td>string</td>
            <td>'alert'</td>
            <td>Base CSS class and prefix for the component. Generally one should only change bsClass to provide new, non-Bootstrap, CSS styles for a component.</td>
          </tr>
           <tr>
            <td>bsStyle</td>
             <td>one of: <code>"success"</code>, <code>"warning"</code>, <code>"danger"</code>, <code>"info"</code></td>
            <td>'info'</td>
            <td>Component visual or contextual style variants.</td>
          </tr>
                   <tr>
            <td>closeLabel</td>
                     <td>string</td>
                     <td>'Close alert'</td>
            <td></td>
          </tr>
                   <tr>
            <td>onDismiss</td>
            <td>function</td>
            <td></td>
                     <td>For Closeable alerts pass the <code>onDismiss</code> function</td>
          </tr>
          </tbody>
          </Table>
            <h3>Related components</h3>
          <ul>
            <li>Notification</li>
          </ul>
        </span>
    ))
    .add('warning', () => (
        <Alert bsStyle='warning'>
          <strong>Holy guacamole!</strong> Best check yo self
        </Alert>
    ))
    .add('danger', () => (
        <Alert bsStyle='danger'>
          <strong>Holy guacamole!</strong> Best check yo self
        </Alert>
    ))
    .add('info', () => (
        <Alert bsStyle='info'>
          <strong>Holy guacamole!</strong> Best check yo self
        </Alert>
    ))
