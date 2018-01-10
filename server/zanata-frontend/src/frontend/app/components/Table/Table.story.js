import React from 'react'
import { storiesOf } from '@storybook/react'
import { Table, Well } from 'react-bootstrap'

storiesOf('Table', module)
    .add('default', () => (
        <span>
          <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Table</h2>
          <Well bsSize="large">Use tables to display data - not for layouts. Use the <code>striped</code>, <code>bordered</code>, <code>condensed</code> and <code>hover</code> props to customise the table.</Well>
        <Table striped bordered condensed hover>
          <thead>
          <tr>
            <th>#</th>
            <th>First Name</th>
            <th>Last Name</th>
            <th>Username</th>
          </tr>
          </thead>
          <tbody>
          <tr>
            <td>1</td>
            <td>Mark</td>
            <td>Otto</td>
            <td>@mdo</td>
          </tr>
          <tr>
            <td>2</td>
            <td>Jacob</td>
            <td>Thornton</td>
            <td>@fat</td>
          </tr>
          <tr>
            <td>3</td>
            <td colSpan='2'>Larry the Bird</td>
            <td>@twitter</td>
          </tr>
          </tbody>
        </Table>
          <h3>Responsive tables</h3>
          <p>Add <code>responsive</code> prop to make them scroll horizontally up to small devices (under 768px). When viewing on anything larger than 768px wide, you will not see any difference in these tables.</p>
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
            <td>bordered</td>
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
            <td>condensed</td>
            <td>boolean</td>
            <td>false</td>
            <td></td>
          </tr>
                     <tr>
            <td>hover</td>
            <td>boolean</td>
            <td>false</td>
            <td></td>
          </tr>
                     <tr>
            <td>responsive</td>
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
