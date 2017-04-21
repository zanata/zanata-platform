import React from 'react'
import { storiesOf, action } from '@kadira/storybook'
import { Badge, Nav, NavItem, Button } from 'react-bootstrap'

storiesOf('Badge', module)
    .add('default', () => (
        <p>Badge <Badge>23</Badge></p>
    ))
    .add('in nav item', () => (
        <Nav bsStyle='pills' stacked className='sg-nav-pills'>
          <NavItem className='active'>
            <Badge className='pull-right'>42</Badge>
            Home
          </NavItem>
          <NavItem>Profile</NavItem>
          <NavItem>
            <Badge className='pull-right'>3</Badge>
            Messages
          </NavItem>
        </Nav>
    ))
    .add('in button', () => (
        <Button bsStyle='primary' type='button'>
          Messages <Badge>4</Badge>
        </Button>
    ))
