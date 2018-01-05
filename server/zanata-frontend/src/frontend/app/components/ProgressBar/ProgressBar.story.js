import React from 'react'
import { storiesOf } from '@storybook/react'
import { ProgressBar, Well, Table } from 'react-bootstrap'

const now = 60

storiesOf('ProgressBar', module)
    .add('default', () => (
        <span>
          <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Progress bar</h2>
          <Well bsSize="large">Provide up-to-date feedback on the progress of a workflow or action with simple yet flexible progress bars.</Well>
          <p>Add a <code>label</code> prop to show a visible percentage. For low percentages, consider adding a min-width to ensure the label's text is fully visible.</p>
          <ProgressBar now={now} label={`${now}%`} />
          <p>Add a <code>srOnly</code> prop to hide the label visually.</p>
          <ProgressBar now={60} />
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
            <td>active</td>
            <td>boolean</td>
            <td>false</td>
            <td></td>
          </tr>
           <tr>
            <td>bsClass</td>
            <td>string</td>
            <td>'progress-bar'</td>
            <td>Base CSS class and prefix for the component. Generally one should only change bsClass to provide new, non-Bootstrap, CSS styles for a component.</td>
          </tr>
           <tr>
            <td>bsStyle</td>
             <td>one of: <code>"success"</code>, <code>"warning"</code>, <code>"danger"</code>, <code>"info"</code>, <code>"default"</code>, <code>"primary"</code>, <code>"link"</code></td>
            <td>'default'</td>
            <td>Component visual or contextual style variants.</td>
           </tr>
           <tr>
            <td>children</td>
            <td>onlyProgressBar</td>
            <td></td>
            <td></td>
          </tr>
           <tr>
            <td>label</td>
            <td>node</td>
            <td></td>
            <td></td>
          </tr>
           <tr>
            <td>max</td>
            <td>number</td>
            <td>100</td>
            <td></td>
          </tr>
           <tr>
            <td>min</td>
            <td>number</td>
            <td>0</td>
            <td></td>
          </tr>
           <tr>
            <td>now</td>
            <td>number</td>
            <td></td>
            <td></td>
          </tr>
           <tr>
            <td>srOnly</td>
            <td>boolean</td>
            <td>false</td>
            <td></td>
          </tr>
           <tr>
            <td>striped</td>
            <td>boolean</td>
            <td>false</td>
            <td></td>
          </tr>
          </tbody>
          </Table>
        </span>
    ))

    .add('translation states', () => (
        <span>
          <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Progress bars - translation states</h2>
          <Well bsSize="large">The boostrap style classes have been used for the translation progress bars and <code>bsStyle</code> props are needed.</Well>
          Translated
          <ProgressBar bsStyle='success' now={40} />
          <p><code>bsStyle='success'</code></p>
          Approved
          <ProgressBar bsStyle='info' now={20} />
          <p><code>bsStyle='success'</code></p>
          Needs Work
          <ProgressBar bsStyle='warning' now={60} />
          <p><code>bsStyle='warning'</code></p>
          Rejected
          <ProgressBar bsStyle='danger' now={80} />
          <p><code>bsStyle='danger'</code></p>
        </span>
    ))

    .add('stacked', () => (
        <span>
          <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Progress bars - stacked</h2>
        <Well bsSize="large">Nest <code>ProgressBar</code>s to stack them.</Well>
        <ProgressBar>
          <ProgressBar striped bsStyle='success' now={35} key={1} />
          <ProgressBar bsStyle='warning' now={20} key={2} />
          <ProgressBar active bsStyle='danger' now={10} key={3} />
        </ProgressBar>
        </span>
    ))
