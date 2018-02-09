import React from 'react'
import {storiesOf} from '@storybook/react'
import {Badge, Nav, NavItem, Button, Table, Well} from 'react-bootstrap'

storiesOf('Badge', module)
    .add('default', () => (
        <span>
        <h2><img src="https://i.imgur.com/v4qLk4p.png" width="42px" />Badge</h2>
          <Well bsSize="large">Highlight new or unread items, numbers of members or any other numerical value.</Well>
        <p>Badge <Badge>23</Badge></p>
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
            <td>'badge'</td>
            <td>Base CSS class and prefix for the component. Generally one should only change bsClass to provide new, non-Bootstrap, CSS styles for a component.</td>
          </tr>
           <tr>
            <td>pullRight</td>
            <td>boolean</td>
            <td>false</td>
            <td>Component visual or contextual style variants.</td>
          </tr>
          </tbody>
          </Table>
        </span>
    ))
    .add('in nav item', () => (
        <span>
          <h2>Sidebar navigation</h2>
          <Well bsSize="large">Use this navigation layout with the project version <code>Sidebar</code>.</Well>
        <p><Nav bsStyle='pills' stacked className='sg-nav-pills'>
          <NavItem className='active'>
            <Badge className='u-pullRight'>42</Badge>
            Home
          </NavItem>
          <NavItem>Profile</NavItem>
          <NavItem>
            <Badge className='u-pullRight'>3</Badge>
            Messages
          </NavItem>
        </Nav></p>
          <hr />
          <h3>Related components</h3>
          <p><code>Sidebar</code>, <code>Tabs</code></p>
        </span>
    ))
    .add('in button', () => (
        <Button bsStyle='primary' type='button'>
          Messages <Badge>4</Badge>
        </Button>
    ))
