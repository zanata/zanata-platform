import * as React from 'react'
import { storiesOf } from '@storybook/react'
import { Label, Well, Table } from 'react-bootstrap'

storiesOf('Label', module)
  .add('in headings', () => (
    <span>
         <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Labels in headings</h2>
          <Well bsSize="large">Use labels to highlight information.</Well>
          <h1>Label <Label>New</Label></h1>
      <h2>Label <Label>New</Label></h2>
      <h3>Label <Label>New</Label></h3>
      <h4>Label <Label>New</Label></h4>
      <h5>Label <Label>New</Label></h5>
      <p>Label <Label>New</Label></p>
          <hr />
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
            <td>'label'</td>
            <td>Base CSS class and prefix for the component. Generally one should only change bsClass to provide new, non-Bootstrap, CSS styles for a component.</td>
          </tr>
           <tr>
            <td>bsStyle</td>
             <td>one of: <code>"success"</code>, <code>"warning"</code>, <code>"danger"</code>, <code>"info"</code>, <code>"default"</code>, <code>"primary"</code>, <code>"link"</code></td>
            <td>'default'</td>
            <td>Component visual or contextual style variants.</td>
           </tr>
          </tbody>
      </Table>
    </span>
  ))

  .add('validation states', () => (
    <span>
      <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Label validation states</h2>
      <Label bsStyle='default'>Default</Label>
                <p><code>bsStyle="default"</code></p>

      <Label bsStyle='primary'>Primary</Label>
                <p><code>bsStyle="primary"</code></p>

      <Label bsStyle='success'>Success</Label>
                <p><code>bsStyle="success"</code></p>

      <Label bsStyle='warning'>Warning</Label>
          <p><code>bsStyle="warning"</code></p>

      <Label bsStyle='danger'>Danger</Label>
              <p><code>bsStyle="danger"</code></p>

    </span>
  ))
