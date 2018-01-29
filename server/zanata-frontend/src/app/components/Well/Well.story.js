import * as React from 'react'
import { storiesOf } from '@storybook/react'
import { Well, Table } from 'react-bootstrap'

storiesOf('Well', module)
    .add('default', () => (
        <span>
          <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Well</h2>
          <Well bsSize="large">Use the well as a simple effect on an element to give it an inset effect.
          </Well>
          <Well>Look I'm in a well! <a href='#'>I am a link
            in a well!</a></Well>
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
            <td>'btn'</td>
            <td>Base CSS class and prefix for the component. Generally one should only change bsClass to provide new, non-Bootstrap, CSS styles for a component.</td>
          </tr>
           <tr>
            <td>bsSize</td>
             <td>one of: <code>"lg"</code>, <code>"large"</code>, <code>"sm"</code>, <code>"small"</code>, <code>"xs"</code>, <code>"xsmall"</code></td>
            <td></td>
            <td>Component size variations</td>
          </tr>
          </tbody>
          </Table>
        </span>
    ))
    .add('small', () => (
        <span>
          <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Well - small</h2>
        <Well bsSize='small'>Look I'm in a small well!</Well>
        <p><code>bsSize="small"</code></p>
        </span>

    ))

    .add('large', () => (
        <span>
          <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Well - large</h2>
        <Well bsSize='large'>Look I'm in a large well!</Well>
        <p><code>bsSize="large"</code></p>
        </span>

))
